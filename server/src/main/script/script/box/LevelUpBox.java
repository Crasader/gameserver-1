package script.box;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is a type of ItemPojo box, that when user's level reach the box level,
 * the user can open it and get all the rewards in the box.
 * 
 * @author wangqi
 *
 */
public class LevelUpBox {

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
		
		int userLevel = user.getLevel();
		int boxLevel = itemPojo.getLevel();
		PickRewardResult pickResult = PickRewardResult.NOTHING;
		
		if ( userLevel >= boxLevel ) {
			ArrayList rewards = itemPojo.getRewards();
			pickResult = Box.openBox(user, rewards, pew);
			/*
			 **
			 * If the user is vip, then double the rewards
			 *
			if ( user.isVip() ) {
				pickResult = Box.openBox(user, rewards, pew);
			}
			*/
		} else {
			pickResult = PickRewardResult.LEVEL_FAIL;
			Object[] params = {boxLevel};
			String message = Text.text("box.levelupbox.fail", params);
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user, message, Action.NOOP, Type.NORMAL);
		}
		
		ArrayList list = new ArrayList();
		list.add(pickResult);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
