package com.xinqihd.sns.gameserver.transport;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.proto.XinqiBceHeartbeat.BceHeartbeat;

/**
 * The GameClient is only for test purpose. 
 * 
 * @author wangqi
 *
 */
public class GameClient extends IoHandlerAdapter {

	public static final int CONNECT_TIMEOUT = 8000;
	
	private static final Log log = LogFactory.getLog(GameClient.class);
	
	private static final XinqiMessage HEART_BEAT_MSG = new XinqiMessage();
	
	private Object lastMessage = null;
	
	private ArrayList lastMessages = new ArrayList();
	
	static {
		BceHeartbeat hearbeat = BceHeartbeat.getDefaultInstance();
		HEART_BEAT_MSG.index = 0;
		HEART_BEAT_MSG.type = MessageToId.messageToId(hearbeat);
		HEART_BEAT_MSG.payload = hearbeat;
	}
	
	private static final StatIoFilterListener statListener = new StatIoFilterListener();
		
	private Lock resourceLock = new ReentrantLock();
//	private Condition connectedCond = resourceLock.newCondition();
		
	//Connection
	private SocketConnector connector;
	
	protected IoSession session;
	
	private String remoteHost;
	
	private int remotePort;
	
	private IoHandler handler;
		
	public GameClient(String remoteHost, int remotePort) {
		this(remoteHost, remotePort, null);
	}
	
	public GameClient(String remoteHost, int remotePort, IoHandler handler) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		if ( handler == null ) {
			this.handler = this;
		} else {
			this.handler = handler;
		}
	}
	
	/**
	 * Connect to remote message server.
	 * @return
	 */
	public boolean connectToServer() {
		try {
			resourceLock.lock();
			
			if ( log.isInfoEnabled() ) {
				log.info("Connect to message server : " + remoteHost + ":" + remotePort);
			}
			connector = new NioSocketConnector();
			connector.getFilterChain().addLast("codec", new GameProtocolCodecFilter());
			connector.setHandler(this.handler);
			int heartBeatSecond = GlobalConfig.getInstance().getIntProperty("message.heartbeat.second");
			if ( log.isDebugEnabled() ) {
				log.debug("heartBeatSecond : " + heartBeatSecond);
			}
			connector.getSessionConfig().setBothIdleTime(heartBeatSecond);
			
			// Make a new connection
	    ConnectFuture connectFuture = connector.connect(new InetSocketAddress(remoteHost, remotePort));
	    // Wait until the connection is make successfully.
	    connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
	    try {
	        session = connectFuture.getSession();
	        //Tell caller we can write message.
//	        connectedCond.signal();
	        if ( log.isInfoEnabled() ) {
	        	log.info("connect to " + remoteHost + ":" + remotePort);
	        }
	        return true;
	    }
	    catch (Throwable e) {
	  		disconnectFromServer();
	  		return false;
	    }
		} finally {
			resourceLock.unlock();
		}
	}
	
	/**
	 * Disconnect from remote message server and release resources.
	 * @return
	 */
	public boolean disconnectFromServer() {
		try {
			if ( log.isInfoEnabled() ) {
				log.info("Disconnect from game server : " + remoteHost + ":" + remotePort);
			}
			resourceLock.lock();
			if ( session != null ) {
				session.close(false);
			}
			if ( connector != null ) {
				connector.dispose();
			}
			return true;
		} catch (Throwable e) {
			if ( log.isDebugEnabled() ) {
				log.debug(e, e);
			}
			return false;
		} finally {
			resourceLock.unlock();
		}
	}
	
	/**
	 * Session opened
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		try {
			resourceLock.lock();
			if ( log.isDebugEnabled() ) {
				log.debug("session has been opened. ");
			}
		} finally {
			resourceLock.unlock();
		}
	}

	/**
	 * Session closed
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		try {
			resourceLock.lock();
			if ( log.isDebugEnabled() ) {
				log.debug("session has been closed. ");
			}
		} finally {
			resourceLock.unlock();
		}
	}

	/**
	 * When a session is idle for configed seconds, it will send a heart-beat message
	 * to remote.
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		try {
			resourceLock.lock();
			if ( log.isDebugEnabled() ) {
				log.debug("session has been idle for a while. Send a heartbeat message.");
			}
			sendMessageToServer(HEART_BEAT_MSG);
			Stat.getInstance().messageHearbeatSent++;
		} finally {
			resourceLock.unlock();
		}
	}

	/**
	 * Invoked when any exception is thrown by user IoHandler implementation or by MINA. 
	 * If cause is an instance of IOException, MINA will close the connection automatically.
	 * 
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		try {
			resourceLock.lock();
			Stat.getInstance().messageClientSentFail++;
			if ( log.isDebugEnabled() ) {
				log.debug(cause, cause);
			}
		} finally {
			resourceLock.unlock();
		}
	}

	/**
	 * A message received when testing.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		//Server will never send a message to clients.
		try {
			log.info("message received: " + message);
			this.lastMessage = message;
			this.lastMessages.add(message);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	/**
	 * Send a message to server.
	 * @param msg
	 */
 public void sendMessageToServer(XinqiMessage msg) {
	 try { 
		 resourceLock.lock();
		 if ( session == null || !session.isConnected() ) {
				disconnectFromServer();
				connectToServer();
		 }
		 WriteFuture future = session.write(msg);
		 future.addListener(statListener);
	 } catch (Throwable t) {
		 if ( log.isDebugEnabled() ) {
			 log.debug(t, t);
		 }
	 } finally {
		 resourceLock.unlock();
	 }
	}
 
 
 /**
	 * @return the lastMessge
	 */
	public Object getLastMessage() {
		return lastMessage;
	}
	
	public ArrayList getLastMessages() {
		return lastMessages;
	}
	
	public void clearLastMessages() {
		this.lastMessages.clear();
	}

	public void clearLastMessage() {
		this.lastMessage = null;
	}

static class StatIoFilterListener implements IoFutureListener<IoFuture> {

	@Override
	public void operationComplete(IoFuture future) {
		Stat.getInstance().messageClientSent++;
	}

 }
	
}
