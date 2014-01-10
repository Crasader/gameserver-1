package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * Generate random wind in a battle
 * 
 * 风力值以上一回合的风力为基础，用随机数生成-1到1之间的一个随机数，与基础值相加，
 * 如果超过了[-5,5]的区间，则保持不变。
 * 改变风力的道具会直接改变本回合的基础风力
 * 
 * @author wangqi
 *
 */
public class BattleWind {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		Battle battle = (Battle)parameters[0];
		BattleUser battleUser = (BattleUser)parameters[1];
		
		boolean windOk = false;
		int level = 0;
		try {
			level = battleUser.getUser().getLevel();
		} catch (Exception e) {
		}
		if ( level > 5 ) {
			windOk = true;
		}
		int wind = battle.getRoundWind();
		int windDir = battle.getRoundWindDir();
		wind *= windDir;
		if ( windOk ) {
			boolean sign = MathUtil.nextDouble() < 0.5;
			if ( sign ) {
				wind++;
				if ( wind > 5 ) {
					wind = 5;
				}
			} else {
				wind--;
				if ( wind < -5 ) {
					wind = -5;
				}
			}
		}
		
		ArrayList list = new ArrayList();
		list.add(wind);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
