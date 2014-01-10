package com.xinqihd.sns.gameserver.admin.data;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;

/**
 * 武器进行数值测试的配置对象
 * @author wangqi
 *
 */
public class WeaponBalanceTestConfig {

	private int minUserLevel = 1;
	private int maxUserLevel = 100;
	private boolean useRandomWeapon = true;
	private WeaponPojo selectedWeapon = null;
	private boolean useStrength = false;
	private int strengthMin = 1;
	private int strengthMax = 12;
	private boolean useOtherEquips = false;
	private boolean isAttackCompose = false;
	private boolean isDefendCompose = false;
	private boolean isAgilityCompose = false;
	private boolean isLuckCompose = false;
	private String composeStone = null;
	
	/**
	 * @return the minUserLevel
	 */
	public int getMinUserLevel() {
		return minUserLevel;
	}
	/**
	 * @param minUserLevel the minUserLevel to set
	 */
	public void setMinUserLevel(int minUserLevel) {
		this.minUserLevel = minUserLevel;
	}
	/**
	 * @return the maxUserLevel
	 */
	public int getMaxUserLevel() {
		return maxUserLevel;
	}
	/**
	 * @param maxUserLevel the maxUserLevel to set
	 */
	public void setMaxUserLevel(int maxUserLevel) {
		this.maxUserLevel = maxUserLevel;
	}
	/**
	 * @return the useRandomWeapon
	 */
	public boolean isUseRandomWeapon() {
		return useRandomWeapon;
	}
	/**
	 * @param useRandomWeapon the useRandomWeapon to set
	 */
	public void setUseRandomWeapon(boolean useRandomWeapon) {
		this.useRandomWeapon = useRandomWeapon;
	}
	/**
	 * @return the selectedWeapon
	 */
	public WeaponPojo getSelectedWeapon() {
		return selectedWeapon;
	}
	/**
	 * @param selectedWeapon the selectedWeapon to set
	 */
	public void setSelectedWeapon(WeaponPojo selectedWeapon) {
		this.selectedWeapon = selectedWeapon;
	}
	/**
	 * @return the useStrength
	 */
	public boolean isUseStrength() {
		return useStrength;
	}
	/**
	 * @param useStrength the useStrength to set
	 */
	public void setUseStrength(boolean useStrength) {
		this.useStrength = useStrength;
	}
	/**
	 * @return the useOtherEquips
	 */
	public boolean isUseOtherEquips() {
		return useOtherEquips;
	}
	/**
	 * @param useOtherEquips the useOtherEquips to set
	 */
	public void setUseOtherEquips(boolean useOtherEquips) {
		this.useOtherEquips = useOtherEquips;
	}
	/**
	 * @return the isAttackCompose
	 */
	public boolean isAttackCompose() {
		return isAttackCompose;
	}
	/**
	 * @param isAttackCompose the isAttackCompose to set
	 */
	public void setAttackCompose(boolean isAttackCompose) {
		this.isAttackCompose = isAttackCompose;
	}
	/**
	 * @return the isDefendCompose
	 */
	public boolean isDefendCompose() {
		return isDefendCompose;
	}
	/**
	 * @param isDefendCompose the isDefendCompose to set
	 */
	public void setDefendCompose(boolean isDefendCompose) {
		this.isDefendCompose = isDefendCompose;
	}
	/**
	 * @return the isAgilityCompose
	 */
	public boolean isAgilityCompose() {
		return isAgilityCompose;
	}
	/**
	 * @param isAgilityCompose the isAgilityCompose to set
	 */
	public void setAgilityCompose(boolean isAgilityCompose) {
		this.isAgilityCompose = isAgilityCompose;
	}
	/**
	 * @return the isLuckCompose
	 */
	public boolean isLuckCompose() {
		return isLuckCompose;
	}
	/**
	 * @param isLuckCompose the isLuckCompose to set
	 */
	public void setLuckCompose(boolean isLuckCompose) {
		this.isLuckCompose = isLuckCompose;
	}
	/**
	 * @return the composeStone
	 */
	public String getComposeStone() {
		return composeStone;
	}
	/**
	 * @param composeStone the composeStone to set
	 */
	public void setComposeStone(String composeStone) {
		this.composeStone = composeStone;
	}
	/**
	 * @return the strengthMin
	 */
	public int getStrengthMin() {
		return strengthMin;
	}
	/**
	 * @param strengthMin the strengthMin to set
	 */
	public void setStrengthMin(int strengthMin) {
		this.strengthMin = strengthMin;
	}
	/**
	 * @return the strengthMax
	 */
	public int getStrengthMax() {
		return strengthMax;
	}
	/**
	 * @param strengthMax the strengthMax to set
	 */
	public void setStrengthMax(int strengthMax) {
		this.strengthMax = strengthMax;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("测试配置 [用户最小等级=");
		builder.append(minUserLevel);
		builder.append(", 用户最大等级=");
		builder.append(maxUserLevel);
		builder.append(", 使用强化=");
		builder.append(useStrength);
		builder.append(", 从=");
		builder.append(strengthMin);
		builder.append(", 到=");
		builder.append(strengthMax);
		builder.append(", 激活全身装备=");
		builder.append(useOtherEquips);
		builder.append(", 攻击合成=");
		builder.append(isAttackCompose);
		builder.append(", 防御合成=");
		builder.append(isDefendCompose);
		builder.append(", 敏捷合成=");
		builder.append(isAgilityCompose);
		builder.append(", 幸运合成=");
		builder.append(isLuckCompose);
		builder.append(", 合成石类型=");
		builder.append(composeStone);
		builder.append("]");
		return builder.toString();
	}
	
}
