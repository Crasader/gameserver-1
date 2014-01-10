package script.task;

import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
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
public class BuyItemByMedal {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.BUY_ITEM ) {
			Object[] array = (Object[])parameters[3];
			ShopPojo shopPojo = (ShopPojo)array[0];
			PropData propData = (PropData)array[1];
			int indateTypeIndex = (Integer)array[2];
			
			if ( shopPojo.getMoneyType() == MoneyType.MEDAL ) {
				TaskStep.step(task, user);
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
