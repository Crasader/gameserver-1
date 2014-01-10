package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.WeiboManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceWeibo.BceWeibo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceEnterRoomHandler is used for protocol EnterRoom 
 * @author wangqi
 *
 */
public class BceWeiboHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceWeiboHandler.class);
	
	private static final BceWeiboHandler instance = new BceWeiboHandler();
	
	private BceWeiboHandler() {
		super();
	}

	public static BceWeiboHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceWeibo");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BceWeibo weibo = (BceWeibo)request.payload; 
		
		WeiboManager.getInstance().processWeibo(user, weibo);
	}
	
	
}
