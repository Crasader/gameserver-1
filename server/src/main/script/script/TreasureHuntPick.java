package script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.treasure.TreasurePojo;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 随机抽取一项物品
 * 
 * @author wangqi
 *
 */
public class TreasureHuntPick {
	
	private static final String TREASURE_HUNT_PICK = "treasure:pick:";
	private static final String FIELD_TOTALCOUNT = "totalcount";
	private static final String FIELD_NEXTREWARDCOUNT = "nextrewardcount";
	private static final String FIELD_TOTALREWARD = "totalreward";
	//今天的日期
	private static final String FIELD_TODAYSTR = "todaystr";
	//今天未抽中奖品的次数
	private static final String FIELD_TODAYVALUE = "todayvalue";
	
	private static final Set includeSet = new HashSet();
	static {
		includeSet.add(EquipType.BUBBLE);
		includeSet.add(EquipType.DECORATION);
		includeSet.add(EquipType.FACE);
		includeSet.add(EquipType.HAIR);
		includeSet.add(EquipType.HAT);
		includeSet.add(EquipType.EXPRESSION);
		includeSet.add(EquipType.GLASSES);
		includeSet.add(EquipType.JEWELRY);
	}


	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		String roleName = user.getRoleName();
		String key = StringUtil.concat(new String[]{TREASURE_HUNT_PICK, roleName});
		Jedis jedisDB = JedisFactory.getJedisDB();
		String totalPickCountStr = jedisDB.hget(key, FIELD_TOTALCOUNT);
		String lastRewardCountStr = jedisDB.hget(key, FIELD_NEXTREWARDCOUNT);
		String totalRewardStr = jedisDB.hget(key, FIELD_TOTALREWARD);
		String todayStr = jedisDB.hget(key, FIELD_TODAYSTR);
		String todayValueStr = jedisDB.hget(key, FIELD_TODAYVALUE);
		int totalPickCount = StringUtil.toInt(totalPickCountStr, 0); 
		int nextRewardCount = StringUtil.toInt(lastRewardCountStr, 0);
		int totalReward = StringUtil.toInt(totalRewardStr, 0);
		String today = DateUtil.getToday(System.currentTimeMillis());
		int todayValue = 0;
		if ( today.equals(todayStr) ) {
			todayValue = StringUtil.toInt(todayValueStr, 0) + 1;
		} else {
			jedisDB.hset(key, FIELD_TODAYSTR, today);
		}
		jedisDB.hset(key, FIELD_TOTALCOUNT, String.valueOf(totalPickCount+1));
		//抽奖数据如果玩家7天未抽奖则重置
		jedisDB.expire(key, 86400*7);
		
		TreasurePojo treasure = (TreasurePojo)parameters[1];
		int d = Math.round((float)(MathUtil.nextDouble()*1000));
		List ratios = treasure.getRatios();
		List rewards = treasure.getGifts();
		Reward propData = null;
		int rewardIndex = 0;
		int length = ratios.size();
		for (int i=0; i<length; i++) {
			int r = (int)((Float)ratios.get(i)*10);
			if ( d<r ) {
				//检查预计下次抽中的次数
				if ( i<3 && totalPickCount >= nextRewardCount ) {
					propData = (Reward)rewards.get(i);
					rewardIndex = i+1;
					if ( totalReward < 10 ) {
						nextRewardCount = totalPickCount + 10*totalReward;
					} else {
						nextRewardCount = totalPickCount + 100;
					}
					jedisDB.hset(key, FIELD_NEXTREWARDCOUNT, String.valueOf(nextRewardCount));
					jedisDB.hset(key, FIELD_TOTALREWARD, String.valueOf(totalReward+1));
					todayValue = 0;
					break;
				}
			}
		}
		
		//保存今日抽奖的总次数
		jedisDB.hset(key, FIELD_TODAYVALUE, String.valueOf(todayValue));
		
		if ( propData == null ) {
			if ( totalPickCount == 10 ) {
				//新手第10次抽中奖品
				rewardIndex = rewards.size();
				propData = (Reward)rewards.get(rewardIndex-1);
			} else {
				if ( todayValue == 800 ) {
					//今天已经累计800次未抽中奖品了，给头奖
					propData = (Reward)rewards.get(0);
					rewardIndex = 1;
				}
				if ( todayValue == 300 ) {
					//今天已经累计300次未抽中奖品了，给四等奖
					propData = (Reward)rewards.get(3);
					rewardIndex = 4;
				}
			}
		}
		Reward reward = null;
		//String rewardName = Constant.EMPTY;
		if ( propData == null ) {
			//No big reward
			if ( d < 20 ) {
				reward = RewardManager.getInstance().getRewardYuanbao();
			} else if ( d < 150 ) {
				reward = RewardManager.getInstance().generateRandomStone(user);
			} else if ( d < 450 ) {
				reward = RewardManager.getInstance().getRewardExp(user);
			} else if ( d < 750 ) {
				reward = RewardManager.getInstance().generateRandomWeapon(user, includeSet);
			} else {
				reward = RewardManager.getInstance().getRewardGolden(user);
			}
		} else {
			reward = new Reward();
			reward.setId(propData.getPropId());
			String name = null;
			if ( propData.getType() == RewardType.WEAPON ) {
				reward.setType(RewardType.WEAPON);
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(propData.getPropId());
				if ( weapon != null ) {
					name = weapon.getName();
				}
			} else {
				reward.setType(RewardType.ITEM);
				ItemPojo item = ItemManager.getInstance().getItemById(propData.getPropId());
				if ( item != null ) {
					name = item.getName();
				}
			}
			reward.setPropCount(1);
			reward.setPropColor(propData.getPropColor());
			reward.setPropLevel(propData.getPropLevel());
			reward.setPropIndate(propData.getPropIndate());
			int maxlv = MathUtil.nextGaussionInt(5, 13);
			reward.setMaxStrength(maxlv);
			String info = null;
			if ( reward.getPropLevel() >= 7 ) {
				if ( reward.getPropColor() == WeaponColor.PINK ) {
					info = Text.text("notice.treasure.strength.pink", new Object[]{user.getRoleName(), 
							reward.getPropLevel(), name});
				} else if ( reward.getPropColor() == WeaponColor.ORGANCE ) {
					info = Text.text("notice.treasure.strength.orange", new Object[]{user.getRoleName(), 
							reward.getPropLevel(), name});
				} else {
					info = Text.text("notice.treasure.strength", new Object[]{user.getRoleName(), 
							reward.getPropLevel(), name});
				}
			} else {
				if ( reward.getPropColor() == WeaponColor.PINK ) {
					info = Text.text("notice.treasure.pink", new Object[]{user.getRoleName(), name});
				} else if ( reward.getPropColor() == WeaponColor.ORGANCE ) {
					info = Text.text("notice.treasure.orange", new Object[]{user.getRoleName(), name});
				}
			}
			if ( info != null ) {
				ChatManager.getInstance().processChatToWorldAsyn(null, info);
			}
		}
		
//		try {
//			if ( reward.getType() == RewardType.WEAPON ) {
//				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(reward.getPropId());
//				if ( weapon != null ) {
//					rewardName = weapon.getName();
//				}
//			} else if ( reward.getType() == RewardType.ITEM ) {
//				ItemPojo item = ItemManager.getInstance().getItemById(reward.getPropId() );
//				if ( item != null ) {
//					rewardName = item.getName();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		ArrayList list = new ArrayList();
		list.add(reward);
		list.add(rewardIndex);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
