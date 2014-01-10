package com.xinqihd.sns.gameserver.entity.user;

/**
 * 购买时的价值单位(来源确定) 0:金币/任务/历史遗留/战斗获得 1:礼金 2:功勋 3:元宝券 4.元宝
 * 
 * @author wangqi
 *
 */
public enum PropDataValueType {

	GAME(0),
	BONUS(1),
	MEDAL(2),
	COUPON(3),
	YUANBAO(4),
	REWARD(5);
	
	private int index = 0;
	
	PropDataValueType(int index) {
		this.index = index;
	}
	
}
