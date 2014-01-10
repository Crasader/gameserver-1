package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceDead.BceDead;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceDeadHandler is used for protocol Dead 
 * @author wangqi
 *
 */
public class BceDeadHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceDeadHandler.class);
	
	private static final BceDeadHandler instance = new BceDeadHandler();
	
	private BceDeadHandler() {
		super();
	}

	public static BceDeadHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceDead");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceDead dead = (BceDead)request.payload;
		
		GameContext.getInstance().getBattleManager().roleDead(sessionKey, dead);
	}
	
	
}
