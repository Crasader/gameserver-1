package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Called by RoleActionManager to collect daily user action points.
 * 
 * @author wangqi
 *
 */
public class ActionLimitDaily {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int dbTodayBuy = StringUtil.toInt((String)parameters[1], 0);
		
	  //当日可用的购买次数
		int buyCount = VipManager.getInstance().getVipLevelRoleActionBuyCount(user);
		//购买的价格
		int buyPrice = 10;
		if ( dbTodayBuy > 2 ) {
			//价格逐渐加成
			buyPrice *= 1.2*buyPrice*(dbTodayBuy-2);
		}
		//每次购买增加的体力点数
		int buyValue = 100;
		
		ArrayList list = new ArrayList();
		list.add(buyCount);
		list.add(buyPrice);
		list.add(buyValue);
		list.add(dbTodayBuy);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
