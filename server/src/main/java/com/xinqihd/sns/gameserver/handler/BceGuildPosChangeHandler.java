package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildPosChange;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildPosChangeHandler is used for protocol GuildPosChange 
 * @author wangqi
 *
 */
public class BceGuildPosChangeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildPosChangeHandler.class);
	
	private static final BceGuildPosChangeHandler instance = new BceGuildPosChangeHandler();
	
	private BceGuildPosChangeHandler() {
		super();
	}

	public static BceGuildPosChangeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildPosChange");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildPosChange.BseGuildPosChange.Builder builder = XinqiBseGuildPosChange.BseGuildPosChange.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildPosChange.BceGuildPosChange)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildPosChange: " + response);
	}
	
	
}
