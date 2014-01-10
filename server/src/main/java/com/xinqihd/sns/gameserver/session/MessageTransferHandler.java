package com.xinqihd.sns.gameserver.session;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;

/**
 * Transfer the SessionRawMessage between MessageServers
 * @author wangqi
 *
 */
public class MessageTransferHandler extends IoHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(MessageTransferHandler.class);
	
	private MessageQueue messageQueue = MinaMessageQueue.getInstance();
	
	/**
	 * This will be called when a message delivered from other GameServers.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		/**
		 * Process the RawMessage protocol
		 */
		if ( message instanceof SessionRawMessage ) {
			SessionRawMessage rawMessage = (SessionRawMessage)message;
			SessionKey sessionKey = rawMessage.getSessionkey();
			
			if ( sessionKey.getRawKey() != null && sessionKey.getRawKey().length > 0 &&
					sessionKey.getRawKey()[0] == 0 ) {
				//It is a heart-beat messge. ignore it.
				if ( logger.isDebugEnabled() ) {
					logger.debug("Heartbeat message received from {}", session.getRemoteAddress());
				}
				Stat.getInstance().messageHearbeatReceived++;
				return;
			}
			
			IoSession userSession = null;
			try {
				userSession = messageQueue.findSession(sessionKey);
			} catch (Throwable t) {
				logger.warn("Exception: {}", t.getMessage());
				if ( logger.isDebugEnabled() ) {
					logger.debug(t.getMessage(), t);
				}
			}
			if ( userSession != null && userSession.isConnected() ) {
				byte[] rawMessageBytes = rawMessage.getRawMessage();
				if ( rawMessageBytes != null && rawMessageBytes.length > 0 ) {
					IoBuffer buf = IoBuffer.wrap(rawMessageBytes);
					messageQueue.sessionWrite(sessionKey, buf);
				} else {
					if ( logger.isDebugEnabled() ) {
						logger.debug("rawMessageBytes is null." + userSession);
					}
				}
			} else {
				if ( logger.isDebugEnabled() ) {
					logger.debug("User's IoSession is not connected." + userSession);
				}
			}
			Stat.getInstance().messageServerReceived++;
		}
	}

}
