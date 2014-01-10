package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BcePRCEnterHandler is used for protocol PRCEnter 
 * @author wangqi
 *
 */
public class BcePRCEnterHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BcePRCEnterHandler.class);
	
	private static final BcePRCEnterHandler instance = new BcePRCEnterHandler();
	
	private BcePRCEnterHandler() {
		super();
	}

	public static BcePRCEnterHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BcePRCEnter");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BsePRCEnter not exists
		//XinqiBsePRCEnter.BsePRCEnter.Builder builder = XinqiBsePRCEnter.BsePRCEnter.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBcePRCEnter.BcePRCEnter)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BcePRCEnter: " + response);
	}
	
	
}
