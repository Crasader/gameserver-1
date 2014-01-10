package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceChgShootModeHandler is used for protocol ChgShootMode 
 * @author wangqi
 *
 */
public class BceChgShootModeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceChgShootModeHandler.class);
	
	private static final BceChgShootModeHandler instance = new BceChgShootModeHandler();
	
	private BceChgShootModeHandler() {
		super();
	}

	public static BceChgShootModeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceChgShootMode");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseChgShootMode not exists
		//XinqiBseChgShootMode.BseChgShootMode.Builder builder = XinqiBseChgShootMode.BseChgShootMode.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceChgShootMode.BceChgShootMode)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceChgShootMode: " + response);
	}
	
	
}
