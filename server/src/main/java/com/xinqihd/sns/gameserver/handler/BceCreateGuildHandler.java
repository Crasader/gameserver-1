package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceCreateGuild.BceCreateGuild;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceCreateGuildHandler is used for protocol CreateGuild 
 * @author wangqi
 *
 */
public class BceCreateGuildHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCreateGuildHandler.class);
	
	private static final BceCreateGuildHandler instance = new BceCreateGuildHandler();
	
	private BceCreateGuildHandler() {
		super();
	}

	public static BceCreateGuildHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCreateGuild");
		}
		XinqiMessage request = (XinqiMessage)message;
		BceCreateGuild createGuild = (BceCreateGuild)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		String guildName = createGuild.getGuildName();
		String announcement = createGuild.getAnnouncements();
		
		GuildManager.getInstance().createGuild(user, guildName, announcement);
	}
	
}
