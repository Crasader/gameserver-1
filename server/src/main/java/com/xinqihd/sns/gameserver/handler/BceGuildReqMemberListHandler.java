package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseGuildReqMemberList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceGuildReqMemberListHandler is used for protocol GuildReqMemberList 
 * @author wangqi
 *
 */
public class BceGuildReqMemberListHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceGuildReqMemberListHandler.class);
	
	private static final BceGuildReqMemberListHandler instance = new BceGuildReqMemberListHandler();
	
	private BceGuildReqMemberListHandler() {
		super();
	}

	public static BceGuildReqMemberListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceGuildReqMemberList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseGuildReqMemberList.BseGuildReqMemberList.Builder builder = XinqiBseGuildReqMemberList.BseGuildReqMemberList.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceGuildReqMemberList.BceGuildReqMemberList)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceGuildReqMemberList: " + response);
	}
	
	
}
