package com.xinqihd.sns.gameserver.reward;

/**
 * 织女宝箱: 牛郎钥匙 
 * @author wangqi
 *
 */
public class RewardCondition {

	private String id;
	
	private int count;
	
	private RewardType rewardType;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the rewardType
	 */
	public RewardType getRewardType() {
		return rewardType;
	}

	/**
	 * @param rewardType the rewardType to set
	 */
	public void setRewardType(RewardType rewardType) {
		this.rewardType = rewardType;
	}
	
}
