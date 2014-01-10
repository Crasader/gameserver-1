package script.task;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.Pojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * 合成x级火神石
 * 
 * Parameters:							
 * user, task, TaskHook.CRAFT_COMPOSE, Object[]{equipType, newPropData}
 * 
 * @author wangqi
 *
 */
public class CraftComposeFire {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.CRAFT_COMPOSE ) {
			Object[] array = (Object[])parameters[3];
			int funcType  = (Integer)array[0];
			PropData newPropData = (PropData)array[1];
			
			String attackStoneId = String.valueOf(GameDataManager.getInstance().
					getGameDataAsInt(GameDataKey.CRAFT_STONE_ATTACK, 20004));
			
			boolean reachLevel = TaskStep.level(task, user, newPropData.getLevel());
			if ( reachLevel ) {
				Pojo pojo = newPropData.getPojo();
				if ( pojo instanceof ItemPojo ) {
					ItemPojo itemPojo = (ItemPojo)pojo;
					if ( attackStoneId.equals(itemPojo.getTypeId()) ) {
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
