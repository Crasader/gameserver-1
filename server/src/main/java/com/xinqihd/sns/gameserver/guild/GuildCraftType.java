package com.xinqihd.sns.gameserver.guild;

/**
 * 公会铁匠铺的加成类型
 * 
 * @author wangqi
 *
 */
public enum GuildCraftType {
	//装备合成
	COMPOSE_EQUIP,
	//石头升级
	COMPOSE_STONE,
	//颜色熔炼
	COMPOSE_COLOR,
  //装备强化
	COMPOSE_STRENGTH,
	//将石头合成到装备上
	COMPOSE_EQUIP_WITH_STONE,
	//装备转移
	COMPOSE_TRANSFER,
}
