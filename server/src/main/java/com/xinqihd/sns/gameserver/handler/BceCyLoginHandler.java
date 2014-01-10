package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.proto.XinqiBceCyLogin.BceCyLogin;
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
public class BceCyLoginHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(BceCyLoginHandler.class);

	private static final BceCyLoginHandler instance = new BceCyLoginHandler();

	private BceCyLoginHandler() {
		super();
	}

	public static BceCyLoginHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("->BceCyLogin");
			}

			XinqiMessage request = (XinqiMessage) message;

			BceCyLogin loginInfo = (BceCyLogin) request.payload;
			String userName = loginInfo.getUsername();
			String password = loginInfo.getPassword();
			
			
		} catch (Throwable e) {
			
		}
	}
	
}
