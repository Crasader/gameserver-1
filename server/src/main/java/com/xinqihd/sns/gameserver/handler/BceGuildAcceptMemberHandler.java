package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildAcceptMember;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildAcceptMemberHandler is used for protocol GuildAcceptMember 
 * @author wangqi
 *
 */
public class BceGuildAcceptMemberHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildAcceptMemberHandler.class);
	
	private static final BceGuildAcceptMemberHandler instance = new BceGuildAcceptMemberHandler();
	
	private BceGuildAcceptMemberHandler() {
		super();
	}

	public static BceGuildAcceptMemberHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildAcceptMember");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildAcceptMember.BseGuildAcceptMember.Builder builder = XinqiBseGuildAcceptMember.BseGuildAcceptMember.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildAcceptMember.BceGuildAcceptMember)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildAcceptMember: " + response);
	}
	
	
}
