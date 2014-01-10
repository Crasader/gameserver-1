package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceDebugHandler is used for protocol Debug 
 * @author wangqi
 *
 */
public class BceDebugHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceDebugHandler.class);
	
	private static final BceDebugHandler instance = new BceDebugHandler();
	
	private BceDebugHandler() {
		super();
	}

	public static BceDebugHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceDebug");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseDebug not exists
		//XinqiBseDebug.BseDebug.Builder builder = XinqiBseDebug.BseDebug.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceDebug.BceDebug)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceDebug: " + response);
	}
	
	
}
