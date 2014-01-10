package com.xinqihd.sns.gameserver.battle;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class BattleRoom {

	public static final String PREFIX = "BATTLE_";
	
	//The battle room's session key
	private SessionKey sessionKey;
	
	//The left room
	private Room roomLeft;
	
	//The right room
	private Room roomRight;
	
	private int roomLeftRound = 0;
	
	private int roomRightRound = 0;

	/**
	 * @return the sessionKey
	 */
	public SessionKey getSessionKey() {
		return sessionKey;
	}

	/**
	 * @param sessionKey the sessionKey to set
	 */
	public void setSessionKey(SessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * @return the roomLeft
	 */
	public Room getRoomLeft() {
		return roomLeft;
	}

	/**
	 * @param roomLeft the roomLeft to set
	 */
	public void setRoomLeft(Room roomLeft) {
		this.roomLeft = roomLeft;
		this.roomLeft.setBattleRoom(this);
	}

	/**
	 * @return the roomRigth
	 */
	public Room getRoomRight() {
		return roomRight;
	}

	/**
	 * @param roomRigth the roomRigth to set
	 */
	public void setRoomRigth(Room roomRigth) {
		this.roomRight = roomRigth;
		this.roomRight.setBattleRoom(this);
	}

	/**
	 * @return the roomLeftRound
	 */
	public int getRoomLeftRound() {
		return roomLeftRound;
	}

	/**
	 * @param roomLeftRound the roomLeftRound to set
	 */
	public void setRoomLeftRound(int roomLeftRound) {
		this.roomLeftRound = roomLeftRound;
	}

	/**
	 * @return the roomRightRound
	 */
	public int getRoomRightRound() {
		return roomRightRound;
	}

	/**
	 * @param roomRightRound the roomRightRound to set
	 */
	public void setRoomRightRound(int roomRightRound) {
		this.roomRightRound = roomRightRound;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomLeft == null) ? 0 : roomLeft.hashCode());
		result = prime * result + ((roomRight == null) ? 0 : roomRight.hashCode());
		result = prime * result
				+ ((sessionKey == null) ? 0 : sessionKey.hashCode());
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
		BattleRoom other = (BattleRoom) obj;
		if (roomLeft == null) {
			if (other.roomLeft != null)
				return false;
		} else if (!roomLeft.equals(other.roomLeft))
			return false;
		if (roomRight == null) {
			if (other.roomRight != null)
				return false;
		} else if (!roomRight.equals(other.roomRight))
			return false;
		if (sessionKey == null) {
			if (other.sessionKey != null)
				return false;
		} else if (!sessionKey.equals(other.sessionKey))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringUtil.concat("BattleRoom [sessionKey=", sessionKey, ", roomLeft=", roomLeft,
				", roomRight=", roomRight, "]");
	}

}
