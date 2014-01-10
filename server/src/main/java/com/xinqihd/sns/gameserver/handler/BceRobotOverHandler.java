package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRobotOverHandler is used for protocol RobotOver 
 * @author wangqi
 *
 */
public class BceRobotOverHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRobotOverHandler.class);
	
	private static final BceRobotOverHandler instance = new BceRobotOverHandler();
	
	private BceRobotOverHandler() {
		super();
	}

	public static BceRobotOverHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRobotOver");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseRobotOver not exists
		//XinqiBseRobotOver.BseRobotOver.Builder builder = XinqiBseRobotOver.BseRobotOver.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceRobotOver.BceRobotOver)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceRobotOver: " + response);
	}
	
	
}
