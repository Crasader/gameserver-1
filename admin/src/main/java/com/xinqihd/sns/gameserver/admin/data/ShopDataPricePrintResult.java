package com.xinqihd.sns.gameserver.admin.data;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;

public class ShopDataPricePrintResult {

	private WeaponPojo weaponPojo = null;
	
	private int goldenPrice; 
	private int medalPrice;
	private int voucherPrice;
	private int yuanbaoPrice;
	
	private int normalGoldPrice;
	private int normalMedalPrice;
	private int normalVoucherPrice;
	private int normalYuanbaoPrice;
	
	private int solidGoldPrice;
	private int solidMedalPrice;
	private int solidVoucherPrice;
	private int solidYuanbaoPrice;
	
	private int eternalGoldPrice;
	private int eternalMedalPrice;
	private int eternalVoucherPrice;
	private int eternalYuanbaoPrice;

	/**
	 * @return the weaponPojo
	 */
	public WeaponPojo getWeaponPojo() {
		return weaponPojo;
	}

	/**
	 * @param weaponPojo the weaponPojo to set
	 */
	public void setWeaponPojo(WeaponPojo weaponPojo) {
		this.weaponPojo = weaponPojo;
	}

	/**
	 * @return the goldenPrice
	 */
	public int getGoldenPrice() {
		return goldenPrice;
	}

	/**
	 * @param goldenPrice the goldenPrice to set
	 */
	public void setGoldenPrice(int goldenPrice) {
		if ( goldenPrice < 15 ) {
			this.goldenPrice = 15;
		} else {
			this.goldenPrice = goldenPrice;
		}
	}

	/**
	 * @return the medalPrice
	 */
	public int getMedalPrice() {
		return medalPrice;
	}

	/**
	 * @param medalPrice the medalPrice to set
	 */
	public void setMedalPrice(int medalPrice) {
		this.medalPrice = medalPrice;
	}

	/**
	 * @return the voucherPrice
	 */
	public int getVoucherPrice() {
		return voucherPrice;
	}

	/**
	 * @param voucherPrice the voucherPrice to set
	 */
	public void setVoucherPrice(int voucherPrice) {
		this.voucherPrice = voucherPrice;
	}

	/**
	 * @return the yuanbaoPrice
	 */
	public int getYuanbaoPrice() {
		return yuanbaoPrice;
	}

	/**
	 * @param yuanbaoPrice the yuanbaoPrice to set
	 */
	public void setYuanbaoPrice(int yuanbaoPrice) {
		if ( yuanbaoPrice < 2 ) {
			this.yuanbaoPrice = 2;
		} else {
			this.yuanbaoPrice = yuanbaoPrice;
		}
	}

	/**
	 * @return the normalGoldPrice
	 */
	public int getNormalGoldPrice() {
		return normalGoldPrice;
	}

	/**
	 * @param normalGoldPrice the normalGoldPrice to set
	 */
	public void setNormalGoldPrice(int normalGoldPrice) {
		if ( normalGoldPrice < 30 ) {
			this.normalGoldPrice = 30;
		} else {
			this.normalGoldPrice = normalGoldPrice;
		}
	}

	/**
	 * @return the normalMedalPrice
	 */
	public int getNormalMedalPrice() {
		return normalMedalPrice;
	}

	/**
	 * @param normalMedalPrice the normalMedalPrice to set
	 */
	public void setNormalMedalPrice(int normalMedalPrice) {
		this.normalMedalPrice = normalMedalPrice;
	}

	/**
	 * @return the normalVoucherPrice
	 */
	public int getNormalVoucherPrice() {
		return normalVoucherPrice;
	}

	/**
	 * @param normalVoucherPrice the normalVoucherPrice to set
	 */
	public void setNormalVoucherPrice(int normalVoucherPrice) {
		this.normalVoucherPrice = normalVoucherPrice;
	}

	/**
	 * @return the normalYuanbaoPrice
	 */
	public int getNormalYuanbaoPrice() {
		return normalYuanbaoPrice;
	}

	/**
	 * @param normalYuanbaoPrice the normalYuanbaoPrice to set
	 */
	public void setNormalYuanbaoPrice(int normalYuanbaoPrice) {
		if ( normalYuanbaoPrice < 4 ) {
			this.normalYuanbaoPrice = 4;
		} else {
			this.normalYuanbaoPrice = normalYuanbaoPrice;
		}
	}

	/**
	 * @return the solidGoldPrice
	 */
	public int getSolidGoldPrice() {
		return solidGoldPrice;
	}

	/**
	 * @param solidGoldPrice the solidGoldPrice to set
	 */
	public void setSolidGoldPrice(int solidGoldPrice) {
		if ( solidGoldPrice < 75 ) {
			this.solidGoldPrice = 75;
		} else {
			this.solidGoldPrice = solidGoldPrice;
		}
	}

	/**
	 * @return the solidMedalPrice
	 */
	public int getSolidMedalPrice() {
		return solidMedalPrice;
	}

	/**
	 * @param solidMedalPrice the solidMedalPrice to set
	 */
	public void setSolidMedalPrice(int solidMedalPrice) {
		this.solidMedalPrice = solidMedalPrice;
	}

	/**
	 * @return the solidVoucherPrice
	 */
	public int getSolidVoucherPrice() {
		return solidVoucherPrice;
	}

	/**
	 * @param solidVoucherPrice the solidVoucherPrice to set
	 */
	public void setSolidVoucherPrice(int solidVoucherPrice) {
		this.solidVoucherPrice = solidVoucherPrice;
	}

	/**
	 * @return the solidYuanbaoPrice
	 */
	public int getSolidYuanbaoPrice() {
		return solidYuanbaoPrice;
	}

	/**
	 * @param solidYuanbaoPrice the solidYuanbaoPrice to set
	 */
	public void setSolidYuanbaoPrice(int solidYuanbaoPrice) {
		if ( solidYuanbaoPrice < 10 ) {
			this.solidYuanbaoPrice = 10;
		} else {
			this.solidYuanbaoPrice = solidYuanbaoPrice;
		}
	}

	/**
	 * @return the eternalGoldPrice
	 */
	public int getEternalGoldPrice() {
		return eternalGoldPrice;
	}

	/**
	 * @param eternalGoldPrice the eternalGoldPrice to set
	 */
	public void setEternalGoldPrice(int eternalGoldPrice) {
		if ( eternalGoldPrice < 150 ) {
			this.eternalGoldPrice = 150;
		} else {
			this.eternalGoldPrice = eternalGoldPrice;
		}
	}

	/**
	 * @return the eternalMedalPrice
	 */
	public int getEternalMedalPrice() {
		return eternalMedalPrice;
	}

	/**
	 * @param eternalMedalPrice the eternalMedalPrice to set
	 */
	public void setEternalMedalPrice(int eternalMedalPrice) {
		this.eternalMedalPrice = eternalMedalPrice;
	}

	/**
	 * @return the eternalVoucherPrice
	 */
	public int getEternalVoucherPrice() {
		return eternalVoucherPrice;
	}

	/**
	 * @param eternalVoucherPrice the eternalVoucherPrice to set
	 */
	public void setEternalVoucherPrice(int eternalVoucherPrice) {
		this.eternalVoucherPrice = eternalVoucherPrice;
	}

	/**
	 * @return the eternalYuanbaoPrice
	 */
	public int getEternalYuanbaoPrice() {
		return eternalYuanbaoPrice;
	}

	/**
	 * @param eternalYuanbaoPrice the eternalYuanbaoPrice to set
	 */
	public void setEternalYuanbaoPrice(int eternalYuanbaoPrice) {
		if ( eternalYuanbaoPrice < 20 ) {
			this.eternalYuanbaoPrice = 20;
		} else {
			this.eternalYuanbaoPrice = eternalYuanbaoPrice;
		}
	}
	
	
}
