package com.xinqihd.sns.gameserver.bootstrap;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.admin.AdminServer;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFacade;
//import com.xinqihd.sns.gameserver.config.ZooKeeperFactory;
import com.xinqihd.sns.gameserver.server.AIServer;
import com.xinqihd.sns.gameserver.server.GameServer;
import com.xinqihd.sns.gameserver.server.MessageServer;
import com.xinqihd.sns.gameserver.server.RpcServer;
import com.xinqihd.sns.gameserver.server.SimpleHttpServer;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * This is the system entry class used to bootstrap the server. 
 * It will: 
 * 1. Set up a hot-swapping enabled classloader.
 * 2. Set up a telnet command line interface 
 * 3. Set up the core protocol buffer handlers 
 * 4. Set up a broadcast group to get aware of other game servers.
 * 
 * TODO Add telnet command line 'status' to report status.
 * TODO Add telnet command line 'reload' to reload class.
 * TODO Add telnet command line 'shutdown' to shutdown server.
 * TODO Add telnet command line 'maintain' to maintain server.
 * 
 * @author wangqi
 * 
 */
public class Bootstrap {
	
	private static final Log log = LogFactory.getLog(Bootstrap.class);
	
	private static final String LIFECYCLE_CLASS = "com.xinqihd.sns.gameserver.GameServerLifecycle";
	
	//The global configuration data.
	public static HashMap<String,String> SYS_CONFIG = new HashMap<String, String>();
	
	private static long serverStartMillis = System.currentTimeMillis();
	
	private URL[] classpathURL = null;
	
	//private ZooKeeperFacade zooKeeper = null; 
	
	private static Bootstrap instance = new Bootstrap();
	
	private ServerLifecycle lifecycle = null; 
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				Bootstrap.getInstance().shutdownServer();
			}
		});
	}
	
	private Bootstrap() {};
	
	/**
	 * Get the singleton object.
	 * @return
	 */
	public static Bootstrap getInstance() {
		return instance;
	}
	
	/**
	 * Reload the class loader.
	 */
	public void reload() {
		ReloadClassLoader.newClassloader(classpathURL);
	}

	
	/**
	 * Shutdown the server and release resources.
	 */
	public void shutdownServer() {
		try {
			//Notify lifecycle to stop
			if ( lifecycle != null ) {
				lifecycle.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//Shutdown GameServer
			GameServer.getInstance().stopServer();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			//Shutdown GameServer
			MessageServer.getInstance().stopServer();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			//Shutdown the http server
			SimpleHttpServer.getInstance().stopServer();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			//Shutdown the admin server
			//AdminServer.getInstance().stopServer();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Manage the system exit process.
	 * For Netty to exit, there are critical resources need to be recycled.
	 * @param msg
	 */
	public static void exit(String msg, int code) {
		log.info(msg);
		System.exit(code);
	}
	

	/**
	 * Main method to bootstrap the system.
	 * 
	 * Command line arguments:
	 * Form1, in production, use the ZooKeeper to start the process.
	 * 	> Bootstrap -t zoo -z <connect string>  -r [zkpath]
	 * Form2, in test phase, use the command line.
	 *  > Bootstrap -t test -h [bind_host> -p [bind_port] -a [admin port] -u [url for classes or java] -u [] ...
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//Set Netty's defaut logger.
		Bootstrap gameBootstrap = Bootstrap.getInstance();
		
		String host = null;
		String httpHost = null;
		int gamePort = 3443;
		int messagePort = gamePort+1;
		int rpcPort = messagePort+1;
		int aiPort = rpcPort+1;
		int adminPort = aiPort+1;
		int httpPort = adminPort+1;
		//String zkConnectString = null;
		String scriptFilePath = null;
		String deployDataPath = null;
		String runtimeHost = null;
		
		ArrayList<URL> urlList = new ArrayList<URL>(5);
		
		if ( args != null && args.length > 0 ) {
			for ( int i=0; i<args.length; i++ ) {
				if ( "-h".equals(args[i]) ) {
					host = args[i+1];
				} else if ( "-p".equals(args[i]) ) {
					gamePort = Integer.parseInt(args[i+1]);
				} else if ( "-http".equals(args[i]) ) {
					httpPort = Integer.parseInt(args[i+1]);
				} else if ( "-s".equals(args[i]) ) {
					scriptFilePath = args[i+1];
				} else if ( "-d".equals(args[i]) ) {
					deployDataPath = args[i+1];
				} else if ( "-u".equals(args[i]) ) {
					String urlStr = args[i+1];
					if ( urlStr.startsWith("http://") ) {
						urlList.add(new URL(urlStr));
					} else {
						File file = null;
						if ( urlStr.charAt(0) != File.separatorChar ) { 
							file = new File(System.getProperty("user.dir"), urlStr);
						} else {
							file = new File(urlStr);
						}
						log.info("classpathurl: " + file.toURL());
						urlList.add(file.toURL());
					}
				} else if ( "-z".equals(args[i]) ) {
					//zkConnectString = args[i+1];
				} else if ( "-host".equals(args[i]) ) {
					runtimeHost = args[i+1];
				} else if ( "-httphost".equals(args[i]) ) {
					httpHost = args[i+1];
				}
			}
		} else {
			System.err.println(
					   "Command line arguments: \n"+
						 "  Form1, in production, use the ZooKeeper to start the process.\n"+
						 " 	> Bootstrap -t zoo -z <connectString> -r [zookeeper path]\n"+
						 "  Note: If you want to different zookeeper root, use <host>:<ip>/zkroot format." +
						 "  Form2, in test phase, use the command line.\n"+
						 "  > Bootstrap -t test -h [bind_host> -s [script dir] -p [bind_port] -a [admin port] -c [config port] -z <connectString> -u [url for classes or java] -u [] ..."
					);
			System.exit(0);
		}
		if ( deployDataPath == null ) {
			System.err.println(
					"A new parameter '-d' should be set for the 'deploy' dir. It is in under the path 'babywar/server/deploy' in svn."
					);
			System.exit(0);
		}
		gameBootstrap.classpathURL = urlList.toArray(new URL[urlList.size()]);
		
		//Initialize the ZooKeeper connection.
		/*
		if ( zkConnectString == null ) {
			System.err.println("ZooKeeper's ConnectString should be specified by -z parameter");
			System.exit(-1);
		}
		*/
				
		messagePort = gamePort+1;
		rpcPort = messagePort+1;
		aiPort = rpcPort+1;
		adminPort = aiPort+1;
		
		log.info("Binding          host: " + host);
		log.info("Http             host: " + httpHost);
		log.info("GameServer       port: " + gamePort);
		log.info("MessageServer    port: " + messagePort);
		//log.info("RpcServer        port: " + rpcPort);
		log.info("AIServer         port: " + aiPort);
		log.info("GameAdmin        port: " + adminPort);
		log.info("Http             port: " + httpPort);
		
		String hostName = null;
		if ( runtimeHost != null ) {
			hostName = runtimeHost;
			log.info("Runtime Host: " + runtimeHost);
		} else {
			hostName = OtherUtil.getHostName();
		}
		
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_HOSTNAME, hostName);
		
		//Get message server's root
		String zkRoot = GlobalConfig.getInstance().getStringProperty("zookeeper.root");
		StringBuilder buf = new StringBuilder(30);
		buf.append(zkRoot).append(GlobalConfig.getInstance().getStringProperty("zookeeper.message.root"));
		String messageServerRoot = buf.toString();
		
		//Set global config runtime properties.
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_MESSAGE_LIST_ROOT, 
				messageServerRoot);
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, 
				StringUtil.concat(hostName, Constant.COLON, messagePort));
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_GAME_SERVERID, 
				StringUtil.concat(hostName, Constant.COLON, gamePort));
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_RPC_SERVERID, 
				StringUtil.concat(hostName, Constant.COLON, rpcPort));
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_AI_SERVERID, 
				StringUtil.concat(hostName, Constant.COLON, aiPort));
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_HTTP_SERVERID, 
				StringUtil.concat(httpHost, Constant.COLON, httpPort));
		
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_TMP_DIR, 
				StringUtil.concat(System.getProperty("java.io.tmpdir"), Constant.PATH_SEP, hostName, Constant.UNDERLINE, gamePort));
		
		if ( scriptFilePath != null ) {
			GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, scriptFilePath);
		} else {
			log.warn("YOU MAY FORGET THE -s [script dir or url] ARGUMENT!!!. Use src/main/script");
			GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		}
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, deployDataPath);
		
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.chat_word_file, deployDataPath+"/word.txt");
		
		log.info("Server's hostname : " + hostName);
		
		ReloadClassLoader reloader = ReloadClassLoader.newClassloader(gameBootstrap.classpathURL);
		
		//Startup the admin server.
		//AdminServer.getInstance().startServer(host, adminPort);
		//Startup the Http server.
		SimpleHttpServer.getInstance().startServer(host, httpPort);
		//Startup the MessageServer
		MessageServer.getInstance().startServer(host, messagePort);
		//Startup the RPC server
		//RpcServer.getInstance().startServer(host, rpcPort);
		//Startup the AI server
		AIServer.getInstance().startServer(host, aiPort);
		
		gameBootstrap.lifecycle = (ServerLifecycle)(reloader.loadClass(LIFECYCLE_CLASS).newInstance());
		gameBootstrap.lifecycle.init();
		
		//Startup the GameServer server.
		GameServer.getInstance().startServer(host, gamePort);
	}

}
