package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildBagPut.BceGuildBagPut;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildBagEventHandler is used for protocol GuildBagEvent 
 * @author wangqi
 *
 */
public class BceGuildBagPutHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildBagPutHandler.class);
	
	private static final BceGuildBagPutHandler instance = new BceGuildBagPutHandler();
	
	private BceGuildBagPutHandler() {
		super();
	}

	public static BceGuildBagPutHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildBagEvent");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildBagPut proto = (BceGuildBagPut)request.payload;
		
		GuildManager.getInstance().movePropDataToGuildStorage(user, user.getGuild(), proto.getPew());
	}
	
	
}
