package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleChangeDirection.BceRoleChangeDirection;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleChangeDirectionHandler is used for protocol RoleChangeDirection 
 * @author wangqi
 *
 */
public class BceRoleChangeDirectionHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleChangeDirectionHandler.class);
	
	private static final BceRoleChangeDirectionHandler instance = new BceRoleChangeDirectionHandler();
	
	private BceRoleChangeDirectionHandler() {
		super();
	}

	public static BceRoleChangeDirectionHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleChangeDirection");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceRoleChangeDirection roleDir = (BceRoleChangeDirection)request.payload;
		
		GameContext.getInstance().getBattleManager().changeDirection(sessionKey, roleDir);
		
	}
	
	
}
