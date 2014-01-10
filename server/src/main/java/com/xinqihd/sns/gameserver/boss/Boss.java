package com.xinqihd.sns.gameserver.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBossInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseBossSync.BseBossSync;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The boss's current status
 * 
 * @author wangqi
 *
 */
public class Boss implements Comparable<Boss> {
	
	private static final Logger logger = LoggerFactory.getLogger(Boss.class);
	
	/**
	 * The id for the underlying BossPojo in database
	 */
	private String id;
	
	/**
	 * The bossId for this instance.
	 */
	private String bossId;

	/**
	 * it can be the blood to hurt from users 
	 * or the diamond to collect.
	 */
	private int progress;
	
	/**
	 * The total required progress for this boss.
	 */
	private int totalProgress;
	
	/**
	 * The minimum progress to win the boss.
	 */
	private int winProgress;
	
	/**
	 * The begin timestamp for this boss.
	 */
	private int beginSecond = 0;
	
	/**
	 * The end timeout timestamp for this boss.
	 */
	private int endSecond = 0;
	
	/**
	 * The boss current status.
	 */
	private BossStatus bossStatusType = null;
	
	/**
	 * The total required users to join the challenge
	 */
	private int totalUsers = 0;
	
	/**
	 * The total number of times that an user 
	 * can challenge the boss every hour.
	 */
	private int limit = 0;
	
	/**
	 * The increse number of times that an user
	 * can challenge the boss.
	 */
	private int increasePerHour = 0;
	
	/**
	 * The underlying boss pojo object
	 */
	private BossPojo bossPojo = null;

	//-------------------------------------- Instance methods

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
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * @return the timeoutMillis
	 */
	public int getEndSecond() {
		return endSecond;
	}

	/**
	 * @param timeoutMillis the timeoutMillis to set
	 */
	public void setEndSecond(int timeoutSecond) {
		this.endSecond = timeoutSecond;
	}

	/**
	 * @return the totalProgress
	 */
	public int getTotalProgress() {
		return totalProgress;
	}

	/**
	 * @param totalProgress the totalProgress to set
	 */
	public void setTotalProgress(int totalProgress) {
		this.totalProgress = totalProgress;
	}

	/**
	 * @return the beginMillis
	 */
	public int getBeginSecond() {
		return beginSecond;
	}

	/**
	 * @param beginMillis the beginMillis to set
	 */
	public void setBeginSecond(int beginSecond) {
		this.beginSecond = beginSecond;
	}

	/**
	 * @return the bossStatusType
	 */
	public BossStatus getBossStatusType() {
		return bossStatusType;
	}

	/**
	 * @param bossStatusType the bossStatusType to set
	 */
	public void setBossStatusType(BossStatus bossStatusType) {
		this.bossStatusType = bossStatusType;
	}

	/**
	 * @return the bossId
	 */
	public String getBossId() {
		return bossId;
	}

	/**
	 * @param bossId the bossId to set
	 */
	public void setBossId(String bossId) {
		this.bossId = bossId;
	}

	/**
	 * @return the totalUsers
	 */
	public int getTotalUsers() {
		return totalUsers;
	}

	/**
	 * @param totalUsers the totalUsers to set
	 */
	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}
	
	/**
	 * @return the winProgress
	 */
	public int getWinProgress() {
		return winProgress;
	}

	/**
	 * @param winProgress the winProgress to set
	 */
	public void setWinProgress(int winProgress) {
		this.winProgress = winProgress;
	}

	/**
	 * @return the bossPojo
	 */
	public BossPojo getBossPojo() {
		if ( bossPojo == null ) {
			bossPojo = BossManager.getInstance().getBossPojoById(id);
		}
		return bossPojo;
	}

	/**
	 * @param bossPojo the bossPojo to set
	 */
	public void setBossPojo(BossPojo bossPojo) {
		this.bossPojo = bossPojo;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * @return the increasePerHour
	 */
	public int getIncreasePerHour() {
		return increasePerHour;
	}

	/**
	 * @param increasePerHour the increasePerHour to set
	 */
	public void setIncreasePerHour(int increasePerHour) {
		this.increasePerHour = increasePerHour;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		//bossId, progress, totalprogress, beginMillis, endMillis, bossStatusType, totalUsers
		return String
				.format(
						"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
						id, bossId, progress, totalProgress, beginSecond, endSecond,
						bossStatusType, totalUsers, limit, increasePerHour, winProgress);
	}
	
	public static Boss fromString(String string) {
		Boss instance = null;
		if ( StringUtil.checkNotEmpty(string) ) {
			String[] fields = string.split(Constant.COMMA);
			if ( fields.length >= 11 ) {
				try {
					instance = new Boss();
					//id, bossId, progress, totalprogress, beginMillis, endMillis, bossStatusType, totalUsers
					String id = fields[0];
					instance.setId(id);
					String bossId = fields[1];
					instance.setBossId(bossId);
					int progress = StringUtil.toInt(fields[2], 0);
					instance.setProgress(progress);
					int totalProgress = StringUtil.toInt(fields[3],0);
					instance.setTotalProgress(totalProgress);
					int beginSecond = StringUtil.toInt(fields[4], 0);
					instance.setBeginSecond(beginSecond);
					int endSecond = StringUtil.toInt(fields[5], 0);
					instance.setEndSecond(endSecond);
					BossStatus bossStatusType = BossStatus.valueOf(fields[6]);
					instance.setBossStatusType(bossStatusType);
					int totalUsers = StringUtil.toInt(fields[7], 0);
					instance.setTotalUsers(totalUsers);
					int limit = StringUtil.toInt(fields[8], 0);
					instance.setLimit(limit);
					int increase = StringUtil.toInt(fields[9], 0);
					instance.setIncreasePerHour(increase);
					int winProgress = StringUtil.toInt(fields[10], 0);
					instance.setWinProgress(winProgress);
				} catch (Exception e) {
					logger.warn("Failed to create boss from str", e);
					instance = null;
				}
			} else {
				logger.info("#fromString: the BossInstance {} has wrong field {}", 
						string, fields.length);
			}
			return instance;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Boss o) {
		if ( o == null ) {
			return -1;
		}
		return this.id.compareTo(o.id);
//		int value = (int)(this.beginSecond - o.beginSecond);
//		if ( value == 0 ) {
//		}
//		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Boss other = (Boss) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Convert this object to Protobuf's BossInfo
	 * @return
	 */
	public XinqiBossInfo.BossInfo toXinqiBossInfo(User user) {
		//id, bossId, progress, totalprogress, 
		//beginMillis, endMillis, bossStatusType, totalUsers
		BossPojo bossPojo = this.getBossPojo();
		if ( bossPojo == null ) {
			return null;
		}
		XinqiBossInfo.BossInfo.Builder builder = XinqiBossInfo.BossInfo.newBuilder();
		builder.setId(id);
		builder.setBossid(bossId);
		builder.setProgress(progress);
		builder.setTotalprogress(totalProgress);
		int currentSecond = (int)(System.currentTimeMillis()/1000);
		if ( beginSecond <= currentSecond ) {
			//Boss is ready now
			builder.setBegintime(currentSecond);
			builder.setEndtime(endSecond);
		} else {
			//Boss is not ready now.
			builder.setBegintime(endSecond);
			builder.setEndtime(endSecond);
		}
		//Check reward taken status
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = BossManager.getInstance().getBossRewardUserKey(user, id);
		String rewardTaken = jedisDB.hget(key, BossManager.FIELD_BOSS_USER_REWARD);
		if ( Constant.ONE.equals(rewardTaken) ) {
			builder.setStatus(bossStatusType.TAKEN.ordinal());
		} else {
			builder.setStatus(bossStatusType.ordinal());
		}
		builder.setTotaluser(totalUsers);
		int challcount = BossManager.getInstance().
				getChallengeCount(user, this, System.currentTimeMillis());
		builder.setChallcount(challcount);

		builder.setName(bossPojo.getName());
		builder.setTitle(bossPojo.getTitle());
		builder.setTarget(bossPojo.getTarget());
		builder.setMapId(bossPojo.getMapId());
		builder.setDesc(bossPojo.getDesc());
		builder.setLevel(bossPojo.getLevel());
		
		builder.setAttack(bossPojo.getAttack());
		builder.setDefend(bossPojo.getDefend());
		builder.setAgility(bossPojo.getAgility());
		builder.setLucky(bossPojo.getLucky());
		builder.setThew(bossPojo.getThew());
		builder.setBlood(bossPojo.getBlood());
		
		builder.setBosstype(bossPojo.getBossType().ordinal());
		builder.setTotalround(bossPojo.getTotalRound());
		/**
		 * Change the reward type
		 */
		//List<Reward> rewards = bossPojo.getRewards();
		HashMap<HardMode, ArrayList<Reward>> rewards = bossPojo.getRewardMap(); 
		for ( HardMode hardMode : rewards.keySet() ) {
			XinqiBossInfo.BossReward.Builder rewardBuilder = XinqiBossInfo.BossReward.newBuilder();
			ArrayList<Reward> list = bossPojo.getRewards(hardMode);
			if ( list != null && list.size() > 0 ) {
				rewardBuilder.setHardmode(hardMode.getTitle());
				for ( Reward reward : list ) {
					if ( reward == null ) continue;
					rewardBuilder.addGifts(reward.toGift());
				}
				builder.addReward(rewardBuilder.build());
			}
		}
		builder.setWidth(bossPojo.getWidth());
		builder.setHeight(bossPojo.getHeight());
		
		builder.setDamage(0);
		builder.setSkin(0);
		int power = (int)Math.round(EquipCalculator.calculateWeaponPower(
				bossPojo.getAttack(), bossPojo.getDefend(), 
				bossPojo.getAgility(), bossPojo.getLucky(), 0, 0));
		builder.setPower(power);
		
		List<BossCondition> conditions = bossPojo.getRequiredConditions();
		if ( user != null && conditions != null ) {
			for ( BossCondition condition : conditions ) {
				builder.addCondition(condition.getRequiredInfo());
				boolean meet = condition.checkRequiredCondition(user, bossPojo);
				builder.addMeetcond(meet);
			}
		}

		return builder.build();
	}
	
	/**
	 * Convert this object to Protobuf's BossInfo
	 * @return
	 */
	public BseBossSync toXinqiBossSync(User user, boolean rewardTaken) {
		//id, bossId, progress, totalprogress, 
		//beginMillis, endMillis, bossStatusType, totalUsers
		BseBossSync.Builder builder = BseBossSync.newBuilder();
		builder.setBossid(bossId);
		if ( this.bossStatusType == BossStatus.SUCCESS ) {
			builder.setSuccess(true);
		}
		builder.setProgress(progress);
		builder.setTotalprogress(totalProgress);
		if ( rewardTaken ) {
			builder.setStatus(bossStatusType.TAKEN.ordinal());
		} else {
			builder.setStatus(bossStatusType.ordinal());
		}
		builder.setTotaluser(totalUsers);
		int challcount = BossManager.getInstance().
				getChallengeCount(user, this, System.currentTimeMillis());
		builder.setUsedtime(challcount);
		builder.setTotaluser(this.totalUsers);
		builder.setTotalprogress(this.totalProgress);
		builder.setProgress(this.progress);
		builder.setTotaltime(this.limit);
		
		return builder.build();
	}
}
