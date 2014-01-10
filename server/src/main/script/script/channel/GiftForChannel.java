package script.channel;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 360渠道首次注册需要发放一个礼包
 * 
 * @author wangqi
 *
 */
public class GiftForChannel {
	
	/**
	 * 29020	360特权礼包	Dalibao0001	
	 * 360特权礼包打开可获得：
	 *   元宝*10、小喇叭*20、强化石Lv4*10、火神石Lv4*10、水神石Lv4*10、土神石Lv4*10、幸运符15%*5、绿色熔炼符*2.
	 */
	private static final String ITEM_ID_360 = "29020";
	private static final String ITEM_ID_DANGLE = "29022";
	private static final String ITEM_ID_UC = "29030";

	/**
	 * 
	 * @param parameters: User, ItemPojo, Pew(in user bag)
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		String channel = user.getChannel();
		if ( StringUtil.checkNotEmpty(channel) ) {
			if ( channel.contains("_360_") ) {
				sendGift(user, ITEM_ID_360, Text.text("360.gift.subject"), Text.text("360.gift.content"));
			} else if ( channel.contains("_dangle_") ) {
				sendGift(user, ITEM_ID_DANGLE, Text.text("dangle.gift.subject"), Text.text("dangle.gift.content"));
			} else if ( channel.contains("_uc_") ) {
				sendGift(user, ITEM_ID_UC, "欢迎尊贵的UC玩家，我们为您准备了丰厚的游戏大礼包", "感谢您的访问，我们为您准备了游戏大礼包一份，请点击领取。");
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * @param user
	 */
	private static void sendGift(User user, String giftId, String subject, String content) {
		ItemPojo item = ItemManager.getInstance().getItemById(giftId);
		if ( item != null ) {
			Reward reward = RewardManager.getInstance().getRewardItem(item);
			ArrayList rewards = new ArrayList();
			rewards.add(reward);
			MailMessageManager.getInstance().sendMail(null, user.get_id(), 
					subject, content, rewards, true);
		}
	}

}
