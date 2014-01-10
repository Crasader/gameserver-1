package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;

/**
 * Caculate the User's blood according to {@link UserCalculator#calculateBlood(int, double)}
 * 
 * @author wangqi
 *
 */
public class UserCalculateBlood {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int userLevel = user.getLevel();
		int userDefend = user.getDefend();

		int blood = (int)UserCalculator.calculateBlood(userLevel, userDefend);
		
		ArrayList list = new ArrayList();
		list.add(blood);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
