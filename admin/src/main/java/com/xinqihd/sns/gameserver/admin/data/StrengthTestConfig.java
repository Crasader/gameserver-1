package com.xinqihd.sns.gameserver.admin.data;

import java.util.List;

public class StrengthTestConfig {

	private List<Double> proxList = null;
	
	private int maxTry = 0;
	
	private int basePrice = 0;
	
	private boolean use15Lucky = false;
	
	private boolean use25Lucky = false;
	
	private int stoneLevel = 0;
	
	private boolean useGod = false;

	/**
	 * @return the proxList
	 */
	public List<Double> getProxList() {
		return proxList;
	}

	/**
	 * @param proxList the proxList to set
	 */
	public void setProxList(List<Double> proxList) {
		this.proxList = proxList;
	}

	/**
	 * @return the maxTry
	 */
	public int getMaxTry() {
		return maxTry;
	}

	/**
	 * @param maxTry the maxTry to set
	 */
	public void setMaxTry(int maxTry) {
		this.maxTry = maxTry;
	}

	/**
	 * @return the basePrice
	 */
	public int getBasePrice() {
		return basePrice;
	}

	/**
	 * @param basePrice the basePrice to set
	 */
	public void setBasePrice(int basePrice) {
		this.basePrice = basePrice;
	}

	/**
	 * @return the use15Lucky
	 */
	public boolean isUse15Lucky() {
		return use15Lucky;
	}

	/**
	 * @param use15Lucky the use15Lucky to set
	 */
	public void setUse15Lucky(boolean use15Lucky) {
		this.use15Lucky = use15Lucky;
	}

	/**
	 * @return the use25Lucky
	 */
	public boolean isUse25Lucky() {
		return use25Lucky;
	}

	/**
	 * @param use25Lucky the use25Lucky to set
	 */
	public void setUse25Lucky(boolean use25Lucky) {
		this.use25Lucky = use25Lucky;
	}

	/**
	 * @return the stoneLevel
	 */
	public int getStoneLevel() {
		return stoneLevel;
	}

	/**
	 * @param stoneLevel the stoneLevel to set
	 */
	public void setStoneLevel(int stoneLevel) {
		this.stoneLevel = stoneLevel;
	}

	/**
	 * @return the useGod
	 */
	public boolean isUseGod() {
		return useGod;
	}

	/**
	 * @param useGod the useGod to set
	 */
	public void setUseGod(boolean useGod) {
		this.useGod = useGod;
	}
	
	
}
