package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseChangeSeat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceChangeSeatHandler is used for protocol ChangeSeat 
 * @author wangqi
 *
 */
public class BceChangeSeatHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceChangeSeatHandler.class);
	
	private static final BceChangeSeatHandler instance = new BceChangeSeatHandler();
	
	private BceChangeSeatHandler() {
		super();
	}

	public static BceChangeSeatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceChangeSeat");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseChangeSeat.BseChangeSeat.Builder builder = XinqiBseChangeSeat.BseChangeSeat.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceChangeSeat.BceChangeSeat)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceChangeSeat: " + response);
	}
	
	
}
