package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildShopLevelup;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildShopLevelupHandler is used for protocol GuildShopLevelup 
 * @author wangqi
 *
 */
public class BceGuildShopLevelupHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildShopLevelupHandler.class);
	
	private static final BceGuildShopLevelupHandler instance = new BceGuildShopLevelupHandler();
	
	private BceGuildShopLevelupHandler() {
		super();
	}

	public static BceGuildShopLevelupHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildShopLevelup");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildShopLevelup.BseGuildShopLevelup.Builder builder = XinqiBseGuildShopLevelup.BseGuildShopLevelup.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildShopLevelup.BceGuildShopLevelup)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildShopLevelup: " + response);
	}
	
	
}
