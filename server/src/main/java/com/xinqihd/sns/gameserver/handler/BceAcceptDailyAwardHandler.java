package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceAcceptDailyAwardHandler is used for protocol AcceptDailyAward 
 * @author wangqi
 *
 */
public class BceAcceptDailyAwardHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceAcceptDailyAwardHandler.class);
	
	private static final BceAcceptDailyAwardHandler instance = new BceAcceptDailyAwardHandler();
	
	private BceAcceptDailyAwardHandler() {
		super();
	}

	public static BceAcceptDailyAwardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceAcceptDailyAward");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseAcceptDailyAward not exists
		//XinqiBseAcceptDailyAward.BseAcceptDailyAward.Builder builder = XinqiBseAcceptDailyAward.BseAcceptDailyAward.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceAcceptDailyAward.BceAcceptDailyAward)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceAcceptDailyAward: " + response);
	}
	
	
}
