package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.db.mongo.DailyLimitManager.BuyResult;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.entity.user.UserStatus;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseActionLimitBuy.BseActionLimitBuy;
import com.xinqihd.sns.gameserver.proto.XinqiBseActionLimitQuery.BseActionLimitQuery;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Every user will have a action point. It is like a Cool-Down time to 
 * control the game frequency.
 * 
 * @author wangqi
 * 
 */
public class RoleActionManager {

	private static final Logger logger = LoggerFactory
			.getLogger(RoleActionManager.class);

	private static RoleActionManager instance = new RoleActionManager();
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "action:";
	
	//User's current roleaction
	private static final String FIELD_ROLEACTION_KEY = "roleaction";
	
	//User's role action last changed timestamp
	private static final String FIELD_LASTTIME_KEY = "lasttime";
	
	//当日的时间做KEY
	private static final String FIELD_TODAY_STR_KEY = "todaystr";
	//当日可供购买的次数
	private static final String FIELD_TODAY_TOTAL_KEY = "todaytotal";
	//当日已经购买的次数，用来计算价格的加成
	private static final String FIELD_TODAY_BUY_KEY = "todaybuy";
	
	private boolean disableRoleAction = false;
	
	//The common underlying manager
	private DailyLimitManager manager = new DailyLimitManager(KEY_PREFIX);
	

	RoleActionManager() {
	}

	/**
	 * Get a singleton instance for this manager.
	 * 
	 * @return
	 */
	public static final RoleActionManager getInstance() {
		return instance;
	}

	/**
	 * @return the disableRoleAction
	 */
	public boolean isDisableRoleAction() {
		return manager.isDisableDailyLimit();
	}

	/**
	 * @param disableRoleAction the disableRoleAction to set
	 */
	public void setDisableRoleAction(boolean disableRoleAction) {
		this.manager.setDisableDailyLimit(disableRoleAction);
	}
	
	/**
	 * Set the roleAction point's buyCount everyday.
	 * The 'buyCount' limits the user's buying for extra role points. 
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean setRoleActionBuyCount(User user, int buyCount, long currentTimeMillis) {		
		return manager.resetDailyBuyLimit(user, buyCount, currentTimeMillis);
	}
	
	/**
	 * Query daily user's action point data.
	 * Redis stores the following data:
	 *   FIELD_TODAY_STR_KEY: 2012-09-13
	 *   FIELD_TODAY_TOTAL_KEY: <today's total buying limit>
	 *   FIELD_TODAY_BUY_KEY: <today's already buying count>
	 * @param user
	 * @param currentTimeMillis
	 */
	public int[] queryRoleActionLimit(User user, long currentTimeMillis, boolean sendBse) {
		
		int[] results = manager.queryRoleActionLimit(user, currentTimeMillis, ScriptHook.ROLE_ACTION_LIMIT_DAILY);
		if ( sendBse ) {
		  //当日可用的购买次数
			int buyCount = results[0];
			//购买的价格
			int buyPrice = results[1];
			//每次购买增加的体力点数
			int buyValue = results[2];
			//当日已经购买的次数，用来计算价格
			int buyTimes = results[3];
			int increasePerHour = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.ROLE_ACTION_GAIN_HOURLY, 5);
			String info = Text.text("action.grow", increasePerHour);
			BseActionLimitQuery.Builder bseActionQuery = BseActionLimitQuery.newBuilder();
			bseActionQuery.setBuycount(buyCount);
			bseActionQuery.setBuyprice(buyPrice);
			bseActionQuery.setBuyvalue(buyValue);
			bseActionQuery.setInfo(info);
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), bseActionQuery.build());
			//Send Stat
			StatClient.getIntance().sendDataToStatServer(user, StatAction.ActionLimitQuery,
					buyCount, buyValue);
			UserActionManager.getInstance().addUserAction(user.getRoleName(), UserActionKey.ActionLimitQuery);
		}
		return results;
	}
	
	/**
	 * Users buy extra roleActionPoints from game system.
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean buyRoleActionPoint(User user, long currentTimeMillis) {
		boolean success = false;
		String info = null;
		BuyResult buyResult = manager.buyNewOpportunity(user, 
				ScriptHook.ROLE_ACTION_LIMIT_DAILY, FIELD_ROLEACTION_KEY, currentTimeMillis);
		switch ( buyResult ) {
			case NO_CHANGE:
				info = Text.text("action.limit.nochance");
				success = false;
				break;
			case NO_MONEY:
				success = false;
				break;
			case SUCCESS:
				success = true;
				info = Text.text("action.buy.success");
				GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
				break;
			default:
				break;
		}
		BseActionLimitBuy.Builder bseActionQuery = BseActionLimitBuy.newBuilder();
		bseActionQuery.setSuccess(success);
		if ( info != null ) {
			bseActionQuery.setInfo(info);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), bseActionQuery.build());
		
		//Send Stat
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.ActionLimitBuy, buyResult);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), UserActionKey.ActionLimitBuy);
		
		return success;
	}

	/**
	 * Set the new value for given user. If this method returns false,
	 * then the given 'gain' cannot be added to system.
	 * 
	 * @param user
	 * @param gain: The incremented action point.
	 * @return
	 */
	public boolean consumeRoleActionPoint(User user, int gain, long currentTimeMillis) {
		return consumeRoleActionPoint(user, gain, currentTimeMillis, false);
	}
	
	/**
	 * 
	 * @param user
	 * @param gain
	 * @param currentTimeMillis
	 * @param sendBse
	 * @return
	 */
	public boolean consumeRoleActionPoint(User user, int gain, long currentTimeMillis, 
			boolean sendBse) {
		int increasePerHour = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_HOURLY, 5);
		boolean success = manager.consumeOpportunity(user, FIELD_ROLEACTION_KEY, gain, 
				user.getRoleTotalAction(), increasePerHour, currentTimeMillis);
		if ( sendBse ) {
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
		}
		return success;
	}
	
	/**
	 * When the user levels up, the role action point will grow 50 points if the current
	 * role action point is greater than 50. If the roleAction is greater than totalRoleAction,
	 * the value is un-changed.
	 *  
	 * @param user
	 * @param currentTimeMillis
	 * @param sendBse
	 * @return
	 */
	public boolean userLevelUpRoleActionGrow(User user, long currentTimeMillis, boolean sendBse) {
		boolean success = false;
		int increaseLevelup = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_LEVELUP, 50);
		success = addRoleActionPoint(user, currentTimeMillis, sendBse, increaseLevelup);
		return success;
	}

	/**
	 * @param user
	 * @param currentTimeMillis
	 * @param sendBse
	 * @param success
	 * @param increasePerHour
	 * @param increaseLevelup
	 * @return
	 */
	public final boolean addRoleActionPoint(User user, long currentTimeMillis,
			boolean sendBse, int count) {
		boolean success = false;
		int increasePerHour = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_HOURLY, 20);
		int roleAction = manager.getCurrentOpportunity(user, FIELD_ROLEACTION_KEY, 
				user.getRoleTotalAction(), increasePerHour, currentTimeMillis);
		int totalRoleAction = user.getRoleTotalAction();
		if ( roleAction > 0 ) {
			int diff = roleAction - count;
			if ( diff != 0 ) {
				success = manager.setDailyOpportunityCount(user, FIELD_ROLEACTION_KEY, currentTimeMillis, diff);
				if ( sendBse ) {
					GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
					String message = Text.text("roleaction.add", totalRoleAction-diff);
					SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
				}
			}
		} else {
			String message = Text.text("roleaction.max");
			SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
			success = false;
		}
		return success;
	}

	/**
	 * Get the current action point.
	 * The action point will grow 5 points every hour.
	 * 
	 * @param user
	 * @return
	 */
	public int getRoleActionPoint(User user, long currentTimeMillis) {
		int limit = user.getRoleTotalAction();
		
		int increasePerHour = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_HOURLY, 5);
		return manager.getCurrentOpportunity(user, FIELD_ROLEACTION_KEY, limit, increasePerHour, currentTimeMillis);
	}
	
	/**
	 * Update the user's current roleAction if it automatically changed every hour.
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean updateRoleActionPointIfChanged(User user, long currentTimeMillis) {
		boolean changed = false;
		int oldRoleAction = user.getRoleAction();
		int roleAction = getRoleActionPoint(user, currentTimeMillis);
		if ( roleAction != oldRoleAction ) {
			user.setRoleAction(roleAction);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
			changed = true;
		}
		return changed;
	}
	
	/**
	 * Check If the user still have extra role action points
	 * @param user
	 * @return
	 */
	public final boolean checkUserHasRoleActionPoint(User user) {
  	int limit = user.getRoleTotalAction();
		int increasePerHour = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_HOURLY, 5);
  	return manager.checkUserHasOpportunities(user, limit, increasePerHour, FIELD_ROLEACTION_KEY);
	}
	
	
	/**
	 * Get the Redis "mail" list name for inbox type
	 * @param user
	 * @return
	 */
	public final DailyLimitManager getDailyLimitManager() {
		return this.manager;
	}
	
}
