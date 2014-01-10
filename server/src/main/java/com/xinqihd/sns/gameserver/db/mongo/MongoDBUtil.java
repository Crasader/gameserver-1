package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It is used to hide/manage the DBObject
 * 
 * TODO 
 * In future, we will support multiple Mongo database on different hosts.
 * For that, we can register all database names with hosts in a map.
 * For an operation, we will first check the host of Mongo that contains 
 * the database and then select the Mongo instance.
 * 
 * @author wangqi
 *
 */
public class MongoDBUtil {
	
	public static final String CLASS_PREFIX = "com.xinqihd.sns.gameserver";
	public static final int CLASS_PREFIX_LEN = "com.xinqihd.sns.gameserver".length();

	private static final Logger logger = LoggerFactory.getLogger(MongoDBUtil.class);

	/**
	 * It is the global MongoDB driver. In theory, we only need one instance of it.
	 */
	//private static Mongo defaultMongo = null;

	private static HashMap<String, Mongo> mongoMap = new HashMap<String, Mongo>();

	private static final String[] USER_COLL = {
		"accounts", "users", "bags", "relations", "unlocks", "uuids", "bibilo", "guilds", 
		"guildapplys", "guildmembers", "guildfacilities", "guildbags", "guildbagevents"
	};
	
	private static final String[] CFG_COLL = {
		"bosses", "charges", "dailymarks", "equipments_new", 
		"gamedata", "gameres", "icons", "items", "levels", "logins", 
		"maps", "shops_new", "tasks", "vipperids", "vips", "battletools", 
		"servers", "rewards", "tips", "promotions", "cdkeys", "exits", "puzzles",
		"rewardlevels"
	};
	
	private static String userDatabaseName = null;
	private static String userNamespace = null;
	private static String cfgDatabaseName = null;
	private static String cfgNamespace = null;
	
	static {
		//Open MongoDB
		try {
			String mongoHost = GlobalConfig.getInstance().getStringProperty("mongdb.host");
			int    mongoPort = GlobalConfig.getInstance().getIntProperty("mongdb.port");
			String mongoCfgHost = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_host);
			int    mongoCfgPort = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mongo_configdb_port);
			userDatabaseName = GlobalConfig.getInstance().getStringProperty("mongdb.database");
			userNamespace = GlobalConfig.getInstance().getStringProperty("mongdb.namespace");
			cfgDatabaseName = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database);
			cfgNamespace = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace);
			logger.info("userdb:{}; userNs:{}, cfgdb:{}, cfgNs:{}", 
					new Object[]{userDatabaseName, userNamespace, cfgDatabaseName, cfgNamespace});
			initUserMongo(mongoHost, mongoPort);
			initCfgMongo(mongoCfgHost, mongoCfgPort);
		} catch (Exception e) {
			logger.info("Failed to open mongodb", e);
		}
	}
	
	/**
	 * Intialize the mongo database connection
	 * @param mongoHost
	 * @param mongoPort
	 */
	public static Mongo initUserMongo(String mongoHost, int mongoPort) {
		try {
			ServerAddress address = new ServerAddress(mongoHost, mongoPort);
			MongoOptions options = new MongoOptions();
			options.autoConnectRetry = true;
			options.connectionsPerHost = GlobalConfig.getInstance().getIntProperty("mongdb.connectionsPerHost");
			options.connectTimeout = GlobalConfig.getInstance().getIntProperty("mongdb.connectTimeout");
			options.maxWaitTime = GlobalConfig.getInstance().getIntProperty("mongdb.maxWaitTime");
			options.socketTimeout = GlobalConfig.getInstance().getIntProperty("mongdb.socketTimeout");
			options.threadsAllowedToBlockForConnectionMultiplier = GlobalConfig.getInstance().getIntProperty("mongdb.threadsAllowedToBlockForConnectionMultiplier");
			Mongo mongo = new Mongo(address, options);
			if ( logger.isInfoEnabled() ) {
				logger.info("MongoDB for users is initialized OK: host:{}", mongoHost);
			}
			for ( String userColl : USER_COLL ) {
				String key = StringUtil.concat(userNamespace, Constant.DOT, userColl);
				if ( !mongoMap.containsKey(key) ) {
					//Put it into our cache
					mongoMap.put(key, mongo);
					logger.debug("Put the user db mongo key: {} for server", key);
				} else {
					logger.warn("Key:{} is duplicate in mongo database", key);
				}
				if ( !mongoMap.containsKey("guildbagevents") ) {
					
				}
			}
			return mongo;
		} catch (Exception e) {
			logger.info("Failed to open mongodb", e);
		}
		return null;
	}
	
	/**
	 * 
	 * @param mongoHost
	 * @param mongoPort
	 * @return
	 */
	public static Mongo initCfgMongo(String mongoHost, int mongoPort) {
		try {
			ServerAddress address = new ServerAddress(mongoHost, mongoPort);
			MongoOptions options = new MongoOptions();
			options.autoConnectRetry = true;
			Mongo mongo = new Mongo(address, options);
			if ( logger.isInfoEnabled() ) {
				logger.info("MongoDB for cfg is initialized OK: host:{}", mongoHost);
			}
			for ( String cfgColl : CFG_COLL ) {
				String key = StringUtil.concat(cfgNamespace, Constant.DOT, cfgColl);
				if ( !mongoMap.containsKey(key) ) {
					//Put it into our cache
					mongoMap.put(key, mongo);
					logger.debug("Put the cfg db mongo key: {} for server", key);
				} else {
					logger.warn("Key:{} is duplicate in mongo database", key);
				}
			}
			return mongo;
		} catch (Exception e) {
			logger.info("Failed to open mongodb", e);
		}
		return null;
	}

	/**
	 * Create a default DBObject
	 * @return
	 */
	public static final DBObject createDBObject() {
		return new BasicDBObject();
	}
	
	/**
	 * Create a default DBObject
	 * @return
	 */
	public static final DBObject createDBObject(String key, Object value) {
		return new BasicDBObject(key, value);
	}
	
	/**
	 * Create a default DBObject
	 * @return
	 */
	public static final MapDBObject createMapDBObject() {
		return new MapDBObject();
	}
	
	/**
	 *  Save i.e. INSERT or UPDATE an object to mongodb.
	 *  
	 * @param query usually the _id of collection, which is byte[] , null means insert a new row
	 * @param objectToSave the object to save
	 * @param databaseName the database
	 * @param namespace the namespace, maybe null
	 * @param isSafeWrite whether to enable the SafeWrite mode
	 */
	public static final void saveToMongo(DBObject query, DBObject objectToSave, 
			String databaseName, String namespace, String collection, boolean isSafeWrite) {
	
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( isSafeWrite ) {
			if ( query == null ) {
				coll.insert(objectToSave);
			} else {
				coll.update(query, objectToSave, true, false, WriteConcern.SAFE);
			}
		} else {
			if ( query == null ) {
				coll.insert(objectToSave);
			} else {
				coll.update(query, objectToSave, true, false, WriteConcern.NONE);
			}
		}
	}

	/**
	 * Create a MapDBObject with the given Object as parameter.
	 * @param o
	 * @return
	 */
	public static final MapDBObject createMapDBObject(Object o) {
		MapDBObject mapObj = new MapDBObject();
		mapObj.putAll(o);
		return mapObj;
	}
	
	/**
	 * Add a key:value pair into parent DBObject. If the subtitle is not null, create a sub DBObject 
	 * and put key/value in it. Otherwise, put them into parent directly.
	 *  
	 * @param parent
	 * @param subtitle
	 * @param key
	 * @param value
	 * @return
	 */
	public static final DBObject addDBObject(DBObject parent, String subTitle, String key, Object value) {
		DBObject target = null;
		if ( parent == null ) {
			parent = createDBObject();
		}
		target = parent;
		if ( subTitle != null ) {
			Object obj = parent.get(subTitle);
			if ( obj == null || ! (obj instanceof DBObject) ) {
				target = createDBObject();
				parent.put(subTitle, target);
			} else {
				target = (DBObject)obj;
			}
		}
		target.put(key, value);
		return parent;
	}
	
	/**
	 * Delete an object from mongo database.
	 * 
	 * @param query
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param isSafeWrite
	 * @return
	 */
	public static final void deleteFromMongo(DBObject query, String databaseName, 
			String namespace, String collection, boolean isSafeWrite) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( isSafeWrite ) {
			coll.remove(query, WriteConcern.SAFE);
		} else {
			coll.remove(query, WriteConcern.NONE);
		}
		
	}
	
	/**
	 * Find a DBObject from database using the query. If the ‘fields' argument is null, 
	 * then return the whole document. Otherwise, only returns given fields.
	 * 
	 * This method uses reflection to convert the result DBObject into given Object.
	 * 
	 * @param query The query condition
	 * @param databaseName The database name
	 * @param namespace The collection namespace
	 * @param collection The collection name
	 * @param filterFields The fields that will be returned.
	 * @return
	 */
	public static final Object queryObjectFromMongo(DBObject query, String databaseName,
			String namespace, String collection, DBObject filterFields) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		DBObject dbObject = null;
		if ( filterFields != null ) {
			dbObject = coll.findOne(query, filterFields);
		} else {
			dbObject = coll.findOne(query);
		}
		if ( dbObject != null ) {
			return constructObject(dbObject);
		} else {
			return null;
		}
	}
	
	/**
	 * Construct an object according to the object class.
	 * @param dbObject
	 * @param objectClass
	 * @return
	 */
	public static final Object constructObject(DBObject dbObject) {
		Object target = null;
		try {
			String className = (String)dbObject.get(Constant.CLASS);
			if ( className == null ) {
				logger.warn("Failed to find the className.");
				return null;
			}
			if ( className.startsWith(".") ) {
				className = CLASS_PREFIX.concat(className);
			}
			Class objectClass = MongoDBUtil.class.getClassLoader().loadClass(className);
			target = objectClass.newInstance();
			Field[] fields = objectClass.getDeclaredFields();
			for ( int i=0; i<fields.length; i++ ) {
				fields[i].setAccessible(true);
				int modifier = fields[i].getModifiers();
				if ( Modifier.isStatic(modifier) || Modifier.isTransient(modifier) ) {
					continue;
				}
 				Object value = dbObject.get(fields[i].getName());
				if ( fields[i].getType().isEnum() ) {
					if ( value != null ) {
						Method nameMethod = null;
						Method[] methods = fields[i].getType().getMethods();
						if ( value instanceof String ) {
							for ( Method method : methods ) {
								if ( "from".equals(method.getName()) ) {
									nameMethod = method;
									break;
								}
							}
						} else if ( value instanceof Integer ) {
							for ( Method method : methods ) {
								if ( "fromIndex".equals(method.getName()) ) {
									nameMethod = method;
									break;
								}
							}
						}						
						if ( nameMethod == null ) {
							nameMethod = fields[i].getType().getDeclaredMethod("valueOf", String.class);
						}
						Object enumValue = nameMethod.invoke(null, value);
						fields[i].set(target, enumValue);
					}
				} else if ( UserId.class.isAssignableFrom(fields[i].getType()) ) {
					UserId userId = null;
					if ( value instanceof byte[] ) {
						byte[] bytes = (byte[])value;
						userId = UserId.fromBytes(bytes);
					} else if ( value instanceof String) {
						userId = UserId.fromString((String)value);
						if ( userId == null ) {
							userId = UserId.fromBytes(StringUtil.hexStringToBytes((String)value));
						}
					}
					fields[i].set(target, userId);
				} else if ( Collection.class.isAssignableFrom(fields[i].getType()) ) {
					Collection list = (Collection)value;
					ArrayList newList = new ArrayList();
					Type type = fields[i].getGenericType();
					Class actualClass = null;
					if ( type instanceof ParameterizedType ) {
						ParameterizedType pt = (ParameterizedType) type;
						actualClass = (Class)pt.getActualTypeArguments()[0];
					}
					boolean isEnum = false;
					if ( actualClass != null && actualClass.isEnum() ) {
						isEnum = true;
					}
					if ( list != null ) {
						for (Object object : list) {
							if ( isEnum ) {
								Method nameMethod = actualClass.getDeclaredMethod("valueOf", String.class);
								Object enumValue = nameMethod.invoke(null, object);
								newList.add(enumValue);
							} else if ( object instanceof String ) {
								newList.add((String)object);
							} else {
								DBObject obj = (DBObject)object;
								Object newObject = constructObject(obj);
								newList.add(newObject);
							}
						}
					}
					if ( List.class.isAssignableFrom(fields[i].getType()) ) {
						fields[i].set(target, newList);
					} else {
						String fieldName = fields[i].getName();
						StringBuilder buf = new StringBuilder(10);
						buf.append("set").append(fieldName);
						buf.setCharAt(3, Character.toUpperCase(fieldName.charAt(0)));
						Method setMethod = objectClass.getDeclaredMethod(buf.toString(), Collection.class);
						setMethod.invoke(target, newList);
					}
				} else if ( Map.class.isAssignableFrom(fields[i].getType()) ) {
					Map mapField = constructMapObject((DBObject)value);
					fields[i].set(target, mapField);
//					String keyClassName = (String)dbObject.get(Constant.CLASS);
//					if ( keyClassName != null ) {
//						
//					}
//					Set<String> keys = dbObject.keySet();
//					Class objectClass = MongoUtil.class.getClassLoader().loadClass(className);
//					if ( objectClass.isEnum() ) {
//						Method nameMethod = objectClass.getDeclaredMethod("valueOf", String.class);
//						Object enumValue = nameMethod.invoke(null, object);
//						newList.add(enumValue);
//					}
				} else {
					if ( String.class.isAssignableFrom(fields[i].getType()) ) {
						if ( value != null ) {
							fields[i].set(target, value.toString());
						}
					} else if ( float.class.isAssignableFrom(fields[i].getType()) && value instanceof Double ) {
						Double doubleValue = (Double)value;
						if ( doubleValue != null ) {
							fields[i].set(target, doubleValue.floatValue());
						} else {
							fields[i].set(target, 0);
						}
					} else {
						if ( value != null ) {
							fields[i].set(target, value);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.warn("Failed to put Object in Map.", e);
			logger.warn("DBObject: {}", dbObject);
		}
		return target;
	}
	
	/**
	 * Convert an DBObject to an Map object. 
	 * If the DBObject contains '<KEY>class' key, it will be treated as the key class type.
	 * Otherwise, the key will be treated as string type.
	 * 
	 * @param dbObject
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static final Map constructMapObject(DBObject dbObject) 
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, 
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		HashMap mapField = new HashMap();
		if ( dbObject == null ) {
			return null;
		}
		Set<String> keys = dbObject.keySet();
		for ( String key : keys ) {
			if ( key.endsWith(Constant.CLASS) ) {
				continue;
			}
			Object keyClassObj = dbObject.get(key.concat(Constant.CLASS));
			Object keyValue = key;
			if ( keyClassObj != null ) {
				String keyClassName = keyClassObj.toString();
				if ( keyClassName.startsWith(".") ) {
					keyClassName = CLASS_PREFIX.concat(keyClassName);
				}
				Class keyClass = MongoDBUtil.class.getClassLoader().loadClass(keyClassName);
				if ( keyClass.isEnum() ) {
					Method nameMethod = keyClass.getDeclaredMethod("valueOf", String.class);
					keyValue = nameMethod.invoke(null, key);
				}
			}
			//Construct the value
			Object valueObj = dbObject.get(key);
			if ( valueObj instanceof DBObject ) {
				//valueObj = constructMapObject((DBObject)valueObj);
				if ( ((DBObject) valueObj).containsField(Constant.CLASS) ) {
					valueObj = constructObject((DBObject)valueObj);
				} else {
					valueObj = constructMapObject((DBObject)valueObj);
				}
			}
			mapField.put(keyValue, valueObj);
		}

		return mapField;
	}
	
	/**
	 * Find a DBObject from database using the query. If the ‘fields' argument is null, 
	 * then return the whole document. Otherwise, only returns given fields.
	 * 
	 * @param query The query condition
	 * @param databaseName The database name
	 * @param namespace The collection namespace
	 * @param collection The collection name
	 * @param fields The fields that will be returned.
	 * @return
	 */
	public static final DBObject queryFromMongo(DBObject query, String databaseName,
			String namespace, String collection, DBObject fields) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( fields != null ) {
			return coll.findOne(query, fields);
		} else {
			return coll.findOne(query);
		}
	}
	
	/**
	 * Find a DBObject from database using the query. If the ‘fields' argument is null, 
	 * then return the whole document. Otherwise, only returns given fields.
	 * 
	 * @param query The query condition
	 * @param databaseName The database name
	 * @param namespace The collection namespace
	 * @param collection The collection name
	 * @param fields The fields that will be returned.
	 * @return
	 */
	public static final DBObject queryFromMongo(DBObject query, String databaseName,
			String namespace, String collection, DBObject fields, DBObject sorts) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( fields != null && sorts != null ) {
			return coll.findOne(query, fields, sorts);
		} else if ( fields != null && sorts == null ) {
			return coll.findOne(query, fields);
		} else if ( fields == null && sorts != null ){
			return coll.findOne(query, null, sorts);
		} else {
			return coll.findOne(query);
		}
	}
	
	/**
	 * Find all DBObjects from database using this query. 
	 * Note 1: it may do a full table scan if the query contains no index keys.
	 * Note 2: it will fetch all content into JVM memory rather than use lazy loading.
	 * So make sure you call it only at small collection such as configuration data.
	 * 
	 * @param query
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param fields
	 * @return
	 */
	public static final List<DBObject> queryAllFromMongo(DBObject query, String databaseName,
			String namespace, String collection, DBObject fields) {
		
		return queryAllFromMongo(query, databaseName, namespace, collection, fields, null);
	}
	
	/**
	 * Find all DBObjects from database using this query. 
	 * Note 1: it may do a full table scan if the query contains no index keys.
	 * Note 2: it will fetch all content into JVM memory rather than use lazy loading.
	 * So make sure you call it only at small collection such as configuration data.
	 * 
	 * @param query
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param fields
	 * @return
	 */
	public static final List<DBObject> queryAllFromMongo(DBObject query, 
			String databaseName, String namespace, String collection, 
			DBObject fields, DBObject sortFields) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		//int count = (int)coll.count(query);
		ArrayList<DBObject> objList = new ArrayList<DBObject>();
		DBCursor list = coll.find(query, fields);
		if ( sortFields != null ) {
			list = list.sort(sortFields);
		}
		while ( list.hasNext() ) {
			objList.add(list.next());
		}
		return objList;
	}
	
	/**
	 * Find all DBObjects from database using this query. 
	 * Note 1: it may do a full table scan if the query contains no index keys.
	 * Note 2: it will fetch all content into JVM memory rather than use lazy loading.
	 * So make sure you call it only at small collection such as configuration data.
	 * 
	 * @param query
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param fields
	 * @return
	 */
	public static final List<DBObject> queryAllFromMongo(DBObject query, 
			String databaseName, String namespace, String collection, 
			DBObject fields, DBObject sortFields, int numToSkip, int limit) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		int count = (int)coll.count(query);
		ArrayList<DBObject> objList = new ArrayList<DBObject>();
		DBCursor list = coll.find(query, fields).skip(numToSkip).limit(limit);
		if ( sortFields != null ) {
			list = list.sort(sortFields);
		}
		while ( list.hasNext() ) {
			objList.add(list.next());
		}
		return objList;
	}
	
	/**
	 * Count the result number for a query
	 * @param query
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param fields
	 * @return
	 */
	public static final long countQueryResult(DBObject query, String databaseName,
			String namespace, String collection) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( coll != null ) {
			return coll.count(query);
		} else {
			return 0;
		}
	}
	
	/**
	 * Make sure an index is created. If the index is already created, it will
	 * do nothing.
	 * 
	 * @param index
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 */
	public static final void ensureIndex(String databaseName,
			String namespace, String collection, String columnKey, boolean unique) {

		DBObject keys = createDBObject();
		keys.put(columnKey, ONE);
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( coll == null ) {
			logger.warn("collection:{} does not exist.", collection);
			return;
		}
		if ( !unique ) {
			coll.ensureIndex(keys);
		} else {
			DBObject option = createDBObject();
			option.put("unique", Boolean.TRUE);
			coll.ensureIndex(keys, columnKey, unique);
		}
	}
	
	/**
	 * Drop the given collection from database.
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 */
	public static final void dropCollection(String databaseName,
			String namespace, String collection ) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		coll.drop();
	}
	
	/**
	 * Remove the given collection from database.
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 */
	public static final void removeDocument(String databaseName,
			String namespace, String collection, DBObject query) {
		
		DBCollection coll = getDBCollection(databaseName, namespace, collection);
		if ( query == null ) {
			query = MongoDBUtil.createDBObject();
		}
		coll.remove(query);
	}
	
	/**
	 * Do a server-side eval operation. 
	 * Note it is a block operation.
	 * 
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @param code
	 * @param args
	 * @return
	 */
	public static final CommandResult doEval(String databaseName,
			String namespace, String collection, String code, Object[] args) {
		DB db = getDB(databaseName, namespace, collection);
		return db.doEval(code, args);
	}
	
	/**
	 * Get the DBCollection.
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @return
	 */
	public static final DBCollection getDBCollection(String databaseName,
			String namespace, String collection) {

		DB db = getDB(databaseName, namespace, collection);
		String collectionName = null;
		if ( namespace != null ) {
			collectionName = concat(namespace, DOT, collection);
		} else {
			collectionName = collection;
		}
		if ( db != null ) {
			DBCollection coll = db.getCollection(collectionName);
			return coll;
		} else {
			logger.warn("Failed to find database:{}, namespace:{}, collection:{}", new Object[]{
					databaseName, namespace, collection
			});
			return null;
		}
	}
	
	/**
	 * Get the proper DB object by given database and collection.
	 * @param databaseName
	 * @param namespace
	 * @param collection
	 * @return
	 */
	public static final DB getDB(String databaseName,
			String namespace, String collection) {
		String key = null;
		if ( namespace == null ) {
			key = StringUtil.concat(collection);
		} else {
			key = StringUtil.concat(namespace, DOT, collection);
		}
		Mongo mongo = mongoMap.get(key);
		if ( mongo == null ) {
			logger.warn("Failed to find Mongo by key:{}. Need refresh", key);
			return null;
		} else {
			DB db = mongo.getDB(databaseName);
			return db;
		}
	}
}
