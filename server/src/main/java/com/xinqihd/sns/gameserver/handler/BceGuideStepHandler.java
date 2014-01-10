package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuideStepHandler is used for protocol GuideStep 
 * @author wangqi
 *
 */
public class BceGuideStepHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuideStepHandler.class);
	
	private static final BceGuideStepHandler instance = new BceGuideStepHandler();
	
	private BceGuideStepHandler() {
		super();
	}

	public static BceGuideStepHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuideStep");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseGuideStep not exists
		//XinqiBseGuideStep.BseGuideStep.Builder builder = XinqiBseGuideStep.BseGuideStep.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuideStep.BceGuideStep)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuideStep: " + response);
	}
	
	
}
