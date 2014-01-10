package com.xinqihd.sns.gameserver.session;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The SessionMessage wraps SessionKey and the message content. 
 * 
 * @author wangqi
 *
 */
public class SessionMessage {
	
	private SessionKey sessionkey;
	
	private XinqiMessage message;

	/**
	 * @return the sessionkey
	 */
	public SessionKey getSessionkey() {
		return sessionkey;
	}

	/**
	 * @param sessionkey the sessionkey to set
	 */
	public void setSessionkey(SessionKey sessionkey) {
		this.sessionkey = sessionkey;
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

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("SessionMessage:");
		buf.append(this.sessionkey.toString());
		buf.append(Constant.PATH_SEP);
		if ( this.message == null ) {
			buf.append("null");
		} else if ( this.message instanceof XinqiMessage ) {
			buf.append(this.message);
		} else {
			buf.append(this.message.getClass());
		}
		return buf.toString();
	}
}
