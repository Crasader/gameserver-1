package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceExitGuild.BceExitGuild;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceExitGuildHandler is used for protocol ExitGuild 
 * @author wangqi
 *
 */
public class BceExitGuildHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceExitGuildHandler.class);
	
	private static final BceExitGuildHandler instance = new BceExitGuildHandler();

	private BceExitGuildHandler() {
		super();
	}

	public static BceExitGuildHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceExitGuild");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = (User)GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BceExitGuild payload = (BceExitGuild)request.payload;

		GuildManager.getInstance().quitGuild(user, payload.getGuildID());
	}
	
	
}
