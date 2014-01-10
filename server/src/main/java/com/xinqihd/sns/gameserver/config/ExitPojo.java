package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.reward.Reward;


/**
 * The game's tip can be displayed when game content is loading.
 * @author wangqi
 *
 */
public class ExitPojo {

	private int _id;
	private int days = 0;
	private String rewardStr;
	private String channel;
	private long startMillis;
	private long endMillis;
	
	/**
	 * @return the id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this._id = id;
	}

	/**
	 * @return the tip
	 */
	public Reward getReward() {
		if ( rewardStr != null ) {
			return Reward.fromString(rewardStr);
		} else {
			return null;
		}
	}

	/**
	 * @param tip the tip to set
	 */
	public void setReward(Reward reward) {
		this.rewardStr = reward.toString();
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the startMillis
	 */
	public long getStartMillis() {
		return startMillis;
	}

	/**
	 * @param startMillis the startMillis to set
	 */
	public void setStartMillis(long startMillis) {
		this.startMillis = startMillis;
	}

	/**
	 * @return the endMillis
	 */
	public long getEndMillis() {
		return endMillis;
	}

	/**
	 * @param endMillis the endMillis to set
	 */
	public void setEndMillis(long endMillis) {
		this.endMillis = endMillis;
	}

	/**
	 * @return the days
	 */
	public int getDays() {
		return days;
	}

	/**
	 * @param days the days to set
	 */
	public void setDays(int days) {
		this.days = days;
	}
	
}
