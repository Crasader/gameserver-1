package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildInviteRspHandler is used for protocol GuildInviteRsp 
 * @author wangqi
 *
 */
public class BceGuildInviteRspHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildInviteRspHandler.class);
	
	private static final BceGuildInviteRspHandler instance = new BceGuildInviteRspHandler();
	
	private BceGuildInviteRspHandler() {
		super();
	}

	public static BceGuildInviteRspHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildInviteRsp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseGuildInviteRsp not exists
		//XinqiBseGuildInviteRsp.BseGuildInviteRsp.Builder builder = XinqiBseGuildInviteRsp.BseGuildInviteRsp.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildInviteRsp.BceGuildInviteRsp)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildInviteRsp: " + response);
	}
	
	
}
