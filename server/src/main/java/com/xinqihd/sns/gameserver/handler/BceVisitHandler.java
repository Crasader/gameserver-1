package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseVisit;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceVisitHandler is used for protocol Visit 
 * @author wangqi
 *
 */
public class BceVisitHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceVisitHandler.class);
	
	private static final BceVisitHandler instance = new BceVisitHandler();
	
	private BceVisitHandler() {
		super();
	}

	public static BceVisitHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceVisit");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseVisit.BseVisit.Builder builder = XinqiBseVisit.BseVisit.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceVisit.BceVisit)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceVisit: " + response);
	}
	
	
}
