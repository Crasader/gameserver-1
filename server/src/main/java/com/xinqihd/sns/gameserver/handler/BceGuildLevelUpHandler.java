package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildLevelUp;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildLevelUpHandler is used for protocol GuildLevelUp 
 * @author wangqi
 *
 */
public class BceGuildLevelUpHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildLevelUpHandler.class);
	
	private static final BceGuildLevelUpHandler instance = new BceGuildLevelUpHandler();
	
	private BceGuildLevelUpHandler() {
		super();
	}

	public static BceGuildLevelUpHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildLevelUp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildLevelUp.BseGuildLevelUp.Builder builder = XinqiBseGuildLevelUp.BseGuildLevelUp.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildLevelUp.BceGuildLevelUp)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildLevelUp: " + response);
	}
	
	
}
