package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class UserUpdateStat {

	/**
	 * TODO Remove corresponding properties to user.
	 * 
	 * 伤害=武器的伤害+其他伤害（附加属性，镶嵌的宝珠等）
	 * 护甲=衣服的护甲+帽子的护甲+其他护甲（附加属性，镶嵌的宝珠等）
	 * 攻击=全身装备的攻击总和
	 * 防御=全身装备的防御总和
	 * 敏捷=全身装备的敏捷总和
	 * 幸运=全身装备的幸运总和
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		if ( !user.isAI() && !user.isProxy() ) {
			boolean battleWin = (Boolean)parameters[1];
			
			if ( battleWin ) {
				user.setWins( user.getWins() + 1 ); 
			} else {
				user.setFailcount( user.getFailcount() + 1 );
			}
			user.setBattleCount( user.getBattleCount() + 1 ) ;
			double winOdd = user.getWins() * 100.0 / user.getBattleCount();
			user.setWinOdds( (int)winOdd );
			//Save user's statusangong
			UserManager.getInstance().saveUser(user, false);
			BseRoleInfo roleInfo = user.toBseRoleInfo();
			GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
		}
		
		ArrayList list = new ArrayList();
		list.add(user);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
