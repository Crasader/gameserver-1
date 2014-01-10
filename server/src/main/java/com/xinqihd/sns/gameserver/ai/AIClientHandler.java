package com.xinqihd.sns.gameserver.ai;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.transport.GameHandler;

/**
 * Internal AI message client handler
 * @author wangqi
 *
 */
public class AIClientHandler extends IoHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(AIClientHandler.class);
	
	public static final SessionAIMessage HEART_BEAT_MSG = new SessionAIMessage();
	
	public AIClientHandler() {
	}

	// ----------------------------------------------- IoSession

	/**
	 * Session opened
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("session has been opened. ");
			}
		} finally {
		}
	}

	/**
	 * Session closed
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("session has been closed. ");
			}
		} finally {
		}
	}

	/**
	 * When a session is idle for configed seconds, it will send a heart-beat
	 * message to remote.
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
//		try {
//			if (logger.isDebugEnabled()) {
//				logger.debug("session has been idle for a while. Send a heartbeat message.");
//			}
//		} finally {
//		}
	}

	/**
	 * Invoked when any exception is thrown by user IoHandler implementation or
	 * by MINA. If cause is an instance of IOException, MINA will close the
	 * connection automatically.
	 * 
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		try {
			Stat.getInstance().messageClientSentFail++;
			if (logger.isDebugEnabled()) {
				logger.debug("Caught Exception: ", cause);
			}
//			if (!session.isConnected()) {
//				if (logger.isInfoEnabled()) {
//					logger.info("reconnect to server due to closed session");
//				}
//			}
		} finally {
		}
	}

	/**
	 * A message received when testing.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		messageProcess(session, message);
	}
	
	/**
	 * Process the message 
	 * @param session
	 * @param message
	 */
	public static void messageProcess(IoSession session, Object message) {
	// Server will never send a message to clients.
			try {
				if ( message != null && message instanceof SessionAIMessage ) {
					SessionAIMessage sessionAIMessage = (SessionAIMessage)message;
					if ( sessionAIMessage.getSessionKey() != null 
							&& sessionAIMessage.getMessage() != null ) {
						GameHandler.messageProcess(session, sessionAIMessage.getMessage(), sessionAIMessage.getSessionKey());
					} else {
						logger.info("sessionAIMessage's sessionKey or message is null", sessionAIMessage);
					}
				} else {
					logger.debug("#AIClientHandler.messageReceived: ignore non SessionAIMessage: {}", message);
				}
			} catch (Exception e) {
				logger.error("Caught Exception: {}", e.getMessage());
				logger.debug("Caught Exception", e);
			}
	}
	
	static class StatIoFilterListener implements IoFutureListener<IoFuture> {
		@Override
		public void operationComplete(IoFuture future) {
			Stat.getInstance().aiMessageSent++;
		}
	}
}
