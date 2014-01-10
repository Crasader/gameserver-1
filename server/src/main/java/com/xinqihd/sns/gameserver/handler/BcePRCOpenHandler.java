package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBsePRCOpen;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BcePRCOpenHandler is used for protocol PRCOpen 
 * @author wangqi
 *
 */
public class BcePRCOpenHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BcePRCOpenHandler.class);
	
	private static final BcePRCOpenHandler instance = new BcePRCOpenHandler();
	
	private BcePRCOpenHandler() {
		super();
	}

	public static BcePRCOpenHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BcePRCOpen");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBsePRCOpen.BsePRCOpen.Builder builder = XinqiBsePRCOpen.BsePRCOpen.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBcePRCOpen.BcePRCOpen)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BcePRCOpen: " + response);
	}
	
	
}
