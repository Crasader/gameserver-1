package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildBagTake.BceGuildBagTake;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildBagEventHandler is used for protocol GuildBagEvent 
 * @author wangqi
 *
 */
public class BceGuildBagTakeHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildBagTakeHandler.class);
	
	private static final BceGuildBagTakeHandler instance = new BceGuildBagTakeHandler();
	
	private BceGuildBagTakeHandler() {
		super();
	}

	public static BceGuildBagTakeHandler getInstance() {
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
		BceGuildBagTake proto = (BceGuildBagTake)request.payload;

		GuildManager.getInstance().moveGuildStorageToBag(user, user.getGuild(), proto.getIndex());
	}
	
	
}
