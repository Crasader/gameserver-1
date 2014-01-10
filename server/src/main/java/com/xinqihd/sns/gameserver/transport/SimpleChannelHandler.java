package com.xinqihd.sns.gameserver.transport;

import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * All the business logic will be implemented in the subclass.
 * @author wangqi
 *
 */
public class SimpleChannelHandler {
	
	protected GameContext gameContext = GameContext.getInstance();

	/**
	 * Receive a XinqiMessage and do the business logic.
	 * 
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	public void messageProcess(IoSession session, Object message, SessionKey userSessionKey) 
			throws Exception {
	}
	
}
