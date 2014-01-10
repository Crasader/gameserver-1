package com.xinqihd.sns.gameserver.transport;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.util.ClientPool;

/**
 * The MessageClient will transport messages to other GameServer
 * 
 * @author wangqi
 * 
 */
public class GameProxyClient {

	public static final int CONNECT_TIMEOUT = 1000;

	private static final Logger logger = LoggerFactory.getLogger(GameProxyClient.class);

	private static final XinqiProxyMessage HEART_BEAT_MSG = new XinqiProxyMessage();

	private static final StatIoFilterListener statListener = new StatIoFilterListener();

	private GameProtocolCodecFilter filter = new GameProtocolCodecFilter();

	private IoHandler[] handlers = null;

	private ClientPool clientPool = null;
	
	private String remoteHost;
	
	private int remotePort;

	public GameProxyClient(String remoteHost, int remotePort) {
		this(remoteHost, remotePort, Constant.CPU_CORES);
	}
	
	public GameProxyClient(String remoteHost, int remotePort, int count) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		handlers = new MessageIoHandler[count];
		for ( int i=0; i<handlers.length; i++ ) {
			handlers[i] = new MessageIoHandler();
		}
		int heartBeatSecond = GlobalConfig.getInstance().getIntProperty("message.heartbeat.second");
		if ( logger.isDebugEnabled() ) {
			logger.debug("heartBeatSecond : " + heartBeatSecond);
		}
		clientPool = new ClientPool(filter, handlers, remoteHost, remotePort, count);
		clientPool.setStatListener(statListener);
		clientPool.setHeartBeatSecond(heartBeatSecond);
		clientPool.setHeartBeatMessage(HEART_BEAT_MSG);
		clientPool.initQueue();
	}

	/**
	 * Connect to remote message server.
	 * 
	 * @return
	 */
	public boolean connectToServer() {
		return clientPool.connectToServer();
	}

	/**
	 * Disconnect from remote message server and release resources.
	 * 
	 * @return
	 */
	public boolean disconnectFromServer() {
		return clientPool.connectToServer();
	}

	/**
	 * Send a message to server.
	 * 
	 * @param msg
	 */
	public WriteFuture sendMessageToServer(XinqiProxyMessage msg) {
		return clientPool.sendMessageToServer(msg);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(32);
		buf.append(remoteHost).append(Constant.COLON).append(remotePort);
		return buf.toString();
	}

	/**
	 * Internal MessageIoHandler.
	 * @author wangqi
	 *
	 */
	private class MessageIoHandler extends IoHandlerAdapter {

		/**
		 * Session opened
		 */
		@Override
		public void sessionOpened(IoSession session) throws Exception {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy session has been opened. ");
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
					logger.debug("Proxy session has been closed. ");
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
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy session has been idle for a while. Send a heartbeat message.");
				}
				sendMessageToServer(HEART_BEAT_MSG);
				Stat.getInstance().gameHearbeatSent++;
			} finally {
			}
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
				Stat.getInstance().gameClientSentFail++;
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy Caught Exception: {}", cause.getMessage());
				}
				if (!session.isConnected()) {
					if (logger.isInfoEnabled()) {
						logger.info("Proxy reconnect to server due to closed session");
					}
					disconnectFromServer();
					connectToServer();
				}
			} finally {
			}
		}

		/**
		 * A message received when testing.
		 */
		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			// Server will never send a message to clients.
			try {
				logger.info("Proxy message received");
			} catch (Exception e) {
				logger.error("Proxy Caught Exception: {}", e.getMessage());
			}
		}
	}

	static class StatIoFilterListener implements IoFutureListener<IoFuture> {

		@Override
		public void operationComplete(IoFuture future) {
			Stat.getInstance().gameClientSent++;
		}

	}

}
