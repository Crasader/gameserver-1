package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * When a weapon is upgraded, its properties will change. This script calculator
 * 
 * When an user upgrade a level, his attack/defend/agility/lucky will change. 
 * This script calculate the new value for him.
 * 
 * @author wangqi
 *
 */
public class UserLevelUpgrade {
	
	/**
	 * Parameters:
	 * 1. The User object
	 * 2. The level difference before upgrading and after upgrading
	 * 
	 * @param parameters
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
//		int levelDiff = (Integer)parameters[1];
		
		LevelPojo level = LevelManager.getInstance().getLevel(user.getLevel());
		
		//玩家的血量
		user.setBlood(level.getBlood());

	  // 玩家的伤害值
	  user.setDamage(0);

	  // 玩家护甲
	  user.setSkin(level.getSkin());

	  // 玩家体力
	  int baseThew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_THEW_BASE, 210);
	  int agilityUnit = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.GAME_AGILITY_UNIT, 5);
	  user.setTkew(baseThew + (int)(level.getAgility()*1.0/agilityUnit));
	
	  // 玩家体力
		user.setAttack(level.getAttack());

	  // 玩家防御
		user.setDefend(level.getDefend());

	  // 玩家敏捷
		user.setAgility(level.getAgility());

	  // 玩家幸运
		user.setLuck(level.getLucky());
	  
	  //重新计算玩家的战斗力
	  Bag bag = user.getBag();
	  List bagList = bag.getWearPropDatas();
	  int size = bagList.size();
	  for ( int i=0; i<size; i++ ) {
	  	PropData propData = (PropData)bagList.get(i);
	  	if ( propData != null && !propData.isExpire() ) {
	  		UserCalculator.updateWeaponPropData(user, propData, true);
	  	}
	  }
	  
	  // 战斗力
		int power = (int)UserCalculator.calculatePower(user);
	  user.setPower(power);
		
		ArrayList list = new ArrayList();
		list.add(user);		

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
