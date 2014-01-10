package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildContribute.BceGuildContribute;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildContributeHandler is used for protocol GuildContribute 
 * @author wangqi
 *
 */
public class BceGuildContributeHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildContributeHandler.class);
	
	private static final BceGuildContributeHandler instance = new BceGuildContributeHandler();
	
	private BceGuildContributeHandler() {
		super();
	}

	public static BceGuildContributeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildContribute");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildContribute payload = (BceGuildContribute)request.payload;
		
		GuildManager.getInstance().contributeToGuild(user, payload.getYuanbao(), payload.getGolden());
	}
	
}
