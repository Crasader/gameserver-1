package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseServerList.BseServerList;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class ServerListManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(ServerListManager.class);

	private static final String COLL_NAME = "servers";
	
	private static final String INDEX_NAME = "_id";
	
	/**
	 * The hardcoded serverId
	 */
	public static final String FIRST_SERVER_ID = "s0001";
	
	private static ConcurrentHashMap<String, ServerPojo> dataMap = 
			new ConcurrentHashMap<String, ServerPojo>();
	
	private static Set<ServerPojo> serverSet = new TreeSet<ServerPojo>();
	
	private static final ServerListManager instance = new ServerListManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static ServerListManager getInstance() {
		return instance;
	}
	
	ServerListManager() {
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
		serverSet.clear();
		
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized ( dataMap ) {
			for ( DBObject obj : list ) {
				ServerPojo server = (ServerPojo)MongoDBUtil.constructObject(obj);
				dataMap.put(server.getId(), server);
				logger.debug("Load server id {} name {} from database.", server.getId(), server.getName());
				serverSet.add(server);
			}
		}
	}
	
	/**
	 * Get the given weapon by its id.
	 * @param id
	 * @return
	 */
	public ServerPojo getServerById(String id) {
		if ( id != null ) {
			return dataMap.get(id);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the underlying Weapon collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public Collection<ServerPojo> getServers() {
		return serverSet;
	}
	
	/**
	 * 
	 * @param pojo
	 */
	public void addServer(ServerPojo server) {
		MapDBObject obj = MongoDBUtil.createMapDBObject(server);
		String _id = server.getId();
		DBObject query = MongoDBUtil.createDBObject("_id", _id);
		MongoDBUtil.saveToMongo(query, obj, databaseName, namespace, COLL_NAME, isSafeWrite);
		synchronized ( dataMap ) {
			dataMap.put(server.getId(), server);
			serverSet.add(server);
		}
	}

	/**
	 * Remove the serverId prefix from user's roleName
	 * @param roleName
	 * @param serverId
	 * @return
	 */
	public final String removeServerPrefix(String roleName, String serverId) {
		/**
		if ( StringUtil.checkNotEmpty(roleName) && StringUtil.checkNotEmpty(serverId) ) {
			if ( roleName.startsWith(serverId) ) {
				return roleName.substring(serverId.length()+1);
			}
		} else {
			logger.debug("roleName is null");
		}
		*/
		return roleName;
	}
	
	/**
	 * Add the serverId prefix to user's roleName
	 * @param roleName
	 * @param serverId
	 * @return
	 */
	public final String addServerPrefix(String roleName, String serverId) {
		/*
		if ( StringUtil.checkNotEmpty(roleName) && StringUtil.checkNotEmpty(serverId) ) {
			if ( !roleName.startsWith(serverId) ) {
				return StringUtil.concat(serverId, Constant.DOT, roleName);
			}
		} else {
			logger.debug("roleName is null");
		}
		*/
		return roleName;
	}
	
	/**
	 * Get the recommended server setting.
	 * @return
	 */
	public ServerPojo getRecommendServer(Account account) {
		if ( serverSet != null && account != null ) {
			for ( ServerPojo serverPojo : serverSet ) {
				if ( checkServerPojo(serverPojo, account.getChannel(), account.getVersion())) {
					return serverPojo;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the recommended server setting.
	 * @return
	 */
	public ServerPojo getRecommendServer(String channel, String version) {
		if ( serverSet != null && channel != null ) {
			for ( ServerPojo serverPojo : serverSet ) {
				if ( checkServerPojo(serverPojo, channel, version)) {
					return serverPojo;
				}
			}
		}
		return null;
	}
	
	/**
	 * Convert this object to BseServerList
	 * @return
	 */
	public BseServerList toBseServerList(Account account) {
		BseServerList.Builder builder = BseServerList.newBuilder();
		String channel = null;
		if ( account != null ) {
			channel = account.getChannel();
		}
		for (  ServerPojo serverPojo : serverSet ) {
			/**
			 * Only if the serverPojo matchs the user's channel
			 * and it has not been merged to other server are 
			 * displayed.
			 * 2013-02-04 
			 */
			if ( checkServerPojo(serverPojo, channel, account.getVersion()) && 
					!StringUtil.checkNotEmpty(serverPojo.getMergeId()) ) {
				builder.addServerlist(serverPojo.toXinqiServer(account));
			}
		}
		builder.setRecommend(getRecommendServer(account).toXinqiServer(account));
		if ( account != null ) {
			ServerPojo lastServer = this.getServerById(account.getLastServerId());
			boolean success = true;
			if ( lastServer != null ) {
				/**
				 * 需要检查下当前这个lastServer是否还可用
				 */
				success = checkServerPojo(lastServer, channel, account.getVersion());
				if ( success ) {
					builder.setLastserver(lastServer.toXinqiServer(account));
				}
			}
			UserId lastUserId = account.getLastUserId();
			String lastServerId = account.getLastServerId();
			if ( !success ) {
				//如果服务器隐藏了，上次登陆的玩家ID也需要隐藏
				lastUserId = null;
				lastServerId = null;
				account.setLastServerId(null);
				account.setLastUserId(null);
			}
			BasicUser basicUser = null;
			if ( lastUserId != null ) {
				basicUser = UserManager.getInstance().queryBasicUser(lastUserId);
			} else {
				/**
				 * 判断用户是否第一次登陆
				 */
				ArrayList<ServerRoleList> serverRoleList = account.getServerRoles();
				if ( serverRoleList.size()>0 ) {
					//Old user
					UserId userId = null;
					ServerRoleList roleList = serverRoleList.get(serverRoleList.size()-1);
					ArrayList<String> userIds = roleList.getUserIds();
					if ( userIds.size() > 0 ) {
						String userIdStr = userIds.get(userIds.size()-1);
						userId = UserId.fromString(userIdStr);
					}
					if ( userId != null ) {
						basicUser = UserManager.getInstance().queryBasicUser(userId);
					} else {
						ArrayList<String> roleNames = roleList.getRoleNames();
						if ( roleNames.size() > 0 ) {
							/**
							 * TODO Account中新增加了userId字段，为了向后
							 * 兼容，需要保存userId到账号中.
							 */
							int index = roleNames.size()-1;
							String roleName = roleNames.get(index);
							basicUser = UserManager.getInstance().queryBasicUserByRoleName(roleName);
							if ( roleList.getUserIds().size()>index ) {
								roleList.getUserIds().set(index, basicUser.get_id().toString());
							} else {
								roleList.addUserId(basicUser.get_id());
							}
							AccountManager.getInstance().saveAccount(account);
						}
					}
				}
			}
			if ( basicUser != null ) {
				builder.setLastuserid(basicUser.get_id().toString());
				builder.setLastrolename(basicUser.getRoleName());
			}
			if ( lastServerId !=null ) {
				ServerPojo serverPojo = ServerListManager.getInstance().getServerById(lastServerId);
				builder.setLastserver(serverPojo.toXinqiServer(account));
			} else {
				//Default to the recomment server
				builder.setLastserver(ServerListManager.getInstance().
						getRecommendServer(account).toXinqiServer(account));
			}
		}
		return builder.build();
	}
	
	/**
	 * Check if the server pojo is suitable for given server.
	 * @param serverPojo
	 * @param channel
	 * @return
	 */
	public boolean checkServerPojo(ServerPojo serverPojo, String channel, String version) {
		boolean success = true;
		if ( StringUtil.checkNotEmpty(serverPojo.getChannel()) && 
				StringUtil.checkNotEmpty(channel)) {
			Pattern pattern = Pattern.compile(serverPojo.getChannel());
			if ( pattern.matcher(channel).find() ) {
				success = true;
			} else {
				success = false;
			}
		} else {
			success = true;
		}
		
		if ( success ) {
			//Check the server version pattern
			if ( StringUtil.checkNotEmpty(serverPojo.getVersion()) && 
					StringUtil.checkNotEmpty(version)) {
				Pattern pattern = Pattern.compile(serverPojo.getChannel());
				if ( pattern.matcher(channel).find() ) {
					success = true;
				} else {
					success = false;
				}
			} else {
				success = true;
			}
		}
		return success;
	}
}
