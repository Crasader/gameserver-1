package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceBattleStageReadyHandler is used for protocol BattleStageReady 
 * @author wangqi
 *
 */
public class BceCloseBagHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceCloseBagHandler.class);
	
	private static final BceCloseBagHandler instance = new BceCloseBagHandler();
	
	private BceCloseBagHandler() {
		super();
	}

	public static BceCloseBagHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		logger.debug("->BceCloseBag");
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		UserManager.getInstance().saveUserBag(user, false);
		user.updatePowerRanking();
		
		StatClient.getIntance().sendDataToStatServer(
				user, StatAction.CloseBag);
	}
	
	
}
