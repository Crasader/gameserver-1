package script.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * When an user wants to sell his good to Shop, this script will
 * calculate the final golden price.
 * 
 * @author wangqi
 *
 */
public class SellGood {

	private static final Logger logger = LoggerFactory.getLogger(SellGood.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.SELL_GOOD ) {
			Object[] array = (Object[])parameters[3];
			PropData propData = (PropData)array[0];
			Integer finalPrice = (Integer)array[1]; 
			
			if ( task.getCondition1() <= 0 ) {
				TaskStep.step(task, user);
			} else {
				String itemId = String.valueOf(task.getCondition1());
				if ( propData.getItemId().equals(itemId) ) {
					TaskStep.step(task, user);
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
