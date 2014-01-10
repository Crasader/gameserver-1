package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;

/**
 * The reward config pojo 
 * 
 * @author wangqi
 *
 */
public class RewardLevelPojo {
	
	/**
	 * The reward id
	 */
	private String _id = null;
	
	/**
	 * The name desc for this rewardpojo
	 */
	private String name = null;

	/**
	 * The dropped ratio for this item
	 */
	private double ratio = 1;
	
	/**
	 * The min level for this item to drop.
	 */
	private int minLevel = 1;
	
	/**
	 * The max level for this item to drop.
	 */
	private int maxLevel = LevelManager.MAX_LEVEL;
	
	/**
	 * Whether it is enabled.
	 */
	private boolean enabled = true;
	

	/**
	 * @return the _id
	 */
	public String get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(String _id) {
		this._id = _id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ratio
	 */
	public double getRatio() {
		return ratio;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	/**
	 * @return the minLevel
	 */
	public int getMinLevel() {
		return minLevel;
	}

	/**
	 * @param minLevel the minLevel to set
	 */
	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
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
	
	/**
	 * Get the weapon id
	 * @param typeNme
	 * @return
	 */
	public String getWeaponId(int userLevel) {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(_id, userLevel);
		if ( weapon != null ) {
			return weapon.getId();
		} else {
			return _id;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RewardLevelPojo [_id=");
		builder.append(_id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", ratio=");
		builder.append(ratio);
		builder.append(", minLevel=");
		builder.append(minLevel);
		builder.append(", maxLevel=");
		builder.append(maxLevel);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append("]");
		return builder.toString();
	}
	
}
