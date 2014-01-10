package com.xinqihd.sns.gameserver.handler;

import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildFacility;
import com.xinqihd.sns.gameserver.guild.GuildFacilityType;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityList;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildAbilityList.BceGuildAbilityList;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildFacilityList.BseGuildFacilityList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildAbilityListHandler is used for protocol GuildAbilityList 
 * @author wangqi
 *
 */
public class BceGuildAbilityListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildAbilityListHandler.class);
	
	private static final BceGuildAbilityListHandler instance = new BceGuildAbilityListHandler();
	
	private BceGuildAbilityListHandler() {
		super();
	}

	public static BceGuildAbilityListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildAbilityList");
		}
		XinqiMessage request = (XinqiMessage)message;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		
		BseGuildFacilityList.Builder builder = BseGuildFacilityList.newBuilder();
		//公会个人技能
		HashMap<GuildFacilityType, GuildFacility> facilities = user.getGuildMember().getFacilities(); 
		for ( GuildFacility facility : facilities.values() ) {
			XinqiBseGuildFacilityList.GuildFacility f = facility.toGuildFacility();
			if ( f != null ) {
				builder.addFacility(f);
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
	
	
}
