package script;
import java.util.*;

import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptHook;

public class Hello {

	public static ScriptResult func(Object[] context) {
   List list= new ArrayList(); 
   list.add("hello"); 
   ScriptResult result = new ScriptResult(); 
   result.setType(ScriptResult.Type.SUCCESS_RETURN); 
   result.setResult(list); 
   return result; 
   
 }
}
