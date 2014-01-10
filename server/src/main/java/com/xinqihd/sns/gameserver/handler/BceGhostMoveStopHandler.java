package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGhostMoveStop;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGhostMoveStopHandler is used for protocol GhostMoveStop 
 * @author wangqi
 *
 */
public class BceGhostMoveStopHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGhostMoveStopHandler.class);
	
	private static final BceGhostMoveStopHandler instance = new BceGhostMoveStopHandler();
	
	private BceGhostMoveStopHandler() {
		super();
	}

	public static BceGhostMoveStopHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGhostMoveStop");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGhostMoveStop.BseGhostMoveStop.Builder builder = XinqiBseGhostMoveStop.BseGhostMoveStop.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGhostMoveStop.BceGhostMoveStop)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGhostMoveStop: " + response);
	}
	
	
}
