package com.xinqihd.sns.gameserver.reward;

import java.util.EnumSet;

/**
 * The treasure box's type
 * @author wangqi
 *
 */
public enum RewardType {
	
  //获奖的道具ID
  //金币:-1
  //礼券:-2
  //元宝:-3
  //勋章:-4

	//extra experience
	EXP(0),
	//golden
	GOLDEN(1),
	//yuanbao
	YUANBAO(2),
	//
	VOUCHER(3),
	//
	MEDAL(4),
	//random combat tool
//	TOOL(5),
	//random item
	ITEM(6),
	//random stones
	STONE(7),
	//random weapon
	WEAPON(8),
	//achivement
	ACHIVEMENT(9),
  //unknown
	UNKNOWN(10);
	
	private int index;
	
	RewardType(int i) {
		this.index = i;
	}
	
	public int index() {
		return this.index;
	}
	
	public static final EnumSet<RewardType> TYPES = 
			EnumSet.noneOf(RewardType.class);
	
	static {
		for ( RewardType type : RewardType.values() ) {
			if ( type != UNKNOWN && type != ACHIVEMENT && type != VOUCHER && type != MEDAL ) {
				TYPES.add(type);
			}
		}
	}
}
