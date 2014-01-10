package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.reward.Reward;


/**
 * 玩家输入CDKEY兑换礼品
 * @author wangqi
 *
 */
public class CDKeyPojo {

	private String _id;
	private String rewardStr;
	private String channel;
	private long startMillis;
	private long endMillis;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this._id = id;
	}
	
	/**
	 * @return the rewardStr
	 */
	public Reward getReward() {
		Reward reward = null;
		if ( rewardStr != null ) {
			reward = Reward.fromString(rewardStr);
		}
		return reward;
	}

	/**
	 * @param rewardStr the rewardStr to set
	 */
	public void setReward(Reward reward) {
		if (reward != null) {
			this.rewardStr = reward.toString();
		}
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
	
}
