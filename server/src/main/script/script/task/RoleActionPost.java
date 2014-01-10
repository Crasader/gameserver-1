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
 * 领取体力值后增加体力值点数
 * 
 * @author wangqi
 *
 */
public class RoleActionPost {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		
		Bag bag = user.getBag();
		List propDatas = bag.getOtherPropDatas();
		int pew = -1;
		for (Iterator iter = propDatas.iterator(); iter.hasNext();) {
			PropData propData = (PropData) iter.next();
			if ( propData != null && "99999".equals(propData.getItemId()) ) {
				pew = propData.getPew();
				break;
			}
		}
		if ( pew >= 0 ) {
			RoleActionManager.getInstance().addRoleActionPoint(user, 
					System.currentTimeMillis(), true, 80);
			bag.removeOtherPropDatas(pew);
			UserManager.getInstance().saveUserBag(user, false);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
		}
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
