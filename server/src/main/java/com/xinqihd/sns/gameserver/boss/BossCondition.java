package com.xinqihd.sns.gameserver.boss;

import com.xinqihd.sns.gameserver.entity.user.User;

/**
 * The required conditions for challenge the boss.
 * @author wangqi
 *
 */
public interface BossCondition {

	/**
	 * Get the required info string for this condition
	 * @return
	 */
	public String getRequiredInfo();
	
	/**
	 * Check if the user meets the required conditions of given boss.
	 * 
	 * @return
	 */
	public boolean checkRequiredCondition(User user, BossPojo boss);
	
	/**
	 * If the user meets the required conditions of given boss, 
	 * return null string. Otherwise, return the message
	 * for user to display.
	 * 
	 * @return
	 */
	public String getErrorDescForUser(User user, BossPojo boss);
	
	/**
	 * The hook to be called before the challenge begin.
	 * If this method returns false, the challenge cannot begin.
	 * 
	 * @param user
	 * @param boss
	 */
	public boolean startChallenge(User user, BossPojo boss);
	
	/**
	 * The hook to be called before the challenge end
	 * 
	 * @param user
	 * @param boss
	 */
	public void endChallenge(User user, BossPojo boss);
}
