package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseMergeProp;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceMergePropHandler is used for protocol MergeProp 
 * @author wangqi
 *
 */
public class BceMergePropHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceMergePropHandler.class);
	
	private static final BceMergePropHandler instance = new BceMergePropHandler();
	
	private BceMergePropHandler() {
		super();
	}

	public static BceMergePropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceMergeProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseMergeProp.BseMergeProp.Builder builder = XinqiBseMergeProp.BseMergeProp.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceMergeProp.BceMergeProp)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceMergeProp: " + response);
	}
	
	
}
