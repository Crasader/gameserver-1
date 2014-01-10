package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Serve as a test for script engine.
 * @author wangqi
 *
 */
public class Test {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		int a = (Integer) parameters[0];
		int b = (Integer) parameters[1];
		double c = Math.sqrt(Math.pow(a, b))*Math.sin(a+1.0)*Math.cos(b+1.0);
		
		ArrayList list = new ArrayList();
		list.add(c);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		
		return result;
	}
}
