package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.BiblioManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceTakeUserBiblioReward.BceTakeUserBiblioReward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBuyToolHandler is used for protocol BuyTool 
 * @author wangqi
 *
 */
public class BceTakeUserBiblioRewardHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceTakeUserBiblioRewardHandler.class);
	
	private static final BceTakeUserBiblioRewardHandler instance = new BceTakeUserBiblioRewardHandler();
	
	private BceTakeUserBiblioRewardHandler() {
		super();
	}

	public static BceTakeUserBiblioRewardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceTakeUserBiblioRewardHandler");
		}

		XinqiMessage request = (XinqiMessage)message;

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BiblioManager.getInstance().takenUserBiblioReward(user);
	}
	
}
