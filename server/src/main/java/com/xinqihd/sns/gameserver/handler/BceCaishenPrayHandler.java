package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.CaishenManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceActionLimitQuery.BceActionLimitQuery;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceCaishenPray is used for caishen 
 * @author wangqi
 *
 */
public class BceCaishenPrayHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCaishenPrayHandler.class);
	
	private static final BceCaishenPrayHandler instance = new BceCaishenPrayHandler();
	
	private BceCaishenPrayHandler() {
		super();
	}

	public static BceCaishenPrayHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCaishenPray");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		CaishenManager.getInstance().prayGolden(user, System.currentTimeMillis());
	}
	
}
