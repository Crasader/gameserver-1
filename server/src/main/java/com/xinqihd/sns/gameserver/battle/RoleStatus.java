package com.xinqihd.sns.gameserver.battle;

/**
 * An user's status in a combat.
 * @author wangqi
 *
 */
public enum RoleStatus {
	NORMAL,
	DEAD,
	HIDDEN,
	ICED,
	FLYING;
	
	/**
	 * Set the user's role status bit.
	 * @param status
	 * @return
	 */
	public int toUserModeBit() {
		int userMode = 0;
		switch ( this ) {
			case DEAD:
				userMode |= 1;
				break;
			case HIDDEN:
				userMode |= 2;
				break;
			case ICED:
				userMode |= 4;
				break;
			case FLYING:
				userMode |= 8;
				break;
		}
		return userMode;
	}
	
	/**
	 * Check if the user is in given status
	 * @param userMode
	 * @return
	 */
	public static final RoleStatus checkStatus(int userMode) {
		if ( (userMode & 1) == 1 ) {
			return RoleStatus.DEAD;
		} else if ( (userMode & 2) == 2 ) {
			return RoleStatus.HIDDEN;
		} else if ( (userMode & 4) == 4 ) {
			return RoleStatus.ICED;
		} else if ( (userMode & 8) == 8 ) {
			return RoleStatus.FLYING;
		}
		return RoleStatus.NORMAL;
	}
}
