package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGhostMove;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGhostMoveHandler is used for protocol GhostMove 
 * @author wangqi
 *
 */
public class BceGhostMoveHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGhostMoveHandler.class);
	
	private static final BceGhostMoveHandler instance = new BceGhostMoveHandler();
	
	private BceGhostMoveHandler() {
		super();
	}

	public static BceGhostMoveHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGhostMove");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGhostMove.BseGhostMove.Builder builder = XinqiBseGhostMove.BseGhostMove.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGhostMove.BceGhostMove)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGhostMove: " + response);
	}
	
	
}
