package com.xinqihd.sns.gameserver.battle;

import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.HardMode;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.RpcRoomManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceChangeAutomode.BceChangeAutomode;
import com.xinqihd.sns.gameserver.proto.XinqiBceChangeMap.BceChangeMap;
import com.xinqihd.sns.gameserver.proto.XinqiBceEditSeat.BceEditSeat;
import com.xinqihd.sns.gameserver.proto.XinqiBceEnterRoom.BceEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBceLeaveRoom.BceLeaveRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBceReadyStart.BceReadyStart;
import com.xinqihd.sns.gameserver.proto.XinqiBseChangeAutomode.BseChangeAutomode;
import com.xinqihd.sns.gameserver.proto.XinqiBseChangeMap.BseChangeMap;
import com.xinqihd.sns.gameserver.proto.XinqiBseEditSeat.BseEditSeat;
import com.xinqihd.sns.gameserver.proto.XinqiBseEnterRoom.BseEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBseToolList.BseToolList;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserEnterRoom.BseUserEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserLeaveRoom.BseUserLeaveRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserReadyStart.BseUserReadyStart;
import com.xinqihd.sns.gameserver.proto.XinqiRoleInfo.RoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.rpc.MinaRpcPoolChannel;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It heavily depends on the Redis database and RPC to implement
 * distributed Room management. Thanks to Redis and Google.
 * 
 * @author wangqi
 *
 */
public class RedisRoomManager extends RoomManager {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisRoomManager.class);
	
	//The following is Redis Hash keys
	public static final String H_MACHINE_ID = "machineid";
	private static final String H_ROOM_TYPE = "room_type";
	private static final String H_ROOM_STATUS = "room_status";
	private static final String H_SESSION_KEY = "owner_session_key";
	private static final String[] H_USER_SESSION_KEYS = new String[]{
		"user_session_1", "user_session_2", 
		"user_session_3", "user_session_4"
	};
	private static final String[] H_USER_JOIN_KEYS = new String[]{
		"user_join_1", "user_join_2", 
		"user_join_3", "user_join_4"
	};
	private static final String H_MAX_USER_COUNT = "max_user_count";
	private static final String H_CURRENT_USER_COUNT = "current_user_count";
	private static final String H_REDAY_USER_COUNT = "ready_user_count";
	public static final String H_CURRENT_SET_NAME = "current_set_name";
	private static final String H_CREATED_DATE = "created_date";
	private static final String H_READY_DATE = "ready_date";
	private static final String H_OWNER_POWER = "owner_power";
	private static final String H_OWNER_LEVEL = "owner_level";
	private static final String H_AVG_POWER = "avg_power";
	private static final String H_MAP_ID = "map_id";
	private static final String H_MAX_LEVEL = "maxlv";
	
	private static final String H_ROOMKEY = "roomkey";
	private static final String H_AUTOMODE = "auto";

	private static final String H_READY_USERS = "ready_users";
	
	//The following is used in "user_room_map"
	public static final String H_ROOM_SESSION_KEY = "room_session_key";
	
	//The following are used as Redis zset's names.
	public static final String[] ZSET_UNFULL_NAME = {
		"room_unfull_set_1", "room_unfull_set_2", "room_unfull_set_3", "room_unfull_set_4"
	};
	public static final String ZSET_FULL_NAME = "room_full_set";
	public static final String[] ZSET_READY_NAME = {
		"room_ready_set_1", "room_ready_set_2", "room_ready_set_3", "room_ready_set_4"
	};
	
	public static final String ROOM_SESSION_KEY_PREFIX = "ROOM_";
	
	public static final String ROOM_COUNT_SUFFIX = ":count";
	public static final String ROOM_MAX_SUFFIX = ":max";
	
	/**
	 * The roomMaps stores all Room that are created by this JVM.
	 * With it, the Room's lock can work.
	 */
	private final ConcurrentHashMap<SessionKey, Room> roomMaps = new 
			ConcurrentHashMap<SessionKey, Room>();
	
	private String gameServerId = null;
	
	private String localRoomSetName = null;
	
	//The room's timeout before matching.
//	private int roomTimeout = 30000;
	//The user's timeout before he/she become ready.
//	private int userJoinTimeout = 15000;
	
	//The machineid to RpcRoomManager interface.
	private final HashMap<String, RpcRoomManager.RoomManager> machineRoomManagerMap = new 
			HashMap<String, RpcRoomManager.RoomManager>();
	
	/**
	 * The Room's checker.
	 */
	private final Checker checker = new Checker();
	
	/**
	 * Use {@link RoomManager#getInstance()} to acquire
	 * an instance.
	 */
	protected RedisRoomManager() {
		super();
		//Make sure the Jedis is initialized
		JedisFactory.initJedis();
		//Get the remote JVM id.
		gameServerId = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_GAME_SERVERID);
		if ( gameServerId == null ) {
			gameServerId = "localhost:3443";
		}
		localRoomSetName = "room_".concat(gameServerId);
//		logger.debug("rpcServerId: {}, localRoomSetName: {}", rpcServerId, localRoomSetName);
		
//		roomTimeout = GlobalConfig.getInstance().getIntProperty("room.ready.timeout");
//		userJoinTimeout = GlobalConfig.getInstance().getIntProperty("room.userjoin.timeout");
//		logger.debug("roomTimeout: {}, userJoinTimeout: {} ", roomTimeout, userJoinTimeout);
		//Initialize the remote call interface.
	}
	
	/**
	 * @return the localRoomSetName
	 */
	public String getLocalRoomSetName() {
		return localRoomSetName;
	}

	/**
	 * Schedule the room's checker's first run.
	 */
	@Override
	public void init() {
		//Start the room's checker
		Thread checkerThread = new Thread(checker);
		checkerThread.setName("RedisRoomManager");
		checkerThread.setDaemon(true);
		checkerThread.start();
		logger.debug("RedisRoomManager's checker starts.");
		/*
		GameContext.getInstance().scheduleTask(checker, 
				GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_READY_TIMEOUT, 20000), 
				TimeUnit.MILLISECONDS);
			*/		
	}

	/**
	 * Do nothing
	 */
	@Override
	public void destroy() {
	}

	/**
	 * Assign a new user a room. 
	 * If there are available rooms, pick one and assign the user in.
	 * The room may be in another remote JVM server. If there is no
	 * available room, or the user selected single-player mode, a 
	 * new room will be created. 
	 * 
	 * @param user
	 * @param roomType
	 * @return
	 */
	@Override
	public Room assignRoom(User user, RoomType roomType) {
		switch ( roomType ) {
			//PVE game room, up to 4 users.
			case PVE_ROOM:
				Room room = this.createRoom(user, RoomType.PVE_ROOM);
				return room;
			//Single player mode.
			case SINGLE_ROOM:
				room = this.createRoom(user, RoomType.SINGLE_ROOM);
				return room;
			//Multi users mode
			case MULTI_ROOM:
				room = this.popUnfullMultiUserRoom(user);
				if ( room == null ) {
					room = this.createRoom(user, RoomType.MULTI_ROOM);
				}
				return room;
			//Friend combat mode
			case FRIEND_ROOM:
				room = this.createRoom(user, RoomType.FRIEND_ROOM);
				return room;
			//On-machine mode.
			case DESK_ROOM:
				room = this.createRoom(user, RoomType.DESK_ROOM);
				return room;
			//Training room.
			case TRAINING_ROOM:
				room = this.createRoom(user, RoomType.TRAINING_ROOM);
				return room;
			//Challenge roo,
			case CHALLENGE_ROOM:
				break;
			//Rank competing room
			case RANK_ROOM:
				break;
			//Guild room.
			case GUILD_ROOM:
				room = this.createRoom(user, RoomType.GUILD_ROOM);
				return room;
			case OFFLINE_ROOM:
				room = this.createRoom(user, RoomType.OFFLINE_ROOM);
				return room;
			default:
				logger.warn("Unknown room type: {}", roomType);
				break;
		}
		return null;
	}

	/**
	 * In the original design, leaving a room means changing to a new room.
	 * In the current implementation, leaving a room means leaving a room
	 * without joining a new room.
	 * 
	 * @param userSessionKey
	 * @return
	 */
	@Override
	public boolean leaveRoom(SessionKey userSessionKey) {
		SessionKey roomSessionKey = this.findRoomSessionKeyByUserSession(userSessionKey);
		Room room = this.acquireRoom(roomSessionKey, true);
		if ( room == null ) {
			logger.debug("User {} leave a deleted room {}", userSessionKey, roomSessionKey);
			this.removeLocalRoom(roomSessionKey);
			return false;
		}
		if ( !room.isRemote() ) {
			kickUser(room, userSessionKey, true);
						
			//Check if the room is desk room. If it is, remove all proxy users.
			clearUserAIAndProxyUsers(room);
			
			BattleRoom battleRoom = room.getBattleRoom();
			if ( battleRoom != null ) {
				Room otherRoom = battleRoom.getRoomRight();
				if ( otherRoom == room ) {
					otherRoom = battleRoom.getRoomLeft();
				}
				boolean isAllAI = true;
				/**
				 * Check battle' two rooms are all AI users.
				 */
				List<UserInfo> users = room.getUserInfoList();
				for ( UserInfo userInfo : users ) {
					if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
					if ( !AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
						isAllAI = false;
						break;
					}
				}
				if ( isAllAI ) {
					users = otherRoom.getUserInfoList();
					for ( UserInfo userInfo : users ) {
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
						if ( !AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
							isAllAI = false;
							break;
						}
					}
				}
				if ( isAllAI || room.getRoomType() == RoomType.DESK_ROOM ) {
					logger.debug("Room {} is all AI or a desk room. Clean all AI or proxy users.", otherRoom.getRoomSessionKey());
					clearUserAIAndProxyUsers(otherRoom);
				}
			}	
		} else {
			String gameServerId = room.getGameServerId();
			logger.debug("User {} leaves a remote room in gameserver: {}", userSessionKey, gameServerId);
			BceLeaveRoom.Builder builder = BceLeaveRoom.newBuilder();
				
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
		}
		return false;
	}

	/**
	 * Clear AI users and proxy users in the given room
	 * @param room
	 */
	private void clearUserAIAndProxyUsers(Room room) {
		if ( room.getRoomStatus() == RoomStatus.COMBAT ) {
			logger.debug("Do not remove ai users from a combating room {}.", room.getRoomSessionKey().toString());
			return;
		}
		ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>(room.getUserInfoList());
		for ( int i=0; i<userInfos.size(); i++ ) {
			UserInfo userInfo = userInfos.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			if ( AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
				logger.debug("Remove {} user from room {} because it is AI.", 
						userInfo.getUserSessionKey(), room.getRoomSessionKey());
				kickUser(room, userInfo.getUserSessionKey(), true);
				//Remove the AI user after battle
				AIManager.getInstance().destroyAIUser(userInfo.getUserSessionKey());
			} else {
				User user = GameContext.getInstance().findLocalUserBySessionKey(userInfo.getUserSessionKey());
				if ( user != null && user.isProxy() ) {
					kickUser(room, userInfo.getUserSessionKey(), true);
					GameContext.getInstance().deregisterUserBySessionKey(userInfo.getUserSessionKey());
				}
			}
		}
	}
	
	/**
	 * If an user in a room turns in readyStart status, change the room's status.
	 * If all users are in ready status now, put the whole room is ready status.
	 * 
	 * @param userSessionKey
	 * @param isReady
	 * @return
	 */
	@Override
	public boolean readyStart(SessionKey userSessionKey, boolean isReady) {
		SessionKey roomSessionKey = this.findRoomSessionKeyByUserSession(userSessionKey);
		Room room = acquireRoom(roomSessionKey, true);
		if ( room == null ) {
			SysMessageManager.getInstance().sendClientInfoMessage(userSessionKey, "prompt.room.notexist", Type.NORMAL);
			return false;
		}
		if ( !room.isRemote() ) {
			try {
			room.lock();
			switch ( room.getRoomType() ) {
				case SINGLE_ROOM:
					User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
					if ( user != null ) {
						if ( user.getLevel() <= 3 ) {
							//对1级用户进行保护
							//The real user exist in a remove JVM
							User aiUser = AIManager.getInstance().createAIUser(user);
							int agility = user.getAgility()-1;
							if ( agility < 0 ) agility = 1;
							aiUser.setAgility(agility);
							AIManager.getInstance().registerAIUser(aiUser);
							Room aiRoom = RoomManager.getInstance().assignRoom(aiUser, RoomType.TRAINING_ROOM);
							BattleRoom battleRoom = RoomManager.getInstance().createBattleRoomWithoutBegin(room, aiRoom);
							BattleManager.getInstance().battleBegin(battleRoom);
							break;
						}
					}
				case MULTI_ROOM:
				case GUILD_ROOM:
				case OFFLINE_ROOM:
				{
					if ( isReady ) {
						if ( room.getRoomType() == RoomType.GUILD_ROOM && 
								room.getCurrentUserCount() == 1 ) {
							String message = Text.text("guild.combat.mincount");
							SysMessageManager.getInstance().sendClientInfoRawMessage(userSessionKey, message, 5000);
							break;
						}
						logger.debug("Room 's user {} is in ready status", userSessionKey);
						room.addReadyUser(userSessionKey);
						verifyReadyUserCount(room);
						room.setReadyStartMillis(System.currentTimeMillis());
						//Disable users' timeout
						List<UserInfo> userInfos = room.getUserInfoList();
						sendUserReadyStart(room, userSessionKey, userInfos, isReady);
						//Change the status and store it again.
						this.storeRoom(room);
						if ( room.getRoomType() == RoomType.GUILD_ROOM ) {
							BattleRoom battleRoom = room.getBattleRoom();
							if ( battleRoom != null ) { 
								Room otherRoom = battleRoom.getRoomLeft();
								if ( otherRoom == room ) {
									otherRoom = battleRoom.getRoomRight();
								}
								userInfos = otherRoom.getUserInfoList();
								sendUserReadyStart(otherRoom, userSessionKey, userInfos, isReady);
								boolean locked = otherRoom.tryLock();
								try {
									if ( locked ) {
										checkRoom(otherRoom);
									} else {
										logger.debug("Other room is locked by other thread and ignore checking.");
									}
								} finally {
									if ( locked ) {
										otherRoom.unlock();
									}
								}
								
								if ( room.getReadyUserCount() >= room.getCurrentUserCount() ) {
									logger.debug("Room {} turns in ready mode.", room.getRoomSessionKey());
									if ( otherRoom.getRoomStatus() == RoomStatus.DELETED ) {
										logger.debug("#readyStart: the opponent room is deleted. Try to random pick one", roomSessionKey);
										room.setBattleRoom(null);
										room.setMaxUserCount(room.getCurrentUserCount());
										room.setRoomStatus(RoomStatus.READY);
										this.storeRoom(room);									
									} else if ( otherRoom.getReadyUserCount() >= otherRoom.getCurrentUserCount() ) {
										BattleManager.getInstance().battleBegin(battleRoom);
									}
								}
							}
						}
						if ( room.getRoomStatus() == RoomStatus.READY ) {
							logger.debug("Room {} turns in ready mode.", room.getRoomSessionKey());
							return this.matchRoom(room);
						}
					} else {
						logger.debug("Room 's user {} cancels ready status", userSessionKey);
						List<UserInfo> userInfos = room.getUserInfoList();
						for ( UserInfo userInfo : userInfos ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
							BseUserReadyStart.Builder builder = BseUserReadyStart.newBuilder();
							builder.setReady(isReady);
							builder.setRoomId(room.getRoomSessionKey().toString());
							builder.setSessionId(userSessionKey.toString());
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), builder.build());
						}
						
						int count = room.getReadyUserCount()-1;
						room.setReadyUserCount(count<0?0:count);
						storeRoom(room);
					}	
				}
					break;
				case FRIEND_ROOM:
				{
					if ( isReady ) {
						final BattleRoom battleRoom = room.getBattleRoom();
						logger.debug("Room 's user {} is in ready status", userSessionKey);
						room.addReadyUser(userSessionKey);
						room.setReadyStartMillis(System.currentTimeMillis());
						verifyReadyUserCount(room);
						//Disable users' timeout
						List<UserInfo> userInfos = room.getUserInfoList();
						sendUserReadyStart(room, userSessionKey, userInfos, isReady);
						//Change the status and store it again.
						this.storeRoom(room);
						
						if ( battleRoom != null ) { 
							Room otherRoom = battleRoom.getRoomLeft();
							if ( otherRoom == room ) {
								otherRoom = battleRoom.getRoomRight();
							}
							userInfos = otherRoom.getUserInfoList();
							sendUserReadyStart(otherRoom, userSessionKey, userInfos, isReady);
							boolean locked = otherRoom.tryLock();
							try {
								if ( locked ) {
									checkRoom(otherRoom);
								} else {
									logger.debug("Other room is locked by other thread and ignore checking.");
								}
							} finally {
								if ( locked ) {
									otherRoom.unlock();
								}
							}
							
							if ( room.getReadyUserCount() >= room.getCurrentUserCount() ) {
								logger.debug("Room {} turns in ready mode.", room.getRoomSessionKey());
								if ( otherRoom.getRoomStatus() == RoomStatus.DELETED ) {
									logger.debug("#readyStart: the opponent room is deleted. Try to random pick one", roomSessionKey);
									room.setBattleRoom(null);
									room.setMaxUserCount(room.getCurrentUserCount());
									room.setRoomStatus(RoomStatus.READY);
									this.storeRoom(room);									
								} else if ( otherRoom.getReadyUserCount() >= otherRoom.getCurrentUserCount() ) {
									BattleManager.getInstance().battleBegin(battleRoom);
								}
							}
						} else {
							logger.debug("#readyStart: there is no opponent room. Try to random pick one", roomSessionKey);
							if ( room.getReadyUserCount() >= room.getCurrentUserCount() ) {
								room.setMaxUserCount(room.getCurrentUserCount());
								room.setRoomStatus(RoomStatus.READY);
								this.storeRoom(room);
							}
						}
						
					} else {
						logger.debug("Room 's user {} cancels ready status", userSessionKey);
						List<UserInfo> userInfos = room.getUserInfoList();
						for ( UserInfo userInfo : userInfos ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
							BseUserReadyStart.Builder builder = BseUserReadyStart.newBuilder();
							builder.setReady(isReady);
							builder.setRoomId(room.getRoomSessionKey().toString());
							builder.setSessionId(userSessionKey.toString());
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), builder.build());
						}
						
						int count = room.getReadyUserCount()-1;
						room.setReadyUserCount(count<0?0:count);
						storeRoom(room);
					}	
				}
					break;
				case DESK_ROOM:
				{
					if ( isReady ) {
						final BattleRoom battleRoom = room.getBattleRoom();
						logger.debug("Room 's user {} is in ready status", userSessionKey);
						room.setReadyUserCount(room.getReadyUserCount()+1);
						room.setReadyStartMillis(System.currentTimeMillis());
						//Update user ready status
						List<UserInfo> userInfos = room.getUserInfoList();
						sendUserReadyStart(room, userSessionKey, userInfos, isReady);
						//Change the status and store it again.
						this.storeRoom(room);
						
						if ( battleRoom != null ) { 
							Room otherRoom = battleRoom.getRoomLeft();
							if ( otherRoom == room ) {
								otherRoom = battleRoom.getRoomRight();
							}
							userInfos = otherRoom.getUserInfoList();
							sendUserReadyStart(otherRoom, userSessionKey, userInfos, isReady);
							this.storeRoom(otherRoom);
							
							logger.debug("Room {} turns in ready mode.", room.getRoomSessionKey());
							BattleManager.getInstance().battleBegin(battleRoom);
						} else {
							logger.debug("#readyStart: there is opponent room. Try to random pick one", roomSessionKey);
						}
						
					} else {
						logger.debug("Room 's user {} cancels ready status", userSessionKey);
						List<UserInfo> userInfos = room.getUserInfoList();
						for ( UserInfo userInfo : userInfos ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
							BseUserReadyStart.Builder builder = BseUserReadyStart.newBuilder();
							builder.setReady(isReady);
							builder.setRoomId(room.getRoomSessionKey().toString());
							builder.setSessionId(userSessionKey.toString());
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), builder.build());
						}
						
						int count = room.getReadyUserCount()-1;
						room.setReadyUserCount(count<0?0:count);
						storeRoom(room);
					}	
				}
					break;
				case TRAINING_ROOM:
				{
					//The real user exist in a remove JVM
					UserId realUserId = GameContext.getInstance().findUserIdBySessionKey(room.getOwnerSessionKey());
					User realUser = null;
					if (realUserId != null) {
						realUser = UserManager.getInstance().queryUser(realUserId);
					}
					User aiUser = UserManager.getInstance().createTrainingUser(realUser);
					AIManager.getInstance().registerAIUser(aiUser);
					Room aiRoom = RoomManager.getInstance().assignRoom(aiUser, RoomType.TRAINING_ROOM);
					BattleRoom battleRoom = RoomManager.getInstance().createBattleRoomWithoutBegin(room, aiRoom);
					BattleManager.getInstance().battleBegin(battleRoom);
					break;
				}
				case PVE_ROOM:
				{
					boolean canChallengeBoss = true;
					if ( isReady ) {
						//The real user exist in a remove JVM
						User realUser = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
						room.addReadyUser(userSessionKey);
						verifyReadyUserCount(room);
						room.setReadyStartMillis(System.currentTimeMillis());
						List<UserInfo> userInfos = room.getUserInfoList();
						sendUserReadyStart(room, userSessionKey, userInfos, isReady);
						if ( room.getReadyUserCount() >= room.getCurrentUserCount() ) {
							//roomLeft.setMapId("18");
							Boss boss = (Boss)realUser.getUserData(BossManager.USER_BOSS_ID);
							HardMode hardMode = (HardMode)realUser.getUserData(BossManager.BOSS_HARDMODE);
							if ( boss != null ) {
								BossManager manager = BossManager.getInstance();
								BossPojo bossPojo = boss.getBossPojo();
								user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
								canChallengeBoss = manager.challengeBoss(user, boss, System.currentTimeMillis());
								if ( canChallengeBoss ) {
									User[] bossUsers = manager.convertToBossUsers(bossPojo, boss, user);
									for ( User bossUser : bossUsers ) {
										AIManager.getInstance().registerAIUser(bossUser);
									}
									Room bossRoom = RoomManager.getInstance().assignRoom(bossUsers[0], RoomType.PVE_ROOM);
									bossRoom.setMaxUserCount(bossUsers.length);
									for ( int i=1; i<bossUsers.length; i++ ) {
										RoomManager.getInstance().joinRoom(bossRoom.getRoomSessionKey(), bossUsers[i].getSessionKey());
									}
									bossRoom.setMapId(bossPojo.getMapId());
									BattleRoom battleRoom = RoomManager.getInstance().createBattleRoomWithoutBegin(room, bossRoom);
									Battle battle = BattleManager.getInstance().battleBegin(battleRoom);
									battle.setBoss(boss);
								} else {
									SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.challenge.runout", Type.NORMAL);
								}
							} else {
								logger.warn("Given boss is unavailable in system.");
							}
						}
					} else {
						canChallengeBoss = false;
						logger.debug("Room 's user {} cancels ready status", userSessionKey);
					}
					
					if ( !canChallengeBoss ) {
						List<UserInfo> userInfos = room.getUserInfoList();
						for ( UserInfo userInfo : userInfos ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
							BseUserReadyStart.Builder builder = BseUserReadyStart.newBuilder();
							builder.setReady(isReady);
							builder.setRoomId(room.getRoomSessionKey().toString());
							builder.setSessionId(userSessionKey.toString());
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), builder.build());
						}
						
						int count = room.getReadyUserCount()-1;
						room.setReadyUserCount(count<0?0:count);
						storeRoom(room);
					}
				}
				}
			} finally {
				room.unlock();
			}
		} else {
			String gameServerId = room.getGameServerId();
			logger.debug("User {} ready start a remote room in gameserver: {}", userSessionKey, gameServerId);
			BceReadyStart.Builder builder = BceReadyStart.newBuilder();
			builder.setIsReady(isReady);
			
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
		}
		return false;
	}

	/**
	 * Verify the user ready start status
	 * @param room
	 */
	private void verifyReadyUserCount(Room room) {
		//Verify user ready count
		if ( room.getMaxUserCount() <= 0 || 
				room.getMaxUserCount() < room.getCurrentUserCount() ) {
			room.setMaxUserCount(room.getCurrentUserCount());
		}
		if ( room.getReadyUserCount() < room.getMaxUserCount() ) {
			Set<SessionKey> readySet = room.getReadyUserSet();
			int readyCount = readySet.size();
			for ( UserInfo userInfo : room.getUserInfoList() ) {
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				SessionKey usk = userInfo.getUserSessionKey();
				if ( !readySet.contains(usk) ) {
					User user = GameContext.getInstance().findLocalUserBySessionKey(usk);
					if ( user != null && (user.isAI() || user.isProxy()) ) {
						readyCount++;
					}
				}
			}
			room.setReadyUserCount(readyCount);
		}
	}
	
	/**
	 * The user wants to change the room's default map.
	 * 
	 * @param userSessionKey
	 * @return
	 */
	@Override
	public boolean changeMap(SessionKey userSessionKey, String mapId) {
		SessionKey roomSessionKey = this.findRoomSessionKeyByUserSession(userSessionKey);
		Room room = acquireRoom(roomSessionKey, true);
		if ( room == null ) {
			return false;
		}
		if ( !room.isRemote() ) {
			try {
				room.lock();
				if ( userSessionKey != null 
						&& userSessionKey.equals(room.getOwnerSessionKey()) ) {
					if ( StringUtil.checkNotEmpty(mapId) ) {
						room.setMapId(mapId);
						storeRoom(room);
						List<UserInfo> users = room.getUserInfoList();
						for ( UserInfo userInfo : users ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO || userInfo.getUserSessionKey() == null ) continue;
							BseChangeMap.Builder builder = BseChangeMap.newBuilder();
							builder.setMapID(StringUtil.toInt(mapId, -1));
							builder.setChangeUserID(userSessionKey.toString());
							GameContext.getInstance().writeResponse(
									userInfo.getUserSessionKey(), builder.build());
						}
						return true;
					} else {
						logger.debug("User {} choose random map", userSessionKey);
						room.setMapId(null);
						storeRoom(room);
					}
				} else {
					logger.debug("User {} is not the room owner and cannot change map.", userSessionKey);
				}
			} finally {
				room.unlock();
			}
		} else {
			String gameServerId = room.getGameServerId();
			logger.debug("User {} changeMap a remote room in gameserver: {}", userSessionKey, gameServerId);
			BceChangeMap.Builder builder = BceChangeMap.newBuilder();
			builder.setMapID(StringUtil.toInt(mapId, 1));
			
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
		}
		return false;
	}
	
	/**
	 * The user wants to change the room's default map.
	 * 
	 * @param userSessionKey
	 * @return
	 */
	@Override
	public boolean changeAutomode(SessionKey userSessionKey, boolean autoMode) {
		SessionKey roomSessionKey = this.findRoomSessionKeyByUserSession(userSessionKey);
		Room room = acquireRoom(roomSessionKey, true);
		if ( room == null ) {
			return false;
		}
		if ( !room.isRemote() ) {
			try {
				room.lock();
				if ( userSessionKey != null 
						&& userSessionKey.equals(room.getOwnerSessionKey()) ) {
					boolean allowAutoMode = checkRoomAllowAutoMode(room);
					if ( allowAutoMode ) {
						List<UserInfo> users = room.getUserInfoList();
						for ( UserInfo userInfo : users ) {
							if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO || userInfo.getUserSessionKey() == null ) continue;
							BseChangeAutomode.Builder builder = BseChangeAutomode.newBuilder();
							builder.setAutomode(autoMode);
							builder.setSessionid(userInfo.getUserSessionKey().toString());
							GameContext.getInstance().writeResponse(
									userInfo.getUserSessionKey(), builder.build());
						}
						return true;
					} else {
						SysMessageManager.getInstance().sendClientInfoMessage(userSessionKey, "room.denyauto", Type.CONFIRM);
					}
				} else {
					logger.debug("User {} is not the room owner and cannot change map.", userSessionKey);
				}
			} finally {
				room.unlock();
			}
		} else {
			String gameServerId = room.getGameServerId();
			logger.debug("User {} changeMap a remote room in gameserver: {}", userSessionKey, gameServerId);
			BceChangeAutomode.Builder builder = BceChangeAutomode.newBuilder();
			builder.setAutomode(autoMode);
			
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
		}
		return false;
	}

	/**
	 * In a multi-user room, the owner can open or close a seat. 
	 * If a new seat is opened, the room will be changed to unfull set and waiting for new users.
	 * If a seat is closed, the user at that seat will be removed and assign to another room. If 
	 * all the left users are ready, the room changes to 'ready' mode. If all left seats have users,
	 * the room will be changed to 'full' status. 
	 * 
	 * 
	 * @param userSessionKey
	 * @param seatIndex
	 * @param isOpen
	 * @return
	 */
	@Override
	public boolean editSeat(SessionKey userSessionKey, int seatIndex,
			boolean isOpen) {
		SessionKey roomSessionKey = this.findRoomSessionKeyByUserSession(userSessionKey);
		Room room = acquireRoom(roomSessionKey, true);
		if ( room == null ) {
			return false;
		}
		if ( !room.isRemote() ) {
			try {
				room.lock();
				if ( room.getRoomType() == RoomType.SINGLE_ROOM || 
						room.getRoomType() == RoomType.TRAINING_ROOM  ) {
					return false;
				}
				
				if ( userSessionKey != null 
						&& userSessionKey.equals(room.getOwnerSessionKey()) ) {
					if ( room.getRoomStatus() == RoomStatus.READY || room.getRoomStatus() == RoomStatus.COMBAT ) {
						SysMessageManager.getInstance().sendClientInfoRawMessage(userSessionKey, Text.text("room.ready.editseat"), 2000);
					} else {
						if ( seatIndex >=0 && seatIndex < Room.MAX_USER ) {
							List<UserInfo> userInfoList = room.getUserInfoList();
							UserInfo oldUserInfo = userInfoList.get(seatIndex);
							if ( isOpen ) {
								if ( oldUserInfo == Room.BLOCKED_USER_INFO ) {
									room.setUser(null, seatIndex);
									room.setMaxUserCount(room.getMaxUserCount()+1);
									room.setRoomStatus(RoomStatus.UNFULL);
									storeRoom(room);
								}
								
								BseEditSeat.Builder builder = BseEditSeat.newBuilder();
								builder.setIndex(seatIndex);
								builder.setSessionID(userSessionKey.toString());
								builder.setOpen(isOpen);
								
								GameContext.getInstance().writeResponse(userSessionKey, builder.build());
								
								return true;
							} else {
								if ( oldUserInfo == null || oldUserInfo == Room.BLOCKED_USER_INFO ) {
									if ( oldUserInfo == null ) {
										room.setMaxUserCount(room.getMaxUserCount()-1);
										if ( room.getCurrentUserCount() >= room.getMaxUserCount() ) {
											if ( room.getReadyUserCount() >= room.getMaxUserCount() ) {
												room.setRoomStatus(RoomStatus.READY);
											} else {
												room.setRoomStatus(RoomStatus.FULL);
											}
										}
									}
									room.setUser(Room.BLOCKED_USER_INFO, seatIndex);
									storeRoom(room);
									
									BseEditSeat.Builder builder = BseEditSeat.newBuilder();
									builder.setIndex(seatIndex);
									builder.setSessionID(userSessionKey.toString());
									builder.setOpen(isOpen);
									
									GameContext.getInstance().writeResponse(userSessionKey, builder.build());
									
									return true;
								} else {
									logger.debug("User {} cannot close seatIndex {} because it is not empty", userSessionKey, seatIndex);
									
									BseEditSeat.Builder builder = BseEditSeat.newBuilder();
									builder.setOpen(false);
									GameContext.getInstance().writeResponse(userSessionKey, builder.build());
									
									return false;
								}
							}						
						} else {
							logger.debug("seatIndex {} is out of range.", seatIndex);
							BseEditSeat.Builder builder = BseEditSeat.newBuilder();
							builder.setOpen(false);
							builder.setSessionID(userSessionKey.toString());
							builder.setIndex(0);
							GameContext.getInstance().writeResponse(userSessionKey, builder.build());
						}
					}
				} else {
					logger.debug("User {} is not the room owner and cannot edit seat.", userSessionKey);
					BseEditSeat.Builder builder = BseEditSeat.newBuilder();
					builder.setOpen(false);
					GameContext.getInstance().writeResponse(userSessionKey, builder.build());
				}
			} finally {
				room.unlock();
			}
		} else {
			String gameServerId = room.getGameServerId();
			logger.debug("User {} editSeat a remote room in gameserver: {}", userSessionKey, gameServerId);
			BceEditSeat.Builder builder = BceEditSeat.newBuilder();
			builder.setIndex(seatIndex);
			builder.setOpen(isOpen);
			
			GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, builder.build());
		}

		return false;
	}

	/**
	 * When the room is created, it is in CREATED status, and waiting for users to join.
	 * When the user number in the room equals to its max user count, the room is in FULL status.
	 * After all users in room are in ready status, the room is in READY status and waiting for match.
	 * If two rooms are picked up and matched, they are both in COMBAT status. 
	 * After the combat, both rooms are back to FULL status.
	 * 
	 * If anyone leaves the room (because offline or user timeout), the room's status will be changed.
	 * COMBAT
	 * READY -> UNFULL
	 * FULL  -> UNFULL
	 * UNFULL -> DELETED
	 * 
	 * 
	 * @param room
	 * @return
	 */
	@Override
	public void checkRoom(Room room) {
		if( room == null || room.isRemote() || room.getRoomStatus() == RoomStatus.DELETED ) {
			return;
		}
		try {
			room.lock();
			
			//1.> Check if all users are still online. Kick those offline users.
			List<UserInfo> userList = room.getUserInfoList();
			for ( UserInfo userInfo : userList ) {
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				SessionKey userSessionKey = userInfo.getUserSessionKey();
				if ( GameContext.getInstance().findUserIdBySessionKey(userSessionKey) == null ) {
					//User is offline
					logger.debug("Kick user {} because he is offline", userInfo.getUserSessionKey() );
					this.kickUser(room, userSessionKey, true);
				}
			}
			
			//Check the room owner
			/*
			SessionKey ownerSessionKey = room.getOwnerSessionKey();
			if ( GameContext.getInstance().findUserIdBySessionKey(ownerSessionKey) == null ) {
				//TODO
			}
			*/
			
			//Check room size
			/*
			int currentRoomCount = 0;
			int maxRoomCount = 0;
			for ( int i=0; i<Room.MAX_USER; i++ ) {
				UserInfo userInfo = userList.get(i);
				if ( userInfo != Room.BLOCKED_USER_INFO ) {
					maxRoomCount++;
					if ( userInfo != null ) {
						currentRoomCount++;
					}
				}
			}
			room.setMaxUserCount(maxRoomCount);
			*/
			
			//2.> Do specific RoomType's user checking.
			RoomType roomType = room.getRoomType();
			switch ( roomType ) {
				//PVE game room, up to 4 users.
				case PVE_ROOM:
					break;
				//Single player mode.
				case SINGLE_ROOM:
					break;
				//Multi users mode
				case MULTI_ROOM:
				  //2.> Check if all users do not timeout. Kick those timeout users.
					/*
					userList = room.getUserInfoList();
					//The room owner which index is 0 should not be kicked.
					for ( int i=1; i<userList.size(); i++ ) {
						UserInfo userInfo = userList.get(i);
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
						SessionKey userSessionKey = userInfo.getUserSessionKey();
						if ( System.currentTimeMillis() - userInfo.getUserJoinTime() > 
							GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_JOIN_TIMEOUT, 15000)) {
					  	//User is timeout, change him to a new room.
							logger.debug("Kick user {} because he is timeout", userInfo.getUserJoinTime() );
							this.kickUser(room, userSessionKey);
							//TODO assign the user to a new room.
						}
					}
					*/
					break;
				//Friend combat mode
				case FRIEND_ROOM:
					break;
				//On-machine mode.
				case DESK_ROOM:
					break;
				//Training room.
				case TRAINING_ROOM:
					break;
				//Challenge roo,
				case CHALLENGE_ROOM:
					break;
				//Rank competing room
				case RANK_ROOM:
					break;
				//Guild room.
				case GUILD_ROOM:
					break;
				case OFFLINE_ROOM:
					break;
				default:
					logger.warn("Unknown room type: {}", roomType);
					break;
			}
			
			//If room is empty, delete it.
			boolean roomDelete = deleteRoomIfEmpty(room);

			if ( !roomDelete ) {
				RoomStatus currentStatus = room.getRoomStatus();
				RoomStatus nextStatus = null;
				while ( nextStatus != room.getRoomStatus() ) {
					nextStatus = room.getRoomStatus();
					switch ( room.getRoomStatus() ) {
						case UNFULL:
							if ( room.getCurrentUserCount() >= room.getMaxUserCount() ) {
								nextStatus = RoomStatus.FULL;
							} else if ( room.getCurrentUserCount() == 0 ) {
								nextStatus = RoomStatus.DELETED;
							}
							break;
						case FULL:
							if ( room.getReadyUserCount() >= room.getMaxUserCount() ) {
								nextStatus = RoomStatus.READY;
							} else {
								if ( room.getCurrentUserCount() < room.getMaxUserCount() ) {
									nextStatus = RoomStatus.UNFULL;
								}
							}
							break;
						case READY:
							if ( room.getReadyUserCount() < room.getMaxUserCount() ) {
								nextStatus = RoomStatus.FULL;
							}
							break;
						case COMBAT:
							break;
						default:
							logger.warn("Room is in unproper status: {}", room.getRoomStatus());
							break;
					}
					room.setRoomStatus(nextStatus);
				}
				if ( currentStatus != nextStatus ) {
					logger.debug("Room will change from {} to {}", currentStatus, nextStatus);
				}
			}
		} finally {
			room.unlock();
		}
	}

	// ----------------------------------------------- Supporting methods.
	
	/**
	 * Create a new room and managed it in current JVM.
	 */
	@Override
	public Room createRoom(User user, RoomType roomType) {
		return createRoom(user, roomType, 0);
	}
	
	/**
	 * Create a new room and managed it in current JVM.
	 * @param user The current user who create the room.
	 * @param roomType
	 * @return
	 */
	@Override
	public Room createRoom(User user, RoomType roomType, int basePos) {
		SessionKey roomSessionKey = SessionKey.createSessionKeyFromRandomString(ROOM_SESSION_KEY_PREFIX);
		return createRoom(user, roomType, basePos, roomSessionKey);
	}
	
	@Override
	public Room createRoom(User user, RoomType roomType, int basePos, SessionKey roomSessionKey) {
		//Store the roomType in user's data
		user.putUserData(ROOM_TYPE_KEY, roomType);
		
		Room room = new Room();
		room.setRoomSessionKey(roomSessionKey);
		room.setGameServerId(gameServerId); 
		room.setRoomType(roomType);
		room.setRoomStatus(RoomStatus.UNFULL);
		room.setOwnerSessionKey(user.getSessionKey());
		//Add the room owner.
		UserInfo userInfo = new UserInfo();
		userInfo.setUserJoinTime(System.currentTimeMillis());
		userInfo.setUserSessionKey(user.getSessionKey());
		room.addUser(userInfo);
		int maxUserCount = 2;
		switch ( roomType ) {
			//PVE game room, up to 4 users.
			case PVE_ROOM:
				maxUserCount = 2;
				room.setUser(Room.BLOCKED_USER_INFO, 2);
				room.setUser(Room.BLOCKED_USER_INFO, 3);
				break;
			//Single player mode.
			case SINGLE_ROOM:
				maxUserCount = 1;
				break;
			//Multi users mode
			case MULTI_ROOM:
				maxUserCount = 2;
				room.setUser(Room.BLOCKED_USER_INFO, 2);
				room.setUser(Room.BLOCKED_USER_INFO, 3);
				break;
			//Friend combat mode
			case FRIEND_ROOM:
				maxUserCount = 4;
				break;
			//On-machine mode.
			case DESK_ROOM:
				maxUserCount = 4;
				break;
			//Training room.
			case TRAINING_ROOM:
				maxUserCount = 1;
				break;
			//Challenge roo,
			case CHALLENGE_ROOM:
				maxUserCount = 2;
				break;
			//Rank competing room
			case RANK_ROOM:
				maxUserCount = 2;
				break;
			//Guild room.
			case GUILD_ROOM:
				maxUserCount = 2;
				break;
			case OFFLINE_ROOM:
				maxUserCount = 1;
			default:
				logger.warn("Unknown room type: {}", roomType);
				break;
		}
		room.setMaxUserCount(maxUserCount);
		room.setReadyUserCount(0);
		room.setCreatedMillis(System.currentTimeMillis());
		room.setOwnerPower(user.getPower());
		room.setAveragePower(user.getPower());
		room.setOwnerLevel(user.getLevel());
		room.setMaxLevel(user.getLevel());
		if ( maxUserCount <= 1 ) {
			room.setCurrentSetName(ZSET_FULL_NAME);
		} else {
			room.setCurrentSetName(ZSET_UNFULL_NAME[maxUserCount-1]);
		}
		room.setBattleRoomSessionKey(null);
		
		//Store the room into Redis database.
		/* Redis Transaction */
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		pipeline.multi();
		//Store the roomSessionKey to "localRoomSetName" set
		pipeline.sadd(localRoomSetName, room.getRoomSessionKey().toString());
		//Store the user's roomSessionKey to <userSessionKey:{hash}> entry
		//TODO do I need to move the following two calls to SessionManager?
		updateUserSessionMap(user.getSessionKey(), room.getRoomSessionKey(), false, pipeline);
		//Store the roomSessionKey to the "rooms_unfull_set_<maxUserCount>" or "rooms_full_set"
		pipeline.zadd(room.getCurrentSetName(), room.getCreatedMillis(), room.getRoomSessionKey().toString());
		/* Redis Transaction */
		storeRoom(room, pipeline);
		String key = getRoomTypeCurrentUserKeyString(roomType);
		pipeline.incr(key);
		pipeline.exec();
		pipeline.sync();
		//Check the room's current status

		roomMaps.put(room.getRoomSessionKey(), room);
		
		logger.debug("User {} create a room {}", user.getRoleName(), room.getRoomSessionKey());
		
		sendBseEnterRoom(room, user, user.getSessionKey(), basePos);
		return room;
	}
	
	/**
	 * Reset the room's status for the next battle.
	 * This method is called after the battle is over, both rooms
	 * will be reset to the following status: 
	 * 1> Room's creationMillis is set to current time.
	 * 2> Room's readyCount is set to 0.
	 * 3> Room's status is set to UNFULL
	 * 4> Room's battleSessionKey is set to null.
	 * 5> Room's all users joining time are time to current time. 
	 * 6> Check room's user count 
	 *    if ( userCount < maxCount ) {
	 *      put the room in unfull list
	 *    } else {
	 *    	put the room in full list.
	 *    }
	 * 
	 * Remove all AI users in this room.
	 * 
	 * @param room
	 * @return
	 */
	@Override
	public boolean resetRoom(Room room) {
		if ( room != null ) {
			room.lock();
			try {
				logger.debug("Reset room {}", room.getRoomSessionKey());
				List<UserInfo> userList = new ArrayList<UserInfo>(room.getUserInfoList());
				boolean pveRoom = room.getRoomType() == RoomType.PVE_ROOM;
				boolean offlineRoom = room.getRoomType() == RoomType.OFFLINE_ROOM;
				for (int i=0; i<userList.size(); i++) {
					UserInfo userInfo = userList.get(i);
					if ( userInfo != null && userInfo != Room.BLOCKED_USER_INFO) {
						if ( AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
							logger.debug("Remove {} user from room {} because it is AI.", 
									userInfo.getUserSessionKey(), room.getRoomSessionKey());
							kickUser(room, userInfo.getUserSessionKey(), true);
							//sendBseUserLeaveRoomToOthers(userInfo.getUserSessionKey(), room.getBattleRoom());
							//Remove the AI user after battle
							AIManager.getInstance().destroyAIUser(userInfo.getUserSessionKey());
						} else {
							if ( pveRoom ) {
								logger.debug("Remove {} user from room {} because it is AI.", 
										userInfo.getUserSessionKey(), room.getRoomSessionKey());
								kickUser(room, userInfo.getUserSessionKey(), false);
							}
							if ( offlineRoom ) {
								kickUser(room, userInfo.getUserSessionKey(), false);
							}
							/**
							 * Keep the users in room in DESK mode
							 */
							/*
							User user = GameContext.getInstance().findLocalUserBySessionKey(userInfo.getUserSessionKey());
							if ( user != null && user.isProxy() ) {
								kickUser(room, userInfo.getUserSessionKey());
								GameContext.getInstance().deregisterUserBySessionKey(userInfo.getUserSessionKey());
							} else {
								userInfo.setUserJoinTime(System.currentTimeMillis());
							}
							*/
						}
					}
				}
				room.getReadyUserSet().clear();
				room.setCreatedMillis(System.currentTimeMillis());
				room.setReadyUserCount(0);
				if ( room.getRoomType() == RoomType.MULTI_ROOM ) {
					room.setRoomStatus(RoomStatus.UNFULL);
				}
				if ( room.getRoomType() == RoomType.FRIEND_ROOM ||
						room.getRoomType() == RoomType.GUILD_ROOM ||
						room.getRoomType() == RoomType.DESK_ROOM ) {
					room.setMaxUserCount(room.MAX_USER);
				}
				room.setBattleRoomSessionKey(null);
				/**
				 * Keep the DESK ROOM 's battleRoom for next battle
				 */
				/*
				if ( room.getRoomType() != RoomType.DESK_ROOM && 
						room.getRoomType() != RoomType.FRIEND_ROOM ) {
					room.setBattleRoom(null);
				}
				*/
				if ( room.getCurrentUserCount() == room.getMaxUserCount() ) {
					room.setCurrentSetName(ZSET_FULL_NAME);
					room.setRoomStatus(RoomStatus.FULL);
				} else {
					room.setCurrentSetName(ZSET_UNFULL_NAME[room.getMaxUserCount()-1]);
					room.setRoomStatus(RoomStatus.UNFULL);
				}
				checkRoom(room);
//				logger.debug("Room {} is reset to normal state", room.getRoomSessionKey());
			} finally {
				storeRoom(room);
				room.unlock();
			}
		} else {
			logger.debug("Given room is null. Cannot reset it.");
		}
		return false;
	}

	/**
	 * Delete a room from system. If there are users (except AI users) in room,
	 * it cannot be deleted and return false. 
	 * @param roomSessionKey
	 * @return
	 */
	@Override
	public boolean deleteRoomIfEmpty(Room room) {
		try {
			room.lock();

			List<UserInfo> userList = room.getUserInfoList();
			boolean emptyRoom = true;
			for ( int i=0; i<userList.size(); i++ ) {
				UserInfo userInfo = userList.get(i);
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) {
					continue;
				} else {
					UserId userId = GameContext.getInstance().findUserIdBySessionKey(userInfo.getUserSessionKey());
					if ( userId != null ) {
						emptyRoom = false;
					}
					break;
				}
			}
			if ( emptyRoom ) {
				logger.debug("Delete room {} because it is empty.", room.getRoomSessionKey());
				
				Pipeline pipeline = JedisFactory.getJedis().pipelined();
				//pipeline.multi();
				pipeline.del(room.getRoomSessionKey().toString());
				pipeline.zrem(room.getCurrentSetName(), room.getRoomSessionKey().toString());
				pipeline.srem(localRoomSetName, room.getRoomSessionKey().toString());
				//pipeline.exec();
				pipeline.sync();
				
				roomMaps.remove(room.getRoomSessionKey());
				
				room.setRoomStatus(RoomStatus.DELETED);
				return true;
			} else {
				return false;
			}
		} finally {
			room.unlock();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean joinRoom(SessionKey roomSessionKey, SessionKey sessionKey) {
		return joinRoom(roomSessionKey, sessionKey, -1);
	}

	/**
	 * The given user tries to join the given room. If the room is full just before
	 * this user joining (it is multi-thread), this method returns false, otherwise 
	 * returns true. 
	 * 
	 * @param roomSessionKey
	 * @param userSessionKey
	 * @return
	 */
	@Override
	public boolean joinRoom(SessionKey roomSessionKey, SessionKey sessionKey, int roomPosition) {
		int roomIdx = roomPosition;
		int baseIdx = 0;
		if ( roomPosition >= Room.MAX_USER ) {
			roomIdx = roomPosition - Room.MAX_USER;
			baseIdx = Room.MAX_USER;
		}
		Room room = this.acquireRoom(roomSessionKey, false);
		if ( room != null ) {
			room.lock();
			try {
				if ( (room.getRoomType() != RoomType.FRIEND_ROOM && room.getRoomType() != RoomType.GUILD_ROOM) 
						&& room.getCurrentUserCount() >= room.getMaxUserCount() ) {
					logger.warn("It is a program logic error. The room {} is already full.", roomSessionKey);
					return false;
				}
				User user = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
				if ( user != null ) {
					/**
					 * Check if the user already exists in this room
					 */
					boolean foundUser = false;
					for ( UserInfo userInfo : room.getUserInfoList() ) {
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
						if ( sessionKey.equals(userInfo.getUserSessionKey()) ) {
							foundUser = true;
							break;
						}
					}
					if ( !foundUser ) {
						UserInfo myUserInfo = new UserInfo();
						myUserInfo.setUserJoinTime(System.currentTimeMillis());
						myUserInfo.setUserSessionKey(sessionKey);
						if ( roomIdx < 0 ) {
							room.addUser(myUserInfo);
						} else {
							room.setUser(myUserInfo, roomIdx);
						}
						if ( user.getLevel()>room.getMaxLevel() ) {
							room.setMaxLevel(user.getLevel());
						}
						String key = getRoomTypeCurrentUserKeyString(room.getRoomType());
						Jedis jedis = JedisFactory.getJedis();
						jedis.incr(key);
						
						sendBseEnterRoom(room, user, sessionKey, baseIdx);
						if ( room.getBattleRoom() != null ) {
							sendBseEnterRoom(room.getBattleRoom());
						}
						
						logger.debug("User {} join the room {}", user.getRoleName(), room.getRoomSessionKey());
					} else {
						logger.debug("User {} already join the room {}", user.getRoleName(), room.getRoomSessionKey());
					}
					return true;
				}
			} finally {
				storeRoom(room);
				room.unlock();
			}
		} else {
			/**
			 * In current stage, I donot support distributed room.
			 * That is, all room's users should be in one server.
			 */
			//Room is in remote server
			//logger.debug("The required room {} is in gameserver {}", roomSessionKey, gameServerId);
			logger.debug("#joinRoom: cannot find room by roomSessionKey: {}", roomSessionKey);
			//TODO support this method in multi-users room.
		}
		return false;
	}

	/**
	 * Kick an user from given room. The user will be assigned to another room randomly.
	 * If the room is null or its status is DELETED, return false.
	 * @param userSessionKey
	 * @param roomSessioinKey
	 * 
	 * @return
	 */
	@Override
	public boolean kickUser(Room room, SessionKey userSessionKey, boolean sendBse) {
		try {
			room.lock();
			
			if ( room.getRoomStatus() == RoomStatus.DELETED ) {
				logger.debug("Kick user {} from deleted room {}", userSessionKey, room.getRoomSessionKey());
				return false;
			}
			logger.debug("Kick user {} from room {}", userSessionKey, room.getRoomSessionKey());
			Pipeline pipeline = JedisFactory.getJedis().pipelined();
			pipeline.multi();
			//Clear user session map's roomSessionKey and isAI
			pipeline.hdel(userSessionKey.toString(), H_ROOM_SESSION_KEY);
			List<UserInfo> userList = room.getUserInfoList();
			int index = -1;
			UserInfo deleteUserInfo = null;
			for ( int i=0; i<userList.size(); i++ ) {
				UserInfo userInfo = userList.get(i);
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				if ( userSessionKey.equals(userInfo.getUserSessionKey()) ) {
					index = i;
					deleteUserInfo = userInfo;
					break;
				}
			}
			if ( index >= 0 ) {
				//Send response back				
				List<UserInfo> list = room.getUserInfoList();
				if ( sendBse ) {
					BseUserLeaveRoom.Builder builder = BseUserLeaveRoom.newBuilder();
					builder.setMasterId(room.getOwnerSessionKey().toString());
					builder.setSessionId(userSessionKey.toString());
					builder.setRoomId(room.getRoomSessionKey().toString());
					BseUserLeaveRoom kickUser = builder.build();
					
					for ( UserInfo user : list ) {
						if ( user == null || user == Room.BLOCKED_USER_INFO ) continue;
						//if ( user.getUserSessionKey().equals(userSessionKey) ) continue;
						GameContext.getInstance().writeResponse(user.getUserSessionKey(), kickUser);
					}
					BattleRoom battleRoom = room.getBattleRoom();
					if ( battleRoom != null ) {
						Room otherRoom = battleRoom.getRoomRight();
						if ( otherRoom == room ) {
							otherRoom = battleRoom.getRoomLeft();
						}
						list = otherRoom.getUserInfoList();
						for ( UserInfo user : list ) {
							if ( user == null || user == Room.BLOCKED_USER_INFO ) continue;
							if ( user.getUserSessionKey().equals(userSessionKey) ) continue;
							GameContext.getInstance().writeResponse(user.getUserSessionKey(), kickUser);
						}
					}
				}
				//Delete the given user
				room.removeUser(index);
				pipeline.hdel(room.getRoomSessionKey().toString(), H_USER_SESSION_KEYS[index]);
				pipeline.hdel(room.getRoomSessionKey().toString(), H_USER_JOIN_KEYS[index]);
				String key = getRoomTypeCurrentUserKeyString(room.getRoomType());
				pipeline.decr(key);
				
				if ( deleteUserInfo.getUserJoinTime() == Long.MAX_VALUE ) {
					//User is in ready status
					room.setReadyUserCount(room.getReadyUserCount()-1);
				}
				room.removeReadyUser(userSessionKey);
				
				//Check if the user is a proxy user. If it is, deregister it
				User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
				if ( user != null && user.isProxy() ) {
					GameContext.getInstance().deregisterUserBySessionKey(userSessionKey);
				}
				
				//Chown room owner
				if ( room.getOwnerSessionKey().equals(userSessionKey) ) {
					for ( UserInfo userInfo : list ) {
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO || 
								userInfo.getUserSessionKey().equals(userSessionKey)) continue;
						User roomUser = GameContext.getInstance().findLocalUserBySessionKey(userInfo.getUserSessionKey());
						if ( roomUser != null && !roomUser.isAI() ) {
							room.setOwnerSessionKey(userInfo.getUserSessionKey());
							room.setOwnerLevel(roomUser.getLevel());
							room.setOwnerPower(roomUser.getPower());
							break;
						}
					}
				}
			}
			pipeline.exec();
			pipeline.sync();
			
		} finally {
			storeRoom(room);
			room.unlock();
		}
		return true;
	}

	/**
	 * Match a ready or timeout room with another room. 
	 * These two room will become a big battle room.
	 * The battle room is managed by current JVM's BattleRoomManager.
	 * 
	 * @param roomLeft
	 * @return
	 */
	@Override
	public boolean matchRoom(Room roomLeft) {
		
		switch ( roomLeft.getRoomType() ) {
			case SINGLE_ROOM:
			case GUILD_ROOM:
			case MULTI_ROOM:
				{
					String zsetName = roomLeft.getCurrentSetName();
					
					Jedis jedis = JedisFactory.getJedis();
					long count = jedis.zcard(zsetName);
					logger.debug("Match room will check all {} rooms in {}", count, zsetName);
					
					for ( int i=0; i<count; i++ ) {
						Tuple tuple = JedisUtil.popKeyFromZset(zsetName);
						
						SessionKey roomRightSessionKey = SessionKey.createSessionKeyFromHexString(tuple.getElement());
						
						if ( roomLeft.getRoomSessionKey().equals(roomRightSessionKey) ) {
							logger.debug("Ignore the same room in zset");
							continue;
						}
						logger.debug("Find a matching room in redis {} for room {} ", roomRightSessionKey, roomLeft.getRoomSessionKey());
						Room roomRight = acquireRoom(roomRightSessionKey, true);
						boolean matched = false;
						try {
							if ( roomRight == null ) {
								return false;
							} else if ( !roomRight.tryLock() ) {
								logger.debug("The roomRight is not locked by current thread.");
								matched = false; 
							} else {
								//Call script
								matched = ScriptManager.getInstance().runScriptForBoolean(
										ScriptHook.PICK_BATTLE_MATCH_ROOM, roomLeft, roomRight);							
							}
							if ( matched ) {
								createBattleRoom(roomLeft, roomRight);
								return true;
							} else {
								logger.debug("RoomLeft's power {} and RoomRight's power {} is not matched.", roomLeft.getAveragePower(),
										roomRight.getAveragePower());
								jedis.zadd(zsetName, System.currentTimeMillis(), tuple.getElement());
							}
						} finally {
							if ( roomRight != null ) {
								roomRight.unlock();
							}
						}
					}
					
					//no room in queue, put this in and return null;
					logger.debug("No matching room in {}, put this in it.", zsetName);
					jedis.zadd(zsetName, System.currentTimeMillis(), roomLeft.getRoomSessionKey().toString());
					
					return false;
				}
		}
		return false;
	}
	
	/**
	 * Create a battle room with roomLeft and roomRight
	 */
	@Override
	public void createBattleRoom(Room roomLeft, Room roomRight) {
		final BattleRoom battleRoom = createBattleRoomWithoutBegin(roomLeft, roomRight);
		
		GameContext.getInstance().scheduleTask(new Runnable(){
			public void run() {
				BattleManager.getInstance().battleBegin(battleRoom);
			}
		}, 3, TimeUnit.SECONDS);
	}
	
	/**
	 * Create a battle room with roomLeft and roomRight
	 */
	@Override
	public BattleRoom createBattleRoomWithoutBegin(Room roomLeft, Room roomRight) {
		roomLeft.setRoomStatus(RoomStatus.COMBAT);
		roomRight.setRoomStatus(RoomStatus.COMBAT);
		this.storeRoom(roomLeft);
		this.storeRoom(roomRight);
		
		BattleRoom battleRoom = new BattleRoom();
		battleRoom.setSessionKey(SessionKey.createSessionKeyFromRandomString(BattleRoom.PREFIX));
		battleRoom.setRoomLeft(roomLeft);
		battleRoom.setRoomRigth(roomRight);

		if ( roomLeft.getRoomType() != RoomType.DESK_ROOM && 
				roomLeft.getRoomType() != RoomType.PVE_ROOM) {
			sendBseEnterRoom(battleRoom);
		}
		
		return battleRoom;
	}
	
	/**
	 * Update the user's session map in Redis.
	 * 
	 * @param userSessionKey
	 * @param isAI
	 * @return
	 */
	@Override
	public boolean updateUserSessionMap(SessionKey userSessionKey, 
			SessionKey roomSessionKey, boolean isAI, Pipeline pipeline) {
		pipeline.hset(userSessionKey.toString(), H_ROOM_SESSION_KEY, roomSessionKey.toString());
//		pipeline.hset(userSessionKey.toString(), H_ISAI, V_FALSE);
		return true;
	}

	/**
	 * Find the Room's SessionKey by the user's SessionKey
	 * @param userSessionKey
	 * @return
	 */
	@Override
	public SessionKey findRoomSessionKeyByUserSession(SessionKey userSessionKey) {
		if ( userSessionKey == null ) return null;
		Jedis jedis = JedisFactory.getJedis();
		String bytes = jedis.hget(userSessionKey.toString(), H_ROOM_SESSION_KEY);
		if ( bytes != null ) {
			SessionKey roomSessionKey = SessionKey.createSessionKeyFromHexString(bytes);
			return roomSessionKey;
		}
		return null;
	}
	
	/**
	 * Store the room status in database.
	 * @param room
	 */
	@Override
	public boolean storeRoom(Room room) {
		if ( room == null ) return false;
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		try {
			boolean result = this.storeRoom(room, pipeline);
			return result;
		} catch (Throwable e) {
			logger.warn("Failed to store room. {}", e.toString());
			logger.debug("Stacktrace", e);
		} finally {
			pipeline.sync();
		}
		return false;
	}

	/**
	 * Store the room status in database.
	 * @param room
	 */
	public boolean storeRoom(Room room, Pipeline pipeline) {
		try {
			room.lock();
			//First check the room status
			RoomStatus oldStatus = room.getRoomStatus();
			checkRoom(room);
			if ( room.getRoomStatus() == RoomStatus.DELETED ) {
				return false;
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put(H_MACHINE_ID, room.getGameServerId());
			map.put(H_ROOM_TYPE, room.getRoomType().name());
			map.put(H_ROOM_STATUS, room.getRoomStatus().name());
			SessionKey ownerSessionKey = room.getOwnerSessionKey();
			if ( ownerSessionKey != null ) {
				map.put(H_SESSION_KEY, ownerSessionKey.toString());
			} else {
//				throw new IllegalArgumentException("Room owner's sessionKey is null.");
				logger.warn("Room owner's sessionKey is null.");
				return false;
			}
			List<UserInfo> users = room.getUserInfoList();
			for ( int i=0; i<users.size(); i++ ) {
				UserInfo userInfo = users.get(i);
				if ( userInfo == null ) {
					map.put(H_USER_SESSION_KEYS[i], Constant.EMPTY);
				} else if ( userInfo == Room.BLOCKED_USER_INFO ) {
					map.put(H_USER_SESSION_KEYS[i], "BLOCKED");
					map.put(H_USER_JOIN_KEYS[i], Constant.EMPTY);
				} else {
					map.put(H_USER_SESSION_KEYS[i], userInfo.getUserSessionKey().toString());
					map.put(H_USER_JOIN_KEYS[i], String.valueOf(userInfo.getUserJoinTime()));					
				}
			}
			Set<SessionKey> readyUserSet = room.getReadyUserSet();
			StringBuilder buf = new StringBuilder();
			for ( SessionKey sessionKey : readyUserSet ) {
				buf.append(sessionKey.toString()).append(Constant.COMMA);
			}
			map.put(H_READY_USERS, buf.toString());
			map.put(H_MAX_USER_COUNT, String.valueOf(room.getMaxUserCount()));
			map.put(H_CURRENT_USER_COUNT, String.valueOf(room.getCurrentUserCount()));
			map.put(H_REDAY_USER_COUNT, String.valueOf(room.getReadyUserCount()));
			map.put(H_MAX_LEVEL, String.valueOf(room.getMaxLevel()));
			if ( StringUtil.checkNotEmpty(room.getMapId()) ) {
				map.put(H_MAP_ID, room.getMapId());
			}
			if ( StringUtil.checkNotEmpty(room.getRoomKey()) ) {
				map.put(H_ROOMKEY, room.getRoomKey());
			}
			map.put(H_AUTOMODE, String.valueOf(room.isAutoMode()));
			//Delete the room from old set
			if ( room.isRoomStatusChanged() ) {
				String roomKey = room.getRoomSessionKey().toString();
				for ( String zsetName : RedisRoomManager.ZSET_UNFULL_NAME ) {
					pipeline.zrem(zsetName, roomKey);
				}
				for ( String zsetName : RedisRoomManager.ZSET_READY_NAME ) {
					pipeline.zrem(zsetName, roomKey);					
				}
				pipeline.zrem(RedisRoomManager.ZSET_FULL_NAME, roomKey);
				
				String newSetName = null;
				switch ( room.getRoomStatus() ) {
					case UNFULL:
						newSetName = ZSET_UNFULL_NAME[room.getMaxUserCount()-1];
						break;
					case FULL:
						newSetName = ZSET_FULL_NAME;
						break;
					case READY:
						newSetName = ZSET_READY_NAME[room.getMaxUserCount()-1];
						break;
				}
				if ( newSetName != null ) {
					if ( room.getRoomType() == RoomType.SINGLE_ROOM || 
							room.getRoomType() == RoomType.MULTI_ROOM ) {
						pipeline.zadd(newSetName, room.getCreatedMillis(), room.getRoomSessionKey().toString());
					} else if ( 
							(room.getRoomType() == RoomType.FRIEND_ROOM || room.getRoomType() == RoomType.GUILD_ROOM) && 
							room.getRoomStatus() == RoomStatus.READY ) {
						pipeline.zadd(newSetName, room.getCreatedMillis(), room.getRoomSessionKey().toString());
					}
					map.put(H_CURRENT_SET_NAME, newSetName);
					room.setCurrentSetName(newSetName);
					logger.debug("The room's current set changes to: {}", newSetName);
				}					
				room.clearRoomStatusChanged();
			}
			map.put(H_CREATED_DATE, String.valueOf(room.getCreatedMillis()));
			map.put(H_READY_DATE, String.valueOf(room.getReadyStartMillis()));
			map.put(H_OWNER_POWER, String.valueOf(room.getOwnerPower()));
			map.put(H_OWNER_LEVEL, String.valueOf(room.getOwnerLevel()));
			map.put(H_AVG_POWER, String.valueOf(room.getAveragePower()));
			
			pipeline.hmset(room.getRoomSessionKey().toString(), map);
			pipeline.expire(room.getRoomSessionKey().toString(), 
					Constant.QUARTER_DAY_SECONDS);
			return true;
		} finally {
			room.unlock();
		}
	}

	/**
	 * Get and restore the room's data from database if the room is managed by 
	 * current JVM.
	 * 
	 * If the room is managed by another JVM and 'getRemote' is false, an empty 
	 * room will be returned with only its rpcserverid field is set. 
	 * 
	 * If 'getRemote' is ture, even a remote room object will be returned.
	 * 
	 * The return room may be null if it is cannot found.
	 * 
	 * @param roomSessionKey
	 * @param getRemote
	 * @return
	 */
	@Override
	public Room acquireRoom(SessionKey roomSessionKey, boolean getRemote) {
		if ( roomSessionKey == null ) {
			return null;
		}
		Room room = roomMaps.get(roomSessionKey);
		if ( room == null ) {
			if ( getRemote ) {
				Jedis jedis = JedisFactory.getJedis();
				Map<String, String> map = jedis.hgetAll(roomSessionKey.toString());
				if ( map == null || map.isEmpty() ) {
					return null;
				}
				String rpcServerId = map.get(H_MACHINE_ID);
				room = new Room();
				room.setRoomSessionKey(roomSessionKey);
				room.setGameServerId(rpcServerId);
				
				room.setRoomType(RoomType.valueOf(map.get(H_ROOM_TYPE)));
				room.setRoomStatus(RoomStatus.valueOf(map.get(H_ROOM_STATUS)));
				room.setOwnerSessionKey(SessionKey.createSessionKeyFromHexString(map.get(H_SESSION_KEY)));
				room.setMaxUserCount(toInt(map.get(H_MAX_USER_COUNT), 2));
				room.setReadyUserCount(toInt(map.get(H_REDAY_USER_COUNT), 0));
				String readyUserList = map.get(H_READY_USERS);
				if ( checkNotEmpty(readyUserList) ) {
					int bIndex = 0;
					int eIndex = readyUserList.indexOf(',');
					int len = readyUserList.length();
					while ( bIndex >= 0 && bIndex < eIndex && eIndex > 0 && eIndex < len) {
						String sess = readyUserList.substring(bIndex, eIndex);
						bIndex = eIndex+1;
						eIndex = readyUserList.indexOf(',');
						room.addReadyUser(SessionKey.createSessionKeyFromHexString(sess));
					}
				}
				String mapId = map.get(H_MAP_ID);
				if ( StringUtil.checkNotEmpty(mapId) ) {
					room.setMapId(mapId);
				}
				String roomKey = map.get(H_ROOMKEY);
				if ( StringUtil.checkNotEmpty(roomKey) ) {
					room.setRoomKey(roomKey);
				}
				String maxLevel = map.get(H_MAX_LEVEL);
				room.setMaxLevel(StringUtil.toInt(maxLevel, 0));
				room.setAutoMode(Boolean.parseBoolean(map.get(H_AUTOMODE)));
				
				for ( int i=0; i<Room.MAX_USER; i++ ) {
					String sessionStr = map.get(H_USER_SESSION_KEYS[i]);
					if ( sessionStr == null ) {
						continue;
					} else if ( Constant.EMPTY.equals(sessionStr) ) {
						room.setUser(null, i);
					} else if ( "BLOCKED".equals(sessionStr) ) {
						room.setUser(Room.BLOCKED_USER_INFO, i);
					} else {
						SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionStr);
						long joinMilli = 0l;
						try {
							joinMilli = Long.parseLong(map.get(H_USER_JOIN_KEYS[i]));
						} catch (NumberFormatException e) {
						}
						UserInfo userInfo = new UserInfo();
						userInfo.setUserSessionKey(sessionKey);
						userInfo.setUserJoinTime(joinMilli);
						room.setUser(userInfo, i);						
					}
				}
//					room.setCurrentUserCount(room.getUserList().size());
				room.setCurrentSetName(map.get(H_CURRENT_SET_NAME));
				room.setCreatedMillis(Long.parseLong(map.get(H_CREATED_DATE)));
				room.setOwnerPower(toInt(map.get(H_OWNER_POWER), 0));
				room.setOwnerLevel(toInt(map.get(H_OWNER_LEVEL), 0));
				room.setAveragePower(toInt(map.get(H_AVG_POWER), 0));
				if ( !this.gameServerId.equals(rpcServerId) ) {
					room.setRemote(true);
				}
				
			}
		}
		return room;
	}
	
	/**
	 * Get all local rooms created by this JVM
	 * @return
	 */
	@Override
	public Collection<Room> getLocalRoomCollection() {
		return this.roomMaps.values();
	}
	
	/**
	 * 
	 * @param roomSessionKey
	 */
	@Override
	public void removeLocalRoom(SessionKey roomSessionKey) {
		if ( roomSessionKey != null ) {
			this.roomMaps.remove(roomSessionKey);
		}
	}
	
	/**
	 * When an user select the 'multi-users' mode room,
	 * the game will first try to select unfull rooms
	 * in Redis. The 4-users room has higher priority 
	 * than 3-users room and respectivly. If there is 
	 * no such rooms, it returns null.
	 *  
	 * @param user
	 * @return
	 */
	@Override
	public Room popUnfullMultiUserRoom(User user) {
		Jedis jedis = JedisFactory.getJedis();
		
		for ( int i=ZSET_UNFULL_NAME.length-1; i>=0; i-- ) {
			String zsetName = ZSET_UNFULL_NAME[i];
			
			long count = jedis.zcard(zsetName);
			logger.debug("Match room will check all {} rooms in {}", count, zsetName);
			
			for ( int j=0; j<count; j++ ) {
				Tuple tuple = JedisUtil.popKeyFromZset(zsetName);
				
				SessionKey unfullRoomSessionKey = SessionKey.createSessionKeyFromHexString(tuple.getElement());
				/** 
				 * Note the synchronized operation problem. If a new user is coming in as well as an old
				 * user is changing his status. The second operation may be flushed by the first, if
				 * the two users are in different servers.
				 */
				/**
				 * Forbidden the remote room match because it will cause a lot of
				 * problems.
				 */
				//Room unfullRoom = acquireRoom(unfullRoomSessionKey, true);
				Room unfullRoom = acquireRoom(unfullRoomSessionKey, false);
				if ( unfullRoom != null ) {
					if ( !unfullRoom.isRemote() ) {
						logger.debug("Find an unfull room {} for user {}.", unfullRoom.getRoomSessionKey(), user.getRoleName());
						
						int diff = Math.abs(unfullRoom.getOwnerLevel() - user.getLevel());
						int maxDiff = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_MATCH_LEVEL, 5);
						if ( diff < maxDiff ) {
							boolean success = joinRoom(unfullRoomSessionKey, user.getSessionKey());
							logger.debug("Room {}'s status {}.", unfullRoom.getRoomSessionKey(), unfullRoom.getRoomStatus());
							if ( success ) {
								return unfullRoom;
							} else {
								return null;
							}
						} else {
							logger.debug("User {}' level is too much higher or lower than room owner's level {}", user.getRoleName(), unfullRoom.getOwnerLevel());
							jedis.zadd(zsetName, System.currentTimeMillis(), tuple.getElement());
						}
					} else {
						String gameServerId = unfullRoom.getGameServerId();
						logger.debug("User {} enter a remote room in gameserver: {}", user.getRoleName(), gameServerId);
						BceEnterRoom.Builder builder = BceEnterRoom.newBuilder();
						builder.setRoomId(Constant.EMPTY);
						builder.setMapId(StringUtil.toInt(unfullRoom.getMapId(), 0));
						builder.setRoomName(Constant.EMPTY);
						builder.setBattleMode(0);
						builder.setChooseMode(0);
						builder.setRoomType(unfullRoom.getRoomType().ordinal());
						GameContext.getInstance().proxyToRemoteGameServer(user.getSessionKey(), gameServerId, builder.build());
						return unfullRoom;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Send the user ready start data to all users in the room
	 * @param room
	 * @param userSessionKey
	 * @param userInfos
	 * @param isReady
	 */
	@Override
	public void sendUserReadyStart(Room room, SessionKey userSessionKey, List<UserInfo> userInfos, boolean isReady) {
		BseUserReadyStart.Builder builder = BseUserReadyStart.newBuilder();
		builder.setReady(isReady);
		builder.setRoomId(room.getRoomSessionKey().toString());
		builder.setSessionId(userSessionKey.toString());
		BseUserReadyStart readyStart = builder.build();
		
		BattleRoom battleRoom = room.getBattleRoom();
		if ( battleRoom != null ) {
			List<UserInfo> leftList = battleRoom.getRoomLeft().getUserInfoList();
			List<UserInfo> rightList = battleRoom.getRoomRight().getUserInfoList();
			
			for ( int i=0; i<leftList.size(); i++ ) {
				UserInfo userInfo = leftList.get(i);
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				if ( AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) continue;
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), readyStart);
			}
			for ( int i=0; i<rightList.size(); i++ ) {
				UserInfo userInfo = rightList.get(i);
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				if ( AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) continue;
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), readyStart);
			}	
		} else {
			for ( UserInfo userInfo : userInfos ) {
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				if ( userInfo.getUserSessionKey().equals(userSessionKey) ) {
					userInfo.setUserJoinTime(Long.MAX_VALUE);
				}
				if ( AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) continue;
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), builder.build());
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------
	
	/**
	 * Send BseEnterRoom to new coming user and all old users in room.
	 * @param room
	 * @param sessionKey
	 */
	private void sendBseEnterRoom(Room room, User user, SessionKey sessionKey, int basePos) {
		//First find the new coming user's roomIndex.
		List<UserInfo> list = room.getUserInfoList();
		int newUserIndex = 0;
		for ( int i=0; i<list.size(); i++ ) {
			UserInfo userInfo = list.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			if ( sessionKey.equals(userInfo.getUserSessionKey()) ) {
				newUserIndex = i;
			}
		}
		newUserIndex += basePos;
		boolean allowAutoMode = checkRoomAllowAutoMode(room);
		BattleCamp newUserCamp = BattleCamp.LEFT;
		BattleCamp otherCamp = BattleCamp.RIGHT;
		if ( basePos >= Room.MAX_USER ) {
			newUserCamp = BattleCamp.RIGHT;
			otherCamp = BattleCamp.LEFT;
		}
		/**
		 * 如果还存在另一个房间，则需要向该房间的更新新玩家位置
		 */
//		BattleRoom battleRoom = room.getBattleRoom();
//		Room otherRoom = null;
//		List<UserInfo> otherList = null;
//		if ( battleRoom != null ) {
//			otherRoom = battleRoom.getRoomRight();
//			if ( room == otherRoom ) {
//				room = battleRoom.getRoomLeft();
//			}
//			otherList = otherRoom.getUserInfoList();
//		}
		//Second Send BseEnterRoom to new coming user
		if ( !user.isProxy() && !AIManager.getInstance().isAIUser(user.getSessionKey()) ) {
	    BseEnterRoom.Builder builder = BseEnterRoom.newBuilder();
	    builder.setRoomId(room.getRoomSessionKey().toString());
			builder.setMasterId(room.getOwnerSessionKey().toString());
	    builder.setBattleMode(0);
	    builder.setChooseMode(0);
	    builder.setMapId(StringUtil.toInt(room.getMapId(), 0));
	    builder.setRoomIdx(newUserIndex);
	    builder.setAutomode(allowAutoMode);
	    /**
	     * 为了防止跨服取不到bossId，使用roomKey
	     */
	    /*
	    if ( room.getRoomType() == RoomType.PVE_ROOM ) {
				Boss boss = (Boss)user.getUserData(BossManager.USER_BOSS_ID);
	    	if ( boss != null ) {
	    		builder.setRoomkey(boss.getBossId());
	    	}
	    }
	    */
	    if ( room.getRoomKey() != null ) {
	    	builder.setRoomkey(room.getRoomKey());
	    } else {
		    if ( room.getRoomType() == RoomType.PVE_ROOM ) {
					Boss boss = (Boss)user.getUserData(BossManager.USER_BOSS_ID);
		    	if ( boss != null ) {
		    		room.setRoomKey(boss.getBossId());
		    		builder.setRoomkey(boss.getBossId());
		    	}
		    }
	    }
	    builder.setRoomType(room.getRoomType().ordinal());
			GameContext.getInstance().writeResponse(user.getSessionKey(),  builder.build());
			BseToolList bseToolList = user.toBseToolList();
			GameContext.getInstance().writeResponse(user.getSessionKey(), bseToolList);
			logger.debug("Send BseEnterRoom to roomIndex:{} to new joining user {}", newUserIndex, user.getRoleName());
	
			//Third Update the new user about old users in the room
			for ( int i=0; i<list.size(); i++ ) {
				if ( i == newUserIndex ) continue;
				UserInfo userInfo = list.get(i);
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				User other = GameContext.getInstance().findGlobalUserBySessionKey(userInfo.getUserSessionKey());
				if ( other != null ) {
					BseUserEnterRoom.Builder userEnterRoom = BseUserEnterRoom.newBuilder();
					userEnterRoom.setRoomId(room.getRoomSessionKey().toString());
					//userEnterRoom.setRole( other.toRoleInfo(null, BattleCamp.LEFT.id(), i, null) );
					userEnterRoom.setRole( other.toRoleInfo(null, newUserCamp.id(), i+basePos, null) );
					GameContext.getInstance().writeResponse(sessionKey, userEnterRoom.build());
					logger.debug("Send BseUserEnterRoom to new user about oldUsers: roomIndex:{}, room user {}", i+basePos, other.getRoleName());
				}
			}
			//If the other room exist, tell that room's users about the new user.
//			if ( otherList != null ) {
//				for ( int i=0; i<otherList.size(); i++ ) {
//					UserInfo userInfo = otherList.get(i);
//					if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
//					User other = GameContext.getInstance().findGlobalUserBySessionKey(userInfo.getUserSessionKey());
//					if ( other != null ) {
//						BseUserEnterRoom.Builder userEnterRoom = BseUserEnterRoom.newBuilder();
//						userEnterRoom.setRoomId(otherRoom.getRoomSessionKey().toString());
//						userEnterRoom.setRole( other.toRoleInfo(null, otherCamp.id(), i+basePos, null) );
//						GameContext.getInstance().writeResponse(sessionKey, userEnterRoom.build());
//						logger.debug("Send BseUserEnterRoom to new user about oldUsers: roomIndex:{}, room user {}", i+basePos, other.getRoleName());
//					}
//				}
//			}
		}
		
		//Fourth, update all old users about the new user.
		RoleInfo roleInfo = user.toRoleInfo(null, newUserCamp.id(), newUserIndex, null);
		BseUserEnterRoom.Builder userEnterRoom = BseUserEnterRoom.newBuilder();
		userEnterRoom.setRoomId(room.getRoomSessionKey().toString());
		userEnterRoom.setRole( roleInfo );
		userEnterRoom.setAutomode(allowAutoMode);
		BseUserEnterRoom bseUserEnterRoom = userEnterRoom.build();
		for ( int i=0; i<list.size(); i++ ) {
			if ( i == newUserIndex ) continue;
			UserInfo userInfo = list.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), bseUserEnterRoom);
			logger.debug("Send BseUserEnterRoom to old users about new user: roomIndex:{} userSessionKey:{}", newUserIndex, user.getRoleName());
		}
	  //If the other room exist, tell that room old users about the new user.
//		if ( otherList != null ) {
//			for ( int i=0; i<otherList.size(); i++ ) {
//				if ( i == newUserIndex ) continue;
//				UserInfo userInfo = otherList.get(i);
//				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
//				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), bseUserEnterRoom);
//				logger.debug("Send BseUserEnterRoom to old users about new user: roomIndex:{} userSessionKey:{}", newUserIndex, user.getRoleName());
//			}
//		}
	}

	/**
	 * @param room
	 * @return
	 */
	private boolean checkRoomAllowAutoMode(Room room) {
		boolean allowAutoMode = false;
		if ( room.getRoomType() == RoomType.SINGLE_ROOM || 
				room.getRoomType() == RoomType.CHALLENGE_ROOM ) {
			allowAutoMode = true;
		}
		return allowAutoMode;
	}
		
	/**
	 * Send BseEnterRoom to new coming user and all old users in room.
	 * @param room
	 * @param sessionKey
	 */
	@Override
	public void sendBseEnterRoom(BattleRoom battleRoom) {		
		//First Update the left room's user about the right room's user position
		List<UserInfo> leftList = battleRoom.getRoomLeft().getUserInfoList();
		List<UserInfo> rightList = battleRoom.getRoomRight().getUserInfoList();
		ArrayList<BseUserEnterRoom> left = new ArrayList<BseUserEnterRoom>();
		ArrayList<BseUserEnterRoom> right = new ArrayList<BseUserEnterRoom>();
		
		for ( int i=0; i<leftList.size(); i++ ) {
			UserInfo userInfo = leftList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			User user = GameContext.getInstance().findGlobalUserBySessionKey(userInfo.getUserSessionKey());
			if ( user != null ) {
				BseUserEnterRoom.Builder userEnterRoom = BseUserEnterRoom.newBuilder();
				userEnterRoom.setRoomId(battleRoom.getSessionKey().toString());
				userEnterRoom.setRole( user.toRoleInfo(null, BattleCamp.LEFT.id(), i, null) );
				left.add(userEnterRoom.build());
			}
		}
		for ( int i=0; i<rightList.size(); i++ ) {
			UserInfo userInfo = rightList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			User user = GameContext.getInstance().findGlobalUserBySessionKey(userInfo.getUserSessionKey());
			if ( user != null ) {
				BseUserEnterRoom.Builder userEnterRoom = BseUserEnterRoom.newBuilder();
				userEnterRoom.setRoomId(battleRoom.getSessionKey().toString());
				userEnterRoom.setRole( user.toRoleInfo(null, BattleCamp.LEFT.id(), 4+i, null) );
				right.add(userEnterRoom.build());
			}
		}
		
		for ( int i=0; i<leftList.size(); i++ ) {
			UserInfo userInfo = leftList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			for ( BseUserEnterRoom enterRoom : left ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
			for ( BseUserEnterRoom enterRoom : right ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
		}
		for ( int i=0; i<rightList.size(); i++ ) {
			UserInfo userInfo = rightList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			for ( BseUserEnterRoom enterRoom : left ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
			for ( BseUserEnterRoom enterRoom : right ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
		}
	}
	
	/**
	 * Send BseEnterRoom to new coming user and all old users in room.
	 * @param room
	 * @param sessionKey
	 */
	@Override
	public void sendBseUserLeaveRoom(BattleRoom battleRoom) {		
		//First Update the left room's user about the right room's user position
		List<UserInfo> leftList = battleRoom.getRoomLeft().getUserInfoList();
		List<UserInfo> rightList = battleRoom.getRoomRight().getUserInfoList();
		ArrayList<BseUserLeaveRoom> left = new ArrayList<BseUserLeaveRoom>();
		ArrayList<BseUserLeaveRoom> right = new ArrayList<BseUserLeaveRoom>();
		
		for ( int i=0; i<leftList.size(); i++ ) {
			UserInfo userInfo = leftList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			BseUserLeaveRoom.Builder userLeaveRoom = BseUserLeaveRoom.newBuilder();
			userLeaveRoom.setRoomId(battleRoom.getSessionKey().toString());
			userLeaveRoom.setMasterId(userInfo.getUserSessionKey().toString());
			userLeaveRoom.setSessionId(userInfo.getUserSessionKey().toString());
			left.add(userLeaveRoom.build());
		}
		for ( int i=0; i<rightList.size(); i++ ) {
			UserInfo userInfo = rightList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			BseUserLeaveRoom.Builder userLeaveRoom = BseUserLeaveRoom.newBuilder();
			userLeaveRoom.setRoomId(battleRoom.getSessionKey().toString());
			userLeaveRoom.setMasterId(userInfo.getUserSessionKey().toString());
			userLeaveRoom.setSessionId(userInfo.getUserSessionKey().toString());
			right.add(userLeaveRoom.build());
		}
		
		for ( int i=0; i<leftList.size(); i++ ) {
			UserInfo userInfo = leftList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			for ( BseUserLeaveRoom enterRoom : right ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
		}
		for ( int i=0; i<rightList.size(); i++ ) {
			UserInfo userInfo = rightList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			for ( BseUserLeaveRoom enterRoom : left ) {
				GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), enterRoom);
			}
		}
	}
	
	/**
	 * Send BseEnterRoom to new coming user and all old users in room.
	 * @param room
	 * @param sessionKey
	 */
	@Override
	public void sendBseUserLeaveRoomToOthers(SessionKey leftUserSessionKey, 
			BattleRoom battleRoom) {
		if ( leftUserSessionKey == null || battleRoom == null ) return;
		
		//First Update the left room's user about the right room's user position
		List<UserInfo> leftList = battleRoom.getRoomLeft().getUserInfoList();
		List<UserInfo> rightList = battleRoom.getRoomRight().getUserInfoList();
		
		BseUserLeaveRoom.Builder userLeaveRoom = BseUserLeaveRoom.newBuilder();
		userLeaveRoom.setRoomId(battleRoom.getSessionKey().toString());
		userLeaveRoom.setMasterId(leftUserSessionKey.toString());
		userLeaveRoom.setSessionId(leftUserSessionKey.toString());
		BseUserLeaveRoom leaveRoom = userLeaveRoom.build();
		
		for ( int i=0; i<leftList.size(); i++ ) {
			UserInfo userInfo = leftList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), leaveRoom);
		}
		for ( int i=0; i<rightList.size(); i++ ) {
			UserInfo userInfo = rightList.get(i);
			if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
			GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), leaveRoom);
		}
	}
	
	/**
	 * Get the room's user number and the max user.
	 * 
	 */
	@Override
	public int[] getRoomUserNumber(RoomType roomType) {
		String key = getRoomTypeCurrentUserKeyString(roomType);
		Jedis jedis = JedisFactory.getJedis();
		String value = jedis.get(key);
		int number = StringUtil.toInt(value, 0);
		if ( number < 0 ) {
			number = 0;
			jedis.set(key, String.valueOf(number));
		}
		String maxKey = getRoomTypeMaxUserKeyString(roomType);
		String maxValue = jedis.get(maxKey);
		int max = StringUtil.toInt(maxValue, 1000);
		if ( number > max ) {
			max = number;
			jedis.set(maxKey, String.valueOf(max));
		}
		return new int[]{number, max};
	}
	
	//--------------------------------------------------- Internal Methods
	
	/**
	 * Get the room type's count key string
	 * @param roomType
	 * @return
	 */
	public final String getRoomTypeCurrentUserKeyString(RoomType roomType) {
		String key = StringUtil.concat(ROOM_SESSION_KEY_PREFIX, 
				roomType.toString(), ROOM_COUNT_SUFFIX);
		return key;
	}
	
	/**
	 * Get the room type's count key string
	 * @param roomType
	 * @return
	 */
	public final String getRoomTypeMaxUserKeyString(RoomType roomType) {
		String key = StringUtil.concat(ROOM_SESSION_KEY_PREFIX, 
				roomType.toString(), ROOM_MAX_SUFFIX);
		return key;
	}
		
	/**
	 * Get the Rpc client interface for calling remote JVM's RoomManager method.
	 * 
	 * @param machineId
	 * @return
	 */
	private RpcRoomManager.RoomManager getRpcRoomManager(String machineId) {
		RpcRoomManager.RoomManager rpcRoomManager = machineRoomManagerMap.get(machineId);
		if ( rpcRoomManager == null ) {
			String[] results = splitMachineId(machineId);
			if ( results != null ) {
				MinaRpcPoolChannel rpcChannel = new MinaRpcPoolChannel(
						results[0], toInt(results[1], 0), Constant.CPU_CORES);
				RpcRoomManager.RoomManager roomManager = RpcRoomManager.RoomManager.newStub(rpcChannel);
				machineRoomManagerMap.put(machineId, roomManager);
				return roomManager;
			} else {
				logger.warn("machineid {} is malformed. it cannot get RpcRoomManager", machineId);
			}
		}
		return null;
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
		
		private User getRealUser(Room room) {
			//Create an AI User
			User realUser = null;
			int maxPower = 0;
			for ( UserInfo userInfo : room.getUserInfoList() ) {
				if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
				SessionKey userSessionKey = userInfo.getUserSessionKey();
				User user = GameContext.getInstance().findGlobalUserBySessionKey(userSessionKey);
				if ( user == null || user.isAI() || user.isProxy() ) continue;
				if ( user.getPower() > maxPower ) {
					maxPower = user.getPower();
					realUser = user;
				}
			}
			if ( realUser == null ) {
				logger.warn("#Checker: realUserId should not be null. It is a logic error!");
			}
			return realUser;
		}

		@Override
		public void run() {
			while ( true ) {
				//logger.info("#RedisRoomManager.Checker runs...");
				// Get all rooms managed by this JVM 
				int roomReadyTimeout = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_READY_TIMEOUT, 15000);
				RoomManager roomManager = RoomManager.getInstance();
				Collection<Room> roomCollection = roomManager.getLocalRoomCollection();
				ArrayList<SessionKey> deletedRoomKeys = new ArrayList<SessionKey>();
				
	//			logger.info("RoomManager's Checker starts to check total {} rooms.", roomCollection.size());
				int waitTime = Integer.MAX_VALUE;
				for ( Room room : roomCollection ) {
					try {
						Jedis jedis = JedisFactory.getJedis();
						//Refresh room status from database
						SessionKey roomSessionKey = room.getRoomSessionKey();
						room = roomManager.acquireRoom(roomSessionKey, false);
						if ( room == null ) {
							logger.debug("Room {} is already deleted from Redis.", roomSessionKey);
							deletedRoomKeys.add(roomSessionKey);
							continue;
						}
						if ( room.getRoomStatus() == RoomStatus.COMBAT ) {
							String value = jedis.hget(room.getRoomSessionKey().toString(), H_CREATED_DATE);
							boolean needToCheckRoom = false;
							if ( value == null ) {
								needToCheckRoom = true;
							} else {
								long createdMillis = Long.parseLong(value);
								int timeoutSeconds = GlobalConfig.getInstance().getIntProperty(
										GlobalConfigKey.combat_room_timeout_seconds) * 1000;
								if ( System.currentTimeMillis() - createdMillis > timeoutSeconds ) {
									needToCheckRoom = true;
								}
							}
							if ( needToCheckRoom ) {
								logger.debug("Room {} that is in combat status timeout. Check it now.", 
										room.getRoomSessionKey());
								roomManager.deleteRoomIfEmpty(room);
							} else {
								logger.debug("Ignore room {} that is in combat status.", room.getRoomSessionKey());
							}
						} else {
							boolean locked = room.tryLock();
							try {
								if ( !locked ) {
									logger.info("#Checker. room {} is locked by other threads", room.getRoomSessionKey());
									continue;
								}
								roomManager.checkRoom(room);
								if ( room.getRoomType() == RoomType.GUILD_ROOM ) {
									/**
									 * If the guild room is not matched for a long time,
									 * give user's a hint
									 * 2013-3-12
									 */
									if ( room.getRoomStatus() == RoomStatus.READY ) {
										if ( System.currentTimeMillis() - room.getReadyStartMillis() > 20000) {
											SysMessageManager.getInstance().sendClientInfoMessage(room.getOwnerSessionKey(), 
													"room.guild.match.timeout", Type.NORMAL);
											room.setReadyStartMillis(System.currentTimeMillis());
										}
									}
								} else if ( room.getRoomType() == RoomType.SINGLE_ROOM || room.getRoomType() == RoomType.MULTI_ROOM || 
										room.getRoomType() == RoomType.FRIEND_ROOM ) {
									String zsetName = room.getCurrentSetName();
									if ( zsetName != null && zsetName.startsWith("room_ready_set") ) {
										Long numberOfDelete = jedis.zrem(zsetName, room.getRoomSessionKey().toString());
										logger.debug("#checker: pop room {} from zsetName {}", room.getRoomSessionKey(), zsetName);
										if ( numberOfDelete != null && numberOfDelete.intValue() > 0 ) {
											boolean needToPushBack = true;
											/*
											if ( room.getRoomStatus() != RoomStatus.READY || 
													room.getRoomStatus() != RoomStatus.COMBAT ) {
												if ( System.currentTimeMillis() - room.getCreatedMillis() > 
													GameDataManager.getInstance().getGameDataAsInt(
															GameDataKey.ROOM_READY_TIMEOUT, 20000)) {
													logger.info("Room '{}' ready-clock is timeout. Should add AI users to start game. ", room.getRoomSessionKey().toString());
												}
											}
											*/
											//wangqi 2012-2-23
											if ( room.getRoomStatus() == RoomStatus.READY ) {
												if ( System.currentTimeMillis() - room.getReadyStartMillis() > 
													GameDataManager.getInstance().getGameDataAsInt(
															GameDataKey.ROOM_READY_TIMEOUT, 20000)) {

													logger.info("Room '{}' ready-clock is timeout and no matched room found. Add an AI room to start game. ",
															room.getRoomSessionKey().toString());
													User realUser = getRealUser(room);
													if ( realUser != null ) {
														if ( room.getRoomType() == RoomType.SINGLE_ROOM ) {
															User aiUser = AIManager.getInstance().createAIUser(realUser);
															Room aiRoom = RoomManager.getInstance().assignRoom(aiUser, RoomType.SINGLE_ROOM);
															RoomManager.getInstance().createBattleRoom(room, aiRoom);
															//BattleManager.getInstance().stageReady(aiUser.getSessionKey());
															needToPushBack = false;
														} else if ( room.getRoomType() == RoomType.MULTI_ROOM || room.getRoomType() == RoomType.FRIEND_ROOM ) {
															int userCount = room.getCurrentUserCount();
															User aiUser = AIManager.getInstance().createAIUser(realUser);
															/**
															 * The RoomType.FRIEND_ROOM should be matched with a RoomType.MULTI_ROOM
															 * if the opponent room is not-assigned.
															 */
															Room aiRoom = RoomManager.getInstance().createRoom(aiUser, RoomType.MULTI_ROOM);
															
															for ( int i=1; i<userCount; i++ ) {
																aiUser = AIManager.getInstance().createAIUser(realUser);
																RoomManager.getInstance().editSeat(aiRoom.getOwnerSessionKey(), i, true);
																RoomManager.getInstance().joinRoom(aiRoom.getRoomSessionKey(), aiUser.getSessionKey());
															}
															RoomManager.getInstance().createBattleRoom(room, aiRoom);
															
															//Send ai user readystart
															List<UserInfo> userInfos = aiRoom.getUserInfoList();
															for ( UserInfo aiUserInfo : userInfos ) {
																if ( aiUserInfo == null || aiUserInfo == Room.BLOCKED_USER_INFO ) continue;
																RoomManager.getInstance().sendUserReadyStart(room, aiUserInfo.getUserSessionKey(), 
																		room.getUserInfoList(), true);
															}
															needToPushBack = false;
														} else {
															logger.warn("#Checker: Only support SINGLE_ROOM AI users");
														}
													} else {
														RoomManager.getInstance().resetRoom(room);
														RoomManager.getInstance().deleteRoomIfEmpty(room);
													}
												}// if timeout ... 
												int leftMillis = roomReadyTimeout - (int)(System.currentTimeMillis() - room.getCreatedMillis());
												if ( waitTime > leftMillis ) {
													waitTime = leftMillis;
												}
											}
											
											//Put the room into redis again.
											//Double check the roomStatus is READY. Note the status may be changed in ckeckRoom() method.
											if ( needToPushBack ) {
												jedis.zadd(zsetName, System.currentTimeMillis(), room.getRoomSessionKey().toString());
												logger.debug("#checker: Push the room {} into {} set again", room.getRoomSessionKey(), zsetName);
											}
										} else {
											logger.info("Room {} is not found in zset {}", room.getRoomSessionKey(), zsetName);
											jedis.zadd(zsetName, room.getCreatedMillis(), room.getRoomSessionKey().toString());
										}
									} else if ( zsetName.startsWith("room_unfull_set") ) {
									//Popup the room from unfull set
										Long numberOfDelete = jedis.zrem(zsetName, room.getRoomSessionKey().toString());
										if ( numberOfDelete != null && numberOfDelete.intValue() > 0) {
											if ( room.getRoomStatus() == RoomStatus.UNFULL ) {
												if ( room.getRoomType() == RoomType.MULTI_ROOM ) {								
													int unfullTimeout = GameDataManager.getInstance().getGameDataAsInt(
															GameDataKey.ROOM_UNFULL_TIMEOUT, 2000);
													if ( System.currentTimeMillis() - room.getLastUserJoinMillis() > unfullTimeout ) {
														//Add a new ai user
														User realUser = getRealUser(room);
														if ( realUser != null ) {
															logger.debug("Room {} is in unfull timeout. Add an AI user", room.getRoomSessionKey());
															User aiUser = AIManager.getInstance().createAIUser(realUser);
															RoomManager.getInstance().joinRoom(room.getRoomSessionKey(), aiUser.getSessionKey());
															RoomManager.getInstance().readyStart(aiUser.getSessionKey(), true);
														} else {
															//roomManager.getInstance().deleteRoomIfEmpty(room);
															roomManager.getInstance().resetRoom(room);
															RoomManager.getInstance().deleteRoomIfEmpty(room);
														}
													}
												}
											}
											//Put the room into redis again.
											//Double check the roomStatus is READY. Note the status may be changed in ckeckRoom() method.
											if ( room.getRoomStatus() != RoomStatus.DELETED ) {
												zsetName = room.getCurrentSetName();
												jedis.zadd(zsetName, System.currentTimeMillis(), room.getRoomSessionKey().toString());
												logger.debug("#checker: Push the room {} into {} set again", room.getRoomSessionKey(), zsetName);
											}
										}
									}
								}
							} finally {
								if ( locked ) {
									room.unlock();
								}
							}
						}
					} catch (Throwable t) {
						logger.debug("RoomManager#CheckerException: {}", t);
					} finally {
					}
				}
				
				for ( SessionKey roomSessionKey : deletedRoomKeys ) {
					roomManager.removeLocalRoom(roomSessionKey);
				}
				
				//Set next round run's schedule
				if ( waitTime <= 0 || waitTime >= Integer.MAX_VALUE ) {
					waitTime = roomReadyTimeout;
				}

				//GameContext.getInstance().scheduleTask(this, waitTime, TimeUnit.MILLISECONDS);
//			logger.debug("The RoomManager's Check will run after {} milliseconds", waitTime);
				//logger.info("#RedisRoomManager.Checker sleeps...");
				try {
					Thread.currentThread().sleep(waitTime);
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
}
