package script.task;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 在指定的时间段内实现经验值的加成
 * cond1: 13301430, 表示13:30开始到14:30结束双倍经验
 * 
 * @author wangqi
 *
 */
public class ExpGain {

	private static final Logger logger = LoggerFactory.getLogger(ExpGain.class);
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.EXPGAIN ) {
			String timeRange = String.valueOf(task.getCondition1());
			int startHour = StringUtil.toInt(timeRange.substring(0, 2), -1);
			int startMin = StringUtil.toInt(timeRange.substring(2, 4), -1);
			int endHour = StringUtil.toInt(timeRange.substring(4, 6), -1);
			int endMin = StringUtil.toInt(timeRange.substring(6, 8), -1);
			if ( startHour >= 0 && startMin >= 0 && endHour >= 0 && endMin >= 0 ) {
				Calendar startCal = Calendar.getInstance();
				startCal.set(Calendar.HOUR_OF_DAY, startHour);
				startCal.set(Calendar.MINUTE, startMin);
				Calendar endCal = Calendar.getInstance();
				endCal.set(Calendar.HOUR_OF_DAY, endHour);
				endCal.set(Calendar.MINUTE, endMin);
				int startSecond = (int)(startCal.getTimeInMillis()/1000);
				int endSecond = (int)(endCal.getTimeInMillis()/1000);
				int currentSecond = (int)(System.currentTimeMillis());
				if ( startSecond < endSecond ) {
					if ( currentSecond>startSecond && currentSecond<endSecond ) {
						int ttl = endSecond - currentSecond;
						ActivityManager.getInstance().setActivityExpRate(null, 2.0f, ttl);
					}
				} else {
					logger.warn("The expgain time range is wrong. start:{}, end:{}", startCal.getTime(), endCal.getTime());
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
