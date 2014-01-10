package com.xinqihd.sns.gameserver.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.AbstractMongoManager;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;

public class AdminUserManager {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMongoManager.class);

	//In development, I suggest use the saftWrite mode to find bugs early.
	//In production, we should disable it.
	protected boolean isSafeWrite = true;
	
	protected String databaseName = "gameadmin";
	
	protected String namespace = "babywar";
	
	protected String collection = "adminusers";
	
	protected String indexKey = "username";
	
	private static AdminUserManager instance = new AdminUserManager();
		
	/**
	 * The constructor connect to database according to given parameters.
	 * 
	 * Please call the {@link UserManager#getInstance()}
	 * DO NOT call it directly or the system will be banded to Mongodb. 
	 */
	AdminUserManager() {
		if ( logger !=null && logger.isDebugEnabled() ) {
			logger.debug("database:"+databaseName+",namespace:"+namespace+
					",isSafeWrite:"+isSafeWrite + ",collection:" + collection + 
					",index:"+indexKey);
		}
		//Ensure Index
		MongoUtil.ensureIndex(databaseName, namespace, collection, indexKey, true);
	}

	
	public static AdminUserManager getInstance() {
		return instance;
	}
	
	/**
	 * Save the adminuser to database.
	 * @param adminUser
	 * @return
	 */
	public void saveAdminUser(AdminUser adminUser) {
		MapDBObject adminUserObj = new MapDBObject();
		adminUserObj.putAll(adminUser);
		DBObject query = MongoUtil.createDBObject(indexKey, adminUser.getUsername());
		MongoUtil.saveToMongo(query, adminUserObj, databaseName, namespace, collection, isSafeWrite);
	}
	
	/**
	 * Query the admin user from database
	 * @param username
	 * @return
	 */
	public AdminUser queryAdminUser(String username) {
		DBObject query = MongoUtil.createDBObject(indexKey, username);
		DBObject adminUserObj = MongoUtil.queryFromMongo(query, databaseName, namespace, collection, null);
		if ( adminUserObj != null ) {
			AdminUser adminUser = (AdminUser)MongoUtil.constructObject(adminUserObj);
			return adminUser;
		}
		return null;
	}
	
	/**
	 * Remove an admin user from database.
	 * @param username
	 * @return
	 */
	public void removeAdminUser(String username) {
		DBObject query = MongoUtil.createDBObject(indexKey, username);
		MongoUtil.deleteFromMongo(query, databaseName, namespace, collection, isSafeWrite);
	}
	
	public static void main(String[] args) {
		AdminUserManager manager = AdminUserManager.getInstance();
		AdminUser admin = new AdminUser();
		admin.setUsername("wangqi");
		admin.setPassword("wangqi123");
		admin.addPriviledge(PriviledgeKey.all_priviledge);
		manager.saveAdminUser(admin);
		
		admin = new AdminUser();
		admin.setUsername("liyang");
		admin.setPassword("liyang123");
		admin.addPriviledge(PriviledgeKey.all_priviledge);
		manager.saveAdminUser(admin);
		
		admin = new AdminUser();
		admin.setUsername("chenxinyan");
		admin.setPassword("chenxinyan123");
		admin.addPriviledge(PriviledgeKey.all_priviledge);
		manager.saveAdminUser(admin);
		
		admin = new AdminUser();
		admin.setUsername("jsding");
		admin.setPassword("jsding123");
		admin.addPriviledge(PriviledgeKey.all_priviledge);
		manager.saveAdminUser(admin);
	}
}
