package com.xinqihd.sns.gameserver.battle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Room is an unit for battles. If user choose 'single-player' mode, the room
 * will contain only one users. Otherwise, it will contain up to 4 users.
 * 
 * When all users in a room turn into 'ready' status, the ready room will be 
 * matched with another same level ready room. The two rooms become a battle
 * room. 
 * 
 * @author wangqi
 *
 */
public class Room {
	
	private static final Logger logger = LoggerFactory.getLogger(Room.class);
	
	public static final int MAX_USER = 4;
	
	public static final UserInfo BLOCKED_USER_INFO = new UserInfo();
	
	private SessionKey roomSessionKey;
	
	//The RPC server id.
	private String gameServerId; 
	
	//The room's game type.
	private RoomType roomType;
	
	//The room's status.
	private RoomStatus roomStatus;
	
	//The max number of users can join this room.
	private int maxUserCount;
	
	//The current number of users who joined this room.
	private int currentUserCount;

	//The number of users already in ready mode.
	private int readyUserCount;
	
	//The room's created millis.
	private long createdMillis;
	
	//The room's millis that it turns into readyStart status
	private long readyStartMillis;
	
	//The last user's joining time
	private long lastUserJoinMillis;
	
	//The room's owner combat power. It's used to match room
	private int ownerPower;
	
	//The room's average combat power. It's used to match room
	private int averagePower;
	
	//The owner's user level
	private int ownerLevel;
	
	//It's implementation specific. 
	//It's the current zset's name in Redis
	private String currentSetName;
	
	/**
	 * If the room is in battle mode, this field
	 * stores its battle session key.
	 */
	private SessionKey battleRoomSessionKey;
	
	/**
	 * True means this room is managed by remote JVM
	 * false otherwise.
	 */
	private boolean isRemote;
	
	//The room's owner
	private SessionKey ownerSessionKey  = null;
	
	private ArrayList<UserInfo> userLists = new ArrayList<UserInfo>(MAX_USER);
	
	private HashSet<SessionKey> readyUserSet = new HashSet<SessionKey>();
	
	private ReentrantLock lock = new ReentrantLock();
	
	private boolean roomStatusChanged = false;
	
	private String mapId = null;
	
	/**
	 * In PVE room, it stores the bossId
	 */
	private String roomKey = null;
	
	/**
	 * If true, the room battle will 
	 * do auto mode.
	 */
	private boolean autoMode = false;
	
	//The temporary battleRoom stored in memory
	private transient BattleRoom battleRoom;
	
	/**
	 * The max user's level
	 */
	private int maxLevel = 0;
		
	private Thread lockThread = null;
	
	public Room() {
		//Make placement for users.
		for ( int i=0; i<4; i++ ) {
			userLists.add(null);
		}
	}
	
	// --------------------------------------------- Properties.

	/**
	 * @return the rpcServerId
	 */
	public String getGameServerId() {
		return gameServerId;
	}

	/**
	 * @param rpcServerId the rpcServerId to set
	 */
	public void setGameServerId(String rpcServerId) {
		this.gameServerId = rpcServerId;
	}

	/**
	 * @return the roomType
	 */
	public RoomType getRoomType() {
		return roomType;
	}

	/**
	 * @param roomType the roomType to set
	 */
	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}
	
	/**
	 * @return the roomSessionKey
	 */
	public SessionKey getRoomSessionKey() {
		return roomSessionKey;
	}

	/**
	 * @param roomSessionKey the roomSessionKey to set
	 */
	public void setRoomSessionKey(SessionKey roomSessionKey) {
		this.roomSessionKey = roomSessionKey;
	}
	
	/**
	 * @return the maxUserCount
	 */
	public int getMaxUserCount() {
		return maxUserCount;
	}

	/**
	 * @param maxUserCount the maxUserCount to set
	 */
	public void setMaxUserCount(int maxUserCount) {
		if ( this.maxUserCount != maxUserCount ) {
			this.roomStatusChanged = true;
		}
		this.maxUserCount = maxUserCount;
	}

	/**
	 * @return the currentUserCount
	 */
	public int getCurrentUserCount() {
		return currentUserCount;
	}

	/**
	 * @param currentUserCount the currentUserCount to set
	 */
//	public void setCurrentUserCount(int currentUserCount) {
//		this.currentUserCount = currentUserCount;
//	}

	/**
	 * @return the readyUserCount
	 */
	public int getReadyUserCount() {
		return readyUserCount;
	}

	/**
	 * @param readyUserCount the currentReadyUserCount to set
	 */
	public void setReadyUserCount(int currentReadyUserCount) {
		if ( this.readyUserCount != currentReadyUserCount ) {
			this.roomStatusChanged = true;
		}
		this.readyUserCount = currentReadyUserCount;
	}

	/**
	 * @return the createdMillis
	 */
	public long getCreatedMillis() {
		return createdMillis;
	}

	/**
	 * @param createdMillis the createdMillis to set
	 */
	public void setCreatedMillis(long createdMillis) {
		this.createdMillis = createdMillis;
	}

	/**
	 * @return the ownerPower
	 */
	public int getOwnerPower() {
		return ownerPower;
	}

	/**
	 * @param ownerPower the ownerPower to set
	 */
	public void setOwnerPower(int ownerPower) {
		this.ownerPower = ownerPower;
	}

	/**
	 * @return the averagePower
	 */
	public int getAveragePower() {
		return averagePower;
	}

	/**
	 * @param averagePower the averagePower to set
	 */
	public void setAveragePower(int averagePower) {
		this.averagePower = averagePower;
	}

	/**
	 * @return the currentSetName
	 */
	public String getCurrentSetName() {
		return currentSetName;
	}

	/**
	 * @param currentSetName the currentSetName to set
	 */
	public void setCurrentSetName(String currentSetName) {
		this.currentSetName = currentSetName;
	}
	
	/**
	 * Add a new user into room.
	 * @param userInfo
	 */
	public void addUser(UserInfo userInfo) {
		if ( userInfo != null ) {
			for ( int i=0; i<MAX_USER; i++ ) {
				UserInfo ui = this.userLists.get(i);
				if ( ui == null || ui == Room.BLOCKED_USER_INFO ) {
					this.userLists.set(i, userInfo);
					this.currentUserCount++;
					if ( this.ownerSessionKey == null && i == 0 ) {
						this.ownerSessionKey = userInfo.getUserSessionKey();
						User user = GameContext.getInstance().findLocalUserBySessionKey(this.ownerSessionKey);
						if ( user != null ) {
							this.ownerLevel = user.getLevel();
							this.ownerPower = user.getPower();
						}
					}
					//update user redis
					Jedis jedis = JedisFactory.getJedis();
					String key = userInfo.getUserSessionKey().toString();
					if ( jedis.exists(key) ) {
						jedis.hset(key, 
							RedisRoomManager.H_ROOM_SESSION_KEY, this.roomSessionKey.toString());
					}
					this.setLastUserJoinMillis(System.currentTimeMillis());
					break;
				}
			}
		}
	}
	
	/**
	 * Set the given user at the specified seat.
	 * @param userInfo
	 * @param index
	 */
	public void setUser(UserInfo userInfo, int index) {
		if ( index >=0 && index < MAX_USER ) {
			UserInfo ui = this.userLists.get(index);
			this.userLists.set(index, userInfo);
			if ( (userInfo != null && userInfo != Room.BLOCKED_USER_INFO) && 
					(ui == null || ui == Room.BLOCKED_USER_INFO ) ) {
				this.currentUserCount++;
				if ( this.ownerSessionKey == null && index == 0 ) {
					this.ownerSessionKey = userInfo.getUserSessionKey();
					User user = GameContext.getInstance().findLocalUserBySessionKey(this.ownerSessionKey);
					if ( user != null ) {
						this.ownerLevel = user.getLevel();
						this.ownerPower = user.getPower();
					}
				}
				//update user redis
				Jedis jedis = JedisFactory.getJedis();
				String key = userInfo.getUserSessionKey().toString();
				if ( jedis.exists(key) ) {
					jedis.hset(userInfo.getUserSessionKey().toString(), 
						RedisRoomManager.H_ROOM_SESSION_KEY, this.roomSessionKey.toString());
				}
				this.setLastUserJoinMillis(System.currentTimeMillis());
			}
		}
	}
	
	/**
	 * Remove the user at given index.
	 * @param index
	 */
	public UserInfo removeUser(int index) {
		if ( index >=0 && index < MAX_USER ) {
			UserInfo userInfo = this.userLists.get(index);
			this.userLists.set(index, null);
			if ( userInfo != null && userInfo != Room.BLOCKED_USER_INFO ) {
				this.currentUserCount--;
				//update user redis
				Jedis jedis = JedisFactory.getJedis();
				String key = userInfo.getUserSessionKey().toString();
				if ( jedis.exists(key) ) {
					jedis.hdel(userInfo.getUserSessionKey().toString(), 
						RedisRoomManager.H_ROOM_SESSION_KEY);
				}
			}
			return userInfo;
		}
		return null;
	}
	
	/**
	 * Remove an user by his/her sessionkey
	 * @param userSessionKey
	 * @return
	 */
	public UserInfo removeUser(SessionKey userSessionKey) {
		if ( userSessionKey == null ) return null;
		int index = -1;
		for ( int i=0; i<MAX_USER; i++ ) {
			UserInfo userInfo = this.userLists.get(i);
			if ( userSessionKey.equals(userInfo.getUserSessionKey()) ) {
				index = i;
				break;
			}
		}
		if ( index >= 0 ) {
			return removeUser(index);
		}
		return null;
	}
	
	/**
	 * Get the underlying list. DO NOT MODIFY IT.
	 * @return
	 */
	public List<UserInfo> getUserInfoList() {
		return this.userLists;
	}

	/**
	 * @return the roomStatus
	 */
	public RoomStatus getRoomStatus() {
		return roomStatus;
	}

	/**
	 * @param roomStatus the roomStatus to set
	 */
	public void setRoomStatus(RoomStatus roomStatus) {
		if ( this.roomStatus != roomStatus ) {
			this.roomStatusChanged = true;
		}
		this.roomStatus = roomStatus;
	}

	/**
	 * @return the battleRoomSessionKey
	 */
	public SessionKey getBattleRoomSessionKey() {
		return battleRoomSessionKey;
	}

	/**
	 * @param battleRoomSessionKey the battleRoomSessionKey to set
	 */
	public void setBattleRoomSessionKey(SessionKey battleRoomSessionKey) {
		this.battleRoomSessionKey = battleRoomSessionKey;
	}
	
	/**
	 * @return the ownerSessionKey
	 */
	public SessionKey getOwnerSessionKey() {
		return ownerSessionKey;
	}

	/**
	 * @param ownerSessionKey the ownerSessionKey to set
	 */
	public void setOwnerSessionKey(SessionKey ownerSessionKey) {
		this.ownerSessionKey = ownerSessionKey;
	}

	/**
	 * @return the isRemote
	 */
	public boolean isRemote() {
		return isRemote;
	}

	/**
	 * @param isRemote the isRemote to set
	 */
	public void setRemote(boolean isRemote) {
		this.isRemote = isRemote;
	}
	
	/**
	 * Add a new user into ready set
	 * @param userSessionKey
	 */
	public void addReadyUser(SessionKey userSessionKey) {
		this.readyUserSet.add(userSessionKey);
	}
	
	/**
	 * Remove a user from ready set
	 * @param userSessionKey
	 */
	public void removeReadyUser(SessionKey userSessionKey) {
		this.readyUserSet.add(userSessionKey);
	}
	
	/**
	 * Get the ready user set.
	 * @return
	 */
	public Set<SessionKey> getReadyUserSet() {
		return this.readyUserSet;
	}

	/**
	 * Lock this room object in current JVM for further operation. 
	 */
	public final void lock() {
		try {
			lock.tryLock(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		//lock.lock();
		this.lockThread = Thread.currentThread();
	}
	
	/**
	 * Unlock the room object in current JVM for other threads.
	 */
	public final void unlock() {
		try {
			lock.unlock();
		} catch (RuntimeException e) {
			logger.error("Failed to unlock the room:{}", this.roomSessionKey);
		}
		this.lockThread = null;
	}
	
	/**
	 * Check if the room is locked by other threads.
	 * @return
	 */
	public boolean isLocked() {
		return lock.isLocked();
	}
	
	/**
	 * Acquires the lock only if it is not held by another thread at 
	 * the time of invocation. Acquires the lock if it is not held by 
	 * another thread and returns immediately with the value true, 
	 * setting the lock hold count to one. Even when this lock has been 
	 * set to use a fair ordering policy, a call to tryLock() will immediately 
	 * acquire the lock if it is available, whether or not other threads 
	 * are currently waiting for the lock. This "barging" behavior can 
	 * be useful in certain circumstances, even though it breaks fairness. 
	 * If you want to honor the fairness setting for this lock, then use 
	 * tryLock(0, TimeUnit.SECONDS) which is almost equivalent 
	 * (it also detects interruption). If the current thread already holds 
	 * this lock then the hold count is incremented by one and the method 
	 * returns true. If the lock is held by another thread then this method 
	 * will return immediately with the value false.
	 * 
	 * @return
	 */
	public boolean tryLock() {
		return lock.tryLock();
	}
	
	/**
	 * @return the readyStartMillis
	 */
	public long getReadyStartMillis() {
		return readyStartMillis;
	}

	/**
	 * @param readyStartMillis the readyStartMillis to set
	 */
	public void setReadyStartMillis(long readyStartMillis) {
		this.readyStartMillis = readyStartMillis;
	}

	/**
	 * @return the roomStatusChanged
	 */
	public boolean isRoomStatusChanged() {
		return roomStatusChanged;
	}

	/**
	 * @param roomStatusChanged the roomStatusChanged to set
	 */
	public void clearRoomStatusChanged() {
		this.roomStatusChanged = false;
	}

	/**
	 * @return the mapId
	 */
	public String getMapId() {
		return mapId;
	}

	/**
	 * @param mapId the mapId to set
	 */
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	/**
	 * @return the roomKey
	 */
	public String getRoomKey() {
		return roomKey;
	}

	/**
	 * @param roomKey the roomKey to set
	 */
	public void setRoomKey(String roomKey) {
		this.roomKey = roomKey;
	}

	/**
	 * @return the ownerLevel
	 */
	public int getOwnerLevel() {
		return ownerLevel;
	}

	/**
	 * @param ownerLevel the ownerLevel to set
	 */
	public void setOwnerLevel(int ownerLevel) {
		this.ownerLevel = ownerLevel;
	}

	/**
	 * @return the lastUserJoinMillis
	 */
	public long getLastUserJoinMillis() {
		return lastUserJoinMillis;
	}

	/**
	 * @param lastUserJoinMillis the lastUserJoinMillis to set
	 */
	public void setLastUserJoinMillis(long lastUserJoinMillis) {
		this.lastUserJoinMillis = lastUserJoinMillis;
	}

	/**
	 * @return the autoMode
	 */
	public boolean isAutoMode() {
		return autoMode;
	}

	/**
	 * @param autoMode the autoMode to set
	 */
	public void setAutoMode(boolean autoMode) {
		this.autoMode = autoMode;
	}

	/**
	 * @return the battleRoom
	 */
	public BattleRoom getBattleRoom() {
		return battleRoom;
	}

	/**
	 * @param battleRoom the battleRoom to set
	 */
	public void setBattleRoom(BattleRoom battleRoom) {
		this.battleRoom = battleRoom;
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}
	
  //--------------------------------------------- Inner Class.

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Room [roomSessionKey=");
		builder.append(roomSessionKey);
		builder.append(", rpcServerId=");
		builder.append(gameServerId);
		builder.append(", roomType=");
		builder.append(roomType);
		builder.append(", roomStatus=");
		builder.append(roomStatus);
		builder.append(", maxUserCount=");
		builder.append(maxUserCount);
		builder.append(", currentUserCount=");
		builder.append(currentUserCount);
		builder.append(", readyUserCount=");
		builder.append(readyUserCount);
		builder.append(", createdMillis=");
		builder.append(createdMillis);
		builder.append(", readyStartMillis=");
		builder.append(readyStartMillis);
		builder.append(", ownerPower=");
		builder.append(ownerPower);
		builder.append(", averagePower=");
		builder.append(averagePower);
		builder.append(", currentSetName=");
		builder.append(currentSetName);
		builder.append(", battleRoomSessionKey=");
		builder.append(battleRoomSessionKey);
		builder.append(", isRemote=");
		builder.append(isRemote);
		builder.append(", ownerSessionKey=");
		builder.append(ownerSessionKey);
		builder.append(", userLists=");
		builder.append(userLists);
		builder.append(", lock=");
		builder.append(lock);
		builder.append(", roomStatusChanged=");
		builder.append(roomStatusChanged);
		builder.append(", mapId=");
		builder.append(mapId);
		builder.append("]");
		return builder.toString();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + averagePower;
		result = prime
				* result
				+ ((battleRoomSessionKey == null) ? 0 : battleRoomSessionKey.hashCode());
		result = prime * result + (int) (createdMillis ^ (createdMillis >>> 32));
		result = prime * result
				+ ((currentSetName == null) ? 0 : currentSetName.hashCode());
		result = prime * result + currentUserCount;
		result = prime * result + (isRemote ? 1231 : 1237);
		result = prime * result + ((mapId == null) ? 0 : mapId.hashCode());
		result = prime * result + maxUserCount;
		result = prime * result + ownerPower;
		result = prime * result
				+ ((ownerSessionKey == null) ? 0 : ownerSessionKey.hashCode());
		result = prime * result
				+ (int) (readyStartMillis ^ (readyStartMillis >>> 32));
		result = prime * result + readyUserCount;
		result = prime * result
				+ ((roomSessionKey == null) ? 0 : roomSessionKey.hashCode());
		result = prime * result
				+ ((roomStatus == null) ? 0 : roomStatus.hashCode());
		result = prime * result + (roomStatusChanged ? 1231 : 1237);
		result = prime * result + ((roomType == null) ? 0 : roomType.hashCode());
		result = prime * result
				+ ((gameServerId == null) ? 0 : gameServerId.hashCode());
		result = prime * result + ((userLists == null) ? 0 : userLists.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room other = (Room) obj;
		if (averagePower != other.averagePower)
			return false;
		if (battleRoomSessionKey == null) {
			if (other.battleRoomSessionKey != null)
				return false;
		} else if (!battleRoomSessionKey.equals(other.battleRoomSessionKey))
			return false;
		if (createdMillis != other.createdMillis)
			return false;
		if (currentSetName == null) {
			if (other.currentSetName != null)
				return false;
		} else if (!currentSetName.equals(other.currentSetName))
			return false;
		if (currentUserCount != other.currentUserCount)
			return false;
		if (isRemote != other.isRemote)
			return false;
		if (mapId == null) {
			if (other.mapId != null)
				return false;
		} else if (!mapId.equals(other.mapId))
			return false;
		if (maxUserCount != other.maxUserCount)
			return false;
		if (ownerPower != other.ownerPower)
			return false;
		if (ownerSessionKey == null) {
			if (other.ownerSessionKey != null)
				return false;
		} else if (!ownerSessionKey.equals(other.ownerSessionKey))
			return false;
		if (readyStartMillis != other.readyStartMillis)
			return false;
		if (readyUserCount != other.readyUserCount)
			return false;
		if (roomSessionKey == null) {
			if (other.roomSessionKey != null)
				return false;
		} else if (!roomSessionKey.equals(other.roomSessionKey))
			return false;
		if (roomStatus != other.roomStatus)
			return false;
		//if (roomStatusChanged != other.roomStatusChanged)
		//	return false;
		if (roomType != other.roomType)
			return false;
		if (gameServerId == null) {
			if (other.gameServerId != null)
				return false;
		} else if (!gameServerId.equals(other.gameServerId))
			return false;
		if (userLists == null) {
			if (other.userLists != null)
				return false;
		} else if (!userLists.equals(other.userLists))
			return false;
		return true;
	}


	/**
	 * It's user's info in this room.
	 *
	 */
	public static class UserInfo {
		private SessionKey userSessionKey;
		private long userJoinTime;
		
		/**
		 * @return the userSessionKey
		 */
		public SessionKey getUserSessionKey() {
			return userSessionKey;
		}
		/**
		 * @param userSessionKey the userSessionKey to set
		 */
		public void setUserSessionKey(SessionKey userSessionKey) {
			this.userSessionKey = userSessionKey;
		}
		/**
		 * @return the userJoinTime
		 */
		public long getUserJoinTime() {
			return userJoinTime;
		}
		/**
		 * @param userJoinTime the userJoinTime to set
		 */
		public void setUserJoinTime(long userJoinTime) {
			this.userJoinTime = userJoinTime;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (userJoinTime ^ (userJoinTime >>> 32));
			result = prime * result
					+ ((userSessionKey == null) ? 0 : userSessionKey.hashCode());
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserInfo other = (UserInfo) obj;
			if (userJoinTime != other.userJoinTime)
				return false;
			if (userSessionKey == null) {
				if (other.userSessionKey != null)
					return false;
			} else if (!userSessionKey.equals(other.userSessionKey))
				return false;
			return true;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return StringUtil.concat("UserInfo [userSessionKey=", userSessionKey, 
					", userJoinTime=", userJoinTime, "]");
		}
		
	}
}
