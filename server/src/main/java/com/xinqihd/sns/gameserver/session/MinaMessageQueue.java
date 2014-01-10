package com.xinqihd.sns.gameserver.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.ai.SessionAIMessage;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * MinaMessageQueue is a MessageQueue implementation for Mina framework.
 * It can asynchrously write the message back to user. 
 * 
 * @author wangqi
 *
 */
public class MinaMessageQueue extends IoHandlerAdapter implements MessageQueue {
	
	private static final Logger logger = LoggerFactory.getLogger(MinaMessageQueue.class);
	
	private static final MessageQueue instance = new MinaMessageQueue();
	
	/**
	 * The underlying message quee.
	 */
	private LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	
	/**
	 * The resource lock for this queue.
	 */
	private Lock lock = new ReentrantLock();
	
	private int numberOfCpuCores = 1;//Constant.CPU_CORES;
	
	private ExecutorService writeService = null;
	
	private String messageServerZkRoot = null;
	
	/**
	 * It is the map to store message client tunnel to other MessageServers.
	 */
	private ConcurrentHashMap<String, MessageClient> messageClientMap = new ConcurrentHashMap<String, MessageClient>(8);
	
	private String localMessageServerId = null;
	
	//private ConfigWatcher configWatcher = new ConfigWatcher();
	
	//Synchronized write
	private boolean syncWrite = false;
	
	/**
	 * Instead create new instance, call the getInstance() to get a Singleton.
	 * 
	 * Note: The ReloadClassLoader will call this constructor so it is public.
	 */
	public MinaMessageQueue() {
		if ( numberOfCpuCores < 1 ) {
			numberOfCpuCores = 2;
		}
		if ( logger.isDebugEnabled() ) {
			logger.debug("Start #" + numberOfCpuCores + " for processing session.write");
		}
		
		//Get zookeeper's root
		messageServerZkRoot = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_MESSAGE_LIST_ROOT);
		localMessageServerId = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_MESSAGE_SERVER_ID);
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("MessageServerListRoot: " + messageServerZkRoot + ", localMessageServerId: " + localMessageServerId);
		}
		if ( !syncWrite ) {
			writeService = Executors.newFixedThreadPool(numberOfCpuCores);
			for ( int i=0; i<numberOfCpuCores; i++ ) {
				writeService.submit(new SessionWriteTask(this.messageQueue));
			}
		}
	}
	
	/**
	 * Initialize the MessageQueue
	 */
	@Override
	public final void initQueue() {
		/*
		try {
			ZooKeeperFacade facade = ZooKeeperFactory.getInstance();
			if ( facade != null ) {
				ZooKeeper zooKeeper = facade.getZooKeeper();
				List<String> messageServers = zooKeeper.getChildren(messageServerZkRoot, configWatcher);
				setUpMessageClient(messageServers);
			} else {
				if ( logger.isWarnEnabled() ) {
					logger.warn("ZooKeeper is unavailable");
				}
			}
		} catch (Throwable e) {
			logger.warn("#initQueue exception: {}", e.getMessage());
			logger.debug(e.getMessage(), e);
		}
		*/
	}
	
	/**
	 * Destroy the MessageQueue
	 */
	@Override
	public final void destroyQueue() {
		Set<String> serverKeySet = messageClientMap.keySet();
		for ( String serverKey : serverKeySet ) {
			MessageClient client = messageClientMap.remove(serverKey);
			if ( client != null ) {
				client.disconnectFromServer();
				client = null;
			}
			if ( logger.isDebugEnabled() ) {
				logger.debug("Close down the MessageClient: " + client);
			}
		}
	}
	
	/**
	 * Set up the message server map. Note a server may be added or removed.
	 * @param messageServerIds
	 */
	public final void setUpMessageClient(List<String> messageServerIds) {
		//Remove those obsolete servers
		ArrayList<String> servers = new ArrayList<String>(messageServerIds);
		Collections.sort(servers);
		Set<String> serverKeySet = messageClientMap.keySet();
		for ( String serverKey : serverKeySet ) {
			int index = Collections.binarySearch(servers, serverKey);
			if ( index < 0 ) {
				MessageClient client = messageClientMap.remove(serverKey);
				if ( client != null ) {
					client.disconnectFromServer();
					client = null;
				}
				if ( logger.isDebugEnabled() ) {
					logger.debug("Remove a obsolete MessageServer: " + serverKey);
				}
			}
		}
		
		
		for ( String messageServerId : servers ) {
			if ( localMessageServerId.equals(messageServerId) ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug("Ignore same JVM MessageServer: " + messageServerId);
				}
				continue;
			}
			MessageClient client = messageClientMap.get(messageServerId);
			if ( client == null ) {
				client = setUpSingleMessageClient(messageServerId);
			} // if client == null...
			else {
				if ( logger.isDebugEnabled() ) {
					logger.debug("The connection to MessageServer: " + messageServerId + " is already open");
				}
			}
		}
	}

	/**
	 * Setup a single message client by the given machineid.
	 * @param messageServerId
	 * @return
	 */
	public MessageClient setUpSingleMessageClient(String messageServerId) {
		MessageClient client = null;
		int colonIndex = messageServerId.indexOf(':');
		String host = null;
		int port = 0;
		if ( colonIndex > 0 && colonIndex < messageServerId.length() ) {
			host = messageServerId.substring(0, colonIndex);
			port = StringUtil.toInt(messageServerId.substring(colonIndex+1), 0);
		}
		if ( host != null && host.length()>0 && port > 0 && port < 65536 ) {
			client = new MessageClient(host, port);
			client.connectToServer();
			messageClientMap.put(messageServerId, client);
			if ( logger.isDebugEnabled() ) {
				logger.debug("Add a new MessageServer: " + host+":"+port);
			}
		}
		return client;
	}
		
	/**
	 * Get the Singleton impelmentation.
	 * @return
	 */
	public static MessageQueue getInstance() {
		return instance;
	}
	
	/**
	 * Get an exception when connected.
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		Stat.getInstance().messageServerReceivedFail++;
		logger.warn("#exceptionCaught exception: {}", cause.getMessage());
		if ( logger.isDebugEnabled() ) {
			logger.debug(session.getRemoteAddress().toString(), cause.getCause());
		}
	}
	
	/**
	 * Directly output a message to local JVM
	 */
	@Override
	public void sessionWrite(IoSession session, Object message, SessionKey sessionKey) {
		try {
			lock.lock();
			if ( session == null ) {
				//It is in the JUnit test mode.
				return;
			}
			XinqiMessage xinqiMessage = null;
			IoBuffer ioBuffer = null;
			if ( message instanceof XinqiMessage ) {
				xinqiMessage = (XinqiMessage)message;
				xinqiMessage.index = (int)(System.currentTimeMillis()/1000);
			} else if ( message instanceof MessageLite ) {
				xinqiMessage = new XinqiMessage();
				xinqiMessage.payload = (MessageLite)message;
				xinqiMessage.index = (int)(System.currentTimeMillis()/1000);
			} else if ( message instanceof IoBuffer ) {
				ioBuffer = (IoBuffer)message;
			}
			
			Message msg = new Message();
			if ( sessionKey != null ) {
				User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
				if ( user.isAI() ) {
					SessionAIMessage aiMessage = new SessionAIMessage();
					aiMessage.setMessage(xinqiMessage);
					aiMessage.setSessionKey(sessionKey);
					msg.message = aiMessage;
					msg.session = session;
				} else {
					if ( xinqiMessage != null ) {
						msg.message = xinqiMessage;
					} else {
						msg.message = ioBuffer;
					}
					msg.session = session;
				}
			} else {
				if ( xinqiMessage != null ) {
					msg.message = xinqiMessage;
				} else {
					msg.message = ioBuffer;
				}
				msg.session = session;
			}

			//TODO test the synchronized method to send message
			//wangqi 2012-7-13
			if ( syncWrite ) {
				writeResponse(msg);
			} else {
				messageQueue.put(msg);
				Stat.getInstance().writeQueueSize++;
			}
			
			if ( logger.isDebugEnabled() ) {
				SessionKey key = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
				logger.debug("Queue a write message to session: " + key + ", SocketAddress: " + session.getRemoteAddress());
			}
		} catch ( Exception e) {
			logger.debug("#sessionWrite exception: {}", e);
			logger.warn(e.getMessage());
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Send a message to client.
	 * 
	 * @param sessionKey
	 * @param message
	 */
	@Override
	public void sessionWrite(SessionKey sessionKey, Object message) {
		if ( sessionKey == null || message == null ) return;
			//Find the user at local jvm session table
			IoSession userSession = findSession(sessionKey);
			if ( userSession == null ) {
				//TODO The user exists in a different GameServer.
				//find the machineid for this session.
				//send it thru messageclient.
				try {
					//First check if it is a remote AI user
					boolean isAI = AIManager.getInstance().isAIUser(sessionKey);
					if ( isAI ) {
						XinqiMessage xinqi = null;
						if ( message instanceof XinqiMessage ) {
							xinqi = (XinqiMessage)message;
							xinqi.index = (int)(System.currentTimeMillis()/1000);
						} else if ( message instanceof MessageLite ) {
							MessageLite liteMessage = (MessageLite)message;
							xinqi = new XinqiMessage();
							xinqi.payload = liteMessage;
							xinqi.index = (int)(System.currentTimeMillis()/1000);
						}
						SessionAIMessage aiMessage = new SessionAIMessage();
						aiMessage.setMessage(xinqi);
						aiMessage.setSessionKey(sessionKey);
						
						AIManager.getInstance().sendClientMessageToAIServer(
								aiMessage.getSessionKey(), aiMessage);
					} else {
						String machineId = GameContext.getInstance().findMachineId(sessionKey);
						if ( machineId != null ) {
							MessageClient client = messageClientMap.get(machineId);
							if ( client == null ) {
								if ( logger.isDebugEnabled() ) {
									logger.debug("Not found MessageClient for machineid: " + machineId+", Try to setup it.");
								}
								client = setUpSingleMessageClient(machineId);
							}
							if ( client == null ) {
								return;
							}
							if ( message instanceof XinqiMessage ) {
								SessionRawMessage rawMessage = new SessionRawMessage();
								rawMessage.setSessionkey(sessionKey);
								XinqiMessage xinqi = (XinqiMessage)message;
								xinqi.index = (int)(System.currentTimeMillis()/1000);
								rawMessage.setRawMessage(xinqi.toByteArray());
								
								client.sendMessageToServer(rawMessage);
							} else if ( message instanceof MessageLite ) {
								SessionRawMessage rawMessage = new SessionRawMessage();
								rawMessage.setSessionkey(sessionKey);
								MessageLite liteMessage = (MessageLite)message;
								XinqiMessage xinqi = new XinqiMessage();
								xinqi.payload = liteMessage;
								xinqi.index = (int)(System.currentTimeMillis()/1000);
								rawMessage.setRawMessage(xinqi.toByteArray());
								
								client.sendMessageToServer(rawMessage);
							} else if ( message instanceof SessionRawMessage ){
								client.sendMessageToServer((SessionRawMessage)message);
							} else {
								if ( logger.isDebugEnabled() ) {
									logger.debug("sessionWrite not support message type " + message.getClass());
								}
							}
						} else {
							if ( logger.isDebugEnabled() ) {
								logger.debug("Cannot send message to user because the machineid is null.");
							}
						}
					}
				} catch (Throwable e) {
					logger.warn("Send message exception: {}", e.getMessage());
					logger.debug("Send message exception", e);
				}
			} else {
				if ( logger.isDebugEnabled() ) {
					logger.debug("Send message to this GameServer.");
				}
				sessionWrite(userSession, message, sessionKey);
			}
	}
	
	/**
	 * Find a IoSession by its session key.
	 * @param sessionKey
	 * @return
	 */
	@Override
	public IoSession findSession(SessionKey sessionKey) {
		return GameContext.getInstance().findLocalUserIoSession(sessionKey);
	}
	
	/**
	 * Write the response back
	 * @param message
	 */
	private static void writeResponse(Message message) {
		try {
			//Write the actual message to session.
			if ( message == null || message.message == null ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug("Message to send is null.");
				}
			} else {
				if ( message.message instanceof SessionAIMessage ) {
					//The sendClientMessageToAIServer support reconnect to server.
					SessionAIMessage sessionAIMessage = (SessionAIMessage)message.message;
					AIManager.getInstance().sendClientMessageToAIServer(
							sessionAIMessage.getSessionKey(), sessionAIMessage);
				} else if ( message.session != null && message.session.isConnected() ) {
					message.session.write(message.message);
					if ( message.message instanceof XinqiMessage ) {
						XinqiMessage xinqi = (XinqiMessage)message.message;
						if ( logger.isDebugEnabled() ) {
							logger.debug("Send to client:" + message.session.getRemoteAddress() 
									+ ", message: " + message.message );
						}
						/*
						if ( logger.isInfoEnabled() ) {
							int currentSecond = (int)(System.currentTimeMillis()/1000);
							logger.info("message type:{}, second elapse:{}", xinqi.type, currentSecond-xinqi.index);
						}
						*/
					}
				} else {
					if ( logger.isDebugEnabled() ) {
						logger.debug("Client address: " + message.session.getRemoteAddress() + " has been closed.");
					}
				}
			}
		} catch (Throwable e) {
			logger.warn("SessionWriteTask.run exception: {}", e.getMessage());
			logger.debug(e.getMessage(), e);
		}
	}
	
	/**	
	 * The backgroud session write task
	 * @author wangqi
	 *
	 */
	private static class SessionWriteTask implements Runnable {
		
		private LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
		
		public SessionWriteTask(LinkedBlockingQueue<Message> messageQueue) {
			this.messageQueue = messageQueue;
		}
		
		public void run() {
			for ( ; ; ) {
				try {
					Message message = messageQueue.take();
					writeResponse(message);
					Stat.getInstance().writeQueueSize--;
					Stat.getInstance().gameServerSent++;
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	/**
	 * The internal message for queue.
	 * @author wangqi
	 *
	 */
	private static class Message {
		IoSession session;
		Object message;
	}

	/**
	 * Watch the config path.
	 * @author wangqi
	 *
	 */
	/*
	class ConfigWatcher implements Watcher {
		
		public void process(WatchedEvent event) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Event type="+event.getType()+", path="+event.getPath() + " changed. Read it again. ");
			}
			initQueue();
			if ( event.getType() == Watcher.Event.EventType.NodeDeleted ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug(event.getPath() + " is deleted.");
				}
			}
		}
	}	
	*/
}
