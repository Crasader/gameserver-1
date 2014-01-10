package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseAnswerInvite;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceAnswerInviteHandler is used for protocol AnswerInvite 
 * @author wangqi
 *
 */
public class BceAnswerInviteHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceAnswerInviteHandler.class);
	
	private static final BceAnswerInviteHandler instance = new BceAnswerInviteHandler();
	
	private BceAnswerInviteHandler() {
		super();
	}

	public static BceAnswerInviteHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceAnswerInvite");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseAnswerInvite.BseAnswerInvite.Builder builder = XinqiBseAnswerInvite.BseAnswerInvite.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceAnswerInvite.BceAnswerInvite)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceAnswerInvite: " + response);
	}
	
	
}
