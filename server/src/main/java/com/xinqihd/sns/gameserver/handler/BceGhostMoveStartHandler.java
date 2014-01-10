package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGhostMoveStart;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGhostMoveStartHandler is used for protocol GhostMoveStart 
 * @author wangqi
 *
 */
public class BceGhostMoveStartHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGhostMoveStartHandler.class);
	
	private static final BceGhostMoveStartHandler instance = new BceGhostMoveStartHandler();
	
	private BceGhostMoveStartHandler() {
		super();
	}

	public static BceGhostMoveStartHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGhostMoveStart");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGhostMoveStart.BseGhostMoveStart.Builder builder = XinqiBseGhostMoveStart.BseGhostMoveStart.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGhostMoveStart.BceGhostMoveStart)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGhostMoveStart: " + response);
	}
	
	
}
