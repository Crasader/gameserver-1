package com.xinqihd.sns.gameserver.config.equip;

import java.awt.Color;

import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 1: simple weapon
 * 2: normal weapon
 * 3: advanced weapon
 * 4: powerup
 * 
 * {@link com.xinqihd.sns.gameserver.config.equip.WeaponPojo#quality}
 * 
 * @author wangqi
 *
 */
public enum WeaponColor {

	WHITE(Color.WHITE),
  // 1: simple weapon
	GREEN(Color.GREEN),
	// 2: normal weapon
	BLUE(Color.BLUE),
	// 3: advanced weapon
	PINK(Color.PINK),
	// 4: powerup
	ORGANCE(Color.ORANGE),
	//PURPLE(new Color(230, 230, 250));
	PURPLE(new Color(140, 48, 205));
	
	private Color color = Color.WHITE;
	private String colorStr = null;
	private int id = 0;
	private String title = null;
	
	private WeaponColor(Color color) {
		this.color = color;
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
		this.colorStr = StringUtil.concat("#", r, g, b);
		this.title = Text.text(toString());
	}
	
	public int toIntColor() {
		return this.color.getRGB();
	}
	
	public String toColorString() {
		return this.colorStr;
	}

	public static WeaponColor from(String obj) {
		if ( obj == null ) {
			return WHITE;
		} else {
			return WeaponColor.valueOf(obj);
		}
	}
	
	public static WeaponColor fromIndex(int index) {
		if ( index >= 0 && index < WeaponColor.values().length ) {
			return WeaponColor.values()[index];
		}
		return WHITE;
	}
	
	public String getTitle() {
		return title;
	}
	
}
