package com.xinqihd.sns.gameserver;

import static com.xinqihd.sns.gameserver.util.StringUtil.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.google.protobuf.Service;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.chat.ChatSender;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.DailyMarkManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.MapManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TipManager;
import com.xinqihd.sns.gameserver.db.mysql.MysqlUtil;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.forge.CraftManager;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.MessageQueue;
import com.xinqihd.sns.gameserver.session.MinaMessageQueue;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.GameProxyClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.XinqiProxyMessage;
import com.xinqihd.sns.gameserver.transport.rpc.MinaRpcPoolChannel;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.LocaleThreadLocal;

/**
 * The context contains all global needed information like config data, script engine,
 * database interface etc.
 * 
 * @author wangqi
 *
 */
public class GameContext {
	
	/**
	 * The config data's version. 
	 * Every restart of server will change the VERSION. 
	 * Plus, the reload method will change it too.
	 */
	public static int VERSION = (int)(System.currentTimeMillis()/1000);

	private static final Logger logger = LoggerFactory.getLogger(GameContext.class);
		
	private static GameContext instance = new GameContext();
		
	private String zkRoot = "/config";
	
	//The message queue used to output messages to users.
	private MessageQueue messageQueue = MinaMessageQueue.getInstance();
		
	//Manage user's session.
	private SessionManager sessionManager = new SessionManager();
	
	//Manage Room
	private RoomManager roomManager = RoomManager.getInstance();
	
	//Script manager
	private ScriptManager scriptManager = ScriptManager.getInstance();
	
	//Battle manager
	private BattleManager battleManager = BattleManager.getInstance();
	
	//Map manager
	private MapManager mapManager = MapManager.getInstance();
	
	//Equip manager
	private EquipManager equipManager = EquipManager.getInstance();
	
	//Item manager
	private ItemManager itemManager = ItemManager.getInstance();
	
	//Shop manager
	private ShopManager shopManager = ShopManager.getInstance();
	
	//Task manager
	private TaskManager taskManager = TaskManager.getInstance();
	
	//DailyMark manager
	private DailyMarkManager dailyMarkManager = DailyMarkManager.getInstance();
	
	//BattleTool manager
//	private BattleToolManager battleToolManager = BattleToolManager.getInstance();
	
	//GameDataManager
	private GameDataManager gameDataManager = GameDataManager.getInstance();
	
	//Tip manager
	private TipManager tipManager = TipManager.getInstance();
	
	//Craft manager
	private CraftManager craftManager = CraftManager.getInstance();
	
	//The chat sending service.
	private ChatSender chatWorker = ChatSender.getInstance();
	
	//The chat manager
	private ChatManager chatManager = ChatManager.getInstance();
	
	//The user manager used to manage user's information.
	private UserManager userManager = UserManager.getInstance();
	
	//All login user will be stored here.
	private ConcurrentHashMap<SessionKey, User> loginUserMap = new 
			ConcurrentHashMap<SessionKey, User>();
	
	private String gameServerId = "localhost:3443";
	
	//The jndi resource context
	private JndiContext jndiContext = new JndiContext();
	
	private final HashMap<String, Service> rpcServiceMap = new HashMap<String, Service>();
	
	private final HashMap<String, MinaRpcPoolChannel> rpcPoolChannelMap = 
			new HashMap<String, MinaRpcPoolChannel>();
	
	private final HashMap<String, GameProxyClient> gameClientChannelMap = 
			new HashMap<String, GameProxyClient>();
	
	private static final ScheduledExecutorService scheduleTaskPool = 
			Executors.newScheduledThreadPool(Constant.CPU_CORES);
	
	private static final ExecutorService smallTaskPool = 
			Executors.newFixedThreadPool(Constant.CPU_CORES);
	
	private final LocaleThreadLocal localeThreadLocal = new LocaleThreadLocal();
	
	private GameContext() {
		try {
			String root = GlobalConfig.getInstance().getStringProperty("zookeeper.root");
			if ( root != null && root.length() > 0 ) {
				zkRoot = root.concat(zkRoot);
				if ( logger.isInfoEnabled() ) {
					logger.info("Use " + zkRoot + " as zookeepr root. ");
				}
			}
			/*
			ZooKeeperFacade facade = ZooKeeperFactory.getInstance();
			if ( facade != null ) {
				logger.info("ZooKeeper initialized ok.");
			} else {
				if ( logger.isWarnEnabled() ) {
					logger.warn("ZooKeeper is unavailable.");
				}
			}
			*/
			//Register JNDI
			System.setProperty(Context.INITIAL_CONTEXT_FACTORY, 
					JndiContextFactory.class.getName());
			
		} catch (Throwable e) {
			logger.error("Failed to create gameConfig", e);
		}
						
	}
	
	/**
	 * Intialized the GameContext
	 */
	public void initContext() {
		//reloadContext();
		messageQueue.initQueue();
		//Load map data.
		BattleDataLoader4Bitmap.loadBattleMaps();
		//Load bullets data
		BattleDataLoader4Bitmap.loadBattleBullet();
		//Clean Redis
		JedisUtil.cleanRedis();
		//Start RoomManager's check
		roomManager.init();
		battleManager.init();
		//Initialize mysql connection
		String database = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_database);
		String username = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_username);
		String password = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_password);
		String server = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_server);
		int maxConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_billing_max_conn);
		int minConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_billing_min_conn);
		String jndi = JndiContextKey.mysql_billing_db.name();
		MysqlUtil.init(database, username, password, server, maxConn, minConn, jndi);
		//Initialize discuz mysql connection
		database = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_discuz_database);
		username = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_discuz_username);
		password = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_discuz_password);
		server = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_discuz_server);
		maxConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_discuz_max_conn);
		minConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_discuz_min_conn);
		jndi = JndiContextKey.mysql_discuz_db.name();
		MysqlUtil.init(database, username, password, server, maxConn, minConn, jndi);
		if ( logger.isInfoEnabled() ) {
			logger.info("GameConfig#initContext OK");
		}
		//Register rpc service
		//this.registerRpcService(UserRefreshService.getInstance());
		//this.registerRpcService(RoomManager.getInstance());
		this.gameServerId = GlobalConfig.getInstance().getStringProperty(
				GlobalConfig.RUNTIME_GAME_SERVERID);
	}
	
	/**
	 * Destroy the GameContext
	 */
	public void destroyContext() {
		messageQueue.destroyQueue();
		chatWorker.stopWorkers();
		//Clean Redis
		JedisUtil.cleanRedis();
		//Shutdown the schedule threads.
		scheduleTaskPool.shutdown();
		roomManager.destroy();
		battleManager.destroy();
		//Destroy mysql
		MysqlUtil.destroy();
		if ( logger.isInfoEnabled() ) {
			logger.info("GameConfig#destroyContext OK");
		}
	}
	
	/**
	 * Reload all the context settings.
	 */
	public void reloadContext() {
		try {
//			battleToolManager.reload();
			dailyMarkManager.reload();
			equipManager.reload();
			gameDataManager.reload();
			itemManager.reload();
			mapManager.reload();
			shopManager.reload();
			taskManager.reload();
			tipManager.reload();
			//Change the version
			VERSION++;
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
		
	}
	
	/**
	 * This is a singleton class and need to create from factory method.
	 * @return
	 */
	public static GameContext getInstance() {
		return instance;
	}
	
	/**
	 * This is only for Junit test purpose. DO NOT CALL IT.
	 * @return
	 */
	public static GameContext getTestInstance() {
		instance = new GameContext();
		return instance;
	}
	
	/**
	 * Get the JNDI context object.
	 * @return
	 */
	public Context getJndiContext() {
		return jndiContext;
	}
	
	// ------------------------------------------- Business logic
	
	
//	/**
//	 * This is a helper method to get the EquipPojo from global setting.
//	 * The 'variable' is a abtest key. It can be null for default
//	 * 
//	 * @param variable abtestkey
//	 * @return An EquipPojo object or null.
//	 */
//	public EquipPojo getEquipPojo(String variable) {
//		List<Pojo> pojos = getPojo(EQUIP_CONFIG_URI, variable);
//		EquipPojo equipPojo = null;
//		if ( pojos != null ) {
//			if ( pojos.size() == 1 ) {
//				equipPojo = (EquipPojo)pojos.get(0);
//			} else {
//				logger.warn("There should one EquipPojo in system.");
//			}
//		}
//		return equipPojo;
//	}
	
	/**
	 * This is a helper method to get the EquipPojo from global setting.
	 * The 'variable' is a abtest key. It can be null for default
	 * 
	 * @param variable abtestkey
	 * @return An EquipPojo object or null.
	 */
//	public List<Pojo> getMapPojo(String variable) {
//		List<Pojo> pojos = getPojo(MAP_CONFIG_URI, variable);
//		return pojos;
//	}
	
//	/**
//	 * This is a helper method to get the ItemsPojo from global setting.
//	 * The 'variable' is a abtest key. It can be null for default
//	 * 
//	 * @param variable abtestkey
//	 * @return An EquipPojo object or null.
//	 */
//	public ItemsPojo getItemsPojo(String variable) {
//		List<Pojo> pojos = getPojo(ITEM_CONFIG_URI, variable);
//		ItemsPojo itemsPojo = null;
//		if ( pojos != null ) {
//			if ( pojos.size() == 1 ) {
//				itemsPojo = (ItemsPojo)pojos.get(0);
//			} else {
//				logger.warn("There should one ItemsPojo in system.");
//			}
//		}
//		return itemsPojo;
//	}
	
	/**
	 * Get the UserManager. It is a singleton.
	 * @return
	 */
	public UserManager getUserManager() {
		return userManager;
	}
	
	/**
	 * Get the RoomManager instance. It is a singleton.
	 * @return
	 */
	public RoomManager getRoomManager() {
		return roomManager;
	}
	
	/**
	 * Get the ScriptManager instance. It is a singleton.
	 * @return
	 */
	public ScriptManager getScriptManager() {
		return scriptManager;
	}
	
	/**
	 * Get the BattleManager instance. It is a singleton.
	 * @return
	 */
	public BattleManager getBattleManager() {
		return battleManager;
	}
	
	/**
	 * Get the GameDataManager.
	 * @return
	 */
	public GameDataManager getGameDataManager() {
		return GameDataManager.getInstance();
	}
	
	/**
	 * Get the equipment manager
	 * @return
	 */
	public EquipManager getEquipManager() {
		return equipManager;
	}
	
	/**
	 * Get the map manager
	 * @return
	 */
	public MapManager getMapManager() {
		return mapManager;
	}
	
	/**
	 * Get the item manager.
	 * @return
	 */
	public ItemManager getItemManager() {
		return itemManager;
	}
	
	/**
	 * Get the shop manager.
	 * @return
	 */
	public ShopManager getShopManager() {
		return shopManager;
	}
	
	/**
	 * Get the task manager.
	 * @return
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}
	
	/**
	 * Get the daily mark manager
	 * @return
	 */
	public DailyMarkManager getDailyMarkManager() {
		return dailyMarkManager;
	}
	
	/**
	 * Get the tip manager.
	 * @return
	 */
	public TipManager getTipManager() {
		return tipManager;
	}
	
	/**
	 * Get the craft manager
	 * @return
	 */
	public CraftManager getCraftManager() {
		return craftManager;
	}
	
	/**
	 * Get the chat manager
	 * @return
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}
	
	/**
	 * Get the session manager
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}
	
//	/**
//	 * Internal method to get a Pojo
//	 * @param uri
//	 * @param variable
//	 * @return
//	 */
//	private List<Pojo> getPojo(String uri, String variable) {
//		GameConfig gameConfig = gameConfigMap.get(uri);
//		if ( gameConfig == null ) {
//			logger.warn("Equip gameConfig is null. System is not initialized properly.");
//			return null;
//		}
//		BaseManager baseManager = gameConfig.getBaseManager(variable);
//		if ( baseManager == null ) {
//			logger.warn("BaseManager is null. System is not initialized properly");
//			return null;
//		}
//		List<Pojo> pojos = baseManager.getCachedPojos();
//		return pojos;
//	}
	
	/**
	 * Find a machineId matching the given sessionKey
	 * @param sessionKey
	 * @return
	 */
	public String findMachineId(SessionKey sessionKey) {
		String machineId = sessionManager.findSessionMachineId(sessionKey);
		return machineId;
	}
	
	/**
	 * Find a machineId matching the given sessionKey
	 * @param sessionKey
	 * @return
	 */
	public String findMachineId(UserId userId) {
		String machineId = sessionManager.findUserMachineId(userId);
		return machineId;
	}
	
	/**
	 * Find an UserId matching the given sessionKey
	 * @param sessionKey
	 * @return
	 */
	public UserId findUserIdBySessionKey(SessionKey sessionKey) {
		User user = loginUserMap.get(sessionKey);
		if ( user != null ) {
			return user.get_id();
		} else {
			UserId userId = sessionManager.findUserIdBySessionKey(sessionKey);
			return userId;
		}
	}
	
	/**
	 * Retrieve the SessionKey from IoSession attribute. It may be null
	 * if user has not login or the connection is broken.
	 *
	 * The method SHOULD NOT BE CALLED outside, because the IoSession
	 * may not be mapping to an unique user in AI or proxy protocol
	 * 
	 * @param session
	 * @return
	 */
	SessionKey findSessionKeyByIoSession(IoSession session) {
		return (SessionKey)session.getAttribute(Constant.SESSION_KEY);
	}
	
	/**
	 * Find User object according to a IoSession. It may be null.
	 * 
	 * The method SHOULD NOT BE CALLED outside, because the IoSession
	 * may not be mapping to an unique user in AI or proxy protocol
	 * 
	 * 
	 * @param session
	 * @return
	 */
	User findLocalUserByIoSession(IoSession session) {
		return (User)session.getAttribute(Constant.USER_KEY);
	}
	
	/**
	 * It find all users currently online, including those
	 * exist in other JVM servers.
	 * 
	 * @return
	 */
	public List<SessionKey> findAllOnlineUsers() {
		return sessionManager.findAllOnlineUsers(-1);
	}
	
	/**
	 * Find the session key in current JVM server.
	 * @param sessionKey
	 * @return
	 */
	public IoSession findLocalUserIoSession(SessionKey sessionKey) {
		User user = loginUserMap.get(sessionKey);
		if ( user != null ) {
			return user.getSession();
		}
		return null;
	}
	
	/**
	 * Find the User by its sessionKey in the local JVM.
	 * Note: it does not use Redis so it cannot find remote JVM 
	 * user.
	 * 
	 * Note: When the user's socket is closed abnormaly, his sessionKey
	 * may be valid in Redis for a short period of time. However, his
	 * User object in 'loginUserMap' will be removed by {@link #deregisterUserByIoSession(IoSession)}
	 * So the returned User maybe null.
	 * 
	 * @param sessionKey
	 * @return
	 */
	public User findLocalUserBySessionKey(SessionKey sessionKey) {
		if ( sessionKey == null ) return null;
	  User user = loginUserMap.get(sessionKey);
	  return user;
	}
	
	/**
	 * Update the local user object
	 * @param sessionKey
	 * @return
	 */
	public User updateLocalUserBySessionKey(SessionKey sessionKey, User user) {
		if ( sessionKey == null || user == null ) return null;
	  loginUserMap.put(sessionKey, user);
	  return user;
	}
		
	/**
	 * Find the given userid's sessionkey if he/she is online.
	 * @param sessionKey
	 * @return
	 */
	public SessionKey findSessionKeyByUserId(UserId userId) {
		return sessionManager.findSessionKeyByUserId(userId);
	}
		
	/**
	 * Register a login user with his/her session.
	 * The session key will be stored in database.
	 * @param session
	 * @param user
	 */
	public void registerUserSession(IoSession session, User user, SessionKey existSessionKey) {
		SessionKey sessionKey = sessionManager.registerSession(session, user, existSessionKey);
		if ( !user.isProxy() ) {
			session.setAttribute(Constant.USER_KEY, user);
		}
		if ( sessionKey != null ) {
			loginUserMap.put(sessionKey, user);
		}
	}
	
	/**
	 * Deregister an user from game server.
	 * The session key will be removed from database.
	 * 
	 * For AI and proxy based session, the behaviour is different, 
	 * because one IoSession will bear many users' session.
	 * 
	 * @param session
	 * @param user
	 */
	public void deregisterUserByIoSession(IoSession session) {
		SessionKey sessionKey = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
		session.removeAttribute(Constant.USER_KEY);
		deregisterUserBySessionKey(sessionKey);
		
		ArrayList<SessionKey> proxySessionKeys = (ArrayList<SessionKey>)session.getAttribute(
				SessionManager.PROXY_SESSION_KEYS);
		if ( proxySessionKeys != null ) {
			ArrayList<SessionKey> tempList = new ArrayList<SessionKey>(proxySessionKeys);
			for ( SessionKey proxySessionKey: tempList ) {
				deregisterUserBySessionKey(proxySessionKey);
			}
		}
	}
	
	/**
	 * Deregister an user from game server by its global session key.
	 * @param sessionKey
	 */
	public void deregisterUserBySessionKey(SessionKey sessionKey) {
		if ( sessionKey != null ) {
			User user = this.findLocalUserBySessionKey(sessionKey);
			loginUserMap.remove(sessionKey);
			if ( user != null ) {
				GuildManager.getInstance().markGuildMemberOnline(user, false, false);
				//update user's online status
				if ( !user.isProxy() ) {
					LoginManager.getInstance().sendFriendStatus(user, false);
				}
				sessionManager.deregisterSession(user.getSession(), user);
				//Update user stat 
				//change the minute to second
				int milliSeconds = (int)(System.currentTimeMillis() - user.getLdate().getTime());
				int seconds = milliSeconds / 1000;
				user.setTotalmin(user.getTotalmin()+seconds);
				if ( !user.isAI() && !user.isProxy() ) {
					this.userManager.saveUser(user, false);
					logger.debug("User {} total seconds in game is: {}", user.getRoleName(), user.getTotalmin());
					
					StatClient.getIntance().sendDataToStatServer(user, 
							StatAction.OnlineSecond, seconds);
					
					ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_ONLINE, user, seconds);
				}
				/*
				try {
					byte[] bytes = UserManager.getInstance().serializeUser(user);
					FileOutputStream fos = new FileOutputStream("user.bin");
					fos.write(bytes);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
				user = null;
			} else {
				logger.warn("Cannot find the user object by sessionKey", sessionKey);
			} 
		}
	}

	/**
	 * Write the message back to client.
	 * @param session
	 * @param message
	 */
	public void writeResponse(IoSession session, Object message, SessionKey sessionKey) {
		Stat.getInstance().gameServerFinished++;
		messageQueue.sessionWrite(session, message, sessionKey);
	}
	
	/**
	 * Write the message back to client.
	 * @param session
	 * @param message
	 */
	public void writeResponse(SessionKey sessionKey, Object message) {
		Stat.getInstance().gameServerFinished++;
		messageQueue.sessionWrite(sessionKey, message);
	}
	
	/**
	 * Register a remote RPC service to system.
	 * @param service
	 */
	public void registerRpcService(Service service) {
		String serviceName = service.getDescriptorForType().getFullName();
		rpcServiceMap.put(serviceName, service);
//		List<MethodDescriptor> methods = service.getDescriptorForType().getMethods();
		if ( logger.isInfoEnabled() ) {
			logger.info("register remote service: {}", serviceName);
		}
//		for ( MethodDescriptor method : methods ) {
//			String methodName = method.getName();
//			String remoteServiceName = StringUtil.concat(serviceName, DOT, methodName);
//			rpcServiceMap.put(remoteServiceName, service);
//			if ( log.isInfoEnabled() ) {
//				log.info("register remote service: " + remoteServiceName );
//			}
//		}
	}
	
	/**
	 * Find a Service for a remote method call.
	 * @param serviceName
	 * @param methodName
	 * @return
	 */
	public Service findRpcService(String serviceName, String methodName) {
		return rpcServiceMap.get(serviceName);
	}
	
	/**
	 * Get the MinaRpcPoolChannel from the given rpcServerId.
	 * If it does not exist, gameContext will create a new one and return.
	 * @param rpcServerId
	 * @return
	 */
	public MinaRpcPoolChannel findRpcChannel(String rpcServerId) {
		MinaRpcPoolChannel channel = rpcPoolChannelMap.get(rpcServerId);
		if ( channel == null ) {
			String[] results = splitMachineId(rpcServerId);
			if ( results != null ) {
				channel = new MinaRpcPoolChannel(
						results[0], toInt(results[1], 3445), Constant.CPU_CORES);
			}
			rpcPoolChannelMap.put(rpcServerId, channel);
		}
		return channel;
	}
	
	/**
	 * Get the MinaRpcPoolChannel from the given rpcServerId.
	 * If it does not exist, gameContext will create a new one and return.
	 * @param rpcServerId
	 * @return
	 */
	public GameProxyClient findGameProxyChannel(String gameServerId) {
		if ( this.gameServerId.equals(gameServerId) ) {
			return null;
		}
		GameProxyClient channel = gameClientChannelMap.get(gameServerId);
		if ( channel == null ) {
			String[] results = splitMachineId(gameServerId);
			if ( results != null ) {
				channel = new GameProxyClient(
						results[0], toInt(results[1], 3443), Constant.CPU_CORES);
			}
			gameClientChannelMap.put(gameServerId, channel);
		}
		return channel;
	}
	
	/**
	 * Get local game serverid.
	 * @return
	 */
	public String getGameServerId() {
		return gameServerId;
	}
	
	/**
	 * Set the local game serverid
	 * @param gameServerId
	 */
	public void setGameServerId(String gameServerId) {
		this.gameServerId = gameServerId;
	}
	
	/**
	 * Get a user object from userInfo
	 * @param userInfo
	 * @return
	 */
	public final User findGlobalUserBySessionKey(SessionKey sessionKey) {
		User user = this.findLocalUserBySessionKey(sessionKey);
		if ( user == null ) {
			UserId userId = this.findUserIdBySessionKey(sessionKey);
			if ( userId != null ) {
				user = this.getUserManager().queryUser(userId);
				if ( user != null ) {
					boolean isAI = AIManager.getInstance().isAIUser(sessionKey);
					user.setAI( isAI );
					if ( !isAI ) {
						String gameserverId = this.sessionManager.findUserGameServerId(sessionKey);
						user.setGameserverId(gameserverId);
						this.getUserManager().queryUserBag(user);
						this.getUserManager().queryUserUnlock(user);
						this.getUserManager().queryUserRelation(user);
						TaskManager.getInstance().getUserLoginTasks(user);						
					}
				} else {
					logger.warn("#findGlobalUserBySessionKey: Cannot find user by sessionKey:{}", sessionKey);
				}
				return user;
			}
		}
		if ( user == null ) {
			logger.warn("#findGlobalUserBySessionKey: Cannot find user by sessionKey:{}", sessionKey);
		}
		return user;
	}
	
	/**
	 * Proxy the request to remote game server.
	 * @param userSessionKey
	 * @param pbMessage
	 */
	public final boolean proxyToRemoteGameServer(SessionKey userSessionKey, String gameServerId, Message pbMessage) {
		if ( gameServerId != null ) {
			if ( gameServerId.equals(this.gameServerId) ) {
				logger.debug("Prevent send message to myself {}", gameServerId);
				return false;
			} else {
				logger.debug("User {} proxyToRemoteGameServer to gameserver: {}", userSessionKey, gameServerId);
				XinqiProxyMessage proxy = new XinqiProxyMessage();
				proxy.userSessionKey = userSessionKey;
				XinqiMessage xinqi = new XinqiMessage();
				xinqi.payload = pbMessage;
				proxy.xinqi = xinqi;
	
				GameProxyClient client = GameContext.getInstance().findGameProxyChannel(gameServerId);
				if ( client != null ) {
					client.sendMessageToServer(proxy);
				}
				return true;
			}
		} else {
			logger.info("#proxyToRemoteGameServer: the user {}'s gameServerId is null", userSessionKey);
		}
		return false;
	}
	
	/**
	 * Schedule a task for future run.
	 * 
	 * @param command
	 * @param delay
	 * @param unit
	 */
	public final void scheduleTask(Runnable command, long delay, TimeUnit unit) {
		ScheduleWorker worker = new ScheduleWorker(command);
		ScheduledFuture<?> future = this.scheduleTaskPool.schedule(worker, delay, unit);
	}
	
	/**
	 * Run a small task in separate thread.
	 * @param command
	 */
	public final void runSmallTask(Runnable command) {
		this.smallTaskPool.execute(command);
	}
	
	/**
	 * Force user to go back homepage
	 */
	public final void forceUserToHome(SessionKey userSessionKey) {
		UserId userId = this.findUserIdBySessionKey(userSessionKey);
		if (userId !=null) {
			XinqiMessage response = new XinqiMessage();
			XinqiBseInit.BseInit.Builder builder = XinqiBseInit.BseInit.newBuilder();
			String newToken = CipherManager.getInstance().generateEncryptedUserToken(userId);
			builder.setSuccess(true);
			builder.setToken(newToken);
			builder.setRefresh(true);
			response.payload = builder.build();
			GameContext.getInstance().writeResponse(userSessionKey, response);
		} else {
			XinqiMessage response = new XinqiMessage();
			XinqiBseInit.BseInit.Builder builder = XinqiBseInit.BseInit.newBuilder();
			builder.setSuccess(false);
			builder.setRefresh(true);
			response.payload = builder.build();
			GameContext.getInstance().writeResponse(userSessionKey, response);
		}
	}
	
	/**
	 * @return the localeThreadLocal
	 */
	public LocaleThreadLocal getLocaleThreadLocal() {
		return localeThreadLocal;
	}

	public static class ScheduleWorker implements Runnable {
		private Runnable command = null;
		public ScheduleWorker(Runnable command) {
			this.command = command;
		}
		public void run() {
			try {
				command.run();
			} catch (Exception e) {
				logger.warn("Failed to execute the schedule task", e);
			}
		}
	}
	
	// -------------------------------------------------- Evalution implementation

}
