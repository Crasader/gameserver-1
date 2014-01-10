package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseSplitProp;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceSplitPropHandler is used for protocol SplitProp 
 * @author wangqi
 *
 */
public class BceSplitPropHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceSplitPropHandler.class);
	
	private static final BceSplitPropHandler instance = new BceSplitPropHandler();
	
	private BceSplitPropHandler() {
		super();
	}

	public static BceSplitPropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceSplitProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseSplitProp.BseSplitProp.Builder builder = XinqiBseSplitProp.BseSplitProp.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceSplitProp.BceSplitProp)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceSplitProp: " + response);
	}
	
	
}
