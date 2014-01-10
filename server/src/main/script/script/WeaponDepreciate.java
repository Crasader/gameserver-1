package script;

import java.util.List;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * The equipments that users wear will depreciate in every battle.
 * And they all finally need re-fix or lengthen indate value.
 * @author wangqi
 *
 */
public class WeaponDepreciate {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		Bag bag = user.getBag();
		List wearedPropData = bag.getWearPropDatas();
		for ( int i=0; i<wearedPropData.size(); i++  ) {
			PropData propData = (PropData)wearedPropData.get(i);
			if ( propData != null ) {
				int leftTimes = propData.getPropUsedTime() - 1;
				if ( leftTimes < 0 ) leftTimes = 0;
				propData.setPropUsedTime( leftTimes );
				bag.setChangeFlagOnItem(propData);
			}
		}
		//Save the propData status.
		UserManager.getInstance().saveUserBag(user, false);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
