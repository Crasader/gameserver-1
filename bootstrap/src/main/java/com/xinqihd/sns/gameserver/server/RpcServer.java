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
public class RpcServer extends CommonServer {

	private static final Log log = LogFactory.getLog(RpcServer.class);
	
	public static final String PROTOCOL_CODEC = "com.xinqihd.sns.gameserver.transport.rpc.RpcProtocolCodecFilter";
	public static final String PROTOCOL_HANDLER = "com.xinqihd.sns.gameserver.transport.rpc.RpcHandler";
	private static final String SERVER_NAME = "RpcServer";
	
	private static RpcServer instance = new RpcServer();
	
	private RpcServer() {
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
	public static RpcServer getInstance() {
		return instance;
	}
}
