package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceFillProfile.BceFillProfile;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRegisterHandler is used for protocol Register 
 * @author wangqi
 *
 */
public class BceFillProfileHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceFillProfileHandler.class);
	
	private static final BceFillProfileHandler instance = new BceFillProfileHandler();
	
	private BceFillProfileHandler() {
		super();
	}

	public static BceFillProfileHandler getInstance() {
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
		BceFillProfile fillProfile = (BceFillProfile)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
    /**
	   * operation action
	   * 0: change rolename
	   * 1: change password
	   * 2: change email
     */
		switch ( fillProfile.getAction() ) {
			case 0:
				AccountManager.getInstance().changeRoleName(user, 
						fillProfile.getValue());
				break;
			case 1:
				AccountManager.getInstance().changePassword(user, 
						fillProfile.getValue(), user.getAccount());
				break;
			case 2:
				AccountManager.getInstance().changeEmail(session, sessionKey, 
						fillProfile.getValue(), user.getAccount(), user);
				break;
		}
	}
	
}
