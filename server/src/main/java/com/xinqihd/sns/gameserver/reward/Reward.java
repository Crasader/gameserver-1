package com.xinqihd.sns.gameserver.reward;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseLoginLottery;
import com.xinqihd.sns.gameserver.proto.XinqiBseLoginLottery.LoginLotteryData;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The reward item that is picked after a battle is over.
 * 
 * @author wangqi
 *
 */
public class Reward {
	
	private static final Logger logger = LoggerFactory.getLogger(Reward.class);
	
	
  //获奖的道具ID
  //金币:-1
  //礼券:-2
  //元宝:-3
  //勋章:-4
	//经验:-5
	private String id = null;
	
	/**
	 * Weapon's type
	 */
	private String typeId = null;
	
  //---奖品类型---
	//经验值：EXP：0
	//金币：GOLDEN：1
	//元宝：YUANBAO：2
	//礼券：VOUCHER：3
	//金币：MEDAL：4
	//便携道具：TOOL: 5
	//背包道具：ITEM: 6
	//武器：WEAPON： 7
	private RewardType type = RewardType.UNKNOWN;
	
	//获取的道具等级，货币类为-1
	private int level = -1;
	
	//获取的数量
	private int count = 0;

	//总有效期
	private int indate = 0;
	
	//已经过期的次数
	private int usedTimes = 0;
	
	//For in battle treasure box
	private int x = 0;
	
	private int y = 0;
	
	private WeaponColor color = WeaponColor.WHITE;
	
	/**
	 * Does this reward need to be broadcasted to all users?
	 */
	private boolean isBroadcast = false;
	
	/**
	 * These fields contain the composed
	 * added value.
	 */
	private int addAttack = 0;
	private int addDefend = 0;
	private int addAgility = 0;
	private int addLucky= 0;
	/**
	 * 插槽数
	 */
	private int slot = 4;
	/**
	 * 最高强化等级
	 */
	private int maxStrength = 0;
	
	/**
	 * @return the propId
	 */
	public String getPropId() {
		return id;
	}

	/**
	 * @param propId the propId to set
	 */
	public void setPropId(String propId) {
		this.id = propId;
	}

	/**
	 * @return the type
	 */
	public RewardType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(RewardType type) {
		this.type = type;
	}

	/**
	 * @return the propLevel
	 */
	public int getPropLevel() {
		return level;
	}

	/**
	 * @param propLevel the propLevel to set
	 */
	public void setPropLevel(int propLevel) {
		this.level = propLevel;
	}

	/**
	 * @return the propCount
	 */
	public int getPropCount() {
		return count;
	}

	/**
	 * @param propCount the propCount to set
	 */
	public void setPropCount(int propCount) {
		this.count = propCount;
	}

	/**
	 * @return the propIndate
	 */
	public int getPropIndate() {
		return indate;
	}

	/**
	 * @param propIndate the propIndate to set
	 */
	public void setPropIndate(int propIndate) {
		this.indate = propIndate;
	}

	/**
	 * @return the propColor
	 */
	public WeaponColor getPropColor() {
		return color;
	}

	/**
	 * @param propColor the propColor to set
	 */
	public void setPropColor(WeaponColor propColor) {
		this.color = propColor;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the usedTimes
	 */
	public int getUsedTimes() {
		return usedTimes;
	}

	/**
	 * @param usedTimes the usedTimes to set
	 */
	public void setUsedTimes(int usedTimes) {
		this.usedTimes = usedTimes;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
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
	 * @return the addAttack
	 */
	public int getAddAttack() {
		return addAttack;
	}

	/**
	 * @param addAttack the addAttack to set
	 */
	public void setAddAttack(int addAttack) {
		this.addAttack = addAttack;
	}

	/**
	 * @return the addDefend
	 */
	public int getAddDefend() {
		return addDefend;
	}

	/**
	 * @param addDefend the addDefend to set
	 */
	public void setAddDefend(int addDefend) {
		this.addDefend = addDefend;
	}

	/**
	 * @return the addAgility
	 */
	public int getAddAgility() {
		return addAgility;
	}

	/**
	 * @param addAgility the addAgility to set
	 */
	public void setAddAgility(int addAgility) {
		this.addAgility = addAgility;
	}

	/**
	 * @return the addLucky
	 */
	public int getAddLucky() {
		return addLucky;
	}

	/**
	 * @param addLucky the addLucky to set
	 */
	public void setAddLucky(int addLucky) {
		this.addLucky = addLucky;
	}

	/**
	 * @return the isBroadcast
	 */
	public boolean isBroadcast() {
		return isBroadcast;
	}

	/**
	 * @param isBroadcast the isBroadcast to set
	 */
	public void setBroadcast(boolean isBroadcast) {
		this.isBroadcast = isBroadcast;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * @return the maxStrength
	 */
	public int getMaxStrength() {
		return maxStrength;
	}

	/**
	 * @param maxStrength the maxStrength to set
	 */
	public void setMaxStrength(int maxStrength) {
		this.maxStrength = maxStrength;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format(
						"{%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%d,%d,%d,%d,%d}",
						id, typeId, type, level, count, indate, x, y, color.ordinal(), isBroadcast, usedTimes, 
						addAttack, addDefend, addAgility, addLucky, slot, maxStrength);
	}
	
	/**
	 * Parse the string format reward back to Reward object
	 * @param rewardString
	 * @return
	 */
	public static Reward fromString(String rewardString) {
		if ( StringUtil.checkNotEmpty(rewardString) ) {
			if ( rewardString.length()>2 ) {
				int length = rewardString.length();
				if ( rewardString.charAt(0)=='{' && rewardString.charAt(length-1)=='}' ) {
					String string = rewardString.substring(1, length-1);
					String[] fields = string.split(Constant.COMMA);
					if ( fields.length >= 11 ) {
						Reward reward = new Reward();
						reward.id = fields[0];
						if ( "null".equals(fields[1]) || fields[1].length()==0 ) {
							reward.typeId = null;
						} else {
							reward.typeId = fields[1];
						}
						reward.type = RewardType.valueOf(fields[2]);
						reward.level = StringUtil.toInt(fields[3], 0);
						reward.count = StringUtil.toInt(fields[4], 1);
						reward.indate = StringUtil.toInt(fields[5], 30);
						reward.x = StringUtil.toInt(fields[6], 0);
						reward.y = StringUtil.toInt(fields[7], 0);
						int colorIndex = StringUtil.toInt(fields[8], 0);
						reward.color = WeaponColor.values()[colorIndex];
						reward.isBroadcast = Boolean.parseBoolean(fields[9]);
						reward.usedTimes = StringUtil.toInt(fields[10], 0);
						if ( fields.length>11 ) {
							reward.addAttack = StringUtil.toInt(fields[11], 0);
							reward.addDefend = StringUtil.toInt(fields[12], 0);
							reward.addAgility = StringUtil.toInt(fields[13], 0);
							reward.addLucky = StringUtil.toInt(fields[14], 0);
						}
						if ( fields.length>15 ) {
							reward.slot = StringUtil.toInt(fields[15], 0);
							reward.maxStrength = StringUtil.toInt(fields[16], 0);
						}
						return reward;
					} else {
						logger.debug("Reward.fromString: not enough length:{}", string, fields.length);	
					}
				} else {
					logger.debug("Reward.fromString: no brace:{}", rewardString);
				}
			} else {
				logger.debug("Reward.fromString: length less than 2:{}", rewardString);
			}
		} else {
			logger.debug("Reward.fromString: empty string:{}", rewardString);
		}
		return null;
	}

	/**
	 * Convert this object to ProtoBuf's LoginLotteryData
	 * @return
	 */
	public XinqiBseLoginLottery.LoginLotteryData toLoginLotteryData() {
		LoginLotteryData.Builder builder = LoginLotteryData.newBuilder();
		int intId = StringUtil.toInt(id, 0);
		builder.setId(intId);
		builder.setPropId(intId);
		builder.setLevel(level);
		builder.setCount(count);
		return builder.build();
	}
	
	public Gift toGift() {
		Gift.Builder builder = Gift.newBuilder();
		int quality = 0;
		switch ( this.type ) {
			case EXP:
				builder.setItemId("-5");
				break;
			case MEDAL:
				builder.setItemId("-4");
				break;
			case YUANBAO:
				builder.setItemId("-3");
				break;
			case VOUCHER:
				builder.setItemId("-2");
				break;
			case GOLDEN:
				builder.setItemId("-1");
				break;
			case ITEM:
			case STONE:
			case WEAPON:
				builder.setItemId(this.id);
				break;
			default:
				logger.warn("unsupport gift type:{}", this.type);
		}
		if ( this.typeId != null ) {
			builder.setTypeId(this.typeId);
		} else {
			builder.setTypeId(Constant.EMPTY);
		}
		builder.setRewardTypeIndex(this.type.ordinal());
		builder.setLevel(this.level);
		builder.setCount(this.count);
		builder.setIndate(this.indate);
		if ( this.type == RewardType.WEAPON ) {
			builder.setColorIndex(this.color.toIntColor());
		} 
		builder.setMaxlv(this.maxStrength);
		builder.setSlot(this.slot);
		return builder.build();
	}

	public static Reward fromGift(Gift gift) {
		Reward reward = new Reward();
		reward.id = gift.getItemId();
		reward.type = RewardType.values()[gift.getRewardTypeIndex()];
		reward.typeId = gift.getTypeId();
		reward.level = gift.getLevel();
		reward.count = gift.getCount();
		reward.indate = gift.getIndate();
		if (gift.getColorIndex() == WeaponColor.WHITE.toIntColor() ) {
			reward.color = WeaponColor.WHITE;
		} else if (gift.getColorIndex() == WeaponColor.GREEN.toIntColor() ) {
			reward.color = WeaponColor.GREEN;
		} else if (gift.getColorIndex() == WeaponColor.BLUE.toIntColor() ) {
			reward.color = WeaponColor.BLUE;
		} else if (gift.getColorIndex() == WeaponColor.PINK.toIntColor() ) {
			reward.color = WeaponColor.PINK;
		} else if (gift.getColorIndex() == WeaponColor.ORGANCE.toIntColor() ) {
			reward.color = WeaponColor.ORGANCE;
		}
		reward.maxStrength = gift.getMaxlv();
		reward.slot = gift.getSlot();
		return reward;
	}
	
	/**
	 * Convert the XinqiPropData. It only supports the 
	 * non-PropData type.
	 * @return
	 */
	public XinqiPropData.PropData toXinqiPropData(User user) {
		XinqiPropData.PropData.Builder builder = XinqiPropData.PropData.newBuilder();
		switch ( this.type ) {
			case EXP:
				builder.setPropID("-5");
				builder.setCount(this.count);
				break;
			case MEDAL:
				builder.setPropID("-4");
				builder.setCount(this.count);
				break;
			case YUANBAO:
				builder.setPropID("-3");
				builder.setCount(this.count);
				break;
			case VOUCHER:
				builder.setPropID("-2");
				builder.setCount(this.count);
				break;
			case GOLDEN:
				builder.setPropID("-1");
				builder.setCount(this.count);
				break;
			case ITEM:
			case STONE:
				PropData propData = RewardManager.getInstance().convertRewardItemToPropData(this);
				return propData.toXinqiPropData(user);
			case WEAPON:
				if ( user != null ) {
					propData = RewardManager.getInstance().convertRewardWeaponToPropData(this, user);
					return propData.toXinqiPropData(user);
				} else {
					return null;
				}
			default:
				logger.warn("unsupport propData type:{}", this.type);
				return null;
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + color.ordinal();
		result = prime * result + count;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + indate;
		result = prime * result + (isBroadcast ? 1231 : 1237);
		result = prime * result + level;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
		result = prime * result + usedTimes;
		result = prime * result + x;
		result = prime * result + y;
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
		Reward other = (Reward) obj;
		if (color != other.color)
			return false;
		if (count != other.count)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (indate != other.indate)
			return false;
		if (isBroadcast != other.isBroadcast)
			return false;
		if (level != other.level)
			return false;
		if (type != other.type)
			return false;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;
		if (usedTimes != other.usedTimes)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
}
