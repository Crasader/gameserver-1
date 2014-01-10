package com.xinqihd.sns.gameserver.handler;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseDailyMark.BseDailyMark;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.reward.DailyMarkReward;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceDailyMarkHandler is used for protocol DailyMark 
 * @author wangqi
 *
 */
public class BceDailyMarkHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceDailyMarkHandler.class);
	
	private static final BceDailyMarkHandler instance = new BceDailyMarkHandler();
	
	private BceDailyMarkHandler() {
		super();
	}

	public static BceDailyMarkHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceDailyMark");
		}
		
		long currentTimeMillis = System.currentTimeMillis();
		//Get the daily reward
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		//Take the reward.
		boolean success = RewardManager.getInstance().
				takeDailyMarkReward(user, currentTimeMillis);
		DailyMarkReward dailyMarkReward = RewardManager.getInstance().
				processDailyMarkReward(user, currentTimeMillis);
		
		if ( success ) {
			//Send BseDailyMark
			BseDailyMark.Builder mark = BseDailyMark.newBuilder();
			mark.setDate(dailyMarkReward.getDayOfMonth());
			mark.addAllMarkArr(dailyMarkReward.getMarkArray());
			mark.setDaycount(dailyMarkReward.getTotalCount());
			mark.setResult(true);
			
			//Put it into the bag.
			boolean changed = false;
			List<Reward> rewards = dailyMarkReward.getDailyMark().getRewards();
			changed = RewardManager.getInstance().pickReward(user, rewards,
					StatAction.ProduceDailyMark);
			if ( !changed ) {
				//Save user's new remainTimes
				GameContext.getInstance().getUserManager().saveUser(user, false);
				
				//Notify client user's role data is changed.
				BseRoleInfo roleInfo = user.toBseRoleInfo();
				GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
			}
			logger.debug("User {} take the daily mark reward:{}", user.getRoleName(), rewards.size());
			
			XinqiMessage dailyMarkXinqi = new XinqiMessage();
			dailyMarkXinqi.payload = mark.build();
			GameContext.getInstance().writeResponse(user.getSessionKey(), dailyMarkXinqi);
		}
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.DailyMark, success);
	}
	
	
}
