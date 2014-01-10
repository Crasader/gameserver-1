package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildLimit;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildLimitHandler is used for protocol GuildLimit 
 * @author wangqi
 *
 */
public class BceGuildLimitHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildLimitHandler.class);
	
	private static final BceGuildLimitHandler instance = new BceGuildLimitHandler();
	
	private BceGuildLimitHandler() {
		super();
	}

	public static BceGuildLimitHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildLimit");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildLimit.BseGuildLimit.Builder builder = XinqiBseGuildLimit.BseGuildLimit.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildLimit.BceGuildLimit)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildLimit: " + response);
	}
	
	
}
