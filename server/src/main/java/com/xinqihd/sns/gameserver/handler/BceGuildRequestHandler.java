package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildRequest;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildRequestHandler is used for protocol GuildRequest 
 * @author wangqi
 *
 */
public class BceGuildRequestHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildRequestHandler.class);
	
	private static final BceGuildRequestHandler instance = new BceGuildRequestHandler();
	
	private BceGuildRequestHandler() {
		super();
	}

	public static BceGuildRequestHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildRequest");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildRequest.BseGuildRequest.Builder builder = XinqiBseGuildRequest.BseGuildRequest.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildRequest.BceGuildRequest)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildRequest: " + response);
	}
	
	
}
