package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseEnterHall;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceEnterHallHandler is used for protocol EnterHall 
 * @author wangqi
 *
 */
public class BceEnterHallHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceEnterHallHandler.class);
	
	private static final BceEnterHallHandler instance = new BceEnterHallHandler();
	
	private BceEnterHallHandler() {
		super();
	}

	public static BceEnterHallHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceEnterHall");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseEnterHall.BseEnterHall.Builder builder = XinqiBseEnterHall.BseEnterHall.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceEnterHall.BceEnterHall)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceEnterHall: " + response);
	}
	
	
}
