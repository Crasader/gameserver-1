package com.xinqihd.sns.gameserver.util;

import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

/**
 * The Client will transport messages to other GameServer 
 * 
 * @author wangqi
 *
 */
public class SimpleClient implements Client, IoHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleClient.class);
	
	private static int totalClientNo = 0;
	
	public static final String TIME_OUT_MAP_KEY = "client_timeout_map:";
	private static final String COUNT_FIELD = "count:";
	private static final String NEXTTIMEOUT_FIELD = "nexttimeout:";
			
	private Lock resourceLock = new ReentrantLock();
	
	private Object heartBeatMessage = null;
	
	private IoFutureListener<IoFuture> statListener = null;
		
	//Unchanged fields
	private String remoteHost;
	
	private int remotePort;
	
	//The second to send heart beat message
	private int heartBeatSecond = HEARTBEAT_MILLIS;
	
	private int connectTimeout = CONNECT_TIMEOUT;
	
	private IoFilter ioFilter;
	
	private IoHandler ioHandler;
	
	private int clientNo = 0;
	
	//should be protected by multi-thread access
	private SocketConnector connector;
	
	private IoSession session;
	
	private boolean isResourceProtected = true;
	
	private String connectKey = null;
	
	/**
	 * Create a new SimpleClient without handler.
	 * @param filter
	 * @param remoteHost
	 * @param remotePort
	 */
	public SimpleClient(IoFilter filter, String remoteHost, int remotePort) {
		this(filter, null, remoteHost, remotePort);
	}
		
	/**
	 * Create a new Client
	 * @param filter
	 * @param handler
	 * @param remoteHost
	 * @param remotePort
	 */
	public SimpleClient(IoFilter filter, IoHandler handler, 
			String remoteHost, int remotePort) {
		this.ioFilter = filter;
		this.ioHandler = handler;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.connectKey = StringUtil.concat(remoteHost, Constant.COLON, remotePort);
		this.clientNo = SimpleClient.totalClientNo++;
	}

	// ------------------------------------------- Properties
	

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getConnectTimeout()
	 */
	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * @return the connectKey
	 */
	public String getConnectKey() {
		return connectKey;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setConnectTimeout(int)
	 */
	@Override
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getHeartBeatMessage()
	 */
	@Override
	public Object getHeartBeatMessage() {
		return heartBeatMessage;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setHeartBeatMessage(com.google.protobuf.Message)
	 */
	@Override
	public void setHeartBeatMessage(Object heartBeatMessage) {
		this.heartBeatMessage = heartBeatMessage;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getStatListener()
	 */
	@Override
	public IoFutureListener<IoFuture> getStatListener() {
		return statListener;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setStatListener(org.apache.mina.core.future.IoFutureListener)
	 */
	@Override
	public void setStatListener(IoFutureListener<IoFuture> statListener) {
		this.statListener = statListener;
	}

	/**
	 * @return the isResourceProtected
	 */
	public boolean isResourceProtected() {
		return isResourceProtected;
	}

	/**
	 * @param isResourceProtected the isResourceProtected to set
	 */
	public void setResourceProtected(boolean isResourceProtected) {
		this.isResourceProtected = isResourceProtected;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getHeartBeatSecond()
	 */
	@Override
	public int getHeartBeatSecond() {
		return heartBeatSecond;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setHeartBeatSecond(int)
	 */
	@Override
	public void setHeartBeatSecond(int heartBeatMillis) {
		this.heartBeatSecond = heartBeatMillis;
	}
	
	//------------------------------------------- Methods

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#connectToServer()
	 */
	@Override
	public boolean connectToServer() {
		try {
			if ( isResourceProtected ) {
				resourceLock.lock();
			}

			Jedis jedis = JedisFactory.getJedis();
			String key = StringUtil.concat(TIME_OUT_MAP_KEY, this.connectKey);
			long nextTimeout = 0;
			if ( jedis.exists(key) ) {
				String nextTimeoutStr = jedis.hget(key, NEXTTIMEOUT_FIELD);
				try {
					nextTimeout = Long.parseLong(nextTimeoutStr);
				} catch (NumberFormatException e) {
				}
			}
			if ( System.currentTimeMillis() < nextTimeout ) {
				logger.debug( "SimpleClient will not try to connect {} within the timeout", connectKey );
				this.session = null;
				this.connector = null;
				return false;
			}
			
			//Release resource if connected.
			disconnectFromServer();
			
			if ( logger.isInfoEnabled() ) {
				logger.info("Connect to server : {}:{}", remoteHost, String.valueOf(remotePort));
			}
			connector = new NioSocketConnector();
			connector.getFilterChain().addLast("codec", this.ioFilter);
			connector.setHandler(this);
//			if ( logger.isDebugEnabled() ) {
//				logger.debug("heartBeatSecond : " + heartBeatSecond);
//			}
			connector.getSessionConfig().setBothIdleTime(heartBeatSecond);
			//The default timeout is 3 seconds
			connector.setConnectTimeoutMillis(GlobalConfig.getInstance().
					getIntProperty(GlobalConfigKey.simple_client_connect_timeout));
			
			// Make a new connection
	    ConnectFuture connectFuture = connector.connect(new InetSocketAddress(remoteHost, remotePort));
	    
	    Semaphore semaphore = new Semaphore(0);
	    DefaultIoFutureListener listener = new DefaultIoFutureListener(this, semaphore, 
	    		remoteHost, remotePort);
	    connectFuture.addListener(listener);
	    /**
	     * BUG
	     * DEAD LOCK: IoFuture.await() was invoked from an I/O processor thread.  
	     * Please use IoFutureListener or configure a proper thread model alternatively.
	     */
	    // Wait until the connection is make successfully.
	    //connectFuture.awaitUninterruptibly(connectTimeout);
	    try {
				semaphore.acquire();
			} catch (InterruptedException e) {
			}
	    if ( !listener.isSuccess() ) {
	    	disconnectFromServer();
	    }
	    return listener.isSuccess();
		} finally {
			if ( isResourceProtected ) {
				resourceLock.unlock();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#disconnectFromServer()
	 */
	@Override
	public boolean disconnectFromServer() {
		try {
			if ( logger.isInfoEnabled() ) {
				logger.info("#{} disconnect from message server: ",
					 this.clientNo, this.connectKey);
			}
			if ( isResourceProtected ) {
				resourceLock.lock();
			}
			if ( session != null ) {
				session.close(false);
				session = null;
			}
			if ( connector != null ) {
				connector.dispose();
				connector = null;
			}
			return true;
		} catch (Throwable e) {
			if ( logger.isDebugEnabled() ) {
				logger.debug(e.getMessage(), e);
			}
			return false;
		} finally {
			if ( isResourceProtected ) {
				resourceLock.unlock();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#sendMessageToServer(com.google.protobuf.Message)
	 */
 @Override
public WriteFuture sendMessageToServer(Object msg) {
	 try {
		 if ( isResourceProtected ) {
			 resourceLock.lock();
		 }
		 if ( session == null || !session.isConnected() ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug("Client #{} try to reconnect. session:{}, isConnected:", 
							this.clientNo, session);
				}
				connectToServer();
		 }
		 if ( session == null || !session.isConnected() ) {
			 return null;
		 }
		 WriteFuture future = session.write(msg);
		 if ( this.statListener != null ) {
			 future.addListener(statListener);
		 }
		 if ( logger.isDebugEnabled() ) {
			 logger.debug("Client send message to server.");
		 }
		 return future;
	 } catch (Throwable t) {
		 if ( logger.isDebugEnabled() ) {
			 logger.debug(t.getMessage(), t);
		 }
	 } finally {
		 if ( isResourceProtected ) {
			 resourceLock.unlock();
		 }
	 }
	 return null;
	}
	
	//------------------------------------------- IoHandler
	
	/**
	 * Session opened
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		if ( this.ioHandler != null ) {
			this.ioHandler.sessionOpened(session);
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("session has been opened. ");
		}
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if ( this.ioHandler != null ) {
			this.ioHandler.sessionCreated(session);
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("session has been created. ");
		}
	}

	/**
	 * Session closed
	 */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if ( this.ioHandler != null ) {
			this.ioHandler.sessionClosed(session);
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("session has been closed. ");
		}
	}

	/**
	 * When a session is idle for configed seconds, it will send a heart-beat message
	 * to remote.
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		
		if ( heartBeatMessage != null ) {
			sendMessageToServer(heartBeatMessage);
			/*
			if ( logger.isDebugEnabled() ) {
				logger.debug("session has been idle for a while. Send a heartbeat message.");
			}
			*/
		}
		if ( this.ioHandler != null ) {
			this.ioHandler.sessionIdle(session, status);
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

		if ( this.ioHandler != null ) {
			this.ioHandler.exceptionCaught(session, cause);
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug(cause.getMessage(), cause);
		}
		if ( !session.isConnected() ) {
			if ( logger.isInfoEnabled() ) {
				logger.info("reconnect to server due to closed session");
			}
			try {
				resourceLock.lock();
				connectToServer();
			} finally {
				resourceLock.unlock();
			}
		}
	}

	/**
	 * A message received when testing.
	 */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("message received: {}", message);
		}
		if ( this.ioHandler != null ) {
			this.ioHandler.messageReceived(session, message);
		}
	}
	
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		if ( this.ioHandler != null ) {
			this.ioHandler.messageSent(session, message);
		}
		if ( logger.isDebugEnabled() ) {
			if ( message == null ) {
				logger.debug("message sent: null");
			} else {
				logger.debug("message sent: {}", message.getClass());
			}
		}
	}
	
	/**
	 * Calculate the next timeout.
	 * @param timeout
	 * @return
	 */
	public static final long calcNextTimeout(int currentCount, long currentTime) {
		int count = (currentCount+1)%10;
		long nextTimeout = 1000 * 2<<count;
		logger.debug("Next timeout will be after {} millis", nextTimeout);
		nextTimeout = currentTime + nextTimeout;
		return nextTimeout;
	}
	 
 @Override
 public String toString() {
	 StringBuilder buf = new StringBuilder(32);
	 buf.append(remoteHost).append(Constant.COLON).append(remotePort);
	 return buf.toString();
 }

 /**
  * The IoFutureListener is used to monitor the connection result.
  * @author wangqi
  *
  */
	private static class DefaultIoFutureListener implements
			IoFutureListener<IoFuture> {

		SimpleClient client = null;
		Semaphore semaphore = null;
		String remoteHost = null;
		int remotePort = 0;
		boolean success = false;

		DefaultIoFutureListener(SimpleClient client, Semaphore semaphore, 
				String remoteHost, int remotePort) {
			this.client = client;
			this.semaphore = semaphore;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
		}
		
		public boolean isSuccess() {
			return this.success;
		}

		@Override
		public void operationComplete(IoFuture future) {
			String connectKey = StringUtil.concat(SimpleClient.TIME_OUT_MAP_KEY, remoteHost, Constant.COLON, remotePort);
			try {
				IoSession session = future.getSession();
				// Tell caller we can write message.
				// connectedCond.signal();
				if (session != null && session.isConnected()) {
					if (logger.isInfoEnabled()) {
						logger.info("Client connected to {}:{}", remoteHost, remotePort);
					}
					success = true;
					this.client.session = session;
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("Client failed connect to {}:{}", remoteHost,
								remotePort);
					}
					success = false;
				}
			} catch (Throwable e) {
				logger.warn("Failed to connect to server: {}:{}", remoteHost, remotePort);
				if (logger.isDebugEnabled()) {
					logger.debug("Exception:", e);
				}				
				success = false;
			} finally {
				this.semaphore.release();
				Jedis jedis = JedisFactory.getJedis();
				if ( success ) {
					//Remove it from timeout table if exist
					jedis.del(connectKey);
				} else {
					//Put the result into timeout table
					int count = 0;
					long nextTimeout = 0;
					if ( jedis.exists(connectKey) ) {
						String countStr = jedis.hget(connectKey, COUNT_FIELD);
						String nextTimeoutStr = jedis.hget(connectKey, NEXTTIMEOUT_FIELD);
						count = StringUtil.toInt(countStr, 0);
						try {
							nextTimeout = Long.parseLong(nextTimeoutStr);
						} catch (NumberFormatException e) {
						}
					}
					nextTimeout = SimpleClient.calcNextTimeout(count, System.currentTimeMillis());
					Pipeline pipeline = jedis.pipelined();
					pipeline.hset(connectKey, COUNT_FIELD, String.valueOf(count+1));
					pipeline.hset(connectKey, NEXTTIMEOUT_FIELD, String.valueOf(nextTimeout));
					pipeline.expireAt(connectKey, nextTimeout/1000);
					pipeline.sync();
				}
			}
		}
	}
 
	/**
	 * Get the underlying IoSession object if connected. 
	 * Otherwise, return null.
	 * 
	 * @return
	 */
	public IoSession getIoSession() {
		if ( this.session != null && this.session.isConnected() ) {
			return this.session;
		}
		return null;
	}
}
