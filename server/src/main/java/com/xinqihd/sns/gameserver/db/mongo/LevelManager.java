package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;

/**
 * It is used to manage the user's level.
 * 
 * @author wangqi
 *
 */
public class LevelManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(LevelManager.class);
	
	public static final int MAX_LEVEL = 100;
	
	private static final String COLL_NAME = "levels";
	
	private static final String INDEX_NAME = "_id";
	
	private static TreeMap<Integer, LevelPojo> dataMap = 
			new TreeMap<Integer, LevelPojo>();
	
	private static final LevelManager instance = new LevelManager();
	
	private LevelPojo firstLevel = null;
	private LevelPojo lastLevel = null;
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static LevelManager getInstance() {
		return instance;
	}
	
	LevelManager() {
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
		DBObject sort = MongoDBUtil.createDBObject("_id", Constant.ONE);
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null, sort);
		int sumExp = 0;
		for ( DBObject obj : list ) {
			LevelPojo level = (LevelPojo)MongoDBUtil.constructObject(obj);
			sumExp += level.getExp();
			level.setSumExp(sumExp);
			if ( level.getLevel() == 1 ) {
				firstLevel = level;
			} else if ( level.getLevel() == MAX_LEVEL ) {
				lastLevel = level;
			}
			dataMap.put(level.getLevel(), level);
			//logger.debug("Load item id {} name {} from database.", item.getId(), item.getName());
		}
		logger.debug("Load total {} levels from database.", dataMap.size());
	}
	
	/**
	 * Get the given item by its id.
	 * @param id
	 * @return
	 */
	public LevelPojo getLevel(int level) {
		 if ( level <= 0 ) {
			 return firstLevel;
		 } else if ( level >= MAX_LEVEL ) {
			 return lastLevel;
		 } else {
			 LevelPojo pojo = dataMap.get(level);
			 return pojo;
		 }
	}
	
	/**
	 * Get the underlying item collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<LevelPojo> getLevels() {
		return dataMap.values();
	}
	
}
