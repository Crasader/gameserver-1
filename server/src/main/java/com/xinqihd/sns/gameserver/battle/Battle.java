package com.xinqihd.sns.gameserver.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MapPojo.Point;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.OfflineChallManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.geom.BitmapUtil;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattlePickBox.BceBattlePickBox;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleMove.BceRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBceRolePower.BceRolePower;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserRefresh.BceUserRefresh;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserSync.BceUserSync;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddFriend.BseAddFriend;
import com.xinqihd.sns.gameserver.proto.XinqiBseAskRoundOver.BseAskRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleInit.BseBattleInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleOver.BseBattleOver;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattlePickBox.BseBattlePickBox;
import com.xinqihd.sns.gameserver.proto.XinqiBseBulletTrack.BseBulletTrack;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseDead.BseDead;
import com.xinqihd.sns.gameserver.proto.XinqiBseFriendList.BseFriendList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetValue.BseGetValue;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleChangeDirection.BseRoleChangeDirection;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMove.BseRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBseRolePower.BseRolePower;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleUseTool.BseRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.proto.XinqiBseToolList.BseToolList;
import com.xinqihd.sns.gameserver.proto.XinqiBseVoiceChat.BseVoiceChat;
import com.xinqihd.sns.gameserver.proto.XinqiRoleAudit.RoleAudit;
import com.xinqihd.sns.gameserver.proto.XinqiRoleInfo.RoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The Battle is responsible for two teamings combating.
 * @author wangqi
 *
 */
public class Battle {
	
	private static final Logger logger = LoggerFactory.getLogger(Battle.class);
	
	private static final Logger battleLogger = LoggerFactory.getLogger("BATTLE_STAT");
	
	public static final String BATTLE_SERVER_KEY = "battle_server";
	
	public static final String BATTLE_SESSION_KEY = "battle_session";
	
	public static final String BATTLE_HAS_REWARD = "battle.has.reward";
	
	private static final int CAMP_COUNT = 2;
	
	private BattleRoom battleRoom;

	private List<BattleUser> userLeftList = null;

	private List<BattleUser> userRightList = null;
	
	private HashMap<SessionKey, BattleUser> battleUserMap = 
			new HashMap<SessionKey, BattleUser>();
	
	private BattleBitSetMap battleMap = null;
	
	private String gameServerId = null;
	
	//The current's round number.
	private int roundCount = 1;
	
	private int roundWind = 0;
	
	//The round wind's direction. It's 1 or -1
	private int roundWindDir = 1;

	private HashMap<Integer, Reward> treasureBox = new HashMap<Integer, Reward>();
	
	private BattleStatus status = BattleStatus.NORMAL;
	
	private boolean statusChanged = false;
	
	//The battle created timestamp
	private long battleCreatedTimestamp = Long.MAX_VALUE;
	
	//The timestamp that the battle in a new status.
	private long enterStatusTimestamp = Long.MAX_VALUE;
	
	//The battle over timestamp
	private long battleOverTimestamp = Long.MAX_VALUE;
	
	private BattleUser roundOwner = null;
	
	//上一回合玩家
	private BattleUser lastRoundOwner = null;
	
	//上上回合玩家
	private BattleUser lastLastRoundOwner = null;
	
	//This is the  current round bullet tracks
	private BulletTrack[] roundBulletTracks = null;
	
	//This semaphore is used to wait/notify bullet track calculation result
	private Semaphore roundBulletTrackSemaphore = new Semaphore(0);
	
  //The total kill number in the combat.
	private HashSet<SessionKey> totalKill = new HashSet<SessionKey>();
	
	//The battle's room type
	private RoomType battleRoomType = null;
	
	private int battleUserNumber = 0;
	
	private RoomType roomType = null;
	
	/**
	 * If the battle is PVE, then the 
	 * boss object should be set.
	 */
	private Boss boss = null;
	
	public Battle() {
		//For test purpose
	}
	
	/**
	 * Create a new instance of Battle object.
	 * @param battleRoom
	 */
	public Battle(BattleRoom battleRoom, String gameServerId) {
		this.battleRoom = battleRoom;
		this.gameServerId = gameServerId;
		
		Room roomLeft = battleRoom.getRoomLeft();
		Room roomRight = battleRoom.getRoomRight();
		roomType = roomLeft.getRoomType();
				
		List<UserInfo> users1 = roomLeft.getUserInfoList();
		this.userLeftList = makeUserList(users1, roomLeft.getRoomSessionKey(), 
				roomLeft.getCurrentUserCount(), BattleCamp.LEFT.id(), roomLeft.getRoomType());
		
		List<UserInfo> users2 = roomRight.getUserInfoList();
		this.userRightList = makeUserList(users2, roomRight.getRoomSessionKey(), 
				roomRight.getCurrentUserCount(), BattleCamp.RIGHT.id(), roomRight.getRoomType());
		
		this.battleUserNumber = this.userLeftList.size() + this.userRightList.size();
		
		//Choose the mapId
		this.battleMap = chooseBattleMap(roomLeft.getMapId(), roomRight.getMapId());
		
		//Save to Redis
		String gameServerValue = null;
		if ( this.gameServerId != null ) {
			gameServerValue = this.gameServerId;
		} else {
			gameServerValue = Constant.EMPTY;
		}
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		Collection<BattleUser> battleUsers = battleUserMap.values();
		for (Iterator iter = battleUsers.iterator(); iter.hasNext();) {
			BattleUser battleUser = (BattleUser) iter.next();
			pipeline.hset(battleUser.getUserSessionKey().toString(), BATTLE_SERVER_KEY, gameServerValue);
			pipeline.hset(battleUser.getUserSessionKey().toString(), BATTLE_SESSION_KEY, 
					this.battleRoom.getSessionKey().toString());
		}
		pipeline.sync();

		//Setup relationship here
		if ( this.battleRoom.getRoomRight().getRoomType() != RoomType.PVE_ROOM ) {
			setupRelation();
		}
		
		/**
		 * Store the battle room type.
		 */
		battleRoomType = this.battleRoom.getRoomLeft().getRoomType();
	}

	/**
	 * Setup all the users in rival relationship.
	 * 
	 */
	public void setupRelation() {
		for ( BattleUser leftUser : this.userLeftList ) {
			User user = leftUser.getUser();
			if ( user.isAI() || user.isBoss() || user.isProxy() ) continue;
			boolean changed = false;
			Relation relation = user.getRelation(RelationType.RIVAL);
			if ( relation == null ) {
				relation = new Relation();
				relation.set_id(user.get_id());
				relation.setParentUser(user);
				relation.setType(RelationType.RIVAL);
				user.addRelation(relation);
				changed = true;
			}
			for ( BattleUser rightUser : this.userRightList ) {
				User rival = rightUser.getUser();
				People people = relation.findPeopleByUserName(rival.getRoleName());
				if ( people == null ) {
					people = new People();
					people.setId(user.get_id());
					people.setMyId(rival.get_id());
					people.setUsername(rival.getUsername());
					people.setRolename(rival.getRoleName());
					relation.addPeople(people);
					//Send message to client
					BseAddFriend.Builder addFriend = BseAddFriend.newBuilder();
					addFriend.setBlacklist(false);
					addFriend.setSucc(true);
					addFriend.setUsername(rival.getRoleName());
					GameContext.getInstance().writeResponse(rival.getSessionKey(), addFriend.build());
					
					changed = true;
				}
			}
			if ( changed ) {
				Collection<Relation> relations = user.getRelations();
				if ( relations != null ) {
					try {
						UserManager.getInstance().saveUserRelation(relations);
					} catch (Exception e) {
						logger.warn("Failed to store user rival friends.", e.toString());
					}
				}
			}
		}
		for ( BattleUser rightUser : this.userRightList ) {
			User user = rightUser.getUser();
			if ( user.isAI() || user.isBoss() || user.isProxy() ) continue; 
			boolean changed = false;
			Relation relation = user.getRelation(RelationType.RIVAL);
			if ( relation == null ) {
				relation = new Relation();
				relation.set_id(user.get_id());
				relation.setParentUser(user);
				relation.setType(RelationType.RIVAL);
				user.addRelation(relation);
				changed = true;
			}
			for ( BattleUser leftUser : this.userLeftList ) {
				User rival = leftUser.getUser();
				People people = relation.findPeopleByUserName(rival.getRoleName());
				if ( people == null ) {
					people = new People();
					people.setId(user.get_id());
					people.setMyId(rival.get_id());
					people.setUsername(rival.getUsername());
					people.setRolename(rival.getRoleName());
					relation.addPeople(people);
					//Send message to client
					BseAddFriend.Builder addFriend = BseAddFriend.newBuilder();
					addFriend.setBlacklist(false);
					addFriend.setSucc(true);
					addFriend.setUsername(rival.getRoleName());
					GameContext.getInstance().writeResponse(rival.getSessionKey(), addFriend.build());
					
					changed = true;
				}
			}
			if ( changed ) {
				Collection<Relation> relations = user.getRelations();
				if ( relations != null ) {
					try {
						UserManager.getInstance().saveUserRelation(relations);
					} catch (Exception e) {
						logger.warn("Failed to store user rival friends.", e.toString());
					}
				}
			}
		}
	}
	
	/**
	 * When the battle is over, update friend's info about win / lose ratio.
	 * @param userList
	 * @param battleUser
	 */
	private void updateFriendWinOrLose(List<BattleUser> userList,
			BattleUser battleUser, boolean win) {
		if ( battleRoomType == RoomType.OFFLINE_ROOM ) {
			return;
		}
		//update win status
		Map<RelationType, Collection<People>> friends = new HashMap<RelationType, Collection<People>>();
		for ( BattleUser rivalUser : userList ) {
			Map<RelationType, Collection<People>> map = UserManager.getInstance().saveFriendWinOrLose(battleUser.getUser(), 
					rivalUser.getUser(), win);
			for ( RelationType key : map.keySet() ) {
				Collection<People> value = map.get(key);
				if ( value != null && value.size() > 0 ) {
					Collection<People> friendCol = friends.get(key);
					if ( friendCol == null ) {
						friendCol = new ArrayList<People>();
						friends.put(key, friendCol);
					}
					friendCol.addAll(value);
				}
			}
		}
		/**
		 * Do not update the friend list now
		 * because it will duplicate the friend list.
		 * 
		 * wangqi 2012-10-25
		 */
		//send friend changed list
		BseFriendList.Builder bseFriendListBuilder = BseFriendList.newBuilder();
		bseFriendListBuilder.setType(1);
		for ( RelationType type : friends.keySet() ) {
			Collection<People> people = friends.get(type);
			if ( people != null ) {
				for ( People p : people ) {
					bseFriendListBuilder.addFriendList(p.toFriendInfoLite(type, p.getBasicUser(), true));
					logger.debug("add win on friend {} of num {}", p.getUsername(), p.getWin());
				}
			}
		}
		GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), bseFriendListBuilder.build());
	}
	
	/**
	 * @return the battleRoomType
	 */
	public RoomType getBattleRoomType() {
		return battleRoomType;
	}

	/**
	 * @param battleRoomType the battleRoomType to set
	 */
	public void setBattleRoomType(RoomType battleRoomType) {
		this.battleRoomType = battleRoomType;
	}

		/**
		 * Initialize the battle for two combating room.
		 * @param battleRoom
		 */
		public void battleBegin() {
			BseBattleInit.Builder builder = BseBattleInit.newBuilder();
			builder.setRoomId(battleRoom.getSessionKey().toString());
			builder.setCampCount(CAMP_COUNT);
	//		builder.setMapId(StringUtil.toInt(this.mapPojo.getId(), 0));
			builder.setMapId(StringUtil.toInt(this.battleMap.getMapId(), 0));
			/**
			 * Use this battle mode to send battle total seconds
			 * 2012-11-22
			 */
			int maxSecond = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BATTLE_MAX_SECONDS, 300);
			/**
			 * It is used in BattleInt
			 */
			builder.setBattleMode(this.battleRoom.getRoomLeft().getRoomType().ordinal());
			
			List<User> userList = new ArrayList<User>(8);
			
			ArrayList<Point> allPoints = this.battleMap.getMapPojo().getStartPoints();
			pickStartPoint(userLeftList, BattleCamp.LEFT.id(), allPoints);
			for ( int i=0; i<this.userLeftList.size(); i++ ) {
				BattleUser user = this.userLeftList.get(i);
				if ( user == null ) continue;
				RoleInfo roleInfo = makeRoleInfo(user, BattleCamp.LEFT.id(), i);
				if ( roleInfo == null ) {
					logger.warn("Failed to find the user in database. ");
				} else {
					builder.addRoleArr(roleInfo);
					userList.add(user.getUser());
				}
				//Make user's equipment deprecated
				/**
				 * Now it will call 
				 * ShopManager.getInstance().reduceUserEquipmentDuration(user);
				 */
				//ScriptManager.getInstance().runScript(ScriptHook.WEAPON_DEPRECIATE, user.getUser());
				
				if ( !user.getUser().isAI() && !user.getUser().isProxy() ) {
					StatClient.getIntance().sendDataToStatServer(user.getUser(), 
						StatAction.BattleBegin);
				}
			}
			
			pickStartPoint(userRightList, BattleCamp.RIGHT.id(), allPoints);
			for ( int i=0; i<this.userRightList.size(); i++ ) {
				BattleUser user = this.userRightList.get(i);
				if ( user == null ) continue;
				RoleInfo roleInfo = makeRoleInfo(user, BattleCamp.RIGHT.id(), i);
				if ( roleInfo == null ) {
					logger.warn("Failed to find the user in database. ");
				} else {
					builder.addRoleArr(roleInfo);
					userList.add(user.getUser());
				}
				/**
				 * Now it will call 
				 * ShopManager.getInstance().reduceUserEquipmentDuration(user);
				 */
				//Make user's equipment deprecated
				//ScriptManager.getInstance().runScript(ScriptHook.WEAPON_DEPRECIATE, user.getUser());

				if ( !user.getUser().isAI() ) {
					StatClient.getIntance().sendDataToStatServer(user.getUser(), 
						StatAction.BattleBegin);
				}
			}
			
			//Select the random battle wether effect
			Object[] effects = MathUtil.randomPick(WeatherEffect.getValueList(), 1);
			WeatherEffect effect = (WeatherEffect)effects[0];
			builder.setEnv(effect.ordinal());
			
			XinqiMessage xinqiMessage = new XinqiMessage();
			xinqiMessage.payload = builder.build();
	
			for ( User user : userList ) {
				if ( user.isProxy() ) {
					GameContext.getInstance().writeResponse(user.getProxySessionKey(), xinqiMessage);
				} else {
					GameContext.getInstance().writeResponse(user.getSessionKey(), xinqiMessage);
				}
			}
	
			//Remember the battle begin timestamp
			this.battleCreatedTimestamp = System.currentTimeMillis();
			
		}

	/**
	 * If either side in battle are all dead or exit abnormaly,
	 * the other side will win and the battle is over.
	 */
	public void battleOver(BattleStatus status) {
		if ( this.battleOverTimestamp != Long.MAX_VALUE ) {
			logger.debug("The battleOver for {} is already called ", this.getBattleSessionKey());
			return;
		}
		this.battleOverTimestamp = System.currentTimeMillis();
		
		//Clean the battleSessionKey in user's session map
		Collection<BattleUser> battleUsers = battleUserMap.values();
		int totalUserNumber = battleUsers.size();
		
		BseBattleOver.Builder overBuilder = BseBattleOver.newBuilder();
		//Exp ratio
		int expRate = GameContext.getInstance().getScriptManager().
				runScriptForInt(ScriptHook.BATTLE_EXP_RATE, battleUsers);
		overBuilder.setExpRate(expRate);
		
		int winCamp = BattleCamp.LEFT.id();
		if ( status == BattleStatus.ROOM_LEFT_WIN ) {
			winCamp = BattleCamp.LEFT.id();
		} else if ( status == BattleStatus.ROOM_RIGHT_WIN ) {
			winCamp = BattleCamp.RIGHT.id();
		} else if ( status == BattleStatus.ROOM_IN_DRAW ) {
			BattleCamp camp = (BattleCamp)ScriptManager.getInstance().runScriptForObject(
					ScriptHook.BATTLE_TIMEOUT_WINNER, this, userLeftList, userRightList);
			winCamp = camp.id();
		}
		overBuilder.setWinCamp(winCamp);
		
		//Call script to calculate user's experience got in the battle.
		ScriptResult result = GameContext.getInstance().getScriptManager().
				runScript(ScriptHook.BATTLE_OVER, battleUsers, winCamp, this);
		
		boolean trainingOrDeskRoom = false;
		if ( battleRoomType == RoomType.TRAINING_ROOM ) {
			trainingOrDeskRoom = true;
		} else if ( battleRoomType == RoomType.DESK_ROOM ) {
			trainingOrDeskRoom = true;
		}
		
		for (Iterator<BattleUser> iterator = battleUsers.iterator(); iterator.hasNext();) {
			final BattleUser battleUser = iterator.next();
			final User user = battleUser.getUser();
			
			//if ( battleUser.getUser().isAI() ) continue;
			
			Jedis jedis = JedisFactory.getJedis();
			if ( !user.isAI() && !user.isBoss() && !user.isProxy() && 
					!battleUser.isLeaveBattle() ) {
				//Consume user's roleaction
				ScriptManager.getInstance().runScript(
						ScriptHook.ROLE_ACTION_CONSUME, this, user, 
						this.battleRoom.getRoomLeft().getRoomType());
				
				Long ttlLong = jedis.ttl(user.getSessionKey().toString());
				int ttl = -1;
				if ( ttlLong != null ) {
					ttl = ttlLong.intValue();
				}
				if ( trainingOrDeskRoom ) {
					logger.debug("Training room and desk room has no rewards");
					jedis.hset(user.getSessionKey().toString(), BattleManager.BATTLE_HAS_REWARD_KEY, Boolean.FALSE.toString());
					jedis.hdel(user.getSessionKey().toString(), BattleManager.BATTLE_ROOM_TYPE_KEY);
				} else {
					if ( battleUser.getTotalEnemyHurt() > 0 ) {
						//user.putUserData(BATTLE_HAS_REWARD, Boolean.TRUE);
						jedis.hset(user.getSessionKey().toString(), BattleManager.BATTLE_HAS_REWARD_KEY, Boolean.TRUE.toString());
						jedis.hset(user.getSessionKey().toString(), BattleManager.BATTLE_ROOM_TYPE_KEY, battleRoomType.toString());
					} else {
						logger.debug("User {} does not hurt enemies and has no reward", user.getRoleName());
						//user.putUserData(BATTLE_HAS_REWARD, Boolean.FALSE);
						jedis.hset(user.getSessionKey().toString(), BattleManager.BATTLE_HAS_REWARD_KEY, Boolean.FALSE.toString());
						jedis.hdel(user.getSessionKey().toString(), BattleManager.BATTLE_ROOM_TYPE_KEY);
					}
				}
				if ( ttl >= 0 ) {
					jedis.expire(user.getSessionKey().toString(), ttl);
				}
			}
			RoleAudit.Builder builder = RoleAudit.newBuilder();
	    builder.setSessionId(battleUser.getUserSessionKey().toString());
	    builder.setLevel(user.getLevel());
	    //VIP加成经验
	    builder.setExp(battleUser.getVipExp());
	    //战斗基础经验
	    builder.setExpDeta(battleUser.getBaseExp());
	    //活动经验
	    double rate = ActivityManager.getInstance().getActivityExpRate(user);
	    if ( rate < 0.0 ) { 
	    	rate = 0.0;
	    } else if ( rate > 1.0 ) {
	    	rate = 1.0;
	    }
	    ActivityManager.getInstance().consumeDoubleExp(user);
	    int actExp = (int)(rate*battleUser.getBaseExp());
	    if ( actExp > 0 ) {
	    	logger.info("User {} has activity exp rate: {}, baseExp:{}, orig rate:{}", 
	    			new Object[]{
	    			user.getRoleName(),
	    			rate, 
	    			"baseExp:{}", 
	    			battleUser.getBaseExp(),
	    			ActivityManager.getInstance().getActivityExpRate(user)
	    	});
	    }
	    builder.setActexp(actExp);
	    //总经验
	    builder.setMaxExp( battleUser.getVipExp()+battleUser.getBaseExp() + actExp);
	    //杀敌数
	    builder.setKill(battleUser.getTotalKill());
	    if ( !trainingOrDeskRoom && battleUser.getTotalKill() > 0 ) {
	    	user.setTotalKills(user.getTotalKills()+battleUser.getTotalKill());
	    }
	    //命中率
	    builder.setHit((int)(battleUser.getHitRatio()*100));
	    if ( logger.isDebugEnabled() ) {
	    	logger.debug("#battleOver audit: base exp:{}, vip exp:{}, " +
	    			"sum exp:{}, kill:{}, hit ratio:{}", 
	    			new Object[] {battleUser.getBaseExp(), 
	    			battleUser.getVipExp(), builder.getMaxExp(), builder.getKill(),
	    			builder.getHit()} );
	    }
	    //Output new audit data
	    HashMap<BattleAuditItem, Integer> auditExpMap = battleUser.getAuditExpMap();
	    for ( BattleAuditItem key : BattleAuditItem.values() ) {
	    	switch ( key ) {
	    		case AccurateNum:
	    			builder.setAccurateNum(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case DropNum:
	    			builder.setDropNum(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case HitRatio:
	    			builder.setHitRatio(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case HurtBlood:
	    			builder.setHurtBlood(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case KillNum:
	    			builder.setKillNum(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case Perfect:
	    			builder.setPerfect(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case PowerDiff:
	    			builder.setPowerDiff(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case RoundNum:
	    			builder.setRoundNum(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case SecondKill:
	    			builder.setSecondKill(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case Spy:
	    			builder.setSpy(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case TotalUser:
	    			builder.setTotalUser(getAuditExpItem(auditExpMap, key));
	    			break;
	    		case WinGame:
	    			builder.setWinGame(getAuditExpItem(auditExpMap, key));
	    			break;
	    	}
	    }
	    String remark = battleUser.getRemark();
	    if ( remark == null ) {
	    	remark = Constant.EMPTY;
	    } else {
	    	logger.debug("{} remark: {}", battleUser.getUser().getRoleName(), remark);
	    }
	    builder.setRemark(remark);
	    builder.setBattleMode(this.battleRoom.getRoomLeft().getRoomType().ordinal());
	    builder.setRoomType(this.battleRoom.getRoomLeft().getRoomType().ordinal());
	    builder.setDamage(battleUser.getTotalEnemyHurt());
	    builder.setCreditDeta(battleUser.getCreditDeta());
	    
	    final int finalActExp = actExp;
	    if ( !battleUser.isLeaveBattle() && !trainingOrDeskRoom ) {
		    GameContext.getInstance().scheduleTask(new Runnable(){
		    	public void run() {
		  	    //Update user's exp
		    		User user = battleUser.getUser();
		  	    if ( battleUser.getTotalExp() > 0 && !user.isProxy() && !user.isAI()) {
		  	    	if ( GameContext.getInstance().getGameServerId().equals(user.getGameserverId())) {
		  	    		//In the same server
			  	    	user.setExp(user.getExp() + battleUser.getTotalExp() + finalActExp);
			  				//Save user and user's bag to database.
			  				GameContext.getInstance().getUserManager().saveUser(user, false);

			  				//Notify client user's role data is changed.
			  				//Send the data back to client
			  				BseRoleInfo roleInfo = user.toBseRoleInfo();
			  				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
			  				logger.debug("The new roleInfo data for user {} is sent to client.", user.getRoleName());
			  				
		  	    	} else {
		  	    		//In different game server
		  	    		BceUserSync.Builder builder = BceUserSync.newBuilder();
		  	    		builder.setMode(1);
		  	    		builder.setValue(battleUser.getTotalExp());
		  	    		GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
		  	    				user.getGameserverId(), builder.build());
		  	    	}
		  	    	StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
									StatAction.ProduceBattleReward, "Exp", battleUser.getTotalExp());
		  	    }
		    	}
		    }, 5, TimeUnit.SECONDS);
	    }
	    if ( !user.isAI() && user.isTutorial() ) {
    		/**
    		 * Training can only happen at user's gameserver. 
    		 * It is unnecessary to check user gameserverid.
    		 */
    		user.setTutorial(false);
    		GameContext.getInstance().getUserManager().saveUser(user, false);
    		if ( !GameContext.getInstance().getGameServerId().equals(user.getGameserverId())) {
    			BceUserRefresh.Builder refresh = BceUserRefresh.newBuilder();
    			refresh.setRefreshmode(1);
    			GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
	    				user.getGameserverId(), refresh.build());
    		}
    	}
	    
	    overBuilder.addAuditArr(builder.build());
	    
	    //Update user's battle status
	    ScriptManager.getInstance().runScript(ScriptHook.USER_UPDATE_STAT, 
	    		battleUser.getUser(), winCamp==battleUser.getCamp());
	    
	    ScriptManager.getInstance().runScript(ScriptHook.BATTLE_REMARK, 
	    		this, battleUser, roundCount, (this.battleOverTimestamp-this.battleCreatedTimestamp));	    
	    
	    PropData weapon = user.getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
	    if ( weapon != null ) {
		    if ( battleUser.getCamp() == winCamp ) {
		    	StatClient.getIntance().sendDataToStatServer(
		    			user, StatAction.BattleWin, weapon.getName(), battleRoomType);
		    	UserActionManager.getInstance().addUserAction(user.getRoleName(), 
		    			UserActionKey.BattleWin);
		    } else {
		    	StatClient.getIntance().sendDataToStatServer(
		    			user, StatAction.BattleFail, weapon.getName(), battleRoomType);
		    	UserActionManager.getInstance().addUserAction(user.getRoleName(), 
		    			UserActionKey.BattleFail);
		    }
	    }
	    
	    //Store offline data
			if ( battleRoomType == RoomType.OFFLINE_ROOM ) {
				BattleUser challengee = null;
				if ( battleRoom.getRoomLeft().getOwnerSessionKey().equals(battleUser.getUserSessionKey()) ) {
					if ( this.userRightList.size()>0 ) {
						challengee = this.userRightList.get(0);
						
						OfflineChallManager.getInstance().storeChallengeInfo(battleUser.getUser(), 
							challengee.getUser(), battleUser.getCamp() != winCamp, System.currentTimeMillis());
					} else {
						logger.debug("The offline challengee is deleted.");
					}
				}
			}
		}//for...
		
		BseBattleOver battleOver = overBuilder.build();
		logger.debug("Battle is over. {} is winner.", winCamp);
		
		List<BattleUser> winUserList = null;
		List<BattleUser> loseUserList = null;
		if ( status == BattleStatus.ROOM_LEFT_WIN ) {
			winUserList = this.userLeftList; 
			loseUserList = this.userRightList;
		} else if ( status == BattleStatus.ROOM_RIGHT_WIN ) {
			loseUserList = this.userLeftList; 
			winUserList = this.userRightList;
		}
		
		if ( battleRoomType == RoomType.PVE_ROOM ) {
			//sync the battle progress to global server
			ScriptManager.getInstance().runScript(ScriptHook.BOSS_BATTLE_SYNC, this, winUserList, loseUserList);
		}

		if ( !trainingOrDeskRoom && winUserList != null && loseUserList != null ) {
			sendBattleOver(totalUserNumber, battleRoomType, battleOver, winUserList,
					loseUserList);
		} else {
			for (Iterator<BattleUser> iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser battleUser = iterator.next();
				boolean win = (winCamp==battleUser.getCamp());
				if ( !battleUser.isLeaveBattle() && !battleUser.getUser().isProxy() ) {
					GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), battleOver);

					if ( !battleUser.getUser().isAI() ) {
						StatClient.getIntance().
							sendDataToStatServer(battleUser.getUser(), 
									StatAction.BattleOver, (this.battleOverTimestamp-this.battleCreatedTimestamp), roundCount, win);
					}
				}

				//Call the TaskHook
				TaskManager.getInstance().processUserTasks(battleUser.getUser(), TaskHook.COMBAT, 
						win, totalUserNumber, battleRoomType);
				
			}			
		}
		
	}

	/**
	 * @return the battleUserNumber
	 */
	public int getBattleUserNumber() {
		return battleUserNumber;
	}

	/**
	 * Check if it is null and get the audit exp item 
	 * @param auditExpMap
	 * @param key
	 * @return
	 */
	private int getAuditExpItem(
			HashMap<BattleAuditItem, Integer> auditExpMap, BattleAuditItem key) {
		Integer exp = auditExpMap.get(key);
		if ( exp == null ) return 0;
		return exp.intValue();
	}

	/**
	 * Send BattleOver to user list.
	 * @param totalUserNumber
	 * @param roomType
	 * @param battleOver
	 * @param winUserList
	 * @param loseUserList
	 */
	private void sendBattleOver(
			int totalUserNumber, RoomType roomType, BseBattleOver battleOver, 
			List<BattleUser> winUserList, List<BattleUser> loseUserList) {
		for ( BattleUser battleUser : winUserList ) {
			if ( !battleUser.isLeaveBattle() && !battleUser.getUser().isProxy() ) {
				GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), battleOver);
				
				if ( !battleUser.getUser().isAI() ) {
					StatClient.getIntance().
						sendDataToStatServer(battleUser.getUser(), 
								StatAction.BattleOver, (this.battleOverTimestamp-this.battleCreatedTimestamp), roundCount, true);
				}
			}				
			//Call the TaskHook
			TaskManager.getInstance().processUserTasks(battleUser.getUser(), TaskHook.COMBAT, 
					true, totalUserNumber, roomType);

			updateFriendWinOrLose(loseUserList, battleUser, true);
		}
		for ( BattleUser battleUser : loseUserList ) {
			if ( !battleUser.isLeaveBattle() && !battleUser.getUser().isProxy() ) {
				GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), battleOver);
				
				if ( !battleUser.getUser().isAI() ) {
					StatClient.getIntance().
						sendDataToStatServer(battleUser.getUser(), 
								StatAction.BattleOver, (this.battleOverTimestamp-this.battleCreatedTimestamp), roundCount, false);
				}
			}
			//Call the TaskHook
			TaskManager.getInstance().processUserTasks(battleUser.getUser(), TaskHook.COMBAT, 
					false, totalUserNumber, roomType);

			updateFriendWinOrLose(winUserList, battleUser, false);
		}
	}
	
	/**
	 * After the Battle's reward is processed, the battle ends and all
	 * data in Redis should be cleaned.
	 * 
	 * @param status
	 */
	public void battleEnd() {
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		
		Collection<BattleUser> battleUsers = battleUserMap.values();
		
		for (Iterator<BattleUser> iterator = battleUsers.iterator(); iterator.hasNext();) {
			BattleUser battleUser = iterator.next();
			if ( !battleUser.isLeaveBattle() ) {
				pipeline.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SERVER_KEY);
				pipeline.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SESSION_KEY);
			}
		}
		
		pipeline.sync();

	}
	
	/**
	 * Make an user is ready for a new round. It happens after an user completely loaded
	 * the battle map and landed on ground.
	 * @param userSessionKey
	 */
	public void stageReady(SessionKey userSessionKey) {
		BattleUser currentUser = battleUserMap.get(userSessionKey);
		if ( currentUser != null ) {
			currentUser.setStageReady(true);
			
			boolean allReady = true;
			
			Collection<BattleUser> battleUsers = battleUserMap.values();
			for (BattleUser battleUser : battleUsers) {
				if ( !battleUser.getUser().isProxy() && !battleUser.getUser().isAI() && !battleUser.isStageReady() ) {
					allReady = false;
					break;
				}
			}

			if ( allReady ) {
				logger.debug("All users in battle are stageReady. A new round will be started.");

				roundStart();
				
				allReady = true;
			} else {
				sendStageReadyStatus();
			}
		} else {
			logger.warn("A userSessionKey is not found in battle.", userSessionKey);
		}
	}
	
	/**
	 * Tell all users in a battle a new round is started and the owner is 
	 * given user.
	 * 
	 * @param roundOwner
	 */
	public void roundStart() {
		roundStart(true);
	}
	
	/**
	 * Tell all users in a battle a new round is started and the owner is 
	 * given user.
	 * 
	 * @param roundOwner
	 */
	public void roundStart(boolean checkStatus) {
		//Check the first
		if ( checkStatus ) {
			status = this.checkBattleUsers();
		}

		//Check if the battle is in normal status.
		if ( status != BattleStatus.NORMAL ) {
			logger.debug("#roundStart. The battle is in {} status. donot send roundStart message.", status);
			this.setStatus(status);
//			battleOver(status);
			return;
		}
		
		/**
		 * Check the PVE total round limit
		 */
		if ( boss != null ) {
			int totalRound = boss.getBossPojo().getTotalRound();
			if ( totalRound > 0 ) {
				if ( this.roundCount > totalRound ) {
					//The battleOver is not called yet
					status = BattleStatus.ROOM_RIGHT_WIN;
					BattleManager.getInstance().battleOverWithRoundOver(this, status);
					BattleManager.getInstance().battleEnd(this);
					return;
				}
			}
		}
		
		//Pick the next round's owner.
		Collection<BattleUser> battleUsers = battleUserMap.values();
		ScriptManager manager = GameContext.getInstance().getScriptManager();
		
		//Check if there are new users who are dead before the roundOwner changes
		//It is for the dropping users case.
		for ( BattleUser bUser : this.battleUserMap.values() ) {
			if ( bUser.containStatus(RoleStatus.DEAD) ) {
				if ( !this.totalKill.contains(bUser.getUserSessionKey()) ) {
					logger.debug("A new user {} is dead before the roundstart.", bUser.getUser().getRoleName());
					/*
					if ( this.roundOwner != null ) {
						BattleUserAudit audit = this.roundOwner.getHurtUser(bUser.getUserSessionKey());
						audit.setKilled(true);
						audit.setBattleUser(bUser);
						audit.setEnemy( !(bUser.getCamp()==this.roundOwner.getCamp()) );
					}
					*/
					this.totalKill.add(bUser.getUserSessionKey());
				}
			}
		}
		
		final BattleUser roundOwner = (BattleUser)GameContext.getInstance().
				getScriptManager().runScriptForObject(
						ScriptHook.PICK_ROUND_USER, battleUsers, this.roundOwner, 
						this.battleRoom, this.userLeftList, this.userRightList);

		this.lastLastRoundOwner = this.lastRoundOwner;
		this.lastRoundOwner = roundOwner;
		this.roundOwner = roundOwner;
		//Reset the bullet tracks
		this.roundBulletTracks = null;
		
		String roundOwnerSessionKey = Constant.EMPTY;
		
		if ( this.roundOwner != null ) {
			this.roundOwner.setLastActionMillis(System.currentTimeMillis());
			this.roundOwner.setActionType(ActionType.DEFAULT);
			this.roundOwner.setPowerAttack(false);
			this.roundOwner.clearUsedTool();

			String roleName = this.roundOwner.getUser().getRoleName();
			roundOwnerSessionKey = this.roundOwner.getUserSessionKey().toString();
			logger.debug("The new round owner is {} (session:{})", 
					roleName, 
					this.roundOwner.getUserSessionKey());
			/*
			if ( roundOwner.containStatus(RoleStatus.ICED) ) {
				//All users are frozen
				GameContext.getInstance().scheduleTask(new Runnable() {
					public void run() {
						logger.info("Send frozen round over {}", roundOwner.getUser().getRoleName());
						roundOver(roundOwner.getUserSessionKey(), true);
					}
				}, 5, TimeUnit.SECONDS);
			}
			*/
		} else {
			logger.warn("#roundStart(): The new round owner is null.");
		}
		
		int roundWind = GameContext.getInstance().getScriptManager().
				runScriptForInt(ScriptHook.BATTLE_WIND, this, roundOwner);
		this.setRoundWind(roundWind);
		
		BseRoundStart.Builder builder = BseRoundStart.newBuilder();
		builder.setWind(this.roundWind);
		builder.setSessionId(roundOwnerSessionKey);
		builder.setCurRound(roundCount++);
		for (BattleUser battleUser : battleUsers) {
			/**
			 * display the dead user
			 * 2012-11-16
			 */
			//if ( !battleUser.containStatus(RoleStatus.DEAD) ) {
				//Call script to check the user's role status
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.BATTLE_ROUND_START_CHECK_ROLE_STATUS, this, battleUser);
			
			builder.addUserId(battleUser.getUserSessionKey().toString());
			builder.addBlood(battleUser.getBlood());
			builder.addStrength(battleUser.getThew());
			//Every round add new energy to user
			int energy =  battleUser.getEnergy() + 10;
			if ( energy > 100 ) {
				energy = 100;	
			}
			battleUser.setEnergy(energy);
			builder.addEnergy(energy);
			
			if ( battleUser.getUser().isBoss() ) {
				User user = battleUser.getUser();
				BossPojo bossPojo = (BossPojo)user.getUserData(BossManager.USER_BOSS_POJO);
				if ( bossPojo.getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
					battleUser.addStatus(RoleStatus.HIDDEN);
				}
			}
			builder.addUserMode(battleUser.convertStatusToUserBit());
			builder.addCampid(battleUser.getCamp());
			/**
			 * If user is ai, update its position at server side
			 */
			if ( !BattleManager.getInstance().isUseDistributed() 
					&& battleUser.getUser().isAI() ) {
				SimplePoint newPoint = BitmapUtil.dropOnGround(battleUser.getPosX(), 
						battleUser.getPosY(), 
						BitmapUtil.DEFAULT_SCALE, this.battleMap.getMapBitSet());
				if ( logger.isDebugEnabled() ) {
					logger.debug("AI user {} old pos: {},{} to new pos: {},{}", 
						new Object[]{battleUser.getUser().getUsername(), battleUser.getPosX(), battleUser.getPosY(),
						newPoint.getX(), newPoint.getY()});
				}
				builder.addPosX(newPoint.getX());
				builder.addPosY(newPoint.getY());
			} else {
				builder.addPosX(battleUser.getPosX());
				builder.addPosY(battleUser.getPosY());
			}
			//}
		}

		if ( boss == null || !(boss.getBossPojo().getBossWinType() == BossWinType.COLLECT_DIAMOND) ) {
			int tboxCount = this.treasureBox.size();
			builder.setBoxcount(tboxCount);
			for ( Integer key : this.treasureBox.keySet() ) {
				Reward treasureBox = this.treasureBox.get(key);
				if ( treasureBox != null ) {
					builder.addBoxindex(key);
					builder.addBoxpropid(StringUtil.toInt(treasureBox.getId(), 0));
					builder.addBoxpropcount(treasureBox.getPropCount());
					builder.addBoxposx(treasureBox.getX());
					builder.addBoxposy(treasureBox.getY());
				}
			}
		} else {
			User user = null;
			if ( this.roundOwner!= null ) {
				user = roundOwner.getUser();
			}
			if ( user != null && !user.isBoss() ) {
				Reward reward = (Reward)ScriptManager.getInstance().runScriptForObject(ScriptHook.DIAMOND_REWARD, 
						user, boss);
				if ( reward == null ) {
					reward = RewardManager.getRewardGolden(user);
				}
				this.treasureBox.clear();
				this.treasureBox.put(0, reward);
				builder.setBoxcount(1);
				//reward
				builder.addBoxindex(0);
				builder.addBoxpropid(StringUtil.toInt(reward.getId(), 0));
				builder.addBoxpropcount(reward.getPropCount());
				builder.addBoxposx(reward.getX());
				builder.addBoxposy(reward.getY());
			} else {
				builder.setBoxcount(0);
			}
		}
		
		//Send back to users.
		BseRoundStart roundStart = builder.build();
		for (BattleUser battleUser : battleUsers) {
			if ( !battleUser.isLeaveBattle() && !battleUser.getUser().isProxy() ) {
				//logger.debug("send roundstart to user {}", battleUser.getUser().getRoleName());
				GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), roundStart);
			}
		}
		
	}
	
	/**
	 * The user's round is over.
	 * Recaculate all the energy for all users.
	 * 
	 * If 'userAsked' is true, it means the user press save the energy for this round.
	 * 
	 * @param userSessionKey
	 * @param userAsked
	 */
	public void roundOver(SessionKey userSessionKey, boolean userAsked) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);

		if ( battleUser == null ) {
			logger.info("The battle user for {} is already removed.", userSessionKey);
		} else {
			if ( userAsked ) {
				battleUser.setActionType(ActionType.SAVE);
			}
			//Call script
			ScriptResult result = GameContext.getInstance().getScriptManager().
					runScript(ScriptHook.BATTLE_ROUND_OVER, battleUser, userAsked);
		}
		if ( userAsked ) {
			roundOverWithoutStart(userSessionKey);
			logger.info("Send roundover to user: " + battleUser.getUser().getUsername());
			roundStart();
			logger.info("Send roundstart: " + battleUser.getUser().getUsername());
		} else {
			/**
			 * Start a new round immediatly because the client
			 * will control the latency.
			 * 2012-07-30
			 */
			roundStart();
			/**
			 * Start a new round.
			 */
			/*
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				GameContext.getInstance().scheduleTask(new Runnable(){
					public void run() {
						roundStart();
					}
				}, 2, TimeUnit.SECONDS);
			}
			*/
		}
		
		/*
		if ( !battleUser.getUser().isAI() ) {
			StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
				StatAction.RoundOver, userAsked);
		}
		*/
	}
	
	/**
	 * Send BseAskRoundOver without sending the roundstart.
	 * It is used when user exist a battle by leaving room
	 * @param userSessionKey
	 */
	public void roundOverWithoutStart(SessionKey userSessionKey) {
		//Send the round over message to client
		BseAskRoundOver.Builder builder = BseAskRoundOver.newBuilder();
		builder.setSessionId(userSessionKey.toString());
		sendToAllUsers(builder.build());
		logger.debug("Send BseAskRoundOver to all users");
	}

	
	/**
	 * An user picks a treasure box during attacking others.
	 * @param userSessionKey
	 * @param pickBox
	 */
	public void pickTreasureBox(SessionKey userSessionKey, BceBattlePickBox pickBox) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		battleUser.setLastActionMillis(System.currentTimeMillis());
		
		List<Integer> boxIndexes = pickBox.getBoxindexList();
		
		for ( int boxIndex : boxIndexes ) {
			Reward box = this.treasureBox.get(boxIndex);
			if ( box != null ) {
				ArrayList<Reward> rewards = new ArrayList<Reward>();
				rewards.add(box);
				User user = battleUser.getUser();
				RewardManager.getInstance().pickReward(user, rewards, 
						StatAction.ProduceBattlePick);
				if ( !GameContext.getInstance().getGameServerId().equals(user.getGameserverId())) {
					//In different game server
					RewardType rewardType = box.getType();
					switch ( rewardType ) {
						case ITEM:
						case STONE:
						case WEAPON:
							String propName = RewardManager.getInstance().getRewardName(box);
							String subject = Text.text("battle.pickbox.subject", propName);
							String content = Text.text("battle.pickbox.content");		
							MailMessageManager.getInstance().sendMail(null, user.get_id(), 
									subject, content, rewards, true);
							break;
						  /**
						   * 1: add exp
						   * 2: add golden
						   * 3: add yuanbao
						   */
						case EXP:
	  	    		BceUserSync.Builder builder = BceUserSync.newBuilder();
	  	    		builder.setMode(1);
	  	    		builder.setValue(box.getPropCount());
	  	    		GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
	  	    				user.getGameserverId(), builder.build());
	  	    		break;
						case GOLDEN:
	  	    		builder = BceUserSync.newBuilder();
	  	    		builder.setMode(2);
	  	    		builder.setValue(box.getPropCount());
	  	    		GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
	  	    				user.getGameserverId(), builder.build());
	  	    		break;
						case YUANBAO:
	  	    		builder = BceUserSync.newBuilder();
	  	    		builder.setMode(3);
	  	    		builder.setValue(box.getPropCount());
	  	    		GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
	  	    				user.getGameserverId(), builder.build());
	  	    		break;
					}
				}
				treasureBox.put(boxIndex, null);
				BseBattlePickBox.Builder builder = BseBattlePickBox.newBuilder();
				builder.setSessionId(battleUser.getUserSessionKey().toString());

				builder.addBoxindex(boxIndex);
				this.treasureBox.remove(boxIndex);
				
				sendToOtherUsers(battleUser, builder.build());
				
				//Check the diamond collect boss
				/*
				if ( boss != null && boss.getBossPojo().getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
					if ( this.roundOwner != null ) {
						try {
							BossManager.getInstance().syncBossInstance(this.roundOwner.getUser(), boss.getId(), 
									1, 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				*/
				this.roundOwner.addTotalDiamonds(1);
			}
		}// for...
		
	}
	
	/**
	 * The round's owner send an attack to other bodies.
	 * 
	 * @param userSessionKey
	 * @param attck
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void roleAttack(final SessionKey userSessionKey, final BceRoleAttack attack) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser == null ) return;
		battleUser.setActionType(ActionType.FIRE);
		battleUser.setLastActionMillis(System.currentTimeMillis());
		
		/**
		 * It is a bug to update the AI user's position according to
		 * attack pos because the AI user's position may be updated
		 * by client between the attack.
		 * 
		 * wangqi 2012-07-30
		 */
		//Update the user's position
		battleUser.setPosX(attack.getUserx());
		battleUser.setPosX(attack.getUsery());
		if ( logger.isDebugEnabled() ) {
			logger.debug("roleAttack: user:{}, pos:{}:{}",
					new Object[]{
						battleUser.getUser().getRoleName(),
						attack.getUserx(), attack.getUsery()
					});
		}
		
		Collection<BattleUser> battleUsers = battleUserMap.values();

		RoleAttackTask task = new RoleAttackTask(
				this, battleUser, battleUsers, attack, roundWind);
		if ( BattleManager.getInstance().isUseDistributed() ) {
			logger.debug("Use distributed alogrithm");
			
			RoleAttack roleAttack = (RoleAttack)ScriptManager.getInstance().
					runScriptForObject(ScriptHook.BATTLE_BULLET_COUNT, 
							this, battleUser, attack, this.roundWind);
			BulletTrack[] tracks = roleAttack.getBulletTracks();
			if ( tracks != null && tracks.length>0 ) {
				BseBulletTrack.Builder builder = BseBulletTrack.newBuilder();
				builder.setSessionId(userSessionKey.toString());
				builder.setRoundNo(this.roundCount);
				for ( BulletTrack bullet : roleAttack.getBulletTracks() ) {
					builder.addBullet(bullet.toBseBullet());
				}
				sendToAllUsersExceptAI(builder.build());
			}
			
			BattleManager.getInstance().submitExecutor(task);
		} else {
			task.run();
		}

		/*
		if ( !battleUser.getUser().isAI() ) {
			StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
					StatAction.RoleAttack, attack.getPower(), attack.getAngle(), attack.getUserx(), attack.getUsery(), roundWind);
		}
		*/
	}
	
	/**
	 * Received all the bullets sent from clients.
	 * @param userSessionKey The user sent these bullets
	 * @param bullets
	 */
	public void bulletTrack(SessionKey userSessionKey, BulletTrack[] bullets, 
			int roundNumber) {
		if ( this.roundBulletTracks == null && roundNumber == this.roundCount ) {
			this.roundBulletTracks = bullets;
			logger.debug("#bulletTrack is set in battle:{}, round:{}.", 
					this.battleRoom.getSessionKey(), roundNumber);
			this.roundBulletTrackSemaphore.release();
		} else {
			logger.debug("#bulletTrack already set by other users in battle:{}, round:{}.", 
					this.battleRoom.getSessionKey(), roundNumber);
		}
	}
	
	/**
	 * The round's owner is moving
	 * @param userSessionKey
	 */
	public void roleMove(SessionKey userSessionKey, BceRoleMove move) {
		int x = move.getX();
		int y = move.getY();
		String sessionStr = move.getSessionId();
		SessionKey targetUserSessionKey = userSessionKey;
		if ( StringUtil.checkNotEmpty(sessionStr) ) {
			targetUserSessionKey = SessionKey.createSessionKeyFromHexString(sessionStr);
			if ( targetUserSessionKey == null ) {
				targetUserSessionKey = userSessionKey;
			}
		}
		
		BattleUser battleUser = this.battleUserMap.get(targetUserSessionKey);
		if ( battleUser != null ) {
			//Check mapheight
			if ( y >= 65535 ) {
				logger.debug("User {} is dropping out of screen", battleUser.getUser().getRoleName());
				battleUser.clearStatus();
				battleUser.addStatus(RoleStatus.DEAD);

				//the dropped user is not counted in kill users list.
				BattleUserAudit audit = null;
				BattleUser killer = null;
				if ( this.lastRoundOwner != null && lastRoundOwner != battleUser) {
					killer = lastRoundOwner;
					audit = lastRoundOwner.getHurtUser(
							battleUser.getUserSessionKey());
				} else if ( lastLastRoundOwner != null && lastLastRoundOwner != battleUser ) {
					killer = lastLastRoundOwner;
					audit = lastLastRoundOwner.getHurtUser(
							battleUser.getUserSessionKey());
				}
				if ( audit != null ) {
					audit.setDroped(true);
					battleUser.setKillerUser(killer);
				}
			} else {
				battleUser.setLastActionMillis(System.currentTimeMillis());
				battleUser.setPosX(x);
				battleUser.setPosY(y);
				
				BseRoleMove.Builder builder = BseRoleMove.newBuilder();
				builder.setSessionId(battleUser.getUserSessionKey().toString());
				builder.setX(x);
				builder.setY(y);
				logger.debug("Move the target user: {} to {}:{}", 
						new Object[]{battleUser.getUser().getRoleName(), battleUser.getPosX(), battleUser.getPosY()});
				
				//Call script
				ScriptResult result = GameContext.getInstance().getScriptManager().
						runScript(ScriptHook.BATTLE_ROLE_MOVE, battleUser);
		
				BseRoleMove roleMove = builder.build();
				sendToOtherAndAIUsers(battleUser, roleMove);
			}
			
			/*
			if ( !battleUser.getUser().isAI() ) {
				StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
					StatAction.RoleMove, x, y);
			}
			*/
		} else {
			logger.info("#roleMove: {} key's battle user is null", targetUserSessionKey);
		}
	}
	
	/**
	 * An user is using power attack
	 * @param userSessionKey
	 */
	public void rolePower(SessionKey userSessionKey, BceRolePower power) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser == null ) return;
		battleUser.setLastActionMillis(System.currentTimeMillis());
		battleUser.setPowerAttack(true);
		
		BseRolePower rolePower = BseRolePower.newBuilder().setSessionID(
				battleUser.getUserSessionKey().toString()).setFull(true).build();
				
		//Call script
		ScriptResult result = GameContext.getInstance().getScriptManager().
				runScript(ScriptHook.BATTLE_ROLE_POWER, battleUser);
		
		sendToOtherUsers(battleUser, rolePower);
		
		/*
		if ( !battleUser.getUser().isAI() ) {
			StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
				StatAction.RolePower);
		}
		*/
	}
	
	/**
	 * An user is using a extra buff or tool
	 * @param userSessionKey
	 * @param tool
	 */
	public void roleUseTool(SessionKey userSessionKey, BceRoleUseTool tool) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser == null ) return;
		int slotId = tool.getSlot();
		BuffToolIndex usedTool = BuffToolIndex.fromSlot(slotId);
		if ( usedTool == null ) {
			logger.debug("Invalid slotId {} for roleUseTool", slotId);
		} else {
			User user = battleUser.getUser();

			BuffToolType toolType = null;
			boolean canUseTool = false;
			List<BuffToolType> tools = user.getTools();
			//Should we store the user into db?
			boolean useStatusChanged = false;
						
			switch ( usedTool ) {
				case AttackOneMoreTimes:
					toolType = BuffToolType.AttackOneMoreTimes;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case AttackTwoMoreTimes:
					toolType = BuffToolType.AttackTwoMoreTimes;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case AttackThreeBranch:
					toolType = BuffToolType.AttackThreeBranch;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case Flying:
					toolType = BuffToolType.Fly;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					if ( canUseTool ) {
						battleUser.addStatus(RoleStatus.FLYING);
						battleUser.setActionType(ActionType.FLY);
					}
					break;
				case HurtAdd10:
					toolType = BuffToolType.HurtAdd10;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case HurtAdd20:
					toolType = BuffToolType.HurtAdd20;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case HurtAdd30:
					toolType = BuffToolType.HurtAdd30;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case HurtAdd40:
					toolType = BuffToolType.HurtAdd40;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case HurtAdd50:
					toolType = BuffToolType.HurtAdd50;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					break;
				case UsePower:
					//TODO Does power cost thew and add delay?
					toolType = BuffToolType.POWER;
					canUseTool = checkDelayAndThew(battleUser, toolType);
					if ( canUseTool ) {
						battleUser.setPowerAttack(true);
					}
					break;
				case UserTool1:
					if ( tools.size() > 0 ) {
						toolType = tools.get(0);
						if ( toolType != null ) {
							canUseTool = checkDelayAndThew(battleUser, toolType);
							if ( canUseTool ) {
								user.removeTool(0);
								useStatusChanged = true;
							}
						}
					}
					break;
				case UserTool2:
					if ( tools.size() > 1 ) {
						toolType = tools.get(1);
						if ( toolType != null ) {
							canUseTool = checkDelayAndThew(battleUser, toolType);
							if ( canUseTool ) {
								user.removeTool(1);
								useStatusChanged = true;
							}
						}
					}
					break;
				case UserTool3:
					if ( tools.size() > 2 ) {
						toolType = tools.get(2);
						if ( toolType != null ) {
							canUseTool = checkDelayAndThew(battleUser, toolType);
							if ( canUseTool ) {
								user.removeTool(2);
								useStatusChanged = true;
							}
						}
					}
					break;
			}
			if ( user.isAI() ) {
				canUseTool = true;
			}
			if ( canUseTool && toolType != null ) {
				logger.debug("User {} use tool {}", user.getRoleName(), toolType);
				battleUser.setLastActionMillis(System.currentTimeMillis());
				battleUser.setUsedTool(toolType);

				BseRoleUseTool bseUseTool = BseRoleUseTool.newBuilder().
						setBuffID(toolType.id()).
						setLaunchUserId(userSessionKey.toString()).
						setSessionId(userSessionKey.toString()).
						setSlot(slotId).setType(slotId).build();
				
				//Sync with client about the list
				BseToolList bseToolList = user.toBseToolList();
				XinqiMessage xinqi = new XinqiMessage();
				xinqi.payload = bseToolList;
				GameContext.getInstance().writeResponse(user.getSessionKey(), xinqi);
				
				//Call script to apply the BuffTool
				ScriptResult result = GameContext.getInstance().getScriptManager().
						runScript(ScriptHook.BATTLE_ROLE_USETOOL, this, battleUser, toolType);

				//Call the TaskHook
				TaskManager.getInstance().processUserTasks(battleUser.getUser(), 
						TaskHook.USE_TOOL, toolType);
				
				sendToOtherUsers(battleUser, bseUseTool);
				
				//Save the user tool status
				if ( useStatusChanged && !user.isAI() && !user.isProxy() ) {
					if ( GameContext.getInstance().getGameServerId().equals(user.getGameserverId())) {
						//In the same game server
						UserManager.getInstance().saveUser(user, false);
					} else {
						//In remote game server
						//At current time, use the refresh mode
						UserManager.getInstance().saveUser(user, false);
						BceUserRefresh.Builder builder = BceUserRefresh.newBuilder();
						builder.setRefreshmode(1);
						GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), 
								user.getGameserverId(), builder.build());
					}
				}
			} else {
				logger.debug("User has insufficient thew {} to use tool: {}", 
						battleUser.getThew(), toolType);
			}
			/*
			if ( !battleUser.getUser().isAI() ) {
				StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
					StatAction.RoleUseTool, toolType, canUseTool);
			}
			*/
		}
	}
		
	/**
	 * The user changes his/her direction
	 * @param userSessionKey
	 * @param dir
	 */
	public void changeDirection(SessionKey userSessionKey, RoleDirection dir) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser == null ) return;
		battleUser.setLastActionMillis(System.currentTimeMillis());
		battleUser.setDirection(dir);
		
		BseRoleChangeDirection roleDir = BseRoleChangeDirection.newBuilder().setSessionId(
				battleUser.getUserSessionKey().toString()).setDirection(dir.ordinal()).build();
		
		sendToOtherUsers(battleUser, roleDir);
		
		/*
		if ( !battleUser.getUser().isAI() ) {
			StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
				StatAction.RoleChangeDirection, dir);
		}
		*/
	}
	
	/**
	 * The client tells server an user is dead and the animation is done.
	 * @param userSessionKey
	 */
	public void roleDead(SessionKey userSessionKey) {
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser != null && !battleUser.containStatus(RoleStatus.DEAD) ) {
			battleUser.clearStatus();
			battleUser.addStatus(RoleStatus.DEAD);
			battleUser.setBlood(0);
			
		  //TODO Add statistic script 
			
			BseDead roleDead = BseDead.newBuilder().setSessionID(
					battleUser.getUserSessionKey().toString()).build();
			
			sendToOtherUsers(battleUser, roleDead);
			/*
			if ( !battleUser.getUser().isAI() ) {
				StatClient.getIntance().sendDataToStatServer(battleUser.getUser(), 
					StatAction.Dead);
			}
			*/		
		} else {
			logger.debug("The battleUser {} is already dead.", userSessionKey);
		}
	}
	
	/**
	 * The user leave the battle. Remove it from user list.
	 * @param userSessionKey
	 */
	public void leaveBattle(SessionKey userSessionKey ) {
		if ( userSessionKey == null ) {
			return;
		}
		BattleUser battleUser = this.battleUserMap.get(userSessionKey);
		if ( battleUser == null ) {
			logger.debug("#leaveBattle cannot find battleUser {} in the battle.", userSessionKey);
			return;
		}
		battleUser.setLeaveBattle(true);
		
		/**
		 * Leave battle should not delete user from battle
		 * 2012-11-16
		 */
		Jedis jedis = JedisFactory.getJedis();
		jedis.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SERVER_KEY);
		jedis.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SESSION_KEY);
		/*
		boolean online = true;
		try {
			online = jedis.exists(userSessionKey.toString());
		} catch (Exception e) {
			logger.warn("Jedis.exist exception: {}", e.getMessage());
			logger.debug("Stacktrace: ", e);
		}
		if ( online ) {
			//User is online. Clean his/her session data.
			jedis.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SERVER_KEY);
			jedis.hdel(battleUser.getUserSessionKey().toString(), BATTLE_SESSION_KEY);
	    
			logger.debug("User {} is leaving room. Remove it from battle.", battleUser.getUser().getRoleName());
		} else {
			logger.debug("User {} is offline. Remove it from battle.", battleUser.getUser().getRoleName());
		}
		*/
		
		//Send BseAskRoundOver to all users.
		/**
		 * If the leaving user is current round owner, switch the round immediatly
		 * wangqi 2012-10-12
		 */
		//roundOverWithoutStart(userSessionKey);
		if ( this.roundOwner != null && 
				userSessionKey.equals(this.roundOwner.getUserSessionKey()) ) {
			roundOver(userSessionKey, true);
		}
		if ( battleUserMap.values().size() <= 2 ) {
			//The battle should be ended
			if ( this.getBattleOverTimestamp() == Long.MAX_VALUE ) {
				BattleStatus status = BattleStatus.ROOM_IN_DRAW;
				if ( this.userLeftList.contains(battleUser) ) {
					status = BattleStatus.ROOM_RIGHT_WIN;
				} else if ( this.userRightList.contains(battleUser) ) {
					status = BattleStatus.ROOM_LEFT_WIN;
				}
				//The battleOver is not called yet
				BattleManager.getInstance().battleOverWithRoundOver(this, status);
				BattleManager.getInstance().battleEnd(this);
			}
		}
		
		/**
		 * Donot remove the user, instead, make him dead.
		 * 2012-11-16
		 */
		//Remove the user from battle
		//this.battleUserMap.remove(userSessionKey);
		roleDead(userSessionKey);
		//The user may exist in left room or right room. give it a try
		/*
		if ( !this.userLeftList.remove(battleUser) ) {
			this.userRightList.remove(battleUser);
		}
		*/
	}
	
	public final void checkOnlineUsers() {
		synchronized ( this ) {
			Jedis jedis = JedisFactory.getJedis();
			Collection<BattleUser> battleUsers = battleUserMap.values();
			for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser battleUser = (BattleUser) iterator.next();
				SessionKey sessionKey = battleUser.getUserSessionKey();
				boolean online = true;
				try {
					online = jedis.exists(sessionKey.toString());
				} catch (Exception e) {
					logger.warn("Jedis.exist exception: {}", e.getMessage());
					logger.debug("Stacktrace: ", e);
				}
				if ( !battleUser.getUser().isAI() && !battleUser.getUser().isProxy() ) {
					if ( !online ) {
						if (  battleUser.isOnline() ) {
							battleUser.setOnline(false);
							battleUser.setLeaveBattle(true);
							//Remove the user from battle
							//iterator.remove();
							//The user may exist in left room or right room. give it a try
							/*
							if ( this.userLeftList.remove(battleUser) ) {
							} else {
								this.userRightList.remove(battleUser);
							}
							*/
							/**
							 * Make him dead 
							 * 2012-11-16
							 */
							if ( !battleUser.containStatus(RoleStatus.DEAD) ) {
								roleDead(sessionKey);
							}
							//Check round owner
							if ( this.roundOwner == battleUser ) {
								roundOverWithoutStart(sessionKey);
								roundStart(false);
							}
							//GameContext.getInstance().getRoomManager().leaveRoom(battleUser.getUserSessionKey());
							
							logger.debug("User {} is offline. Remove it from battle and room.", battleUser.getUser().getRoleName());
						}
					} else {
						//User is online but he/she left the room. Remove it from battle.
						String bytes = jedis.hget(sessionKey.toString(), RedisRoomManager.H_ROOM_SESSION_KEY);
						SessionKey userRoomSessionKey = null;
						if ( bytes != null ) {
							userRoomSessionKey = SessionKey.createSessionKeyFromHexString(bytes);
						}
						if ( userRoomSessionKey == null || !userRoomSessionKey.equals(battleUser.getRoomSessionKey()) ) {
							/**
							 * Make him dead, instead of deletion 
							 * 2012-11-16
							 */
							roleDead(sessionKey);
							//iterator.remove();
							//The user may exist in left room or right room. give it a try
							//if ( !this.userLeftList.remove(battleUser) ) {
							//	this.userRightList.remove(battleUser);
							//}
							//logger.debug("User {} is leaving room. Remove it from battle.", battleUser.getUser().getRoleName());
							logger.debug("User {} is leaving room. make him dead.", battleUser.getUser().getRoleName());
						}
					}
				}
			}//for...
		}//syn...
	}
	
	/**
	 * Check two things:
	 * 1.) Remove those users who are offline.
	 * 2.) If there is one side that all users are dead or being offline, call battle over.
	 * 
	 */
	public final BattleStatus checkBattleUsers() {
		//This method will be called from multi-thread.
		if ( this.status == BattleStatus.NORMAL ) {
			synchronized ( this ) {
				BattleStatus status = BattleStatus.NORMAL;
				//1. Check if there are users being offline or leaving the room			
				checkOnlineUsers();
				
				//2. Check if one side wins.
				boolean hasRealUsers = false;
				boolean allLeftRealUserAreDead = true;
				int leftBlood = 0;
				int rightBlood = 0;
				for ( BattleUser bUser : this.userLeftList ) {
					leftBlood += bUser.getBlood();
					if ( bUser.containStatus(RoleStatus.DEAD) || bUser.getBlood() <= 0 ) {
						bUser.clearStatus();
						bUser.addStatus(RoleStatus.DEAD);
						if ( this.roundOwner == bUser ) {
							roundOverWithoutStart(bUser.getUserSessionKey());
							roundStart(false);
						}
						logger.debug("User {} is dead. Blood:{}", bUser.getUser().getRoleName(), bUser.getBlood());
					} else {
						allLeftRealUserAreDead = false;
					}
					User user = bUser.getUser();
					if ( !user.isAI() && !user.isBoss() && !bUser.isLeaveBattle() ) {
						hasRealUsers = true;
					}
				}
				boolean allRightRealUserAreDead = true;
				for ( BattleUser bUser : this.userRightList ) {
					rightBlood += bUser.getBlood();
					if ( bUser.containStatus(RoleStatus.DEAD) || bUser.getBlood() <= 0 ) {
						bUser.clearStatus();
						bUser.addStatus(RoleStatus.DEAD);
						if ( this.roundOwner == bUser ) {
							roundOverWithoutStart(bUser.getUserSessionKey());
							roundStart(false);
						}
						logger.debug("User {} is dead. Blood:{}", bUser.getUser().getRoleName(), bUser.getBlood());
					} else {
						allRightRealUserAreDead = false;
					}
					User user = bUser.getUser();
					if ( !user.isAI() && !user.isBoss() && !bUser.isLeaveBattle() ) {
						hasRealUsers = true;
					}
				}
				if ( !hasRealUsers ) {
					if ( roomType != RoomType.PVE_ROOM ) {
						return BattleStatus.ROOM_LEFT_WIN;
					} else {
						return BattleStatus.ROOM_RIGHT_WIN;
					}
				} else {
					if ( allLeftRealUserAreDead && allRightRealUserAreDead ) {
						if ( leftBlood > rightBlood ) {
							return BattleStatus.ROOM_LEFT_WIN;
						} else {
							return BattleStatus.ROOM_RIGHT_WIN;
						}
					} else  if ( allLeftRealUserAreDead ) {
						return BattleStatus.ROOM_RIGHT_WIN;
					} else if ( allRightRealUserAreDead ) {
						return BattleStatus.ROOM_LEFT_WIN;
					}
				}
				/*
				if ( allLeftDead && allRightDead ) {
					return BattleStatus.ROOM_IN_DRAW;
				} else if ( allLeftDead ) {
					return BattleStatus.ROOM_RIGHT_WIN;
				} else if ( allRightDead ) {
					return BattleStatus.ROOM_LEFT_WIN;
				}
				*/
				return status;
			}
		} else {
			return this.status;
		}
	}
	
	// -------------------------------------------------- Facility methods
	
	/**
	 * Send a message to other users in the battle.
	 * @param currentUser
	 * @param message
	 */
	public final void sendToOtherUsers(BattleUser currentUser, Message message) {
		Collection<BattleUser> battleUsers = battleUserMap.values();
		for (BattleUser otherUser : battleUsers) {
			if ( !otherUser.isLeaveBattle() && otherUser != currentUser && !otherUser.getUser().isProxy() ) {
				GameContext.getInstance().writeResponse(otherUser.getUserSessionKey(), message);
				//logger.debug("Send user {} the message {}", otherUser.getUser().getRoleName(), message.getClass());
			}
		}
	}
	
	/**
	 * Send a message to other users in the battle.
	 * @param currentUser
	 * @param message
	 */
	public final void sendToOtherAndAIUsers(BattleUser currentUser, Message message) {
		Collection<BattleUser> battleUsers = battleUserMap.values();
		for (BattleUser otherUser : battleUsers) {
			if ( !otherUser.isLeaveBattle() && (otherUser.getUser().isAI()) || (otherUser != currentUser && !otherUser.getUser().isProxy()) ) {
				GameContext.getInstance().writeResponse(otherUser.getUserSessionKey(), message);
			}
		}
	}
	
	/**
	 * Send a message to other users in the battle.
	 * @param currentUser
	 * @param message
	 */
	public final void sendToAllUsers(Message message) {
		Collection<BattleUser> battleUsers = battleUserMap.values();
		for (BattleUser user : battleUsers) {
			if ( !user.isLeaveBattle() ) {
				GameContext.getInstance().writeResponse(user.getUserSessionKey(), message);
			}
		}
	}
	
	/**
	 * Send a message to other users in the battle.
	 * @param currentUser
	 * @param message
	 */
	public final void sendToAllUsersExceptAI(Message message) {
		Collection<BattleUser> battleUsers = battleUserMap.values();
		for (BattleUser user : battleUsers) {
			if ( !user.isLeaveBattle() && !user.getUser().isAI() ) {
				GameContext.getInstance().writeResponse(user.getUserSessionKey(), message);
			}
		}
	}
	
	/**
	 * Get all the treasure box generated in combat.
	 * @return
	 */
	public final Map<Integer, Reward> getTreasureBox() {
		return this.treasureBox;
	}
		
	/**
	 * Get the Battle's SessionKey
	 * @return
	 */
	public final SessionKey getBattleSessionKey() {
		return this.battleRoom.getSessionKey();
	}
	
	/**
	 * Get the internal BattleUser map. Do not modify it.
	 * @return
	 */
	public final HashMap<SessionKey, BattleUser> getBattleUserMap() {
		return this.battleUserMap;
	}

	/**
	 * @return the status
	 */
	public BattleStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(BattleStatus status) {
		if ( this.status != status ) {
			this.status = status;
			this.statusChanged = true;
			this.enterStatusTimestamp = System.currentTimeMillis();
		}
	}
	
	/**
	 * Get the mark if the battle's status has been
	 * changed. After the query, the mark will be reset.
	 * @return
	 */
	public boolean isStatusChanged() {
		boolean changed = this.statusChanged;
		this.statusChanged = false;
		return changed;
	}
	
	/**
	 * @return the battleCreatedTimestamp
	 */
	public long getBattleCreatedTimestamp() {
		return battleCreatedTimestamp;
	}

	/**
	 * Return the timestamp that the battle is in current status.
	 * @return
	 */
	public long getEnterStatusTimestamp() {
		return this.enterStatusTimestamp;
	}

	/**
	 * @return the battleOverTimestamp
	 */
	public long getBattleOverTimestamp() {
		return battleOverTimestamp;
	}
	
	/**
	 * Send a chat message to all users in battle
	 */
	public void sendChatToAllUsers(SessionKey senderSessionKey, String message) {
		try {
			BattleUser sender = battleUserMap.get(senderSessionKey);
			BseChat.Builder chatBuilder = BseChat.newBuilder();
			chatBuilder.setMsgType(ChatType.ChatCurrent.ordinal());
			chatBuilder.setMsgContent(message);
			chatBuilder.setUsrId(sender.getUser().get_id().toString());
			String displayRoleName = sender.getUser().getRoleName();
			chatBuilder.setUsrNickname(displayRoleName);
			BseChat bseChat = chatBuilder.build();
			Collection<BattleUser> battleUsers = battleUserMap.values();
			for (BattleUser battleUser : battleUsers ) {
				if ( !battleUser.isLeaveBattle() ) {
					SessionKey userKey = battleUser.getUserSessionKey();
					GameContext.getInstance().writeResponse(userKey, bseChat);
				}
			}
			logger.debug("Successfully send chat '{}' to all users.", message);
		} catch (Throwable e) {
			logger.debug("Failed to send chat message to all users", e);
			logger.warn("Failed to send chat message to all users. exception: {}", e.getMessage());
		}
	}
	
	/**
	 * Send a chat message to all users in battle
	 */
	public void sendVoiceChatToAllUsers(SessionKey senderSessionKey, byte[] message, boolean autoplay, int seconds) {
		try {
			BattleUser sender = battleUserMap.get(senderSessionKey);
			BseVoiceChat.Builder chatBuilder = BseVoiceChat.newBuilder();
			chatBuilder.setMsgType(ChatType.ChatCurrent.ordinal());
			if ( autoplay ) {
				chatBuilder.setMsgContent(ByteString.copyFrom(message));
			} else {
				byte[] voiceId = ChatManager.getInstance().storeVoiceContent(message);
				chatBuilder.setVoiceid(ByteString.copyFrom(voiceId));
			}
			chatBuilder.setUsrId(sender.getUser().get_id().toString());
			chatBuilder.setAutoplay(autoplay);
			chatBuilder.setSecond(seconds);
			String displayRoleName = sender.getUser().getRoleName();
			chatBuilder.setUsrNickname(displayRoleName);
			BseVoiceChat bseChat = chatBuilder.build();
			Collection<BattleUser> battleUsers = battleUserMap.values();
			for (BattleUser battleUser : battleUsers ) {
				if ( !battleUser.isLeaveBattle() ) {
					SessionKey userKey = battleUser.getUserSessionKey();
					GameContext.getInstance().writeResponse(userKey, bseChat);
				}
			}
			logger.debug("Successfully send chat '{}' to all users.", message);
		} catch (Throwable e) {
			logger.debug("Failed to send chat message to all users", e);
			logger.warn("Failed to send chat message to all users. exception: {}", e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Battle [userLeftList=");
		builder.append(userLeftList);
		builder.append(", userRightList=");
		builder.append(userRightList);
		builder.append(", battleMap=");
		builder.append(battleMap);
		builder.append(", roundCount=");
		builder.append(roundCount);
		builder.append(", roundWind=");
		builder.append(roundWind);
		builder.append(", status=");
		builder.append(status);
		builder.append(", battleOverTimestamp=");
		builder.append(battleOverTimestamp);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Construct the role info
	 * @param user
	 * @param campId
	 * @param userInRoomIndex
	 * @return
	 */
	public RoleInfo makeRoleInfo(BattleUser battleUser, int campId, int userInRoomIndex) {
//		ArrayList<Point> startPoints = mapPojo.getStartPoints();
		/*
		ArrayList<Point> startPoints = this.battleMap.getMapPojo().getStartPoints();
		Point point = null;
		if ( startPoints != null && startPoints.size() > 0 ) {
			int baseIndex = 0;
			int limit = startPoints.size()/2;
			if ( campId == BattleCamp.LEFT.id() ) {
				baseIndex = (userInRoomIndex + (int)(MathUtil.nextDouble() * limit))%limit;
			} else if ( campId == BattleCamp.RIGHT.id() ) {
				baseIndex = (userInRoomIndex + (int)(MathUtil.nextDouble() * limit))%limit + limit;
			}
			point = startPoints.get(baseIndex);
			user.setPosX( point.x );
			user.setPosY( point.y );
		}
		*/
		RoleInfo roleInfo = battleUser.getUser().toRoleInfo(this.battleRoom.getSessionKey(), 
				campId, userInRoomIndex, new Point(battleUser.getPosX(), battleUser.getPosY()));
		
		return roleInfo;
	}
	
	/**
	 * Randomly pick all start points for all battle users.
	 * @param userList
	 * @param campId
	 * @param allPoints
	 */
	public void pickStartPoint(List<BattleUser> userList, int campId, ArrayList<Point> allPoints) {
		int limit = allPoints.size()/2;
		int base = 0, count = limit;
		if ( campId == BattleCamp.RIGHT.id() ) {
			base = limit;
			count = allPoints.size();
		}
		ArrayList<Point> startPoints = new ArrayList<Point>(limit);
		for ( int i=base; i<count; i++ ) {
			startPoints.add(allPoints.get(i));
		}
		int pickCount = userList.size();
		List<Point> points = null;
		if ( pickCount > 0 && startPoints.size() > 0 ) {
			points = MathUtil.randomPickShuffle(startPoints, pickCount);
		} else {
			logger.warn("pickStartPoint: startPoints:{}, pickCount:{}", startPoints, pickCount);
		}
		if ( points != null ) {
			for ( int i=0; i<points.size(); i++ ) {
				BattleUser bUser = userList.get(i);
				Point p = points.get(i);
				bUser.setPosX(p.x);
				bUser.setPosY(p.y);
			}
			for ( int i=points.size(); i<pickCount; i++ ) {
				BattleUser bUser = userList.get(i);
				Point p = points.get(i%points.size());
				bUser.setPosX(p.x);
				bUser.setPosY(p.y);
			}
		}
	}
		
	/**
	 * Make the real user list
	 * @param userInfos
	 * @return
	 */
	private List<BattleUser> makeUserList(List<UserInfo> userInfos, 
			SessionKey roomSessionKey, int count, int camp, RoomType roomType) {
		
		ArrayList<BattleUser> userList = new ArrayList<BattleUser>(count);
		for ( int i=0; i<userInfos.size(); i++ ) {
			UserInfo userInfo = userInfos.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			User user = GameContext.getInstance().findGlobalUserBySessionKey(userInfo.getUserSessionKey());
			if ( user != null ) {
				BattleUser battleUser = new BattleUser();
				battleUser.setUserSessionKey(userInfo.getUserSessionKey());
				battleUser.setRoomSessionKey(roomSessionKey);
				battleUser.setUser(user);
				battleUser.setBlood(user.getBloodTotal());
				battleUser.setEnergy(0);
				battleUser.setThew(user.getTkewTotal());
				battleUser.setCamp(camp);
				battleUser.setFriendUsers(userList);
				userList.add(battleUser);
				battleUserMap.put(battleUser.getUserSessionKey(), battleUser);
				if ( roomType != RoomType.DESK_ROOM && roomType != RoomType.TRAINING_ROOM ) {
					//Change the duration of all the equipments that user wearing
					ShopManager.getInstance().reduceUserEquipmentDuration(user);
				}
			}
		}
		
		return userList;
	}

	/**
	 * @return the battleRoom
	 */
	public BattleRoom getBattleRoom() {
		return battleRoom;
	}

	/**
	 * @return the roundOwner
	 */
	public BattleUser getRoundOwner() {
		return roundOwner;
	}
	
	/**
	 * @return the roundCount
	 */
	public int getRoundCount() {
		return roundCount;
	}
	
	/**
	 * @return the roundWind
	 */
	public int getRoundWind() {
		return roundWind;
	}

	/**
	 * @param roundWind the roundWind to set
	 */
	public void setRoundWind(int roundWind) {
		this.roundWind = roundWind;
	}

	/**
	 * @return the roundWindDir
	 */
	public int getRoundWindDir() {
		return roundWindDir;
	}

	/**
	 * @param roundWindDir the roundWindDir to set
	 */
	public void setRoundWindDir(int roundWindDir) {
		this.roundWindDir = roundWindDir;
	}

	/**
	 * @return the battleMap
	 */
	public BattleBitSetMap getBattleMap() {
		return battleMap;
	}

	/**
	 * @param battleMap the battleMap to set
	 */
	public void setBattleMap(BattleBitSetMap battleMap) {
		this.battleMap = battleMap;
	}
	
	/**
	 * @return the totalKill
	 */
	public int getTotalKill() {
		return totalKill.size();
	}

	/**
	 * @return the boss
	 */
	public Boss getBoss() {
		return boss;
	}

	/**
	 * @param boss the boss to set
	 */
	public void setBoss(Boss boss) {
		this.boss = boss;
	}

	/**
	 * @param totalKill the totalKill to set
	 */
	public void addTotalKill(SessionKey killedSessionKey) {
		this.totalKill.add(killedSessionKey);
	}
	
	/**
	 * Check if the user is already dead.
	 * @param killedSessionKey
	 * @return
	 */
	public boolean containsKill(SessionKey killedSessionKey) {
		return this.totalKill.contains(killedSessionKey);
	}
	
	/**
	 * Choose the proper map for two room
	 * @param left
	 * @param right
	 * @return
	 */
	public BattleBitSetMap chooseBattleMap(String leftMapId, String rightMapId) {
		BattleBitSetMap battleMap = null;
		String finalMapId = null;
		if ( leftMapId != null && rightMapId != null) {
			//Check if the owner is vip
			try {
				BattleUser leftOwner = this.battleUserMap.get(battleRoom.getRoomLeft().getOwnerSessionKey());
				BattleUser rightOwner = this.battleUserMap.get(battleRoom.getRoomRight().getOwnerSessionKey());
				if ( leftOwner.getUser().isVip() && !rightOwner.getUser().isVip() ) {
					finalMapId = leftMapId;
					SysMessageManager.getInstance().sendClientInfoMessage(rightOwner.getUser(), 
							"battlemap.vip", Action.NOOP, new Object[]{
						leftOwner.getUser().getRoleName()});
				} else if ( !leftOwner.getUser().isVip() && rightOwner.getUser().isVip() ) {
					finalMapId = rightMapId;
					SysMessageManager.getInstance().sendClientInfoMessage(leftOwner.getUser(), 
							"battlemap.vip", Action.NOOP, new Object[]{
						rightOwner.getUser().getRoleName()});
				} else {
					if ( MathUtil.nextDouble() < 0.5 ) {
						finalMapId = leftMapId;
						SysMessageManager.getInstance().sendClientInfoMessage(rightOwner.getUser(), 
								"battlemap.choose", Action.NOOP, new Object[]{
							leftOwner.getUser().getRoleName()});
					} else {
						finalMapId = rightMapId;
						SysMessageManager.getInstance().sendClientInfoMessage(leftOwner.getUser(), 
								"battlemap.choose", Action.NOOP, new Object[]{
							rightOwner.getUser().getRoleName()});
					}
				}
			} catch (Exception e) {
				logger.warn("Failed to choose a map", e);
			}
		} else if ( leftMapId != null ) {
			finalMapId = leftMapId;
		} else if ( rightMapId != null ){
			finalMapId = rightMapId;
		}
		if ( finalMapId != null ) {
			battleMap = BattleDataLoader4Bitmap.getBattleMapById(finalMapId);
		}
		if ( battleMap == null ) {
			battleMap = BattleDataLoader4Bitmap.getRandomBattleMap(this);
		}
		return battleMap;
	}
	
	/**
	 * When the battle is loading, send the stage ready status
	 */
	private void sendStageReadyStatus() {
		BseGetValue.Builder builder = BseGetValue.newBuilder();
		builder.setAction("battleload");
		for ( BattleUser battleUser : battleUserMap.values() ) {
			builder.addKeys(battleUser.getUser().getSessionKey().toString());
			String status = null;
			if ( battleUser.getUser().isAI() || battleUser.isStageReady() ) {
				status = Text.text("battle.load.ready", battleUser.getUser().getRoleName());
			} else {
				status = Text.text("battle.load.loading", battleUser.getUser().getRoleName());
			}
			builder.addValues(status);
		}
		Message message = builder.build();
		for ( BattleUser battleUser : battleUserMap.values() ) {
			if ( !battleUser.isLeaveBattle() && !battleUser.getUser().isAI() ) {
				GameContext.getInstance().writeResponse(battleUser.getUserSessionKey(), message);
			}
		}
	}
	
	/**
	 * Check if the battle user can use a BuffTool in a battle.
	 * If it is true, add the given delay and substract the thew.
	 * 
	 * @param battleUser
	 * @param toolType
	 * @param dataKey
	 * @return
	 */
	private boolean checkDelayAndThew(BattleUser battleUser, 
			BuffToolType toolType) {
		boolean canUseTool = false;
		GameDataKey thewDataKey = null; 
		GameDataKey delayDataKey = null;
		switch ( toolType ) {
			case POWER:
				thewDataKey = GameDataKey.THEW_POWER;
				delayDataKey = GameDataKey.DELAY_POWER;
				break;
			case AllHidden:
				thewDataKey = GameDataKey.THEW_TOOL_ALLHIDDEN;
				delayDataKey = GameDataKey.DELAY_TOOL_ALLHIDDEN;
				break;
			case AllRecover:
				thewDataKey = GameDataKey.THEW_TOOL_ALLRECOVER;
				delayDataKey = GameDataKey.DELAY_TOOL_ALLRECOVER;
				break;
			case Atom:
				thewDataKey = GameDataKey.THEW_TOOL_ATOM;
				delayDataKey = GameDataKey.DELAY_TOOL_ATOM;
				break;
			case AttackOneMoreTimes:
				thewDataKey = GameDataKey.THEW_TOOL_AttackOneMoreTimes;
				delayDataKey = GameDataKey.DELAY_TOOL_AttackOneMoreTimes;
				break;
			case AttackThreeBranch:
				thewDataKey = GameDataKey.THEW_TOOL_AttackThreeBranch;
				delayDataKey = GameDataKey.DELAY_TOOL_AttackThreeBranch;
				break;
			case AttackTwoMoreTimes:
				thewDataKey = GameDataKey.THEW_TOOL_AttackTwoMoreTimes;
				delayDataKey = GameDataKey.DELAY_TOOL_AttackTwoMoreTimes;
				break;
			case Energy:
				thewDataKey = GameDataKey.THEW_TOOL_ENERGY;
				delayDataKey = GameDataKey.DELAY_TOOL_ENERGY;
				break;
			case Fly:
				thewDataKey = GameDataKey.THEW_TOOL_FLY;
				delayDataKey = GameDataKey.DELAY_TOOL_FLY;
				break;
			case Guide:
				thewDataKey = GameDataKey.THEW_TOOL_GUIDE;
				delayDataKey = GameDataKey.DELAY_TOOL_GUIDE;
				break;
			case Hidden:
				thewDataKey = GameDataKey.THEW_TOOL_HIDDEN;
				delayDataKey = GameDataKey.DELAY_TOOL_HIDDEN;
				break;
			case HurtAdd10:
				thewDataKey = GameDataKey.THEW_TOOL_HurtAdd10;
				delayDataKey = GameDataKey.DELAY_TOOL_HurtAdd10;
				break;
			case HurtAdd20:
				thewDataKey = GameDataKey.THEW_TOOL_HurtAdd20;
				delayDataKey = GameDataKey.DELAY_TOOL_HurtAdd20;
				break;
			case HurtAdd30:
				thewDataKey = GameDataKey.THEW_TOOL_HurtAdd30;
				delayDataKey = GameDataKey.DELAY_TOOL_HurtAdd30;
				break;
			case HurtAdd40:
				thewDataKey = GameDataKey.THEW_TOOL_HurtAdd40;
				delayDataKey = GameDataKey.DELAY_TOOL_HurtAdd40;
				break;
			case HurtAdd50:
				thewDataKey = GameDataKey.THEW_TOOL_HurtAdd50;
				delayDataKey = GameDataKey.DELAY_TOOL_HurtAdd50;
				break;
			case Ice:
				thewDataKey = GameDataKey.THEW_TOOL_ICE;
				delayDataKey = GameDataKey.DELAY_TOOL_ICE;
				break;
			case NoHole:
				thewDataKey = GameDataKey.THEW_TOOL_NOHOLE;
				delayDataKey = GameDataKey.DELAY_TOOL_NOHOLE;
				break;
			case Recover:
				thewDataKey = GameDataKey.THEW_TOOL_RECOVER;
				delayDataKey = GameDataKey.DELAY_TOOL_RECOVER;
				break;
			case Wind:
				thewDataKey = GameDataKey.THEW_TOOL_WIND;
				delayDataKey = GameDataKey.DELAY_TOOL_WIND;
				break;
		}
		int thew = battleUser.getThew() - GameDataManager.getInstance().
				getGameDataAsInt(thewDataKey, 110);
		if ( thew >= 0 ) {
			battleUser.setThew( thew );
			/*
			int delay = battleUser.getDelay() + GameDataManager.getInstance().
					getGameDataAsInt(delayDataKey, 40);
			battleUser.setDelay( delay );
			
			logger.debug("User {} use tool {}, thew {}, delay {}", 
					new Object[]{battleUser.getUser().getUsername(), toolType, thew, delay});

			*/
			canUseTool = true;
			
		} else {
			canUseTool = false;
			logger.debug("User {} cannot use the tool {} for no thew", 
					battleUser.getUser().getRoleName(), toolType);
		}
		return canUseTool;
	}
	
	/**
	 * A role attack task that can be run asynchronized.
	 * @author wangqi
	 *
	 */
	private class RoleAttackTask implements Runnable {
		
		private Battle battle;
		private BattleUser battleUser;
		private Collection<BattleUser> battleUsers; 
		private BceRoleAttack attack;
		private int roundWind;
		
		RoleAttackTask( Battle battle, BattleUser battleUser, 
				Collection<BattleUser> battleUsers, 
				BceRoleAttack attack, int roundWind) {
			this.battle = battle;
			this.battleUser = battleUser;
			this.battleUsers = battleUsers;
			this.attack = attack;
			this.roundWind = roundWind;
		}
		
		public void run() {
			boolean success = false;
			if ( BattleManager.getInstance().isUseDistributed() ) {
				try {
					logger.debug("RoleAttack will wait on semaphore");
					success = roundBulletTrackSemaphore.tryAcquire(
							BattleManager.getInstance().getRoundBulletTrackSeconds(), 
							TimeUnit.MILLISECONDS);
					logger.debug("RoleAttack awaken on semaphore: success: {}", success);
				} catch (InterruptedException e) {
				}
			} else {
				success = true;
			}
			
			//Boolean sendRoundOver = Boolean.FALSE;
			long maxFlySeconds = 1;
			if ( success ) {
				//Call script
				ScriptResult result = GameContext.getInstance().getScriptManager().
						runScript(ScriptHook.BATTLE_BITMAP_ROLE_ATTACK, 
								Battle.this, battleUser, battleUsers, 
								battleMap, attack, this.roundWind, Battle.this.roundBulletTracks);
				
				if ( result.getType() == Type.SUCCESS_RETURN ) {
					Double sec = (Double)result.getResult().get(0);
					maxFlySeconds = (long)(sec.doubleValue()*1000)+1000;
					//sendRoundOver = (Boolean)result.getResult().get(1);
				}

				//logger.debug("Schedule the roundOver task after {} seconds.", maxFlySeconds);
			} else {
				//sendRoundOver = Boolean.TRUE;
			}
			/*
			final boolean userAskRoundOver = sendRoundOver.booleanValue();
			if ( userAskRoundOver ) {
				roundOver(battleUser.getUserSessionKey(), userAskRoundOver);
			} else {
				roundStart();
			}
			*/
			GameContext.getInstance().scheduleTask(new Runnable() {
				public void run() {
					roundOver(battleUser.getUserSessionKey(), false);
				}
			}, maxFlySeconds, TimeUnit.MILLISECONDS);
		}
	}
}
