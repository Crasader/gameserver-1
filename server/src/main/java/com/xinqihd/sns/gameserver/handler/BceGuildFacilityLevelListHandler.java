package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildFacilityLevelList.BceGuildFacilityLevelList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildFacilityLevelListHandler is used for protocol GuildFacilityLevelList 
 * @author wangqi
 *
 */
public class BceGuildFacilityLevelListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildFacilityLevelListHandler.class);
	
	private static final BceGuildFacilityLevelListHandler instance = new BceGuildFacilityLevelListHandler();
	
	private BceGuildFacilityLevelListHandler() {
		super();
	}

	public static BceGuildFacilityLevelListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildFacilityLevelList");
		}
		XinqiMessage request = (XinqiMessage)message;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildFacilityLevelList payload = (BceGuildFacilityLevelList)request.payload;
		
		GuildManager.getInstance().listGuildFacilityLevelList(user, user.getGuild());
	}
	
	
}
