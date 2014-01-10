package com.xinqihd.sns.gameserver.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.bootstrap.CommonServer;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFacade;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFactory;
//import com.xinqihd.sns.gameserver.config.ZooKeeperUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Create a message server used to receive other GameServer's message.
 * If GameServer wants to send a user in other GameServer a message, it will
 * be sent to its MessageServer.
 * @author wangqi
 *
 */
public class MessageServer extends CommonServer {

	private static final Log log = LogFactory.getLog(GameServer.class);
	
	public static final String PROTOCOL_CODEC = "com.xinqihd.sns.gameserver.session.MessageProtocolRawCodecFilter";
	public static final String PROTOCOL_HANDLER = "com.xinqihd.sns.gameserver.session.MessageTransferHandler";
	private static final String SERVER_NAME = "MessageServer";
	
	private static MessageServer instance = new MessageServer();
	
	private String zooHostRoot = null;
	
	private MessageServer() {
		super(PROTOCOL_CODEC, PROTOCOL_HANDLER, SERVER_NAME);
	};
	
	/**
	 * Get the singleton object.
	 * @return
	 */
	public static MessageServer getInstance() {
		return instance;
	}

	/**
	 * Start the message server and register it with ZooKeeper if availabe.
	 */
	@Override
	public void startServer(String bindAddr, int port) {
		/*
		super.startServer(bindAddr, port);
		
		try {
			ZooKeeperFacade zooKeeperFacade = ZooKeeperFactory.getInstance();
			if ( zooKeeperFacade != null ) {
				String hostName = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_HOSTNAME);
				String messageServerRoot = GlobalConfig.getInstance().getStringProperty(GlobalConfig.RUNTIME_MESSAGE_LIST_ROOT);
				zooHostRoot = StringUtil.concat(messageServerRoot, Constant.PATH_SEP, Constant.LIST, 
						Constant.PATH_SEP, hostName, Constant.COLON, String.valueOf(port));
				//ZooKeeperUtil.createZNode(zooHostRoot, null, zooKeeperFacade.getZooKeeper());
				log.info("Register MessageServer at: " + zooHostRoot);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		*/
	}

	/**
	 * 
	 */
	@Override
	public void stopServer() {
		/*
		super.stopServer();
		try {
			ZooKeeperFacade zooKeeperFacade = ZooKeeperFactory.getInstance();
			if ( zooKeeperFacade != null ) {
				ZooKeeperUtil.deleteZNode(zooHostRoot, zooKeeperFacade.getZooKeeper());
				log.info("Deregister MessageServer from: " + zooHostRoot);
			}
		} catch (Exception e) {
			log.error(e, e);
		}
		*/
	}
	
}
