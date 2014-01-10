package com.xinqihd.sns.gameserver.config;

import java.util.List;

import com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList;
import com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList.DailyMarkData;
import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * User can mark his login everyday for a cumulated reward.
 * This class contains the reward configuration data.
 * 
 * @author wangqi
 *
 */
public class DailyMarkPojo {
		
	//The reward's id.
	private String _id;
	
	/**
	 * The user's level for this reward 
	 */
	private int level;
	
	private int step;
	
	private int dayNum;
	
	//For now, there are totally 5 items for each award.
	private List<Reward> rewards;

	/**
	 * @return the dayNum
	 */
	public int getDayNum() {
		return dayNum;
	}

	/**
	 * @param dayNum the dayNum to set
	 */
	public void setDayNum(int dayNum) {
		this.dayNum = dayNum;
	}

	/**
	 * @return the rewards
	 */
	public List<Reward> getRewards() {
		return rewards;
	}

	/**
	 * @param rewards the rewards to set
	 */
	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}

	/**
	 * @return the _id
	 */
	public String getId() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(String _id) {
		this._id = _id;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(int step) {
		this.step = step;
	}
	
	/**
	 * Convert this object to ProtoBuf's Dailymark
	 * @return
	 */
	public XinqiBseDailyMarkList.DailyMarkData toDailyMarkData() {
		DailyMarkData.Builder builder = DailyMarkData.newBuilder();
		builder.setId(_id);
		builder.setStep(step);
		builder.setLevel(level);
		builder.setDaynum(dayNum);
		for ( Reward reward : rewards ) {
			XinqiBseDailyMarkList.Reward.Builder pbReward = XinqiBseDailyMarkList.Reward.newBuilder();
			pbReward.setItemId(reward.getPropId());
			pbReward.setLevel(reward.getPropLevel());
			pbReward.setNumber(reward.getPropCount());
			pbReward.setDesc(reward.getType().name());
			builder.addRewards(pbReward.build());
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("_id\t");
		builder.append(_id);
		builder.append("\tlevel\t");
		builder.append(level);
		builder.append("\tstep\t");
		builder.append(step);
		builder.append("\tdayNum\t");
		builder.append(dayNum);
		builder.append("\trewards\t");
		builder.append(rewards);
		return builder.toString();
	}
	
	
}
