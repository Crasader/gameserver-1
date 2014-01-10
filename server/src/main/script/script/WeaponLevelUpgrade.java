package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;

/**
 * When a weapon is upgraded, its properties will change. This script calculator
 * 
 * @author wangqi
 *
 */
public class WeaponLevelUpgrade {

	/**
	 * Parameters:
	 * 1. The old PojoData object
	 * 2. The new level 
	 * 
	 * @param parameters
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		PropData propData = (PropData)parameters[0];
		int newLevel = (Integer)parameters[1];
		
		EquipCalculator.weaponUpLevel(propData, newLevel);
		
		ArrayList list = new ArrayList();
		list.add(propData);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
