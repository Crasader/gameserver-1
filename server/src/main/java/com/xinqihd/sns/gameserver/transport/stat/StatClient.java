package com.xinqihd.sns.gameserver.transport.stat;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.stat.JavaCStatAction.CStatAction;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.ClientPool;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The MessageClient will transport messages to other GameServer
 * 
 * @author wangqi
 * 
 */
public class StatClient {

	public static final int CONNECT_TIMEOUT = 1000;

	private static final Logger logger = LoggerFactory.getLogger(StatClient.class);
	//private static final Logger STAT_LOGGER = LoggerFactory.getLogger("stat");

	private static final StatProtocolCodecFilter statListener = new StatProtocolCodecFilter();
	
	private static StatClient instance = new StatClient();

	private StatProtocolCodecFilter filter = new StatProtocolCodecFilter();
	
	private ArrayBlockingQueue<XinqiMessage> messageQueue = new ArrayBlockingQueue<XinqiMessage>(200);

	private IoHandler[] handlers = null;

	private ClientPool clientPool = null;
	
	private String remoteHost;
	
	private int remotePort;
	
	private boolean statEnabled = false;
	

	public StatClient() {
		statEnabled = Boolean.parseBoolean(GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.stat_enabled));
		
		if ( statEnabled ) {
			String statHost = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.stat_host);
			int statPort = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.stat_port);
			
			this.remoteHost = statHost;
			this.remotePort = statPort;
			
			handlers = new MessageIoHandler[Constant.CPU_CORES];
			for ( int i=0; i<handlers.length; i++ ) {
				handlers[i] = new MessageIoHandler();
			}
			clientPool = new ClientPool(filter, handlers, remoteHost, remotePort, Constant.CPU_CORES);
			clientPool.initQueue();
			
			Worker worker = new Worker();
			worker.start();
		} else {
			logger.info("The StatServer connection is disabled");
		}
	}
	
	public final static StatClient getIntance() {
		return instance;
	}

	/**
	 * Connect to remote message server.
	 * 
	 * @return
	 */
	public final boolean connectToServer() {
		return clientPool.connectToServer();
	}

	/**
	 * Disconnect from remote message server and release resources.
	 * 
	 * @return
	 */
	public final boolean disconnectFromServer() {
		return clientPool.connectToServer();
	}
	
	/**
	 * Send the client to server.
	 * @param action
	 * @return
	 */
	public final WriteFuture sendDataToStatServer(User user, StatAction action) {
		return sendDataToStatServer(user, action, null);
	}

	/**
	 * Send 
	 * @param user
	 * @param action
	 * @param params
	 * @return
	 */
	public final WriteFuture sendDataToStatServer(BasicUser user, StatAction action, Object... params) {
		String uuid = Constant.EMPTY;
		if ( user != null && user.getUuid() != null ) {
			uuid = user.getUuid();
		}
		return sendDataToStatServer(user, uuid, action, params);
	}
		
	/**
	 * Send a message to server.
	 * 
	 * @param msg
	 */
	public final WriteFuture sendDataToStatServer(BasicUser user, String uuid, StatAction action, Object... params) {
		if ( statEnabled ) {
			try {
				CStatAction.Builder builder = CStatAction.newBuilder();
				builder.setAction(action.toString());
				builder.setTimestamp(System.currentTimeMillis());
				if ( user != null ) {
					if ( user.getRoleName() != null ) {
						builder.setRolename(user.getRoleName());
					}
				}
				builder.setUuid(uuid);
				if ( params != null && params.length > 0 ) {
					for ( Object param : params ) {
						if ( param != null ) {
							String p = param.toString();
							if ( p.length()>30 ) {
								p = p.substring(0, 30);
							}
							builder.addParams(p);
						} else {
							builder.addParams(Constant.EMPTY);
						}
					}
				}
				XinqiMessage message = new XinqiMessage();
				message.payload = builder.build();
				message.type = MessageToId.messageToId(message.payload);
				boolean success = messageQueue.offer(message);
			} catch (Exception e) {
				logger.debug("#sendDataToStatServer: fail {}", e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Send a message to server.
	 * 
	 * @param msg
	 */
	public final WriteFuture sendDataToStatServer(Account account, String uuid, StatAction action, Object... params) {
		if ( statEnabled ) {
			try {
				CStatAction.Builder builder = CStatAction.newBuilder();
				builder.setAction(action.toString());
				builder.setTimestamp(System.currentTimeMillis());
				if ( account != null ) {
					if ( account.getUserName() != null ) {
						builder.setRolename(account.getUserName());
					}
				}
				builder.setUuid(uuid);
				if ( params != null && params.length > 0 ) {
					for ( Object param : params ) {
						if ( param != null ) {
							String p = param.toString();
							if ( p.length()>30 ) {
								p = p.substring(0, 30);
							}
							builder.addParams(p);
						} else {
							builder.addParams(Constant.EMPTY);
						}
					}
				}
				XinqiMessage message = new XinqiMessage();
				message.payload = builder.build();
				message.type = MessageToId.messageToId(message.payload);
				boolean success = messageQueue.offer(message);
			} catch (Exception e) {
				logger.debug("#sendDataToStatServer: fail {}", e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Check if the stat server is enabled.
	 * @return
	 */
	public final boolean isStatEnabled() {
		return this.statEnabled;
	}

	@Override
	public final String toString() {
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
					logger.debug("Caught Exception: {}", cause.getMessage());
				}
				if (!session.isConnected()) {
					if (logger.isInfoEnabled()) {
						logger.info("reconnect to stat server due to closed session");
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
				logger.info("stat message received");
			} catch (Exception e) {
				logger.error("Caught Exception: {}", e.getMessage());
			}
		}
	}

	public class Worker extends Thread {
		
		public Worker() {
			this.setName("StatClientWorker");
		}
		
		public void run() {
			while ( true ) {
				XinqiMessage message;
				try {
					message = messageQueue.take();
					clientPool.sendMessageToServer(message);
				} catch (Exception e) {
				}
			}
		}
	}
}
