package com.xinqihd.sns.gameserver.util;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

public interface Client {
	
	public static final int CONNECT_TIMEOUT = 1000;
	
	public static final int HEARTBEAT_MILLIS = 60000;

	/**
	 * @return the connectTimeout
	 */
	public abstract int getConnectTimeout();

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public abstract void setConnectTimeout(int connectTimeout);

	/**
	 * @return the heartBeatMessage
	 */
	public abstract Object getHeartBeatMessage();

	/**
	 * @param heartBeatMessage the heartBeatMessage to set
	 */
	public abstract void setHeartBeatMessage(Object heartBeatMessage);

	/**
	 * @return the statListener
	 */
	public abstract IoFutureListener<IoFuture> getStatListener();

	/**
	 * @param statListener the statListener to set
	 */
	public abstract void setStatListener(IoFutureListener<IoFuture> statListener);

	/**
	 * @return the heartBeatSecond
	 */
	public abstract int getHeartBeatSecond();

	/**
	 * @param heartBeatSecond the heartBeatSecond to set
	 */
	public abstract void setHeartBeatSecond(int heartBeatSecond);

	/**
	 * Connect to remote message server.
	 * @return
	 */
	public abstract boolean connectToServer();

	/**
	 * Disconnect from remote message server and release resources.
	 * @return
	 */
	public abstract boolean disconnectFromServer();

	/**
	 * Send a message to server.
	 * @param msg
	 */
	public abstract WriteFuture sendMessageToServer(Object msg);
	
}