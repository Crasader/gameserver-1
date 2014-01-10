package script.promotion;

import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.DateUtil;

/**
 * 聊天活动
 * 玩家在聊天中输入“新年快乐”，则会进行识别
 * 
 * @author wangqi
 *
 */
public class ChatPromotionForNewYear {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatPromotionForNewYear.class);
	//开始日期
	private static final Calendar startCal = Calendar.getInstance();
	//结束日期
	private static final Calendar endCal = Calendar.getInstance();
	
	/**
	 * 29021	寻宝卡
	 * 99999	体力卡
	 * 24001	神恩符
	 * 20003	水神石Lv3
	 * 20008	土神石Lv3
	 * 24002	幸运符+15%
	 * 30003	武器残片
	 * 30014	双倍经验卡
	 * 30015	符文大宝箱
	 */
	private static final Reward[][] DAYS = new Reward[][]{
		//day 1
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("24001", 1),
		},
		//day 2
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("20008", 1),
		},
		//day 3
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("20003", 1),
		},
		//day 4
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("24002", 1),
		},
		//day 5
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("30003", 3),
		},
		//day 6
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("30014", 1),
		},
		//day 7
		{
			RewardManager.getRewardGolden(20000), 
			RewardManager.getRewardItem("29021", 2), 
			RewardManager.getRewardItem("99999", 2),
			RewardManager.getRewardItem("30015", 1),
		}
	};
	
	static {
		startCal.set(Calendar.YEAR, 2013);
		/**
		 * 新年酬宾
		 * 2013-2-24 到 2013-2-25日
		 */
		startCal.set(Calendar.MONTH, 1);
		startCal.set(Calendar.DAY_OF_MONTH, 9);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		
		endCal.set(Calendar.YEAR, 2013);
		endCal.set(Calendar.MONTH, 1);
		endCal.set(Calendar.DAY_OF_MONTH, 15);
		endCal.set(Calendar.HOUR_OF_DAY, 23);
		endCal.set(Calendar.MINUTE, 59);
		endCal.set(Calendar.SECOND, 59);
	}
	
	private static final String REDIS_PREFIX = "chat_promotion:";

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		String chatContent = (String)parameters[1];
		boolean available = false;
		boolean debugUser = false;
		long currentTimeMillis = System.currentTimeMillis();
		if ( currentTimeMillis >= startCal.getTimeInMillis() && 
				currentTimeMillis <= endCal.getTimeInMillis() ) {
			//logger.debug("The charge promotion is in date:{} to {}", startCal.getTime(), endCal.getTime());
			available = true;
		}
		if ( "jsding".equals(user.getRoleName()) || "心海洋".equals(user.getRoleName()) ) {
			debugUser = true;
		}
		if ( debugUser || available ) {
			Calendar today = Calendar.getInstance();
			String todayStr = DateUtil.formatDate(today.getTime());
			String key = REDIS_PREFIX.concat(user.getUsername());
			Jedis jedisDB = JedisFactory.getJedisDB();
			String mark = jedisDB.hget(key, todayStr);
			if ( mark == null ) {
				jedisDB.hset(key, todayStr, Constant.ONE);
				int dayDiff = today.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH);
				if ( dayDiff < 0 ) {
					dayDiff = 0;
				} else if ( dayDiff >= DAYS.length ) {
					dayDiff = DAYS.length - 1;
				}
				if ( chatContent != null && chatContent.contains("元宵猜灯谜") ) {					
					/*
					RewardManager.getInstance().sendOnlineReward(user, DAYS[dayDiff]);
					ArrayList onlineRewards = new ArrayList();
					for ( int i=0; i<DAYS[0].length; i++ ) {
						Reward r = DAYS[0][i];
						onlineRewards.add(r);
					}
					user.setOnlineRewards(onlineRewards);
					*/
				}
			}
			jedisDB.expire(key, 86400);
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

}
