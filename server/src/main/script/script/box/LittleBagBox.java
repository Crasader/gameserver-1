package script.box;

import java.util.ArrayList;
import java.util.Collection;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.reward.Box;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * 大背包，打开后扩充70个背包项目
 * 
 * 
 * @author wangqi
 *
 */
public class LittleBagBox {

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
		
		//Enlarge user's bag
		Bag bag = user.getBag();
		int newLimit = Math.min(bag.getMaxCount()+30, 100);
		if ( newLimit > bag.getMaxCount() ) {
			bag.setMaxCount(newLimit);
			bag.removeOtherPropDatas(pew);
			UserManager.getInstance().saveUserBag(user, false);
		
			BseUseProp.Builder builder = BseUseProp.newBuilder();
			builder.setSuccessed(PickRewardResult.SUCCESS.ordinal());
			builder.addDelPew(pew);
			GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(user, 
					"prop.bag.limit", Action.NOOP, new Object[]{itemPojo.getName(), 100});
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

}
