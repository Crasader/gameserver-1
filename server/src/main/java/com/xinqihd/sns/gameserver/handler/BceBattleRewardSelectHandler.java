package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattleRewardSelect.BceBattleRewardSelect;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceOnlineRewardHandler is used for protocol OnlineReward 
 * @author wangqi
 *
 */
public class BceBattleRewardSelectHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBattleRewardSelectHandler.class);
	
	private static final BceBattleRewardSelectHandler instance = new BceBattleRewardSelectHandler();
	
	private BceBattleRewardSelectHandler() {
		super();
	}

	public static BceBattleRewardSelectHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBattleReward");
		}
				
		XinqiMessage request = (XinqiMessage)message;
		BceBattleRewardSelect payload = (BceBattleRewardSelect)request.payload;
		int count = payload.getSlotIndexCount();
		int[] slotIndex = new int[count];
		for ( int i=0; i<count; i++ ) {
			//Client slot index begins from 1
			slotIndex[i] = payload.getSlotIndex(i) - 1;
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getBattleManager().pickReward(user, slotIndex);

	}
	
	
}
