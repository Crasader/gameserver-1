package com.xinqihd.sns.gameserver.util;

import java.util.Locale;

public class LocaleThreadLocal extends ThreadLocal<Locale> {
	
	private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

	/* (non-Javadoc)
	 * @see java.lang.ThreadLocal#initialValue()
	 */
	@Override
	protected Locale initialValue() {
		return defaultLocale;
	}

}
