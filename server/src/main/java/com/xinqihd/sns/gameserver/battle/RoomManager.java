package com.xinqihd.sns.gameserver.battle;

import java.util.Collection;
import java.util.List;

import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * RoomManager is responsible for managing room and users.
 * @author wangqi
 *
 */
public abstract class RoomManager {
	
	public static final String ROOM_TYPE_KEY = "room_type";
	
	private static RoomManager instance = new RedisRoomManager();
	
	/**
	 * Protected method.
	 */
	protected RoomManager() {
		super();
	}
	
	/**
	 * Get the Singleton implementation
	 * @return
	 */
	public static RoomManager getInstance() {
		return instance;
	}
	
	/**
	 * Initialize the RoomManager
	 */
	public abstract void init();
	
	/**
	 * Destroy the RoomManager
	 */
	public abstract void destroy();
 
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
	public abstract Room assignRoom(User user, RoomType roomType);
	
	/**
	 * In the original design, leaving a room means changing to a new room.
	 * In the current implementation, leaving a room means leaving a room
	 * without joining a new room.
	 * 
	 * @param userSessionKey
	 * @return
	 */
	public abstract boolean leaveRoom(SessionKey userSessionKey);
	
	/**
	 * The user wants to change the room's default map.
	 * 
	 * @param userSessionKey
	 * @return
	 */
	public abstract boolean changeMap(SessionKey userSessionKey, String mapId);
	
	/**
	 * 
	 * @param userSessionKey
	 * @param isReady
	 * @return
	 */
	public abstract boolean readyStart(SessionKey userSessionKey, boolean isReady);
	
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
	public abstract boolean editSeat(SessionKey userSessionKey, int seatIndex, boolean isOpen);
	
	/**
	 * It will first check all users in the room is online and available. 
	 * If any user's sessionkey does not exist(offline), it will be removed from room
	 * and the room's status will be changed.
	 * 
	 * It then check if the room is timeout for waiting and change the room's status.
	 * 
	 * The method is mainly used by a maintenance thread.
	 * 
	 * @param room
	 * @return
	 */
	public abstract void checkRoom(Room room);
	
	
	// ----------------------------------------------- Supporting methods.
	
	/**
	 * Create a new room and managed it in current JVM.
	 * @param user
	 * @param roomType
	 * @return
	 */
	public abstract Room createRoom(User user, RoomType roomType);
	
	/**
	 * Delete a room from system. If there are users (except AI users) in room,
	 * it cannot be deleted and return false. 
	 * @param roomSessionKey
	 * @return
	 */
	public abstract boolean deleteRoomIfEmpty(Room room);
	
	/**
	 * The given user tries to join the given room. If the room is full just before
	 * this user joining (it is multi-thread), this method returns false, otherwise 
	 * returns true. 
	 * 
	 * @param roomSessionKey
	 * @param userSessionKey
	 * @return
	 */
	public abstract boolean joinRoom(SessionKey roomSessionKey, SessionKey userSessionKey);
	
	/**
	 * Kick an user from given room. The user will be assigned to another room randomly.
	 * @param userSessionKey
	 * @param sendBse TODO
	 * @param roomSessioinKey
	 * 
	 * @return
	 */
	public abstract boolean kickUser(Room room, SessionKey userSessionKey, boolean sendBse);
	
	/**
	 * Create a battle room with roomLeft and roomRight
	 * @return 
	 */
	public abstract void createBattleRoom(Room roomLeft, Room roomRight);
	/**
	 * Store the room status in database.
	 * @param room
	 */
	public abstract boolean storeRoom(Room room);
	
	/**
	 * Reset the room's status for the next battle.
	 * This method is called after the battle is over, both rooms
	 * will be reset to the following status: 
	 * 1> Room's creationMillis is set to current time.
	 * 2> Room's readyCount is set to 0.
	 * 3> Check room's user count 
	 *    if ( userCount < maxCount ) {
	 *      put the room in unfull list
	 *    } else {
	 *    	put the room in full list.
	 *    }
	 * 
	 * @param room
	 * @return
	 */
	public abstract boolean resetRoom(Room room);
	
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
	public abstract Room acquireRoom(SessionKey roomSessionKey, boolean getRemote);

	/**
	 * Update the user's session map in Redis.
	 * 
	 * @param userSessionKey
	 * @param isAI
	 * @return
	 */
	public abstract boolean updateUserSessionMap(SessionKey userSessionKey,
		 SessionKey roomSessionKey, boolean isAI, Pipeline pipeline);
	
	/**
	 * Find the Room's SessionKey by the user's SessionKey
	 * @param userSessionKey
	 * @return
	 */
	public abstract SessionKey findRoomSessionKeyByUserSession(SessionKey userSessionKey);

	/**
	 * Get all local rooms managed by this JVM.
	 * @return
	 */
	public abstract Collection<Room> getLocalRoomCollection();

	public abstract void removeLocalRoom(SessionKey roomSessionKey);

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
	public abstract Room popUnfullMultiUserRoom(User user);

	/**
	 * Send battle room notification to all users.
	 * @param battleRoom
	 * @param user
	 * @param pos
	 * @param sessionKey
	 */
	public abstract void sendBseEnterRoom(BattleRoom battleRoom);

	public abstract BattleRoom createBattleRoomWithoutBegin(Room roomLeft, Room roomRight);

	public abstract Room createRoom(User user, RoomType roomType, int basePos);

	/**
	 * Join the room at given index
	 * @param roomSessionKey
	 * @param sessionKey
	 * @param roomIdx
	 * @return
	 */
	public abstract boolean joinRoom(SessionKey roomSessionKey, SessionKey sessionKey, int roomIdx);

	/**
	 * Send the user ready start data to all users in the room
	 * @param room
	 * @param userSessionKey
	 * @param userInfos
	 * @param isReady
	 */
	public abstract void sendUserReadyStart(Room room, SessionKey userSessionKey,
			List<UserInfo> userInfos, boolean isReady);

	/**
	 * Match the room in redis
	 * @param roomLeft
	 * @return
	 */
	public abstract boolean matchRoom(Room roomLeft);

	public abstract int[] getRoomUserNumber(RoomType roomType);

	/**
	 * 
	 * @param battleRoom
	 */
	public abstract void sendBseUserLeaveRoom(BattleRoom battleRoom);

	/**
	 * 
	 * @param user
	 * @param roomType
	 * @param basePos
	 * @param roomSessionKey
	 * @return
	 */
	public abstract Room createRoom(User user, RoomType roomType, int basePos,
			SessionKey roomSessionKey);

	/**
	 * 
	 * @param leftUserSessionKey
	 * @param battleRoom
	 */
	public abstract void sendBseUserLeaveRoomToOthers(SessionKey leftUserSessionKey,
			BattleRoom battleRoom);

	/**
	 * 
	 * @param userSessionKey
	 * @param autoMode
	 * @return
	 */
	public abstract boolean changeAutomode(SessionKey userSessionKey, boolean autoMode);

}
