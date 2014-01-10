package com.xinqihd.sns.gameserver.ai;

import java.net.SocketAddress;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;

/**
 * If the AI user is in localhost, then use this fake IoSession
 * to transfer protocol package.
 * 
 * @author wangqi
 *
 */
public class AILocalIoSession extends DummySession {

	/* (non-Javadoc)
	 * @see org.apache.mina.core.session.AbstractIoSession#write(java.lang.Object, java.net.SocketAddress)
	 */
	@Override
	public WriteFuture write(Object message, SocketAddress remoteAddress) {
		System.out.println(message);
		return null;
	}

}
