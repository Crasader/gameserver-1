package script.promotion;

import java.text.MessageFormat;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;

/**
 * 开展充值赠送活动
		【活动时间】 2012.10.16 至 2012.11.23
		
		【活动奖励】 
		
		一次性充值5000元宝 返赠1000元宝
		一次性充值2000元宝 返赠500元宝
		一次性充值1000元宝 返赠200元宝

 * 
 * @author wangqi
 *
 */
public class ChargePromotion {
	
	private static final Logger logger = LoggerFactory.getLogger(ChargePromotion.class);
	//开始日期
	private static final Calendar startCal = Calendar.getInstance();
	//结束日期
	private static final Calendar endCal = Calendar.getInstance();
	
	private static final String subject = "新年充值大酬宾";
	private static final String content = 
			"为了回馈广大玩家，我们开启新年充值大返利活动，活动时间从{}到{}，详情如下：\n" +
			"一次性充值满10000元宝，返赠10000元宝; \n" +
			"一次性充值满8000元宝，返赠6000元宝; \n" +
			"一次性充值满5000元宝，返赠2500元宝；\n" +
			"一次性充值满3000元宝，返赠1000元宝；\n" +
			"一次性充值满1000元宝，返赠200元宝。\n" +
			"您本次充值了{}元宝，将获得额外赠送的{}元宝，祝您游戏愉快";
	
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

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		Integer chargedYuanbao = (Integer)parameters[1];
		boolean available = false;
		long currentTimeMillis = System.currentTimeMillis();
		if ( currentTimeMillis >= startCal.getTimeInMillis() && 
				currentTimeMillis <= endCal.getTimeInMillis() ) {
			//logger.debug("The charge promotion is in date:{} to {}", startCal.getTime(), endCal.getTime());
			available = true;
		}
		if ( available ) {
			String startDateStr = DateUtil.formatDateTime(startCal.getTime());
			String endDateStr = DateUtil.formatDateTime(endCal.getTime());
			if ( chargedYuanbao.intValue() >= 10000 ) {
				sendMail(user, chargedYuanbao, 10000, startDateStr, endDateStr);
			} else if ( chargedYuanbao.intValue() >= 8000 ) {
				sendMail(user, chargedYuanbao, 6000, startDateStr, endDateStr);
			} else if ( chargedYuanbao.intValue() >= 5000 ) {
				sendMail(user, chargedYuanbao, 2500, startDateStr, endDateStr);
			} else if ( chargedYuanbao >= 3000 ) {
				sendMail(user, chargedYuanbao, 1000, startDateStr, endDateStr);
			} else if ( chargedYuanbao >= 1000 ) {
				sendMail(user, chargedYuanbao, 200, startDateStr, endDateStr);
			}
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
	private static void sendMail(User user, Integer chargedYuanbao, int yuanbao, 
			String startDateStr, String endDateStr) {
		Reward reward = RewardManager.getInstance().getRewardYuanbao(yuanbao);
		String message = MessageFormatter.arrayFormat(content, 
				new Object[]{startDateStr, endDateStr, chargedYuanbao, yuanbao}).getMessage();
		MailMessageManager.getInstance().sendAdminMail(user.get_id(), subject, message, reward);
	}
}
