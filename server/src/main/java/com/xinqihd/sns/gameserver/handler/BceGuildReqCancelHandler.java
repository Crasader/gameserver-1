package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildReqCancel;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildReqCancelHandler is used for protocol GuildReqCancel 
 * @author wangqi
 *
 */
public class BceGuildReqCancelHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildReqCancelHandler.class);
	
	private static final BceGuildReqCancelHandler instance = new BceGuildReqCancelHandler();
	
	private BceGuildReqCancelHandler() {
		super();
	}

	public static BceGuildReqCancelHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildReqCancel");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildReqCancel.BseGuildReqCancel.Builder builder = XinqiBseGuildReqCancel.BseGuildReqCancel.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildReqCancel.BceGuildReqCancel)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildReqCancel: " + response);
	}
	
	
}
