package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BattleRoom;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomStatus;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.HardMode;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceEnterRoom.BceEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceEnterRoomHandler is used for protocol EnterRoom 
 * @author wangqi
 *
 */
public class BceEnterRoomHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceEnterRoomHandler.class);
	
	private static final BceEnterRoomHandler instance = new BceEnterRoomHandler();
	
	private BceEnterRoomHandler() {
		super();
	}

	public static BceEnterRoomHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceEnterRoom");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceEnterRoom enterRoom = (BceEnterRoom)request.payload; 
		
		String roomId =  enterRoom.getRoomId();
		//String roomSessionKey = enterRoom.getRoomKey();
		int battleMode = enterRoom.getBattleMode();
		String challengeId = enterRoom.getChallengeId();
		int chooseMode = enterRoom.getChooseMode();
		int mapId = enterRoom.getMapId();
		String roomName = enterRoom.getRoomName();
		int roomType = enterRoom.getRoomType();
		int hardMode = enterRoom.getHardmode();
		RoomType rtype = null;
		boolean success = true;
		String roomKey = null;
		RoomManager roomManager = GameContext.getInstance().getRoomManager();
		User user = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
		if ( Constant.EMPTY.equals(roomId) ) {
			if ( roomType >=0 && roomType < RoomType.values().length ) {
				rtype = RoomType.values()[roomType];
				boolean isRoomLocked = checkRoomLock(user, rtype);
				if ( !isRoomLocked ) {
					String bossId = null;
					if ( rtype == RoomType.PVE_ROOM ) {
						//Check the PVE requirement
						bossId = challengeId;
						Boss boss = BossManager.getInstance().getBossInstance(user, challengeId);
						if ( boss != null ) {
							success = BossManager.getInstance().checkBossStatus(user, boss);
							if ( success ) {
								success = BossManager.getInstance().checkBossRequirement(user, boss.getBossPojo());
								if ( success ) {
									int count = BossManager.getInstance().getChallengeCount(user, boss, System.currentTimeMillis());
									if (count > 0) {
										success = true;
										bossId = challengeId;
										HardMode mode = HardMode.values()[hardMode];
										user.putUserData(BossManager.USER_BOSS_ID, boss);
										user.putUserData(BossManager.BOSS_HARDMODE, mode);
										roomKey = boss.getBossId();
									} else {
										SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.challenge.runout", Type.NORMAL);
										success = false;
									}
								}
							}
						} else {
							success = false;
							logger.warn("Failed to find the given boss {}", challengeId);
						}
					}
					//Check if the user is already in a room, because users may click the join room
					//button more than one time. The game server should not create more than one 
					//room for a given user.
					if ( success ) {
						SessionKey roomSession = roomManager.findRoomSessionKeyByUserSession(user.getSessionKey());
						Room room = null;
						if ( roomSession != null ) {
							logger.debug("User {} already in the room {}. delete it first:", user.getRoleName(), roomSession);
							room = roomManager.acquireRoom(roomSession, true);
							if ( room != null ) {
								roomManager.kickUser(room, sessionKey, true);
							} else {
								logger.debug("Room {} is already deleted.", roomSession);
							}
						}
						room = roomManager.assignRoom(user, rtype);
						if ( room != null ) {
							if ( mapId >= 0 ) {
								room.setMapId(String.valueOf(mapId));
							}
							room.setRoomKey(roomKey);
						}
						if ( room == null ) {
							SysMessageManager.getInstance().sendClientInfoMessage(user, "room.unsupport", Type.NORMAL);
						}
					}				
				}
			} else {
				logger.warn("Invalid RoomType {} ", roomType);
				success = false;
			}
		} else {
			SessionKey roomSessionKey = SessionKey.createSessionKeyFromHexString(roomId);
			Room room = roomManager.acquireRoom(roomSessionKey, true);
			rtype = room.getRoomType();
			if ( room == null ) {
				logger.debug("Failed to find room by roomId {}", roomId);
				success = false;
			} else if ( !room.isRemote() ) {
				if ( room.getRoomType() == RoomType.FRIEND_ROOM ) {
					/**
					 * The 'challengeId' is used for friendSessionKey
					 * The 'battleMode' is used for pos
					 * It is a special case to be called from InviteManager
					 */
					int pos = battleMode;
					SessionKey friendSessionKey = SessionKey.createSessionKeyFromHexString(challengeId);
					User friendUser = GameContext.getInstance().findGlobalUserBySessionKey(friendSessionKey);

					if ( pos >= Room.MAX_USER ) {
						BattleRoom battleRoom = room.getBattleRoom();
						if ( battleRoom == null ) {
							//Create a new battleRoom
							Room otherRoom = roomManager.createRoom(friendUser, RoomType.FRIEND_ROOM, 4);
							battleRoom = roomManager.createBattleRoomWithoutBegin(room, otherRoom);
							if ( battleRoom.getRoomLeft() == room ) {
								battleRoom.setRoomRigth(otherRoom);
							} else {
								battleRoom.setRoomLeft(otherRoom);
							}
							room.setRoomStatus(RoomStatus.UNFULL);
							otherRoom.setRoomStatus(RoomStatus.UNFULL);
							roomManager.storeRoom(room);
							roomManager.storeRoom(otherRoom);
						} else {
							Room otherRoom = battleRoom.getRoomRight();
							if ( room == otherRoom ) {
								otherRoom = battleRoom.getRoomLeft();
							}
							roomManager.joinRoom(otherRoom.getRoomSessionKey(), friendSessionKey, pos - Room.MAX_USER);
							//roomManager.storeRoom(otherRoom);
						}
					} else {
						roomManager.joinRoom(room.getRoomSessionKey(), friendSessionKey, pos);
						//roomManager.storeRoom(room);
					}
				} else {
					roomManager.joinRoom(roomSessionKey, sessionKey);
				}
			} else {
				logger.debug("Proxy BceEnterRoom to remote server. {}", room.getGameServerId());
				GameContext.getInstance().proxyToRemoteGameServer(sessionKey, room.getGameServerId(), enterRoom);
			}
		}
		
		StatAction action = StatAction.EnterRoom;
		UserActionKey actionKey = null;
		switch ( rtype ) {
			case TRAINING_ROOM:
				action = StatAction.EnterRoomTraining;
				actionKey = UserActionKey.EnterRoomTraining; 
				break;
			case SINGLE_ROOM:
				action = StatAction.EnterRoomSingle;
				actionKey = UserActionKey.EnterRoomTraining;
				break;
			case MULTI_ROOM:
				action = StatAction.EnterRoomMulti;
				actionKey = UserActionKey.EnterRoomMulti;
				break;
			case FRIEND_ROOM:
				action = StatAction.EnterRoomFriend;
				actionKey = UserActionKey.EnterRoomFriend;
				break;
			case DESK_ROOM:
				action = StatAction.EnterRoomDesk;
				actionKey = UserActionKey.EnterRoomDesk;
				break;
			case PVE_ROOM:
				action = StatAction.EnterRoomPVE;
				actionKey = UserActionKey.EnterRoomPVE;
				break;
			case CHALLENGE_ROOM:
				action = StatAction.EnterRoomChallenge;
				actionKey = UserActionKey.EnterRoomPVE;
				break;
			case GUILD_ROOM:
				action = StatAction.EnterRoomGuild;
				actionKey = UserActionKey.EnterRoomGuild;
				break;
			case RANK_ROOM:
				action = StatAction.EnterRoomRank;
				actionKey = UserActionKey.EnterRoomRank;
				break;
 		}

		StatClient.getIntance().sendDataToStatServer(user, action, rtype, mapId, success);
		UserActionManager.getInstance().addUserAction(user.getRoleName(), actionKey);
	}

	/**
	 * Check if the room is locked for given user.
	 * @param user
	 * @param roomType
	 * @return
	 */
	public static final boolean checkRoomLock(User user, RoomType roomType) {
		boolean isRoomLocked = false;
		String  lockInfo = null;
		ScriptResult result = ScriptManager.getInstance().
				runScript(ScriptHook.ROOM_CHECK_LOCK, user, roomType);
		if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
			isRoomLocked = (Boolean)result.getResult().get(0);
			lockInfo = (String)result.getResult().get(1);
			if ( isRoomLocked && lockInfo!=null ) {
				SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), lockInfo, 5000);
			}
		}
		return isRoomLocked;
	}
}
