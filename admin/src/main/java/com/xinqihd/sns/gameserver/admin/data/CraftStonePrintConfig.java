package com.xinqihd.sns.gameserver.admin.data;

import com.xinqihd.sns.gameserver.entity.user.PropData;

public class CraftStonePrintConfig {

	private String stoneTypeId = null;
	private int userLevel = 1;
	private double[] qArray = null;
	private PropData equipProp = null;
	
	/**
	 * @return the stoneTypeId
	 */
	public String getStoneTypeId() {
		return stoneTypeId;
	}
	/**
	 * @param stoneTypeId the stoneTypeId to set
	 */
	public void setStoneTypeId(String stoneTypeId) {
		this.stoneTypeId = stoneTypeId;
	}
	
	/**
	 * @return the userLevel
	 */
	public int getUserLevel() {
		return userLevel;
	}
	/**
	 * @param userLevel the userLevel to set
	 */
	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}
	/**
	 * @return the qArray
	 */
	public double[] getqArray() {
		return qArray;
	}
	/**
	 * @param qArray the qArray to set
	 */
	public void setqArray(double[] qArray) {
		this.qArray = qArray;
	}
	/**
	 * @return the equipProp
	 */
	public PropData getEquipProp() {
		return equipProp;
	}
	/**
	 * @param equipProp the equipProp to set
	 */
	public void setEquipProp(PropData equipProp) {
		this.equipProp = equipProp;
	}
	
	
}
