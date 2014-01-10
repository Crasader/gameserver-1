package com.xinqihd.sns.gameserver.boss.condition;

import com.xinqihd.sns.gameserver.boss.AbstractBossCondition;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user should reach specified level for given boss.
 * @author wangqi
 *
 */
public class LevelCondition extends AbstractBossCondition {
	
	/**
	 * The required minimun level for given boss
	 */
	private int minLevel = 1;
	private int maxLevel = 2;

	/**
	 * @return the minLevel
	 */
	public int getMinLevel() {
		return minLevel;
	}

	/**
	 * @param minLevel the minLevel to set
	 */
	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.boss.BossCondition#getRequiredInfo()
	 */
	@Override
	public String getRequiredInfo() {
		String info = Text.text("boss.condition.level", this.minLevel, this.maxLevel);
		return info;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.boss.BossCondition#checkRequiredCondition(com.xinqihd.sns.gameserver.entity.user.User, com.xinqihd.sns.gameserver.boss.BossPojo)
	 */
	@Override
	public boolean checkRequiredCondition(User user, BossPojo boss) {
		if ( user.getLevel()>=this.minLevel && user.getLevel()<this.maxLevel) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.boss.BossCondition#getErrorDescForUser(com.xinqihd.sns.gameserver.entity.user.User, com.xinqihd.sns.gameserver.boss.BossPojo)
	 */
	@Override
	public String getErrorDescForUser(User user, BossPojo boss) {
		if ( checkRequiredCondition(user, boss) ) {
			return null;
		} else {
			String info = Text.text("boss.condition.level.desc", this.minLevel, this.maxLevel);
			return info;
		}
	}

}
