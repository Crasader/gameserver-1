package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildDismiss;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildDismissHandler is used for protocol GuildDismiss 
 * @author wangqi
 *
 */
public class BceGuildDismissHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildDismissHandler.class);
	
	private static final BceGuildDismissHandler instance = new BceGuildDismissHandler();
	
	private BceGuildDismissHandler() {
		super();
	}

	public static BceGuildDismissHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildDismiss");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildDismiss.BseGuildDismiss.Builder builder = XinqiBseGuildDismiss.BseGuildDismiss.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildDismiss.BceGuildDismiss)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildDismiss: " + response);
	}
	
	
}
