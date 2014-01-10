package script.guild;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 查询捐献的数额
 * 捐献公式：
 *   捐献1元宝=5贡献=10财富
 *   捐献100000金币=1贡献=2财富
			
 * @author wangqi
 *
 */
public class GuildContributeQuery {
	
	private static final float YUANBAO_TO_CREDIT = 5f;
	private static final float YUANBAO_TO_WEALTH = 10f;
	
	private static final float GOLDEN_TO_CREDIT = 1/100000f;
	private static final float GOLDEN_TO_WEALTH = 2/100000f;

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int yuanbao = (Integer)parameters[1];
		int golden = (Integer)parameters[2];
		
		int wealth=0, credit=0, goldenWealth=0, goldenCredit=0;
		wealth = Math.round(YUANBAO_TO_WEALTH * yuanbao);
		credit = Math.round(YUANBAO_TO_CREDIT * yuanbao);
		goldenWealth = Math.round(GOLDEN_TO_WEALTH * golden);
		goldenCredit = Math.round(GOLDEN_TO_CREDIT * golden);
		
		ArrayList list = new ArrayList();
		list.add(wealth);
		list.add(credit);
		list.add(goldenWealth);
		list.add(goldenCredit);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
