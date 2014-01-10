package com.xinqihd.sns.gameserver.config;

import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.ChargeData;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ChargePojo implements Comparable<ChargePojo> {
	
	public static final String CHANNEL_IOS_IAP = "ios_iap";
	public static final String CHANNEL_CMCC = "cmcc_sms";
	public static final String CHANNEL_HUAWEI = "huawei";
	public static final String CHANNEL_OPPO = "oppo";
	public static final String CHANNEL_XIAOMI = "xiaomi";
	public static final String CHANNEL_DANGLE = "dangle";

	//The charge id
	private int _id;

	//The price for the good
	private float price;

	//The "$" is for Dollor and "Y" is for Yuan
	private CurrencyUnit currency;

	//The number of yuanbao
	private int yuanbao;

	//The discount is from 0.0 to 9.9
	private float discount = 0f;

	private boolean isHotSale;

	//The billing channel.
	private String channel;

	//The billing identifier for 
	//specific channel. For example,
	//the CMCC SMS billing method
	//wil need the SMS number.
	private String billingIdentifier;

	/**
	 * @return the id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this._id = id;
	}

	/**
	 * @return the price
	 */
	public float getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(float price) {
		this.price = price;
	}


	/**
	 * @return the priceUnit
	 */
	public CurrencyUnit getCurrency() {
		return currency;
	}


	/**
	 * @param priceUnit the priceUnit to set
	 */
	public void setCurrency(CurrencyUnit priceUnit) {
		this.currency = priceUnit;
	}


	/**
	 * @return the yuanbao
	 */
	public int getYuanbao() {
		return yuanbao;
	}


	/**
	 * @param yuanbao the yuanbao to set
	 */
	public void setYuanbao(int yuanbao) {
		this.yuanbao = yuanbao;
	}


	/**
	 * @return the discount
	 */
	public float getDiscount() {
		return discount;
	}


	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(float discount) {
		this.discount = discount;
	}


	/**
	 * @return the isHotSale
	 */
	public boolean isHotSale() {
		return isHotSale;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the billingIdentifier
	 */
	public String getBillingIdentifier() {
		return billingIdentifier;
	}

	/**
	 * @param billingIdentifier the billingIdentifier to set
	 */
	public void setBillingIdentifier(String billingIdentifier) {
		this.billingIdentifier = billingIdentifier;
	}

	/**
	 * @param isHotSale the isHotSale to set
	 */
	public void setHotSale(boolean isHotSale) {
		this.isHotSale = isHotSale;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ChargePojo o) {
		if ( o == null ) {
			return -1;
		} else {
			if ( this.price - o.price > 0 ) {
				return 1;
			} else if ( this.price - o.price < 0 ) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Clone a new instance
	 */
	@Override
	public ChargePojo clone() {
		ChargePojo pojo = new ChargePojo();
		pojo._id = this._id;
		pojo.currency = this.currency;
		pojo.discount = this.discount;
		pojo.isHotSale = this.isHotSale;
		pojo.price = this.price;
		pojo.yuanbao = this.yuanbao;
		return pojo;
	}
	/**
	 * Convert to Protocol Buffer's ChargeData
	 * @return
	 */
	public ChargeData toChargeData() {
		ChargeData.Builder builder = ChargeData.newBuilder();
		builder.setId(_id);
		builder.setPrice((int)(price*100));
		builder.setCurrency(currency.getCurrencySymbol());
		builder.setDiscount((int)(discount*10));
		builder.setIsHot(isHotSale);
		builder.setYuanbao(yuanbao);
		if ( StringUtil.checkNotEmpty(channel) ) {
			builder.setChannel(channel);
		}
		if ( StringUtil.checkNotEmpty(billingIdentifier) ) {
			builder.setBillingid(billingIdentifier);
		}
		return builder.build();
	}
	
	/**
	 * Output with discount
	 * The discount is from 0.0 to 9.9
	 * 
	 * @return
	 */
	public ChargeData toChargeData(float vipDiscount) {
		ChargeData.Builder builder = ChargeData.newBuilder();
		builder.setId(_id);
		builder.setPrice((int)(price*100));
		builder.setCurrency(currency.getCurrencySymbol());
		if ( discount <= 0.0 ) {
			builder.setDiscount((int)(10*vipDiscount));
		} else {
			builder.setDiscount((int)(discount*10*vipDiscount));
		}
		builder.setIsHot(isHotSale);
		builder.setYuanbao((int)(yuanbao*10/vipDiscount));
		if ( StringUtil.checkNotEmpty(channel) ) {
			builder.setChannel(channel);
		}
		if ( StringUtil.checkNotEmpty(billingIdentifier) ) {
			builder.setBillingid(billingIdentifier);
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format(
						"ChargePojo [_id=%s, price=%s, currency=%s, yuanbao=%s, discount=%s, isHotSale=%s, channel=%s, billingIdentifier=%s]",
						_id, price, currency, yuanbao, discount, isHotSale, channel,
						billingIdentifier);
	}

}


