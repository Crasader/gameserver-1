package com.xinqihd.sns.gameserver.config;


/**
 * The biblio object
 * 
 * @author wangqi
 *
 */
public class BiblioPojo {

	/**
	 * The weapon's typeId
	 */
	private String weaponType;
	
	/**
	 * The higher level weapon id
	 */
	private String weaponId;
	
	private int power;
	private int attack;
	private int defend;
	private int agility;
	private int lucky;

	/**
	 * @return the weaponType
	 */
	public String getWeaponType() {
		return weaponType;
	}

	/**
	 * @param weaponType the weaponType to set
	 */
	public void setWeaponType(String weaponType) {
		this.weaponType = weaponType;
	}

	/**
	 * @return the weaponId
	 */
	public String getWeaponId() {
		return weaponId;
	}

	/**
	 * @param weaponId the weaponId to set
	 */
	public void setWeaponId(String weaponId) {
		this.weaponId = weaponId;
	}

	/**
	 * @return the power
	 */
	public int getPower() {
		return power;
	}

	/**
	 * @param power the power to set
	 */
	public void setPower(int power) {
		this.power = power;
	}

	/**
	 * @return the attack
	 */
	public int getAttack() {
		return attack;
	}

	/**
	 * @param attack the attack to set
	 */
	public void setAttack(int attack) {
		this.attack = attack;
	}

	/**
	 * @return the defend
	 */
	public int getDefend() {
		return defend;
	}

	/**
	 * @param defend the defend to set
	 */
	public void setDefend(int defend) {
		this.defend = defend;
	}

	/**
	 * @return the agility
	 */
	public int getAgility() {
		return agility;
	}

	/**
	 * @param agility the agility to set
	 */
	public void setAgility(int agility) {
		this.agility = agility;
	}

	/**
	 * @return the lucky
	 */
	public int getLucky() {
		return lucky;
	}

	/**
	 * @param lucky the lucky to set
	 */
	public void setLucky(int lucky) {
		this.lucky = lucky;
	}
	
}
