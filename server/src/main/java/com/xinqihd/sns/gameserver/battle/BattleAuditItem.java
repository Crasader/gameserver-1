package com.xinqihd.sns.gameserver.battle;

/**
 * When an battle is end, the item is calculated to display 
 * how many exp of each item that the user got.
 *  
 * @author wangqi
 *
 */
public enum BattleAuditItem {

	//伤害对方的血量
	HurtBlood,
	//赢得了战斗
	WinGame,
	//总对战人数
	TotalUser,
	//等级差值加成
	PowerDiff,
	//命中率加成
	HitRatio,
	//杀敌数
	KillNum,
	//掉落数量
	DropNum,
	//完美击杀
	Perfect,
	//杀害自己人
	Spy,
	//秒杀
	SecondKill,
	//回合数加成
	RoundNum,
	//精确打击次数
	AccurateNum,
	
}
