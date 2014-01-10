package com.xinqihd.sns.gameserver.db.mongo;

import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.User.Location;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.guild.Guild;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.guild.GuildMember;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogout.BceLogout;
import com.xinqihd.sns.gameserver.proto.XinqiBseConfigData.BseConfigData;
import com.xinqihd.sns.gameserver.proto.XinqiBseCreateRole.BseCreateRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseFriendList.BseFriendList;
import com.xinqihd.sns.gameserver.proto.XinqiBseFuncUnlock.BseFuncUnlock;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseLoginLottery.BseLoginLottery;
import com.xinqihd.sns.gameserver.proto.XinqiBseOnlineReward.BseOnlineReward;
import com.xinqihd.sns.gameserver.proto.XinqiBsePromotion.BsePromotion;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister.BseRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig.BseRoleConfig;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseTaskList;
import com.xinqihd.sns.gameserver.proto.XinqiBseUpdateOnlineStatus.BseUpdateOnlineStatus;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.proto.XinqiFriendInfoLite.FriendInfoLite;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.LocaleThreadLocal;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Manager user login action
 * @author wangqi
 *
 */
public class LoginManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);
	
	private static LoginManager instance = new LoginManager();
	
	private static final String COLL_NAME = "logins";
	
	private static final String INDEX_NAME = "_id";
	
	private static final char[] CHARS = new char[]{
		'0', '1','2','3','4','5','6','7','8','9',
		'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
	};
	
	public static final int USER_NAME_LEN = 10; 
	public static final int ROLE_NAME_LEN = 5; 
	
	//The major version of client
	private static final String KEY_MAJOR_VERSION = "majorv";
	//The minor version of client
	private static final String KEY_MINOR_VERSION = "minorv";
	//The config data version
	private static final String KEY_CONFIG_VERSION = "configversion";
	
	private int majorVersion = 0;
	
	private int minorVersion = 0;
	
	private int configVersion = 0;
	
	LoginManager() {
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
		DBObject loginDBObj = MongoDBUtil.queryFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		if ( loginDBObj != null ) {
			Integer majorIntObj = (Integer)loginDBObj.get(KEY_MAJOR_VERSION);
			if ( majorIntObj != null ) {
				majorVersion = majorIntObj.intValue();
			}
			Integer minorIntObj = (Integer)loginDBObj.get(KEY_MINOR_VERSION);
			if ( minorIntObj != null ) {
				minorVersion = minorIntObj.intValue();
			}
			Integer configVersionObj = (Integer)loginDBObj.get(KEY_CONFIG_VERSION);
			if ( configVersionObj != null ) {
				configVersion = configVersionObj.intValue();
			}
		}
		logger.debug("majorVersion:{}, minorVersion:{}, configVersion:{}",
				new Object[]{majorVersion, minorVersion, configVersion});
	}
	
	/**
	 * Set the client major and minor version
	 * @param majorVersion
	 * @param minorVersion
	 */
	public void setClientVersion(int majorVersion, int minorVersion, int configVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.configVersion = configVersion;
	}

	/**
	 * Save the client major and minor version to database.
	 */
	public void saveClientVersion() {
		DBObject query = MongoDBUtil.createDBObject();
		MapDBObject objectToSave = MongoDBUtil.createMapDBObject();
		objectToSave.put(KEY_MAJOR_VERSION, this.majorVersion);
		objectToSave.put(KEY_MINOR_VERSION, this.minorVersion);
		objectToSave.put(KEY_CONFIG_VERSION, this.configVersion);
		MongoDBUtil.saveToMongo(query, objectToSave, 
				databaseName, namespace, COLL_NAME, isSafeWrite);
	}
	
	/**
	 * The required minimun major version
	 * @return
	 */
	public int getClientMajorVersion() {
		return this.majorVersion;
	}
	
	/**
	 * The required minimun minor version
	 * @return
	 */
	public int getClientMinorVersion() {
		return this.minorVersion;
	}
	
	/**
	 * The config version
	 * @return
	 */
	public int getClientConfigVersion() {
		return this.configVersion;
	}
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static LoginManager getInstance() {
		return instance;
	}
	
	public void register(
			IoSession session, SessionKey sessionKey, String userName, 
			String roleName, String password, String email, int genderIndex,
			String client, String country, String channel, int locx, int locy) {
		
		BseRegister.Builder builder = BseRegister.newBuilder();
		User newUser = null;
		GameContext gameContext = GameContext.getInstance();
		
		boolean successRegister = false;
		boolean byNewMethod = false;
		boolean isGuest = false;
		boolean syncWithDiscuz = false;
		String  oldRoleName = Constant.EMPTY;
		/**
		 * normalRegister 为 true 表示用户注册
		 */
		boolean normalRegister = false;
		UserManager manager = UserManager.getInstance();
		if ( sessionKey != null ) {
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			if ( user != null ) {
				//玩家完善信息
				newUser = user;
				if ( newUser != null ) {
					isGuest = newUser.isGuest();
					if ( isGuest ) {
						syncWithDiscuz = true;
					}
					//修改玩家昵称
					normalRegister = false;
					oldRoleName = newUser.getRoleName();
					if ( StringUtil.checkNotEmpty(roleName) ) {
						String trimRoleName = roleName.trim();
						if ( trimRoleName.indexOf(' ') < 0 && trimRoleName.length() >= 3 && trimRoleName.length() < 9) {
							if ( !trimRoleName.equals(oldRoleName) ) {
								//The roleName is changing
								if ( ChatManager.getInstance().containBadWord(trimRoleName) ) {
									builder.setCode(RegisterErrorCode.BAD_WORD.ordinal());
									builder.setDesc(Text.text("register.badword"));
									successRegister = true;
								} else {
									boolean dup = gameContext.getUserManager().checkRoleNameExist(trimRoleName);
									if ( dup ) {
										builder.setCode(RegisterErrorCode.EXIST.ordinal());
										builder.setDesc(Text.text("register.exist"));
										successRegister = false;
									} else {
										newUser.setRoleName(trimRoleName);
										successRegister = true;
										String message = Text.text("change.rolename");
										SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
									}
								}
							} else {
								newUser.setRoleName(trimRoleName);
								successRegister = true;
							}
							byNewMethod = true;
						} else {
							builder.setCode(RegisterErrorCode.INVALID.ordinal());
							builder.setDesc(Text.text("register.invalid"));
							successRegister = false;
						}
					} else {
						builder.setCode(RegisterErrorCode.INVALID.ordinal());
						builder.setDesc(Text.text("register.invalid"));
						successRegister = false;
					}
				} else {
					builder.setCode(RegisterErrorCode.OTHERS.ordinal());
					builder.setDesc(RegisterErrorCode.OTHERS.name());
					logger.info("#register: User {} cannot register because the user is not found by sessionKey {}", roleName, sessionKey);
				}
			} else {
				//玩家注册
				normalRegister = true;
			}
		} else {
	  	//玩家注册
			normalRegister = true;
		}
		if ( normalRegister ) {
			if ( StringUtil.checkNotEmpty(userName) ) {
				//玩家直接注册用户名和密码，用户名与昵称相同
				syncWithDiscuz = true;
				newUser = UserManager.getInstance().createDefaultUser();
				if ( userName.length() < 3 ) {
					builder.setCode(RegisterErrorCode.FORBIDDEN.ordinal());
					builder.setDesc(Text.text("register.invalid"));
				//} else if ( userName.length() > 5 ) {
				//	builder.setCode(RegisterErrorCode.TOO_MUCH.ordinal());
				//	builder.setDesc(Text.text("register.toomuch"));
				} else if ( password == null || password.length() < 5 ) {
					builder.setCode(RegisterErrorCode.PASS_LESS.ordinal());
					builder.setDesc(Text.text("register.password.less"));
				} else {
					boolean dup = gameContext.getUserManager().checkUserNameExist(userName);
					if ( dup ) {
						builder.setCode(RegisterErrorCode.EXIST.ordinal());
						builder.setDesc(Text.text("register.exist"));
					} else {
						if ( !checkNotEmpty(roleName) ) {
							roleName = userName;
						}
						dup = gameContext.getUserManager().checkRoleNameExist(roleName);
						if ( dup ) {
							builder.setCode(RegisterErrorCode.EXIST.ordinal());
							builder.setDesc(Text.text("register.exist"));
						} else {
							if ( ChatManager.getInstance().containBadWord(roleName) ) {
								builder.setCode(RegisterErrorCode.BAD_WORD.ordinal());
								builder.setDesc(Text.text("register.badword"));
							} else {
								newUser.set_id(new UserId(userName));
								newUser.setUsername(userName);
								newUser.setRoleName(roleName);
								newUser.setTutorial(true);
								successRegister = true;
								normalRegister = true;
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
			
		}
		
		if ( successRegister ) {
			try {
				if ( checkNotEmpty(password) ) {
					String plainPassword = password;
					String encryptPassword = encryptSHA1(plainPassword);
					newUser.setPassword(encryptPassword);
					String message = Text.text("change.password");
					SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
				}
				newUser.setGuest(false);
				String oldEmail = newUser.getEmail();
				if ( !newUser.isVerifiedEmail() ) {
					if ( checkNotEmpty(email) ) {
						newUser.setEmail(email);
						String message = Text.text("change.email");
						SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
					}
					/**
					 * Disable the verify email sending here. Move it to AccountManager
					 * 2012-11-21
					 */
					//Send verify email
					//EmailManager.getInstance().sendVerifyEmail(newUser);
				} else {
					logger.debug("User {} email {} has been verified", newUser.getRoleName(), oldEmail);
					String message = Text.text("change.email.verified");
					SysMessageManager.getInstance().sendClientInfoRawMessage(sessionKey, message, 5000);
				}
				if ( genderIndex > 0 && genderIndex < Gender.values().length - 1 ) {
					newUser.setGender(Gender.values()[genderIndex]);
				} else {
					newUser.setGender(Gender.MALE);						
				}
				if ( checkNotEmpty(client) ) {
					newUser.setClient(client);
				}
				if ( checkNotEmpty(channel) ) {
					newUser.setChannel(channel);
				}
				if ( checkNotEmpty(country) ) {
					newUser.setCountry(country);
				}
				if ( locx > 0 && locy > 0 ) {
					Location loc = new Location();
					loc.x = locx;
					loc.y = locy;
					newUser.setLocation(loc);
				}
				//Dont sync with discuz
				//wangqi 2012-10-14
				/*
				if ( syncWithDiscuz ) {
					logger.debug("User {} will register on discuz too.", roleName);
					final String r = roleName;
					final String p = password;
					final String e = email;
					GameContext.getInstance().runSmallTask(new Runnable() {
						public void run() {
							DiscuzSync.getInstance().register(r, p, e, null);
						}
					});
				}
				*/
				if ( byNewMethod ) {
					gameContext.getUserManager().saveUser(newUser, false);
					//update user's roleInfo
					gameContext.getInstance().writeResponse(sessionKey, newUser.toBseRoleInfo());
					
					StatClient.getIntance().sendDataToStatServer(newUser, 
							StatAction.FillProfile, RegisterErrorCode.SUCCESS, newUser.getEmail(), newUser.getGender(), oldRoleName);
				} else {
					gameContext.getUserManager().saveUser(newUser, true);
					gameContext.getUserManager().saveUserBag(newUser, true);
					
					//检查用户渠道是否需要赠送礼品
					ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, newUser);
					
					StatClient.getIntance().sendDataToStatServer(newUser, 
							StatAction.Register, RegisterErrorCode.SUCCESS, newUser.getEmail(), newUser.getGender(), oldRoleName);
				}
				builder.setCode(RegisterErrorCode.SUCCESS.ordinal());
				builder.setDesc(RegisterErrorCode.SUCCESS.name());
				
				//Send welcome message to globals
				if ( isGuest ) {
					ChatManager.getInstance().processChatToWorldAsyn(null, Text.text("notice.welcome", roleName));
				}
				
			} catch (Throwable e) {
				if ( logger.isErrorEnabled() ) {
					logger.error("Register error {}", e.getMessage());
				}
				if ( logger.isDebugEnabled() ) {
					logger.debug(e.toString(), e);
				}
				builder.setCode(RegisterErrorCode.OTHERS.ordinal());
				builder.setDesc(RegisterErrorCode.OTHERS.name());
			}
		}
		XinqiMessage response = new XinqiMessage();
		response.payload = builder.build();
		//END
		session.write(response);
	}
	
	/**
	 * Create a new role in game
	 * 
	 * @param roleName
	 * @param genderIndex
	 * @return
	 */
	public boolean createGameRole(IoSession session, String userToken, String roleName, int genderIndex) {
		boolean success = false;
		String  oldRoleName = Constant.EMPTY;
		if ( StringUtil.checkNotEmpty(userToken) ) {
			success = !UserManager.getInstance().checkUserNameExist(userToken);
			if ( success ) {
				if ( StringUtil.checkNotEmpty(roleName) ) {
					Gender gender = Gender.MALE;
					//1: female, 2: male
					if ( genderIndex == 1 ) {
						gender = Gender.FEMALE;
					} else if ( genderIndex == 2 ) {
						gender = Gender.MALE;
					}
					success = !UserManager.getInstance().checkRoleNameExist(roleName);
					if ( success ) {
						User user = UserManager.getInstance().createDefaultUser();
						user.set_id(new UserId(userToken));
						user.setUsername(userToken);
						user.setRoleName(roleName);
						user.setGender(gender);
						success = true;
						
						// Register user's session
						GameContext.getInstance().registerUserSession(session, user, null);
						
						//Mark the user as first time login.
						user.setGuest(true);
						
						UserManager.getInstance().saveUser(user, true);
						UserManager.getInstance().saveUserBag(user, true);
						
						sendBseCreateRole(session, userToken, RegisterErrorCode.SUCCESS.ordinal(), null);
						
						StatClient.getIntance().sendDataToStatServer(user, 
								StatAction.CreateRole, RegisterErrorCode.SUCCESS, user.getEmail(), user.getGender(), userToken);
					} else {
						//role name exist
						sendBseCreateRole(session, 
								RegisterErrorCode.EXIST.ordinal(), Text.text("register.exist"));
					}
				} else {
					//empty rolename
					sendBseCreateRole(session, 
							RegisterErrorCode.INVALID.ordinal(), Text.text("register.invalid"));
				}
			} else {
				//token name exist
				sendBseCreateRole(session, 
						RegisterErrorCode.EXIST.ordinal(), Text.text("register.exist"));
			}
		} else {
			//empty token
			sendBseCreateRole(session, 
					RegisterErrorCode.INVALID.ordinal(), Text.text("register.invalid"));
		}

		return success;
	}
	
	/**
	 * Login the given user.
	 * @param userName
	 * @param password
	 * @param loginConfigVersion The config 
	 * @param session
	 */
	public boolean login(String userName, String password, 
			int loginConfigVersion, int majorVersion, int minorVersion,
			IoSession session, BceLogin loginInfo) {
		return login(userName, password, loginConfigVersion, majorVersion, 
				minorVersion, session, loginInfo, null);
	}
	
	/**
	 * Login the given user.
	 * @param userName
	 * @param password
	 * @param loginConfigVersion The config 
	 * @param session
	 */
	public boolean login(String userName, String password, 
			int loginConfigVersion, int majorVersion, int minorVersion,
			IoSession session, BceLogin loginInfo, SessionKey sessionKey) {
		
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

		//Check client version first
		String clientVersion = StringUtil.concat(majorVersion, ".", this.minorVersion, ".", loginInfo.getTinyversion());
		boolean checkPass = ScriptManager.getInstance().runScriptForBoolean(ScriptHook.VERSION_CHECK,
				majorVersion, minorVersion, loginInfo.getTinyversion(), loginInfo.getChannel(), 
				this.majorVersion, this.minorVersion, session, clientVersion);
		if ( !checkPass ) {
			return false;
		}

		boolean loginSuccess = false;
		User user = null;
		if ( loginInfo.getIsthirdauth() ) {
			if ( userName != null ) {
				boolean alreadyReg = UserManager.getInstance().checkUserNameExist(userName);
				if ( !alreadyReg ) {
					BseLogin.Builder loginRep = BseLogin.newBuilder();
					loginRep.setCode(ErrorCode.THIRD_REG.ordinal());
					loginRep.setDesc(Text.text(ErrorCode.THIRD_REG.desc()));
					XinqiMessage response = new XinqiMessage();
					response.payload = loginRep.build();
					session.write(response);
					loginSuccess = false;
					logger.debug("User {} need reg by third party auth", userName);
				} else {
					user = UserManager.getInstance().queryUser(userName);
					user.setChannel(loginInfo.getChannel());
					UserManager.getInstance().queryUserBag(user);
					UserManager.getInstance().queryUserUnlock(user);
					loginSuccess = true;
					logger.debug("User {} login by third party auth", userName);
					
					if ( user.isGuest() ) {
						user.setGuest(false);
						//检查用户渠道是否需要赠送礼品
						ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, user);
					}

					StatClient.getIntance().sendDataToStatServer(user, loginInfo.getUuid(), 
							StatAction.LoginThird, 
							loginInfo.getLang(), loginInfo.getClient(), 
							clientVersion, loginInfo.getChannel(), loginInfo.getScreen());
				}
			} else {
				BseLogin.Builder loginRep = BseLogin.newBuilder();
				loginRep.setCode(ErrorCode.NOTFOUND.ordinal());
				loginRep.setDesc(Text.text(ErrorCode.NOTFOUND.desc()));
				XinqiMessage response = new XinqiMessage();
				response.payload = loginRep.build();
				session.write(response);
				loginSuccess = false;
			}
		} else if ( !StringUtil.checkNotEmpty(userName) && !StringUtil.checkNotEmpty(password) ) {
			//Guest login & register method
			userName = getRandomUserName();
			String roleName = getRandomRoleName();
			User newUser = UserManager.getInstance().createDefaultUser();
			newUser.set_id(new UserId(userName));
			newUser.setUuid(loginInfo.getUuid());
			newUser.setUsername(userName);
			newUser.setRoleName(roleName);
			newUser.setGender(Gender.MALE);
			newUser.setGuest(true);
			//newUser.setTutorial(true);
			newUser.setTutorial(true);
			newUser.setScreen(loginInfo.getScreen());
			newUser.setDeviceToken(loginInfo.getDevicetoken());
			newUser.setChannel(loginInfo.getChannel());
			UserManager.getInstance().saveUser(newUser, true);
			UserManager.getInstance().saveUserBag(newUser, true);
						
			user = newUser;
			loginSuccess = true;
			
			//检查用户渠道是否需要赠送礼品
			ScriptManager.getInstance().runScript(ScriptHook.GIFT_FOR_CHANNEL, newUser);
			
			StatClient.getIntance().sendDataToStatServer(user, loginInfo.getUuid(), 
					StatAction.LoginQuick, 
					loginInfo.getLang(), loginInfo.getClient(), 
					clientVersion, loginInfo.getChannel(), loginInfo.getScreen());
		} else {
			if ( StringUtil.checkNotEmpty(userName) && !StringUtil.checkNotEmpty(password) ) {
				//用户名不为空但是密码为空，说明用户用10位长的用户名直接登陆
				//只允许使用了游客注册的用户这么登陆
				boolean allowLogin = true;
				if ( userName.length() == USER_NAME_LEN ) {
					for ( char ch : userName.toCharArray() ) {
						if ( ch < '0' || ch > 'z' ) {
							allowLogin = false;
							break;
						}
					}
				} else {
					allowLogin = false;
				}
				if ( allowLogin ) {
					user = GameContext.getInstance().getUserManager().queryUser(userName);
					if (user == null) {
						// User not found
						BseLogin.Builder loginRep = BseLogin.newBuilder();
						loginRep.setCode(ErrorCode.NOTFOUND.ordinal());
						loginRep.setDesc(Text.text(ErrorCode.NOTFOUND.desc()));
						XinqiMessage response = new XinqiMessage();
						response.payload = loginRep.build();
						session.write(response);
					} else {
						loginSuccess = true;
						StatClient.getIntance().sendDataToStatServer(user, loginInfo.getUuid(), 
								StatAction.LoginGuest,
								loginInfo.getLang(), loginInfo.getClient(), 
								clientVersion, loginInfo.getChannel(), loginInfo.getScreen());
					}
				} else {
					// User not found
					BseLogin.Builder loginRep = BseLogin.newBuilder();
					loginRep.setCode(ErrorCode.WRONGPASS.ordinal());
					loginRep.setDesc(Text.text(ErrorCode.WRONGPASS.desc()));
					XinqiMessage response = new XinqiMessage();
					response.payload = loginRep.build();
					session.write(response);
				}
			} else {
				//用户使用标准的角色名和密码登陆
				user = GameContext.getInstance().getUserManager().queryUserByRoleName(userName);
				if (user == null) {
					// User not found
					BseLogin.Builder loginRep = BseLogin.newBuilder();
					loginRep.setCode(ErrorCode.NOTFOUND.ordinal());
					loginRep.setDesc(Text.text(ErrorCode.NOTFOUND.desc()));
					XinqiMessage response = new XinqiMessage();
					response.payload = loginRep.build();
					session.write(response);
					loginSuccess = false;
				} else {
					if ( !StringUtil.checkNotEmpty(password) ) {
						//出于安全考虑，不允许用空白密码登陆
						XinqiMessage response = new XinqiMessage();
						BseLogin.Builder loginRep = BseLogin.newBuilder();
						loginRep.setCode(ErrorCode.WRONGPASS.ordinal());
						loginRep.setDesc(Text.text(ErrorCode.WRONGPASS.desc()));
						response = new XinqiMessage();
						response.payload = loginRep.build();
						GameContext.getInstance().writeResponse(session, response, user.getSessionKey());
						
						loginSuccess = false;
					} else {
						if ( StringUtil.checkNotEmpty(user.getPassword()) ) {
							//The user has password means he has been manually registered.
							String encryptPassword = encryptPassword(password);
							if (user.getPassword().equals(encryptPassword)) {
								loginSuccess = true;
								
								StatClient.getIntance().sendDataToStatServer(user, loginInfo.getUuid(), 
										StatAction.LoginNormal, 
										loginInfo.getLang(), loginInfo.getClient(),
										clientVersion, loginInfo.getChannel(), loginInfo.getScreen());
							} else {
								//Wrong password
								//Check the temp password first
								Jedis jedis = JedisFactory.getJedis();
								String userKey = StringUtil.concat(Constant.FORGET_PASS_KEY, user.getRoleName());
								String tempPassword = jedis.get(userKey);
								if ( password != null && password.equals(tempPassword) ) {
									logger.debug("User {} use temp password to login", user.getRoleName() );
									jedis.del(userKey);
									loginSuccess = true;
								} else {
									XinqiMessage response = new XinqiMessage();
									BseLogin.Builder loginRep = BseLogin.newBuilder();
									loginRep.setCode(ErrorCode.WRONGPASS.ordinal());
									loginRep.setDesc(Text.text(ErrorCode.WRONGPASS.desc()));
									response = new XinqiMessage();
									response.payload = loginRep.build();
									GameContext.getInstance().writeResponse(session, response, user.getSessionKey());
								}
							}
						} else {
							//Empty password
							XinqiMessage response = new XinqiMessage();
							BseLogin.Builder loginRep = BseLogin.newBuilder();
							loginRep.setCode(ErrorCode.WRONGPASS.ordinal());
							loginRep.setDesc(Text.text(ErrorCode.WRONGPASS.desc()));
							response = new XinqiMessage();
							response.payload = loginRep.build();
							GameContext.getInstance().writeResponse(session, response, user.getSessionKey());
						}
					}
				}
			}
		}

		if ( loginSuccess ) {
			// Sent back user's sessionkey
			XinqiMessage login = new XinqiMessage();
			BseLogin.Builder loginRep = BseLogin.newBuilder();
			loginRep.setCode(ErrorCode.SUCCESS.ordinal());
			loginRep.setDesc(Text.text(ErrorCode.SUCCESS.desc()));
			loginRep.setSessionid(user.getSessionKey().toString());
			//loginRep.setToken(accounId);
			loginRep.setTutorial(user.isTutorial());
			login = new XinqiMessage();
			login.payload = loginRep.build();
			GameContext.getInstance().writeResponse(user.getSessionKey(), login);
			
			LoginManager.getInstance().loginProcess(loginConfigVersion, session, 
					loginInfo.getUuid(), loginInfo.getScreen(), loginInfo.getDevicetoken(), 
					loginInfo.getClient(), loginInfo.getLang(), loginInfo.getChannel(), 
					sessionKey, user, user.getUsername());
		}
		/**
		 * The message is already sent
		 */
		/*
		else {
			// Wrong passx
			// TODO max retries...
			XinqiMessage response = new XinqiMessage();
			BseLogin.Builder loginRep = BseLogin.newBuilder();
			loginRep.setCode(ErrorCode.WRONGPASS.ordinal());
			loginRep.setDesc(Text.text(ErrorCode.WRONGPASS.desc()));
			response = new XinqiMessage();
			response.payload = loginRep.build();
			GameContext.getInstance().writeResponse(session, response, user.getSessionKey());
		}
		*/
		return loginSuccess;
	}

	/**
	 * @param userName
	 * @param loginConfigVersion
	 * @param session
	 * @param loginInfo
	 * @param sessionKey
	 * @param user
	 */
	public final boolean loginProcess(int loginConfigVersion,
			IoSession session, String uuid, String screen, String deviceToken, 
			String client, String lang, String channel,
			SessionKey sessionKey, final User user, String accounId) {

		if ( !checkUserLoginStatus(user, session) ) {
			return false;
		}

		logger.debug("User {} successfully login", user.getRoleName());
											
		//Get user's bag and relation and unlock
		GameContext.getInstance().getUserManager().queryUserBag(user);
		GameContext.getInstance().getUserManager().queryUserRelation(user);
		GameContext.getInstance().getUserManager().queryUserUnlock(user);
		
		user.setUuid(uuid);
		user.setScreen(screen);
		user.setDeviceToken(deviceToken);
		/**
		 * Change last login role
		 */
		Account account = user.getAccount();
		if ( account != null ) {
			account.setLastUserId(user.get_id());
			AccountManager.getInstance().saveAccount(account);
		}
		
		checkMulitDeviceLogin(client, sessionKey, user);

		// Register user's session
		GameContext.getInstance().registerUserSession(session, user, sessionKey);
		
		//Find locale
		if ( StringUtil.checkNotEmpty(lang) ) {
			user.setCountry(lang);
			user.setUserLocale(StringUtil.parseLocale(user.getCountry(), Locale.SIMPLIFIED_CHINESE));
			LocaleThreadLocal threadLocal = GameContext.getInstance().getLocaleThreadLocal();
			Locale userLocale = threadLocal.get();
			userLocale = GameContext.getInstance().getSessionManager().
						findUserLocale(sessionKey);
			threadLocal.set(userLocale);
		} else {
			user.setCountry(Constant.DEFAULT_LOCALE);
			user.setUserLocale(Locale.SIMPLIFIED_CHINESE);
		}
		logger.debug("User {} locale is {}", user.getRoleName(), user.getUserLocale());

//		//Generate the secure token
//		XinqiMessage init = new XinqiMessage();
//		XinqiBseInit.BseInit.Builder builder = XinqiBseInit.BseInit.newBuilder();
//		//Regenerate the token
//		String newToken = CipherManager.getInstance().generateEncryptedUserToken(user.get_id());
//		builder.setSuccess(true);
//		builder.setToken(newToken);
//		builder.setRefresh(false);
//		builder.setSessionid(user.getSessionKey().toString());
//		init.payload = builder.build();
//		GameContext.getInstance().writeResponse(user.getSessionKey(), init);
		
		//Check user's vipStatus
		//boolean isVip = UserManager.getInstance().checkUserVipStatus(user);
		sendConfigDataIfNeeded(loginConfigVersion, user, false);
		
		/**
		 * All config datas are stored at http side
		 */
		/*
		BseZip taskZip = TaskManager.getInstance().toBseZip();
		GameContext.getInstance().writeResponse(user.getSessionKey(), taskZip);
		
		XinqiMessage itemData = new XinqiMessage();
		itemData.payload = GameContext.getInstance().getItemManager().toBseZip();
		GameContext.getInstance().writeResponse(user.getSessionKey(), itemData);
		*/
		
		XinqiMessage gameData = new XinqiMessage();
		gameData.payload = GameContext.getInstance().getGameDataManager().toBseGameDataKey(user.getLevel());
		GameContext.getInstance().writeResponse(user.getSessionKey(), gameData);
		
		// 0  玩家登陆奖励	
		ArrayList<Reward> loginRewards = RewardManager.getInstance().generateRewardsFromScript(
				user, 10, ScriptHook.USER_LOGIN_REWARD);
		user.setLoginRewards(loginRewards);
		BseLoginLottery.Builder loginLottery = BseLoginLottery.newBuilder();
		for ( Reward reward: loginRewards ) {
			loginLottery.addLotteries(reward.toLoginLotteryData());
		}
		XinqiMessage loginLotteryXinqi = new XinqiMessage();
		loginLotteryXinqi.payload = loginLottery.build();
		//Make sure it reach client the before the roleInfo
		//GameContext.getInstance().writeResponse(user.getSessionKey(), loginLotteryXinqi);
		GameContext.getInstance().writeResponse(user.getSessionKey(), loginLotteryXinqi);
		
		long currentTimeMillis = System.currentTimeMillis();
		
		// 1. 玩家基本角色信息
		ScriptManager.getInstance().runScript(ScriptHook.BAG_CHECK, user);
		//update user basic prop
		UserCalculator.updateUserBasicProp(user);
		//Prepare login reward
		RewardManager.getInstance().processLoginReward(user, currentTimeMillis);
		// 获取用户公会数据
		if ( StringUtil.checkNotEmpty(user.getGuildId()) ) {
			Guild guild = GuildManager.getInstance().queryGuildById(user.getGuildId());
			if ( guild == null ) {
				//The guild does not exist
				user.setGuildId(null);
				logger.warn("The guildId {} is cleared for user {}", user.getRoleName());
			} else {
				if ( GuildManager.getInstance().checkOperationFee(user, guild) ) {
					GuildMember member = GuildManager.getInstance().queryGuildMemberByUserId(user.get_id());
					user.setGuild(guild);
					user.setGuildMember(member);
					if ( member != null ) {
						if ( user.getRoleName().equals(member.getRoleName()) ) {
							member.setRoleName(user.getRoleName());
							GuildManager.getInstance().saveGuildMember(member);
						}
					} else {
						logger.warn("The user's guild member is null.");
					}
					GuildManager.getInstance().markGuildMemberOnline(user, true, false);
					/**
					 * Improve guild abilities if exist
					 */
					ScriptManager.getInstance().runScript(ScriptHook.GUILD_USER_ABILITY, user);
				} else {
					user.setGuildId(null);
				}
			}
		}
		XinqiMessage roleInfo = new XinqiMessage();
		roleInfo.payload = user.toBseRoleInfo();
		GameContext.getInstance().writeResponse(user.getSessionKey(), roleInfo);
		
		// 2  玩家游戏状态解锁
		Collection<Unlock> unlocks = user.getUnlocks();
		BseFuncUnlock.Builder funcUnlockBuilder = BseFuncUnlock.newBuilder();
		funcUnlockBuilder.setIsnew(false);
		for ( Unlock unlock : unlocks ) {
			funcUnlockBuilder.addUnlocks(unlock.toFuncUnlock());
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), 
				funcUnlockBuilder.build());

		// 2. 玩家角色配置数据
		XinqiMessage roleConfig = new XinqiMessage();
		BseRoleConfig.Builder roleConfigBuilder = BseRoleConfig.newBuilder();
		roleConfigBuilder.setEffectSwitch(user.isConfigEffectSwitch());
		roleConfigBuilder.setEffectVolume(user.getConfigEffectVolume());
		roleConfigBuilder.setMusicSwitch(user.isConfigMusicSwitch());
		roleConfigBuilder.setMusicVolume(user.getConfigMusicVolume());
		roleConfigBuilder.setGuidestep(0);
		roleConfigBuilder.setHideGlasses(user.isConfigHideGlass());
		roleConfigBuilder.setHideHat(user.isConfigHideHat());
		roleConfigBuilder.setHideSuit(user.isConfigHideSuite());
		roleConfig.payload = roleConfigBuilder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), roleConfig);

		// 3. 玩家战斗信息			
		final XinqiMessage roleBattle = new XinqiMessage();
		//Automatically tidy user's bag.
		user.getBag().tidyUserBag();
		GameContext.getInstance().getUserManager().saveUserBag(user, true);
		//Send user's information to client.
		roleBattle.payload = user.toBseRoleBattleInfo(true);
		GameContext.getInstance().writeResponse(user.getSessionKey(), roleBattle);

		// 4. 玩家的任务列表（登陆时， 需要通过该协议初始化任务列表，游戏过程中，可以通过AddTask,
		// DelTask同步客户端的任务列表）
		//Check if we need to refresh daily tasks
		TaskManager taskManager = GameContext.getInstance().getTaskManager();
		TreeSet<TaskPojo> tasks = new TreeSet<TaskPojo>(taskManager.getUserLoginTasks(user));
		for ( TaskType taskType : TaskType.values() ) {
			if ( taskType != TaskType.TASK_ACHIVEMENT && 
					taskType != TaskType.TASK_RANDOM ) {
				tasks.addAll(user.getTaskFinished(taskType));
			}
		}
		//Set the todo task list to user's field.
		XinqiBseTaskList.BseTaskList.Builder taskListBuilder = XinqiBseTaskList.BseTaskList
				.newBuilder();
		BsePromotion.Builder promoBuilder = BsePromotion.newBuilder();
		Collection<TaskPojo> awardedTasks = taskManager.acquireAwardedTasks(user, TaskType.TASK_ACTIVITY);
		tasks.addAll(awardedTasks);
		for ( TaskPojo task : tasks ) {
			if ( task.getType() == TaskType.TASK_ACHIVEMENT || 
					task.getType() == TaskType.TASK_RANDOM ) {
				continue;
			} else if ( task.getType() == TaskType.TASK_ACTIVITY ) {
				logger.debug("Add activity task:{};{}", task.getId(), task.getName());
				promoBuilder.addPromotion(task.toPromotion(user));
			} else {
				taskListBuilder.addTaskList(taskManager.toTaskProtoInfo(user, task));
			}
		}
		XinqiMessage todoTask = new XinqiMessage();
		todoTask.payload = taskListBuilder.build();
//		BseZip.Builder builder = BseZip.newBuilder();
//		builder.setName("");
//		builder.setPayload(todoTask.payload.toByteString());
		GameContext.getInstance().writeResponse(user.getSessionKey(), todoTask);
		
		XinqiMessage promotion = new XinqiMessage();
		promotion.payload = promoBuilder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), promotion);
		
		// 5.检查用户邮件
		MailMessageManager.getInstance().checkMail(user);
		
		// 6.更新所有成就进度信息
		TaskManager.getInstance().sendBseUserAchievements(user);
		
		/**
		 * Check if the "script.task.UserLevelUp" is finished
		 * Because sometimes user may level up more than one levels, and
		 * User.setLevel can only sent one ModiTask a time. So when user
		 * login, we double check all the others tasks for user upgrading
		 * Call script to trigger task
		 */
		TaskManager.getInstance().processUserTasks(user, TaskHook.USER_UPGRADE);
		
		// 7. 玩家好友信息
		sendFriendStatus(user, true);

		// 8. 检查用户当天是否打卡
		/**
		 * 目前停止了打卡的功能，所以直接视用户登陆为打卡
		 * wangqi 2012-10-11
		 */
		boolean success = RewardManager.getInstance().
				takeDailyMarkReward(user, currentTimeMillis);
		/*
		DailyMarkReward dailyMarkReward = 
				RewardManager.getInstance().processDailyMarkReward(
						user, currentTimeMillis);
		if ( dailyMarkReward != null && !dailyMarkReward.isTodayMarked() ) {
			//Send BseDailyMark
			BseDailyMark.Builder mark = BseDailyMark.newBuilder();
			mark.setDate(dailyMarkReward.getDayOfMonth());
			mark.addAllMarkArr(dailyMarkReward.getMarkArray());
			mark.setDaycount(dailyMarkReward.getTotalCount());
			mark.setResult(false);
			
			XinqiMessage dailyMarkXinqi = new XinqiMessage();
			dailyMarkXinqi.payload = mark.build();
			GameContext.getInstance().writeResponse(user.getSessionKey(), dailyMarkXinqi);
		}
		*/
		
		// 9. 启动在线奖励机制
		/*
		OnlineReward onlineReward = RewardManager.getInstance().
				processOnlineReward(user, currentTimeMillis);
		if ( onlineReward != null ) {
			BseOnlineReward.Builder onlineRewardBuilder = BseOnlineReward.newBuilder();
			onlineRewardBuilder.setStepID(onlineReward.getStepId());
			onlineRewardBuilder.setRemainTime(onlineReward.getRemainSeconds());
			ArrayList<Reward> rewards = onlineReward.getRewards();
			for ( Reward reward : rewards ) {
				onlineRewardBuilder.addPropID(StringUtil.toInt(reward.getPropId(), 0));
				onlineRewardBuilder.addPropLevel(reward.getPropLevel());
				onlineRewardBuilder.addPropCount(reward.getPropCount());
			}
			onlineRewardBuilder.setType(onlineReward.getType());
			
			XinqiMessage onlineRewardXinqi = new XinqiMessage();
			onlineRewardXinqi.payload = onlineRewardBuilder.build();
			GameContext.getInstance().writeResponse(user.getSessionKey(), onlineRewardXinqi);
		}
		*/
		
		//Call the script hook here
		GameContext.getInstance().getTaskManager().processUserTasks(user, 
				TaskHook.LOGIN);
		
		// 11. 活动推广公告
		//ActivityManager.getInstance().displayActivityForUser(user);
		
		
		// 12 充值数据
		ScriptManager.getInstance().runScript(ScriptHook.USER_CHARGE_LIST, 
				user, channel);
		
		// 13 VIP离线经验奖励
		VipManager.getInstance().processVipOfflineExp(user, currentTimeMillis, true);
		
		// 14 Challenge数据发送邮件
		String yestDateStr = DateUtil.getYesterday(currentTimeMillis);
		String todayDateStr = DateUtil.getToday(currentTimeMillis);
		OfflineChallManager.getInstance().queryChallengeInfo(user, yestDateStr);
		OfflineChallManager.getInstance().queryChallengeInfo(user, todayDateStr);
		OfflineChallManager.getInstance().cleanChallengeData(user, yestDateStr);
		OfflineChallManager.getInstance().cleanChallengeData(user, todayDateStr);
		
		// 15 Bulletin显示
		BulletinManager.getInstance().displayBulletin(user);
		BulletinManager.getInstance().displayPersonalConfirmMessage(user);
		
		// 16.Promotion公告
		ActivityManager.getInstance().sendPromotionMessages(user);
		
		// 17.ExitGame奖励
		ExitGameManager.getInstance().checkLogin(user);
		
		// 18.Check COLLECT type tasks
		//Check the collect task
		TaskManager.getInstance().processUserTasks(user, TaskHook.COLLECT, null);
		
		// 19.Scan user's biblio
		BiblioManager.getInstance().scanUserBag(user);
				
		//Update user statistic
		user.setLdate(new Date());
		/**
		 * Make the change after user entering training room
		 * 2012-09-10
		 * wangqi
		 */
		//user.setTutorial(false);
		GameContext.getInstance().getUserManager().saveUser(user, false);

		/**
		 * Check VIP level and send notification
		 */
		if ( user.getViplevel()>=7 ) {
			ChatManager.getInstance().processChatToWorldAsyn(null, Text.text("notice.vip.login", 
					user.getViplevel(), user.getRoleName()));
		}
		
		/**
		 * PuzzlePromotioin
		 */
		ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_PUZZLE, user);
		
		/**
		 * Send the token
		 */
		//Generate the secure token
		XinqiMessage init = new XinqiMessage();
		XinqiBseInit.BseInit.Builder builder = XinqiBseInit.BseInit.newBuilder();
		//Regenerate the token
		String newToken = CipherManager.getInstance().generateEncryptedUserToken(user.get_id());
		builder.setSuccess(true);
		builder.setToken(newToken);
		builder.setRefresh(false);
		builder.setSessionid(user.getSessionKey().toString());
		init.payload = builder.build();
		GameContext.getInstance().writeResponse(session, init, null);
		
		/*
		BseOnlineReward.Builder or = BseOnlineReward.newBuilder();
		or.setRemainTime(4);
		for ( int i=0; i<4; i++ ) {
			or.addPropCount(1);
			or.addPropLevel(1);
			or.addPropID(StringUtil.toInt(UserManager.basicUserGiftBoxId, 0));
		}
		or.setStepID(0);
		GameContext.getInstance().writeResponse(session, or.build(), null);
		*/
		
		return true;
	}

	/**
	 * Check if the user logins in multi devices
	 * @param loginInfo
	 * @param sessionKey
	 * @param user
	 * @return
	 */
	public void checkMulitDeviceLogin(String client,
			SessionKey sessionKey, User user) {
		// Check if the user is already login with another device.
		SessionKey userSessionKey = GameContext.getInstance()
				.findSessionKeyByUserId(user.get_id());
		if (userSessionKey != null && !userSessionKey.equals(sessionKey) ) {
			// The user already login.
			// TODO max retries...

			//Check if it is a proxy user
			boolean isProxy = GameContext.getInstance().getSessionManager().
				isSessionKeyFromProxy(userSessionKey);
			
			if ( !isProxy ) {
				BceLogout.Builder logout = BceLogout.newBuilder();
				logout.setClient(client);
				logout.setUserid(user.get_id().toString());
				logout.setUsername(user.getUsername());
				User oldUser = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
				if (  oldUser == null ) {
					String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(userSessionKey);
					GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, logout.build());
				} else {
					logout(oldUser.getSession(), oldUser, userSessionKey, client, true);
				}
				logger.info("User {} already login with session", user.getRoleName(), user.getSession());
			} else {
				logger.debug("User {} already has a proxy user {} online. Ignore it.", user.getRoleName(), userSessionKey);
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
		if ( user != null ) {
			userName = user.getRoleName();
		}
		/*
		if ( session != null ) {
			String message = Text.text("login.more", new Object[]{client});
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user, message, Action.EXIT_GAME, Type.NORMAL);
			logger.debug("Close user {} connection because he logined in by {}", userName, client);
		} else {
			//logger.debug("Clean user {} sessionKey because he logined in by {}", userName, client);
		}
		*/
		if ( loginMultiPrompt ) {
			String message = Text.text("login.more", new Object[]{client});
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user, message, Action.EXIT_GAME, Type.NORMAL);
		}

		GameContext.getInstance().deregisterUserBySessionKey(oldSessionKey);
	}
	
	
	/**
	 * Check to see if all the config data should be sent to user.
	 * If the version is available and equal to game server's version,
	 * send the config data. Otherwise, do not send them.
	 * 
	 * @param version
	 * @param user
	 */
	private void sendConfigDataIfNeeded(int version, User user, boolean isVip) {
		//Send response to client
		XinqiMessage response = new XinqiMessage();
		BseConfigData.Builder builder = BseConfigData.newBuilder();
		builder.setVersion(this.getClientConfigVersion());
		response.payload = builder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
		/**
		 * All the config data are sent by http
		 * 2012-12-06
		 */
		/**
		 * If the config version in server is zero, it means the default data 
		 * does not change. So there is no need to basic config data.
		 * If the server config version is greater than zero, and the client config 
		 * version is less than server's config version, the basic data should 
		 * be sent to client.
		 * 
		 * wangqi 2012-08-10
		 */
		/*
		if ( this.getClientConfigVersion()==-1 || 
				(this.getClientConfigVersion() > 0 && version < this.getClientConfigVersion()) ) {
			logger.debug("Config version changed. Send data again.");
			
			// 0.0 装备的基本数据
//			XinqiMessage equipData = new XinqiMessage();
//			equipData.payload = GameContext.getInstance().getEquipManager().toBseEquipment();
//			GameContext.getInstance().writeResponse(user.getSessionKey(), equipData);
			// Send as zip format
			BseZip equipZip = EquipManager.getInstance().toBseZip();
			GameContext.getInstance().writeResponse(user.getSessionKey(), equipZip);
			
			// 0.1 成就数据
			XinqiMessage achievements = new XinqiMessage();
			achievements.payload = TaskManager.getInstance().toBseAchievement();
			GameContext.getInstance().writeResponse(user.getSessionKey(), achievements);
			
			// 0.2 地图基础数据 
			XinqiMessage mapData = new XinqiMessage();
			mapData.payload = GameContext.getInstance().getMapManager().toBseMap();
			GameContext.getInstance().writeResponse(user.getSessionKey(), mapData);
						
			// 0.3 每日打卡奖励
			XinqiMessage dailyMark = new XinqiMessage();
			dailyMark.payload = GameContext.getInstance().getDailyMarkManager().toBseDailyMark();
			GameContext.getInstance().writeResponse(user.getSessionKey(), dailyMark);

			// 0.4 游戏提示
			XinqiMessage tip = new XinqiMessage();
			tip.payload = GameContext.getInstance().getTipManager().toBseTip(user);
			GameContext.getInstance().writeResponse(user.getSessionKey(), tip);

			// 0.5 游戏任务
//			XinqiMessage taskList = new XinqiMessage();
//			taskList.payload = GameContext.getInstance().getTaskManager().toBseTask();
//			GameContext.getInstance().writeResponse(user.getSessionKey(), taskList);
			// Send as zip format
			//BseZip taskZip = TaskManager.getInstance().toBseZip();
			//GameContext.getInstance().writeResponse(user.getSessionKey(), taskZip);
			
			// 0.6 商城数据
			//XinqiMessage shop = new XinqiMessage();
			/*
			if ( isVip ) {
				int shopDiscount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.VIP_SHOP_DISCOUNT, 80);
				shop.payload = GameContext.getInstance().getShopManager().toBseShop(user, shopDiscount);
				logger.debug("User {} is VIP and all shop item will be discounted to 80%", user.getRoleName(), shopDiscount);
			} else {
				shop.payload = GameContext.getInstance().getShopManager().toBseShop(user);
			}
			* /
			//shop.payload = GameContext.getInstance().getShopManager().toBseShop(user);
			//GameContext.getInstance().writeResponse(user.getSessionKey(), shop);
			
			// 0.7 游戏可配置参数
			/* *
			 * Since the forge/compose/transfer price will change according
			 * to user's level, this data should be send everytime.
			 * 2012-08-10
			 * /
			/*
			XinqiMessage gameData = new XinqiMessage();
			gameData.payload = GameContext.getInstance().getGameDataManager().toBseGameDataKey();
			GameContext.getInstance().writeResponse(user.getSessionKey(), gameData);
			* /
			
			// 0.8 游戏物品数据
//			XinqiMessage itemData = new XinqiMessage();
//			itemData.payload = GameContext.getInstance().getItemManager().toBseItem();
//			GameContext.getInstance().writeResponse(user.getSessionKey(), itemData);

			// 0.9 BseVipPeriodList Empty list
//			BseVipPeriodList.Builder vipPeriod = BseVipPeriodList.newBuilder();
//			XinqiMessage vipData = new XinqiMessage();
//			vipData.payload = vipPeriod.build();
//			GameContext.getInstance().writeResponse(user.getSessionKey(), vipData);
		}
		*/
	}
	
	/**
	 * Check the user's login status
	 * @param user
	 * @return
	 */
	public boolean checkUserLoginStatus(User user, IoSession session) {
		boolean success = false;
		UserLoginStatus status = user.getLoginStatus();
		if ( status == UserLoginStatus.NORMAL ) {
			success =true;
		} else {
			String desc = user.getLoginStatusDesc();
			if ( desc == null ) {
				desc = Constant.EMPTY;
			}
			String message = null;
			ErrorCode code = null;
			if ( status == UserLoginStatus.REMOVED ){
				message = Text.text(ErrorCode.S_REMOVED.desc, desc);
				code = ErrorCode.S_REMOVED;
				success = false;
			} else if ( status == UserLoginStatus.PAUSE ) {
				String key = getUserPauseKey(user.getUsername());
//			jedisDB.set(key, Constant.ONE);
				Jedis jedisDB = JedisFactory.getJedisDB();
				int secondsLeft = 0;
				if ( jedisDB.exists(key) ) {
					Long ttl = jedisDB.ttl(key);
					if ( ttl != null ) {
						secondsLeft = ttl.intValue();
					}
				}
				if ( secondsLeft > 0 ) {
					String timeLeft = convertSecondToDesc(secondsLeft);
					message = Text.text(ErrorCode.S_PAUSE.desc, desc, timeLeft);
					code = ErrorCode.S_PAUSE;
					success = false;
				} else {
					//The pause is expired.
					user.setLoginStatus(UserLoginStatus.NORMAL);
					user.setLoginStatusDesc(Constant.EMPTY);
					success = true;
				}
			}
			if ( !success ) {
				constructResponse(user.getUsername(), code, message, session);
			}
		}
		return success;
	}
	
	/**
	 * Pause the user account for given seconds
	 * @param user
	 * @param seconds
	 */
	public void pauseUserAccount(User user, String message, int seconds) {
		user.setLoginStatus(UserLoginStatus.PAUSE);
		user.setLoginStatusDesc(message);
		String key = getUserPauseKey(user.getUsername());
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.set(key, Constant.ONE);
		jedisDB.expire(key, seconds);
	}
	
	/**
	 * Get the jedis key for user pause entry;
	 * @return
	 */
	public String getUserPauseKey(String userName) {
		String key = StringUtil.concat("login:", userName, ":pause");
		return key;
	}
	
	/**
	 * Encrypt the user's password
	 * @param password
	 * @return
	 */
	public static final String encryptPassword(String password) {
		try {
			String encryptPassword = StringUtil.encryptSHA1(password);
			return encryptPassword;
		} catch (NoSuchAlgorithmException e) {
			return Constant.EMPTY;
		}
	}
	
	/**
	 * Convert the seconds to minutes, hours, days, or months.
	 * @return
	 */
	public String convertSecondToDesc(int seconds) {
		String desc = null;
		if ( seconds > 2592000 ) {
			desc = Text.text("time.month", seconds/2592000);
		} else if ( seconds > 604800 ) {
			desc = Text.text("time.week", seconds/604800);
		} else if ( seconds > 86400 ) {
			desc = Text.text("time.day", seconds/86400);
		} else if ( seconds > 3600 ) {
			desc = Text.text("time.hour", seconds/3600);
		} else if ( seconds > 60 ) {
				desc = Text.text("time.minute", seconds/60);
		} else {
			desc = Text.text("time.second", seconds);
		}
		return desc;
	}
	
	/**
	 * Notify given user's friend he is online or offline
	 * @param user
	 */
	public void sendFriendStatus(User user, boolean isOnline) {
		// 5. 玩家好友信息
		ArrayList<FriendInfoLite> allFriends = new ArrayList<FriendInfoLite>();
		ArrayList<People> onlineP = new ArrayList<People>(); 
		for ( RelationType relationType : RelationType.values() ) {
			Relation relation = user.getRelation(relationType);
			if ( relation != null ) {
				allFriends.addAll(relation.toBseFriendList());
				
				//Notify his friends that the user is online now
				Collection<People> people = relation.listPeople();
				if ( relationType == RelationType.FRIEND ) {
					for ( People p : people ) {
						onlineP.add(p);
					}
				}
			}
		}
		BseFriendList.Builder bseFriendListBuilder = BseFriendList.newBuilder();
		bseFriendListBuilder.setType(1);
		for ( FriendInfoLite lite : allFriends ) {
			bseFriendListBuilder.addFriendList(lite);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), bseFriendListBuilder.build());
		
		for ( People p : onlineP ) {
			SessionKey friendSessionKey =
					GameContext.getInstance().findSessionKeyByUserId(p.getMyId());
			boolean sendOnline = true;
			if ( friendSessionKey != null ) {
				boolean isAI = GameContext.getInstance().getSessionManager().isSessionKeyFromAI(friendSessionKey);
				if ( isAI ) {
					sendOnline = false;
				}
			}
			if ( sendOnline ) {
				if ( isOnline ) {
					logger.debug("Notify user {}'s friend {} he is online.", 
						user.getRoleName(), p.getUsername());
				}
				BseUpdateOnlineStatus.Builder onlineStatusBuilder = 
						BseUpdateOnlineStatus.newBuilder();
				onlineStatusBuilder.setOnline(isOnline);
				onlineStatusBuilder.setUid(user.get_id().toString());
				String roleName = UserManager.getDisplayRoleName(user.getRoleName());
				onlineStatusBuilder.setUsername(roleName);

				XinqiMessage onlineStatus = new XinqiMessage();
				onlineStatus.payload = onlineStatusBuilder.build();
				GameContext.getInstance().writeResponse(friendSessionKey, onlineStatus);	
			}
		}
	}
	
	/**
	 * According to given userId and password, generate 
	 * the random username and make sure it not exist in database
	 * @param userId
	 * @param password
	 * @return
	 */
	public static final String getRandomUserName() {
		String token = Constant.EMPTY;
		boolean dup = true;
		do {
			try {
				char[] userNameChars = new char[USER_NAME_LEN];
				for ( int i=0; i<USER_NAME_LEN; i++ ) {
					userNameChars[i] = CHARS[(int)(MathUtil.nextDouble()*CHARS.length)];
				}
				token = new String(userNameChars);
			} catch (Exception e) {
				logger.error("Failed to generate random name. {}", e.toString());
			}
			dup = UserManager.getInstance().checkUserNameExist(token);
		} while ( dup );
		return token;
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	public static final String getRandomRoleName() {
		String token = Constant.EMPTY;
		boolean dup = true;
		do {
			try {
				char[] userNameChars = new char[ROLE_NAME_LEN];
				for ( int i=0; i<ROLE_NAME_LEN; i++ ) {
					userNameChars[i] = CHARS[(int)(MathUtil.nextDouble()*CHARS.length)];
				}
				token = new String(userNameChars);
			} catch (Exception e) {
				logger.error("Failed to generate random name. {}", e.toString());
			}
			dup = UserManager.getInstance().checkRoleNameExist(token);
		} while ( dup );
		return token;	
	}
	
	/**
	 * Facility method to construct 
	 * @param code
	 * @param session
	 */
	private void constructResponse(String userName, ErrorCode code, 
			String message, IoSession session) {
		// User not found
		BseLogin.Builder rep = BseLogin.newBuilder();
		rep.setCode(code.ordinal());
		rep.setDesc(message);
		XinqiMessage response = new XinqiMessage();
		response.payload = rep.build();
		session.write(response);
		
		logger.debug("User '{}' login message: {}", userName, message);
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
	
	public static enum ErrorCode {
		SUCCESS("login.success"), 
		NOTFOUND("login.notfound"), 
		WRONGPASS("login.wrongpass"), 
		MAXRETRY("login.maxretry"), 
		ALREADY_LOGIN("login.already_login"),
		OTHERS("login.others"),
		VERSION("login.version"),
		S_PAUSE("login.status.paused"),
		S_REMOVED("login.status.removed"),
		TIMEOUT("login.timeout"),
		MAINTANCE("login.maintance"),
		THIRD_REG("login.thirdreg");
		
		private final String desc;
		
		ErrorCode(String desc) {
			this.desc = desc;
		}
		
		public String desc() {
			return this.desc;
		}
	}
	
	public static enum RegisterErrorCode {
		SUCCESS,
		EXIST,
		FORBIDDEN,
		OTHERS,
		BAD_WORD,
		TOO_MUCH,
		PASS_LESS,
		INVALID,
		LOGIN_INVALID,
	}
	
	public static enum UserLoginStatus {
		NORMAL,
		PAUSE,
		REMOVED,
		HIDE
	}
}
