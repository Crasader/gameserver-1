package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;

/**
 * When user wear a new weapon or unwear a weapon, his/her properties such as
 * power, attack, defend etc.. should be recalculated
 */
public class UserPropCalculate {

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
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		PropData propData = (PropData)parameters[1];
		boolean wearEquip = (Boolean)parameters[2];

		UserCalculator.updateWeaponPropData(user, propData, wearEquip);
		
		ArrayList list = new ArrayList();
		list.add(user);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
		
}
