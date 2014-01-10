package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceRegisterHandler is used for protocol Register 
 * @author wangqi
 *
 */
public class BceRegisterHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceRegisterHandler.class);
	
	private static final BceRegisterHandler instance = new BceRegisterHandler();
	
	private BceRegisterHandler() {
		super();
	}

	public static BceRegisterHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceRegister");
		}
		XinqiMessage request = (XinqiMessage)message;
		
		/**
		 * Get the user registration info.
		 */
		XinqiBceRegister.BceRegister registerInfo = ((XinqiBceRegister.BceRegister)request.payload);
		
		String userName = registerInfo.getUsername();
		String password = registerInfo.getPassword();
		String clientVersion = StringUtil.concat(registerInfo.getMajorversion(), ".", 
				registerInfo.getMinorversion(), ".", registerInfo.getTinyversion());
		/**
		 * Use the new accounting system
		 */
		/*
		LoginManager.getInstance().register(
				session, sessionKey, registerInfo.getUsername(), registerInfo.getRolename(), 
				registerInfo.getPassword(), registerInfo.getEmail(), registerInfo.getGender(), 
				registerInfo.getClient(), registerInfo.getCountry(), registerInfo.getChannel(), 
				registerInfo.getLocx(), registerInfo.getLocy());
				*/
		AccountManager.getInstance().register(
				session, sessionKey, registerInfo.getUsername(), registerInfo.getRolename(), 
				registerInfo.getPassword(), registerInfo.getEmail(), registerInfo.getGender(), 
				registerInfo.getClient(), registerInfo.getCountry(), registerInfo.getChannel(), 
				registerInfo.getLocx(), registerInfo.getLocy(), clientVersion);
	}
	
}
