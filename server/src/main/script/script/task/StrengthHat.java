package script.task;

import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.StoneType;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * 
 * 
 * Parameters:							
 * user, task, TaskHook.CRAFT_FORGE, Object[]{StoneType, equipPropData, stoneLevel}
 * 
 * @author wangqi
 *
 */
public class StrengthHat {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.CRAFT_FORGE ) {
			Object[] array = (Object[])parameters[3];
			StoneType stoneType = (StoneType)array[0];
			PropData equipPropData = (PropData)array[1];
			//int stoneLevel = (Integer)array[2];
			
			boolean reachLevel = TaskStep.level(task, user, equipPropData.getLevel());
			if ( reachLevel ) {
				Pojo pojo = equipPropData.getPojo();
				if ( pojo instanceof WeaponPojo ) {
					WeaponPojo weapon = (WeaponPojo)pojo;
					if ( stoneType == StoneType.STRENGTH && 
							weapon.getSlot() == EquipType.HAT) {
						TaskStep.step(task, user);
					}
				}
			}
		}		
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
