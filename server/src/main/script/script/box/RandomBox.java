package script.box;

import java.util.ArrayList;
import java.util.Collection;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is a type of ItemPojo box, that when user opens it, a random
 * item will be given to user.
 * 
 * @author wangqi
 *
 */
public class RandomBox {

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
		
		int count = itemPojo.getCount();
		if ( count <= 0 ) {
			count = 1;
		}
		double q = itemPojo.getQ();
		if ( q < 1.0 ) {
			q = 1.0;
		}
		
		ArrayList rewards = itemPojo.getRewards();
		Collection pickedRewards = MathUtil.randomPickGaussion(rewards, count, q);
		PickRewardResult pickResult = Box.openBox(user, pickedRewards, pew);
		
		String content = null;
		if ( itemPojo.isBroadcast() ) {
			try {
				if ( pickedRewards.size() > 0 ) {
					Object obj = pickedRewards.iterator().next();
					Reward reward = (Reward)obj;
					if ( reward.getType() == RewardType.ITEM ) {
						ItemPojo item = ItemManager.getInstance().getItemById(reward.getId());
						content = Text.text("notice.openItemBox", 
								new Object[]{user.getRoleName(), item.getName(), count});
					} else if ( reward.getType() == RewardType.WEAPON ) {
						WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(
								reward.getTypeId(), user.getLevel());
						if ( reward.getLevel() > 0 ) {
							content = Text.text("notice.openEquipBox", 
									new Object[]{user.getRoleName(), reward.getLevel(), weapon.getName()});
						}
					}
				}
			} catch (Exception e) {
			}
		}
		ArrayList list = new ArrayList();
		list.add(pickResult);
		if ( content != null ) {
			list.add(content);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
