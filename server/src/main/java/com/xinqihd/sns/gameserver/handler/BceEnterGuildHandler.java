package com.xinqihd.sns.gameserver.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.*;
import com.xinqihd.sns.gameserver.proto.XinqiBceEnterGuild.BceEnterGuild;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildBagPut.BceGuildBagPut;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.*;

/**
 * The BceEnterGuildHandler is used for protocol EnterGuild 
 * @author wangqi
 *
 */
public class BceEnterGuildHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceEnterGuildHandler.class);
	
	private static final BceEnterGuildHandler instance = new BceEnterGuildHandler();
	
	private BceEnterGuildHandler() {
		super();
	}

	public static BceEnterGuildHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceEnterGuild");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceEnterGuild proto = (BceEnterGuild)request.payload;
		
		String guildId = proto.getGuildID();
		GuildManager.getInstance().enterGuild(user, guildId);
	}
	
	
}
