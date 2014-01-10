package com.xinqihd.sns.gameserver.boss;

/**
 * The boss type
 * @author wangqi
 *
 */
public enum BossStatus {

	/**
	 * This is a new and pending boss 
	 */
	NEW,
	/**
	 * This boss is open to attack
	 */
	PROGRESS,
	/**
	 * This boss is beaten by users
	 */
	SUCCESS,
	/**
	 * This boss reward is taken
	 */
	TAKEN,
	/**
	 * The boss challenge is running out of time.
	 */
	TIMEOUT,
}
