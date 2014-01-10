package com.xinqihd.sns.gameserver.boss;

/**
 * The winning type for this boss
 * @author wangqi
 *
 */
public enum BossWinType {

	/**
	 * Kill one huge boss for world play
	 */
	KILL_ONE,
	/**
	 * Kill given number of bosses
	 */
	KILL_MANY,
	/**
	 * Just collect given diamonds.
	 */
	COLLECT_DIAMOND,
}
