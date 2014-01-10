package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceTreasureHuntBuy.BceTreasureHuntBuy;
import com.xinqihd.sns.gameserver.proto.XinqiBceTreasureHuntQuery.BceTreasureHuntQuery;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.treasure.TreasureHuntManager;

/**
 * The BceCaishenPray is used for caishen 
 * @author wangqi
 *
 */
public class BceTreasureHuntBuyHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTreasureHuntBuyHandler.class);
	
	private static final BceTreasureHuntBuyHandler instance = new BceTreasureHuntBuyHandler();
	
	private BceTreasureHuntBuyHandler() {
		super();
	}

	public static BceTreasureHuntBuyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTreasureHuntBuy");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTreasureHuntBuy buy = (BceTreasureHuntBuy)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		int mode = buy.getMode();
		TreasureHuntManager.getInstance().doTreasureHunt(user, mode, System.currentTimeMillis());
	}
	
}
