package script.promotion;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.Puzzle;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.PuzzleManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.UserInputManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
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
public class PuzzlePromotion {
	
	private static final Logger logger = LoggerFactory.getLogger(PuzzlePromotion.class);
	//开始日期
	private static final Calendar startCal = Calendar.getInstance();
	//结束日期
	private static final Calendar endCal = Calendar.getInstance();
	
	static {
		startCal.set(Calendar.YEAR, 2013);
		/**
		 * 元宵节猜灯谜活动
		 * 2013-2-24 到 2013-2-25日
		 */
		startCal.set(Calendar.MONTH, 1);
		startCal.set(Calendar.DAY_OF_MONTH, 1);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		
		endCal.set(Calendar.YEAR, 2013);
		endCal.set(Calendar.MONTH, 1);
		endCal.set(Calendar.DAY_OF_MONTH, 1);
		endCal.set(Calendar.HOUR_OF_DAY, 23);
		endCal.set(Calendar.MINUTE, 59);
		endCal.set(Calendar.SECOND, 59);
	}
	
	private static final String REDIS_PREFIX = "puzzle:";
	//总答题数
	private static final String TOTAL_FIELD = "total";
	//总答对数
	private static final String RIGHT_FIELD = "right";
	//活动结束标志
	private static final String FINISHED_FIELD = "finished";

	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		
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
		//if ( debugUser || available ) {
		if ( debugUser ) {
			final String key = REDIS_PREFIX.concat(user.getUsername());
			final Jedis jedisDB = JedisFactory.getJedisDB();
			if ( !jedisDB.exists(key) ) {
				String message = "今天是元宵节，您是否愿意参与元宵猜灯谜活动，赢取元宝大奖呢？";
				ConfirmManager.getInstance().sendConfirmMessage(user, message, "yuanxiaojie", new ConfirmManager.ConfirmCallback() {
					public void callback(User user, int selected) {
						if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
							jedisDB.hset(key, TOTAL_FIELD, Constant.ZERO);
							jedisDB.hset(key, RIGHT_FIELD, Constant.ZERO);
							startpuzzle(user, key, jedisDB);
						}
					}
				});
			} else {
				startpuzzle(user, key, jedisDB);
			}

			jedisDB.expire(key, 86400);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * @param user
	 * @param key
	 * @param jedisDB
	 */
	private static void startpuzzle(User user, final String key,
			final Jedis jedisDB) {
		String finishedMark = jedisDB.hget(key, FINISHED_FIELD);
		if ( finishedMark != null ) {
			String message = "您的答题活动已经结束了，感谢您的参与！";
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user, message, Action.NOOP, Type.NORMAL);
		} else {
			String totalStr = jedisDB.hget(key, TOTAL_FIELD);
			final int total = StringUtil.toInt(totalStr, 0);
			if ( total > 50 ) {
				String message = "您已经答完了50道题目，不能再参与答题活动了，谢谢";
				SysMessageManager.getInstance().sendClientInfoRawMessage(
						user, message, Action.NOOP, Type.NORMAL);
			} else {
				nextPuzzle(user, key, jedisDB, total);
			}
		}
	}

	/**
	 * @param user
	 * @param key
	 * @param jedisDB
	 * @param totalStr
	 * @param total
	 */
	private static void nextPuzzle(User user, final String key,
			final Jedis jedisDB, final int total) {
		final String rightStr = jedisDB.hget(key, RIGHT_FIELD);
		final int right = StringUtil.toInt(rightStr, 0);
		final Puzzle puzzle = PuzzleManager.getInstance().getRandomPuzzle(user);
		String message = "欢迎您参加元宵猜灯谜活动，您只要在50道题目中答对10道题目，即可领取元宝红包一份。" +
				"当前您已经参与了"+total+"道题目，答对了"+right+"道题目。\n\t" +
						puzzle.getQuestion()+"\n 请输入您的答案:";
		UserInputManager.getInstance().sendInputMessage(user, "元宵猜灯谜", 
				message, "puzzle", new UserInputManager.InputCallback() {
					public void callback(User user, String userInput) {
						if ( puzzle.getAnswer().equals(userInput) ) {
							//Answer is right
							int right = StringUtil.toInt(rightStr, 0) + 1;
							if ( right >= 10 ) {
								sendMail(user);
								jedisDB.hset(key, FINISHED_FIELD, Constant.ONE);
							}
							jedisDB.hset(key, RIGHT_FIELD, String.valueOf(right));
						}
						int currentTotalCount = total + 1;
						nextPuzzle(user, key, jedisDB, currentTotalCount);
					}
				});
	}

	private static void sendMail(User user) {
		String subject = "恭喜您成功参与了元宵节猜灯谜活动";
		String content = "您成功的猜对了10道灯谜题目，我们为您送出元宝礼包，祝您元宵节愉快";
		//30016	福字礼包
		Reward reward = RewardManager.getInstance().getRewardItem("30016", 1);
		MailMessageManager.getInstance().sendAdminMail(user.get_id(), subject, content, reward);
	}
}
