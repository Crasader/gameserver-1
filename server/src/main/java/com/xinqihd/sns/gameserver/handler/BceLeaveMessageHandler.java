package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseLeaveMessage;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceLeaveMessageHandler is used for protocol LeaveMessage 
 * @author wangqi
 *
 */
public class BceLeaveMessageHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceLeaveMessageHandler.class);
	
	private static final BceLeaveMessageHandler instance = new BceLeaveMessageHandler();
	
	private BceLeaveMessageHandler() {
		super();
	}

	public static BceLeaveMessageHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceLeaveMessage");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseLeaveMessage.BseLeaveMessage.Builder builder = XinqiBseLeaveMessage.BseLeaveMessage.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceLeaveMessage.BceLeaveMessage)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceLeaveMessage: " + response);
	}
	
	
}
