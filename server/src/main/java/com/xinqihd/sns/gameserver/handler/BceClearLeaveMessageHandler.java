package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseClearLeaveMessage;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceClearLeaveMessageHandler is used for protocol ClearLeaveMessage 
 * @author wangqi
 *
 */
public class BceClearLeaveMessageHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceClearLeaveMessageHandler.class);
	
	private static final BceClearLeaveMessageHandler instance = new BceClearLeaveMessageHandler();
	
	private BceClearLeaveMessageHandler() {
		super();
	}

	public static BceClearLeaveMessageHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceClearLeaveMessage");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseClearLeaveMessage.BseClearLeaveMessage.Builder builder = XinqiBseClearLeaveMessage.BseClearLeaveMessage.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceClearLeaveMessage.BceClearLeaveMessage)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceClearLeaveMessage: " + response);
	}
	
	
}
