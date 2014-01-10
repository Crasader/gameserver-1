package com.xinqihd.sns.gameserver.config.equip;

import java.awt.Color;

import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The decrition color for text in PropData
 * @author wangqi
 *
 */
public enum TextColor {

	WHITE(Color.WHITE),
	GREEN(Color.GREEN),
	CYAN(Color.CYAN),
	BLUE(Color.BLUE),
	LIGHT_BLUE(new Color(0, 153, 204)),
	RED(Color.RED),
	YELLOW(Color.YELLOW),
	ORANGE(Color.ORANGE),
	GRAY(new Color(100, 100, 100)),
	PURPLE(new Color(140, 48, 205));
	
	private String colorPrefix = null;
	
	TextColor(Color color) {
		String r = Integer.toHexString(color.getRed());
		if ( r.length() == 1 ) {
			r = "0".concat(r);
		}
		String g = Integer.toHexString(color.getGreen());
		if ( g.length() == 1 ) {
			g = "0".concat(g);
		}
		String b = Integer.toHexString(color.getBlue());
		if ( b.length() == 1 ) {
			b = "0".concat(b);
		}
		this.colorPrefix = StringUtil.concat("#", r, g, b);
	}
	
	public String getColorStr() {
		return this.colorPrefix;
	}
	
	public String makeColor(String text) {
		return StringUtil.concat(colorPrefix, text);
	}
}
