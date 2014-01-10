package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap.BseMap;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class MapManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(MapManager.class);
	
	private static final String COLL_NAME = "maps";
	
	private static final String INDEX_NAME = "_id";
	
	private static ConcurrentHashMap<String, MapPojo> dataMap = 
			new ConcurrentHashMap<String, MapPojo>();
	
	private static Set<MapPojo> mapSet = new TreeSet<MapPojo>();

	
	private static final MapManager instance = new MapManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static MapManager getInstance() {
		return instance;
	}
	
	MapManager() {
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
		dataMap.clear();
		mapSet.clear();
		
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized ( dataMap ) {
			for ( DBObject obj : list ) {
				MapPojo map = (MapPojo)MongoDBUtil.constructObject(obj);
				dataMap.put(map.getId(), map);
				logger.debug("Load weapon id {} name {} from database.", map.getId(), map.getName());
				mapSet.add(map);
			}
		}
	}
	
	/**
	 * Get the given weapon by its id.
	 * @param id
	 * @return
	 */
	public MapPojo getMapById(String id) {
		 return dataMap.get(id);
	}
	
	/**
	 * Get the underlying Weapon collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<MapPojo> getMaps() {
		return mapSet;
	}
	
	/**
	 * Construct Protobuf's BseMap data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseMap toBseMap() {
		BseMap.Builder builder = BseMap.newBuilder();
		TreeSet<MapPojo> maps = new TreeSet<MapPojo>(dataMap.values());
		for ( MapPojo mapPojo : maps ) {
			builder.addMaps(mapPojo.toMapData());
		}
		return builder.build();
	}
}
