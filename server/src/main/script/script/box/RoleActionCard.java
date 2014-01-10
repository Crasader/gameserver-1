package script.box;

import java.util.ArrayList;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 改名卡，可为玩家允许玩家修改游戏昵称
 * 
 * 
 * @author wangqi
 *
 */
public class RoleActionCard {

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

		Bag bag = user.getBag();
		PropData propData = bag.getOtherPropData(pew);
		
		if ( propData != null && "99999".equals(propData.getItemId()) ) {
			boolean success = RoleActionManager.getInstance().addRoleActionPoint(user, 
					System.currentTimeMillis(), true, 80);
			if ( success ) {
				bag.removeOtherPropDatas(pew);
				UserManager.getInstance().saveUserBag(user, false);
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
				
				//When result is success, the response is sent back by Box.openItem
				BseUseProp.Builder builder = BseUseProp.newBuilder();
				builder.setSuccessed(pew);
				GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
			}
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(
					user.getSessionKey(), "box.not.roleaction", Type.NORMAL);
		}

		ArrayList list = new ArrayList();
		list.add(PickRewardResult.SUCCESS);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}

}
