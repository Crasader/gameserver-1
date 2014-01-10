package com.xinqihd.sns.gameserver.forge;

/**
 * 合成物品的结果
 * 
 * @author wangqi
 *
 */
public enum ComposeStatus {

	//0：无法熔炼；
	UNCOMPOSABLE,
	//1：熔炼成功；
	SUCCESS,
	//2：熔炼失败
	FAILURE,
	//3:操作异常
	EXCEPTION,
	//4: 合成石已经是最大等级
	MAX_LEVEL,
	//5: 放入的合成石等级不一致
	DIFF_LEVEL,
	//6: 您必须放入4块相同的石头+1个熔炼公式方可熔炼
	INVALID_STONE,
	//7: 您的金币不足，无法熔炼
	NO_MONEY,
	//8: 熔炼武器需要相同武器相同颜色
	NOT_SAME_WEAPON,
	//9-12: 要求武器的颜色
	NOT_WHITE_WEAPON,
	NOT_GREEN_WEAPON,
	NOT_BLUE_WEAPON,
	NOT_PINK_WEAPON,
	NOT_ORANGE_WEAPON,
	//武器或者装备熔炼需要四把随机装备
	NOT_ENOUGH_WEAPON,
	NEED_CONFIRM,
}
