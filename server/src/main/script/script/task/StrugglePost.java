package script.task;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.task.TaskPostStatus;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 抢夺类型的任务，系统将限量放出奖励礼包，先到先得
 * 
 * @author wangqi
 *
 */
public class StrugglePost {
	
	private static final String REDIS_STRUGGLE = "struggle:";

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		
		boolean success = false;
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = REDIS_STRUGGLE.concat(task.getId());
		int total = task.getCondition1();
		int count = (int)(jedisDB.incr(key).longValue());
		if ( count > total ) {
			success = false;
			SysMessageManager.getInstance().sendClientInfoMessage(user.getSessionKey(), "struggle.usedup", Type.NORMAL);
		} else {
			success = true;
		}
		/**
		 * 注意限时抢购活动时间不要超过4小时
		 */
		jedisDB.expire(key, 14400);
		
		ArrayList list = new ArrayList();
		if ( success ) {
			list.add(TaskPostStatus.SUCCESS);
		} else {
			list.add(TaskPostStatus.FINISHED);
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
