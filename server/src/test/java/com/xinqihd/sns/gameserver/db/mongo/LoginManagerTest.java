package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.mina.core.session.IoSession;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.CurrencyUnit;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.BseShop;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class LoginManagerTest {

	@Before
	public void setUp() throws Exception {
		String database = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database);
		String namespace = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace);
		MongoDBUtil.deleteFromMongo(MongoDBUtil.createDBObject(), database, namespace, "logins", true);
		JedisUtil.deleteAllKeys();
		UserManager.getInstance().removeUser("test-001");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetAndSaveClientVersion() {
		LoginManager manager = LoginManager.getInstance();
		int major = manager.getClientMajorVersion();
		int minor = manager.getClientMinorVersion();
		assertEquals(0, major);
		assertEquals(0, minor);
		
		manager.setClientVersion(1, 1, 1);
		manager.saveClientVersion();
		
		manager.reload();
		
		assertEquals(1, manager.getClientMajorVersion());
		assertEquals(1, manager.getClientMinorVersion());		
		
		manager.setClientVersion(0, 0, 0);
		manager.saveClientVersion();
	}

	@Test
	public void testCheckClientVersion() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		manager.setClientVersion(1, 1, 1);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		manager.login("test", "test", 0, 0, 0, session, null);
		
		Thread.sleep(200);
		
		XinqiMessage login = list.get(0);
		BseLogin bseLogin = (BseLogin)login.payload;
		assertEquals(6, bseLogin.getCode());
		assertEquals("您的客户端当前版本为0.0，需要升级到版本1.1后才能继续游戏", bseLogin.getDesc());
		
		manager.setClientVersion(0, 0, 0);
		manager.saveClientVersion();
	}
	
	@Test
	public void testCheckLoginStatus() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "000000";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		
		//Change the status
		user.setLoginStatus(UserLoginStatus.REMOVED);
		user.setLoginStatusDesc("因为使用黑卡");
		UserManager.getInstance().saveUser(user, false);
		
		//Login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.S_REMOVED.ordinal(), login.getCode());
		System.out.println(login.getDesc());
	}
	
	@Test
	public void testCheckLoginWithToken() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		//user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
	}
	
	@Test
	public void testCheckLoginStatusPause() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "000000";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		
		//Change the status to pause
		manager.pauseUserAccount(user, "因为使用黑卡,", 1000);
		UserManager.getInstance().saveUser(user, true);
		
		String key = manager.getUserPauseKey(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long ttl = jedisDB.ttl(key);
		assertNotNull(ttl);
		assertTrue(ttl.toString()+">100", ttl.intValue() > 100);
		
		//try to login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.S_PAUSE.ordinal(), login.getCode());
		System.out.println(login.getDesc());
	}
	
	@Test
	public void testCheckLoginStatusPauseToNormal() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "000000";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		
		//Change the status to pause
		manager.pauseUserAccount(user, "因为使用黑卡,", 1);
		UserManager.getInstance().saveUser(user, true);
		
		String key = manager.getUserPauseKey(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long ttl = jedisDB.ttl(key);
		assertNotNull(ttl);
		assertTrue(ttl.toString()+">100", ttl.intValue() > 0);
		
		//try to login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.S_PAUSE.ordinal(), login.getCode());
		System.out.println(login.getDesc());

		//Wait for 1 seconds
		Thread.sleep(1000);
		//try to login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		System.out.println(login.getDesc());
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		assertEquals(UserLoginStatus.NORMAL, actualUser.getLoginStatus());
	}
	
	@Test
	public void testCheckLoginStatusPauseWithoutTTLToNormal() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "000000";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		
		//Change the status to pause
		user.setLoginStatus(UserLoginStatus.PAUSE);
		user.setLoginStatusDesc(UserLoginStatus.PAUSE.name());
		UserManager.getInstance().saveUser(user, true);
		
		String key = manager.getUserPauseKey(userName);
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long ttl = jedisDB.ttl(key);
		assertNotNull(ttl);
		assertTrue(ttl.toString()+"<0", ttl.intValue() < 0);
		
		//try to login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		System.out.println(login.getDesc());
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		assertEquals(UserLoginStatus.NORMAL, actualUser.getLoginStatus());
	}
	
	@Test
	public void testConvertSecondsToDesc() {
		int seconds = 59;
		String desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
		
		seconds = 159;
		desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
		
		seconds = 7259;
		desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
		
		seconds = 86400*3;
		desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
		
		seconds = 86400*8;
		desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
		
		seconds = 86400*31;
		desc = LoginManager.getInstance().convertSecondToDesc(seconds);
		System.out.println(desc);
	}
	
	@Test
	public void testRandomUserName() {
		String userName = LoginManager.getInstance().getRandomUserName();
		System.out.println(userName+", len:"+userName.length());
	}
	
	@Test
	public void testCheckLoginWithPauseStatus() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		String userName = "test-001";
		String password = "test-001";
		
		UserId userId = new UserId(userName);
		User user = new User();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword(manager.encryptPassword(password));
		user.setLoginStatus(UserLoginStatus.NORMAL);
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login(userName, password, 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		
		//Change the status
		user.setLoginStatus(UserLoginStatus.REMOVED);
		user.setLoginStatusDesc("因为使用黑卡");
		UserManager.getInstance().saveUser(user, false);
		
		//Login again
		list.clear();
		manager.login(userName, password, 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.S_REMOVED.ordinal(), login.getCode());
		System.out.println(login.getDesc());
	}
	
	@Test
	public void testCheckLoginWithTutorial() throws Exception {
		LoginManager manager = LoginManager.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		manager.login("", "", 0, 0, 0, session, null);
		
		Thread.sleep(200);
		XinqiMessage xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		BseLogin login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		String token = login.getToken();
		boolean isTutorial = login.getTutorial();
		assertNotNull(token);
		assertTrue(isTutorial);
		User user = UserManager.getInstance().queryUser(token);
		assertEquals(true, user.isGuest());
		
		//Login again
		list.clear();
		manager.login(token, "", 0, 0, 0, session, null);
		xinqi = null;
		for ( XinqiMessage xm : list) {
			if ( xm.payload instanceof BseLogin ) {
				xinqi = xm;
			}
		}
		assertNotNull(xinqi);
		login = (BseLogin)xinqi.payload;
		assertEquals(ErrorCode.SUCCESS.ordinal(), login.getCode());
		assertTrue(!login.getTutorial());
		
	}
	
	/**
	 * Run randomUserName for 100000. Time:38738, Heap:69.17774M
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRandomUserNamePressure() throws Exception {
		TestUtil.doPerform(new Runnable() {
			public void run() {
				String userName = LoginManager.getInstance().getRandomUserName();
				//System.out.println(userName+", len:"+userName.length());				
			}
		}, "randomUserName", 10);
	}
	
	/**
	 * Run randomRoleName for 10000. Time:3447, Heap:69.00551M
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRandomRoleNamePressure() throws Exception {
		TestUtil.doPerform(new Runnable() {
			public void run() {
				String userName = LoginManager.getInstance().getRandomRoleName();
				//System.out.println(userName+", len:"+userName.length());				
			}
		}, "randomRoleName", 10000);
	}
	
	static int loginCounter = 0;
	@Test
	public void testLoginPerformance() throws Exception {
		final LoginManager manager = LoginManager.getInstance();
		final IoSession session = TestUtil.createIoSession();
		EasyMock.replay(session);
		
		TestUtil.doPerform(new Runnable() {
			public void run() {
				String suf = String.valueOf(loginCounter++);
				StringBuilder buf = new StringBuilder(5);
				buf.append("t");
				for ( int i=0; i<4-suf.length(); i++ ) {
					buf.append('0');
				}
				buf.append(suf);
				String userName = suf.toString();
				String password = "000000";
		
				manager.login(userName, password, 0, 0, 0, session, null);
				
			}
		}, "login", 1000000);
		
		Thread.sleep(100000);
	}
	
	@Test
	public void outputConfigToFile() throws Exception{
		Logger logger = LoggerFactory.getLogger(LoginManagerTest.class);
		logger.debug("Config version changed. Send data again.");
		
		// 0.0 装备的基本数据
//		XinqiMessage equipData = new XinqiMessage();
//		equipData.payload = GameContext.getInstance().getEquipManager().toBseEquipment();
//		GameContext.getInstance().writeResponse(user.getSessionKey(), equipData);
		// Send as zip format
		
		String database = "babywar", namespace="server0001", collection="equipments_new";
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(
				null, database, namespace, collection, null, null);
		StringBuilder luaBuffer = new StringBuilder(10000);
		luaBuffer.append("weapons = {\n");
		for ( DBObject obj : list ) {
			WeaponPojo weapon = (WeaponPojo)MongoDBUtil.constructObject(obj);
			luaBuffer.append(weapon.toLuaString(Locale.CHINESE));
		}
		luaBuffer.append("}\n");
		FileOutputStream fos = new FileOutputStream("BseEquipment.txt");
		fos.write(luaBuffer.toString().getBytes());
		fos.close();
		
		// 0.1 成就数据
		XinqiMessage achievements = new XinqiMessage();
		achievements.payload = TaskManager.getInstance().toBseAchievement();
		fos = new FileOutputStream("BseAchievements.txt");
		fos.write(achievements.payload.toByteArray());
		fos.close();
		
		// 0.2 地图基础数据 
		XinqiMessage mapData = new XinqiMessage();
		mapData.payload = GameContext.getInstance().getMapManager().toBseMap();
		fos = new FileOutputStream("BseMap.txt");
		fos.write(mapData.payload.toByteArray());
		fos.close();
					
		// 0.3 每日打卡奖励
		XinqiMessage dailyMark = new XinqiMessage();
		dailyMark.payload = GameContext.getInstance().getDailyMarkManager().toBseDailyMark();
		fos = new FileOutputStream("BseDailyMark.txt");
		fos.write(dailyMark.payload.toByteArray());
		fos.close();

		// 0.4 游戏提示
		XinqiMessage tip = new XinqiMessage();
		tip.payload = GameContext.getInstance().getTipManager().toBseTip(null);
		fos = new FileOutputStream("BseTip.txt");
		fos.write(tip.payload.toByteArray());
		fos.close();

		// 0.5 游戏任务
		list = MongoDBUtil.queryAllFromMongo(null, database, namespace, 
				"tasks", null);
		StringBuilder buf = new StringBuilder(10000);
		buf.append("tasks= {\n");
		for ( DBObject obj : list ) {
			TaskPojo task = (TaskPojo)MongoDBUtil.constructObject(obj);
			if ( StringUtil.checkNotEmpty(task.getScript()) ) {
				buf.append(task.toLuaString(Locale.CHINESE));
			}
		}
		buf.append("}\n");
		fos = new FileOutputStream("BseTask.txt");
		fos.write(buf.toString().getBytes());
		fos.close();
		
		// 0.6 商城数据
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		BseShop.Builder builder = BseShop.newBuilder();
		for ( ShopPojo shopPojo : shops ) {
			if ( shopPojo.getLevel() == -1 || 
					(shopPojo.getLevel() >= 0 && shopPojo.getLevel() < 10) ) {
				builder.addShops(shopPojo.toShopData());
			}
		}
		fos = new FileOutputStream("BseShop.txt");
		fos.write(builder.build().toByteArray());
		fos.close();
		
		// 0.7 游戏可配置参数
		XinqiMessage gameData = new XinqiMessage();
		gameData.payload = GameContext.getInstance().getGameDataManager().toBseGameDataKey(1);
		fos = new FileOutputStream("BseGameDataKey.txt");
		fos.write(gameData.payload.toByteArray());
		fos.close();
		
		// 0.8 游戏物品数据
		XinqiMessage itemData = new XinqiMessage();
		itemData.payload = GameContext.getInstance().getItemManager().toBseItem();
		fos = new FileOutputStream("BseItem.txt");
		fos.write(itemData.payload.toByteArray());
		fos.close();
		
		// 0.9 充值数据
		XinqiMessage chargeData = new XinqiMessage();
		chargeData.payload = ChargeManager.getInstance().toBseChargeList("ios_iap");
		fos = new FileOutputStream("BseChargeList.txt");
		fos.write(chargeData.payload.toByteArray());
		fos.close();
		
	}
}
