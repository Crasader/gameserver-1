package script.reward;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * BOSS挑战成功后显示的武器奖励
 */
public class BossWeaponReward {
	
	private static EquipType[] TYPES = new EquipType[]{
		EquipType.CLOTHES, EquipType.BRACELET, EquipType.RING, EquipType.NECKLACE, EquipType.WEAPON, EquipType.WEAPON
	};
	
	private static int[][] NORMAL_STR_LEVEL = new int[][]{
		new int[]{3, 5},
		new int[]{4, 6},
		new int[]{5, 8},
		new int[]{6, 10},
		new int[]{8, 12},
		new int[]{12, 14},
	};
	private static int[][] PRO_STR_LEVEL = new int[][]{
		new int[]{5, 8},
		new int[]{5, 9},
		new int[]{6, 10},
		new int[]{6, 12},
		new int[]{10, 12},
		new int[]{12, 15},
	};
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		Boss boss = (Boss)parameters[1];
		int count = (Integer)parameters[2];
		
		BossPojo bossPojo = boss.getBossPojo();
		ArrayList rewards = new ArrayList();

		for ( int i=0; i<count; i++ ) {
			PropData propData = null;
			WeaponColor weaponColor = WeaponColor.WHITE;
			int slot = 0;
			int quality = 1;
			if ( bossPojo.getLevel() == 0 ) {
				/**
				 * 简单难度产生 
				 * 1. 白色-绿色 装备
				 * 2. 强化等级不高于8
				 * 3. 插槽数低于3
				 */
				slot = MathUtil.nextGaussionInt(0, 4, 3.0);
				weaponColor = WeaponColor.values()[MathUtil.nextGaussionInt(0, WeaponColor.BLUE.ordinal())];
				if ( MathUtil.nextDouble() < 0.3 ) {
					quality = 2;
				}
			} else if ( bossPojo.getLevel() == 1 ) {
				/**
				 * 简单难度产生 
				 * 1. 白色-蓝色 装备
				 * 2. 强化等级不高于10
				 * 3. 插槽数在4个左右
				 */
				slot = MathUtil.nextGaussionInt(1, 5, 3.0);
				weaponColor = WeaponColor.values()[MathUtil.nextGaussionInt(0, WeaponColor.PINK.ordinal())];
				if ( MathUtil.nextDouble() < 0.3 ) {
					quality = 2;
				}
			} else if ( bossPojo.getLevel() == 2 ) {
				/**
				 * 简单难度产生 
				 * 1. 白色-蓝色 装备
				 * 2. 强化等级不高于10
				 * 3. 插槽数在4个左右
				 */
				slot = MathUtil.nextGaussionInt(2, 6, 3.0);
				weaponColor = WeaponColor.values()[MathUtil.nextGaussionInt(0, WeaponColor.ORGANCE.ordinal())];
				quality = 2;
			}
			Reward reward = makeReward(user, weaponColor, quality);
			rewards.add(reward);
		}

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(rewards);
		return result;
	}

	/**
	 * Make the reward for weapon.
	 * @param user
	 * @param weaponColor
	 * @param quality
	 * @return
	 */
	private static final Reward makeReward(User user, WeaponColor weaponColor, int quality) {
		int maxStrengthLevel = 8;
		int slot = 0;
		int minStrLevel = 3;
		int maxStrLevel = 5;
		if ( quality == 1 ) {
			minStrLevel = NORMAL_STR_LEVEL[weaponColor.ordinal()][0];
			maxStrLevel = NORMAL_STR_LEVEL[weaponColor.ordinal()][1]+1;
		} else if ( quality == 2 ) {
			minStrLevel = PRO_STR_LEVEL[weaponColor.ordinal()][0];
			maxStrLevel = PRO_STR_LEVEL[weaponColor.ordinal()][1]+1;
		}
		maxStrengthLevel = MathUtil.nextGaussionInt(minStrLevel, maxStrengthLevel, 5.0);
		
		/**
		 * 0-6 slot q=3.0
				0: 24.41%
				1: 22.61%
				2: 18.36%
				3: 13.83%
				4: 10.31%
				5: 6.66%
				6: 3.82%
		 */
		slot = MathUtil.nextGaussionInt(0, 7, 3.0);
		ArrayList slotList = RewardManager.getInstance().makeWeaponSlot(slot);
		
		WeaponPojo weapon = null;
		for ( int i=0; i<100; i++) {
			EquipType type = TYPES[(int)(MathUtil.nextDouble()*TYPES.length)];
			weapon = EquipManager.getInstance().getRandomWeapon(user, type, quality);
			if ( weapon != null ) {
				break;
			}
		}
		PropData propData = weapon.toPropData(30, 
				weaponColor, maxStrengthLevel, slotList);
		
		return RewardManager.getInstance().convertPropDataToReward(propData);
	}
}
