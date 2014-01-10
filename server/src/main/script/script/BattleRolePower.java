package script;

import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * BattleUser is moving
 * @author wangqi
 *
 */
public class BattleRolePower {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		BattleUser battleUser = (BattleUser)parameters[0];
		/**
		 * 以下行为增加的DELAY值	
		 * 行为	DELAY值
		 * 怒气	45
		 */
		battleUser.setDelay(battleUser.getDelay()+
				GameDataManager.getInstance().getGameDataAsInt(GameDataKey.DELAY_TOOL_ENERGY, 45));
		battleUser.setEnergy(0);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
