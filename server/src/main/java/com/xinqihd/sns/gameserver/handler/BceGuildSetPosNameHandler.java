package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildSetPosName;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildSetPosNameHandler is used for protocol GuildSetPosName 
 * @author wangqi
 *
 */
public class BceGuildSetPosNameHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildSetPosNameHandler.class);
	
	private static final BceGuildSetPosNameHandler instance = new BceGuildSetPosNameHandler();
	
	private BceGuildSetPosNameHandler() {
		super();
	}

	public static BceGuildSetPosNameHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildSetPosName");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildSetPosName.BseGuildSetPosName.Builder builder = XinqiBseGuildSetPosName.BseGuildSetPosName.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildSetPosName.BceGuildSetPosName)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildSetPosName: " + response);
	}
	
	
}
