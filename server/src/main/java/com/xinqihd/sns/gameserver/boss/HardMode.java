package com.xinqihd.sns.gameserver.boss;

import com.xinqihd.sns.gameserver.util.Text;

/**
 * The boss's hardmode
 * 
 * @author wangqi
 * 
 */
public enum HardMode {

	simple,
	normal,
	hard,
	veryhard;
	
	private String title = null;
	
	HardMode() {
		title = Text.text("boss.".concat(toString()));
	}
	
	public String getTitle() {
		return this.title;
	}
}
