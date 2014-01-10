package com.xinqihd.sns.gameserver.battle;

import com.xinqihd.sns.gameserver.geom.BitSetImage;

/**
 * The bullet data for battle. It includes the normal attack as well as 
 * super attack.
 * 
 * @author wangqi
 *
 */
public class BattleBitSetBullet {
	
	//The bullet's name
	private String bulletName;

	//This is the normal bullet
	private BitSetImage bullet;
	
	//The is the super bullet
	private BitSetImage sBullet;
	
	/**
	 * @return the bulletName
	 */
	public String getBulletName() {
		return bulletName;
	}

	/**
	 * @param bulletName the bulletName to set
	 */
	public void setBulletName(String bulletName) {
		this.bulletName = bulletName;
	}

	/**
	 * @return the bullet
	 */
	public BitSetImage getBullet() {
		return bullet;
	}

	/**
	 * @param bullet the bullet to set
	 */
	public void setBullet(BitSetImage bullet) {
		this.bullet = bullet;
	}

	/**
	 * @return the sBullet
	 */
	public BitSetImage getsBullet() {
		return sBullet;
	}

	/**
	 * @param sBullet the sBullet to set
	 */
	public void setsBullet(BitSetImage sBullet) {
		this.sBullet = sBullet;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BattleBullet [bulletName=");
		builder.append(bulletName);
		builder.append(", bullet=");
		builder.append(bullet);
		builder.append(", sBullet=");
		builder.append(sBullet);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public BattleBitSetBullet clone() {
		BattleBitSetBullet bb = new BattleBitSetBullet();
		bb.bullet = this.bullet.clone();
		bb.bulletName = this.bulletName;
		bb.sBullet = this.sBullet.clone();
		return bb;
	}
}
