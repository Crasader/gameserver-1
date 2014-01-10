package com.xinqihd.sns.gameserver.config;

/**
 * The money type for an user to buy good in shop.
 * @author wangqi
 *
 */
public enum MoneyType {

	/**
	 * 0: 金币
	 * 1: 礼券
	 * 2: 勋章,作为公会元宝
	 * 4: 元宝
	 */
	GOLDEN(0),
	VOUCHER(1),
	MEDAL(2),
	YUANBAO(4);
	
	private int type;
	
	MoneyType(int type) {
		this.type = type;
	}
	
	public int type() {
		return this.type;
	}
	
	public static final MoneyType fromType(int type) {
		MoneyType moneyType = null;
		switch (type) {
			case 0:
				moneyType = GOLDEN;
				break;
			case 1:
				moneyType = VOUCHER;
				break;
			case 2:
				moneyType = MEDAL;
				break;
			case 4:
				moneyType = YUANBAO;
				break;
		}
		return moneyType;
	}
}
