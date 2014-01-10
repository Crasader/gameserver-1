package script.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * When an user get an item or weapon into his/her bag, 
 * it will check if user bag's item count reach given number.
 * 
 * Parameters:							
 * user, task, TaskHook.WEAR, PropData
 * 
 * @author wangqi
 *
 */
public class UserBagCount {
	
	private static final Logger logger = LoggerFactory.getLogger(UserBagCount.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.ADD_BAG_COUNT ) {
			Object[] array = (Object[])parameters[3];
			Object obj = (Object)array[0];
			if ( obj != null && obj instanceof Integer ) {
				int count = (Integer)obj;
				TaskStep.step(task, user, count, true);
			} else {
				logger.debug("Failed to get the bag count: {} for task: {}" , obj, task.getId());
			}
		}		
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
