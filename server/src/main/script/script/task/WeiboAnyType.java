package script.task;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.WeiboOpType;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * The user buy something in the shop
 * 
 * Parameters:
 * 	User, TaskHook.BUY, Object[]{shopPojo, propData, indateTypeIndex}
 * 
 * @author wangqi
 *
 */
public class WeiboAnyType {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.WEIBO ) {
			Object[] array = (Object[])parameters[3];
			WeiboOpType weiboType = (WeiboOpType)array[0];
			
			if ( weiboType == WeiboOpType.ANY ) {
				TaskStep.step(task, user);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
