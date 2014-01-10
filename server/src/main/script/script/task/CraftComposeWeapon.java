package script.task;

import com.xinqihd.sns.gameserver.config.CraftComposeFuncType;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * 熔炼带有颜色的武器
 * 
 * @author wangqi
 *
 */
public class CraftComposeWeapon {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.CRAFT_COMPOSE_EQUIP ) {
			Object[] array = (Object[])parameters[3];
			CraftComposeFuncType funcType = (CraftComposeFuncType)array[0];
			PropData newPropData = (PropData)array[1];
			if ( funcType == CraftComposeFuncType.COLOR_GREEN ) {
				//绿色熔炼
			} else if ( funcType == CraftComposeFuncType.COLOR_BLUE ) {
					//蓝色熔炼
			} else if ( funcType == CraftComposeFuncType.COLOR_PINK ) {
					//粉色熔炼
			} else if ( funcType == CraftComposeFuncType.COLOR_ORANGE ) {
					//橙色熔炼
			} else if ( funcType == CraftComposeFuncType.COMPOSE_STRENGTH ) {
					//强化石熔炼
			} else if ( funcType == CraftComposeFuncType.COMPOSE_WATER ) {
					//水神石熔炼
			} else if ( funcType == CraftComposeFuncType.COMPOSE_FIRE ) {
					//火神石熔炼
			} else if ( funcType == CraftComposeFuncType.COMPOSE_WIND ) {
					//风神石熔炼
			} else if ( funcType == CraftComposeFuncType.COMPOSE_EARTH ) {
					//土神石熔炼
			} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON ) {
					//武器熔炼
				TaskStep.step(task, user);
			} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP ) {
					//装备熔炼
			} else if ( funcType == CraftComposeFuncType.MAKE_WEAPON2 ) {
					//精良武器熔炼
				TaskStep.step(task, user);
			} else if ( funcType == CraftComposeFuncType.MAKE_EQUIP2 ) {
					//精良装备熔炼
			}
				
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
