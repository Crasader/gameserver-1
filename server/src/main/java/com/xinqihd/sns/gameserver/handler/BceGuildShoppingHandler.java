package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceGuildShopping.BceGuildShopping;
import com.xinqihd.sns.gameserver.proto.XinqiBseGuildShopping.BseGuildShopping;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildShoppingHandler is used for protocol GuildShopping 
 * @author wangqi
 *
 */
public class BceGuildShoppingHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGuildShoppingHandler.class);
	
	private static final BceGuildShoppingHandler instance = new BceGuildShoppingHandler();
	
	private BceGuildShoppingHandler() {
		super();
	}

	public static BceGuildShoppingHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGuildShopping");
		}
		XinqiMessage request = (XinqiMessage)message;
		User user = (User)GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
		
		BceGuildShopping payload = (BceGuildShopping)request.payload;
		
		GuildManager.getInstance().getGuildShop(user, user.getGuild(), payload.getShoplevel());
	}
	
	
}
