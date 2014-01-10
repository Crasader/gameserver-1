package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
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
public class BceTreasureHuntQueryHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTreasureHuntQueryHandler.class);
	
	private static final BceTreasureHuntQueryHandler instance = new BceTreasureHuntQueryHandler();
	
	private BceTreasureHuntQueryHandler() {
		super();
	}

	public static BceTreasureHuntQueryHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTreasureHuntBuy");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceTreasureHuntQuery query = (BceTreasureHuntQuery)request.payload;
		boolean refresh = query.getRefresh();
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		TreasureHuntManager.getInstance().queryTreasureHuntInfo(user, System.currentTimeMillis(), refresh);
	}
	
}
