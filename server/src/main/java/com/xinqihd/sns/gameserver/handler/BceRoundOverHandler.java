package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;

/**
 * The BceRoundOverHandler is used for protocol RoundOver 
 * @author wangqi
 *
 */
public class BceRoundOverHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRoundOverHandler.class);
	
	private static final BceRoundOverHandler instance = new BceRoundOverHandler();
	
	private BceRoundOverHandler() {
		super();
	}

	public static BceRoundOverHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRoundOver");
		}
		
		GameContext.getInstance().getBattleManager().roundOver(sessionKey, null);
	}
	
	
}
