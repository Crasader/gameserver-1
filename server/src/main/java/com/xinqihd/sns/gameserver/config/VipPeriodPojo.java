package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.proto.XinqiBseVipPeriodList.VipPeriod;

/**
 * The VIP period data
 * 
 * @author wangqi
 *
 */
public class VipPeriodPojo implements Comparable<VipPeriodPojo> {

	private int _id = 0;
	
	//The number of months for this VIP	
	private int month = 0;
	
	private int yuanbaoPrice = 0;
	
	private int voucherPrice = 0;
	
	private int medalPrice = 0;

	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this._id = month;
		this.month = month;
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
		this.yuanbaoPrice = yuanbaoPrice;
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
	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VipPeriodPojo o) {
		if ( o == null ) {
			return -1;
		}
		return this._id - o._id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VipPeriodPojo [_id=");
		builder.append(_id);
		builder.append(", month=");
		builder.append(month);
		builder.append(", yuanbaoPrice=");
		builder.append(yuanbaoPrice);
		builder.append(", voucherPrice=");
		builder.append(voucherPrice);
		builder.append(", medalPrice=");
		builder.append(medalPrice);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Convert to ProtoBuff's VipPeriod
	 * @return
	 */
	public VipPeriod toVipPeriod() {
		VipPeriod.Builder builder = VipPeriod.newBuilder();
		builder.setMonth(month);
		builder.setYuanbaoPrice(yuanbaoPrice);
		builder.setVoucherPrice(voucherPrice);
		builder.setMedalPrice(medalPrice);
		return builder.build();
	}
}
