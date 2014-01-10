package script.box;

import java.util.ArrayList;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * It is a type of ItemPojo box, that when user opens it, all 
 * the rewards in the box will be given to that user.
 * 
 * @author wangqi
 *
 */
public class PackageBox {

	/**
	 * 
	 * @param parameters: User, ItemPojo, Pew(in user bag)
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		ItemPojo itemPojo = (ItemPojo)parameters[1];
		int pew = (Integer)parameters[2];

		ArrayList rewards = itemPojo.getRewards();
		ArrayList genderBox = new ArrayList();
		for (Iterator iter = rewards.iterator(); iter.hasNext();) {
			Reward reward = (Reward) iter.next();
			if ( reward.getType() == RewardType.WEAPON ) {
				WeaponPojo weapon = RewardManager.getInstance().convertRewardToWeapon(reward, user.getLevel());
				if ( weapon != null && (weapon.getSex() == Gender.ALL || weapon.getSex() == user.getGender()) ) {
					genderBox.add(reward);
				}
			} else {
				genderBox.add(reward);
			}
		}
		PickRewardResult pickResult = Box.openBox(user, genderBox, pew);

		ArrayList list = new ArrayList();
		list.add(pickResult);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
