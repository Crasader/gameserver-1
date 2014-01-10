package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.InviteManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceInvite.BceInvite;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceInviteHandler is used for protocol Invite 
 * @author wangqi
 *
 */
public class BceInviteHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceInviteHandler.class);
	
	private static final BceInviteHandler instance = new BceInviteHandler();
	
	private BceInviteHandler() {
		super();
	}

	public static BceInviteHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceInvite");
		}
		
		User user = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceInvite invite = (BceInvite)request.payload;
		
		InviteManager.getInstance().challengeFriend(user, invite);
	}
	
	
}
