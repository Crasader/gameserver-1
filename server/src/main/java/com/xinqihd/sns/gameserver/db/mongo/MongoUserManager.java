package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.User.Location;
import com.xinqihd.sns.gameserver.entity.user.UserChangeFlag;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseFuncUnlock.BseFuncUnlock;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.JSON;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Use MongoDB as the underlying database to manipulate User.
 * 
 * @author wangqi
 *
 */
public class MongoUserManager extends UserManager {
	
	public static final String LOGIN_USERNAME = "login.username";
	public static final String LOGIN_ROLENAME = "login.rolename";
	public static final String LOGIN_STATUS   = "login.loginstatus";
	public static final String EMAIL_VERIFIED = "profile.verified";

	private static final Logger logger = LoggerFactory.getLogger(MongoUserManager.class);
	
	private static final String USER_COLL_NAME = "users";
	private static final String BAG_COLL_NAME = "bags";
	private static final String REL_COLL_NAME = "relations";
	private static final String UNLOCK_COLL_NAME = "unlocks";
	
	//In development, I suggest use the saftWrite mode to find bugs early.
	//In production, we should disable it.
	private boolean isSafeWrite = false;
	
	private String databaseName;
	
	private String namespace;
	
	private static DBObject basicUserFields;
	
	/**
	 * Please call the {@link UserManager#getInstance()}
	 * DO NOT call it directly or the system will be banded to Mongodb. 
	 */
	public MongoUserManager() {
		this(
				GlobalConfig.getInstance().getStringProperty("mongdb.database"),
				GlobalConfig.getInstance().getStringProperty("mongdb.namespace"),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite")
		 );
	}
	
	/**
	 * It's for the facility of testcase.
	 * 
	 * Please call the {@link UserManager#getInstance()}
	 * DO NOT call it directly or the system will be banded to Mongodb. 
	 */
	public MongoUserManager(String databaseName, String namespace, boolean isSafeWrite) {
		super();
		this.isSafeWrite = isSafeWrite;
		this.databaseName = databaseName;
		this.namespace = namespace;
		
		if ( basicUserFields == null ) {
			basicUserFields = constructBasicUserQueryFields();
		}
		
		if ( logger !=null && logger.isDebugEnabled() ) {
			logger.info("database:"+databaseName+",namespace:"+namespace+",isSafeWrite:"+isSafeWrite);
		}
		
		//Ensure Index
		MongoDBUtil.ensureIndex(databaseName, namespace, USER_COLL_NAME, LOGIN_USERNAME, true);
		MongoDBUtil.ensureIndex(databaseName, namespace, USER_COLL_NAME, LOGIN_ROLENAME, true);
	}

	/**
	 * Save the user excluding its bag and relation
	 * If isNewUser is true, then all fields are stored into database. The
	 * UserChangeFlag is ignored.
	 */
	@Override
	public boolean saveUser(User user, boolean isNewUser) {
		if ( user.isProxy() ) {
			logger.debug("User {} is a proxy user", user.getRoleName());
			return false;
		}
		UserChangeFlag[] flags = null;
		if ( User.USE_CHANGE_FLAG ) {
			flags = user.clearModifiedFlag();
		}
		DBObject dbObj = null;
		if ( isNewUser ) {
			flags = UserChangeFlag.values();
			dbObj = constructUserDBObject(flags, user);
		} else {
			if ( flags.length > 0 ) {
				dbObj = constructUserDBObjectUpdate(flags, user);
			}
		}
		if ( dbObj != null ) {
			//Save User entity
			DBObject query = createDBObject();
			query.put(_ID, user.get_id().getInternal());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, USER_COLL_NAME, isSafeWrite);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#saveUserBag(com.xinqihd.sns.gameserver.entity.user.Bag)
	 */
	@Override
	public boolean saveUserBag(User user, boolean isNewBag) {
		if ( user == null || user.get_id() == null || user.getBag() == null) {
			logger.warn("user or its bag is null. it cannot save its bag.");
			return false;
		}
		if ( user.isProxy() ) {
			logger.debug("User {} is a proxy user", user.getRoleName());
			return false;
		}
		
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		DBObject dbObj = null;
		if ( isNewBag ) {
			dbObj = constructBagDBObject(bag);
		} else {
			dbObj = constructBagDBObjectUpdate(bag);
		}
		if ( dbObj != null ) {
			//Save Bag entity
			DBObject query = createDBObject();
			query.put(_ID, bag.getUserid().getInternal());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, BAG_COLL_NAME, isSafeWrite);
			bag.clearMarkedChangeFlag();
			bag.clearGeneralChangeFlag();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#saveUserRelation(java.util.Collection)
	 */
	@Override
	public boolean saveUserRelation(Collection<Relation> relations) {
		DBObject setObj = createDBObject();
		DBObject unsetObj = createDBObject();
		
		boolean changed = false;
		UserId userId = null;
		for (Iterator<Relation> iter = relations.iterator(); iter.hasNext();) {
			Relation relation = iter.next();
			userId = relation.get_id();
			
			//Check the changed flags
			Set<String> flags = relation.clearChangeMark();
			for (Iterator<String> flagIter = flags.iterator(); flagIter.hasNext();) {
				String userName = flagIter.next();
				People p = relation.findPeopleByUserName(userName);
				if ( p != null ) {
					//People is added or modified
					p.setId(userId);
					setObj.put(concat(relation.getType().tag(), DOT, p.getUsername()), 
							createMapDBObject(p));
					changed = true;
				} else {
					//People is deleted.
					unsetObj.put(concat(relation.getType().tag(), DOT, userName), ONE);
					changed = true;
				}
			}
		}
		
//		relObj.put(_ID, userId.getInternal());
		if ( changed ) {
			DBObject query = createDBObject(_ID, userId.getInternal());
			DBObject relObj = createDBObject();
			relObj.put(OP_SET, setObj);
			relObj.put(OP_UNSET, unsetObj);
			MongoDBUtil.saveToMongo(query, relObj, databaseName, namespace, REL_COLL_NAME, isSafeWrite);
		}
		return changed;
	}
	
	/**
	 * When challenge with a friend, record the win/lose record time with him/her
	 * @param user
	 * @param friendRoleName
	 * @param winning
	 * @return
	 */
	@Override
	public Map<RelationType, Collection<People>> saveFriendWinOrLose(User user, User friend, boolean winning) {
		DBObject setObj = createDBObject();
		DBObject unsetObj = createDBObject();
		
		String friendUserName = friend.getUsername();
		Map<RelationType, Collection<People>> peopleMap = 
				new HashMap<RelationType, Collection<People>>();
		for ( RelationType type : RelationType.values() ) {
			Relation relation = user.getRelation(type);
			ArrayList<People> peopleList = new ArrayList<People>();
			if ( relation != null ) {
				People people = relation.findPeopleByUserName(friend.getUsername());
				if ( people != null ) {
					logger.debug("update win/lose of friend {}. relation:{}", friendUserName, type);
					people.setBasicUser(friend);
					if ( winning ) {
						people.setWin(people.getWin()+1);
						setObj.put(concat(type.tag(), DOT, friendUserName, DOT, "win"), 1);
					} else {
						people.setLose(people.getLose()+1);
						setObj.put(concat(type.tag(), DOT, friendUserName, DOT, "lose"), 1);
					}
					peopleList.add(people);
					relation.modifyPeople(people);
				}
			}
			peopleMap.put(type, peopleList);
		}
		
		UserId userId = user.get_id();
		
//		relObj.put(_ID, userId.getInternal());
		DBObject query = createDBObject(_ID, userId.getInternal());
		DBObject relObj = createDBObject();
		relObj.put(OP_SET, setObj);
		MongoDBUtil.saveToMongo(query, relObj, databaseName, namespace, REL_COLL_NAME, isSafeWrite);
		return peopleMap;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUser(java.lang.String)
	 */
	@Override
	public User queryUser(String userName) {
		if ( StringUtil.checkNotEmpty(userName) ) {
			DBObject query = createDBObject();
			query.put(LOGIN_USERNAME, userName);
			DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, null);
			if ( userObj != null ) { 
				User user = constructUserObject(userObj);
				return user;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUser(java.lang.String)
	 */
	@Override
	public User queryUserByRoleName(String roleName) {
		if ( StringUtil.checkNotEmpty(roleName) ) {
			DBObject query = createDBObject();
			query.put(LOGIN_ROLENAME, roleName);
			DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, null);
			if ( userObj != null ) {
				User user = constructUserObject(userObj);
				return user;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUser(com.xinqihd.sns.gameserver.entity.user.UserId)
	 */
	@Override
	public User queryUser(UserId userId) {
		if ( userId == null ) {
			return null;
		}
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, null);
		if ( userObj != null ) { 
			User user = constructUserObject(userObj);
			return user;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUser(com.xinqihd.sns.gameserver.entity.user.UserId)
	 */
	@Override
	public String queryUserGuildId(UserId userId) {
		if ( userId == null ) {
			return null;
		}
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject field = MongoDBUtil.createDBObject(UserChangeFlag.GUILDID.value(), Constant.ONE);
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, field);
		if ( dbObj != null ) {
			return (String)dbObj.get(UserChangeFlag.GUILDID.value());
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#checkUserNameExist(java.lang.String)
	 */
	@Override
	public boolean checkUserNameExist(String userName) {
		DBObject query = createDBObject();
		query.put(LOGIN_USERNAME, userName);
		long count = MongoDBUtil.countQueryResult(query, databaseName, namespace, USER_COLL_NAME);
		return count == 1;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#checkUserNameExist(java.lang.String)
	 */
	@Override
	public boolean checkRoleNameExist(String roleName) {
		DBObject query = createDBObject();
		query.put(LOGIN_ROLENAME, roleName);
		long count = MongoDBUtil.countQueryResult(query, databaseName, namespace, USER_COLL_NAME);
		return count == 1;
	}

	@Override
	public boolean checkEmailVerified(UserId userId) {
		DBObject query = createDBObject();
		query.put(Constant._ID, userId.getInternal());
		DBObject field = createDBObject();
		field.put(EMAIL_VERIFIED, Constant.ONE);
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, field);
		if ( dbObj != null ) {
			DBObject obj = (DBObject)dbObj.get("profile");
			if ( obj != null ) {
				Boolean emailVerified = (Boolean)obj.get(UserChangeFlag.VERIFIED.value());
				if (emailVerified != null ) {
					return emailVerified.booleanValue();
				}
			}
		}
		return false;
	}
	
	@Override
	public UserLoginStatus checkUserLoginStatus(UserId userId) {
		DBObject query = createDBObject();
		query.put(Constant._ID, userId.getInternal());
		DBObject field = createDBObject();
		field.put(LOGIN_STATUS, Constant.ONE);
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, field);
		if ( dbObj != null ) {
			DBObject obj = (DBObject)dbObj.get("login");
			if ( obj != null ) {
				Object loginStatusObj = obj.get(UserChangeFlag.LOGIN_STATUS.value());
				if ( loginStatusObj != null ) {
					UserLoginStatus status = UserLoginStatus.valueOf(loginStatusObj.toString());
					return status;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryBasicUser(java.lang.String)
	 */
	@Override
	public BasicUser queryBasicUser(String userName) {
		DBObject query = createDBObject();
		query.put(LOGIN_USERNAME, userName);
		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, basicUserFields);
		BasicUser user = null;
		if ( userObj != null ) {
			user = constructBasicUserObject(userObj);
		}
		return user;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryBasicUser(java.lang.String)
	 */
	@Override
	public BasicUser queryBasicUserByRoleName(String roleName) {
		DBObject query = createDBObject();
		query.put(LOGIN_ROLENAME, roleName);
		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, basicUserFields);
		BasicUser user = null;
		if ( userObj != null ) {
			user = constructBasicUserObject(userObj);
		}
		return user;
	}
	
	/**
	 * Query the basicUser as a regex pattern.
	 * @param roleName
	 * @return
	 */
	@Override
	public ArrayList<BasicUser> queryBasicUserByRoleNameRegex(String roleName) {
		ArrayList<BasicUser> userList = new ArrayList<BasicUser>();
		if ( StringUtil.checkNotEmpty(roleName) ) {
			if ( "*".equals(roleName) ) {
				//Forbidden the global match
				return userList;
			} else {
				String roleNameReg = StringUtil.concat(roleName, ".*$");
				DBObject regex = MongoDBUtil.createDBObject("$regex", roleNameReg);
				DBObject query = MongoDBUtil.createDBObject("login.rolename", regex);
				List<DBObject> list = MongoDBUtil.queryAllFromMongo(query, databaseName, namespace, USER_COLL_NAME, basicUserFields);
				for ( DBObject dbObj : list ) {
					BasicUser user = constructBasicUserObject(dbObj);
					userList.add(user);
				}
			}
		}
		return userList;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryBasicUser(com.xinqihd.sns.gameserver.entity.user.UserId)
	 */
	@Override
	public BasicUser queryBasicUser(UserId userId) {
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, basicUserFields);
		BasicUser user = null;
		if ( userObj != null ) {
			user = constructBasicUserObject(userObj);
		}
		return user;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUserBag(java.lang.String)
	 */
	@Override
	public Bag queryUserBag(User user) {
		if ( user == null || user.get_id() == null ) {
			logger.warn("queryUserBag user or userId is null. {}", user);
			return null;
		}
		Bag bag = queryUserBag(user.get_id());
		if ( bag != null ) {
			bag.setParentUser(user);
			user.setBag(bag);
		}
		return bag;
	}
	
	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUserBag(java.lang.String)
	 */
	@Override
	public Bag queryUserBag(UserId userId) {
		if ( userId == null ) {
			logger.warn("queryUserBag user or userId is null. {}", userId);
			return null;
		}
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject bagObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, BAG_COLL_NAME, null);
		if ( bagObj != null ) {
			Bag bag = constructUserBag(bagObj);
			bag.setUserd(userId);
			return bag;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.db.UserManager#queryUserRelation(java.lang.String)
	 */
	@Override
	public User queryUserRelation(User user) {
		DBObject query = createDBObject();
		query.put(_ID, user.get_id().getInternal());
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, REL_COLL_NAME, null);
		if ( dbObj == null ) {
			//user does not exist.
			return null;
		}
		UserId userId = UserId.fromBytes((byte[])dbObj.get(_ID));
		RelationType[] types = RelationType.values();
		for ( RelationType type : types ) {
			DBObject relObj = (DBObject)dbObj.get(type.tag());
			if ( relObj != null ) {
				user.addRelation(constructUserRelationObject(userId, relObj, type));
			}
		}
		return user;
	} 
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	@Override
	public Collection<Unlock> queryUserUnlock(User user) {
		if ( user == null || user.get_id() == null ) {
			logger.warn("queryUserUnlock user or userId is null. {}", user);
			return null;
		}
		UserId userId = user.get_id();
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, UNLOCK_COLL_NAME, null);
		ArrayList<Unlock> unlocks = new ArrayList<Unlock>(); 
		if ( dbObj != null ) {
			BasicDBList unlockObj = (BasicDBList)dbObj.get("unlocks");
			int size = unlockObj.size();
			for ( int i=0; i<size; i++ ) {
				Object o = unlockObj.get(i);
				Unlock unlock = (Unlock)MongoDBUtil.constructObject((DBObject)o);
				unlocks.add(unlock);
			}
			user.setUnlocks(unlocks);
		} else {
			user.setUnlocks(unlocks);
		}
		//默认开启单人对战
		boolean hasSingleGameEnable = false;
		for ( Unlock unlock : unlocks ) {
			if ( unlock.getFuncType() == GameFuncType.Room && 
					unlock.getFuncValue() == RoomType.SINGLE_ROOM.ordinal() ) {
				hasSingleGameEnable = true;
			}
		}
		if ( !hasSingleGameEnable ) {
  		//Unlock single mode game
  		Unlock unlock = new Unlock();
  		unlock.setId(user.get_id());
  		unlock.setFuncType(GameFuncType.Room);
  		unlock.setFuncValue(RoomType.SINGLE_ROOM.ordinal());
  		unlocks.add(unlock);
		}
		return unlocks;
	}
	
	/**
	 * Unlock a given function
	 * 
	 * @param user
	 * @param unlock
	 * @return
	 */
	@Override
	public synchronized boolean addUserNewUnlock(User user, Unlock unlock) {
		if ( user == null || unlock == null ) return false;
		
		Collection<Unlock> unlocks = user.getUnlocks();
		if ( unlocks == null ) {
			unlocks = queryUserUnlock(user);
		}
		if ( unlocks == null ) {
			unlocks = new ArrayList<Unlock>();
			user.setUnlocks(unlocks);
		}
		boolean alreadyUnlocked = false;
		for ( Unlock lock : unlocks ) {
			if ( lock.equals(unlock) ) {
				alreadyUnlocked = true;
			}
		}
		if ( !alreadyUnlocked ) {
			unlocks.add(unlock);
			BasicDBList dbList = new BasicDBList();
			for ( Unlock lock : unlocks ) {
				MapDBObject dbObj = MongoDBUtil.createMapDBObject();
				dbObj.putAll(lock);
				dbList.add(dbObj);
			}
			UserId userId = user.get_id();
			DBObject query = createDBObject();
			query.put(_ID, userId.getInternal());
			DBObject dbObj = MongoDBUtil.createDBObject(_ID, userId.getInternal());
			dbObj.put("unlocks", dbList);
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, UNLOCK_COLL_NAME, alreadyUnlocked);

			/**
			 * Send unlock message to client
			 */
			if ( user.getSessionKey() != null ) {
				BseFuncUnlock.Builder builder = BseFuncUnlock.newBuilder();
				builder.setIsnew(true);
				builder.addUnlocks(unlock.toFuncUnlock());
				GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
				
				/**
				 * Stat the unlock operation.
				 */
				StatClient.getIntance().sendDataToStatServer(user, StatAction.Unlock, 
						unlock.getFuncType(), unlock.getFuncValue());
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Delete an user from database by his/her id, including all the bag and 
	 * relation data.
	 * 
	 * @param userId
	 * @return
	 */
	@Override
	public void removeUser(UserId userId) {
		DBObject query = createDBObject();
		query.put(_ID, userId.getInternal());
		MongoDBUtil.deleteFromMongo(query, databaseName, namespace, USER_COLL_NAME, isSafeWrite);
		MongoDBUtil.deleteFromMongo(query, databaseName, namespace, BAG_COLL_NAME, isSafeWrite);
		MongoDBUtil.deleteFromMongo(query, databaseName, namespace, REL_COLL_NAME, isSafeWrite);
		MongoDBUtil.deleteFromMongo(query, databaseName, namespace, UNLOCK_COLL_NAME, isSafeWrite);
	}
	
	/**
	 * Delete an user from database by his/her name, including all the bag and 
	 * relation data.
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	public void removeUser(String userName) {
		DBObject query = createDBObject(LOGIN_USERNAME, userName);
		DBObject field = createDBObject(_ID, 1);

		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, field);
		if ( userObj == null ) {
			return;
		}
		byte[] bytes = (byte[])userObj.get(_ID);
		if ( bytes != null ) {
			UserId userId = UserId.fromBytes(bytes);
			this.removeUser(userId);
		} 
	}
	
	/**
	 * Delete an user from database by his/her name, including all the bag and 
	 * relation data.
	 * 
	 * @param roleName
	 * @return
	 */
	@Override
	public void removeUserByRoleName(String roleName) {
		DBObject query = createDBObject(LOGIN_ROLENAME, roleName);
		DBObject field = createDBObject(_ID, 1);

		DBObject userObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, USER_COLL_NAME, field);
		if ( userObj == null ) {
			return;
		}
		byte[] bytes = (byte[])userObj.get(_ID);
		if ( bytes != null ) {
			UserId userId = UserId.fromBytes(bytes);
			this.removeUser(userId);
		} 
	}
	
	/**
	 * @param userId
	 * @param success
	 * @return
	 */
	@Override
	public boolean removeUserGuildId(UserId userId) {
		boolean success = false;
		DBObject query = MongoDBUtil.createDBObject(_ID, userId.getInternal());
		DBObject setObj = MongoDBUtil.createDBObject("$unset", 
				MongoDBUtil.createDBObject("profile.guildid", Constant.ONE));
		try {
			MongoDBUtil.saveToMongo(query, setObj, databaseName, namespace, USER_COLL_NAME, isSafeWrite);
			success = true;
		} catch (Exception e) {
			logger.warn("Failed to remove guild member.", e);
		}
		return success;
	}
	
	/**
	 * Convert an User object to json style string, 
	 * for backup purpose.
	 * 
	 * @param user
	 * @return
	 */
	@Override
	public String convertUserToString(User user) {
		DBObject dbObj = constructUserDBObject(UserChangeFlag.values(), user);
		String userStr = JSON.serialize(dbObj);
		return userStr;
	}
	
	/**
	 * 
	 * @param bag
	 * @return
	 */
	@Override
	public String convertBagToString(Bag bag) {
		DBObject dbObj = constructBagDBObject(bag);
		String bagStr = JSON.serialize(dbObj);
		return bagStr;
	}

	/**
	 * Convert a Bag object to json style string,
	 * for backup purpose.
	 * 
	 * @param bag
	 * @return
	 */
	@Override
	public User convertStringToUser(String userStr) {
		DBObject dbObj = (DBObject)JSON.parse(userStr);
		User user = constructUserObject(dbObj);
		return user;
	}
	
	/**
	 * 
	 * @param bagStr
	 * @return
	 */
	@Override
	public Bag convertStringToBag(String bagStr) {
		DBObject dbBagObj = (DBObject) JSON.parse(bagStr);
		Bag bag = constructUserBag(dbBagObj);
		return bag;
	}
	
	/**
	 * Construct the user's relation object.
	 * @param userId
	 * @param relObj
	 * @param type
	 * @return
	 */
	private Relation constructUserRelationObject(UserId userId, DBObject relObj, RelationType type) {
		Relation relation = new Relation();
		relation.set_id(userId);
		relation.setType(type);
		Set<String> keySet = relObj.keySet();
		for (Iterator<String> iter = keySet.iterator(); iter.hasNext();) {
			String username = iter.next();
			DBObject peopleObj = (DBObject)relObj.get(username);
			UserId id = UserId.fromBytes((byte[])peopleObj.get(ID));
			UserId myId = UserId.fromBytes((byte[])peopleObj.get("myId"));
			String rolename = (String)peopleObj.get("rolename");
			Integer win = (Integer)peopleObj.get("win");
			Integer lose = (Integer)peopleObj.get("lose");
			int level = 1;
			Integer levelObj = (Integer)peopleObj.get("level");
			if ( levelObj != null ) {
				level = levelObj.intValue();
			}
			People p = new People();
			p.setId(id);
			p.setMyId(myId);
			p.setUsername(username);
			p.setRolename(rolename);
			if ( win != null ) {
				p.setWin(win.intValue());
			}
			if ( lose != null ) {
				p.setLose(lose.intValue());
			}
			p.setLevel(level);
			relation.addPeople(p);
		}
		relation.clearChangeMark();
		return relation;
	}
	
	/**
	 * Constrcut the UserDBObject according to UserChangeFlags.
	 * @param flags
	 * @param dbObj
	 * @param user
	 */
	private DBObject constructUserDBObjectUpdate(UserChangeFlag[] flags, User user) {
		DBObject setObj = createDBObject();
		for ( UserChangeFlag flag : flags ) {
			switch (flag) {
				//login
				case UUID:
					setObj.put("login.".concat(flag.value()), user.getUuid());
					break;
				case USERNAME:
					setObj.put("login.".concat(flag.value()), user.getUsername());
					break;
				case ROLENAME:
					setObj.put("login.".concat(flag.value()), user.getRoleName() );
					break;
				case PASSWORD:
					setObj.put("login.".concat(flag.value()), user.getPassword() );
					break;
				case LOGIN_STATUS:
					setObj.put("login.".concat(flag.value()), user.getLoginStatus().name() );
					break;
				case LOGIN_STATUS_DESC:
					setObj.put("login.".concat(flag.value()), user.getLoginStatusDesc() );
					break;
				case USER_STATUS:
					setObj.put("login.".concat(flag.value()), user.getStatus().name() );
					break;
				case ACCOUNT_NAME:
					setObj.put("login.".concat(flag.value()), user.getAccountName() );
					break;
				case ISADMIN:
					setObj.put("login.".concat(flag.value()), user.isAdmin() );
					break;
				case SERVERID:
					setObj.put("login.".concat(flag.value()), user.getServerId() );
					break;
				//wealth
				case GOLDEN:
					setObj.put("wealth.".concat(flag.value()), user.getGolden() );
					break;
				case YUANBAO:
					setObj.put("wealth.".concat(flag.value()), user.getYuanbao() );
					break;
				case YUANBAO_FREE:
					setObj.put("wealth.".concat(flag.value()), user.getYuanbaoFree() );
					break;
				case VOUCHER:
					setObj.put("wealth.".concat(flag.value()), user.getVoucher() );
					break;
				case MEDAL:
					setObj.put("wealth.".concat(flag.value()), user.getMedal() );
					break;
				case CHARGED_YUANBAO:
					setObj.put("wealth.".concat(flag.value()), user.getChargedYuanbao() );
					break;
				case CHARGED_COUNT:
					setObj.put("wealth.".concat(flag.value()), user.getChargeCount() );
					break;
				//ability
				case BLOOD:
					setObj.put("ability.".concat(flag.value()), user.getBlood() );
					break;
				case EXP:
					setObj.put("ability.".concat(flag.value()), user.getExp() );
					break;
				case LEVEL:
					setObj.put("ability.".concat(flag.value()), user.getLevel() );
					break;
				case POWER:
					setObj.put("ability.".concat(flag.value()), user.getPower() );
					break;
				case DAMAGE:
					setObj.put("ability.".concat(flag.value()), user.getDamage() );
					break;
				case ATTACK:
					setObj.put("ability.".concat(flag.value()), user.getAttack() );
					break;
				case DEFEND:
					setObj.put("ability.".concat(flag.value()), user.getDefend() );
					break;
				case AGILITY:
					setObj.put("ability.".concat(flag.value()), user.getAgility() );
					break;
				case LUCK:
					setObj.put("ability.".concat(flag.value()), user.getLuck() );
					break;
				case SKIN:
					setObj.put("ability.".concat(flag.value()), user.getSkin() );
					break;
				case TKEW:
					setObj.put("ability.".concat(flag.value()), user.getTkew() );
					break;
				case ACHIEVEMENT:
					setObj.put("ability.".concat(flag.value()), user.getAchievement() );
					break;
				case ROLETOTALACTIONS:
					setObj.put("ability.".concat(flag.value()), user.getRoleTotalAction() );
					break;
					/*
				case VALUEMAP:
					setObj.put("ability.".concat(flag.value()), MongoDBUtil.createMapDBObject(user.getValueMap()) );
					break;
					*/
				//vip
				case ISVIP:
					setObj.put("vip.".concat(flag.value()), user.isVip() );
					break;
				case VIPLEVEL:
					setObj.put("vip.".concat(flag.value()), user.getViplevel() );
					break;
				case VIPBDATE:
					setObj.put("vip.".concat(flag.value()), user.getVipbdate() );
					break;
				case VIPEDATE:
					setObj.put("vip.".concat(flag.value()), user.getVipedate() );
					break;
				case VIPEXP:
					setObj.put("vip.".concat(flag.value()), user.getVipexp() );
					break;
				//config
				case CONFIGEFFECTSWITCH:
					setObj.put("config.".concat(flag.value()), user.isConfigEffectSwitch() );
					break;
				case CONFIGEFFECTVOLUME:
					setObj.put("config.".concat(flag.value()), user.getConfigEffectVolume() );
					break;
				case CONFIGHIDEGLASS:
					setObj.put("config.".concat(flag.value()), user.isConfigHideGlass() );
					break;
				case CONFIGHIDEHAT:
					setObj.put("config.".concat(flag.value()), user.isConfigHideHat() );
					break;
				case CONFIGHIDESUITE:
					setObj.put("config.".concat(flag.value()), user.isConfigHideSuite() );
					break;
				case CONFIGLEADFINISH:
					setObj.put("config.".concat(flag.value()), user.isConfigLeadFinish() );
					break;
				case CONFIGMUSICSWITCH:
					setObj.put("config.".concat(flag.value()), user.isConfigMusicSwitch() );
					break;
				case CONFIGMUSICVOLUME:
					setObj.put("config.".concat(flag.value()), user.getConfigMusicVolume() );
					break;
				//profile
				case EMAIL:
					setObj.put("profile.".concat(flag.value()), user.getEmail() );
					break;
				case ICONURL:
					setObj.put("profile.".concat(flag.value()), user.getIconurl() );
					break;
				case CLIENT:
					setObj.put("profile.".concat(flag.value()), user.getClient() );
					break;
				case GENDER:
					setObj.put("profile.".concat(flag.value()), user.getGender().ordinal() );
					break;
				case COUNTRY:
					setObj.put("profile.".concat(flag.value()), user.getCountry() );
					break;
				case CDATE:
					setObj.put("profile.".concat(flag.value()), user.getCdate() );
					break;
				case LDATE:
					setObj.put("profile.".concat(flag.value()), user.getLdate() );
					break;
				case TDATE:
					setObj.put("profile.".concat(flag.value()), user.getTdate() );
					break;
				case TOTALMIN:
					setObj.put("profile.".concat(flag.value()), user.getTotalmin() );
					break;
				case TOTALKILL:
					setObj.put("profile.".concat(flag.value()), user.getTotalKills() );
					break;
				case WINS:
					setObj.put("profile.".concat(flag.value()), user.getWins() );
					break;
				case WINODDS:
					setObj.put("profile.".concat(flag.value()), user.getWinOdds() );
					break;
				case FAILCOUNT:
					setObj.put("profile.".concat(flag.value()), user.getFailcount() );
					break;
				case BATTLECOUNT:
					setObj.put("profile.".concat(flag.value()), user.getBattleCount() );
					break;
				case CHANNEL:
					setObj.put("profile.".concat(flag.value()), user.getChannel() );
					break;
				case CONTINULOGINTIMES:
					setObj.put("profile.".concat(flag.value()), user.getContinuLoginTimes() );
					break;
				case REMAINLOTTERYTIMES:
					setObj.put("profile.".concat(flag.value()), user.getRemainLotteryTimes() );
					break;
				case ABTEST:
					setObj.put("profile.".concat(flag.value()), user.getAbtest() );
					break;
				case ISGUEST:
					setObj.put("profile.".concat(flag.value()), user.isGuest() );
					break;
				case TUTORIAL:
					setObj.put("profile.".concat(flag.value()), user.isTutorial() );
					break;
				case VERIFIED:
					setObj.put("profile.".concat(flag.value()), user.isVerifiedEmail() );
					break;
				case SCREEN:
					setObj.put("profile.".concat(flag.value()), user.getScreen() );
					break;
				case DEVICETOKEN:
					setObj.put("profile.".concat(flag.value()), user.getDeviceToken() );
					break;
				case TUTORIALMARK:
					setObj.put("profile.".concat(flag.value()), user.getTutorialMark() );
					break;
				case GUILDID:
					setObj.put("profile.".concat(flag.value()), user.getGuildId() );
					break;
				//location
				case LOC:
					DBObject loc = createDBObject();
					loc.put("x", user.getLocation().x);
					loc.put("y", user.getLocation().y);
					setObj.put(flag.value(), loc);
					break;
				//tools
				case TOOLS:
					List<BuffToolType> tools = user.getTools();
					List<String> toolsList = new ArrayList<String>();
					for (BuffToolType tool : tools ) {
						if ( tool == null ) {
							toolsList.add(Constant.EMPTY);
						} else {
							toolsList.add(tool.name());
						}
					}
					setObj.put(flag.value(), toolsList);
					break;
				case MAX_TOOL_COUNT:
					setObj.put("maxtool", user.getMaxToolCount() );
					break;
				case CURRENT_TOOL_COUNT:
					setObj.put("currtool", user.getCurrentToolCount() );
					break;
				case WEIBO:
					setObj.put("weibo", user.getWeiboTokenMap() );
					break;
				default:
					logger.debug("Not found the changed field: " + flag);
					break;
			}
		}//for...
		DBObject dbObj = createDBObject();
		dbObj.put(OP_SET, setObj);
		return dbObj;
	}

	/**
	 * Constrcut the UserDBObject according to UserChangeFlags.
	 * @param flags
	 * @param dbObj
	 * @param user
	 */
	private DBObject constructUserDBObject(UserChangeFlag[] flags, User user) {
		DBObject dbObj = createDBObject();
		dbObj.put(_ID, user.get_id().getInternal());
		for ( UserChangeFlag flag : flags ) {
			switch (flag) {
				//login
				case UUID:
					addDBObject(dbObj, "login", flag.value(), user.getUuid() );
					break;
				case USERNAME:
					addDBObject(dbObj, "login", flag.value(), user.getUsername() );
					break;
				case ROLENAME:
					addDBObject(dbObj, "login", flag.value(), user.getRoleName() );
					break;
				case PASSWORD:
					addDBObject(dbObj, "login", flag.value(), user.getPassword() );
					break;
				case LOGIN_STATUS:
					addDBObject(dbObj, "login", flag.value(), user.getLoginStatus().name() );
					break;
				case LOGIN_STATUS_DESC:
					addDBObject(dbObj, "login", flag.value(), user.getLoginStatusDesc() );
					break;
				case USER_STATUS:
					addDBObject(dbObj, "login", flag.value(), user.getStatus().name() );
					break;
				case ACCOUNT_NAME:
					addDBObject(dbObj, "login", flag.value(), user.getAccountName() );
					break;
				case ISADMIN:
					addDBObject(dbObj, "login", flag.value(), user.isAdmin() );
					break;
				case SERVERID:
					addDBObject(dbObj, "login", flag.value(), user.getServerId() );
					break;
				//wealth
				case GOLDEN:
					addDBObject(dbObj, "wealth", flag.value(), user.getGolden() );
					break;
				case YUANBAO:
					addDBObject(dbObj, "wealth", flag.value(), user.getYuanbao() );
					break;
				case YUANBAO_FREE:
					addDBObject(dbObj, "wealth", flag.value(), user.getYuanbaoFree() );
					break;
				case VOUCHER:
					addDBObject(dbObj, "wealth", flag.value(), user.getVoucher() );
					break;
				case MEDAL:
					addDBObject(dbObj, "wealth", flag.value(), user.getMedal() );
					break;
				case CHARGED_YUANBAO:
					addDBObject(dbObj, "wealth", flag.value(), user.getChargedYuanbao() );
					break;
				case CHARGED_COUNT:
					addDBObject(dbObj, "wealth", flag.value(), user.getChargeCount() );
					break;
				//ability
				case BLOOD:
					addDBObject(dbObj, "ability", flag.value(), user.getBlood() );
					break;
				case EXP:
					addDBObject(dbObj, "ability", flag.value(), user.getExp() );
					break;
				case LEVEL:
					addDBObject(dbObj, "ability", flag.value(), user.getLevel() );
					break;
				case POWER:
					addDBObject(dbObj, "ability", flag.value(), user.getPower() );
					break;
				case DAMAGE:
					addDBObject(dbObj, "ability", flag.value(), user.getDamage() );
					break;
				case ATTACK:
					addDBObject(dbObj, "ability", flag.value(), user.getAttack() );
					break;
				case DEFEND:
					addDBObject(dbObj, "ability", flag.value(), user.getDefend() );
					break;
				case AGILITY:
					addDBObject(dbObj, "ability", flag.value(), user.getAgility() );
					break;
				case LUCK:
					addDBObject(dbObj, "ability", flag.value(), user.getLuck() );
					break;
				case SKIN:
					addDBObject(dbObj, "ability", flag.value(), user.getSkin() );
					break;
				case TKEW:
					addDBObject(dbObj, "ability", flag.value(), user.getTkew() );
					break;
				case ACHIEVEMENT:
					addDBObject(dbObj, "ability", flag.value(), user.getAchievement() );
					break;
				case ROLETOTALACTIONS:
					addDBObject(dbObj, "ability", flag.value(), user.getRoleTotalAction() );
					break;
					/*
				case VALUEMAP:
					addDBObject(dbObj, "ability", flag.value(), MongoDBUtil.createMapDBObject(user.getValueMap()) );
					break;
					*/
				//vip
				case ISVIP:
					addDBObject(dbObj, "vip", flag.value(), user.isVip() );
					break;
				case VIPLEVEL:
					addDBObject(dbObj, "vip", flag.value(), user.getViplevel() );
					break;
				case VIPBDATE:
					addDBObject(dbObj, "vip", flag.value(), user.getVipbdate() );
					break;
				case VIPEDATE:
					addDBObject(dbObj, "vip", flag.value(), user.getVipedate() );
					break;
				case VIPEXP:
					addDBObject(dbObj, "vip", flag.value(), user.getVipexp() );
					break;
				//config
				case CONFIGEFFECTSWITCH:
					addDBObject(dbObj, "config", flag.value(), user.isConfigEffectSwitch() );
					break;
				case CONFIGEFFECTVOLUME:
					addDBObject(dbObj, "config", flag.value(), user.getConfigEffectVolume() );
					break;
				case CONFIGHIDEGLASS:
					addDBObject(dbObj, "config", flag.value(), user.isConfigHideGlass() );
					break;
				case CONFIGHIDEHAT:
					addDBObject(dbObj, "config", flag.value(), user.isConfigHideHat() );
					break;
				case CONFIGHIDESUITE:
					addDBObject(dbObj, "config", flag.value(), user.isConfigHideSuite() );
					break;
				case CONFIGLEADFINISH:
					addDBObject(dbObj, "config", flag.value(), user.isConfigLeadFinish() );
					break;
				case CONFIGMUSICSWITCH:
					addDBObject(dbObj, "config", flag.value(), user.isConfigMusicSwitch() );
					break;
				case CONFIGMUSICVOLUME:
					addDBObject(dbObj, "config", flag.value(), user.getConfigMusicVolume() );
					break;
				//profile
				case EMAIL:
					addDBObject(dbObj, "profile", flag.value(), user.getEmail() );
					break;
				case ICONURL:
					addDBObject(dbObj, "profile", flag.value(), user.getIconurl() );
					break;
				case CLIENT:
					addDBObject(dbObj, "profile", flag.value(), user.getClient() );
					break;
				case GENDER:
					addDBObject(dbObj, "profile", flag.value(), user.getGender().ordinal() );
					break;
				case COUNTRY:
					addDBObject(dbObj, "profile", flag.value(), user.getCountry() );
					break;
				case CDATE:
					addDBObject(dbObj, "profile", flag.value(), user.getCdate() );
					break;
				case LDATE:
					addDBObject(dbObj, "profile", flag.value(), user.getLdate() );
					break;
				case TDATE:
					addDBObject(dbObj, "profile", flag.value(), user.getTdate() );
					break;
				case TOTALMIN:
					addDBObject(dbObj, "profile", flag.value(), user.getTotalmin() );
					break;
				case TOTALKILL:
					addDBObject(dbObj, "profile", flag.value(), user.getTotalKills() );
					break;
				case WINS:
					addDBObject(dbObj, "profile", flag.value(), user.getWins() );
					break;
				case WINODDS:
					addDBObject(dbObj, "profile", flag.value(), user.getWinOdds() );
					break;
				case FAILCOUNT:
					addDBObject(dbObj, "profile", flag.value(), user.getFailcount() );
					break;
				case BATTLECOUNT:
					addDBObject(dbObj, "profile", flag.value(), user.getBattleCount() );
					break;
				case CHANNEL:
					addDBObject(dbObj, "profile", flag.value(), user.getChannel() );
					break;
				case CONTINULOGINTIMES:
					addDBObject(dbObj, "profile", flag.value(), user.getContinuLoginTimes() );
					break;
				case REMAINLOTTERYTIMES:
					addDBObject(dbObj, "profile", flag.value(), user.getRemainLotteryTimes() );
					break;
				case ABTEST:
					addDBObject(dbObj, "profile", flag.value(), user.getAbtest() );
					break;
				case ISGUEST:
					addDBObject(dbObj, "profile", flag.value(), user.isGuest() );
					break;
				case TUTORIAL:
					addDBObject(dbObj, "profile", flag.value(), user.isTutorial() );
					break;
				case VERIFIED:
					addDBObject(dbObj, "profile", flag.value(), user.isVerifiedEmail() );
					break;
				case SCREEN:
					addDBObject(dbObj, "profile", flag.value(), user.getScreen() );
					break;
				case DEVICETOKEN:
					addDBObject(dbObj, "profile", flag.value(), user.getDeviceToken() );
					break;
				case TUTORIALMARK:
					addDBObject(dbObj, "profile", flag.value(), user.getTutorialMark() );
					break;
				case GUILDID:
					addDBObject(dbObj, "profile", flag.value(), user.getGuildId() );
					break;
				//location
				case LOC:
					DBObject loc = createDBObject();
					loc.put("x", user.getLocation().x);
					loc.put("y", user.getLocation().y);
					dbObj.put(flag.value(), loc);
					break;
				//tools
				case TOOLS:
					List<BuffToolType> tools = user.getTools();
					List<String> toolsList = new ArrayList<String>();
					for (BuffToolType tool : tools ) {
						if ( tool == null ) {
							toolsList.add(null);
						} else {
							toolsList.add(tool.name());
						}
					}
					dbObj.put(flag.value(), toolsList);
					break;
				case MAX_TOOL_COUNT:
					dbObj.put("maxtool", user.getMaxToolCount() );
					break;
				case CURRENT_TOOL_COUNT:
					dbObj.put("currtool", user.getCurrentToolCount() );
					break;
				case WEIBO:
					dbObj.put("weibo", user.getWeiboTokenMap());
				default:
					logger.debug("Not found the changed field: " + flag);
					break;
			}
		}//for...
		return dbObj;
	}
	
	/**
	 * Construct the User object from database DBObject.
	 * @param userObj
	 * @return
	 */
	public final User constructUserObject(DBObject userObj) {
		DBObject login = (DBObject)userObj.get("login");
		DBObject wealth = (DBObject)userObj.get("wealth");
		DBObject ability = (DBObject)userObj.get("ability");
		DBObject vip = (DBObject)userObj.get("vip");
		DBObject config = (DBObject)userObj.get("config");
		DBObject profile = (DBObject)userObj.get("profile");
		DBObject location = (DBObject)userObj.get("location");
		DBObject weibo = (DBObject)userObj.get("weibo");
		
		if ( login == null || wealth == null ) {
			return null;
		}
		User user = new User();
		Object dbObjId = userObj.get(_ID);
		byte[] array = null;
		if ( dbObjId instanceof String ) {
			array = StringUtil.hexStringToBytes((String)dbObjId);
		} else {
			array = (byte[])dbObjId;
		}
		user.set_id(UserId.fromBytes(array));
		user.setUuid((String)login.get(UserChangeFlag.UUID.value()));
		user.setUsername((String)login.get(UserChangeFlag.USERNAME.value()));
		user.setAccountName((String)login.get(UserChangeFlag.ACCOUNT_NAME.value()));
		Boolean isAdmin = (Boolean)login.get(UserChangeFlag.ISADMIN.value());
		if ( isAdmin != null ) { 
			user.setAdmin(isAdmin.booleanValue());
		}
		String roleName = (String)login.get(UserChangeFlag.ROLENAME.value());
		if ( !StringUtil.checkNotEmpty(roleName) ) {
			roleName = user.getUsername();
		}
		user.setRoleName(roleName);
		user.setPassword((String)login.get(UserChangeFlag.PASSWORD.value()));
		String serverId = (String)login.get(UserChangeFlag.SERVERID.value());
		if ( !StringUtil.checkNotEmpty(serverId) ) {
			user.setServerId(serverId);
		}
		String loginStatusStr = (String)login.get(UserChangeFlag.LOGIN_STATUS.value());
		if ( StringUtil.checkNotEmpty(loginStatusStr) ) {
			user.setLoginStatus(UserLoginStatus.valueOf(loginStatusStr));
		} else {
			user.setLoginStatus(UserLoginStatus.NORMAL);
		}
		String loginStatusStrDesc = (String)login.get(UserChangeFlag.LOGIN_STATUS_DESC.value());
		user.setLoginStatusDesc(loginStatusStrDesc);
		
		user.setGoldenSimple((Integer)wealth.get(UserChangeFlag.GOLDEN.value()));
		user.setYuanbaoSimple((Integer)wealth.get(UserChangeFlag.YUANBAO.value()));
		Integer yuanbaoFreeSimple = (Integer)wealth.get(UserChangeFlag.YUANBAO_FREE.value());
		if ( yuanbaoFreeSimple == null ) {
			user.setYuanbaoFreeSimple(0);
		} else {
			user.setYuanbaoFreeSimple(yuanbaoFreeSimple.intValue());
		}
		user.setVoucher((Integer)wealth.get(UserChangeFlag.VOUCHER.value()));
		user.setMedal((Integer)wealth.get(UserChangeFlag.MEDAL.value()));
		Integer chargedYuanbao = (Integer)wealth.get(UserChangeFlag.CHARGED_YUANBAO.value());
		if ( chargedYuanbao != null ) {
			user.setChargedYuanbao(chargedYuanbao.intValue());
		} else {
			user.setChargedYuanbao(0);
		}
		Integer chargedCount = (Integer)wealth.get(UserChangeFlag.CHARGED_COUNT.value());
		if ( chargedCount != null ) {
			user.setChargeCount(chargedCount.intValue());
		} else {
			user.setChargeCount(0);
		}
		user.setBlood((Integer)ability.get(UserChangeFlag.BLOOD.value()));
		user.setLevelSimple((Integer)ability.get(UserChangeFlag.LEVEL.value()));
		user.setExpSimple((Integer)ability.get(UserChangeFlag.EXP.value()));
		user.setPowerSimple((Integer)ability.get(UserChangeFlag.POWER.value()));
		user.setDamage((Integer)ability.get(UserChangeFlag.DAMAGE.value()));
		user.setAttack((Integer)ability.get(UserChangeFlag.ATTACK.value()));
		user.setDefend((Integer)ability.get(UserChangeFlag.DEFEND.value()));
		user.setAgility((Integer)ability.get(UserChangeFlag.AGILITY.value()));
		user.setLuck((Integer)ability.get(UserChangeFlag.LUCK.value()));
		user.setSkin((Integer)ability.get(UserChangeFlag.SKIN.value()));
		user.setTkew((Integer)ability.get(UserChangeFlag.TKEW.value()));
		/*
		DBObject valueMapObj = (DBObject)ability.get(UserChangeFlag.VALUEMAP.value());
		if ( valueMapObj != null ) {
			try {
				user.setValueMap((HashMap<PropDataEnhanceType.Field, Integer>)
					MongoDBUtil.constructMapObject(valueMapObj));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		Integer ach = (Integer)ability.get(UserChangeFlag.ACHIEVEMENT.value());
		if ( ach != null ) {
			user.setAchievement(ach.intValue());
		}
		Integer totalActions = (Integer)ability.get(UserChangeFlag.ROLETOTALACTIONS.value());
		if ( totalActions != null ) {
			user.setRoleTotalAction(totalActions.intValue());
		} else {
			int limit = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROLE_ACTION_LIMIT, 200);
			user.setRoleTotalAction(200);
		}
		user.setIsvip((Boolean)vip.get(UserChangeFlag.ISVIP.value()));
		user.setViplevel((Integer)vip.get(UserChangeFlag.VIPLEVEL.value()));
		user.setVipbdate((Date)vip.get(UserChangeFlag.VIPBDATE.value()));
		user.setVipedate((Date)vip.get(UserChangeFlag.VIPEDATE.value()));
		user.setVipexp((Integer)vip.get(UserChangeFlag.VIPEXP.value()));
		user.setConfigEffectSwitch((Boolean)config.get(UserChangeFlag.CONFIGEFFECTSWITCH.value()));
		user.setConfigEffectVolume((Integer)config.get(UserChangeFlag.CONFIGEFFECTVOLUME.value()));
		user.setConfigHideGlass((Boolean)config.get(UserChangeFlag.CONFIGHIDEGLASS.value()));
		user.setConfigHideHat((Boolean)config.get(UserChangeFlag.CONFIGHIDEHAT.value()));
		user.setConfigHideSuite((Boolean)config.get(UserChangeFlag.CONFIGHIDESUITE.value()));
		user.setConfigLeadFinish((Boolean)config.get(UserChangeFlag.CONFIGLEADFINISH.value()));
		user.setConfigMusicSwitch((Boolean)config.get(UserChangeFlag.CONFIGMUSICSWITCH.value()));
		user.setConfigMusicVolume((Integer)config.get(UserChangeFlag.CONFIGMUSICVOLUME.value()));
		user.setEmail((String)profile.get(UserChangeFlag.EMAIL.value()));
		user.setIconurl((String)profile.get(UserChangeFlag.ICONURL.value()));
		user.setClient((String)profile.get(UserChangeFlag.CLIENT.value()));
		user.setGender(Gender.values()[(Integer)profile.get(UserChangeFlag.GENDER.value())]);
		user.setCountry((String)profile.get(UserChangeFlag.COUNTRY.value()));
		user.setCdate((Date)profile.get(UserChangeFlag.CDATE.value()));
		user.setLdate((Date)profile.get(UserChangeFlag.LDATE.value()));
		user.setTdate((Date)profile.get(UserChangeFlag.TDATE.value()));
		user.setTotalmin((Integer)profile.get(UserChangeFlag.TOTALMIN.value()));
		Integer totalKills = (Integer)profile.get(UserChangeFlag.TOTALKILL.value());
		if ( totalKills != null ) {
			user.setTotalKillSimple(totalKills.intValue());
		} else {
			user.setTotalKillSimple(0);
		}
		user.setWins((Integer)profile.get(UserChangeFlag.WINS.value()));
		user.setWinOdds((Integer)profile.get(UserChangeFlag.WINODDS.value()));
		user.setFailcount((Integer)profile.get(UserChangeFlag.FAILCOUNT.value()));
		user.setBattleCount((Integer)profile.get(UserChangeFlag.BATTLECOUNT.value()));
		user.setChannel((String)profile.get(UserChangeFlag.CHANNEL.value()));
		user.setContinuLoginTimes((Integer)profile.get(UserChangeFlag.CONTINULOGINTIMES.value()));
		user.setRemainLotteryTimes((Integer)profile.get(UserChangeFlag.REMAINLOTTERYTIMES.value()));
		user.setAbtest((String)profile.get(UserChangeFlag.ABTEST.value()));
		Boolean guestBool = (Boolean)profile.get(UserChangeFlag.ISGUEST.value());
		if ( guestBool != null ) {
			user.setGuest(guestBool.booleanValue());
		}
		Boolean tutorialBool = (Boolean)profile.get(UserChangeFlag.TUTORIAL.value());
		if ( tutorialBool != null ) {
			user.setTutorial(tutorialBool.booleanValue());
		}
		Boolean verifiedBool = (Boolean)profile.get(UserChangeFlag.VERIFIED.value());
		if ( verifiedBool != null ) {
			user.setVerifiedEmail(verifiedBool.booleanValue());
		}
		String screen = (String)profile.get(UserChangeFlag.SCREEN.value());
		if ( screen != null ) {
			user.setScreen(screen);
		}
		String deviceToken = (String)profile.get(UserChangeFlag.DEVICETOKEN.value());
		if ( deviceToken != null ) {
			user.setDeviceToken(deviceToken);
		}
		Integer tutorialMark = (Integer)profile.get(UserChangeFlag.TUTORIALMARK.value());
		if ( tutorialMark != null ) {
			user.setTutorialMark(tutorialMark.intValue());
		}
		String guildId = (String)profile.get(UserChangeFlag.GUILDID.value());
		if ( guildId != null ) {
			user.setGuildId(guildId);
		}
		//Try to set the session key if exist.
		user.setSessionKey(GameContext.getInstance().findSessionKeyByUserId(user.get_id()));
		
		DBObject locationObj = (DBObject)(userObj.get(UserChangeFlag.LOC.value()));
		Location loc = new Location();
		loc.x = (Integer)(locationObj.get("x"));
		loc.y = (Integer)(locationObj.get("y"));
		user.setLocation(loc);
		
		List<String> toolList = (List<String>)userObj.get("tools");
		int index = 0;
		for ( String toolName : toolList ) {
			if ( toolName == null || toolName.length()<=0 ) {
				user.setTool(index++, null);
			} else {
				user.setTool(index++, BuffToolType.valueOf(toolName));
			}
		}
		Integer maxCount = (Integer)userObj.get("maxtool");
		if ( maxCount == null ) {
			user.setMaxToolCount(GameDataManager.getInstance()
					.getGameDataAsInt(GameDataKey.USER_TOOL_MAX, 3));
		} else {
			user.setMaxToolCount(maxCount);
		}
		Integer currCount = (Integer)userObj.get("currtool");
		if ( currCount == null ) {
			user.setCurrentToolCount(0);
		} else {
			user.setCurrentToolCount(currCount);
		}
		if ( weibo != null ) {
			user.getWeiboTokenMap().putAll((Map<String,String>)weibo);
		}
		
		return user;
	}
	
	
	/**
	 * Construct the User object from database DBObject.
	 * @param userObj
	 * @return
	 */
	private BasicUser constructBasicUserObject(DBObject userObj) {
		DBObject login = (DBObject)userObj.get("login");
		DBObject ability = (DBObject)userObj.get("ability");
		DBObject vip = (DBObject)userObj.get("vip");
		DBObject profile = (DBObject)userObj.get("profile");
		
		if ( login == null ) return null;
		
		BasicUser user = new BasicUser();
		user.set_id(UserId.fromBytes((byte[])userObj.get(_ID)));
		user.setUsername((String)login.get(UserChangeFlag.USERNAME.value()));
		user.setRoleName((String)login.get(UserChangeFlag.ROLENAME.value()));
		Integer level = (Integer)ability.get(UserChangeFlag.LEVEL.value());
		if ( level != null ) {
			user.setLevel(level.intValue());
		}
		Integer power = (Integer)ability.get(UserChangeFlag.POWER.value());
		if ( power != null ) {
			user.setPower(power.intValue());
		}
		if ( vip != null ) {
			Boolean vipBool = (Boolean)vip.get(UserChangeFlag.ISVIP.value());
			if ( vipBool != null ) {
				user.setIsvip(vipBool.booleanValue());
			} else {
				user.setIsvip(false);
			}
			Integer vipLevel = (Integer)vip.get(UserChangeFlag.VIPLEVEL.value());
			if ( vipLevel != null ) {
				user.setViplevel(vipLevel.intValue());
			}
		} else {
			user.setIsvip(false);
		}
		user.setIconurl((String)profile.get(UserChangeFlag.ICONURL.value()));
		user.setGender(Gender.values()[(Integer)profile.get(UserChangeFlag.GENDER.value())]);
		user.setCountry((String)profile.get(UserChangeFlag.COUNTRY.value()));
		user.setUuid((String)profile.get(UserChangeFlag.UUID.value()));
		user.setGuildId((String)profile.get(UserChangeFlag.GUILDID.value()));
		
		Integer wins = (Integer)ability.get(UserChangeFlag.WINS.value());
		if ( wins != null ) {
			user.setWins(wins.intValue());
		}
		Integer failcount = (Integer)ability.get(UserChangeFlag.FAILCOUNT.value());
		if ( failcount != null ) {
			user.setFails(failcount);
		}
		return user;
	}
	
	/**
	 * Since the User collection has a lot of fields,
	 * we only need the basic fields. So we have to filter out 
	 * others.
	 * 
	 * @return
	 */
	private DBObject constructBasicUserQueryFields() {
		DBObject fields = createDBObject();
		fields.put(_ID, ONE);
		fields.put(LOGIN_USERNAME, ONE);
		fields.put("login.rolename", ONE);
		fields.put("ability.level", ONE);
		fields.put("ability.power", ONE);
		fields.put("vip.isvip", ONE);
		fields.put("vip.viplevel", ONE);
		fields.put("profile.iconurl", ONE);
		fields.put("profile.gender", ONE);
		fields.put("profile.country", ONE);
		fields.put("profile.wins", ONE);
		fields.put("profile.failcount", ONE);
		fields.put("profile.guildid", ONE);
		fields.put("login.uuid", ONE);
		return fields;
	}
	
	/**
	 * Construct the bag database object.
	 * @param bag
	 * @return
	 */
	private DBObject constructBagDBObject(Bag bag) {
		DBObject dbObj = createDBObject();
		if ( bag.getUserid() != null ) {
			dbObj.put(_ID, bag.getUserid().getInternal());
		}
		List<PropData> wearPropDataList = bag.getWearPropDatas();
		MapDBObject wearObj = null;
		MapDBObject wearIndexObj = createMapDBObject();
		for ( int i=0; i<wearPropDataList.size(); i++ ) {
			PropData propData = wearPropDataList.get(i);
			if ( propData != null ) {
				wearObj = createMapDBObject();
				wearObj.putAll(propData);
				wearIndexObj.put(String.valueOf(i), wearObj);
			}
		}
		dbObj.put("wears", wearIndexObj);
		List<PropData> propDataList = bag.getOtherPropDatas();
		MapDBObject itemIndexObj = createMapDBObject();
		MapDBObject itemObj = null;
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			if ( propData != null ) {
				itemObj = createMapDBObject();
				itemObj.putAll(propData);
				itemIndexObj.put(String.valueOf(i), itemObj);
			} else {
				itemIndexObj.put(String.valueOf(i), null);
			}
		}
		dbObj.put("maxcount", bag.getMaxCount());
		dbObj.put("currentcount", bag.getCurrentCount());
		dbObj.put("items", itemIndexObj);
		
		return dbObj;
	}
	
	/**
	 * Construct the bag database object.
	 * @param bag
	 * @return
	 */
	private DBObject constructBagDBObjectUpdate(Bag bag) {
		Set<Integer> flags = bag.clearMarkedChangeFlag();
		
		DBObject unsetBagObj = createDBObject();
		DBObject setBagObj   = createDBObject();
		
		//Update the wear state.
		List<PropData> wearPropDataList = bag.getWearPropDatas();
		for ( int i=0; i<wearPropDataList.size(); i++ ) {
			PropData propData = wearPropDataList.get(i);
			if ( propData != null ) {
				if ( flags.contains(i) ) {
					flags.remove(i);
					MapDBObject wearObj = createMapDBObject();
					wearObj.putAll(propData);
					setBagObj.put("wears.".concat(String.valueOf(i)), wearObj);
				}
			} else {
				unsetBagObj.put("wears.".concat(String.valueOf(i)), ONE);
			}
		}
		List<PropData> propDataList = bag.getOtherPropDatas();

		//Only update the modified item.
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
	
			if ( propData != null ) {
				if ( flags.contains(BAG_WEAR_COUNT + i) ) {
					flags.remove(BAG_WEAR_COUNT+i);
					MapDBObject itemObj = createMapDBObject();
					itemObj.putAll(propData);
					setBagObj.put("items.".concat(String.valueOf(i)), itemObj);
				}
			} else {
				unsetBagObj.put("items.".concat(String.valueOf(i)), ONE);
			}
		}
		
		//Delete all extra items
		for ( int flag : flags ) {
			unsetBagObj.put("items.".concat(String.valueOf(flag-BAG_WEAR_COUNT)), ONE);
		}
		
		setBagObj.put("maxcount", bag.getMaxCount());
		setBagObj.put("currentcount", bag.getCurrentCount());
		
		DBObject setObj = createDBObject();
		setObj.put(OP_UNSET, unsetBagObj);
		setObj.put(OP_SET,   setBagObj);
		return setObj;
	}
	
	/**
	 * Construct the User object from database DBObject.
	 * @param userObj
	 * @return
	 */
	private Bag constructUserBag(DBObject bagObj) {		
		Bag bag = new Bag();
		
		Object dbObjId = bagObj.get(_ID);
		byte[] array = null;
		if ( dbObjId instanceof String ) {
			array = StringUtil.hexStringToBytes((String)dbObjId);
		} else {
			array = (byte[])dbObjId;
		}
		bag.setUserd(UserId.fromBytes(array));
		
		bag.setMaxCount((Integer)bagObj.get("maxcount"));
		
		DBObject wears = (DBObject)bagObj.get("wears");
		Set<String> keySet = wears.keySet();
		for ( String key : keySet ) {
			DBObject item = (DBObject)wears.get(key);
			int index = toInt(key, -1);
			PropDataEquipIndex equipIndex = PropDataEquipIndex.fromIndex(index);
			if ( equipIndex != null) {
				//PropData propData = convertDBObjectToPropData(item);
				PropData propData = (PropData)constructObject(item);
				bag.setWearPropData(propData, equipIndex);				
			} else {
				if ( logger.isErrorEnabled() ) {
					logger.error("WearPropData for user " + bag.getUserid() + " key: " + key + " is not a number");
				}
			}
		}
		DBObject items = (DBObject)bagObj.get("items");
		if ( items != null ) {
			keySet = items.keySet();
			for ( String key : keySet ) {
				DBObject item = (DBObject)items.get(key);
				PropData propData = null;
				if ( item != null ) {
//					propData = convertDBObjectToPropData(item);
					propData = (PropData)constructObject(item);
				}
				int pew = toInt(key, 0) + Bag.BAG_WEAR_COUNT;
				bag.setOtherPropDataAtPew(propData, pew);
			}
		}

		bag.setCurrentCount((Integer)bagObj.get("currentcount"));
		//Reset the change flag.
		bag.clearGeneralChangeFlag();
		bag.clearMarkedChangeFlag();
		return bag;
	}
	
	/**
	 * Convert the DBObject to PropData object
	 * @param dbObj
	 * @return
	 */
//	private PropData convertDBObjectToPropData(DBObject dbObj) {
//		PropData propData = new PropData();
//		propData.setItemId((String)(dbObj.get("itemId")));
//		propData.setName((String)(dbObj.get("name")));
//		propData.setPropIndate((Integer)(dbObj.get("propIndate")));
//		propData.setPropUsedTime((Integer)(dbObj.get("propUsedTime")));
//		propData.setCount((Integer)(dbObj.get("count")));
//		propData.setColor((Integer)(dbObj.get("color")));
//		propData.setLevel((Integer)(dbObj.get("level")));
//		propData.setAttackLev((Integer)(dbObj.get("attackLev")));
//		propData.setDefendLev((Integer)(dbObj.get("defendLev")));
//		propData.setAgilityLev((Integer)(dbObj.get("agilityLev")));
//		propData.setLuckLev((Integer)(dbObj.get("luckLev")));
//		propData.setSign((Integer)(dbObj.get("sign")));
//		String value = (String)dbObj.get("valuetype");
//		propData.setValuetype(PropDataValueType.valueOf(value));
//		propData.setBanded((Boolean)(dbObj.get("banded")));
//		propData.setDuration((Integer)(dbObj.get("duration")));
//
//		return propData;
//	}
}
