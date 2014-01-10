package com.xinqihd.sns.gameserver.guild;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.util.Text;

/**
 * 公会的设施类型
 * 0: 商城
 * 1: 铁匠铺
 * 2: 仓库
 * 3: 公会
 * 4: 技能
 * 
 * 技能类型
 * 10: 攻击
 * 11: 敏捷 
 * 12: 幸运 
 * 13: 防御 
 * 14: 生命 
 * 15: 寻宝 
 * 16: 祈福
 * 
 * @author wangqi
 *
 */
public enum GuildFacilityType {
  
	/**
	 * 注意shop, craft和storage的序号要保持0、1、2，因为GameDataManager中
	 * 按照这个顺序存储了最低的公会功勋：GUILD_FACILITY_MIN_CREDIT
	 */
	shop(0, 0),
	craft(1, 1),
	storage(2, 2),
	guild(3, 3),
	ability(4, 4),
	
	ab_attack(10, 5),
	ab_agility(11, 6),
	ab_lucky(12, 7),
	ab_defend(13, 8),
	ab_blood(14, 9),
	ab_treasure(15, 10),
	ab_pray(16, 11);
	
	private int id = 0;
	private int index = 0;
	private String name = null;
	private static HashMap<Integer, GuildFacilityType> map = 
			new HashMap<Integer, GuildFacilityType>(); 
	
	static {
		for ( GuildFacilityType f : GuildFacilityType.values() ) {
			map.put(f.id(), f);
		}
	}
	
	GuildFacilityType(int id, int index) {
		this.id = id;
		this.index = index;
		this.name = Text.text("guild.".concat(toString()));
	}
	
	/**
	 * Get the given GuildFacility from id.
	 * 
	 * @param id
	 * @return
	 */
	public static GuildFacilityType fromId(int id) {
		return map.get(id);
	}
	
	public int id() {
		return id;
	}
	
	/**
	 * 指向GameDataKey中双数组的索引
	 * GameDataKey.GUILD_LEVEL_WEALTH
	 * @return
	 */
	public int index() {
		return index;
	}
	
	public String getName() {
		return name;
	}
	
}
