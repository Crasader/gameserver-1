package com.xinqihd.sns.gameserver.boss;

import com.xinqihd.sns.gameserver.entity.user.User;

/**
 *  Boss战斗结束后，服务器向玩家同步Boss战斗信息
 *  
 * @author wangqi
 *
 */
public class BossSync {

	private Boss boss = null;
	
	private User user = null;
	
	private int usedTimes = 0;
	
	private int totalTimes = 0;

	/**
	 * @return the boss
	 */
	public Boss getBoss() {
		return boss;
	}

	/**
	 * @param boss the boss to set
	 */
	public void setBoss(Boss boss) {
		this.boss = boss;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the usedTimes
	 */
	public int getUsedTimes() {
		return usedTimes;
	}

	/**
	 * @param usedTimes the usedTimes to set
	 */
	public void setUsedTimes(int usedTimes) {
		this.usedTimes = usedTimes;
	}

	/**
	 * @return the totalTimes
	 */
	public int getTotalTimes() {
		return totalTimes;
	}

	/**
	 * @param totalTimes the totalTimes to set
	 */
	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}
	
}
