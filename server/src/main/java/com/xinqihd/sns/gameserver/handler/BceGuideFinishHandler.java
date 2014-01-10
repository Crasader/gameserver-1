package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuideFinishHandler is used for protocol GuideFinish 
 * @author wangqi
 *
 */
public class BceGuideFinishHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuideFinishHandler.class);
	
	private static final BceGuideFinishHandler instance = new BceGuideFinishHandler();
	
	private BceGuideFinishHandler() {
		super();
	}

	public static BceGuideFinishHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuideFinish");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseGuideFinish not exists
		//XinqiBseGuideFinish.BseGuideFinish.Builder builder = XinqiBseGuideFinish.BseGuideFinish.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuideFinish.BceGuideFinish)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuideFinish: " + response);
	}
	
	
}
