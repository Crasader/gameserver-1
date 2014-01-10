package script.task;

import java.util.List;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.DailyMarkReward;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * Record the max continuous login date for a given user,
 * if it reach the cond2, the task is finished.
 * 
 * @author wangqi
 *
 */
public class LoginDate {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.LOGIN_DATE ) {
			Object[] array = (Object[])parameters[3];
			DailyMarkReward dailyMark = (DailyMarkReward)array[0];
			int continusDay = 1;
			if ( dailyMark.isTodayMarked() ) {
				continusDay = 0;
			}
			List marks = dailyMark.getMarkArray();
			int count = marks.size();
			for ( int i=count-1; i>=0; i-- ) {
				Boolean mark = (Boolean)marks.get(i);
				if ( mark.booleanValue() ) {
					continusDay++;
				} else {
					break;
				}
			}
			TaskStep.step(task, user, continusDay, true);
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
