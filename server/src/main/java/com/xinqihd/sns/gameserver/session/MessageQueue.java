package com.xinqihd.sns.gameserver.session;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;

/**
 * The MessageQueue will:
 * 1) Asynchronously write message back to clients.
 * 2) Maintain user's session key and iosession. 
 * 3) Process other GameServers' delivered messages.
 * 
 * @author wangqi
 *
 */
public interface MessageQueue extends IoHandler {
	
	public abstract void initQueue();

	public abstract IoSession findSession(SessionKey sessionKey);

	/**
	 * Write a message back to client.
	 * 
	 * @param message
	 */
	public abstract void sessionWrite(SessionKey sessionKey, Object message);

	public abstract void destroyQueue();

	/**
	 * It is for internal use. The message will be writen to session directly.
	 * @param sessionKey
	 * @param session
	 * @param message
	 */
	public abstract void sessionWrite(IoSession session, Object message, SessionKey sessionKey);

}
