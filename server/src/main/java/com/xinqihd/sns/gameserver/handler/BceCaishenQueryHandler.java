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
public class BceCaishenQueryHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCaishenQueryHandler.class);
	
	private static final BceCaishenQueryHandler instance = new BceCaishenQueryHandler();
	
	private BceCaishenQueryHandler() {
		super();
	}

	public static BceCaishenQueryHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCaishenQuery");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		//Send the pray info
		CaishenManager.getInstance().queryCaishenPrayInfo(user, System.currentTimeMillis(), true);
	}
	
}
