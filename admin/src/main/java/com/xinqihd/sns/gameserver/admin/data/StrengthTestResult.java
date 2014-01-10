package com.xinqihd.sns.gameserver.admin.data;

/**
 * 强化测试的结果
 * @author wangqi
 *
 */
public class StrengthTestResult {

	//升级描述
	private String levelDesc = null;
	
	private int tryCount = 0;
	
	private int successCount = 0;
	
	private int downLevelCount = 0;
	
	private long costMoney = 0;
	
	private double successRatio = 0.0;
	
	private double increaseRatio = 0.0;

	/**
	 * @return the levelDesc
	 */
	public String getLevelDesc() {
		return levelDesc;
	}

	/**
	 * @param levelDesc the levelDesc to set
	 */
	public void setLevelDesc(String levelDesc) {
		this.levelDesc = levelDesc;
	}

	/**
	 * @return the tryCount
	 */
	public int getTryCount() {
		return tryCount;
	}

	/**
	 * @param tryCount the tryCount to set
	 */
	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}

	/**
	 * @return the successCount
	 */
	public int getSuccessCount() {
		return successCount;
	}

	/**
	 * @param successCount the successCount to set
	 */
	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	/**
	 * @return the costMoney
	 */
	public long getCostMoney() {
		return costMoney;
	}

	/**
	 * @param costMoney the costMoney to set
	 */
	public void setCostMoney(long costMoney) {
		this.costMoney = costMoney;
	}
	
	/**
	 * @return the downLevelCount
	 */
	public int getDownLevelCount() {
		return downLevelCount;
	}

	/**
	 * @param downLevelCount the downLevelCount to set
	 */
	public void setDownLevelCount(int downLevelCount) {
		this.downLevelCount = downLevelCount;
	}

	/**
	 * @return the successRatio
	 */
	public double getSuccessRatio() {
		return successRatio;
	}

	/**
	 * @return the increaseRatio
	 */
	public double getIncreaseRatio() {
		return increaseRatio;
	}

	/**
	 * @param increaseRatio the increaseRatio to set
	 */
	public void setIncreaseRatio(double increaseRatio) {
		this.increaseRatio = increaseRatio;
	}

	public void addTryCount() {
		this.tryCount++;
	}
	
	public void addSuccessCount() {
		this.successCount++;
		if ( this.successCount == 1 ) {
			this.successRatio = ((int)(1000000.0/this.tryCount))/10000.0;
		}
	}
	
	public void addDownLevelCount() {
		this.downLevelCount++;
	}
	
	public void addCostMoney(int costMoney) {
		this.costMoney+=costMoney;
	}
}
