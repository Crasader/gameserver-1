package com.xinqihd.sns.gameserver.admin.data;

/**
 * 强化石强化结果
 * @author wangqi
 *
 */
public class CraftStoneResult {

	//最终合成的数值
	private int finalData;
	//1级强化石合成的次数
	private int stone1Count;
	//1级强化石+15%幸运石出现的次数
	private int stone1Luck15Count;
	//1级强化石+25%幸运石出现的次数
	private int stone1Luck25Count;
	
	private int stone2Count;
	private int stone2Luck15Count;
	private int stone2Luck25Count;
	
	private int stone3Count;
	private int stone3Luck15Count;
	private int stone3Luck25Count;
	
	private int stone4Count;
	private int stone4Luck15Count;
	private int stone4Luck25Count;
	
	private int stone5Count;
	private int stone5Luck15Count;
	private int stone5Luck25Count;
	/**
	 * @return the finalData
	 */
	public int getFinalData() {
		return finalData;
	}
	/**
	 * @param finalData the finalData to set
	 */
	public void setFinalData(int finalData) {
		this.finalData = finalData;
	}
	/**
	 * @return the stone1Count
	 */
	public int getStone1Count() {
		return stone1Count;
	}
	/**
	 * @param stone1Count the stone1Count to set
	 */
	public void addStone1Count() {
		this.stone1Count++;
	}
	/**
	 * @return the stone1Luck15Count
	 */
	public int getStone1Luck15Count() {
		return stone1Luck15Count;
	}
	/**
	 * @param stone1Luck15Count the stone1Luck15Count to set
	 */
	public void addStone1Luck15Count() {
		this.stone1Luck15Count++;
	}
	/**
	 * @return the stone1Luck25Count
	 */
	public int getStone1Luck25Count() {
		return stone1Luck25Count;
	}
	/**
	 * @param stone1Luck25Count the stone1Luck25Count to set
	 */
	public void addStone1Luck25Count() {
		this.stone1Luck25Count++;
	}
	/**
	 * @return the stone2Count
	 */
	public int getStone2Count() {
		return stone2Count;
	}
	/**
	 * @param stone2Count the stone2Count to set
	 */
	public void addStone2Count() {
		this.stone2Count++;
	}
	/**
	 * @return the stone2Luck15Count
	 */
	public int getStone2Luck15Count() {
		return stone2Luck15Count;
	}
	/**
	 * @param stone2Luck15Count the stone2Luck15Count to set
	 */
	public void addStone2Luck15Count() {
		this.stone2Luck15Count++;
	}
	/**
	 * @return the stone2Luck25Count
	 */
	public int getStone2Luck25Count() {
		return stone2Luck25Count;
	}
	/**
	 * @param stone2Luck25Count the stone2Luck25Count to set
	 */
	public void addStone2Luck25Count() {
		this.stone2Luck25Count++;
	}
	/**
	 * @return the stone3Count
	 */
	public int getStone3Count() {
		return stone3Count;
	}
	/**
	 * @param stone3Count the stone3Count to set
	 */
	public void addStone3Count() {
		this.stone3Count++;
	}
	/**
	 * @return the stone3Luck15Count
	 */
	public int getStone3Luck15Count() {
		return stone3Luck15Count;
	}
	/**
	 * @param stone3Luck15Count the stone3Luck15Count to set
	 */
	public void addStone3Luck15Count() {
		this.stone3Luck15Count++;
	}
	/**
	 * @return the stone3Luck25Count
	 */
	public int getStone3Luck25Count() {
		return stone3Luck25Count;
	}
	/**
	 * @param stone3Luck25Count the stone3Luck25Count to set
	 */
	public void addStone3Luck25Count() {
		this.stone3Luck25Count++;
	}
	/**
	 * @return the stone4Count
	 */
	public int getStone4Count() {
		return stone4Count;
	}
	/**
	 * @param stone4Count the stone4Count to set
	 */
	public void addStone4Count() {
		this.stone4Count++;
	}
	/**
	 * @return the stone4Luck15Count
	 */
	public int getStone4Luck15Count() {
		return stone4Luck15Count;
	}
	/**
	 * @param stone4Luck15Count the stone4Luck15Count to set
	 */
	public void addStone4Luck15Count() {
		this.stone4Luck15Count++;
	}
	/**
	 * @return the stone4Luck25Count
	 */
	public int getStone4Luck25Count() {
		return stone4Luck25Count;
	}
	/**
	 * @param stone4Luck25Count the stone4Luck25Count to set
	 */
	public void addStone4Luck25Count() {
		this.stone4Luck25Count++;
	}
	/**
	 * @return the stone5Count
	 */
	public int getStone5Count() {
		return stone5Count;
	}
	/**
	 * @param stone5Count the stone5Count to set
	 */
	public void addStone5Count() {
		this.stone5Count++;
	}
	/**
	 * @return the stone5Luck15Count
	 */
	public int getStone5Luck15Count() {
		return stone5Luck15Count;
	}
	/**
	 * @param stone5Luck15Count the stone5Luck15Count to set
	 */
	public void addStone5Luck15Count() {
		this.stone5Luck15Count++;
	}
	/**
	 * @return the stone5Luck25Count
	 */
	public int getStone5Luck25Count() {
		return stone5Luck25Count;
	}
	/**
	 * @param stone5Luck25Count the stone5Luck25Count to set
	 */
	public void addStone5Luck25Count() {
		this.stone5Luck25Count++;
	}
	
	public void addCraftStoneResult(CraftStoneResult result) {
		this.stone1Count +=         result.stone1Count;    
		this.stone1Luck15Count +=   result.stone1Luck15Count;      
		this.stone1Luck25Count +=   result.stone1Luck25Count;      
		this.stone2Count +=         result.stone2Count;
		this.stone2Luck15Count +=   result.stone2Luck15Count;      
		this.stone2Luck25Count +=   result.stone2Luck25Count;      
		this.stone3Count +=         result.stone3Count;     
		this.stone3Luck15Count +=   result.stone3Luck15Count;      
		this.stone3Luck25Count +=   result.stone3Luck25Count;     
		this.stone4Count +=         result.stone4Count;     
		this.stone4Luck15Count +=   result.stone4Luck15Count;      
		this.stone4Luck25Count +=   result.stone4Luck25Count;      
		this.stone5Count +=         result.stone5Count;
		this.stone5Luck15Count +=   result.stone5Luck15Count;      
		this.stone5Luck25Count +=   result.stone5Luck25Count;      		
	}
}
