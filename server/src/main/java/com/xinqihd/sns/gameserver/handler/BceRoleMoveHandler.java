package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleMove.BceRoleMove;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRoleMoveHandler is used for protocol RoleMove 
 * @author wangqi
 *
 */
public class BceRoleMoveHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoleMoveHandler.class);
	
	private static final BceRoleMoveHandler instance = new BceRoleMoveHandler();
	
	private BceRoleMoveHandler() {
		super();
	}

	public static BceRoleMoveHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoleMove");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceRoleMove move = (BceRoleMove)request.payload;
		
		GameContext.getInstance().getBattleManager().roleMove(sessionKey, move);
	}
	
	
}
