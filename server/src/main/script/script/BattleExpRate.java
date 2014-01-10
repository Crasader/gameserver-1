package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Caculate the current exp rate for a given user in a battle.
 * It can be used for promotion. For example, VIP users always
 * have double exp rate.
 * 
 * @author wangqi
 *
 */
public class BattleExpRate {

	public static ScriptResult func(Object[] parameters) {
		int expRate = 1;
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
//		Object arg1 = parameters[0];
//		if ( arg1 != null ) {
//			if ( arg1 instanceof BattleUser ) {
//				BattleUser battleUser = (BattleUser)arg1;
//				if ( battleUser.getUser().isVip() ) {
//					expRate = 2;
//				}
//			}
//		}
		
		ArrayList list = new ArrayList();
		list.add(expRate);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
