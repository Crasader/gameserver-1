package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildBuy;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildBuyHandler is used for protocol GuildBuy 
 * @author wangqi
 *
 */
public class BceGuildBuyHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildBuyHandler.class);
	
	private static final BceGuildBuyHandler instance = new BceGuildBuyHandler();
	
	private BceGuildBuyHandler() {
		super();
	}

	public static BceGuildBuyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildBuy");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildBuy.BseGuildBuy.Builder builder = XinqiBseGuildBuy.BseGuildBuy.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildBuy.BceGuildBuy)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildBuy: " + response);
	}
	
	
}
