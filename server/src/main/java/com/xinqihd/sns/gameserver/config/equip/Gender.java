package com.xinqihd.sns.gameserver.config.equip;

import com.xinqihd.sns.gameserver.util.Text;

public enum Gender {
  /**
   * 1: female
   * 2: male
   */
	NONE,
	FEMALE,
	MALE,
	ALL;
	
	private String title;
	Gender() {
		title = Text.text("gender.".concat(toString()));
	}
	
	public String getTitle() {
		return title;
	}
}
