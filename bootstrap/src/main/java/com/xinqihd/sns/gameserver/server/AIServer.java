package com.xinqihd.sns.gameserver.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.bootstrap.CommonServer;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Starts the Rcp Server instance.
 * 
 * @author wangqi
 *
 */
public class AIServer extends CommonServer {

	private static final Log log = LogFactory.getLog(AIServer.class);
	
	public static final String PROTOCOL_CODEC   = "com.xinqihd.sns.gameserver.ai.AIProtocolCodecFilter";
	public static final String PROTOCOL_HANDLER = "com.xinqihd.sns.gameserver.ai.AIServerHandler";
	private static final String SERVER_NAME = "AIServer";
	
	private static AIServer instance = new AIServer();
	
	private AIServer() {
		super(PROTOCOL_CODEC, PROTOCOL_HANDLER, SERVER_NAME);
	};
	
	@Override
	public void startServer(String bindAddr, int port) {
		super.startServer(bindAddr, port);
	}
	
	/**
	 * Get the singleton object.
	 * @return
	 */
	public static AIServer getInstance() {
		return instance;
	}
}
