package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBossTakeReward.BceBossTakeReward;
import com.xinqihd.sns.gameserver.proto.XinqiBceMailRead.BceMailRead;
import com.xinqihd.sns.gameserver.proto.XinqiBseBossTakeReward.BseBossTakeReward;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailRead.BseMailRead;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceBossTakeRewardHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBossTakeRewardHandler.class);
	
	private static final BceBossTakeRewardHandler instance = new BceBossTakeRewardHandler();
	
	private BceBossTakeRewardHandler() {
		super();
	}

	public static BceBossTakeRewardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBossTakeReward");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBossTakeReward takeReward = (BceBossTakeReward)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		String bossId = takeReward.getBossid();
		Boss boss = BossManager.getInstance().getBossInstance(user, bossId);
		boolean success = false;
		if ( boss != null ) {
			BossManager.getInstance().takeBossReward(user, boss);
		} else {
			logger.debug("The boss for id {} is null", bossId);
		}
				
		BseBossTakeReward.Builder bseTakeReward = BseBossTakeReward.newBuilder();
		bseTakeReward.setSuccess(success);
		GameContext.getInstance().writeResponse(sessionKey, bseTakeReward.build());
	}
	
}
