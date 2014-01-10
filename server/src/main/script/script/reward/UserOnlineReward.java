package script.reward;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class UserOnlineReward {
	
	private static final Logger logger = LoggerFactory.getLogger(UserOnlineReward.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		int stepId = (Integer)parameters[1];
		int slot = 1;
		
		ArrayList stoneTypes = new ArrayList();
	  //水神石typei d
		stoneTypes.add(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.CRAFT_STONE_LUCK,    20001));
		stoneTypes.add(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.CRAFT_STONE_DEFEND,  20002));
		stoneTypes.add(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.CRAFT_STONE_AGILITY, 20003));
		stoneTypes.add(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.CRAFT_STONE_ATTACK,  20004));
		stoneTypes.add(GameDataManager.getInstance().getGameDataAsInt(GameDataKey.CRAFT_STONE_STRENGTH,20005));
				
		ArrayList rewards = new ArrayList(slot);
		int userLevel = user.getLevel();
		
		int stoneLevel = 1;
		int stoneCount = 1;
		if ( userLevel < 10 ) {
			stoneLevel = 1;
			stoneCount = 1;
		} else if ( userLevel < 20 ) {
			stoneLevel = 2;
			stoneCount = 2;
		} else if ( userLevel < 25 ) {
			stoneLevel = 2;
			stoneCount = 3;
		} else if ( userLevel < 30 ) {
			stoneLevel = 3;
			stoneCount = 2;
		} else if ( userLevel > 30 ) {
			stoneLevel = 4;
			stoneCount = 4;
		}
		
		if ( stoneLevel + stepId > 4 ) {
			stoneCount += stepId/2;
		} else {
			stoneLevel += stepId;
		}
		
		//Two stones
		for ( int i = 0; i<2; i++ ) {
			Collection pickup = MathUtil.randomPickGaussion(stoneTypes, 1, 10.0);
			int stoneTypeId = (Integer)pickup.iterator().next();
			String stoneId = ItemPojo.toId(stoneTypeId, stoneLevel);
			
			Reward reward = new Reward();
			reward.setPropId(stoneId);
			reward.setPropLevel(stoneLevel);
			reward.setPropCount(stoneCount);
			reward.setType(RewardType.ITEM);
			rewards.add(reward);
		}
		
	  //获奖的道具ID
	  //golden:-1
	  //medal:-2
	  //voucher:-3
	  //medal:-4
		//exp: -5
		
		//Exp
		{ 
			int standLevel = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.STANDARD_USER_EXP, 10);
			ArrayList exps = new ArrayList();
			for ( int i=0; i<10; i++ ) {
				exps.add(standLevel*userLevel+100);
			}
			Collection pickup = MathUtil.randomPickGaussion(exps, 1, 3.0);
			int stoneTypeId = (Integer)pickup.iterator().next();
			String stoneId = ItemPojo.toId(stoneTypeId, stoneLevel);

			Integer count = (Integer)pickup.iterator().next();
			Reward reward = new Reward();
			reward.setPropId("-5");
			reward.setPropCount(count);
			reward.setType(RewardType.EXP);
			rewards.add(reward);
		}
		
		//Voucher 
		{
			ArrayList vouchers = new ArrayList();
			for ( int i=1; i<=10; i++ ) {
				vouchers.add(i*10);
			}
			Collection pickup = MathUtil.randomPickGaussion(vouchers, 1, 2.0);
			
			Integer count = (Integer)pickup.iterator().next();
			Reward reward = new Reward();
			reward.setPropId("-3");
			reward.setPropCount(count);
			reward.setType(RewardType.VOUCHER);
			rewards.add(reward);
		}
		
		user.setOnlineRewards(rewards);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(rewards);
		return result;
	}
	
}
