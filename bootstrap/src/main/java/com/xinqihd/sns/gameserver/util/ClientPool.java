package com.xinqihd.sns.gameserver.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;

/**
 * Provide a ClientPool implementation.
 * 
 * @author wangqi
 *
 */
public class ClientPool implements Client {
	
	private BlockingQueue<Client> clientQueue = new LinkedBlockingQueue<Client>();
	
	private int connectTimeout = Client.CONNECT_TIMEOUT;
	
	private Object heartbeatMessage;
	
	private int heartbeatSecond = Client.HEARTBEAT_MILLIS;
	
	private IoFutureListener<IoFuture> statListener;
	
	private int count = 0;
	
	/**
	 * Create a pool with the number of 'count' connections underhood.
	 * To avoid multi-thread access, I suggest provide a distinguish 
	 * IoHandler for each client. So the constructor contains an array
	 * of IoHandlers. They should be not null and has the same
	 * number with count. The IoFilter is usually thread-safe.
	 * 
	 * @param filters
	 * @param handlers
	 * @param remoteHost
	 * @param remotePort
	 * @param count
	 */
	public ClientPool(IoFilter filter, IoHandler[] handlers, 
			String remoteHost, int remotePort, int count) {
		
		this.count = count;
		for ( int i=0; i<count; i++ ) {
			SimpleClient client = new SimpleClient(filter, handlers[i], remoteHost,
					remotePort);
			clientQueue.offer(client);
		}
	}
	
	/**
	 * Initialize all connections under hood. Call this method after 
	 * all settings are done.
	 */
	public void initQueue() {
		SimpleClient[] clients = clientQueue.toArray(new SimpleClient[this.count]);
		for ( SimpleClient client : clients ) {
			client.setConnectTimeout(this.connectTimeout);
			client.setHeartBeatMessage(heartbeatMessage);
			client.setHeartBeatSecond(heartbeatSecond);
			client.setStatListener(statListener);
			client.setResourceProtected(false);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getConnectTimeout()
	 */
	@Override
	public int getConnectTimeout() {
		return connectTimeout;
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
		return heartbeatMessage;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setHeartBeatMessage(com.google.protobuf.Message)
	 */
	@Override
	public void setHeartBeatMessage(Object heartBeatMessage) {
		this.heartbeatMessage = heartBeatMessage;
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

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#getHeartBeatMillis()
	 */
	@Override
	public int getHeartBeatSecond() {
		return heartbeatSecond;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#setHeartBeatMillis(int)
	 */
	@Override
	public void setHeartBeatSecond(int heartBeatMillis) {
		this.heartbeatSecond = heartBeatMillis;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#connectToServer()
	 */
	@Override
	public boolean connectToServer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#disconnectFromServer()
	 */
	@Override
	public boolean disconnectFromServer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.util.Client#sendMessageToServer(com.google.protobuf.Message)
	 */
	@Override
	public WriteFuture sendMessageToServer(Object msg) {
		Client client = null;
		try {
			client = clientQueue.take();
			WriteFuture future = client.sendMessageToServer(msg);
			return future;
		} catch (InterruptedException e) {
		} finally {
			clientQueue.offer(client);
		}
		return null;
	}
}
