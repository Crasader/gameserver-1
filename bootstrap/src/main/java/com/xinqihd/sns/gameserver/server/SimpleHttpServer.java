package com.xinqihd.sns.gameserver.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.xinqihd.sns.gameserver.bootstrap.Bootstrap;
import com.xinqihd.sns.gameserver.bootstrap.CommonServer;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFacade;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFactory;

/**
 * This simple http server only support GET method without chunk-encoding.
 * It's very simple so it is also very fast.
 * Its main purpose is for serving config data for clients.
 * 
 * It can be called from GameSever's bootstrap or it can run as a standalone server.
 * 
 * @author wangqi
 *
 */
public class SimpleHttpServer extends CommonServer {
	
	static final Log log = LogFactory.getLog(SimpleHttpServer.class);
	
	private static final String PROTOCOL_CODEC = "com.xinqihd.sns.gameserver.transport.http.HttpProtocolCodecFiler";
	private static final String PROTOCOL_HANDLER = "com.xinqihd.sns.gameserver.transport.http.HttpGameHandler";
	private static final String SERVER_NAME = "HttpServer";
	
	private static SimpleHttpServer instance  = new SimpleHttpServer();
	
	private SimpleHttpServer() {
		super(PROTOCOL_CODEC, PROTOCOL_HANDLER, SERVER_NAME);
	}
	
	/**
	 * Get a singleton instance.
	 * @return
	 */
	public static SimpleHttpServer getInstance() {
		return instance;
	}
	
}
