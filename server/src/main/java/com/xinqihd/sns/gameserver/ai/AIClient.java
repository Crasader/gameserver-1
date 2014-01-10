package com.xinqihd.sns.gameserver.ai;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.util.SimpleClient;

/**
 * Internal AI message client handler
 * @author wangqi
 *
 */
public class AIClient {
	
	private static final Logger logger = LoggerFactory.getLogger(AIClient.class);
	
	public static final SessionAIMessage HEART_BEAT_MSG = new SessionAIMessage();
	
	private static final StatIoFilterListener statListener = new StatIoFilterListener();
	
	//The client filter
	private AIProtocolCodecFilter filter = new AIProtocolCodecFilter();

	//The AI server client pool
	private SimpleClient aiClient = null;
	
	private IoHandler aiHandler = new AIClientHandler();
	
	private String remoteHost;
	
	private int remotePort;

	public AIClient(String remoteHost, int remotePort) {
		this(remoteHost, remotePort, Constant.CPU_CORES);
	}
	
	public AIClient(String remoteHost, int remotePort, int count) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		/*
		String aiServerIdStr = GlobalConfig.getInstance().getStringProperty(
				GlobalConfig.RUNTIME_AI_SERVERID);
		if ( aiServerIdStr == null ) {
			aiServerIdStr = OtherUtil.getHostName()+":0";
		}
		String remoteHost = "localhost";
		int remotePort = 0;
		String[] remoteStrs = StringUtil.splitMachineId(aiServerIdStr);
		if ( remoteStrs != null ) {
			remoteHost = remoteStrs[0];
			remotePort = StringUtil.toInt(remoteStrs[1], 0);
		}
		*/
		
		int heartBeatSecond = GlobalConfig.getInstance().getIntProperty("message.heartbeat.second");
		if ( logger.isDebugEnabled() ) {
			logger.debug("heartBeatSecond : " + heartBeatSecond);
		}
		aiClient = new SimpleClient(filter, aiHandler, remoteHost, remotePort);
		aiClient.setStatListener(statListener);
		aiClient.setHeartBeatSecond(heartBeatSecond);
		aiClient.setHeartBeatMessage(HEART_BEAT_MSG);
		boolean success = aiClient.connectToServer();
		if ( success ) {
			logger.debug("AI module is connected to {}:{} ", remoteHost, remotePort);
		} else {
			logger.warn("AI module failed to connect to {}:{} ", remoteHost, remotePort);
		}
	}
	
	// -------------------------------------------------- Client Pool
	
	/**
	 * Connect to remote message server.
	 * 
	 * @return
	 */
	public boolean connectToServer() {
		return aiClient.connectToServer();
	}

	/**
	 * Disconnect from remote message server and release resources.
	 * 
	 * @return
	 */
	public boolean disconnectFromServer() {
		return aiClient.connectToServer();
	}
	
	/**
	 * Get the underlying IoSession object if connected. 
	 * Otherwise, return null.
	 * 
	 * @return
	 */
	public IoSession getIoSession() {
		IoSession session = this.aiClient.getIoSession();
		if ( session == null ) {
			boolean success = this.aiClient.connectToServer();
			if ( !success ) {
				logger.debug("#getIoSession: Failed to get the IoSession");
			} else {
				session = this.aiClient.getIoSession();
			}
		}
		return session;
	}

	/**
	 * Send a message to server.
	 * 
	 * @param msg
	 */
	public WriteFuture sendMessageToServer(SessionAIMessage msg) {
		return aiClient.sendMessageToServer(msg);
	}
	
	
	static class StatIoFilterListener implements IoFutureListener<IoFuture> {
		@Override
		public void operationComplete(IoFuture future) {
			Stat.getInstance().aiMessageSent++;
		}
	}
}
