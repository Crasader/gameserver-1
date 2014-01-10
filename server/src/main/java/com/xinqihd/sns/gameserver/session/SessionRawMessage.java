package com.xinqihd.sns.gameserver.session;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * The SessionRawMessage does not encode or decode XinqiMessage.
 * 
 * @author wangqi
 *
 */
public class SessionRawMessage {
	
	private SessionKey sessionkey;
	
	private byte[] rawMessage;

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
	 * @return the rawMessage
	 */
	public byte[] getRawMessage() {
		return rawMessage;
	}

	/**
	 * @param rawMessage the rawMessage to set
	 */
	public void setRawMessage(byte[] rawMessage) {
		this.rawMessage = rawMessage;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("SessionRawMessage:");
		buf.append(this.sessionkey.toString());
		buf.append(Constant.PATH_SEP);
		buf.append('[');
		if ( this.rawMessage != null ) {
			buf.append(rawMessage.length);
		}
		buf.append(']');
		return buf.toString();
	}
}
