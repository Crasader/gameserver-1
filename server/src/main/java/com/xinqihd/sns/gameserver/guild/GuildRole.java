package com.xinqihd.sns.gameserver.guild;

import com.xinqihd.sns.gameserver.util.Text;

/**
 * 职位
 * chief: 会长
 * director: 副会长 
 * manager: 官员
 * elite: 精英 
 * member:  会员
 * 
 * @author wangqi
 *
 */
public enum GuildRole {

	chief,
	director,
	manager,
	elite,
	member;
	
	private String text = null;
	
	GuildRole() {
		text = Text.text(this.toString());
	}
	
	public String getTitle() {
		return text;
	}
	
}
