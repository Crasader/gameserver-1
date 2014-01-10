package script.reward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
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
public class DiamondReward {
	
	/**
	  30009	金币大宝箱
	  30011	经验大宝箱
		30004	水神石宝箱
		30005	土神石宝箱
		30006	风神石宝箱
		30007	火神石宝箱
		30008	强化石宝箱
		30010	装备大宝箱
	 */
	private static final String[] REWARD_ID = {
		//"30009", 
		"30011", "30004", "30005", "30006", "30007", "30008", "30010"
	};

	private static final int[][] POINTS = {
		{57,	410},
		{80,	760},
		{162,	637},
		{160,	86},
		{290,	660},
		{450,	35},
		{450,	823},
		{534,	860},
		{588,	35},
		{988,	800},
		{988,	60},
		{1100, 725},
		{1235, 758},
		{1293, 814},
		{1312, 583},
		{1410, 410},
		{1340, 170},
		{1380, 60},
		{450, 367},
		{510, 493},
		{694, 617},
		{756, 704},
		{832, 598},
		{1015, 434},
		{829, 283},
		{933, 215},
	};
	
	private static final Set excludeRewards = new HashSet();
	static {
		excludeRewards.add(RewardType.YUANBAO);
	}
		
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];

		Reward reward = null;
		double ratio = MathUtil.nextDouble();
		
		//Check RewardPojo config
		TreeSet rewardConfigs = RewardManager.getInstance().getRewardPojoForType(
				user, RewardPojoType.PVE_DIAMOND);
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
			if ( ratio > 0.5 ) {
				String itemId = REWARD_ID[0];
				ItemPojo item = ItemManager.getInstance().getItemById(itemId);
				reward = RewardManager.getRewardItem(item);
			} else {
				String itemId = REWARD_ID[(int)(REWARD_ID.length*MathUtil.nextDouble())];
				ItemPojo item = ItemManager.getInstance().getItemById(itemId);
				reward = RewardManager.getRewardItem(item);
			}
		}
		
		int index = (int)(POINTS.length*MathUtil.nextDouble());
		int[] p = POINTS[index];
		reward.setX(p[0]);
		reward.setY(p[1]);

		ArrayList list = new ArrayList(1);
		list.add(reward);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
