package script;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.VipPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.CaishenManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 本类根据玩家累计充值的金额，给予相应的VIP等级，并设置不同等级的福利
 * 
 * @author wangqi
 *
 */
public class VipChargeLevel {
	
	private static final Logger logger = LoggerFactory.getLogger(VipChargeLevel.class);

	//TODO
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		int boughtYuanbao = (Integer)parameters[1];
		int originLevel = user.getViplevel();
		int chargedYuanbao = user.getChargedYuanbao();
		int targetLevel = 0;
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		if ( vip != null ) {
			targetLevel = vip.getId();
			if ( !user.isVip() ) {
				user.setIsvip(true);
			}
			user.setViplevel(targetLevel);
			if ( targetLevel > originLevel ) {
				//Process VIP level task
				TaskManager.getInstance().processUserTasks(user, TaskHook.VIP_LEVEL, new Object[targetLevel]);
				
				logger.debug("User {} update from vip {} to {}", new Object[]{user.getRoleName(), originLevel, targetLevel} );
				//Send notify
				String notifyMessage = Text.text("notice.vip", 
						new Object[]{user.getRoleName(), targetLevel});
				ChatManager.getInstance().processChatToWorldAsyn(null, notifyMessage);
				
				//1st: Enlarge the bag space. It can avoid the situation that vip gift cannot be put into bag because it is full.
				int[] bagSpace = GameDataManager.getInstance().getGameDataAsIntArray(GameDataKey.VIP_BAG_SPACE);
				Bag bag = user.getBag();
				bag.setMaxCount(bagSpace[targetLevel-1]);
				
				//2nd, Add the VIP gift to user's bag.
				ArrayList giftIds = new ArrayList();
				String[] vipGifts = GameDataManager.getInstance().getGameDataAsStringArray(GameDataKey.VIP_GIFT_BOX_ID);
				int startIndex = originLevel;
				if ( originLevel < 0 ) {
					startIndex = 0;
				}
				for ( int level=startIndex; level<targetLevel; level++ ) {
					String vipGiftId = vipGifts[level];
					ItemPojo item = ItemManager.getInstance().getItemById(vipGiftId);
					if ( item != null ) {
						Reward gift = RewardManager.getInstance().getRewardItem(item);
						Integer vipLevel = new Integer(level+1);
						String subject = Text.text("vip.gift.subject", new Object[]{vipLevel});
						String content = Text.text("vip.gift.content", new Object[]{targetLevel, vipLevel});
						ArrayList gifts = new ArrayList();
						gifts.add(gift);
						MailMessageManager.getInstance().sendMail(null, user.get_id(), subject, content, gifts, true);
					}
				}
				
				//3rd: Reset user's status
				int roleBuyCount = VipManager.getInstance().getVipLevelRoleActionBuyCount(user);
				long currentTimeMillis = System.currentTimeMillis();
				RoleActionManager.getInstance().setRoleActionBuyCount(user, roleBuyCount, currentTimeMillis);
				
				int caishenBuyCount = VipManager.getInstance().getVipLevelCaishenBuyCount(user);
				CaishenManager.getInstance().setCaishenPrayCount(user, caishenBuyCount, currentTimeMillis);
				
				//Save user's bag
				UserManager.getInstance().saveUserBag(user, false);
				
				//Notify stat server that user's vip level is changed.
				StatClient.getIntance().sendDataToStatServer(user, StatAction.VipLevelUp, new Object[]{targetLevel, originLevel});
				
				UserActionManager.getInstance().addUserAction(user.getRoleName(), 
						UserActionKey.Charge, String.valueOf(boughtYuanbao), String.valueOf(targetLevel));
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
