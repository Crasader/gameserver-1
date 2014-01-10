package script.box;

import java.util.ArrayList;
import java.util.Collection;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 宝贝大陆上生长的一种神奇果实，食用之后可立即获得100经验！
 * 
 * 
 * @author wangqi
 *
 */
public class ExpBox {

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
		
		ArrayList list = new ArrayList();
		list.add(pickResult);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
