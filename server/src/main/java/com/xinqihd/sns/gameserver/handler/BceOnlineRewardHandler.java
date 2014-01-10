package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceOnlineReward.BceOnlineReward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;

/**
 * The BceOnlineRewardHandler is used for protocol OnlineReward 
 * @author wangqi
 *
 */
public class BceOnlineRewardHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceOnlineRewardHandler.class);
	
	private static final BceOnlineRewardHandler instance = new BceOnlineRewardHandler();
	
	private BceOnlineRewardHandler() {
		super();
	}

	public static BceOnlineRewardHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceOnlineReward");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceOnlineReward lotteryData = (BceOnlineReward)request.payload;
		
		RewardManager manager = RewardManager.getInstance();
		manager.pickReward(user, user.getOnlineRewards(), StatAction.ProduceOnlineReward);
		// Mark the reward is taken.
		//boolean success = RewardManager.getInstance().takeOnlineReward(
		//		user, System.currentTimeMillis());
		//if ( success ) {
			//pick the reward
			//manager.pickReward(user, user.getOnlineRewards(), StatAction.ProduceOnlineReward);
			
			// Get the next timeclock reward
			/*
			OnlineReward onlineReward = manager.processOnlineReward(
					user, System.currentTimeMillis());
			if ( onlineReward != null ) {
				BseOnlineReward.Builder onlineRewardBuilder = BseOnlineReward.newBuilder();
				onlineRewardBuilder.setStepID(onlineReward.getStepId());
				onlineRewardBuilder.setRemainTime(onlineReward.getRemainSeconds());
				ArrayList<Reward> rewards = onlineReward.getRewards();
				for ( Reward reward : rewards ) {
					onlineRewardBuilder.addPropID(StringUtil.toInt(reward.getPropId(), 0));
					onlineRewardBuilder.addPropLevel(reward.getPropLevel());
					onlineRewardBuilder.addPropCount(reward.getPropCount());
				}
				onlineRewardBuilder.setType(onlineReward.getType());
				
				XinqiMessage onlineRewardXinqi = new XinqiMessage();
				onlineRewardXinqi.payload = onlineRewardBuilder.build();
				GameContext.getInstance().writeResponse(user.getSessionKey(), onlineRewardXinqi);
				
				StatClient.getIntance().sendDataToStatServer(user, 
						StatAction.OnlineReward, user.getOnlineRewards().size());

			}
			*/
		//}
	}
	
	
}
