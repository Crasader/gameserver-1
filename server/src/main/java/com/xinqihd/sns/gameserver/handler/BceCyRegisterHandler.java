package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.proto.XinqiBceCyRegister.BceCyRegister;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRegisterHandler is used for protocol Register 
 * @author wangqi
 *
 */
public class BceCyRegisterHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCyRegisterHandler.class);
	
	private static final BceCyRegisterHandler instance = new BceCyRegisterHandler();
	
	private BceCyRegisterHandler() {
		super();
	}

	public static BceCyRegisterHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCyRegister");
		}
		XinqiMessage request = (XinqiMessage)message;
		
		/**
		 * Get the user registration info.
		 */
		BceCyRegister registerInfo = ((BceCyRegister)request.payload);
		
		String userName = registerInfo.getUsername();
		String password = registerInfo.getPassword();		
	}
	
}
