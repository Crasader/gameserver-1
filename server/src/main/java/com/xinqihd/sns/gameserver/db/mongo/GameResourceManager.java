package com.xinqihd.sns.gameserver.db.mongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It is used to manage the global i18n resources.
 * 
 * @author wangqi
 *
 */
public class GameResourceManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(GameResourceManager.class);
	
	private static final String COLL_NAME = "gameres";
	
	private static final String INDEX_NAME = "_id";
	
	private static HashMap<String, HashMap<Locale, String>> dataMap = 
			new HashMap<String, HashMap<Locale, String>>();
	
	private static HashSet<Locale> locales = new HashSet<Locale>();
		
	public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;
	
	private static final GameResourceManager instance = new GameResourceManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static GameResourceManager getInstance() {
		return instance;
	}
	
	GameResourceManager() {
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
			Set<String> keySet = obj.keySet();
			String resKey = null;
			for ( String key : keySet ) {
				if ( INDEX_NAME.equals(key) ) {
					resKey = obj.get(key).toString();
				} else {
					Locale locale = StringUtil.parseLocale(key, Locale.TRADITIONAL_CHINESE);
					HashMap<Locale, String> localeMap = dataMap.get(obj.get(INDEX_NAME));
					if ( localeMap == null ) {
						localeMap = new HashMap<Locale, String>();
						dataMap.put(resKey, localeMap);
					}
					localeMap.put(locale, obj.get(key).toString());
					locales.add(locale);
				}
			}
		}
		locales.add(DEFAULT_LOCALE);
		logger.debug("Load total {} game resources from database.", dataMap.size());
	}
	
	/**
	 * Get all the game resources in database.
	 * @param id
	 * @return
	 */
	public HashMap<String, HashMap<Locale, String>> getGameResources() {
		return dataMap;
	}
	
	/**
	 * Get all supported locales.
	 * 
	 * @return
	 */
	public Set<Locale> getAllLocales() {
		return locales;
	}
	
	/**
	 * Get the given game resource for the id and locale.
	 * If it is null, return null instead.
	 * 
	 * @return
	 */
	public String getGameResource(String id, Locale locale, String defaultValue) {
		HashMap<Locale, String> localeMap = dataMap.get(id);
		if ( localeMap == null ) {
			return defaultValue;
		} else {
			String value =  localeMap.get(locale);
			if ( value == null ) {
				return defaultValue;
			} else {
				return value;
			}
		}
	}
	
}
