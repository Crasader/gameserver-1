package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;

/**
 * Caculate the current exp rate for a given user in a battle.
 * It can be used for promotion. For example, VIP users always
 * have double exp rate.
 * 
 * @author wangqi
 *
 */
public class UserCalculateThew {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int agility = user.getAgility();
		
		int thew = (int)UserCalculator.calculateThew(agility);
		
		ArrayList list = new ArrayList();
		list.add(thew);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
