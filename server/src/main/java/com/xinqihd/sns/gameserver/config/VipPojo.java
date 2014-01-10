package com.xinqihd.sns.gameserver.config;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseVipInfo;
import com.xinqihd.sns.gameserver.reward.Reward;

/**
 * The VIP config data.
 * 
 * @author wangqi
 *
 */
public class VipPojo implements Comparable<VipPojo> {

	private int _id = 0;
	
	//The number of months for this VIP	
	private int validSeconds = 0;
	
	private int yuanbaoPrice = 0;
	
	private String desc = null;
	
	private String giftId = null;
	
	/**
	 * @return the _id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(int id) {
		this._id = id;
	}

	/**
	 * @return the validSeconds
	 */
	public int getValidSeconds() {
		return validSeconds;
	}

	/**
	 * @param validSeconds the validSeconds to set
	 */
	public void setValidSeconds(int validSeconds) {
		this.validSeconds = validSeconds;
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
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * @return the giftId
	 */
	public String getGiftId() {
		return giftId;
	}

	/**
	 * @param giftId the giftId to set
	 */
	public void setGiftId(String giftId) {
		this.giftId = giftId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(VipPojo o) {
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
		return String.format("VipPojo [_id=%s, validSeconds=%s, yuanbaoPrice=%s]",
				_id, validSeconds, yuanbaoPrice);
	}

	/**
	 * Convert the vipPojo to google protobuf.
	 * @return
	 */
	public XinqiBseVipInfo.VipInfo toBseVipInfo() {
		XinqiBseVipInfo.VipInfo.Builder builder = XinqiBseVipInfo.VipInfo.newBuilder();
		builder.setViplevel(this._id);
		builder.setVipdesc(desc);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(giftId);
		if ( itemPojo != null ) {
			ArrayList<Reward> rewards = itemPojo.getRewards();
			for ( Reward reward : rewards ) {
				builder.addGifts(reward.toGift());
			}
		}
		return builder.build();
	}
}
