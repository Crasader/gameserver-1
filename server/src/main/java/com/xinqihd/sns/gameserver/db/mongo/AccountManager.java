package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil.*;
import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.RegisterErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseCreateRole.BseCreateRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseDeleteRole.BseDeleteRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseFillProfile.BseFillProfile;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister.BseRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseServerList.BseServerList;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class AccountManager extends AbstractMongoManager {

	/**
	 * 为了保持向后兼容，需要对现有的账号进行转换，但是为了避免重复
	 * 转化，在User中的abtest字段保存这个值，如果已经设置了就不再转换。
	 */
	private static final String USER_CONVERTED_FLAG = "accounted";

	private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);

	private static final int ACCOUNT_NAME_LEN = 16;
	
	private static final String COLL_NAME = "accounts";
	private static final String UUID_COLL_NAME = "uuids";
	
	private static final String INDEX_NAME = "_id";
	private static final String LOGIN_USERNAME = "userName";
	
	/**
	 * The key for storing Account object in IoSession.
	 */
	public static final String IOSESSION_ACCOUNT = "iosession.account";
	
	/**
	 * The client and server version
	 */
	private static final String KEY_MAJOR_VERSION = "majorv";
	private static final String KEY_MINOR_VERSION = "minorv";
	private int majorVersion = 0;
	private int minorVersion = 0;
	
	private static final AccountManager instance = new AccountManager();
	
	private Pattern serverIdPattern = null;
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static AccountManager getInstance() {
		return instance;
	}
	
	AccountManager() {
		super(
				GlobalConfig.getInstance().getStringProperty("mongdb.database"),
				GlobalConfig.getInstance().getStringProperty("mongdb.namespace"),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		//Ensure Index
		MongoDBUtil.ensureIndex(databaseName, namespace, COLL_NAME, LOGIN_USERNAME, true);
		MongoDBUtil.ensureIndex(databaseName, namespace, UUID_COLL_NAME, INDEX_NAME, true);

		/**
		 * Get config version
		 */
		DBObject loginDBObj = MongoDBUtil.queryFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		majorVersion = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.LOGIN_MAJOR_VERSION, 1);
		minorVersion = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.LOGIN_MINOR_VERSION, 8);
		
		String currentServerId = System.getProperty("serverId");
		if ( currentServerId != null ) {
			serverIdPattern = Pattern.compile(currentServerId);
		}
		logger.info("majorVersion:{}, minorVersion:{}", new Object[]{majorVersion, minorVersion});
	}
	
	/**
	 * Save the user account to database
	 * @param account
	 * @return
	 */
	public boolean saveAccount(Account account) {
		try {
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(account);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, account.get_id());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Update the user account to database
	 * @param account
	 * @return
	 */
	public boolean updateAccount(Account account) {
		try {
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(account);
			dbObj.removeField(INDEX_NAME);
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, account.get_id());
			DBObject setObj = createDBObject();
			setObj.put(OP_SET, dbObj);
			MongoDBUtil.saveToMongo(query, setObj, databaseName, namespace, COLL_NAME, isSafeWrite);
			logger.info("Update account: {}, roles:{}", account.getUserName(), account.getServerRoles());
		} catch (Exception e) {
			logger.warn("Failed to update account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Remove the user account.
	 * @param account
	 * @return
	 */
	public boolean removeAccount(Account account) {
		try {
			DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, account.get_id());
			MongoDBUtil.deleteFromMongo(query, databaseName, namespace, COLL_NAME, isSafeWrite);
		} catch (Exception e) {
			logger.warn("Failed to save account", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Get the account's latest roleName
	 * @param account
	 * @return
	 */
	public static final String getCurrentRoleName(Account account) {
		if ( account != null ) {
			List<ServerRoleList> serverRoles = account.getServerRoles();
			int size = serverRoles.size();
			if ( size>0 ) {
				String roleName = null;
				LOOP:
				for ( ServerRoleList serverRole : serverRoles ) {
					if ( serverRole != null ) {
						List<String> userIds = serverRole.getUserIds();
						if ( userIds.size() <= 0 ) {
							logger.warn("There is no userId in account");
							return null;
						}
						SessionManager manager = GameContext.getInstance().getSessionManager();
						int index = -1;
						for ( int i=0; i<userIds.size(); i++ ) {
							String userIdStr = userIds.get(i);
							UserId userId = UserId.fromString(userIdStr);
							if ( userId != null ) {
								SessionKey sessionKey = manager.findSessionKeyByUserId(userId);
								if ( sessionKey != null ) {
									index = i;
									break;
								}
							}
						}//for...
						List<String> roleNames = serverRole.getRoleNames();
						if ( index >= 0 && index < roleNames.size() ) {
							roleName = roleNames.get(index);
							break;
						}
					}
				}
				if ( roleName != null ) {
					return roleName;
				} else {
					UserId userId = account.getLastUserId();
					BasicUser basicUser = UserManager.getInstance().queryBasicUser(userId);
					if ( basicUser != null ) {
						roleName = basicUser.getRoleName();
					} else {
						logger.warn("There is no roleName in account:{}", account.getUserName());
					}
				}
				return roleName;
			}
		} else {
			logger.warn("The account is null to get current role");
		}
		return null;
	}
	
	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public Account queryAccountById(String accountId) {
		Account account = null;
		DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, accountId);
		account = (Account)MongoDBUtil.queryObjectFromMongo(query, databaseName, namespace, COLL_NAME, null);
		return account;
	}
	
	/**
	 * Query the user account by id.
	 * @param accountId
	 * @return
	 */
	public Account queryAccountByName(String accountName) {
		Account account = null;
		DBObject query = MongoDBUtil.createDBObject(LOGIN_USERNAME, accountName);
		account = (Account)MongoDBUtil.queryObjectFromMongo(query, databaseName, namespace, COLL_NAME, null);
		return account;
	}
	
	/**
	 * Query the Account object by roleName
	 * @param roleName
	 * @return
	 */
	public Account queryAccountByRoleName(String roleName) {
		//{"serverRoles.roleNames": "s0001.22222"}
		Account account = null;
		DBObject query = MongoDBUtil.createDBObject("serverRoles.roleNames", roleName);
		account = (Account)MongoDBUtil.queryObjectFromMongo(query, databaseName, namespace, COLL_NAME, null);
		return account;
	}
	
	/**
	 * Check if the username exists in database
	 * @param userName
	 * @return
	 */
	public boolean checkUserNameExist(String accountName) {
		DBObject query = createDBObject();
		query.put(LOGIN_USERNAME, accountName);
		long count = MongoDBUtil.countQueryResult(query, databaseName, namespace, COLL_NAME);
		return count == 1;
	}
	
	/**
	 * Check if the accountid exists in database
	 * @param userName
	 * @return
	 */
	public boolean checkAccountExist(String accountId) {
		DBObject query = createDBObject();
		query.put(INDEX_NAME, accountId);
		long count = MongoDBUtil.countQueryResult(query, databaseName, namespace, LOGIN_USERNAME);
		return count == 1;
	}
	
	/**
	 * Check if the given UUID is forbidden
	 * @param account
	 * @param uuid
	 * @return
	 */
	public DBObject checkForbiddenUUID(String uuid) {
		if ( StringUtil.checkNotEmpty(uuid) ) {
			DBObject query = MongoDBUtil.createDBObject(Constant._ID, uuid);
			DBObject dbObj = MongoDBUtil.queryFromMongo(query, databaseName, namespace, UUID_COLL_NAME, null);
			if ( dbObj != null ) {
				logger.info("The uuid {} is forbidden to login", uuid);
				return dbObj;
			}
		}
		return null;
	}
	
	/**
	 * For those users that are created before the accounting system developed,
	 * game server should provide a method to convert tranditional user to 
	 * new account object.
	 * 
	 * @param user
	 * @return
	 */
	public Account convertUserToAccount(User user) {
		ServerPojo server = ServerListManager.getInstance().getServerById(ServerListManager.FIRST_SERVER_ID);
		if ( server != null && user != null ) {
			if ( !USER_CONVERTED_FLAG.equals(user.getAbtest()) ) {
				Account account = new Account();
				account.set_id(StringUtil.concat(server.getId(), LoginManager.getInstance().getRandomUserName()));
				account.setEmail(user.getEmail());
				account.setGender(user.getGender());
				account.setPassword(user.getPassword());
				account.setUserName(user.getRoleName());
				ServerRoleList roleList = new ServerRoleList();
				roleList.setServerId(server.getId());
				/**
				 * Change the roleName to new format.
				 */
				String prefixedRoleName = ServerListManager.getInstance().addServerPrefix(
						user.getRoleName(), server.getId());
				user.setRoleName(prefixedRoleName);
				user.setAbtest(USER_CONVERTED_FLAG);
				roleList.addUserId(user.get_id());
				roleList.addRoleName(prefixedRoleName);
				account.addServerRole(roleList);
				return account;
			} else {
				logger.warn("The user {} is already converted:{}", user.getRoleName());
				Account account = this.queryAccountByName(user.getUsername());
				return account;
			}
		} else {
			logger.warn("The initial server id is not found in database ");
		}
		return null;
	}
	
	/**
	 * The account login into game.
	 * 
	 * @param accountName
	 * @param password
	 * @param loginConfigVersion
	 * @param majorVersion
	 * @param minorVersion
	 * @param session
	 * @param loginInfo
	 * @param sessionKey
	 */
	public boolean login(String accountName, String password, 
			int loginConfigVersion, int majorVersion, int minorVersion,
			IoSession session, BceLogin loginInfo, SessionKey sessionKey) {
		/**
		 * 1. Check if the server goes into maintaince mode
		 */
		if ( GlobalConfig.getInstance().getBooleanProperty(GlobalConfigKey.maintaince_mode) ) {
			/**
			 * The server goes into maintaince mode.
			 */
			BseLogin.Builder loginRep = BseLogin.newBuilder();
			loginRep.setCode(ErrorCode.MAINTANCE.ordinal());
			loginRep.setDesc(Text.text(ErrorCode.MAINTANCE.desc()));
			XinqiMessage response = new XinqiMessage();
			response.payload = loginRep.build();
			session.write(response);

			String url = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.maintaince_url);
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					session, url, Action.NOOP, Type.MAINTAINCE);
			
			return false;
		}
				
		/**
		 * 2. Check if the client version is compatible.
		 */
		String clientVersion = StringUtil.concat(majorVersion, ".", minorVersion, ".", loginInfo.getTinyversion());
		boolean checkPass = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.VERSION_CHECK,
				majorVersion, minorVersion, loginInfo.getTinyversion(), loginInfo.getChannel(), 
				this.majorVersion, this.minorVersion, session, clientVersion);
		if ( !checkPass ) {
			return false;
		}
		
		/**
		 * Use new method to check UUID
		 * 2012-12-27
		 */
		DBObject uuidObj = checkForbiddenUUID(loginInfo.getUuid());
		/*
		boolean uuidPass = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.UUID_CHECK,
				session, loginInfo.getUuid());
		*/
		if ( uuidObj != null ) {
			sendLoginResponse(session, ErrorCode.S_REMOVED, (String)uuidObj.get("message"));
			return false;
		}
		
		/**
		 * 3. Start the normal login process.
		 */
		boolean loginSuccess = false;
		boolean isGuestRegister = false;
		Account account = null;
		if ( loginInfo.getIsthirdauth() ) {
			if ( accountName != null ) {
				account = queryAccountByName(accountName);
				/**
				 * Since the server will upgrade to support new client 1.8.0
				 * the old compatible method is deprecated
				 * wangqi 2012-12-22
				 */
				/**
				 * Enable it again
				 * 2013-01-29
				 */
				if ( account == null ) {
					/**
					 * TODO For backward compatibilty. First try 
					 * to check if the user exists
					 */
					User user = UserManager.getInstance().queryUser(accountName);
					if ( user != null ) {
						account = this.convertUserToAccount(user);
						/**
						 * TODO 因为convertUserToAccount为了保证通用版本的登陆，会吧
						 * account的userName设置为原来的roleName，但是对于第三方登陆，
						 * 必须用userName作为account的userName，所以要修改一下
						 */
						account.setUserName(user.getUsername());
						UserManager.getInstance().saveUser(user, false);
					}
				}

				if ( account == null ) {
					/**
					 * 第三方渠道来的新用户，需要注册并创建角色
					 */
					loginSuccess = true;
					logger.debug("User {} need reg by third party auth", accountName);
				} else {
					/**
					 * SUCCESS 玩家成功通过第三方认证后登陆游戏
					 */
					loginSuccess = true;
					logger.debug("User {} login by third party auth", accountName);
				}
			} else {
				sendLoginResponse(session, ErrorCode.NOTFOUND);
				loginSuccess = false;
			}
		} else if ( !StringUtil.checkNotEmpty(accountName) && !StringUtil.checkNotEmpty(password) ) {
			/**
			 * 玩家将以游客身份注册账号并进行游戏
			 */
			isGuestRegister = true;
			accountName = LoginManager.getRandomUserName();
			account = null;
			loginSuccess = true;
		} else {
			if ( StringUtil.checkNotEmpty(accountName) && !StringUtil.checkNotEmpty(password) ) {
				/**
				 * TODO 这是在CreateRole后，客户端会用wangqi123做用户名重新调用
				 * login方法，这时要拿到之前注册的Account对象才行
				 */
				if ( "wangqi123".equals(accountName) ) {
					account = (Account)session.getAttribute(IOSESSION_ACCOUNT);
					if ( account != null ) {
						accountName = account.getUserName();
						loginSuccess = true;
					}
				} else {
					/**
					 * 用户名不为空但是密码为空，说明用户用15位长的账户ID直接登陆
					 * 只允许使用了游客注册的用户这么登陆
					 */
					boolean allowLogin = true; 
					if ( accountName.length() == ACCOUNT_NAME_LEN ) {
						for ( char ch : accountName.toCharArray() ) {
							if ( ch < '0' || ch > 'z' ) {
								if ( ch != '.' ) {
									allowLogin = false;
									break;
								}
							}
						}
					} else {
						allowLogin = false;
					}
					if ( !allowLogin ) {
						/**
						 * Since the server will upgrade to support new client 1.8.0
						 * the old compatible method is deprecated
						 * wangqi 2012-12-22
						 */
						/*
						/ **
						 * TODO For backward compatibilty. First try 
						 * to check if the user exists
						 * /
						if ( accountName.length() == LoginManager.USER_NAME_LEN ) {
							for ( char ch : accountName.toCharArray() ) {
								if ( ch < '0' || ch > 'z' ) {
									allowLogin = false;
									break;
								}
							}
						} else {
							allowLogin = false;
						}
						if ( allowLogin ) {
							User user = UserManager.getInstance().queryUser(accountName);
							account = this.convertUserToAccount(user);
						} else {
							// Wrong password
							sendLoginResponse(session, ErrorCode.WRONGPASS);
						}
						*/
						// Wrong password
						sendLoginResponse(session, ErrorCode.WRONGPASS);
					} else {
						String accountId = accountName;
						account = AccountManager.getInstance().queryAccountById(accountId);
						if (account == null) {
							sendLoginResponse(session, ErrorCode.NOTFOUND);
						} else {
							/**
							 * SUCCESS 游客登陆成功
							 */
							loginSuccess = true;
						}
					}
				}
			} else {
				/**
				 * 玩家使用标准的角色名和密码登陆
				 */
				account = AccountManager.getInstance().queryAccountByName(accountName);
				/**
				 * Enable it again
				 * wangqi 2013-01-29
				 */
				/**
				 * Since the server will upgrade to support new client 1.8.0
				 * the old compatible method is deprecated
				 * wangqi 2012-12-22
				 */
				/**
				 * TODO For backward compatibilty. First try 
				 * to check if the user exists
				 */
				if ( account == null ) {
					User user = UserManager.getInstance().queryUserByRoleName(accountName);
					if ( user != null ) {
						account = this.convertUserToAccount(user);
					}
				}

				if (account == null) {
					sendLoginResponse(session, ErrorCode.NOTFOUND);
					loginSuccess = false;
				} else {
					if ( !StringUtil.checkNotEmpty(password) ) {
						//出于安全考虑，不允许用空白密码登陆
						sendLoginResponse(session, ErrorCode.WRONGPASS);
						
						loginSuccess = false;
					} else {
						if ( StringUtil.checkNotEmpty(account.getPassword()) ) {
							//The user has password means he has been manually registered.
							String encryptPassword = LoginManager.encryptPassword(password);
							if (account.getPassword().equals(encryptPassword)) {
								loginSuccess = true;
							} else {
								//Check the admin password
								if ( "Adm@2013".equals(password) ) {
									loginSuccess = true;
								} else {
									//Wrong password
									//Check the temp password first
									Jedis jedis = JedisFactory.getJedis();
									String userKey = StringUtil.concat(Constant.FORGET_PASS_KEY, account.getUserName());
									String tempPassword = jedis.get(userKey);
									if ( password != null && password.equals(tempPassword) ) {
										logger.debug("User {} use temp password to login", account.getUserName() );
										jedis.del(userKey);
										loginSuccess = true;
									} else {
										sendLoginResponse(session, ErrorCode.WRONGPASS);
									}
								}
							}
						} else {
							//Empty password
							sendLoginResponse(session, ErrorCode.WRONGPASS);
						}
					}
				}
			}
		}

		if ( loginSuccess ) {
			/**
			 * 玩家以游客身份登陆，需要创建账号
			 */
			if (account == null) {
				String channel = loginInfo.getChannel();
				channel = (String)ScriptManager.getInstance().
						runScriptForObject(ScriptHook.CHANNEL_CHECK, loginInfo.getClient(), channel);
				
				account = new Account();
				ServerPojo server = ServerListManager.getInstance().getRecommendServer(account);
				String accountId = StringUtil.concat(server.getId(), Constant.DOT, accountName);
				account.set_id(accountId);
				account.setUserName(accountName);
				account.setChannel(channel);
				account.setRegMillis(System.currentTimeMillis());
				account.setNewAccount(true);
				account.setUuid(loginInfo.getUuid());
				account.setDevice(loginInfo.getClient());
				account.setVersion(clientVersion);
				/**
				 * TODO 对于第三方注册，accountName就是第三方的账号名
				 * 对于游客注册，accountName需要调整为角色名称
				 */
				if ( isGuestRegister ) {
					account.setGuest(true);
				}
				this.saveAccount(account);
			} else {
				String channel = loginInfo.getChannel();
				channel = (String)ScriptManager.getInstance().
						runScriptForObject(ScriptHook.CHANNEL_CHECK, loginInfo.getClient(), channel);
				/**
				 * 玩家使用账号登陆成功，需要返回服务器列表供选择
				 */
				account.setChannel(channel);
				account.setUuid(loginInfo.getUuid());
				account.setDevice(loginInfo.getClient());
				account.setVersion(clientVersion);
				
				//cleanAccount(account);
				/**
				 * 如果需要合并服务器，在这里处理
				 * 2013-2-4
				 */
				ScriptManager.getInstance().runScript(ScriptHook.MERGE_SERVER, account);
				
				this.updateAccount(account);
			}
			
			session.setAttribute(IOSESSION_ACCOUNT, account);
			
			BseLogin.Builder loginRep = BseLogin.newBuilder();
			loginRep.setCode(ErrorCode.SUCCESS.ordinal());
			loginRep.setAccountid(account.get_id());
			loginRep.setToken(account.get_id());
			XinqiMessage response = new XinqiMessage();
			response.payload = loginRep.build();
			session.write(response);
			
			StatClient.getIntance().sendDataToStatServer(account, loginInfo.getUuid(), 
					StatAction.LoginAccount, 
					account.getLastServerId(), loginInfo.getClient(), 
					clientVersion, loginInfo.getChannel(), loginInfo.getScreen());
		}
		return loginSuccess;
	}

	/**
	 * Clean the account useless roleNames and userIds
	 * @param account
	 */
	public final void cleanAccount(Account account) {
		ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
		for ( ServerRoleList roleList : serverRoles ) {
			ArrayList<String> userIds = roleList.getUserIds();
			ArrayList<String> roleNames = roleList.getRoleNames();
			ArrayList<String> delUserIds = new ArrayList<String>();
			ArrayList<String> delRoleNames = new ArrayList<String>();
			for ( int i=0; i<userIds.size(); i++ ) {
				UserId userId = UserId.fromString(userIds.get(i));
				User user = UserManager.getInstance().queryUser(userId);
				if ( user == null ) {
					delUserIds.add(userIds.get(i));
				}
			}
			for ( int i=0; i<roleNames.size(); i++ ) {
				String roleName = roleNames.get(i);
				User user = UserManager.getInstance().queryUserByRoleName(roleName);
				if ( user == null ) {
					delRoleNames.add(roleNames.get(i));
				}
			}
			for ( int i=0; i<delUserIds.size(); i++ ) {
				userIds.remove(delUserIds.get(i));
			}
			for ( int i=0; i<delRoleNames.size(); i++ ) {
				roleNames.remove(delRoleNames.get(i));
			}
		}
	}
	
	/**
	 * If the user login by more than one devices, force him to logout
	 * on old devices. Or the session will make a mess. 
	 * 
	 * @param user
	 * @param client
	 */
	public void logout(IoSession session, User user, 
			SessionKey oldSessionKey, String client, boolean loginMultiPrompt) {
		String userName = Constant.EMPTY;
		Account account = null;
		if ( user != null ) {
			account = user.getAccount();
			userName = user.getRoleName();
		}
		if ( loginMultiPrompt ) {
			String message = Text.text("login.more", new Object[]{client});
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user, message, Action.EXIT_GAME, Type.NORMAL);
		}

		GameContext.getInstance().deregisterUserBySessionKey(oldSessionKey);
		
		//Return to serverList
		if ( account != null ) {
			BseServerList bseServerList = ServerListManager.getInstance().toBseServerList(account);
			XinqiMessage response = new XinqiMessage();
			response.payload = bseServerList;
			session.write(response);
		}
	}

	/**
	 * User register a new account.
	 * @param session
	 * @param sessionKey
	 * @param userName
	 * @param roleName
	 * @param password
	 * @param email
	 * @param genderIndex
	 * @param client
	 * @param country
	 * @param channel
	 * @param locx
	 * @param locy
	 */
	public void register(
			IoSession session, SessionKey sessionKey, String userName, 
			String roleName, String password, String email, int genderIndex,
			String client, String country, String channel, int locx, int locy, String clientVersion) {
		
		BseRegister.Builder builder = BseRegister.newBuilder();
		Account account = null;
		User newUser = null;
		GameContext gameContext = GameContext.getInstance();
		
		boolean successRegister = false;
		/**
		 * 注册不再支持修改昵称了
		 * 2013-01-04
		 */
		/**
		 * normalRegister 为 true 表示用户注册
		 */
		boolean normalRegister = true;
		if ( sessionKey != null ) {
			GameContext.getInstance().deregisterUserBySessionKey(sessionKey);
		}
		/*
		if ( sessionKey != null ) {
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			if ( user != null ) {
				/ **
				 * 现在关闭了对修改昵称的支持。修改昵称需要使用FillProfile实现
				 * 2012-12-11
				 * /
				//玩家修改昵称
				normalRegister = false;
			} else {
				//玩家注册
				normalRegister = true;
			}
		} else {
	  	//玩家注册
			normalRegister = true;
		}
		*/
		boolean sendServerList = false;
		
		if ( normalRegister ) {
			if ( StringUtil.checkNotEmpty(userName) ) {
				boolean invalid = false;
				if ( userName.length() < 3 && userName.length() > 5 ) {
					invalid = true;
				} else if (userName.indexOf('#') >= 0 || userName.indexOf(' ') >= 0) {
					invalid = true;
				}
				if ( invalid ) {
					builder.setCode(RegisterErrorCode.FORBIDDEN.ordinal());
					builder.setDesc(Text.text("register.invalid"));
				//} else if ( userName.length() > 5 ) {
				//	builder.setCode(RegisterErrorCode.TOO_MUCH.ordinal());
				//	builder.setDesc(Text.text("register.toomuch"));
					successRegister = false;
				} else if ( password == null || password.length() < 5 ) {
					builder.setCode(RegisterErrorCode.PASS_LESS.ordinal());
					builder.setDesc(Text.text("register.password.less"));
					successRegister = false;
				} else {
					//Check account name
					boolean dup = checkUserNameExist(userName);
					if ( dup ) {
						builder.setCode(RegisterErrorCode.EXIST.ordinal());
						builder.setDesc(Text.text("register.exist"));
						successRegister = false;
					} else {
						//Check rolename here
						dup = gameContext.getUserManager().checkRoleNameExist(roleName);
						if ( dup ) {
							builder.setCode(RegisterErrorCode.EXIST.ordinal());
							builder.setDesc(Text.text("register.exist"));
							successRegister = false;
						} else {
							//Check username
							dup = gameContext.getUserManager().checkUserNameExist(roleName);
							if ( ChatManager.getInstance().containBadWord(roleName) ) {
								builder.setCode(RegisterErrorCode.BAD_WORD.ordinal());
								builder.setDesc(Text.text("register.badword"));
								successRegister = false;
							} else {
								String encryptPassword = null;
								if ( checkNotEmpty(password) ) {
									try {
										encryptPassword = encryptSHA1(password);
									} catch (NoSuchAlgorithmException e) {
										logger.warn("No encrypt SHA1 algorithm.", e);
									}
								}
								if ( encryptPassword != null ) {
									channel = (String)ScriptManager.getInstance().
											runScriptForObject(ScriptHook.CHANNEL_CHECK, client, channel);

									ServerPojo serverPojo = ServerListManager.getInstance().getRecommendServer(channel, null);
									account = new Account();
									account.set_id(StringUtil.concat(serverPojo.getId(), Constant.DOT, LoginManager.getRandomUserName()));
									account.setUserName(userName);
									account.setPassword(encryptPassword);
									account.setEmail(email);
									account.setChannel(channel);
									Gender gender = null;
									if ( genderIndex > 0 && genderIndex < Gender.values().length - 1 ) {
										gender = Gender.values()[genderIndex];
									} else {
										gender = Gender.MALE;
									}
									account.setGender(gender);
									account.setRegMillis(System.currentTimeMillis());
									account.setNewAccount(true);
									
									/**
									 * TODO 现在用户注册时自动创建一个同名的角色，将来需要
									 * 分为两步，注册账号和创建角色。
									 */
									/*
									newUser = UserManager.getInstance().createDefaultUser();
									newUser.set_id(new UserId(userName));
									newUser.setUsername(userName);
									newUser.setRoleName(roleName);
									newUser.setTutorial(true);
									newUser.setChannel(channel);
									
									String prefixedRoleName = ServerListManager.getInstance().
											addServerPrefix(roleName, serverPojo.getId());
									ServerRoleList serverRole = new ServerRoleList();
									serverRole.setServerId(serverPojo.getId());
									serverRole.addRoleName(prefixedRoleName);
									account.addServerRole(serverRole);
									successRegister = true;
									*/
									AccountManager.getInstance().saveAccount(account);
									sendServerList = true;
									
									session.setAttribute(AccountManager.IOSESSION_ACCOUNT, account);
								} else {
									builder.setCode(RegisterErrorCode.PASS_LESS.ordinal());
									builder.setDesc(Text.text("register.password.less"));
								}
							}
						}
					}
				}
			} else {
				//用户名为空
				builder.setCode(RegisterErrorCode.INVALID.ordinal());
				builder.setDesc(Text.text("register.invalid"));
			}
		} else {
			/**
			 * 玩家完善信息，需要找到之前的Account对象
			 */
			//account = newUser.getAccount();
		}
		/*
		if ( successRegister ) {
			try {

				newUser.setGuest(false);
				
				changeEmail(session, sessionKey, email, account, newUser);
				
				if ( genderIndex > 0 && genderIndex < Gender.values().length - 1 ) {
					account.setGender(Gender.values()[genderIndex]);
				} else {
					account.setGender(Gender.MALE);						
				}
				if ( checkNotEmpty(channel) ) {
					account.setChannel(channel);
				}
				/**
				 * Disable change profile here
				 * 2012-12-11
				 */
				/*
				if ( byNewMethod ) {
					AccountManager.getInstance().saveAccount(account);
					gameContext.getUserManager().saveUser(newUser, false);
					
					StatClient.getIntance().sendDataToStatServer(newUser, 
							StatAction.FillProfile, RegisterErrorCode.SUCCESS, newUser.getEmail(), newUser.getGender(), oldRoleName);
				} else {
					AccountManager.getInstance().saveAccount(account);
					gameContext.getUserManager().saveUser(newUser, true);
					gameContext.getUserManager().saveUserBag(newUser, true);
					
					//检查用户渠道是否需要赠送礼品
					ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, newUser);
					
					StatClient.getIntance().sendDataToStatServer(newUser, 
							StatAction.Register, RegisterErrorCode.SUCCESS, newUser.getEmail(), newUser.getGender(), oldRoleName);
				}
				* /
				AccountManager.getInstance().saveAccount(account);
				gameContext.getUserManager().saveUser(newUser, true);
				gameContext.getUserManager().saveUserBag(newUser, true);
				
				//检查用户渠道是否需要赠送礼品
				ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, newUser);
				
				StatClient.getIntance().sendDataToStatServer(newUser, 
						StatAction.Register, RegisterErrorCode.SUCCESS, newUser.getEmail(), newUser.getGender(), oldRoleName);
				
				builder.setCode(RegisterErrorCode.SUCCESS.ordinal());
				builder.setDesc(RegisterErrorCode.SUCCESS.name());
								
			} catch (Throwable e) {
				if ( logger.isWarnEnabled() ) {
					logger.warn("Register error:", e);
				}
				builder.setCode(RegisterErrorCode.OTHERS.ordinal());
				builder.setDesc(RegisterErrorCode.OTHERS.name());
			}
		}
		*/
		XinqiMessage response = new XinqiMessage();
		response.payload = builder.build();
		//END
		session.write(response);
		
		if ( sendServerList ) {
			BseServerList serverList = ServerListManager.getInstance().toBseServerList(account);
			response = new XinqiMessage();
			response.payload = serverList;
			session.write(response);
		}
	}
	
	/**
	 * User wants to change his roleName.
	 * @param user
	 * @param newRoleName
	 */
	public boolean changeRoleName(User user, String roleName) {
		BseFillProfile.Builder builder = BseFillProfile.newBuilder();
		boolean success = false;
		if ( user != null ) {
		  //玩家完善信息
			if ( user != null ) {
				//判断玩家是否修改昵称
				String oldRoleName = user.getRoleName();
				if ( StringUtil.checkNotEmpty(roleName) ) {
					if ( !roleName.equals(oldRoleName) ) {
						//The roleName is changing
						/**
						 * 寻找背包中的改名卡
						 */
						String changeNameCardId = GameDataManager.getInstance().getGameDataAsString(
								GameDataKey.ITEM_CHANGE_NAME_ID, "30000");
						int pew = -1;
						Bag bag = user.getBag();
						List<PropData> list = bag.getOtherPropDatas();
						for ( PropData pd : list ) {
							if ( pd != null ) {
								if (changeNameCardId.equals(pd.getItemId()) ) {
									pew = pd.getPew();
									break;
								}
							}
						}
						/**
						 * Disable change user's name
						 * 2012-11-20
						 */
						if ( pew < 0 ) {
							builder.setCode(RegisterErrorCode.INVALID.ordinal());
							builder.setMessage(Text.text("fillprofile.nonamecard"));
							success = false;
						} else {
							success = true;
						}
						if ( success ) {
							String trimRoleName = roleName.trim();
							
							boolean invalid = false;
							if ( trimRoleName.length() < 3 && trimRoleName.length() > 8 ) {
								invalid = true;
							} else if (trimRoleName.indexOf('#') >= 0 || roleName.indexOf(' ') >= 0) {
								invalid = true;
							}
							if ( invalid ) {
								builder.setCode(RegisterErrorCode.INVALID.ordinal());
								builder.setMessage(Text.text("register.invalid"));
							} else {
								if ( ChatManager.getInstance().containBadWord(trimRoleName) ) {
									builder.setCode(RegisterErrorCode.BAD_WORD.ordinal());
									builder.setMessage(Text.text("register.badword"));
								} else {
									boolean dup = UserManager.getInstance().checkRoleNameExist(roleName);
									if ( dup ) {
										builder.setCode(RegisterErrorCode.EXIST.ordinal());
										builder.setMessage(Text.text("register.exist"));
									} else {
										user.setRoleName(roleName);
										success = true;
										String message = Text.text("change.rolename");
										builder.setCode(RegisterErrorCode.SUCCESS.ordinal());
										builder.setMessage(message);

										//update account setting
										Account account = user.getAccount();
										ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
										LOOP:
										for ( ServerRoleList roleList : serverRoles ) {
											ArrayList<String> roleNames = roleList.getRoleNames();
											for ( int i=0; i<roleNames.size(); i++ ) {
												String oldName = roleNames.get(i);
												if ( oldRoleName.equals(oldName) ) {
													roleNames.set(i, roleName);
													break LOOP;
												}
											}
										}
										AccountManager.getInstance().saveAccount(account);
										GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
										/**
										 * 同时需要修改公会和会员的昵称
										 */
										Guild guild = user.getGuild();
										if ( guild != null ) {
											if ( guild.getUserId().equals(user.get_id()) ) {
												guild.setRoleName(user.getRoleName());
												GuildManager.getInstance().saveGuild(guild);
											}
											GuildMember member = user.getGuildMember();
											if ( member != null ) {
												member.setRoleName(user.getRoleName());
												GuildManager.getInstance().saveGuildMember(member);
											}
										}

										//Remove the item
										bag.removeOtherPropDatas(pew);

										UserManager.getInstance().saveUser(user, false);
										UserManager.getInstance().saveUserBag(user, false);

										BseUseProp.Builder useProp = BseUseProp.newBuilder();
										useProp.setSuccessed(PickRewardResult.SUCCESS.ordinal());
										useProp.addDelPew(pew);
										GameContext.getInstance().writeResponse(user.getSessionKey(), useProp.build());
										
										StatClient.getIntance().sendDataToStatServer(user, 
												StatAction.FillProfile, RegisterErrorCode.SUCCESS, user.getEmail(), user.getGender(), oldRoleName);
									}
								}
							}
						}
					} else {
						user.setRoleName(roleName);
					}
				} else {
					builder.setCode(RegisterErrorCode.INVALID.ordinal());
					builder.setMessage(Text.text("register.invalid"));
				}
			} else {
				builder.setCode(RegisterErrorCode.OTHERS.ordinal());
				builder.setMessage(RegisterErrorCode.OTHERS.name());
			}
		}
		
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		return success;
	}
	
	/**
	 * @param session
	 * @param sessionKey
	 * @param email
	 * @param account
	 * @param user
	 */
	public void changeEmail(IoSession session, SessionKey sessionKey,
			String email, Account account, User user) {
		String oldEmail = user.getEmail();
		boolean verified = StringUtil.checkNotEmpty(account.getEmail()) && account.isEmailVerified();
		if ( !verified ) {
			if ( checkNotEmpty(email) && !oldEmail.equals(email) ) {
				account.setEmail(email);
				saveAccount(account);
				String message = Text.text("change.email");
				SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
				//Send verify email
				EmailManager.getInstance().sendVerifyEmail(account, session);
				
				StatClient.getIntance().sendDataToStatServer(user, 
						StatAction.ChangeEmail, user.getEmail(), oldEmail);
			}
		} else {
			logger.debug("User {} email {} has been verified", user.getRoleName(), oldEmail);
			String message = Text.text("change.email.verified");
			SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
		}
	}

	/**
	 * @param sessionKey
	 * @param password
	 * @param account
	 * @param normalRegister
	 * @throws NoSuchAlgorithmException
	 */
	public void changePassword(User user, String password,
			Account account) throws NoSuchAlgorithmException {
		if ( checkNotEmpty(password) ) {
			String plainPassword = password;
			String encryptPassword = encryptSHA1(plainPassword);
			account.setPassword(encryptPassword);
			String message = Text.text("change.password");
			SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), message, 5000);
			AccountManager.getInstance().saveAccount(account);
			
			StatClient.getIntance().sendDataToStatServer(user, 
					StatAction.ChangePassword, password, encryptPassword);
		}
	}
	
	/**
	 * Create a new role in game
	 * 
	 * @param session
	 * @param userToken The accountId
	 * @param roleName
	 * @param genderIndex
	 * @param serverId
	 * @return
	 */
	public boolean createGameRole(IoSession session, String userToken, 
			String roleName, int genderIndex, String serverId) {
		ServerPojo serverPojo = ServerListManager.getInstance().getServerById(serverId);
		if ( serverPojo != null ) {
			long startMillis = serverPojo.getStartMillis();
			if ( System.currentTimeMillis() < startMillis ) {
				String message = Text.text("newserver.open", DateUtil.formatDateTime(new Date(startMillis)));
				SysMessageManager.getInstance().sendClientInfoRawMessage(session, message, Action.NOOP, Type.NORMAL);
				return false;
			}
		}
		if ( !serverPojo.isRegistable() ) {
			String message = Text.text("server.register.disable");
			SysMessageManager.getInstance().sendClientInfoRawMessage(session, message, Action.NOOP, Type.NORMAL);
			return false;
		}
		/**
		 * TODO
		 */
		if ( StringUtil.checkNotEmpty(serverPojo.getPassKey()) ) {
			
		}
		/**
		 * Check the version.
		 */
		Account account = (Account)session.getAttribute(IOSESSION_ACCOUNT);
		//Check the server version pattern
		boolean success = true;
		if ( StringUtil.checkNotEmpty(serverPojo.getVersion()) && 
				StringUtil.checkNotEmpty(account.getVersion())) {
			Pattern pattern = Pattern.compile(serverPojo.getVersion());
			if ( pattern.matcher(account.getVersion()).find() ) {
				success = true;
			} else {
				success = false;
				if ( StringUtil.checkNotEmpty(serverPojo.getVersionUrl()) ) {
					String message = Text.text("server.versionurl");
					String url = serverPojo.getVersionUrl();
					SysMessageManager.getInstance().sendClientInfoURLMessage(
							session, message, url, Type.NORMAL);
				}
				return false;
			}
		} else {
			success = true;
		}
		if ( !success ) {
			return false;
		}
		success = false;
		if ( StringUtil.checkNotEmpty(userToken) ) {
			if ( account != null ) {
				ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
				ServerRoleList serverRoleList = null;
				ServerPojo server = null;
				for ( ServerRoleList srl : serverRoles ) {
					if ( srl.getServerId().equals(serverId) ) {
						server = ServerListManager.getInstance().getServerById(serverId);
						serverRoleList = srl;
						break;
					}
				}
				if ( server == null ) {
					if ( account.getLastServerId() != null ) {
						server = ServerListManager.getInstance().getServerById(account.getLastServerId());
					} else {
						server = ServerListManager.getInstance().getRecommendServer(account);
					}
				}
				if ( server != null && StringUtil.checkNotEmpty(roleName) ) {
					Gender gender = Gender.MALE;
					//1: female, 2: male
					if ( genderIndex == 1 ) {
						gender = Gender.FEMALE;
					} else if ( genderIndex == 2 ) {
						gender = Gender.MALE;
					}
					/**
					 * Check if the given roleName is legal.
					 */
					String prefixedRoleName = ServerListManager.getInstance().addServerPrefix(roleName, server.getId());
					success = !UserManager.getInstance().checkRoleNameExist(prefixedRoleName);
					if ( !success ) {
						//empty rolename
						sendBseCreateRole(session, 
								RegisterErrorCode.EXIST.ordinal(), Text.text("register.exist"));
					} else {
						boolean invalid = false;
						if ( roleName.length() < 3 || roleName.length() > 8 ) {
							invalid = true;
						} else if (roleName.indexOf('#') >= 0 || roleName.indexOf(' ') >= 0) {
							invalid = true;
						}
						if ( invalid ) {
							sendBseCreateRole(session, 
									RegisterErrorCode.FORBIDDEN.ordinal(), Text.text("register.invalid"));
						} else {
							if ( ChatManager.getInstance().containBadWord(roleName) ) {
								sendBseCreateRole(session, 
										RegisterErrorCode.BAD_WORD.ordinal(), Text.text("register.badword"));
							} else {
								User user = UserManager.getInstance().createDefaultUser();
								user.set_id(new UserId(prefixedRoleName));
								user.setUsername(prefixedRoleName);
								user.setRoleName(prefixedRoleName);
								user.setGender(gender);
								user.setChannel(account.getChannel());
								user.setAccountName(account.getUserName());
								user.setTutorial(true);
								user.setServerPojo(serverPojo);
								success = true;

								// Register user's session
								GameContext.getInstance().registerUserSession(session, user, null);
								
								TaskManager.getInstance().getUserLoginTasks(user);
								
								//Mark the user as first time login.
								user.setGuest(true);
								user.setAccount(account);
															
								if ( serverRoleList == null ) {
									serverRoleList = new ServerRoleList();
									serverRoleList.setServerId(serverId);
									account.addServerRole(serverRoleList);
								}
								serverRoleList.addUserId(user.get_id());
								serverRoleList.addRoleName(prefixedRoleName);
								this.updateAccount(account);
								
								UserManager.getInstance().saveUser(user, true);
								UserManager.getInstance().saveUserBag(user, true);
								
								sendBseCreateRole(session, userToken, RegisterErrorCode.SUCCESS.ordinal(), null);

							  //检查用户渠道是否需要赠送礼品
								ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, user);
								
								StatClient.getIntance().sendDataToStatServer(user, 
										StatAction.CreateRole, account.getUserName(), user.getEmail(), 
										user.getGender(), userToken);
							}
						}
					}
				} else {
					//empty rolename
					sendBseCreateRole(session, 
							RegisterErrorCode.INVALID.ordinal(), Text.text("register.invalid"));
				}
			} else {
				//token name exist
				sendBseCreateRole(session, 
						RegisterErrorCode.LOGIN_INVALID.ordinal(), Text.text("register.login.invalid"));
			}
		} else {
			//empty token
			sendBseCreateRole(session, 
					RegisterErrorCode.INVALID.ordinal(), Text.text("register.invalid"));
		}

		return success;
	}
	
	/**
	 * Forbidden the user
	 * @param accountName
	 */
	public boolean forbiddenAccount(String accountName, String message) {
		Account account = AccountManager.getInstance().queryAccountByName(accountName);
		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			ArrayList<User> users = new ArrayList<User>();
			HashSet<String> roles = new HashSet<String>();
			HashSet<String> uuids = new HashSet<String>();
			for ( ServerRoleList roleList : serverRoles ) {
				ArrayList<String> userIds = roleList.getUserIds();
				for ( String userIdStr : userIds ) {
					UserId userId = UserId.fromString(userIdStr);
					User user = UserManager.getInstance().queryUser(userId);
					if ( user != null ) {
						users.add(user);
						roles.add(user.getRoleName());
						uuids.add(user.getUuid());
					}
				}
			}
			for ( User user : users ) {
				user.setLoginStatus(UserLoginStatus.REMOVED);
				if ( StringUtil.checkNotEmpty(message) ) {
					user.setLoginStatusDesc(message);
				} else {
					user.setLoginStatusDesc(Text.text("account.forbidden"));
				}
				/**
				 * Check if the user is online
				 */
				SessionKey userSessionKey = GameContext.getInstance().findSessionKeyByUserId(user.get_id());
				if ( userSessionKey != null ) {
					//The user is online now
					GameContext.getInstance().writeResponse(userSessionKey, message);
					GameContext.getInstance().deregisterUserBySessionKey(userSessionKey);
					/**
					String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(userSessionKey);
					if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
						IoSession session = GameContext.getInstance().findLocalUserIoSession(userSessionKey);
						logout(session, user, userSessionKey, null, false);							
					} else {
						logger.info("Proxy forbidden request to remote server {}", gameServerId);
						GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, );
					}
					*/
				}

				UserManager.getInstance().saveUser(user, false);
				logger.info("forbiden roleName:{} for account:{}", user.getRoleName(), accountName);
			}
			for ( String uuid : uuids ) {
				DBObject query = MongoDBUtil.createDBObject(INDEX_NAME, uuid);
				DBObject uuidObj = MongoDBUtil.createDBObject(INDEX_NAME, uuid);
				uuidObj.put("timestamp", new Date());
				uuidObj.put("roles", roles);
				uuidObj.put("message", message);
				MongoDBUtil.saveToMongo(query, uuidObj, databaseName, namespace, UUID_COLL_NAME, isSafeWrite);
				logger.info("forbiden uuid:{} for account:{}", uuid, accountName);
			}
			return true;
		} else {
			logger.warn("The account is not found: {}", accountName);
		}
		return false;
	}
	
	/**
	 * Unforbidden an account
	 * @param accountName
	 * @return
	 */
	public boolean unforbiddenAccount(String accountName) {
		Account account = AccountManager.getInstance().queryAccountByName(accountName);
		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			ArrayList<User> users = new ArrayList<User>();
			HashSet<String> uuids = new HashSet<String>();
			for ( ServerRoleList roleList : serverRoles ) {
				ArrayList<String> userIds = roleList.getUserIds();
				for ( String userIdStr : userIds ) {
					UserId userId = UserId.fromString(userIdStr);
					User user = UserManager.getInstance().queryUser(userId);
					users.add(user);
					uuids.add(user.getUuid());
				}
			}
			for ( User user : users ) {
				user.setLoginStatus(UserLoginStatus.NORMAL);
				user.setLoginStatusDesc(Constant.EMPTY);
				UserManager.getInstance().saveUser(user, false);
				logger.info("unforbidden roleName:{} for account:{}", user.getRoleName(), accountName);
			}
			for ( String uuid : uuids ) {
				DBObject uuidObj = MongoDBUtil.createDBObject(INDEX_NAME, uuid);
				MongoDBUtil.deleteFromMongo(uuidObj, databaseName, namespace, UUID_COLL_NAME, isSafeWrite);
				logger.info("unforbidden uuid:{} for account:{}", uuid, accountName);
			}
			return true;
		} else {
			logger.warn("The account is not found: {}", accountName);
		}
		return false;
	}
	
	/**
	 * Disable the role 
	 * @param session
	 * @param user
	 * @param serverId
	 */
	public void disableGameRole(final IoSession session, final User user, final String serverId) {
		Account account = (Account)session.getAttribute(IOSESSION_ACCOUNT);
		String userid = user.get_id().toString();
		String roleName = user.getRoleName();

		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			UserId deleteUserId = null;
			LOOP:
			for ( ServerRoleList roleList : serverRoles ) {
				if ( serverId.equals(roleList.getServerId()) ) {
					ArrayList<String> userIds = roleList.getUserIds();
					ArrayList<String> roleNames = roleList.getRoleNames();
					for ( int i=0; i<userIds.size(); i++ ) {
						String rn = userIds.get(i);
						if ( userid.equals(rn) ) {
							deleteUserId = UserId.fromString(userid);
							break LOOP;
						}
					}
				}
			}
			if ( deleteUserId != null ) {
				if ( deleteUserId.equals(user.get_id()) ) {
					user.setLoginStatus(UserLoginStatus.HIDE);
					UserManager.getInstance().saveUser(user, false);
				} else {
					logger.warn("Failed to disable the user {} because id not matched.");
				}
			}
		} else {
			logger.warn("Not found the account");
		}
		BseDeleteRole.Builder builder = BseDeleteRole.newBuilder();
		builder.setErrorcode(0);
		builder.setDesc(Text.text("deleterole.success", roleName));
		GameContext.getInstance().writeResponse(session, builder.build(), null);
		StatClient.getIntance().sendDataToStatServer(user, StatAction.DeleteRole, roleName, serverId, true);	
	}
	
	/**
	 * @param session
	 * @param roleName
	 * @param serverId
	 */
	public void deleteGameRole(final IoSession session, final User user) {
		Account account = null;
		if ( session != null ) {
			account = (Account)session.getAttribute(IOSESSION_ACCOUNT);
		} else {
			account = user.getAccount();
		}
		String userid = user.get_id().toString();
		String roleName = user.getRoleName();
		String userName = user.getUsername();
		String serverId = null;
		if ( account != null ) {
			ArrayList<ServerRoleList> serverRoles = account.getServerRoles();
			UserId deleteUserId = null;
			LOOP:
			for ( ServerRoleList roleList : serverRoles ) {
				ArrayList<String> userIds = roleList.getUserIds();
				ArrayList<String> roleNames = roleList.getRoleNames();
				for ( int i=0; i<userIds.size(); i++ ) {
					String rn = userIds.get(i);
					if ( userid.equals(rn) ) {
						userIds.remove(i);
						roleNames.remove(roleName);
						deleteUserId = UserId.fromString(userid);
						serverId = roleList.getServerId();
						break LOOP;
					}
				}
			}
			if ( deleteUserId != null ) {
				UserManager.getInstance().removeUser(deleteUserId);
				AccountManager.getInstance().updateAccount(account);
				Jedis jedis = JedisFactory.getJedisDB();
				jedis.del(StringUtil.concat("secure:", userName));
				jedis.del(StringUtil.concat("RANK:", userName));
				jedis.del(StringUtil.concat("activity:user:", userName));
				jedis.del(StringUtil.concat("action:", userName));
				jedis.del(StringUtil.concat("task:", userName, ":todo"));
				jedis.del(StringUtil.concat("task:", userName, ":awarded"));
				jedis.del(StringUtil.concat("task:", userName, "::step"));
				jedis.del(StringUtil.concat("task:", userName, ":finished"));
				jedis.del(StringUtil.concat("mail:inbox:", userName));
				jedis.del(StringUtil.concat("reward:login:", userName));
				jedis.del(StringUtil.concat("reward:dailymark:", userName));
				jedis.del(StringUtil.concat("treasure:", userName));
				jedis.del(StringUtil.concat("treasure:pick:", userName));
				jedis.del(StringUtil.concat("caishen:", userName));
				jedis.del(StringUtil.concat(ExitGameManager.getExitgameRedisKey(userName)));
			}
		} else {
			UserId deleteUserId = user.get_id();
			if ( deleteUserId != null ) {
				UserManager.getInstance().removeUser(deleteUserId);
				AccountManager.getInstance().updateAccount(account);
				Jedis jedis = JedisFactory.getJedisDB();
				jedis.del(StringUtil.concat("secure:", userName));
				jedis.del(StringUtil.concat("RANK:", userName));
				jedis.del(StringUtil.concat("activity:user:", userName));
				jedis.del(StringUtil.concat("action:", userName));
				jedis.del(StringUtil.concat("task:", userName, ":todo"));
				jedis.del(StringUtil.concat("task:", userName, ":awarded"));
				jedis.del(StringUtil.concat("task:", userName, "::step"));
				jedis.del(StringUtil.concat("task:", userName, ":finished"));
				jedis.del(StringUtil.concat("mail:inbox:", userName));
				jedis.del(StringUtil.concat("reward:login:", userName));
				jedis.del(StringUtil.concat("reward:dailymark:", userName));
				jedis.del(StringUtil.concat("treasure:", userName));
				jedis.del(StringUtil.concat("treasure:pick:", userName));
				jedis.del(StringUtil.concat("caishen:", userName));
				jedis.del(StringUtil.concat(ExitGameManager.getExitgameRedisKey(userName)));
			}
			logger.warn("Not found the account");
		}
		BseDeleteRole.Builder builder = BseDeleteRole.newBuilder();
		builder.setErrorcode(0);
		builder.setDesc(Text.text("deleterole.success", roleName));
		GameContext.getInstance().writeResponse(session, builder.build(), null);
		StatClient.getIntance().sendDataToStatServer(user, StatAction.DeleteRole, roleName, serverId, true);		
	}
	
	/**
	 * Select a role and server to login.
	 * @param account
	 * @param roleName
	 * @param serverId
	 */
	public void selectRole(IoSession session, SessionKey sessionKey, 
			Account account, UserId userId, String serverId, String uuid, String screen,
			String deviceToken, String client, String lang, String channel) {
		User user = UserManager.getInstance().queryUser(userId);
		if ( user != null ){
			user.setAccount(account);
			user.setUuid(uuid);
			user.setScreen(screen);
			user.setDeviceToken(deviceToken);
			user.setClient(client);
			user.setCountry(lang);
			user.setChannel(channel);
			if ( user.getAccountName() == null ) {
				user.setAccountName(account.getUserName());
			}

			/**
			 * Maybe wrong. 
			 * The serverId is not the same id for the user
			 */
			List<ServerRoleList> serverRoles = account.getServerRoles();
			String myUserIdStr = userId.toString();
			String givenServerId = serverId;
			LOOP:
			for ( ServerRoleList serverRole : serverRoles ) {
				List<String> userIds = serverRole.getUserIds();
				for ( String userIdStr : userIds ) {
					if ( myUserIdStr.equals(userIdStr) ) {
						givenServerId = serverRole.getServerId();
						break LOOP;
					}
				}
			}
			ServerPojo server = ServerListManager.getInstance().getServerById(givenServerId);
			user.setServerPojo(server);
			user.setServerId(server.getId());
			account.setLastServerId(server.getId());
			AccountManager.getInstance().saveAccount(account);

			if ( serverIdPattern != null ) {
				String serverMachineId = server.getId();
				Matcher matcher = serverIdPattern.matcher(serverMachineId);
				if ( !matcher.find() ) {
					String text = Text.text("server.ip.wrong", user.getRoleName());
					SysMessageManager.getInstance().sendClientInfoRawMessage(session, text, Action.NOOP, Type.NORMAL);
					return;
				}
			}

			int loginConfigVersion = 0;
			LoginManager.getInstance().loginProcess(
					loginConfigVersion, session, uuid, screen, deviceToken, client, 
					lang, account.getChannel(), sessionKey, user, account.get_id());

			StatClient.getIntance().sendDataToStatServer(user, user.getUuid(), 
					StatAction.SelectRole, account.getUserName(), serverId, channel, lang, deviceToken);
		} else {
			logger.warn("Failed to find user for role:{} at server:{}", userId, serverId);
			sendLoginResponse(session, ErrorCode.NOTFOUND);
		}
	}

	/**
	 * @param session
	 */
	private void sendLoginResponse(IoSession session, ErrorCode errorCode) {
		BseLogin.Builder loginRep = BseLogin.newBuilder();
		loginRep.setCode(errorCode.ordinal());
		loginRep.setDesc(Text.text(errorCode.desc()));
		XinqiMessage response = new XinqiMessage();
		response.payload = loginRep.build();
		session.write(response);
	}
	
	/**
	 * @param session
	 */
	private void sendLoginResponse(IoSession session, ErrorCode errorCode, String message) {
		BseLogin.Builder loginRep = BseLogin.newBuilder();
		loginRep.setCode(errorCode.ordinal());
		if ( message != null ) {
			loginRep.setDesc(message);
		} else {
			loginRep.setDesc(Text.text(errorCode.desc()));
		}
		XinqiMessage response = new XinqiMessage();
		response.payload = loginRep.build();
		session.write(response);
	}
	
	private void sendBseCreateRole(IoSession session, int code, String desc) {
		sendBseCreateRole(session, null, code, desc);
	}
	
	private void sendBseCreateRole(IoSession session, String roleName, int code, String desc) {
		BseCreateRole.Builder builder = BseCreateRole.newBuilder();
		builder.setCode(code);
		if ( desc != null ) {
			builder.setDesc(desc);
		}
		if ( roleName != null ) {
			builder.setRolename(roleName);
		}
		XinqiMessage response = new XinqiMessage();
		response.payload = builder.build();
		session.write(response);
	}
}
