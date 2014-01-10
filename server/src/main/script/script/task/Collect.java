package script.task;

import java.util.Iterator;
import java.util.List;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * 收集兑换类型的活动任务
 * 
 * @author wangqi
 *
 */
public class Collect {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.COLLECT ) {
			Object[] array = (Object[])parameters[3];
			Bag bag = user.getBag();
			int itemCount = 0;
			String collectedItemId = String.valueOf(task.getCondition1());
			List propDataList = bag.getOtherPropDatas();
			for (Iterator iter = propDataList.iterator(); iter.hasNext();) {
				PropData propData = (PropData) iter.next();
				if ( propData != null && propData.getItemId().equals(collectedItemId) ) {
					itemCount += propData.getCount();
				}
			}
			if ( itemCount < 0 ) {
				itemCount = 0;
			}
			TaskStep.step(task, user, itemCount, true);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
