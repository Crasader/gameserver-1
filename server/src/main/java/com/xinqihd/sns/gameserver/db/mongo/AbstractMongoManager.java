package com.xinqihd.sns.gameserver.db.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;

/**
 * The abstract class collect all common codes to operate a Mongo database
 * for configuration.
 * 
 * @author wangqi
 *
 */
public class AbstractMongoManager {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMongoManager.class);

	//In development, I suggest use the saftWrite mode to find bugs early.
	//In production, we should disable it.
	protected boolean isSafeWrite = false;
	
	protected String databaseName;
	
	protected String namespace;
	
	protected String collectionName;
	
	protected String indexKey;
	
	/**
	 * The default constructor initialize data from GlobalConfig
	 * 
	 * @param collectionName
	 * @param indexKey
	 */
	protected AbstractMongoManager(String collectionName, String indexKey) {
		this(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				collectionName,
				indexKey
		 );
	}
	
	/**
	 * The constructor connect to database according to given parameters.
	 * 
	 * Please call the {@link UserManager#getInstance()}
	 * DO NOT call it directly or the system will be banded to Mongodb. 
	 */
	protected AbstractMongoManager(String databaseName, String namespace, boolean isSafeWrite,
			String collectionName, String indexKey) {
		super();
		this.isSafeWrite = isSafeWrite;
		this.databaseName = databaseName;
		this.namespace = namespace;
		this.collectionName = collectionName;
		this.indexKey = indexKey;
				
		if ( logger !=null && logger.isDebugEnabled() ) {
			logger.debug("database:"+databaseName+",namespace:"+namespace+
					",isSafeWrite:"+isSafeWrite + ",collection:" + collectionName + 
					",index:"+indexKey);
		}
		
		//Ensure Index
		MongoDBUtil.ensureIndex(databaseName, namespace, collectionName, indexKey, true);
	}
}
