package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.EmailManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceForgetPassword.BceForgetPassword;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 罗列各个房间的人数情况
 *
 */
public class BceForgetPasswordHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceForgetPasswordHandler.class);
	
	private static final BceForgetPasswordHandler instance = new BceForgetPasswordHandler();
	
	private BceForgetPasswordHandler() {
		super();
	}

	public static BceForgetPasswordHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceForgetPassword");
		}
		
		XinqiMessage request = (XinqiMessage)message;

		BceForgetPassword bcePassword = (BceForgetPassword)request.payload;
		EmailManager.getInstance().forgetPassword(bcePassword.getRoleName(), session);
	}
	
}
