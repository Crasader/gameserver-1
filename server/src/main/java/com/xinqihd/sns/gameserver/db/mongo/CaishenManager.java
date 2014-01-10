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
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.DailyLimitManager.BuyResult;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseActionLimitBuy.BseActionLimitBuy;
import com.xinqihd.sns.gameserver.proto.XinqiBseActionLimitQuery.BseActionLimitQuery;
import com.xinqihd.sns.gameserver.proto.XinqiBseCaishenPray.BseCaishenPray;
import com.xinqihd.sns.gameserver.proto.XinqiBseCaishenQuery.BseCaishenQuery;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Every user will have an chance to call the Caishen.  
 * Caishen can give user a lot of golden everyday.
 * 
 * @author wangqi
 * 
 */
public class CaishenManager {

	private static final Logger logger = LoggerFactory
			.getLogger(CaishenManager.class);

	private static CaishenManager instance = new CaishenManager();
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "caishen:";
	
	//User's current roleaction
	private static final String FIELD_PRAY_KEY = "pray";
	
	//The common underlying manager
	private DailyLimitManager manager = new DailyLimitManager(KEY_PREFIX);
	

	CaishenManager() {
	}

	/**
	 * Get a singleton instance for this manager.
	 * 
	 * @return
	 */
	public static final CaishenManager getInstance() {
		return instance;
	}

	/**
	 * @return the disableRoleAction
	 */
	public boolean isDisableCaishen() {
		return manager.isDisableDailyLimit();
	}

	/**
	 * @param disableRoleAction the disableRoleAction to set
	 */
	public void setDisableCaishen(boolean disableRoleAction) {
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
	public boolean setCaishenPrayCount(User user, int buyCount, long currentTimeMillis) {		
		return manager.resetDailyBuyLimit(user, buyCount, currentTimeMillis);
	}
	
	/**
	 * Query that the given user can pray how many times ( buyCount), 
	 * how much yuanbao (buyPrice) and how much golden for ( buyValue) 
	 * 
	 * 
	 *  
	 * @param user
	 * @param currentTimeMillis
	 */
	public int[] queryCaishenPrayInfo(User user, long currentTimeMillis, boolean sendBse) {
		
		int[] results = manager.queryRoleActionLimit(user, currentTimeMillis, 
				ScriptHook.CAISHEN_PRAY_LIMIT_DAILY);
		if ( sendBse ) {
		  //当日可用的购买次数
			int buyCount = results[0];
			//购买的价格
			int buyPrice = results[1];
			//每次购买增加的金币数量
			int buyValue = results[2];
			//当日已经购买的次数，用来计算价格
			int buyTimes = results[3];
			BseCaishenQuery.Builder bsePrayQuery = BseCaishenQuery.newBuilder();
			bsePrayQuery.setPrice(buyPrice);
			bsePrayQuery.setGoldenvalue(buyValue);
			bsePrayQuery.setCount(buyCount);
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), bsePrayQuery.build());
			//Send Stat
			StatClient.getIntance().sendDataToStatServer(user, StatAction.CaishenPrayQuery,
					buyCount, buyValue);
		}
		return results;
	}
	
	/**
	 * User pray for the golden
	 * 
	 * @param user
	 * @param currentTimeMillis
	 * @return
	 */
	public boolean prayGolden(User user, long currentTimeMillis) {
		boolean success = false;
		String info = null;
		BuyResult buyResult = manager.buyNewOpportunity(user, 
				ScriptHook.CAISHEN_PRAY_LIMIT_DAILY, FIELD_PRAY_KEY, currentTimeMillis);
		switch ( buyResult ) {
			case NO_CHANGE:
				info = Text.text("pray.caishen.nochance");
				success = false;
				break;
			case NO_MONEY:
				success = false;
				break;
			case SUCCESS:
				info = Text.text("pray.caishen.success");
				success = true;
				break;
			default:
				break;
		}
		//Query for the next changed price.
		int[] results = queryCaishenPrayInfo(user, currentTimeMillis, false);
		BseCaishenPray.Builder bsePray = BseCaishenPray.newBuilder();
		bsePray.setSuccess(success);
		if ( info != null ) {
			bsePray.setResponse(info);
		}
	  //当日可用的购买次数
		int buyCount = results[0];
		//购买的价格
		int buyPrice = results[1];
		//每次购买增加的金币数量
		int buyValue = results[2];
		bsePray.setPrice(buyPrice);
		bsePray.setGoldenvalue(buyValue);
		bsePray.setCount(buyCount);

		if ( success ) {
			/**
			 * Add golden to user's account.
			 */
			user.setGolden(user.getGolden()+buyValue);
			UserManager.getInstance().saveUser(user, false);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
			/**
			 * Consume one pray chance.
			 */
			manager.consumeOpportunity(user, FIELD_PRAY_KEY, 
					1, buyCount, 0, currentTimeMillis);
			
			TaskManager.getInstance().processUserTasks(user, TaskHook.CAISHEN_PRAY);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), bsePray.build());
		
		//Send Stat
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.CaishenPrayBuy, buyResult);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
				UserActionKey.CaishenPrayBuy);
		
		return success;
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
