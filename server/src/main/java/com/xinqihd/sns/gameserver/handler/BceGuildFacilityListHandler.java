package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildFacilityList.BceGuildFacilityList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildFacilityListHandler is used for protocol GuildFacilityList 
 * @author wangqi
 *
 */
public class BceGuildFacilityListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildFacilityListHandler.class);
	
	private static final BceGuildFacilityListHandler instance = new BceGuildFacilityListHandler();
	
	private BceGuildFacilityListHandler() {
		super();
	}

	public static BceGuildFacilityListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildFacilityList");
		}
		XinqiMessage request = (XinqiMessage)message;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildFacilityList payload = (BceGuildFacilityList)request.payload;
		
		GuildManager.getInstance().listGuildFacility(user, user.getGuild());
	}
	
	
}
