package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;

/**
 * Check if all the props in bag are legal
 * @author wangqi
 *
 */
public class CheckServerOption {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		User user = (User)parameters[0];
		StatAction action = (StatAction)parameters[1];
		
		ArrayList list = new ArrayList();
		list.add(Boolean.TRUE);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
