package com.xinqihd.sns.gameserver.config;

/**
 * The charge price unit.
 * @author wangqi
 *
 */
public enum CurrencyUnit {

	CMCC_YUAN("￥"),
	CHINESE_YUAN("￥"),
	US_DOLLOR("$"),
	OPPO("");
	
	private String currencySymbol = null;
	
	CurrencyUnit(String currencyCharacter) {
		this.currencySymbol = currencyCharacter;
	}
	
	/**
	 * Get the currency simbol
	 * @return
	 */
	public String getCurrencySymbol() {
		return this.currencySymbol;
	}
}
