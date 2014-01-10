package script.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
 * When an user get an item or weapon into his/her bag, 
 * it will check if user accomplish some achievements.
 * 
 * Parameters:							
 * user, task, TaskHook.WEAR, PropData
 * 
 * @author wangqi
 *
 */
public class AddItemToBag {
	
	private static final Logger logger = LoggerFactory.getLogger(AddItemToBag.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.ADD_BAG ) {
			Object[] array = (Object[])parameters[3];
			PropData propData = (PropData)array[0];
			String id = String.valueOf(task.getCondition1());
			ItemPojo item = ItemManager.getInstance().getItemById(id);
			String targetId = null;
			if ( item != null ) {
				targetId = item.getId();
			} else {
				//WeaponPojo weapon = EquipManager.getInstance().getWeaponById(id);
				WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(id, user.getLevel());
				if ( weapon != null ) {
					targetId = weapon.getId();
				}
			}
			if ( targetId != null ) {
				int count = propData.getCount();
				if (count < 1) count=1;
				if ( targetId.equals(propData.getItemId()) ) {
					TaskStep.step(task, user, count);
				}
			} else {
				logger.debug("Failed to find the target id : {} for task: {}" , id, task.getId());
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
