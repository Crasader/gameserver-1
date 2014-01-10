package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMoveStop;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleMoveStopHandler is used for protocol RoleMoveStop 
 * @author wangqi
 *
 */
public class BceRoleMoveStopHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleMoveStopHandler.class);
	
	private static final BceRoleMoveStopHandler instance = new BceRoleMoveStopHandler();
	
	private BceRoleMoveStopHandler() {
		super();
	}

	public static BceRoleMoveStopHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleMoveStop");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseRoleMoveStop.BseRoleMoveStop.Builder builder = XinqiBseRoleMoveStop.BseRoleMoveStop.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceRoleMoveStop.BceRoleMoveStop)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceRoleMoveStop: " + response);
	}
	
	
}
