package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;

/**
 * The BceLoadProgressHandler is used for protocol LoadProgress 
 * @author wangqi
 *
 */
public class BceLoadProgressHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceLoadProgressHandler.class);
	
	private static final BceLoadProgressHandler instance = new BceLoadProgressHandler();
	
	private BceLoadProgressHandler() {
		super();
	}

	public static BceLoadProgressHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceLoadProgress");
		}
		
		//It do nothing.
		
//		XinqiMessage request = (XinqiMessage)message;
//		XinqiMessage response = new XinqiMessage();
		
		//XinqiBseLoadProgress.BseLoadProgress.Builder builder = XinqiBseLoadProgress.BseLoadProgress.newBuilder();
		
		// response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceLoadProgress.BceLoadProgress)request.payload).getUid());
		//END
		// session.write(response);
	}
	
	
}
