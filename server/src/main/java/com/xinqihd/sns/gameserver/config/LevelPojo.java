package com.xinqihd.sns.gameserver.config;

/**
 * This pojo contains every level's information, including the
 * required experience, blood, skin and other data.
 * 
 * @author wangqi
 *
 */
public class LevelPojo implements Comparable<LevelPojo> {

	//Same as level
	private int _id; 
	
	//The level sequence number
	private int level;
	
	//The required experience to the next level 
	private int exp;
	
	//The sum of exp of all previous level.
	private int sumExp;
	
	//The damage per round
	private int dpr;
	
	//The blood
	private int blood;
	
	//The skin
	private int skin;
	
	//The attack
	private int attack;
	
	//The defend
	private int defend;
	
	//The agility
	private int agility;
	
	//The lucky
	private int lucky;

	/**
	 * @return the _id
	 */
	public int get_id() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void set_id(int _id) {
		this._id = _id;
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
	 * @return the exp
	 */
	public int getExp() {
		return exp;
	}

	/**
	 * @param exp the exp to set
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}

	/**
	 * @return the sumExp
	 */
	public int getSumExp() {
		return sumExp;
	}

	/**
	 * @param sumExp the sumExp to set
	 */
	public void setSumExp(int sumExp) {
		this.sumExp = sumExp;
	}

	/**
	 * @return the dpr
	 */
	public int getDpr() {
		return dpr;
	}

	/**
	 * @param dpr the dpr to set
	 */
	public void setDpr(int dpr) {
		this.dpr = dpr;
	}

	/**
	 * @return the blood
	 */
	public int getBlood() {
		return blood;
	}

	/**
	 * @param blood the blood to set
	 */
	public void setBlood(int blood) {
		this.blood = blood;
	}

	/**
	 * @return the skin
	 */
	public int getSkin() {
		return skin;
	}

	/**
	 * @param skin the skin to set
	 */
	public void setSkin(int skin) {
		this.skin = skin;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LevelPojo [_id=");
		builder.append(_id);
		builder.append(", level=");
		builder.append(level);
		builder.append(", exp=");
		builder.append(exp);
		builder.append(", dpr=");
		builder.append(dpr);
		builder.append(", blood=");
		builder.append(blood);
		builder.append(", skin=");
		builder.append(skin);
		builder.append(", attack=");
		builder.append(attack);
		builder.append(", defend=");
		builder.append(defend);
		builder.append(", agility=");
		builder.append(agility);
		builder.append(", lucky=");
		builder.append(lucky);
		builder.append("]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id;
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
		LevelPojo other = (LevelPojo) obj;
		if (_id != other._id)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LevelPojo o) {
		if ( o == null ) {
			return -1;
		}
		return _id - o._id;
	}
	
}
