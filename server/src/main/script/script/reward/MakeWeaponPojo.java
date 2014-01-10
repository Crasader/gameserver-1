package script.reward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 生成随机的装备,生成的原则如下：
 * 1. 普通白色装备 3-5
 * 3. 普通蓝色装备为 5-8
 * 5. 普通粉色装备为 6-10
 * 7. 普通橙色装备为 8-12
 * 9. 普通紫色装备为 12-14
 * 
 * 2. 精良白色装备 5-8
 * 4. 精良蓝色装备为 5-10
 * 6. 精良粉色装备为 6-12
 * 8. 精良橙色装备为 10-12
 * 10.精良紫色装备为 12-15
 * 
			 * q=5.0
				8: 17.22%
				9: 16.74%
				10: 15.38%
				11: 12.76%
				12: 12.08%
				13: 10.14%
				14: 8.39%
				15: 7.29%
				
			 * q=4.0
				8: 19.07%
				9: 16.76%
				10: 16.06%
				11: 13.48%
				12: 11.52%
				13: 9.44%
				14: 7.73%
				15: 5.94%
 * 
 * 
 * @author wangqi
 *
 */
public class MakeWeaponPojo {
		
	private static int[][] NORMAL_STR_LEVEL = new int[][]{
		new int[]{3, 12},
		new int[]{4, 12},
		new int[]{5, 12},
		new int[]{6, 12},
		new int[]{8, 14},
		new int[]{12, 15},
	};
	private static int[][] PRO_STR_LEVEL = new int[][]{
		new int[]{8, 12},
		new int[]{8, 12},
		new int[]{10, 12},
		new int[]{10, 12},
		new int[]{10, 13},
		new int[]{12, 15},
	};
	
	private static final Set excludeRewards = new HashSet();
	static {
		excludeRewards.add(RewardType.EXP);
	}

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		WeaponPojo weapon = (WeaponPojo)parameters[0];
		WeaponColor weaponColor = (WeaponColor)parameters[1];
		int quality = weapon.getQuality();
		
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
		maxStrengthLevel = minStrLevel + (int)(MathUtil.nextDouble()*
				(maxStrengthLevel-minStrLevel));
		
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

		ArrayList list = new ArrayList();
		list.add(maxStrengthLevel);
		list.add(slotList);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
