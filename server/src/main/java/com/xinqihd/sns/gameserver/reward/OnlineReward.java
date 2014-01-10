package com.xinqihd.sns.gameserver.reward;

import java.util.ArrayList;
import java.util.Collection;

public class OnlineReward {

	private int stepId = 0;
	
	private int remainSeconds = 0;
	
  //0: 每日登陆奖励 1：黄钻每日登陆奖励 2：黄钻新手奖励
	private int type = 0;
	
	private String timeClock = null;
	
	private ArrayList<Reward> rewards = new ArrayList<Reward>();
	
	private boolean isTaken = false;

	/**
	 * @return the stepId
	 */
	public int getStepId() {
		return stepId;
	}

	/**
	 * @param stepId the stepId to set
	 */
	public void setStepId(int stepId) {
		this.stepId = stepId;
	}

	/**
	 * @return the remainSeconds
	 */
	public int getRemainSeconds() {
		return remainSeconds;
	}

	/**
	 * @param remainSeconds the remainSeconds to set
	 */
	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the rewards
	 */
	public ArrayList<Reward> getRewards() {
		return rewards;
	}

	/**
	 * @param rewards the rewards to set
	 */
	public void setRewards(Collection<Reward> rewards) {
		this.rewards.clear();
		if ( rewards != null ) {
			this.rewards.addAll(rewards);
		}
	}

	/**
	 * @return the isTaken
	 */
	public boolean isTaken() {
		return isTaken;
	}

	/**
	 * @param isTaken the isTaken to set
	 */
	public void setTaken(boolean isTaken) {
		this.isTaken = isTaken;
	}

	/**
	 * @return the timeClock
	 */
	public String getTimeClock() {
		return timeClock;
	}

	/**
	 * @param timeClock the timeClock to set
	 */
	public void setTimeClock(String timeClock) {
		this.timeClock = timeClock;
	}
	
}
