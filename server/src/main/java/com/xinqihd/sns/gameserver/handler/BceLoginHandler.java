package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The BceLoginHandler is used for protocol Login
 * 
 * @author wangqi
 * 
 */
public class BceLoginHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(BceLoginHandler.class);

	private static final BceLoginHandler instance = new BceLoginHandler();

	private BceLoginHandler() {
		super();
	}

	public static BceLoginHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("->BceLogin");
			}

			XinqiMessage request = (XinqiMessage) message;

			BceLogin loginInfo = (BceLogin) request.payload;
			String userName = loginInfo.getUsername();
			String password = loginInfo.getPassword();
			int majorVersion = loginInfo.getMajorversion();
			int minorVersion = loginInfo.getMinorversion();
			
			/**
			 * Try to use new login system
			 */
//			boolean loginSuccess = LoginManager.getInstance().login(userName, password, 
//					loginInfo.getVersion(), majorVersion, minorVersion, session, loginInfo, 
//					sessionKey);
			boolean loginSuccess = AccountManager.getInstance().login(userName, password, 
					loginInfo.getVersion(), majorVersion, minorVersion, session, loginInfo, 
					sessionKey);
			/*
			if ( loginSuccess && guestTryToLogin ) {
				logger.debug("Guest user {} try to login as an existing account and succeed. Remove the guest account", 
						guestUserId, userName);
				UserManager.getInstance().removeUser(guestUserId);
			}
			*/
			
		} catch (Throwable e) {

			BseLogin.Builder loginRep = BseLogin.newBuilder();
			loginRep.setCode(ErrorCode.OTHERS.ordinal());
			loginRep.setDesc(Text.text(ErrorCode.OTHERS.desc()));
			XinqiMessage response = new XinqiMessage();
			response.payload = loginRep.build();
			GameContext.getInstance().writeResponse(session, response, sessionKey);

			logger.warn(e.getMessage(), e);
		}
	}
	
}
