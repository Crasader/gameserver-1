package script;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.forge.ComposeStatus;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 计算武器升级需要的钻石数量
 * 
 */
public class CraftCalDiamond {

	private static final Logger logger = LoggerFactory
			.getLogger(CraftCalDiamond.class);

	/**
	 * 每提升10级，需要的钻石数量+10颗，颜色每提升1个等级，则需要1.2倍钻石数量
			1	1
			2	10
			3	45
			4	80
			5	125
			6	180
			7	245
			8	320
			9	405
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if (result != null) {
			return result;
		}

		User user = (User) parameters[0];
		PropData propData = (PropData) parameters[1];
		int count = 0;
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getItemId());
		if ( weapon != null ) {
			int userLevel = weapon.getUserLevel();
			if ( userLevel == 0 ) {
				count = 1;
			} else if ( userLevel == 10 ) {
				count = 10;
			} else if ( userLevel < 90 ) {
				count = (userLevel/10+1)*(userLevel/10+1)*5;
				int colorIndex = propData.getWeaponColor().ordinal();
				if ( colorIndex > 0 ) {
					count = Math.round(count * 1.2f * colorIndex);
				}
			}
		}

		List list = new ArrayList();
		list.add(count);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
