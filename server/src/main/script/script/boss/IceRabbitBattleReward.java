package script.boss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 战斗结束后的抽奖界面，一般是16个道具，抽奖原则如下：
 * 金币：可以多出，多占一些位置，数额根据玩家等级上浮，0-200
 * 元宝：略出
 * 石头：出低等级石头
 * 装备：
			WEAPON      少出，只出低等级装备，根据玩家背包判断
			EXPRESSION  可出
			FACE,       可出
			DECORATION, 可出
			HAIR,       可出
			WING,       可出
			CLOTHES,    可出
			HAT,        可出
			GLASSES,    可出
			JEWELRY,    可出
			BUBBLE,     不出
			SUIT,       不出
			OFFHANDWEAPON, 不出
			OTHER,      可出
			ITEM,       不出
			GIFT_PACK;  不出
			
	 生成的装备有效期
 * 
 * @author wangqi
 *
 */
public class IceRabbitBattleReward {
		
	/**
		30004	水神石宝箱
		30005	土神石宝箱
		30006	风神石宝箱
		30007	火神石宝箱
		30008	强化石宝箱
		30009	金币大宝箱
		30010	装备大宝箱
		30011	经验大宝箱
	 */
	private static String[] BOXES = new String[]{
		"30004", "30005", "30006", "30007", "30008", "30009", "30010", "30011"
	};

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		int slotSize = (Integer)parameters[1];
		
		//Check RewardPojo config
		Reward reward = null;
		double ratio = MathUtil.nextDouble();
		
		TreeSet rewardConfigs = RewardManager.getInstance().getRewardPojoForType(user, RewardPojoType.PVE_BATTLE_REWARD);
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
		/**
		 * Disable the yuanbao rewards
		 */
		ArrayList list = new ArrayList(slotSize);
		/*
		if ( MathUtil.nextDouble() < 0.1 ) {
			for ( int i=0; i<slotSize; i++ ) {
				int yuanbao = MathUtil.nextGaussionInt(1, 100, 10.0);
				Reward r = null;
				int random = (int)(MathUtil.nextDouble() * 6) + 5;
				if ( i % random == 0 ) {
					r = RewardManager.getInstance().getRewardYuanbao(yuanbao);
				} else {
					r = RewardManager.getInstance().getRewardYuanbao();
				}
				list.add(r);
			}
		} else {
			for ( int i=0; i<slotSize; i++ ) {
				ItemPojo item = ItemManager.getInstance().getItemById(BOXES[(int)(MathUtil.nextDouble()*BOXES.length)]);
				Reward r = RewardManager.getInstance().getRewardItem(item);
				list.add(r);
			}
		}
		*/
		for ( int i=0; i<slotSize; i++ ) {
			ItemPojo item = ItemManager.getInstance().getItemById(BOXES[(int)(MathUtil.nextDouble()*BOXES.length)]);
			Reward r = RewardManager.getInstance().getRewardItem(item);
			list.add(r);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
