package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceForbidUser.BceForbidUser;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 
 * @author wangqi
 *
 */
public class BceForbidUserHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceForbidUserHandler.class);
	
	private static final BceForbidUserHandler instance = new BceForbidUserHandler();
	
	private BceForbidUserHandler() {
		super();
	}

	public static BceForbidUserHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceForbidUser");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceForbidUser forbidUser = (BceForbidUser)request.payload;
		String accountName = forbidUser.getAccountname();
		String content = forbidUser.getMessage();
		AccountManager.getInstance().forbiddenAccount(accountName, content);
		
	}
	
}
