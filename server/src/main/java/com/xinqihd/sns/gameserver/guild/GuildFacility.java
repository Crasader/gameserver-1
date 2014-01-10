package com.xinqihd.sns.gameserver.guild;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityLevelList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityList;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class GuildFacility {

	private GuildFacilityType type = null;
	
	private int level = 0;
	
	//使用公会设施所需要的最低贡献度
	private int credit = 0;
	
	//升级开始的时间戳
	private long upgradeBeginTime = 0l;
	
	//升级结束的时间戳，增加了cooldown的秒数
	private long upgradeEndTime = 0l;
	
	//这个公会设施是否激活
	private boolean enabled = true;
	
	//公会设施可能带有子设施
	private HashMap<GuildFacilityType, GuildFacility> facilities = 
			new HashMap<GuildFacilityType, GuildFacility>();

	/**
	 * @return the type
	 */
	public GuildFacilityType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(GuildFacilityType type) {
		this.type = type;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the upgradeBeginTime
	 */
	public long getUpgradeBeginTime() {
		return upgradeBeginTime;
	}

	/**
	 * @param upgradeBeginTime the upgradeBeginTime to set
	 */
	public void setUpgradeBeginTime(long upgradeBeginTime) {
		this.upgradeBeginTime = upgradeBeginTime;
	}

	/**
	 * @return the upgradeEndTime
	 */
	public long getUpgradeEndTime() {
		return upgradeEndTime;
	}

	/**
	 * @param upgradeEndTime the upgradeEndTime to set
	 */
	public void setUpgradeEndTime(long upgradeEndTime) {
		this.upgradeEndTime = upgradeEndTime;
	}

	/**
	 * Add a new sub facility
	 * @param subFacility
	 */
	public void addFacility(GuildFacility subFacility) {
		this.facilities.put(subFacility.getType(), subFacility);
	}
	
	/**
	 * @return the facilities
	 */
	public HashMap<GuildFacilityType, GuildFacility> getFacilities() {
		return facilities;
	}

	/**
	 * @return the credit
	 */
	public int getCredit() {
		return credit;
	}

	/**
	 * @param credit the credit to set
	 */
	public void setCredit(int credit) {
		this.credit = credit;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuildFacility other = (GuildFacility) obj;
		if (type != other.type)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GuildFacility [type=");
		builder.append(type);
		builder.append(", level=");
		builder.append(level);
		builder.append(", upgradeBeginTime=");
		builder.append(upgradeBeginTime);
		builder.append(", upgradeEndTime=");
		builder.append(upgradeEndTime);
		builder.append(", facilities=");
		builder.append(facilities);
		builder.append("]");
		return builder.toString();
	} 
	
	/**
	 * 这是为了显示设施权限而下行的数据
	 * 设施各等级所需贡献度
			1级	2级	3级	4级	5级	6级
		公会商城	100	770	1540	3850	9625	
		铁匠铺	120	800	2000	4000	10000	
		公会仓库	85	650	1300	3500	8500	

	 * 
	 * @return
	 */
	public XinqiBseGuildFacilityList.GuildFacility toGuildFacility() {
		if ( type == GuildFacilityType.shop || 
				type == GuildFacilityType.craft || 
				type == GuildFacilityType.storage ) {
			
			XinqiBseGuildFacilityList.GuildFacility.Builder builder = 
					XinqiBseGuildFacilityList.GuildFacility.newBuilder();
			/**
			 * 0: 商城
			 * 1: 铁匠铺
			 * 2: 仓库
			 * 3: 公会
			 * 4: 技能
			 */
			//设施的最低贡献度
			int[][] minCredits = GameDataManager.getInstance().getGameDataAsIntArrayArray(
					GameDataKey.GUILD_FACILITY_MIN_CREDIT);
			builder.setType(type.id());
			if ( type.id() < minCredits.length ) {
				int[] minCredit = minCredits[type.id()];
				for ( int level=1; level<=minCredit.length; level++ ) {
					int credit = minCredit[level-1];
					builder.addLevel(level);
					builder.addCredit(credit);
					String key = StringUtil.concat("guild.facility.", type.name(), ".lv", level);
					String desc = Text.text(key);
					builder.addDesc(desc);
				}
			}
			
			return builder.build();
		}
		return null;
	}

	public XinqiBseGuildFacilityLevelList.GuildFacility toLevelGuildFacility(Guild guild) {
		XinqiBseGuildFacilityLevelList.GuildFacility.Builder builder = 
				XinqiBseGuildFacilityLevelList.GuildFacility.newBuilder();
		builder.setType(this.type.id());
		int level = this.level; 
		builder.setEnabled(true);
		int[][] levelUpWealths = GameDataManager.getInstance().getGameDataAsIntArrayArray(GameDataKey.GUILD_LEVEL_WEALTH);
		int[] credits = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_ABILITY_CREDIT);
		if ( this.type == GuildFacilityType.guild ) {
			level = guild.getLevel();
			int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_LEVEL_MAXCOUNT);
			if ( level > 0 ) {
				int thisMax = maxCounts[level-1];
				String thisDesc1 = Text.text("guild.facility.guild.level", level);
				String thisDesc2 = Text.text("guild.facility.guild.people", thisMax);
				builder.addThisdesc(thisDesc1);
				builder.addThisdesc(thisDesc2);
			}
			if ( level < maxCounts.length ) {
				int nextMax = maxCounts[level];
				String nextDesc1 = Text.text("guild.facility.guild.level", level+1);
				String nextDesc2 = Text.text("guild.facility.guild.people", nextMax);
				builder.addNextdesc(nextDesc1);
				builder.addNextdesc(nextDesc2);
			}
			int[] levelUpWealth = levelUpWealths[type.id()];
			if ( level < levelUpWealth.length ) {
				String condition = Text.text("guild.facility.caifu", levelUpWealth[level]);
				builder.addConditions(condition);
			}
		} else if ( this.type == GuildFacilityType.shop ) {
			int[] levelUpWealth = levelUpWealths[type.id()];
			if ( level > 0 ) {
				String thisDesc1 = Text.text("guild.facility.shop.level", level);
				String thisDesc2 = Text.text("guild.facility.shop.people", level);
				builder.addThisdesc(thisDesc1);
				builder.addThisdesc(thisDesc2);
			}
			if ( level < levelUpWealth.length ) {
				String nextDesc1 = Text.text("guild.facility.shop.level", level+1);
				String nextDesc2 = Text.text("guild.facility.shop.people", level+1);
				builder.addNextdesc(nextDesc1);
				builder.addNextdesc(nextDesc2);
			}
			if ( level < levelUpWealth.length ) {
				String condition = Text.text("guild.facility.caifu", levelUpWealth[level]);
				builder.addConditions(condition);
			}
		} else if ( this.type == GuildFacilityType.storage ) {
			int[] maxCounts = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.GUILD_STORAGE_SIZE);
			int thisMax = 0;
			if ( level > 0 ) {
				thisMax = maxCounts[level-1];
				String thisDesc1 = Text.text("guild.facility.bag.level", level);
				String thisDesc2 = Text.text("guild.facility.bag.people", thisMax);
				builder.addThisdesc(thisDesc1);
				builder.addThisdesc(thisDesc2);
			}
			if ( level < maxCounts.length ) {
				int nextMax = maxCounts[level];
				String nextDesc1 = Text.text("guild.facility.bag.level", level+1);
				String nextDesc2 = Text.text("guild.facility.bag.people", nextMax);
				builder.addNextdesc(nextDesc1);
				builder.addNextdesc(nextDesc2);
			}
			int[] levelUpWealth = levelUpWealths[type.id()];
			if ( level < levelUpWealth.length ) {
				String condition = Text.text("guild.facility.caifu", levelUpWealth[level]);
				builder.addConditions(condition);
			}
		} else if ( this.type == GuildFacilityType.craft ) {
			double[] percents = GameDataManager.getInstance().getGameDataAsDoubleArray(GameDataKey.GUILD_CRAFT_STRENGTH);
			double thisPercent = 0;
			if ( level > 0 ) {
				thisPercent = percents[level-1];
				String thisDesc1 = Text.text("guild.facility.craft.level", level);
				String thisDesc2 = Text.text("guild.facility.craft.people", thisPercent*100);
				builder.addThisdesc(thisDesc1);
				builder.addThisdesc(thisDesc2);
			}
			if ( level < percents.length ) {
				double nextMax = percents[level];
				String nextDesc1 = Text.text("guild.facility.craft.level", level+1);
				String nextDesc2 = Text.text("guild.facility.craft.people", nextMax*100);
				builder.addNextdesc(nextDesc1);
				builder.addNextdesc(nextDesc2);
			}
			int[] levelUpWealth = levelUpWealths[type.id()];
			if ( level < levelUpWealth.length ) {
				String condition = Text.text("guild.facility.caifu", levelUpWealth[level]);
				builder.addConditions(condition);
			}
		} else if ( this.type == GuildFacilityType.ability ) {
			/**
			{ "guild.facility.ability.lv1", "可学习技能：防御" },
			{ "guild.facility.ability.lv2", "可学习技能：防御、幸运" },
			{ "guild.facility.ability.lv3", "可学习技能：防御、幸运、攻击" },
			{ "guild.facility.ability.lv4", "可学习技能：防御、幸运、攻击、敏捷" },
			{ "guild.facility.ability.lv5", "可学习技能：防御、幸运、攻击、敏捷、生命、寻宝、祈福" },
			 */
			builder.setEnabled(enabled);
			if ( level > 0 ) {
				String key = StringUtil.concat("guild.facility.",type.name(), ".lv", level);
				String thisDesc = Text.text(key);
				builder.addThisdesc(StringUtil.concat(type.getName(), "Lv", level));
				builder.addThisdesc(thisDesc);
			}
			if ( level < credits.length ) {
				String key = StringUtil.concat("guild.facility.",type.name(), ".lv", level+1);
				String thisDesc = Text.text(key);
				builder.addNextdesc(StringUtil.concat(type.getName(), "Lv", level+1));
				builder.addNextdesc(thisDesc);
			}
			int[] levelUpWealth = levelUpWealths[type.id()];
			if ( level < levelUpWealth.length ) {
				String condition = Text.text("guild.facility.caifu", levelUpWealth[level]);
				builder.addConditions(condition);
			}
		} else if ( this.type == GuildFacilityType.ab_agility ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 4 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_attack ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 3 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_blood ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 5 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_defend ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 1 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_lucky ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 2 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_pray ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 5 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		} else if ( this.type == GuildFacilityType.ab_treasure ) {
			boolean enabled = false;
			if ( guild.getFacility(GuildFacilityType.ability).getLevel() >= 5 ) {
				enabled = true;
			}
			descAbility(builder, level, credits, enabled);
		}
		builder.setLevel(level);
		//builder.addThisdesc("");
		//builder.addNextdesc(value);
		//builder.addConditions(value);
		return builder.build();
	}

	/**
	 * @param builder
	 * @param level
	 * @param levelUpWealths
	 */
	private void descAbility(
			XinqiBseGuildFacilityLevelList.GuildFacility.Builder builder, int level,
			int[] credits, boolean enabled) {
		
		builder.setEnabled(enabled);
		if ( level > 0 ) {
			String key = StringUtil.concat("guild.facility.",type.name(), ".lv", level);
			String thisDesc = Text.text(key);
			builder.addThisdesc(StringUtil.concat(type.getName(), "Lv", level));
			builder.addThisdesc(thisDesc);
		}
		if ( level < credits.length ) {
			String key = StringUtil.concat("guild.facility.",type.name(), ".lv", level+1);
			String thisDesc = Text.text(key);
			builder.addNextdesc(StringUtil.concat(type.getName(), "Lv", level+1));
			builder.addNextdesc(thisDesc);
		}
		if ( level < credits.length ) {
			String condition = Text.text("guild.facility.credit", credits[level]);
			builder.addConditions(condition);
		}
	}
}
