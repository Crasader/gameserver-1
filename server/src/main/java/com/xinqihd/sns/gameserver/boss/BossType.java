package com.xinqihd.sns.gameserver.boss;

/**
 * The boss type
 * @author wangqi
 *
 */
public enum BossType {

	/**
	 * Single Boss. The game server will copy a clone 
	 * for every user to challenge.
	 */
	SINGLE,
	/**
	 * World Boss. The game server will share a boss
	 * for every user in the world.
	 */
	WORLD,
}
