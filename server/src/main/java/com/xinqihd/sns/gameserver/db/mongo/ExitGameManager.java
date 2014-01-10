package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ExitPojo;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseExitGame.BseExitGame;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.treasure.TreasureHuntManager;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class ExitGameManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(ExitGameManager.class);
	
	private static final String COLL_NAME = "exits";
	
	private static final String INDEX_NAME = "_id";
	
	private static HashMap<Integer, ExitPojo> dataMap = new HashMap<Integer, ExitPojo>();
	
	public static final String REDIS_PREFIX = "exitgame:";
	public static final String DATE_FIELD = "date";
	public static final String DAYS_FIELD = "days";
	
	private static final ExitGameManager instance = new ExitGameManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static ExitGameManager getInstance() {
		return instance;
	}
	
	ExitGameManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		try {
			List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
					COLL_NAME, null);
			synchronized (dataMap) {
				dataMap.clear();
				for ( DBObject obj : list ) {
					ExitPojo exitPojo = (ExitPojo)MongoDBUtil.constructObject(obj);
					dataMap.put(exitPojo.getDays(), exitPojo);
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to load exits collection from db.");
		}
	}
	
	/**
	 * Add a new exitPojo
	 * @param exitPojo
	 */
	public void addExitPojo(ExitPojo exitPojo) {
		MapDBObject dbObj = MongoDBUtil.createMapDBObject(exitPojo);
		DBObject query = MongoDBUtil.createDBObject("_id", exitPojo.getId());
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
	}
	
	/**
	 * Get the underlying tip collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<ExitPojo> getExitPojos() {
		return dataMap.values();
	}
	
	/**
	 * Return the exit pojo.
	 * @param days
	 * @return
	 */
	public ExitPojo getExitPojoByDays(int days) {
		ExitPojo exitPojo = dataMap.get(days);
		return exitPojo;
	}
	
	/**
	 * Process the session key
	 * @param user
	 * @param session
	 * @param sessionKey
	 */
	public void exitGame(User user, IoSession session, SessionKey sessionKey) {
		BseExitGame.Builder builder = BseExitGame.newBuilder();
		
		if ( user != null ) {
			long currentMillis = System.currentTimeMillis();
			/**
			 * Check new user's reward
			 */
			String exitMessage = null;
			String userRegDateStr = DateUtil.formatDate(user.getCdate());
			String todayStr = DateUtil.getToday(currentMillis);
			if ( todayStr.equals(userRegDateStr) ) {
				//New user first access
				long userRegMillis = user.getCdate().getTime();
				ExitPojo exitPojo = this.getExitPojoByDays(1);
				if ( exitPojo != null ) {
					String key = getExitgameRedisKey(user.getUsername());
					Jedis jedis = JedisFactory.getJedisDB();
					jedis.hset(key, DATE_FIELD, String.valueOf(userRegMillis+86400000*exitPojo.getDays()));
					jedis.hset(key, DAYS_FIELD, String.valueOf(exitPojo.getDays()));
					jedis.expire(key, 86400*(exitPojo.getDays()+1));
					
					exitMessage = Text.text("exit.1");
				} else {
					logger.warn("The ExitPojo day 1 is not configured.");
				}
			}
			
			int roleAction = user.getRoleTotalAction() - RoleActionManager.getInstance().getRoleActionPoint(user, currentMillis);
			String roleActionMessage = null;
			if ( roleAction > 0 ) {
				roleActionMessage = Text.text("active.roleaction", roleAction);
			} else {
				roleActionMessage = Text.text("active.roleaction.done");
			}
			int treasureFreeCount = TreasureHuntManager.getInstance().getCurrentTreasureHuntFreeCount(user, currentMillis);
			String treasureMessage = null;
			if ( treasureFreeCount > 0 ) {
				treasureMessage = Text.text("active.treasure", treasureFreeCount);
			} else {
				treasureMessage = Text.text("active.treasure.done");
			}
			int[] goldenPrayResult = CaishenManager.getInstance().queryCaishenPrayInfo(user, currentMillis, false);
		  //当日可用的购买次数
			int buyCount = goldenPrayResult[0];
			String prayMessage = null;
			if ( buyCount > 0 ) {
				prayMessage = Text.text("active.pray", buyCount);
			} else {
				prayMessage = Text.text("active.pray.done");
			}
			
			StringBuilder buf = new StringBuilder();
			if ( exitMessage != null ) {
				buf.append(exitMessage);
			}
			buf.append(roleActionMessage);
			buf.append(treasureMessage);
			buf.append(prayMessage);
			builder.setInfo(buf.toString());
		} else {
			builder.setInfo(Text.text("active.general"));
		}
		GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.ExitGame);
	}
	
	/**
	 * Check if it needs to send user a present.
	 * 
	 * @param user
	 */
	public void checkLogin(User user) {
		if ( user != null ) {
			Jedis jedis = JedisFactory.getJedisDB();
			String key = getExitgameRedisKey(user.getUsername());
			Map<String, String> map = jedis.hgetAll(key);
			if ( map != null ) {
				String longStr = map.get(DATE_FIELD);
				String dayStr = map.get(DAYS_FIELD);
				long nextDate = 0;
				if ( longStr != null ) {
					nextDate = Long.parseLong(longStr);
				}
				int days = StringUtil.toInt(dayStr, 1);
				long currentMillis = System.currentTimeMillis();
				if ( nextDate > 0 ) {
					if ( currentMillis > nextDate ) {
						//the user re-login after 24 hour and within 48 hour.
						logger.debug("User {} re-login after 24 hour and within 48 hour", user.getRoleName());
						ExitPojo exitPojo = this.getExitPojoByDays(days);
						if ( exitPojo != null ) {
							String subject = Text.text("exit.success.sub");
							String content = Text.text("exit.success.cont");
							Reward reward = exitPojo.getReward();
							MailMessageManager.getInstance().sendAdminMail(user.get_id(), subject, content, reward);
							StatClient.getIntance().sendDataToStatServer(user, 
									StatAction.ExitGameReward, days, reward.toString());
							
							jedis.del(key);
						}
					} else {
						int diff = (int)((currentMillis-nextDate)/1000);
						int hour = diff/3600;
						int minute = diff/60;
						String message = null;
						if ( hour > 0 ) {
							message = Text.text("exit.hour", hour);
						} else if ( minute > 0 ){
							message = Text.text("exit.minute", minute);
						}
						if ( message != null ) {
							SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 3000);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get the exit game redis key
	 * @param userName
	 * @return
	 */
	public static final String getExitgameRedisKey(String userName) {
		return StringUtil.concat(REDIS_PREFIX, userName);
	}
	
}
