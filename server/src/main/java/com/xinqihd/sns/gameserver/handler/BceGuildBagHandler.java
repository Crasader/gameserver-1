package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildBag.BceGuildBag;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildBagHandler is used for protocol GuildBag 
 * @author wangqi
 *
 */
public class BceGuildBagHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildBagHandler.class);
	
	private static final BceGuildBagHandler instance = new BceGuildBagHandler();
	
	private BceGuildBagHandler() {
		super();
	}

	public static BceGuildBagHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildBag");
		}
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceGuildBag guildBag = (BceGuildBag)request.payload;
		
		GuildManager.getInstance().listGuildStorage(user, user.getGuildId());
	}
	
	
}
