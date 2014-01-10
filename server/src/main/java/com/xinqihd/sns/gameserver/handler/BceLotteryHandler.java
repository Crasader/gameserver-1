package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceLottery.BceLottery;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceLotteryHandler is used for protocol Lottery 
 * @author wangqi
 *
 */
public class BceLotteryHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceLotteryHandler.class);
	
	private static final BceLotteryHandler instance = new BceLotteryHandler();
	
	
	private BceLotteryHandler() {
		super();
	}

	public static BceLotteryHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceLottery");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceLottery lotteryData = (BceLottery)request.payload;
		
		int idx = lotteryData.getIdx();
		boolean isLogin = lotteryData.getIsLogin();
		boolean isPVE = lotteryData.getIsPVE();
		
		if ( isLogin ) {
			Reward reward = null;
			ArrayList<Reward> rewards = user.getLoginRewards();
			if ( rewards != null && idx >=0 && idx < rewards.size() ) {
				reward = user.getLoginRewards().get(idx);
			}
			if ( reward != null ) {
				boolean success = RewardManager.getInstance().takeLoginReward(user, System.currentTimeMillis());
				if ( success ) {
					rewards.set(idx, null);
					ArrayList<Reward> pickRewards = new ArrayList<Reward>();
					pickRewards.add(reward);
					boolean changed = RewardManager.getInstance().pickReward(
							user, pickRewards, StatAction.ProduceLottery);
					if ( !changed ) {
						//Save user's new remainTimes
						GameContext.getInstance().getUserManager().saveUser(user, false);
						
						//Notify client user's role data is changed.
						BseRoleInfo roleInfo = user.toBseRoleInfo();
						GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
					}
					logger.debug("User {} take login reward:{}", user.getRoleName(), reward);
					
					StatClient.getIntance().sendDataToStatServer(user, StatAction.Lottery, reward.getType(), reward.getPropCount(), reward.getLevel(), reward.getPropId(), 
							reward.getPropIndate());
				}
			} else {
				logger.debug("User {} already take login reward, idx:{}", user.getRoleName(), idx);
			}
		} else if ( isPVE ) {
			String id = lotteryData.getId();
			Jedis jedisDB = JedisFactory.getJedisDB();
			Boss boss = BossManager.getInstance().getBossInstance(user, id);
			ArrayList<Reward> rewards = BossManager.getInstance().retriveBossReward(user, jedisDB, boss, false);
			if ( idx >= 0 && idx < rewards.size() ) {
				Reward reward = rewards.get(idx);
				ArrayList<Reward> pickReward = new ArrayList<Reward>();
				pickReward.add(reward);
				RewardManager.getInstance().pickRewardWithResult(user, pickReward, StatAction.PVETakeReward);
				
				String rewardKey = BossManager.getBossRewardUserKey(user, boss.getId());
				jedisDB.hdel(rewardKey, BossManager.FIELD_BOSS_REWARD_ID);
			} else {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "lottery.pve.noreward", Type.NORMAL);
			}
		} else {
			logger.warn("BceLotteryHandler only supports 'isLogin' mode");
		}
	}
	
}
