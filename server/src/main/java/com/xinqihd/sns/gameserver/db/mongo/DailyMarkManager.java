package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList.BseDailyMarkList;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class DailyMarkManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(DailyMarkManager.class);
	
	private static final String COLL_NAME = "dailymarks";
	
	private static final String INDEX_NAME = "_id";
	
	private static ConcurrentHashMap<Integer, DailyMarkPojo> dataMap = 
			new ConcurrentHashMap<Integer, DailyMarkPojo>();

	
	private static final DailyMarkManager instance = new DailyMarkManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static DailyMarkManager getInstance() {
		return instance;
	}
	
	DailyMarkManager() {
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
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		for ( DBObject obj : list ) {
			DailyMarkPojo dailyMark = (DailyMarkPojo)MongoDBUtil.constructObject(obj);
			dataMap.put(dailyMark.getDayNum(), dailyMark);
			logger.debug("Load dailyMark id {} daynum {} from database.", dailyMark.getId(), dailyMark.getDayNum());
		}
	}
	
	/**
	 * Get the given dailyMark by its id.
	 * @param id
	 * @return
	 */
	public DailyMarkPojo getDailyMarkByDayNum(int dayNum) {
		 return dataMap.get(dayNum);
	}
	
	/**
	 * Get the underlying dailyMark collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<DailyMarkPojo> getDailyMarks() {
		return dataMap.values();
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseDailyMarkList toBseDailyMark() {
		BseDailyMarkList.Builder builder = BseDailyMarkList.newBuilder();
		for ( DailyMarkPojo dailyMarkPojo : dataMap.values() ) {
			builder.addDailymarks(dailyMarkPojo.toDailyMarkData());
		}
		return builder.build();
	}
}
