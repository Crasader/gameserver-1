package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattleReward.BceBattleReward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceOnlineRewardHandler is used for protocol OnlineReward 
 * @author wangqi
 *
 */
public class BceBattleRewardHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBattleRewardHandler.class);
	
	private static final BceBattleRewardHandler instance = new BceBattleRewardHandler();
	
	private BceBattleRewardHandler() {
		super();
	}

	public static BceBattleRewardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBattleReward");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBattleReward payload = (BceBattleReward)request.payload;
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getBattleManager().getBattleReward(user, payload.getSlot());
		
		//StatClient.getIntance().sendDataToStatServer(user, StatAction.BattleReward, payload.getSlot());
	}
	
	
}
