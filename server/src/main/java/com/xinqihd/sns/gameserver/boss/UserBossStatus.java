package com.xinqihd.sns.gameserver.boss;

/**
 * The user's status for given boss.
 * It's only usable for world type boss.
 * 
 * @author wangqi
 *
 */
public class UserBossStatus {

	/**
	 * The user's current progress,
	 * it can be the blood to hurt 
	 * or the diamond to collect.
	 */
	private int progress;
	
	/**
	 * The current challenge times
	 */
	private int challengeTimes;
	
	/**
	 * The total available challenge times.
	 */
	private int totalChallengeTimes;
}
