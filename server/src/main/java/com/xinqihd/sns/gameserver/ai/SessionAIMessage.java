package com.xinqihd.sns.gameserver.ai;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * User session's AI message
 * 
 * @author wangqi
 *
 */
public class SessionAIMessage {
	
	private SessionKey sessionKey;
	
	private XinqiMessage message;

	/**
	 * @return the sessionKey
	 */
	public SessionKey getSessionKey() {
		return sessionKey;
	}

	/**
	 * @param sessionKey the sessionKey to set
	 */
	public void setSessionKey(SessionKey sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * @return the message
	 */
	public XinqiMessage getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(XinqiMessage message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SessionAIMessage[sessionKey=");
		builder.append(sessionKey);
		builder.append(",message=");
		if ( message != null && message.payload != null ) {
			builder.append(message.payload.getClass());
		} else {
			builder.append("null");
		}
		return builder.toString();
	}

}
