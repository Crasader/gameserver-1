package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseHeartbeat.BseHeartbeat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceHeartbeatHandler is used for protocol Heartbeat 
 * 
 * DONE
 * 
 * @author wangqi
 *
 */
public class BceHeartbeatHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceHeartbeatHandler.class);
	
	private static final BceHeartbeatHandler instance = new BceHeartbeatHandler();
	
	private BceHeartbeatHandler() {
		super();
	}

	public static BceHeartbeatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		if ( logger.isDebugEnabled() ) {
			logger.debug("BceHeartbeat from user: {}", user.getRoleName());
		}
		XinqiMessage response = new XinqiMessage();
		BseHeartbeat.Builder builder = BseHeartbeat.newBuilder();
		response.payload = builder.build();
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
	}
	
	
}
