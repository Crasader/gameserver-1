package script.promotion;

import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 在线时长活动
 * 在指定的时间内，每日在线时间累计超过XX分钟，系统通过邮件给玩家发送工资
 * 
 * @author wangqi
 *
 */
public class OnlinePromotion {
	
	private static final Logger logger = LoggerFactory.getLogger(OnlinePromotion.class);
	//开始日期
	private static final Calendar startCal = Calendar.getInstance();
	//结束日期
	private static final Calendar endCal = Calendar.getInstance();
	
	static {
		startCal.set(Calendar.YEAR, 2013);
		/**
		 * 新年酬宾
		 * 2013-2-9 到 2013-2-15日
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
	
	private static final String REDIS_PREFIX = "online_promotion:";
	private static final String REDIS_FIELD = "take";
	
	private static final int ONLINE_SECOND = 7200;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		Integer onlineSecond = (Integer)parameters[1];
		
		boolean available = false;
		long currentTimeMillis = System.currentTimeMillis();
		if ( currentTimeMillis >= startCal.getTimeInMillis() && 
				currentTimeMillis <= endCal.getTimeInMillis() ) {
			//logger.debug("The charge promotion is in date:{} to {}", startCal.getTime(), endCal.getTime());
			available = true;
		}
		if ( available ) {
			Calendar today = Calendar.getInstance();
			String todayStr = DateUtil.formatDate(today.getTime());
			String key = REDIS_PREFIX.concat(user.getUsername()).concat(todayStr);
			Jedis jedisDB = JedisFactory.getJedisDB();
			String todayOnlineSecondStr = jedisDB.hget(key, todayStr);
			int todayOnlineSecond = 0;
			if ( todayOnlineSecondStr != null ) {
				todayOnlineSecond = StringUtil.toInt(todayOnlineSecondStr, 0);
			}
			todayOnlineSecond += onlineSecond.intValue();
			jedisDB.hset(key, todayStr, String.valueOf(todayOnlineSecond));
			String mark = jedisDB.hget(key, REDIS_FIELD);
			if ( mark == null && todayOnlineSecond > ONLINE_SECOND ) {
				jedisDB.hset(key, REDIS_FIELD, Constant.ONE);
				sendMail(user, todayOnlineSecond, todayStr);
			}
			jedisDB.expire(key, 86400);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	private static void sendMail(User user, int onlineSecond, 
			String todayStr) {
		//30017	工资礼包
		String subject = Text.text("online.promotion.subject");
		String content = Text.text("online.promotion.content");
		Reward reward = RewardManager.getInstance().getRewardItem("30017", 1);
		String message = MessageFormatter.format(content, todayStr, Math.round(onlineSecond/60.0f)).getMessage();
		MailMessageManager.getInstance().sendAdminMail(user.get_id(), subject, message, reward);
	}
}
