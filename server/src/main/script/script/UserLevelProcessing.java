package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * When an user's level upgrades, the game will send user a gift box.
 * 
 * @author wangqi
 *
 */
public class UserLevelProcessing {
	
	/**
	 * Parameters:
	 * 1. The User object
	 * 
	 * @param parameters
	 * @return
	 */
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		ScriptResult.Type resultType = ScriptResult.Type.SUCCESS;
		
		User user = (User)parameters[0];
		
		int currentLevel = user.getLevel()-1;
		int giftTypeId = GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.BOX_LEVELUP_TYPEID, 25011);
		ItemPojo levelGiftBox = ItemManager.getInstance().
				getItemByTypeIdAndLevel(String.valueOf(giftTypeId), user.getLevel());
		if ( levelGiftBox != null ) {
			Reward reward = RewardManager.getRewardItem(levelGiftBox);
			ArrayList gifts = new ArrayList();
			gifts.add(reward);
			String subject = Text.text("levelup.subject", new Object[]{user.getLevel()});
			String content = Text.text("levelup.content", new Object[]{user.getLevel()});
			MailMessageManager.getInstance().sendMail(null, user.get_id(), subject, content, gifts, true);
		}
		
		if ( !user.isAI() ) {
			processUnlock(user);
			
			//Update user's roleAction
			RoleActionManager.getInstance().userLevelUpRoleActionGrow(user, 
					System.currentTimeMillis(), true);
		}
		
		/**
		 * 弹出道具解锁的说明
		 */
		Collection rewardLevelPojos = RewardManager.getInstance().
				getRewardLevelPojoByUserLevel(user.getLevel());
		if ( rewardLevelPojos != null && rewardLevelPojos.size() > 0 ) {
			StringBuilder buf = new StringBuilder(50);
			int count = 0;
			for (Iterator iter = rewardLevelPojos.iterator(); iter.hasNext();) {
				if ( count++ < 4 ) {
					RewardLevelPojo rewardLevel = (RewardLevelPojo) iter.next();
					buf.append("@").append(rewardLevel.getWeaponId(user.getLevel())).append("@");
				} else {
					break;
				}
			}
			if ( rewardLevelPojos.size()> 4 ) {
				buf.append("...");
			}
			String message = Text.text("user.level.up.unlock", user.getLevel(), buf.toString());
			ConfirmManager.getInstance().sendConfirmMessage(user, message, "userlevelup", null);
		}
		
		result = new ScriptResult();
		result.setType(resultType);
		result.setResult(null);
		return result;
	}
	
	public static final void processUnlock(User user) {
		/**
		 * Check if some of the given mode should be unlocked.
		 */
		if ( user.getLevel() >= 15 ) {
			//Unlock desk mode game
  		checkUnlock(user, GameFuncType.Room, RoomType.DESK_ROOM.ordinal());
		}
		if ( user.getLevel() >= 10 ) {
			//Unlock friend mode game
  		checkUnlock(user, GameFuncType.Room, RoomType.FRIEND_ROOM.ordinal());
		}
		if ( user.getLevel() >= 5 ) {
			//Unlock multi mode game
  		checkUnlock(user, GameFuncType.Room, RoomType.MULTI_ROOM.ordinal());
		}
		if ( user.getLevel() >= 2 ) {
			//Unlock single mode game
  		checkUnlock(user, GameFuncType.Room, RoomType.PVE_ROOM.ordinal());
		}
		/*
		} else if ( user.getLevel() >= 1 ) {
			//Unlock single mode game
  		Unlock unlock = new Unlock();
  		unlock.setId(user.get_id());
  		unlock.setFuncType(GameFuncType.Room);
  		unlock.setFuncValue(RoomType.SINGLE_ROOM.ordinal());
  		GameContext.getInstance().getUserManager().addUserNewUnlock(user, unlock);
		}
		*/
	}

	/**
	 * Check if the given value exists. If not, create one.
	 * 
	 * @param user
	 * @param gameFunc
	 * @param value
	 */
	public final static void checkUnlock(User user, GameFuncType gameFunc, int value) {
		Collection unlocks = user.getUnlocks();
		boolean found = false;
		if ( unlocks != null ) {
			for (Iterator iterator = unlocks.iterator(); iterator.hasNext();) {
				Unlock unlock = (Unlock) iterator.next();
				if ( unlock.getFuncType() == gameFunc && unlock.getFuncValue() == value ) {
					found = true;
					break;
				}
			}
			if ( !found ) {
	  		Unlock unlock = new Unlock();
	  		unlock.setId(user.get_id());
	  		unlock.setFuncType(gameFunc);
	  		unlock.setFuncValue(value);
	  		GameContext.getInstance().getUserManager().addUserNewUnlock(user, unlock);
			}
		}
	}
}
