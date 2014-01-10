package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Get the user's next level required exp data.
 * @author wangqi
 *
 */
public class NextLevelExp {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		int currentLevel = (Integer)parameters[0];
//		int requiredExp = UserCalculator.calculateLevelExp(currentLevel);
		LevelPojo level = LevelManager.getInstance().getLevel(currentLevel);
		int requiredExp = level.getExp();
		
		ArrayList list = new ArrayList();
		list.add(requiredExp);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
