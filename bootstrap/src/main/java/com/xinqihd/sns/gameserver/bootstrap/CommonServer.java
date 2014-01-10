package com.xinqihd.sns.gameserver.bootstrap;

import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;

/**
 * The common server instance.
 * @author wangqi
 *
 */
public class CommonServer implements Server {
	
	private static final Log log = LogFactory.getLog(CommonServer.class);

	private String protocolCodecFilterClassName = null;
	private String protocolHandlerClassName = null;
	private NioSocketAcceptor acceptor = null;
	private long serverStartMillis = 0;
	private ReloadProtocolCodecFilter reloadHandler = null;
	private String serverName = null;

	protected InetSocketAddress bindAddress = null;
	
	/**
	 * Construct the instance.
	 * @param protocolCodecFilterClassName
	 * @param protocolHandlerClassName
	 */
	protected CommonServer(
			String protocolCodecFilterClassName, String protocolHandlerClassName, String serverName) {
		
		this.protocolCodecFilterClassName = protocolCodecFilterClassName;
		this.protocolHandlerClassName = protocolHandlerClassName;
		this.serverName = serverName;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#reload()
	 */
	@Override
	public void reload() {
		if ( reloadHandler != null ) {
			reloadHandler.reload();
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#startServer(java.lang.String, int)
	 */
	@Override
	public void startServer(String bindAddr, int port) {
		try {
			acceptor = new NioSocketAcceptor();
			acceptor.getSessionConfig().setTcpNoDelay(true);
			int sendBuf = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.tcp_sendbuf);
			if ( sendBuf <= 0 ) {
				sendBuf = 1024;
			}
			log.info("Set tcp SO_SNDBUF to " + sendBuf);
			acceptor.getSessionConfig().setSendBufferSize(sendBuf);
			int recvBuf = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.tcp_recvbuf);
			if ( recvBuf <= 0 ) {
				recvBuf = 1024;
			}
			log.info("Set tcp SO_RECVBUF to " + recvBuf);
			acceptor.getSessionConfig().setReceiveBufferSize(recvBuf);
			int backlog = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.tcp_backlog);
			if ( backlog <= 0 ) {
				backlog = 10000;
			}
			log.info("Set tcp SO_BACKLOG to " + backlog);
			acceptor.setBacklog(10000);
			acceptor.getSessionConfig().setKeepAlive(true);
			
			reloadHandler = new ReloadProtocolCodecFilter(
					protocolCodecFilterClassName, protocolHandlerClassName);
			
			acceptor.getFilterChain().addLast("protocol", reloadHandler);
			acceptor.setHandler(reloadHandler);
			
			//Bind and start to accept incoming connections
			//TODO change the port to external configuration.
			if ( bindAddr != null ) {
				bindAddress = new InetSocketAddress(bindAddr, port);
			} else {
				bindAddress = new InetSocketAddress(port);
			}
			//If the user are idle over 10 minutes, call the session idle method.
			int idleTime = GlobalConfig.getInstance().
					getIntProperty("common.server.idle.seconds");
			acceptor.getSessionConfig().setBothIdleTime(idleTime);
			acceptor.setReuseAddress(true);
			acceptor.bind(bindAddress);
			log.info(serverName+" bind at " + bindAddress);
			serverStartMillis = System.currentTimeMillis();
		} catch (Throwable e) {
			log.error("Failed to start " + serverName, e);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.bootstrap.Server#stopServer()
	 */
	@Override
	public void stopServer() {
		try {
			if ( log.isInfoEnabled() ) {
				Date shutdownDate = new Date();
				if ( acceptor != null ) {
					acceptor.unbind();
					acceptor.dispose();
				}
				acceptor = null;
				log.info(serverName + " shutdown at " + shutdownDate + ". It runs for " + 
				(shutdownDate.getTime()-serverStartMillis)/1000 + " seconds");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}