package script.boss;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 每日可挑战Boss的次数
 * 
 * @author wangqi
 *
 */
public class BossLimitDaily {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int dbTodayBuy = StringUtil.toInt((String)parameters[1], 0);
		
		int buyCount = 0;
		/*
		if ( user.isVip() ) {
			switch ( user.getViplevel() ) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					buyCount = 6;
				case 6:
					buyCount = 21;
					break;
				case 7:
				case 8:
				case 9:
				case 10:
					buyCount = 46;
					break;
				default:
					buyCount = 0;
					break;
			}
		}
		*/
		//购买的价格
		int buyPrice = 2;
		if ( dbTodayBuy > 2 ) {
			//价格逐渐加成
			buyPrice *= 1.2*buyPrice*(dbTodayBuy-2);
		}
		//每次购买增加的挑战次数
		int buyValue = 10;
		
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
