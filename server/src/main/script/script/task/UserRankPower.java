package script.task;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * When the user's power rank reach the given value, the task
 * is finished.
 * 
 * @author wangqi
 *
 */
public class UserRankPower {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.RANK ) {
			Object[] array = (Object[])parameters[3];
			RankScoreType scoreType = (RankScoreType)array[0];
			RankFilterType filterType = (RankFilterType)array[1];
			Integer rank = (Integer)array[2];
			if ( scoreType == RankScoreType.POWER &&
					filterType == RankFilterType.TOTAL && 
					rank != null ) {
				TaskStep.reverseStep(task, user, rank.intValue());
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
