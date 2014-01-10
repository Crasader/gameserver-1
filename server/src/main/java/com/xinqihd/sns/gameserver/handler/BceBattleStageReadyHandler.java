package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattleStageReady.BceBattleStageReady;
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
public class BceBattleStageReadyHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBattleStageReadyHandler.class);
	
	private static final BceBattleStageReadyHandler instance = new BceBattleStageReadyHandler();
	
	private BceBattleStageReadyHandler() {
		super();
	}

	public static BceBattleStageReadyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBattleStageReady");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBattleStageReady stageReady = (BceBattleStageReady)request.payload;
		GameContext.getInstance().getBattleManager().stageReady(sessionKey, stageReady);

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		StatClient.getIntance().sendDataToStatServer(user, StatAction.BattleStageReady);
	}
	
	
}
