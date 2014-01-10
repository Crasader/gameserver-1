package com.xinqihd.sns.gameserver.boss;

import com.xinqihd.sns.gameserver.entity.user.User;

/**
 * This is the base adapter for BossCondition
 * 
 * @author wangqi
 *
 */
public abstract class AbstractBossCondition implements BossCondition {


	@Override
	public boolean startChallenge(User user, BossPojo boss) {
		return true;
	}

	@Override
	public void endChallenge(User user, BossPojo boss) {
	}

}
