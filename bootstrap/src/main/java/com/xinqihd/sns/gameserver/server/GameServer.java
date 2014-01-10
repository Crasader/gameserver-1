package com.xinqihd.sns.gameserver.server;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.xinqihd.sns.gameserver.bootstrap.CommonServer;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.bootstrap.ReloadProtocolCodecFilter;

/**
 * This is the game server use to listen and process client connection.
 * 
 * @author wangqi
 * 
 */
public class GameServer extends CommonServer {
	
	private static final Log log = LogFactory.getLog(GameServer.class);
	
	public static final String PROTOCOL_CODEC = "com.xinqihd.sns.gameserver.transport.GameProtocolCodecFilter";
	public static final String PROTOCOL_HANDLER = "com.xinqihd.sns.gameserver.transport.GameHandler";
	private static final String SERVER_NAME = "GameServer";
	
	private static GameServer instance = new GameServer();
	
	
	private GameServer() {
		super(PROTOCOL_CODEC, PROTOCOL_HANDLER, SERVER_NAME);
	};
	
	/**
	 * Get the singleton object.
	 * @return
	 */
	public static GameServer getInstance() {
		return instance;
	}

}
