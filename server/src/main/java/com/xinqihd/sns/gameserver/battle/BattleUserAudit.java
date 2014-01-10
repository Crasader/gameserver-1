package com.xinqihd.sns.gameserver.battle;

import java.util.HashMap;

import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * When an user attack other users in a battle, 
 * the game will do an exp audit for that user.
 * 
 * @author wangqi
 *
 */
public class BattleUserAudit {

	private SessionKey hurtUserSessionKey;
	
	private BattleUser battleUser;
	
	private boolean isEnemy = false;

	//伤害对方的血量
	private int hurtBlood = 0;
	
	private boolean isKilled = false;
	
	private boolean isDroped = false;
	
	//秒杀
	private boolean secondKillNum;

	//精确打击
	private int accurateNum;
	
	//temp data objects
	private HashMap<Object, Object> auditMap = new HashMap<Object, Object>(); 

	/**
	 * @return the hurtUserSessionKey
	 */
	public SessionKey getHurtUserSessionKey() {
		return hurtUserSessionKey;
	}

	/**
	 * @param hurtUserSessionKey the hurtUserSessionKey to set
	 */
	public void setHurtUserSessionKey(SessionKey hurtUserSessionKey) {
		this.hurtUserSessionKey = hurtUserSessionKey;
	}

	/**
	 * @return the isEnemy
	 */
	public boolean isEnemy() {
		return isEnemy;
	}

	/**
	 * @param isEnemy the isEnemy to set
	 */
	public void setEnemy(boolean isEnemy) {
		this.isEnemy = isEnemy;
	}

	/**
	 * @return the hurtBlood
	 */
	public int getHurtBlood() {
		return hurtBlood;
	}

	/**
	 * @param hurtBlood the hurtBlood to set
	 */
	public void setHurtBlood(int hurtBlood) {
		this.hurtBlood = hurtBlood;
	}

	/**
	 * @return the isKilled
	 */
	public boolean isKilled() {
		return isKilled;
	}

	/**
	 * @param isKilled the isKilled to set
	 */
	public void setKilled(boolean isKilled) {
		this.isKilled = isKilled;
	}

	/**
	 * @return the battleUser
	 */
	public BattleUser getBattleUser() {
		return battleUser;
	}

	/**
	 * @param battleUser the battleUser to set
	 */
	public void setBattleUser(BattleUser battleUser) {
		this.battleUser = battleUser;
	}

	/**
	 * @return the isDroped
	 */
	public boolean isDroped() {
		return isDroped;
	}

	/**
	 * @param isDroped the isDroped to set
	 */
	public void setDroped(boolean isDroped) {
		this.isDroped = isDroped;
	}
	
	/**
	 * Set the given battle audit item
	 * @param item
	 * @param value
	 */
	public void setBattleAuditItem(Object key, Object value) {
		if ( key != null && value != null) {
			this.auditMap.put(key, value);
		}
	}
	
	/**
	 * Get the given battle audit value.
	 * @param key
	 * @return
	 */
	public Object getBattleAuditItem(Object key) {
		Object value = this.auditMap.get(key);
		return value;
	}

	/**
	 * @return the secondKillNum
	 */
	public boolean isSecondKillNum() {
		return secondKillNum;
	}

	/**
	 * @param secondKillNum the secondKillNum to set
	 */
	public void setSecondKillNum(boolean secondKillNum) {
		this.secondKillNum = secondKillNum;
	}

	/**
	 * @return the accurateNum
	 */
	public int getAccurateNum() {
		return accurateNum;
	}

	/**
	 * @param accurateNum the accurateNum to set
	 */
	public void setAccurateNum(int accurateNum) {
		this.accurateNum = accurateNum;
	}

	/**
	 * @return the auditMap
	 */
	public HashMap<Object, Object> getAuditMap() {
		return auditMap;
	}

	/**
	 * @param auditMap the auditMap to set
	 */
	public void setAuditMap(HashMap<Object, Object> auditMap) {
		this.auditMap = auditMap;
	}
	
}
