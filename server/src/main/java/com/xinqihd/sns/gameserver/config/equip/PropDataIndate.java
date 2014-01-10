package com.xinqihd.sns.gameserver.config.equip;

/**
 * It represents a weapon's solid type.
 * 
 * Refer to GameDataKey:
 * 	//Weapons
	WEAPON_INDATE_SIMPLE("weapon_indate_simple",   "'简陋'武器使用的最大次数"),
	WEAPON_INDATE_NORMAL("weapon_indate_normal",   "'普通'武器使用的最大次数"),
	WEAPON_INDATE_SOLID("weapon_indate_solid",     "'坚固'武器使用的最大次数"),
	WEAPON_INDATE_ETERNAL("weapon_indate_eternal", "'恒久'武器使用的最大次数"),
 * 
 * @author wangqi
 *
 */
public enum PropDataIndate {

	//简陋
	SIMPLE,
	//普通
	NORMAL,
	//坚固
	SOLID,
	//恒久 
	ETERNAL;
	
}
