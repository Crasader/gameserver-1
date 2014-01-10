package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseSetGuildAnno;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceSetGuildAnnoHandler is used for protocol SetGuildAnno 
 * @author wangqi
 *
 */
public class BceSetGuildAnnoHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceSetGuildAnnoHandler.class);
	
	private static final BceSetGuildAnnoHandler instance = new BceSetGuildAnnoHandler();
	
	private BceSetGuildAnnoHandler() {
		super();
	}

	public static BceSetGuildAnnoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceSetGuildAnno");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseSetGuildAnno.BseSetGuildAnno.Builder builder = XinqiBseSetGuildAnno.BseSetGuildAnno.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceSetGuildAnno.BceSetGuildAnno)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceSetGuildAnno: " + response);
	}
	
	
}
