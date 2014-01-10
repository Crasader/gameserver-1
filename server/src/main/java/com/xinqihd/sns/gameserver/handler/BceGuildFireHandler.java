package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildFire.BceGuildFire;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildFireHandler is used for protocol GuildFire 
 * @author wangqi
 *
 */
public class BceGuildFireHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildFireHandler.class);
	
	private static final BceGuildFireHandler instance = new BceGuildFireHandler();
	
	private BceGuildFireHandler() {
		super();
	}

	public static BceGuildFireHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildFire");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildFire proto = (BceGuildFire)request.payload;
		
		GuildManager.getInstance().fireGuildMember(user, proto.getUserid());
	}
	
	
}
