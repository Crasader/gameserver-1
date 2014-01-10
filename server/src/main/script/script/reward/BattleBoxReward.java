package script.reward;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 战斗中的随机宝箱，
 * 90% 掉了金币
 * 10% 掉落元宝
 * 
 * @author wangqi
 *
 */
public class BattleBoxReward {
	
	//29029	感恩节大礼包
	//private static final String THANKS_GIVING_ID = "29029";
	
	private static final Set includeSet = EnumSet.noneOf(EquipType.class);
	static {
		includeSet.add(EquipType.CLOTHES);
		includeSet.add(EquipType.DECORATION);
		includeSet.add(EquipType.EXPRESSION);
		includeSet.add(EquipType.FACE);
		includeSet.add(EquipType.GLASSES);
		includeSet.add(EquipType.HAIR);
		includeSet.add(EquipType.HAT);
		includeSet.add(EquipType.WEAPON);
		includeSet.add(EquipType.WING);
	}
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		SimplePoint point = (SimplePoint)parameters[1];

		Reward reward = null;
		double ratio = MathUtil.nextDouble();
		
		//Check RewardPojo config
		TreeSet rewardConfigs = RewardManager.getInstance().getRewardPojoForType(user, RewardPojoType.BATTLE_BOX_PICK);
		if ( rewardConfigs != null ) {
			for (Iterator iterator = rewardConfigs.iterator(); iterator.hasNext();) {
				RewardPojo rewardPojo = (RewardPojo) iterator.next();
				double threshold = rewardPojo.getRatio()/1000.0;
				if ( ratio < threshold ) {
					reward = rewardPojo.getReward();
					break;
				}
			}
		}
		
		if ( reward == null ) {
			if ( ratio < 0.1 ) {
				//ItemPojo item = ItemManager.getInstance().getItemById(THANKS_GIVING_ID);
				//reward = RewardManager.getRewardItem(item);
				reward = RewardManager.getRewardGolden(user);
			} else if ( ratio < 0.2 ) {
				reward = RewardManager.getRewardYuanbao();
			} else if ( ratio < 0.4 ) {
				reward = RewardManager.getInstance().generateRandomWeapon(user, includeSet);
			} else {
				reward = RewardManager.getRewardGolden(user);
			}
		}
		
		int x = point.getX();
		int y = point.getY();
		int tx = x-100;
		if ( tx < 0 ) tx = 100;
		reward.setX(tx);
		int ty = y;
		reward.setY(ty);
		
		ArrayList list = new ArrayList(1);
		list.add(reward);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
