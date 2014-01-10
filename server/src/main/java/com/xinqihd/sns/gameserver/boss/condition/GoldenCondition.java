package com.xinqihd.sns.gameserver.boss.condition;

import com.xinqihd.sns.gameserver.boss.AbstractBossCondition;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user's bag should contain given PropData to challenge
 * the boss.
 * 
 * @author wangqi
 *
 */
public class GoldenCondition extends AbstractBossCondition {
	
	/**
	 * The boss will cost user's golden.
	 */
	private int golden = 0;

	/**
	 * @return the minLevel
	 */
	public int getGolden() {
		return golden;
	}

	/**
	 * @param minLevel the minLevel to set
	 */
	public void setGolden(int golden) {
		this.golden = golden;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.boss.BossCondition#getRequiredInfo()
	 */
	@Override
	public String getRequiredInfo() {
		String info = Text.text("boss.condition.golden", this.golden);
		return info;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.boss.BossCondition#checkRequiredCondition(com.xinqihd.sns.gameserver.entity.user.User, com.xinqihd.sns.gameserver.boss.BossPojo)
	 */
	@Override
	public boolean checkRequiredCondition(User user, BossPojo boss) {
		if ( user.getGolden()>=this.golden ) {
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
			String info = Text.text("boss.condition.golden", this.golden);
			return info;
		}
	}

	/**
	 * Ask users to pay for the golden required.
	 */
	@Override
	public boolean startChallenge(User user, BossPojo boss) {
		boolean success = ShopManager.getInstance().payForSomething(
				user, MoneyType.GOLDEN, this.golden, 1, null);
		StatClient.getIntance().sendDataToStatServer(user, StatAction.ConsumeBoss, 
				MoneyType.GOLDEN, this.golden, boss.getTitle());
		return success;
	}

}
