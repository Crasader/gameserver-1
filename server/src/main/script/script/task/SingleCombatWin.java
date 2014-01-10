package script.task;

import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * The user win any type of the games.
 * 
 * Parameters:
 * 	User, TaskHook.COMBAT, Object[]{isWin(bool), totalUserNumber, RoomType}
 * 
 * @author wangqi
 *
 */
public class SingleCombatWin {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.COMBAT ) {
			Object[] array = (Object[])parameters[3];
			boolean winner = (Boolean)array[0];
			int totalUserNumber = (Integer)array[1];
			RoomType roomType = (RoomType)array[2];
			
			if ( winner && roomType == RoomType.SINGLE_ROOM ) {
				TaskStep.step(task, user);
			}			
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
