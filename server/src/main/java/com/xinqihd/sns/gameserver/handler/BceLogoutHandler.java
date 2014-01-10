package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogout.BceLogout;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceLoginHandler is used for protocol Login
 * 
 * @author wangqi
 * 
 */
public class BceLogoutHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(BceLogoutHandler.class);

	private static final BceLogoutHandler instance = new BceLogoutHandler();

	private BceLogoutHandler() {
		super();
	}

	public static BceLogoutHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("->BceLogout");
			}

			XinqiMessage request = (XinqiMessage) message;
			BceLogout logoutInfo = (BceLogout) request.payload;
			String userid = logoutInfo.getUserid();
			String client = logoutInfo.getClient();
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			AccountManager.getInstance().logout(session, user, sessionKey, client, false);
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.LogoutMutil, client, false);
			
		} catch (Throwable e) {
		}
	}
	
}
