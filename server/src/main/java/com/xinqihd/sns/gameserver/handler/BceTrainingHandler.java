package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceTraining.BceTraining;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceRoleUseToolHandler is used for protocol RoleUseTool 
 * @author wangqi
 *
 */
public class BceTrainingHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTrainingHandler.class);
	
	private static final BceTrainingHandler instance = new BceTrainingHandler();
	
	private BceTrainingHandler() {
		super();
	}

	public static BceTrainingHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTraining");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTraining training = (BceTraining)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		StatClient.getIntance().sendDataToStatServer(
				user, StatAction.Training, training.getStep());

	}
	
	
}
