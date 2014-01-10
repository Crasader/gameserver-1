package script.task;

import java.util.Iterator;
import java.util.List;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * 收集兑换类型的活动任务领取后要删除背包中的物品
 * 
 * @author wangqi
 *
 */
public class CollectPost {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		int total = task.getStep();
		Bag bag = user.getBag();
		String collectedItemId = String.valueOf(task.getCondition1());
		List propDataList = bag.getOtherPropDatas();
		boolean used = false;
		for (Iterator iter = propDataList.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propData != null && propData.getItemId().equals(collectedItemId) ) {
				if ( total > 0 ) {
					total = total - bag.removeOtherPropDatasCount(propData.getPew(), total);
					if ( total <= 0 ) {
						used = true;
						break;
					}
				}
			}
		}
		if ( used ) {
			UserManager.getInstance().saveUserBag(user, false);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
