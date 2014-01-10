package script.reward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * 玩家满足图鉴的需求后，可获得奖励
 * 
 * @author wangqi
 *
 */
public class BiblioReward {
	
	private static final Set excludeRewards = new HashSet();
	static {
		excludeRewards.add(RewardType.EXP);
	}

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		
		//Check RewardPojo config
		/**
			3012	赤钢●初号机
			3013	白银●初号机
			3014	黄金●初号机
			3015	琥珀●初号机
			3016	翡翠●初号机
			3017	水晶●初号机
			3018	钻石●初号机
			3019	神圣●初号机
			
			24005	必成符
		 */
		//ItemPojo item = ItemManager.getInstance().getItemById("24005");
		//Reward reward = RewardManager.getInstance().getRewardItem(item);
		WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel("301", user.getLevel()); 
		Reward reward = RewardManager.getInstance().getWeaponReward(weapon, 0, 0, false);

		ArrayList list = new ArrayList();
		list.add(reward);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
