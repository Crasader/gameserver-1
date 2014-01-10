package com.xinqihd.sns.gameserver.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceAskRoundOver.BceAskRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattlePickBox.BceBattlePickBox;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattleStageReady.BceBattleStageReady;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletTrack.BceBulletTrack;
import com.xinqihd.sns.gameserver.proto.XinqiBceDead.BceDead;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleChangeDirection.BceRoleChangeDirection;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleMove.BceRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBceRolePower.BceRolePower;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoundOver.BceRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleReward.BseBattleReward;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class BattleManager {
	
	public static final String BATTLE_HAS_REWARD_KEY = "battle_reward";
	public static final String BATTLE_ROOM_TYPE_KEY = "battle_roomtype";
	
	private static final Logger logger = LoggerFactory.getLogger(BattleManager.class);
	
	private ConcurrentHashMap<SessionKey, Battle> battleMap = new ConcurrentHashMap<SessionKey, Battle>();
	
	private String gameServerId = null;
	
	/**
	 * If true, the server will use distributed 
	 * method to calculate bullet track.
	 */
	private boolean useDistributed = false;
	
	private ThreadPoolExecutor distributeThreadPool = null;
	
	private int roundBulletTrackSeconds = 10000;
	
	
	/**
	 * The Battle's checker.
	 */
	private final Checker checker = new Checker();
	
	private static final BattleManager instance = new BattleManager();
	
	protected BattleManager() {
		//Get the remote JVM id.
		gameServerId = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_GAME_SERVERID);
		logger.debug("gameServerId: {}", gameServerId);
		useDistributed = GlobalConfig.getInstance().getBooleanProperty(
				GlobalConfigKey.battle_distributed);
		int coreSize = GlobalConfig.getInstance().getIntProperty(
				GlobalConfigKey.battle_pool_core_size);
		int maxSize = GlobalConfig.getInstance().getIntProperty(
				GlobalConfigKey.battle_pool_max_size);
		int keepAlive = GlobalConfig.getInstance().getIntProperty(
				GlobalConfigKey.battle_pool_keepalive_seconds);
		if ( useDistributed ) {
			distributeThreadPool = new ThreadPoolExecutor(coreSize, maxSize, keepAlive, 
					TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		}
		this.roundBulletTrackSeconds = GlobalConfig.getInstance().getIntProperty(
				GlobalConfigKey.battle_bullettrack_seconds);
		if ( this.roundBulletTrackSeconds < 10000 ) {
			this.roundBulletTrackSeconds = 10000;
			logger.debug("The roundBulletTimeout is {}", this.roundBulletTrackSeconds);
		}
	}

	/**
	 * Get the BattleManager instance.
	 */
	public static BattleManager getInstance() {
		return instance;
	}
	
	/**
	 * Schedule the room's checker's first run.
	 */
	public void init() {
		//Start the room's checker
		int seconds = GlobalConfig.getInstance().
				getIntProperty(GlobalConfigKey.battle_checker_seconds);
		//Start the battle's checker
		Thread checkerThread = new Thread(checker);
		checkerThread.setName("BattleManager");
		checkerThread.setDaemon(true);
		checkerThread.start();
		//GameContext.getInstance().scheduleTask(checker, seconds, TimeUnit.SECONDS);
		//logger.debug("BattleManager's checker will run after {} seconds.", seconds);
		logger.debug("BattleManager's checker starts.");
	}

	/**
	 * Do nothing
	 */
	public void destroy() {
		if ( useDistributed && distributeThreadPool != null ) {
			logger.debug("Shutdown battle manager's thread pool");
			distributeThreadPool.shutdownNow();
		}
	}

	/**
	 * Create and intialize a Battle using the given BattleRoom.
	 * 
	 * @param battleRoom
	 * @return
	 */
	public Battle battleBegin(BattleRoom battleRoom) {
		Battle battle = new Battle(battleRoom, gameServerId);
		synchronized (battle) {
			battle.battleBegin();
			battleMap.put(battleRoom.getSessionKey(), battle);
			//Save the battle in redis
			Jedis jedis = JedisFactory.getJedis();
			jedis.hset(battleRoom.getSessionKey().toString(), Constant.RPC_SERVER_KEY, gameServerId);
			logger.debug("Create battle {} in Redis", battle.getBattleRoom().getSessionKey().toString());
			return battle;
		}
	}
	
	/**
	 * This method will be callback from a Battle object when 
	 * the it is over.
	 * @param battle
	 */
	public void battleEnd(Battle battle) {
		logger.debug("Battle is end. ");
		Collection<BattleUser> battleUsers = battle.getBattleUserMap().values();
		synchronized (battle) {
			battle.battleEnd();
			//Clean the battle in redis
			Jedis jedis = JedisFactory.getJedis();
			String battleRoomKey = battle.getBattleRoom().getSessionKey().toString();
			jedis.del(battleRoomKey);
			logger.debug("Remove battle {} from Redis", battleRoomKey);
			//Remove the battle object from cache.
			this.battleMap.remove(battle.getBattleSessionKey());
			
			//Update Room's creation date
			Room roomLeft = battle.getBattleRoom().getRoomLeft();
			Room roomRight = battle.getBattleRoom().getRoomRight();
			/*
			if ( roomLeft.getRoomType() != RoomType.DESK_ROOM && 
					roomLeft.getRoomType() != RoomType.FRIEND_ROOM ) {
				RoomManager.getInstance().sendBseUserLeaveRoom(battle.getBattleRoom());
			}
			*/
			GameContext.getInstance().getRoomManager().resetRoom(roomLeft);
			GameContext.getInstance().getRoomManager().resetRoom(roomRight);
		}
	}
	
	/**
	 * This method will be callback from a Battle object when 
	 * the it is over.
	 * @param battle
	 */
	public void battleOver(Battle battle, BattleStatus status) {
		logger.debug("Battle is over. Result: {}", status);
		synchronized (battle) {
			battle.setStatus(status);
			battle.battleOver(status);
		}
	}
	
	/**
	 * This method will be callback from a Battle object when 
	 * the it is over.
	 * @param battle
	 */
	public void battleOverWithRoundOver(Battle battle, BattleStatus status) {
		logger.debug("Battle is over. Result: {}", status);
		synchronized (battle) {
			if ( battle.getRoundOwner() != null ) {
				battle.roundOverWithoutStart(battle.getRoundOwner().getUserSessionKey());
			} else {
				logger.debug("Battle owner is already null");
			}
			battle.setStatus(status);
			battle.battleOver(status);
		}
	}
	
	/**
	 * Call the stageReady with null request.
	 * @param userSessionKey
	 */
	public void stageReady(SessionKey userSessionKey) {
		stageReady(userSessionKey, null);
	}
	
	/**
	 * An user tells server that he/she is ready for combat round
	 * after he/she loaded the battle map and landed on ground.
	 * 
	 * @param userSessionKey
	 */
	public void stageReady(SessionKey userSessionKey, BceBattleStageReady stageReady) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( stageReady != null ) {
					String sessionId = stageReady.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.stageReady(movingSessionKey);
			}
		} else {
			logger.debug("#stageReady call remote BattleManager#stageReady for user {}: ", userSessionKey);
			BceBattleStageReady.Builder builder = BceBattleStageReady.newBuilder();
			proxyToRemoteGameServer(userSessionKey, builder.build());
		}
	}
	
	/**
	 * An user is moving.
	 * 
	 * @param userSessionKey
	 * @param move
	 */
	public void roleMove(SessionKey userSessionKey, BceRoleMove move) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( move != null ) {
					String sessionId = move.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.roleMove(movingSessionKey, move);
			}
		} else {
			logger.debug("#roleMove call remote BattleManager#stageReady for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, move);
		}
	}
	
	/**
	 * An user is attacking.
	 * 
	 * @param userSessionKey
	 * @param move
	 */
	public void roleAttack(SessionKey userSessionKey, BceRoleAttack attack) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( attack != null ) {
					String sessionId = attack.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.roleAttack(movingSessionKey, attack);
			}
		} else {
			logger.debug("#roleAttack call remote BattleManager#roleAttack for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, attack);
		}
	}
	
	/**
	 * An user is using power attack
	 * 
	 * @param userSessionKey
	 * @param move
	 */
	public void rolePower(SessionKey userSessionKey, BceRolePower power) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( power != null ) {
					String sessionId = power.getSessionID();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.rolePower(movingSessionKey, power);
			}
		} else {
			logger.debug("#rolePower call remote BattleManager#rolePower for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, power);
		}
	}
	
	/**
	 * An user is using an extra buff tool
	 * 
	 * @param userSessionKey
	 * @param move
	 */
	public void roleUseTool(SessionKey userSessionKey, BceRoleUseTool tool) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( tool != null ) {
					String sessionId = tool.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.roleUseTool(movingSessionKey, tool);
			}
		} else {
			logger.debug("#roleUseTool call remote BattleManager#roleUseTool for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, tool);
		}
	}
	
	/**
	 * An user tells server that his/her round is over.
	 * @param userSessionKey
	 * @param userAsked
	 */
	public void roundOver(SessionKey userSessionKey, BceAskRoundOver askRoundOver) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		boolean userAsked = askRoundOver != null;
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( askRoundOver != null ) {
					String sessionId = askRoundOver.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.roundOver(movingSessionKey, userAsked);
			}
		} else {
			logger.debug("#roundOver call remote BattleManager#getBattleReward for user {}: ", userSessionKey);
			Message message = null;
			if ( askRoundOver != null ) {
				message = askRoundOver;
			} else {
				BceRoundOver.Builder over = BceRoundOver.newBuilder();
				message = over.build();
			}
			proxyToRemoteGameServer(userSessionKey, message);
		}
	}
	
	/**
	 * User picks up battle rewards.
	 * This method should happen at localserver. Do not call rpc 
	 * of remote battle server.
	 * @param userSessionKey
	 * @param slot
	 */
	public void getBattleReward(final User user, final int slotSize) {
		BseBattleReward.Builder builder = BseBattleReward.newBuilder();
		boolean hasReward = RoleActionManager.getInstance().
				checkUserHasRoleActionPoint(user);
		Jedis jedis = JedisFactory.getJedis();
		List<Reward> rewardList = null; 
		if ( hasReward ) {
			hasReward = Boolean.parseBoolean(jedis.hget(user.getSessionKey().toString(), BATTLE_HAS_REWARD_KEY));
		}
		if ( hasReward ) {
			if ( user.getBattleRewards() == null ) {
				String roomTypeStr = jedis.hget(user.getSessionKey().toString(), BATTLE_ROOM_TYPE_KEY);
				RoomType roomType = null;
				if ( StringUtil.checkNotEmpty(roomTypeStr) ) {
					roomType = RoomType.valueOf(roomTypeStr);
				}
				if ( RoomType.PVE_ROOM == roomType ) {
					Boss boss = (Boss)user.getUserData(BossManager.USER_BOSS_ID);
					String script = boss.getBossPojo().getBattleRewardScript();
					if ( boss != null &&  script != null ) {
						ScriptHook hook = ScriptHook.getScriptHook(script);
						rewardList = RewardManager.getInstance().
								generateRewardsFromScript(user, slotSize, hook);
					} else {
						rewardList = RewardManager.getInstance().
								generateRewardsFromScript(user, slotSize, ScriptHook.PVE_BATTLE_REWARD);
					}
				} else {
					rewardList = RewardManager.getInstance().
						generateRewardsFromScript(user, slotSize, ScriptHook.BATTLE_REWARD);
				}
				for ( Reward reward : rewardList ) {
					builder.addPropID(reward.getPropId());
					builder.addPropCount(reward.getPropCount());
					builder.addPropLevel(reward.getPropLevel());
					builder.addType(reward.getType().index());
					//logger.debug("User {} reward {}, type:{} ", new Object[]{user.getRoleName(), reward.getPropId(), reward.getType()});
				}
				user.setBattleRewards(rewardList);
			} else {
				logger.info("User {} use old battle reward list");
			}
		} else {
			user.setBattleRewards(null);
			logger.debug("User {} has no BATTLE_HAS_REWARD_KEY in redis", user.getRoleName());
		}
		
		BseBattleReward response = builder.build();

		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
		/**
		 * It should be done at the client side
		 * wangqi 2012-08-21
		 */
		/**
		GameContext.getInstance().scheduleTask(new Runnable() {
			public void run() {
				Invite invite = user.getLastInvite();
				if ( invite != null ) {
					user.setLastInvite(null);
					ConfirmManager.getInstance().sendConfirmMessage(user, 
							invite.getMessage(), invite.getChallenge(), invite.getCallback());
					logger.debug("User {} received a pending invitation to confirm.", user.getRoleName());
				}				
			}
		}, 3, TimeUnit.SECONDS);
		*/
	}
	
	/**
	 * User picks up battle rewards
	 * This method should happen at localserver. Do not call rpc 
	 * of remote battle server.
	 * @param userSessionKey
	 * @param slot
	 */
	public void pickReward(User user, int[] slotIndex) {
		List<Reward> rewards = user.getBattleRewards();
		if ( rewards == null || rewards.size() == 0 ) {
			logger.warn("The battle may happened at remote server", user.getRoleName());
			return;
		}
		ArrayList<Reward> pickedReward = new ArrayList<Reward>();
		for ( int i=0; i<slotIndex.length; i++ ) {
			if ( slotIndex[i] < 0 || slotIndex[i]>=rewards.size() ) {
				logger.warn("User {} invalid slotIndex: {}", user.getRoleName(), slotIndex[i]);
				continue;
			}
			Reward reward = rewards.get(slotIndex[i]);
			logger.warn("User {}, slot: {}, reward: {}", new Object[]{
					user.getRoleName(), slotIndex[i], reward});
			if ( reward != null ) {
				pickedReward.add(reward);
			}
		}
		user.setBattleRewards(null);
		Jedis jedis = JedisFactory.getJedis();
		jedis.hdel(user.getSessionKey().toString(), BATTLE_HAS_REWARD_KEY);
		
		RewardManager.getInstance().pickReward(user, pickedReward, 
				StatAction.ProduceBattleReward);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
				UserActionKey.BattleReward);
	}
	
	/**
	 * A user leaves a battle. Normally it is called when the user becomes offline
	 * or the user picks up some reward. It is not a client request currently so 
	 * theoretically it does not need RPC interface.
	 * 
	 * 
	 * @param userSessionKey
	 */
	public void leaveBattle(SessionKey userSessionKey) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				battle.leaveBattle(userSessionKey);
			}
			logger.debug("user {} leaves a battle.", userSessionKey);
		}
	}
	
	/**
	 * An user picks treasure boxes randomly generated in combat.
	 * @param userSessionKey
	 * @param pickBox
	 */
	public void pickTreasureBox(SessionKey userSessionKey, BceBattlePickBox pickBox) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( pickBox != null ) {
					String sessionId = pickBox.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.pickTreasureBox(movingSessionKey, pickBox);
			}
		} else {
			logger.debug("#pickTreasureBox call remote BattleManager#pickTreasureBox for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, pickBox);
		}
	}
	
	/**
	 * Send a chat message to all users in battle
	 */
	public void sendChatToAllUsers(SessionKey userSessionKey, String message) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				battle.sendChatToAllUsers(userSessionKey, message);
			}
		}
	}
	
	/**
	 * Send a chat message to all users in battle
	 */
	public void sendVoiceChatToAllUsers(SessionKey userSessionKey, byte[] message, boolean autoplay, int seconds) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				battle.sendVoiceChatToAllUsers(userSessionKey, message, autoplay, seconds);
			}
		}
	}
	
	/**
	 * The user changes his/her direction
	 * 
	 * @param userSessionKey
	 * @param dir
	 */
	public void changeDirection(SessionKey userSessionKey, BceRoleChangeDirection roleDir) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( roleDir != null ) {
					String sessionId = roleDir.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				
				int dir = roleDir.getDirection();
				if ( dir >= 0 && dir < RoleDirection.values().length ) {
					RoleDirection rdir = RoleDirection.values()[dir];
					battle.changeDirection(movingSessionKey, rdir);
				} else {
					logger.debug("The RoleDirection value is invalid: {}", dir);
				}
			}
		} else {
			logger.debug("#pickTreasureBox call remote BattleManager#stageReady for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, roleDir);
		}
	}
	
	/**
	 * The client tells server an user is dead and the animation is done.
	 * @param userSessionKey
	 */
	public void roleDead(SessionKey userSessionKey, BceDead bceDead) {
		SessionKey targetSessionKey = userSessionKey;
		if ( bceDead != null ) {
			String sessionKeyStr = bceDead.getUserid();
			if (StringUtil.checkNotEmpty(sessionKeyStr)) {
				SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
				if ( sessionKey != null ) {
					targetSessionKey = sessionKey;
					logger.debug("#roleDead. targetSessionKey:{}", targetSessionKey);
				}
			}
		}
		
		Battle battle = findBattleByUserSessionKey(targetSessionKey);
		if ( battle != null ) {
			//The battle is managed by localhost
			synchronized (battle) {
				battle.roleDead(targetSessionKey);
				
				BattleStatus status = battle.checkBattleUsers();
				//Wait the client send BceDead to call BattleEnd
				if ( status != BattleStatus.NORMAL ) {
					this.battleOver(battle, status);
					this.battleEnd(battle);
					logger.debug("Battle {} is over: {}", battle.getBattleSessionKey(), status);
					return;
				} else {
					logger.debug("Battle {} is already over: {}", battle.getBattleSessionKey(), status);
				}
			}
		} else {
			logger.debug("#roleDead call remote BattleManager#stageReady for user {}: ", targetSessionKey);
			proxyToRemoteGameServer(userSessionKey, bceDead);
		}
	}
	
	/**
	 * The client sent server the calculated bullet track result
	 * @param userSessionKey
	 */
	public void bulletTrack(SessionKey userSessionKey, BulletTrack[] bulletTracks, 
			int roundNumber, BceBulletTrack bceBulletTrack) {
		Battle battle = findBattleByUserSessionKey(userSessionKey);
		if ( battle != null ) {
			synchronized (battle) {
				SessionKey movingSessionKey = userSessionKey;
				if ( bceBulletTrack != null ) {
					String sessionId = bceBulletTrack.getSessionId();
					if ( StringUtil.checkNotEmpty(sessionId) ) {
						movingSessionKey = SessionKey.createSessionKeyFromHexString(sessionId);
					}
				}
				battle.bulletTrack(movingSessionKey, bulletTracks, roundNumber);
			}
		} else {
			logger.debug("#bulletTrack call remote BattleManager#bulletTrack for user {}: ", userSessionKey);
			proxyToRemoteGameServer(userSessionKey, bceBulletTrack);
		}
	}
		
	/**
	 * Remove all battles in system.
	 */
	public void clearAllBattles() {
		this.battleMap.clear();
	}
	
	public Collection<Battle> findAllBattles() {
		return this.battleMap.values();
	}
	
	/**
	 * Find a battle's rpcserverid. If it does not exist ( it is a logic failure and 
	 * should not happen ), return null.
	 * @param userSessionKey
	 * @return
	 */
	public String findBattleGameServerId(SessionKey userSessionKey) {
		Jedis jedis = JedisFactory.getJedis();
		String gameServerId = jedis.hget(userSessionKey.toString(), Battle.BATTLE_SERVER_KEY);
		return gameServerId;
	}
	
	/**
	 * Find the Battle by its session key.
	 */
	public Battle findBattleByBattleSessionKey(SessionKey battleSessionKey) {
		return this.battleMap.get(battleSessionKey);
	}
	
	/**
	 * Find a local Battle object by the given User's SessionKey
	 * @param userSessionKey
	 * @return
	 */
	public Battle findBattleByUserSessionKey(SessionKey userSessionKey) {
		Battle battle = null;
		SessionKey battleSessionKey = findBattleSessionKeyByUserSessionKey(userSessionKey);
		if ( battleSessionKey != null ) {
			battle = this.battleMap.get(battleSessionKey);
			if ( battle == null ) {
				logger.warn("It cannot find battle session key {}'s battle in local JVM", battleSessionKey);
			}
		} else {
			logger.warn("It cannot find battle session key {}'s battle in Redis.", battleSessionKey);
		}
		return battle;
	}
	
	/**
	 * Find Battle's session key object by the given User's SessionKey
	 * @param userSessionKey
	 * @return battleSessionKey
	 */
	public SessionKey findBattleSessionKeyByUserSessionKey(SessionKey userSessionKey) {
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(userSessionKey.toString(), Battle.BATTLE_SESSION_KEY);
		if ( bytes != null ) {
			SessionKey battleSessionKey = SessionKey.createSessionKeyFromHexString(bytes);
			return battleSessionKey;
		} else {
			logger.debug("The user {} is not in a battle", userSessionKey);
		}
		return null;
	}
	
	/**
	 * @return the useDistributed
	 */
	public boolean isUseDistributed() {
		return useDistributed;
	}

	/**
	 * Submit a runnable task for asynchronous running
	 * @param runnable
	 */
	public void submitExecutor(Runnable runnable) {
		if ( this.isUseDistributed() ) {
			this.distributeThreadPool.submit(runnable);
		}
	}
	
	/**
	 * Proxy the request to remote game server.
	 * @param userSessionKey
	 * @param pbMessage
	 */
	private void proxyToRemoteGameServer(SessionKey userSessionKey, Message pbMessage) {
		String gameServerId = findBattleGameServerId(userSessionKey);
		if ( gameServerId != null ) {
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, pbMessage);
		} else {
			logger.info("#proxyToRemoteGameServer: failed to find user {}'s battle gameServerId", userSessionKey);
		}
	}

	/**
	 * @return the roundBulletTrackSeconds
	 */
	public int getRoundBulletTrackSeconds() {
		return roundBulletTrackSeconds;
	}

	/**
	 * @param roundBulletTrackSeconds the roundBulletTrackSeconds to set
	 */
	public void setRoundBulletTrackSeconds(int roundBulletTrackSeconds) {
		this.roundBulletTrackSeconds = roundBulletTrackSeconds;
	}

	/**
	 * The checker thread will check:
	 * 1. All rooms managed by this JVM.
	 * 2. If a room is in combat status, ignore it, because the BattleManager
	 *    will do the check.
	 * 3. If one or more users in a room are offline, kick them out.
	 * 4. TODO If the room is timeout, join AI users.  
	 * 
	 * @author wangqi
	 *
	 */
	public static class Checker implements Runnable {

		@Override
		public void run() {
			while ( true ) {
				//logger.info("#BattleManager.Checker runs...");
				// Get all rooms managed by this JVM 
				int battleCheckerMilliSeconds = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.BATTLE_CHECK_IDLE_MILLIS, 1000);
				int battleMaxLiveMilliSeconds = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.BATTLE_MAX_LIVE_MILLIS, 300000);
				
				Collection<Battle> battleCollection = BattleManager.getInstance().findAllBattles();
				//Copy the collection to avoid concurrent modification exception
				ArrayList<Battle> copyList = new ArrayList<Battle>(battleCollection);
				
	//			logger.debug("BattleManager's Checker starts to check total {} battles.", battleCollection.size());
				
				for ( Battle battle : copyList ) {
					try {
						if ( battle != null ) {
							synchronized (battle) {
								BattleStatus status = battle.getStatus();
								//Check if the battle need to be finished
								if ( status != BattleStatus.NORMAL ) {
									if ( battle.getBattleOverTimestamp() == Long.MAX_VALUE ) {
										//The battleOver is not called yet
										status = battle.checkBattleUsers();
										battle.setStatus(status);
										logger.debug("#Checker: Battle {}'s battleOver is not called yet. Call the battleEnd method", battle.getBattleSessionKey(), status);
										//Avoid to send the BattleOver message twice.
										BattleManager.getInstance().battleOverWithRoundOver(battle, status);
										BattleManager.getInstance().battleEnd(battle);
									} else {
										logger.debug("#Checker: Battle {} is already in {} status.", battle.getBattleSessionKey(), status);
									}
								} else {
									status = battle.checkBattleUsers();
									battle.setStatus(status);
									
									//Check the battle last time
									long battleCreatedTimestamp = battle.getBattleCreatedTimestamp();
									long battleMaxSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_MAX_SECONDS, 300)*1000;
									if ( System.currentTimeMillis() - battleCreatedTimestamp > battleMaxSeconds ) {
										logger.debug("The battle {} last too much time and should make it over", battle.getBattleSessionKey());
										BattleManager.getInstance().battleOverWithRoundOver(battle, BattleStatus.ROOM_IN_DRAW);
										BattleManager.getInstance().battleEnd(battle);
									} else {
										//Check the round owner's last action time.
										//Force the round switch if the round owner is idle
										if ( battle.getBattleRoom().getRoomLeft().getRoomType() != RoomType.TRAINING_ROOM ) {
											BattleUser roundOwner = battle.getRoundOwner();
											if ( roundOwner != null ) {
												int idleSeconds = (int)((System.currentTimeMillis()-roundOwner.getLastActionMillis())/1000);
												int maxIdleSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_USER_MAX_IDLE, 40);
												if ( idleSeconds > maxIdleSeconds ) {
													logger.debug("The battleOwner is idle too much time. Switch the round.");
													battle.roundOver(roundOwner.getUserSessionKey(), true);
													roundOwner.setActionType(ActionType.TIMEOUT);
												}
											} else {
												//logger.warn("The battle round owner is null. It may be a logic error.");
											}
										}
									}								
								}
								//Check if all users are offline
								battle.checkOnlineUsers();
								int totalUsers = battle.getBattleUserMap().size();
								if ( totalUsers <= 0 ) {
									logger.debug("#Checker Battle {} is empty. Call the battleEnd method", battle.getBattleSessionKey());
									BattleManager.getInstance().battleEnd(battle);
								}
								//Check if it expires
								if ( status != BattleStatus.NORMAL ) {
									/**
									 * When the battleOver is already called but the battleEnd has not been called
									 * in the timeout seconds, clean the resources.
									 */
									if ( System.currentTimeMillis() - battle.getBattleOverTimestamp() > 
											battleMaxLiveMilliSeconds ) {
										logger.debug("#Checker Battle {} expires. Call the battleEnd method", battle.getBattleSessionKey());
										BattleManager.getInstance().battleEnd(battle);
									} else {
										logger.debug("#Checker battle.getBattleOverTimestamp() {}", 
												(System.currentTimeMillis() - battle.getBattleOverTimestamp()));
									}	
								}
							}
						}
					} catch (Throwable t) {
						logger.debug("BattleManager#CheckerException: {}", t);
					}
				}

				//Set next round run's schedule
				//GameContext.getInstance().scheduleTask(this, battleCheckerSeconds, TimeUnit.SECONDS);
//				logger.debug("The BattleManager's Check will run after {} seconds", battleCheckerSeconds);
				//logger.info("#BattleManager.Checker sleeps...");
				try {
					Thread.currentThread().sleep(battleCheckerMilliSeconds);
				} catch (InterruptedException e) {
				}

			}
		}
		
	}
}
