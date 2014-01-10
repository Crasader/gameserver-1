package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildSearch.BceGuildSearch;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildSearch.BseGuildSearch;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildSearchHandler is used for protocol GuildSearch 
 * @author wangqi
 *
 */
public class BceGuildSearchHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildSearchHandler.class);
	
	private static final BceGuildSearchHandler instance = new BceGuildSearchHandler();
	
	private BceGuildSearchHandler() {
		super();
	}

	public static BceGuildSearchHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildSearch");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		BceGuildSearch payload = (BceGuildSearch)request.payload;
		
		Collection<Guild> guilds = GuildManager.getInstance().searchGuild(payload.getKey(), 0, 20);
		BseGuildSearch.Builder builder = BseGuildSearch.newBuilder(); 
		if ( guilds != null ) {
			for ( Guild guild : guilds ) {
				builder.addGuildList(guild.toGuildSimpleInfo(guild.getRank()));
			}
		}
		GameContext.getInstance().writeResponse(userSessionKey, builder.build());
	}
	
	
}
