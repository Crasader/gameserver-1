package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildContributeQuery.BceGuildContributeQuery;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildContributeQueryHandler is used for protocol GuildContributeQuery 
 * @author wangqi
 *
 */
public class BceGuildContributeQueryHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildContributeQueryHandler.class);
	
	private static final BceGuildContributeQueryHandler instance = new BceGuildContributeQueryHandler();
	
	private BceGuildContributeQueryHandler() {
		super();
	}

	public static BceGuildContributeQueryHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildContributeQuery");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildContributeQuery payload = (BceGuildContributeQuery)request.payload;
		
		GuildManager.getInstance().queryContribute(user, payload.getYuanbao(), payload.getGolden());
	}
	
}
