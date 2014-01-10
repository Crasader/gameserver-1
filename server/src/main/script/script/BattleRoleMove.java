package script;

import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * BattleUser is moving
 * @author wangqi
 *
 */
public class BattleRoleMove {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		/**
		 * The client side is responsible for substract role move tkew.
		 */
		/*
		BattleUser battleUser = (BattleUser)parameters[0];
		battleUser.setThew(battleUser.getThew()-
				GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_ROLE_MOVE, 5));
		*/
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
