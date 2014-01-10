package com.xinqihd.sns.gameserver.config.equip;

import com.xinqihd.sns.gameserver.util.Text;

public enum QualityType {

	NONE,
	//普通品质
	SIMPLE,
	//精良品质
	NORMAL,
	//稀有品质
	PRO,
	//传说品质
	LEGEND;
	
	private String title = null;
	QualityType() {
		title = Text.text("quality.".concat(toString()));
	}
	
	public String getTitle() {
		return title;
	}
}
