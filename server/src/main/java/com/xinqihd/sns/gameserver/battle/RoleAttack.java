package com.xinqihd.sns.gameserver.battle;

/**
 * It is a wrapper class to encapsulte all the data about a role attack
 * @author wangqi
 *
 */
public class RoleAttack {

	private int bulletQuatity = 0;
	
	private int attackTimes = 0;
	
	private BulletTrack[] bulletTracks = null;

	/**
	 * @return the bulletQuatity
	 */
	public int getBulletQuatity() {
		return bulletQuatity;
	}

	/**
	 * @param bulletQuatity the bulletQuatity to set
	 */
	public void setBulletQuatity(int bulletQuatity) {
		this.bulletQuatity = bulletQuatity;
	}

	/**
	 * @return the attackTimes
	 */
	public int getAttackTimes() {
		return attackTimes;
	}

	/**
	 * @param attackTimes the attackTimes to set
	 */
	public void setAttackTimes(int attackTimes) {
		this.attackTimes = attackTimes;
	}

	/**
	 * @return the bulletTracks
	 */
	public BulletTrack[] getBulletTracks() {
		return bulletTracks;
	}

	/**
	 * @param bulletTracks the bulletTracks to set
	 */
	public void setBulletTracks(BulletTrack[] bulletTracks) {
		this.bulletTracks = bulletTracks;
	}
}
