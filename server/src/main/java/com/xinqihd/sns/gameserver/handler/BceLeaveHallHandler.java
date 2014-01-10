package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseLeaveHall;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceLeaveHallHandler is used for protocol LeaveHall 
 * @author wangqi
 *
 */
public class BceLeaveHallHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceLeaveHallHandler.class);
	
	private static final BceLeaveHallHandler instance = new BceLeaveHallHandler();
	
	private BceLeaveHallHandler() {
		super();
	}

	public static BceLeaveHallHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceLeaveHall");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseLeaveHall.BseLeaveHall.Builder builder = XinqiBseLeaveHall.BseLeaveHall.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceLeaveHall.BceLeaveHall)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceLeaveHall: " + response);
	}
	
	
}
