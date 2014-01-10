package com.xinqihd.sns.gameserver.forge;

/**
 * 锻造的装备的结果
 * 
 * @author wangqi
 *
 */
public enum ForgeStatus {

	//0：无法强化；
	UNFORGABLE,
	//1：恭喜，强化成功！；
	SUCCESS,
	//2：强化失败
	FAILURE,
	//3: 因系统原因无法强化
	EXCEPTION,
	//4: 您的装备已达最大强化上限,无法继续强化
	MAX_LEVEL,
	//7: 您的金币不足，无法强化
	NO_MONEY,
	//8: 插槽不足
	NO_SLOT,
}
