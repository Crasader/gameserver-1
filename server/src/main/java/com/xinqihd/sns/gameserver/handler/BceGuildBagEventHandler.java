package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildBagEvent;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildBagEvent.BceGuildBagEvent;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBagEvent.BseGuildBagEvent;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceGuildBagEventHandler is used for protocol GuildBagEvent 
 * @author wangqi
 *
 */
public class BceGuildBagEventHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildBagEventHandler.class);
	
	private static final BceGuildBagEventHandler instance = new BceGuildBagEventHandler();
	
	private BceGuildBagEventHandler() {
		super();
	}

	public static BceGuildBagEventHandler getInstance() {
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
		BceGuildBagEvent proto = (BceGuildBagEvent)request.payload;
		
		Collection<GuildBagEvent> events = GuildManager.getInstance().queryGuildBagEvents(user.getGuildId());
		BseGuildBagEvent.Builder builder = BseGuildBagEvent.newBuilder();
		if ( events != null ) {
			for ( GuildBagEvent event : events ) {
				builder.addEvent(event.toGuildBagEvent());
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		StatClient.getIntance().sendDataToStatServer(user, StatAction.GuildBagEvent);
	}
	
	
}
