package script.box;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 使用后,您的战斗可一直获得双倍经验,直到退出游戏为止。
 * 
 * @author wangqi
 *
 */
public class DoubleExpBox {

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
		final User user = (User)parameters[0];
		final ItemPojo itemPojo = (ItemPojo)parameters[1];
		final int pew = (Integer)parameters[2];
		
		/**
		 * Set the expRate to 2.0
		 * It will be used in 'script.BattleOver'
		 */
		final int battleCount = 20;
		final float expRate = 1.0f;
		int expireHour = 3;
		final int expireSeconds = expireHour * 3600;
		
		String message = Text.text("doubleexp.confirm", new Object[]{battleCount, expireHour});
		String type = "doubleexp";
		
		ConfirmManager.getInstance().sendConfirmMessage(user, message, type, new ConfirmManager.ConfirmCallback() {
			
			public void callback(User user, int selected) {
				if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
					ActivityManager.getInstance().setActivityExpRate(
							user, battleCount, expRate, expireSeconds);
					
					//Remove the item from user's bag
					Bag bag = user.getBag();
					bag.removeOtherPropDatas(pew);
					UserManager.getInstance().saveUserBag(user, false);
					
					BseUseProp.Builder builder = BseUseProp.newBuilder();
					builder.setSuccessed(PickRewardResult.SUCCESS.ordinal());
					builder.addDelPew(pew);
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
					
					SysMessageManager.getInstance().sendClientInfoMessage(user, "doubleexp.enable", Type.CONFIRM);
				} else {
					BseUseProp.Builder builder = BseUseProp.newBuilder();
					builder.setSuccessed(PickRewardResult.CANCEL.ordinal());
					GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
				}
			}
		});
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
