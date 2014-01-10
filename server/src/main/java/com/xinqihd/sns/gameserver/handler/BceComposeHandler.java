package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceCompose.BceCompose;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceComposeHandler is used for protocol Compose 
 * @author wangqi
 *
 */
public class BceComposeHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceComposeHandler.class);
	
	private static final BceComposeHandler instance = new BceComposeHandler();
	
	private BceComposeHandler() {
		super();
	}

	public static BceComposeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceCompose");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceCompose compose = (BceCompose)request.payload;
		int[] pews = new int[compose.getPewsCount()];
		for ( int i=0; i<pews.length; i++ ) {
			pews[i] = compose.getPews(i);
		}
		
		//The User object should not be null because GameHandler is checking it.
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getCraftManager().composeItem(user, pews);
	}
	
	
}
