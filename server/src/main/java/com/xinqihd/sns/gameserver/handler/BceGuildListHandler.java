package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildList.BceGuildList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildListHandler is used for protocol GuildList 
 * @author wangqi
 *
 */
public class BceGuildListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildListHandler.class);
	
	private static final BceGuildListHandler instance = new BceGuildListHandler();
	
	private BceGuildListHandler() {
		super();
	}

	public static BceGuildListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildList");
		}
		XinqiMessage request = (XinqiMessage)message;
		BceGuildList guildList = (BceGuildList)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		int startPos = guildList.getStartpos();
		int count = guildList.getCount();
		
		GuildManager.getInstance().listGuilds(user, startPos, count);
	}
	
	
}
