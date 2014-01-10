package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceUserGuidAtkHandler is used for protocol UserGuidAtk 
 * @author wangqi
 *
 */
public class BceUserGuidAtkHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceUserGuidAtkHandler.class);
	
	private static final BceUserGuidAtkHandler instance = new BceUserGuidAtkHandler();
	
	private BceUserGuidAtkHandler() {
		super();
	}

	public static BceUserGuidAtkHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceUserGuidAtk");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseUserGuidAtk not exists
		//XinqiBseUserGuidAtk.BseUserGuidAtk.Builder builder = XinqiBseUserGuidAtk.BseUserGuidAtk.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceUserGuidAtk.BceUserGuidAtk)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceUserGuidAtk: " + response);
	}
	
	
}
