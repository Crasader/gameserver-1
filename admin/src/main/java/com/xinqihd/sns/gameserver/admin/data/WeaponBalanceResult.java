package com.xinqihd.sns.gameserver.admin.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.entity.user.User;

/**
 * 武器平衡性测试的结果
 * 
 * @author wangqi
 *
 */
public class WeaponBalanceResult {
	
	public static final String ATTACK = "attack";
	public static final String ATT_RATIO = "att_ratio";
	public static final String CRI_RATIO = "cri_ratio";
	public static final String ROUND = "round";
	public static final String HURT = "hurt";
	public static final String BLOOD = "blood";
	
	private User user1;
	private User user2;
	private int user1Win;
	private int roundCount;
	private int user1Exp;
	private int user2Exp;
	private ArrayList<HashMap<String, Object>> 
		detailCombat = new ArrayList<HashMap<String, Object>>();
	private WeaponBalanceTestConfig config1  = null;
	private WeaponBalanceTestConfig config2  = null;

	public WeaponBalanceResult() {
		
	}

	/**
	 * @return the user1
	 */
	public User getUser1() {
		return user1;
	}

	/**
	 * @param user1 the user1 to set
	 */
	public void setUser1(User user1) {
		this.user1 = user1;
	}

	/**
	 * @return the user2
	 */
	public User getUser2() {
		return user2;
	}

	/**
	 * @param user2 the user2 to set
	 */
	public void setUser2(User user2) {
		this.user2 = user2;
	}

	/**
	 * @return the user1Win
	 */
	public int getUser1Win() {
		return user1Win;
	}

	/**
	 * @param user1Win the user1Win to set
	 */
	public void setUser1Win(int user1Win) {
		this.user1Win = user1Win;
	}

	/**
	 * @return the roundCount
	 */
	public int getRoundCount() {
		return roundCount;
	}

	/**
	 * @param roundCount the roundCount to set
	 */
	public void setRoundCount(int roundCount) {
		this.roundCount = roundCount;
	}
	
	/**
	 * @return the detailCombat
	 */
	public ArrayList<HashMap<String, Object>> getDetailCombat() {
		return detailCombat;
	}

	public void addDetail(HashMap<String, Object> detail) {
		this.detailCombat.add(detail);
	}

	/**
	 * @return the config1
	 */
	public WeaponBalanceTestConfig getConfig1() {
		return config1;
	}

	/**
	 * @param config1 the config1 to set
	 */
	public void setConfig1(WeaponBalanceTestConfig config1) {
		this.config1 = config1;
	}

	/**
	 * @return the config2
	 */
	public WeaponBalanceTestConfig getConfig2() {
		return config2;
	}

	/**
	 * @param config2 the config2 to set
	 */
	public void setConfig2(WeaponBalanceTestConfig config2) {
		this.config2 = config2;
	}

	/**
	 * @return the user1Exp
	 */
	public int getUser1Exp() {
		return user1Exp;
	}

	/**
	 * @param user1Exp the user1Exp to set
	 */
	public void setUser1Exp(int user1Exp) {
		this.user1Exp = user1Exp;
	}

	/**
	 * @return the user2Exp
	 */
	public int getUser2Exp() {
		return user2Exp;
	}

	/**
	 * @param user2Exp the user2Exp to set
	 */
	public void setUser2Exp(int user2Exp) {
		this.user2Exp = user2Exp;
	}
	
}
