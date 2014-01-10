package com.xinqihd.sns.gameserver.util;

import java.util.Locale;
import java.util.ResourceBundle;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.entity.user.PropData;

public class Text {
	
	public static final ResourceBundle CHINESE = ResourceBundle.getBundle("i18n.GameResource");
	public static final ResourceBundle TAIWAN = ResourceBundle.getBundle("i18n.GameResource_zh_TW");

	/**
	 * Get a simple internationalization string.
	 * @param key
	 * @return
	 */
	public static final String text(String key) {
		return text(key, CHINESE);
	}
	
	/**
	 * Get a simple internationalization string.
	 * @param key
	 * @param bundle
	 * @return
	 */
	public static final String text(String key, ResourceBundle bundle) {
		String text =  bundle.getString(key);
		return text;
	}
	
	/**
	 * Get a composed i18n string with given arguments.
	 * @param key
	 * @param argument
	 * @return
	 */
	public static final String text(String key, Object argument) {
		Locale userLocale = Locale.SIMPLIFIED_CHINESE;
		if ( Constant.I18N_ENABLE ) {
			LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
			userLocale = threadLocal.get();
			if ( userLocale == null ) {
				userLocale = Locale.SIMPLIFIED_CHINESE;
				//threadLocal.set(userLocale);
			}
		}
		ResourceBundle bundle = CHINESE;
		if ( Locale.TRADITIONAL_CHINESE.equals(userLocale) ) {
			bundle = TAIWAN;
		}
		return text(key, bundle, new Object[]{argument});
	}
	
	/**
	 * Generate a desc for the given propData
	 * @param propData
	 * @return
	 */
	public static final String text(PropData propData) {
		WeaponColor color = propData.getWeaponColor();
		String colorDesc = "#FFFFFF";
		if ( color != WeaponColor.WHITE ) {
			colorDesc = color.toColorString(); 
		}
		int level = propData.getLevel();
		String name = propData.getName();
		String levelDesc = "[";
		if ( level > 0 ) {
			levelDesc = StringUtil.concat("[+", level, " ");
		}
		String desc = StringUtil.concat(colorDesc, levelDesc, name, "]", "#FFFFFF");
		return desc;
	}
	
	/**
	 * Get a composed i18n string with given arguments.
	 * @param key
	 * @param argument
	 * @return
	 */
	public static final String text(String key, Object arg1, Object arg2) {
		Locale userLocale = Locale.SIMPLIFIED_CHINESE;
		if ( Constant.I18N_ENABLE ) {
			LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
			userLocale = threadLocal.get();
			if ( userLocale == null ) {
				userLocale = Locale.SIMPLIFIED_CHINESE;
				//threadLocal.set(userLocale);
			}
		}
		ResourceBundle bundle = CHINESE;
		if ( Locale.TRADITIONAL_CHINESE.equals(userLocale) ) {
			bundle = TAIWAN;
		}
		return text(key, bundle, new Object[]{arg1, arg2});
	}
	
	/**
	 * Get a composed i18n string with given arguments.
	 * @param key
	 * @param argument
	 * @return
	 */
	public static final String text(String key, Object ... argument) {
		Locale userLocale = Locale.SIMPLIFIED_CHINESE;
		if ( Constant.I18N_ENABLE ) {
			LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
			userLocale = threadLocal.get();
			if ( userLocale == null ) {
				userLocale = Locale.SIMPLIFIED_CHINESE;
				//threadLocal.set(userLocale);
			}
		}
		ResourceBundle bundle = CHINESE;
		if ( Locale.TRADITIONAL_CHINESE.equals(userLocale) ) {
			bundle = TAIWAN;
		}
		return text(key, bundle, argument);
	}
	
	/**
	 * Get a composed i18n string with given arguments.
	 * @param key
	 * @param argument
	 * @return
	 */
	public static final String text(String key, ResourceBundle bundle, 
			Object ... argument) {
				
		String text = bundle.getString(key);
		FormattingTuple tuple = MessageFormatter.arrayFormat(text, argument);
		return tuple.getMessage();
	}
}
