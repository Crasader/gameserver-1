package com.xinqihd.sns.gameserver.guild;

import java.util.ArrayList;
import java.util.HashMap;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class GuildStorage {

	//The guildId
	private String _id = null;
	
	/**
	 * The userId to propData list mapping.
	 */
	private HashMap<UserId, ArrayList<PropData>> storage = 
			new HashMap<UserId, ArrayList<PropData>>();
}
