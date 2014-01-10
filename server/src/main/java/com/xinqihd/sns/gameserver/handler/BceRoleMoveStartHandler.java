package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMoveStart;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleMoveStartHandler is used for protocol RoleMoveStart 
 * @author wangqi
 *
 */
public class BceRoleMoveStartHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleMoveStartHandler.class);
	
	private static final BceRoleMoveStartHandler instance = new BceRoleMoveStartHandler();
	
	private BceRoleMoveStartHandler() {
		super();
	}

	public static BceRoleMoveStartHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleMoveStart");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseRoleMoveStart.BseRoleMoveStart.Builder builder = XinqiBseRoleMoveStart.BseRoleMoveStart.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceRoleMoveStart.BceRoleMoveStart)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceRoleMoveStart: " + response);
	}
	
	
}
