package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceDoubleExpOverHandler is used for protocol DoubleExpOver 
 * @author wangqi
 *
 */
public class BceDoubleExpOverHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceDoubleExpOverHandler.class);
	
	private static final BceDoubleExpOverHandler instance = new BceDoubleExpOverHandler();
	
	private BceDoubleExpOverHandler() {
		super();
	}

	public static BceDoubleExpOverHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceDoubleExpOver");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseDoubleExpOver not exists
		//XinqiBseDoubleExpOver.BseDoubleExpOver.Builder builder = XinqiBseDoubleExpOver.BseDoubleExpOver.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceDoubleExpOver.BceDoubleExpOver)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceDoubleExpOver: " + response);
	}
	
	
}
