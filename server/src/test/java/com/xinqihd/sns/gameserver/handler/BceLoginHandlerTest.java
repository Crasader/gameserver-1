package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.Message;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.ErrorCode;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister.BceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.BseChargeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.ChargeData;
import com.xinqihd.sns.gameserver.proto.XinqiBseConfigData.BseConfigData;
import com.xinqihd.sns.gameserver.proto.XinqiBseLogin.BseLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap.BseMap;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.BseShop;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.ShopData;
import com.xinqihd.sns.gameserver.proto.XinqiBseTaskList.BseTaskList;
import com.xinqihd.sns.gameserver.session.MessageQueue;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.IdToMessage;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;
import com.xinqihd.sns.gameserver.util.Text;

public class BceLoginHandlerTest extends AbstractHandlerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_RPC_SERVERID, "localhost:0");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
		BattleDataLoader4Bitmap.loadBattleMaps();
		BattleDataLoader4Bitmap.loadBattleBullet();
		super.setUp(false, "users", Constant.LOGIN_USERNAME);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testSessionNotFoundUser() throws Exception {
		String userName = randomUserName();
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		IoSession session = createMock(IoSession.class);
		
		expect(session.write(anyObject())).andAnswer(new IAnswer<WriteFuture>(){
			@Override
			public WriteFuture answer() throws Throwable {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				BseLogin login = (BseLogin)response.payload;
				assertEquals(ErrorCode.NOTFOUND.ordinal(), login.getCode());
				return null;
			}
		}).times(1);

		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	@Test
	public void testNotFound() throws Exception {
		GameContext.getTestInstance();
		String userName = randomUserName();
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000001");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
		
		Thread.sleep(500);
		
		assertEquals(1, list.size());
		XinqiMessage response = (XinqiMessage)list.get(0);
		BseLogin login = (BseLogin)response.payload;
		assertEquals(ErrorCode.NOTFOUND.ordinal(), login.getCode());
		
		verify(session);
	}
	
	@Test
	public void testWrongPassword() throws Exception {
		GameContext.getTestInstance();
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000001");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
		
		Thread.sleep(500);
		
		assertEquals(1, list.size());
		XinqiMessage response = (XinqiMessage)list.get(0);
		BseLogin login = (BseLogin)response.payload;
		assertEquals(ErrorCode.WRONGPASS.ordinal(), login.getCode());
		
		verify(session);
	}

	@Test
	public void testUserLogin() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
		
		Thread.sleep(200);
		Set<String> classList = new HashSet<String>(16);
		int i = 0;
		for ( XinqiMessage xinqi : list ) {
			classList.add(xinqi.payload.getClass().getName());
			System.out.println(
					"Type: " + xinqi.type + "[" 
							+ IdToMessage.idToMessage(xinqi.type)
					+"], Payload Class: " + xinqi.payload.getClass()
					+ ", Length: " + xinqi.payload.toByteArray().length);
		}
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseInit$BseInit"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseLogin$BseLogin"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo$BseRoleBattleInfo"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig$BseRoleConfig"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo$BseRoleInfo"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTaskList$BseTaskList"));
		
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList$BseDailyMarkList"));
		//assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseEquipment$BseEquipment"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseGameDataKey$BseGameDataKey"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseMap$BseMap"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseShop$BseShop"));
		//assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTask$BseTask"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTip$BseTip"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseItem$BseItem"));
	}
	
	@Test
	public void testGuestUserLogin() throws Exception {
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername("");
		payload.setPassword("");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		//Register as a guest user
		handler.messageProcess(session, message, null);
		
		Thread.sleep(200);
		Set<String> classList = new HashSet<String>(16);
		int i = 0;
		String token = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseLogin ) {
				token = ((BseLogin)xinqi.payload).getToken();
			}
		}
		System.out.println("token="+token);
		assertNotNull(token);
		
		list.clear();
		
		//Now register
		LoginManager manager = LoginManager.getInstance();
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setPassword(manager.encryptPassword("123456"));
		UserManager.getInstance().saveUser(user, true);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		//Make the guest user online
		User guest = new User();
		guest.set_id(new UserId(token));
		guest.setUsername(token);
		GameContext.getInstance().registerUserSession(session, guest, user.getSessionKey());
		
		//pre check
		boolean success = UserManager.getInstance().checkUserNameExist(token);
		assertTrue(success);
		
		//Now login as new user.
		payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername("test-001");
		payload.setPassword("123456");
		payload.setVersion(Integer.MAX_VALUE);
		msg = payload.build();
		message.payload = msg;
		handler.messageProcess(session, message, user.getSessionKey());
		
		success = UserManager.getInstance().checkUserNameExist(token);
		assertTrue(!success);
	}
	
	@Test
	public void testUserLoginWithVersion() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
		
		Set<String> classList = new HashSet<String>(16);
		int i = 0;
		int version = 0;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseConfigData ) {
				version = ((BseConfigData)xinqi.payload).getVersion();
			}
			classList.add(xinqi.payload.getClass().getName());
		}
		assertTrue(version > 0 );
		assertTrue(classList.size()>=19);
		
		//Again with version
		payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		payload.setVersion(version);
		msg = payload.build();
		message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		list.clear();
		classList.clear();
		handler.messageProcess(session, message, null);
		for ( XinqiMessage xinqi : list ) {
			classList.add(xinqi.payload.getClass().getName());
		}
		assertTrue(classList.size()>=9);
	}
	
	@Test
	public void testUserLoginVip() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		List<Message> list = loginUser2(userName);
		
		ShopData shopData = (ShopData)list.get(0);
		ChargeData chargeData = (ChargeData)list.get(1);
		int shopPrice = shopData.getBuyPrices(0).getPrice();
		int shopDiscount = shopData.getDiscount();
		System.out.println("shopPrice = " + shopPrice+", shopDiscount = " + shopDiscount);
		int chargePrice = chargeData.getPrice();
		int chargeDiscount = chargeData.getDiscount();
		int yuanbao = chargeData.getYuanbao();
		System.out.println("chargePrice = " + chargePrice + 
				", chargeDiscount="+chargeDiscount+", yuanbao="+yuanbao);
		
		
		//Make the user VIP
		User user = UserManager.getInstance().queryUser(userName);
		user.setIsvip(true);
		user.setVipedate(new Date(System.currentTimeMillis()+86400));
		UserManager.getInstance().saveUser(user, false);
		
		list = loginUser2(userName);
		shopData = (ShopData)list.get(0);
		chargeData = (ChargeData)list.get(1);
		int newShopPrice = shopData.getBuyPrices(0).getPrice();
		int newShopDiscount = shopData.getDiscount();
		System.out.println("shopPrice = " + newShopPrice+", shopDiscount = " + newShopDiscount);
		assertEquals( shopPrice, newShopPrice);
		assertTrue( newShopDiscount < shopDiscount );
		int newChargePrice = chargeData.getPrice();
		int newChargeDiscount = chargeData.getDiscount();
		int newYuanbao = chargeData.getYuanbao();
		System.out.println("chargePrice = " + newChargePrice + 
				", chargeDiscount="+newChargeDiscount+", yuanbao="+newYuanbao);
		assertEquals(chargePrice, newChargePrice);
		assertTrue(newChargeDiscount < 100);
		assertTrue(newYuanbao > yuanbao);
	}
		
	@Test
	public void testUserLoginVipExpire() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		//Make the user VIP expire
		User user = UserManager.getInstance().queryUser(userName);
		user.setIsvip(true);
		user.setVipedate(new Date(System.currentTimeMillis()-86400));
		UserManager.getInstance().saveUser(user, false);
		
		List<XinqiMessage> list = loginUser(userName);
		
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		assertEquals(false, actualUser.isVip());
	}
	
	@Test
	public void testUserLoginBag() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		User user = manager.queryUser(userName);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.setBag(makeBag(user, 3));
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		handler.messageProcess(session, message, null);
		Thread.sleep(200);
		Set<String> classList = new HashSet<String>(16);
		int i = 0;
		for ( XinqiMessage xinqi : list ) {
			classList.add(xinqi.payload.getClass().getName());
			if ( xinqi.payload instanceof BseRoleBattleInfo ) {
				BseRoleBattleInfo bseRoleInfo = (BseRoleBattleInfo)xinqi.payload;
				List<com.xinqihd.sns.gameserver.proto.XinqiPropData.PropData> bag = bseRoleInfo.getRoleBagInfoList();
				System.out.println(bag);
				assertEquals(3, bag.size());
			} else if ( xinqi.payload instanceof BseMap ) {
				BseMap map = (BseMap)xinqi.payload;
				int count = map.getMapsCount();
				for ( int k=0; k<count; k++ ) {
					XinqiBseMap.MapData mapData = map.getMaps(k);
					System.out.println(mapData);
				}
			}
//			System.out.println(
//					"Type: " + xinqi.type + "[" 
//							+ IdToMessage.idToMessage(xinqi.type)
//					+"], Payload Class: " + xinqi.payload.getClass()
//					+ ", Length: " + xinqi.payload.toByteArray().length);
		}
//		String[] classNames = classList.toArray(new String[0]);
//		Arrays.sort(classNames);
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseInit$BseInit"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseLogin$BseLogin"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo$BseRoleBattleInfo"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig$BseRoleConfig"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo$BseRoleInfo"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTaskList$BseTaskList"));

	}
	
	@Test
	public void testUserTaskList() throws Exception {
		String userName = "te001";
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		//First register user
		registerUser(userName);
		
		User user = manager.queryUser(userName);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.setLevel(4);
		user.setBag(makeBag(user, 3));
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		MessageQueue queue = createNiceMock(MessageQueue.class);
		queue.sessionWrite(anyObject(IoSession.class), anyObject(), anyObject(SessionKey.class));
		final ArrayList<XinqiMessage> messages = new ArrayList<XinqiMessage>(5);
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				XinqiMessage arg = (XinqiMessage)getCurrentArguments()[1];
				messages.add(arg);
				return arg;
			}
			
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getTestInstance(), queue);
		
		replay(session);
		replay(queue);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
		verify(queue);
		
		int i = 0;
		for ( XinqiMessage xinqi : messages ) {
			if ( xinqi.payload instanceof BseTaskList ) {
				BseTaskList taskList = (BseTaskList)xinqi.payload;
				System.out.println(taskList);
				assertTrue(taskList.getTaskListCount()>0);
			}
		}
	}
	
	@Test
	public void testUserTaskListWithDailyTasks() throws Exception {
		String userName = "te001";
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		//First register user
		registerUser(userName);
		
		User user = manager.queryUser(userName);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.setLevel(14);
		user.setBag(makeBag(user, 3));
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		TaskManager.getInstance().deleteUserTasks(user);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		MessageQueue queue = createNiceMock(MessageQueue.class);
		queue.sessionWrite(anyObject(IoSession.class), anyObject(), anyObject(SessionKey.class));
		final ArrayList<XinqiMessage> messages = new ArrayList<XinqiMessage>(5);
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				XinqiMessage arg = (XinqiMessage)getCurrentArguments()[1];
				messages.add(arg);
				return arg;
			}
			
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getTestInstance(), queue);
		
		replay(session);
		replay(queue);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
		verify(queue);
		
		int i = 0;
		for ( XinqiMessage xinqi : messages ) {
			if ( xinqi.payload instanceof BseTaskList ) {
				BseTaskList taskList = (BseTaskList)xinqi.payload;
//				System.out.println(taskList);
				assertTrue(taskList.getTaskListCount()>0);
			}
		}
	}
	
	@Test
	public void testUserTaskListWithRefreshDaily() throws Exception {
		String userName = "te001";
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		//First register user
		registerUser(userName);
		
		User user = manager.queryUser(userName);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.setLevel(14);
		//Set last access date.
		Calendar cal = Calendar.getInstance();
		int date = cal.get(Calendar.DAY_OF_MONTH)-1;
		cal.set(Calendar.DAY_OF_MONTH, date);
		user.setLdate(cal.getTime());
		
		user.setBag(makeBag(user, 3));
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		TaskManager.getInstance().deleteUserTasks(user);
		List<TaskPojo> tasks = new ArrayList<TaskPojo>(user.getTasks(TaskType.TASK_DAILY));
		TaskManager.getInstance().finishTask(user, tasks.get(0).getId());
		assertEquals(0, TaskManager.getInstance().acquireTodoTasks(user).size());
		assertTrue(tasks.size()>0);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		MessageQueue queue = createNiceMock(MessageQueue.class);
		queue.sessionWrite(anyObject(IoSession.class), anyObject(), anyObject(SessionKey.class));
		final ArrayList<XinqiMessage> messages = new ArrayList<XinqiMessage>(5);
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				XinqiMessage arg = (XinqiMessage)getCurrentArguments()[1];
				messages.add(arg);
				return arg;
			}
			
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getTestInstance(), queue);
		
		replay(session);
		replay(queue);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
		verify(queue);
		
		int i = 0;
		for ( XinqiMessage xinqi : messages ) {
			if ( xinqi.payload instanceof BseTaskList ) {
				BseTaskList taskList = (BseTaskList)xinqi.payload;
				System.out.println(taskList);
				assertTrue(taskList.getTaskListCount()>0);
			}
		}
		
		String finishSet = TaskManager.getInstance().getFinishedSetName(user);
		String awardSet = TaskManager.getInstance().getAwardedSetName(user);
		Jedis jedisDB = JedisFactory.getJedisDB();
		//The login task exist in finished set.
		//task: 149:登陆有奖相送, 
		//task: 209:踏上征途, 
		//task: 6:勇往直前LV5
		//task: 238:等级达到10级
		int achievementSize = jedisDB.scard(awardSet).intValue();
		//task 6 and 149
		//7: 勇往直前lv6
		//6: 勇往直前lv5
		//149: 登陆有奖相送
		//13: 勇往直前lv9
		//11: 勇往直前lv7
		//12: 勇往直前lv8
		Set finish = jedisDB.smembers(finishSet);
		//task 229 and 238
		Set award = jedisDB.smembers(awardSet);
		System.out.println(finish);
		System.out.println(award);
		int taskSize = jedisDB.scard(finishSet).intValue();
		assertEquals(""+achievementSize, 2, achievementSize);
		assertEquals(""+taskSize, 6, taskSize);
	}
	
	/**
	 * It is deprecated.
	 * @throws Exception
	 */
	public void testUserAlreadyLogin() throws Exception {
		String userName = randomUserName();
		
		//First register user
		registerUser(userName);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
//		expect(session.getRemoteAddress()).andReturn(new InetSocketAddress("127.0.0.1", 80));
		MessageQueue queue = createNiceMock(MessageQueue.class);
		queue.sessionWrite(anyObject(IoSession.class), anyObject(), anyObject(SessionKey.class));
		final ArrayList<XinqiMessage> messages = new ArrayList<XinqiMessage>(5);
		expectLastCall().andAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				XinqiMessage arg = (XinqiMessage)getCurrentArguments()[1];
				messages.add(arg);
				return arg;
			}
			
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getTestInstance(), queue);
		
		replay(session);
		replay(queue);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
		verify(queue);
		
		assertEquals(3, messages.size());
		String[] classNames = new String[3];
		int i = 0;
		for ( XinqiMessage xinqi : messages ) {
			classNames[i++] = xinqi.payload.getClass().getName();
//			System.out.println(
//					"Type: " + xinqi.type + "[" 
//							+ IdToMessage.idToMessage(xinqi.type)
//					+"], Payload Class: " + xinqi.payload.getClass()
//					+ ", Length: " + xinqi.payload.toByteArray().length);
		}
		Arrays.sort(classNames);
		assertEquals("com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo$BseRoleBattleInfo", classNames[0]);
		assertEquals("com.xinqihd.sns.gameserver.proto.XinqiBseRoleConfig$BseRoleConfig", classNames[1]);
		assertEquals("com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo$BseRoleInfo", classNames[2]);
		
		//login again
		messages.clear();
		handler.messageProcess(session, message, null);
		assertEquals(1, messages.size());
		
		XinqiMessage actual = (XinqiMessage)messages.get(0);
		BseLogin login = (BseLogin)actual.payload;
		assertEquals(Text.text(ErrorCode.ALREADY_LOGIN.desc()), 
				login.getDesc());
	}
						
	private void registerUser(String userName) throws Exception {
		BceRegister.Builder payload = BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createMock(IoSession.class);
		
		session.write(anyObject());
		
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	private void registerUser2(String userName) throws Exception {
		BceRegister.Builder payload = BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createMock(IoSession.class);
		
		session.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	private String randomUserName() {
		String user = "";
		Random r = new Random();
		return user + r.nextInt(99999);
	}
	
	// ----------------------------------------------------------
	
	private Bag makeBag(User user, int count) {
		Bag bag = new Bag();
//		bag.set_id(user.get_id());
//		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(1000+i));
		}
		bag.wearPropData(Constant.BAG_WEAR_COUNT+0, PropDataEquipIndex.WEAPON.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+1, PropDataEquipIndex.RING1.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+2, PropDataEquipIndex.RING2.index());
		return bag;
	}
	
	/**
	 * Make a fake PropData
	 * @param i
	 * @return
	 */
	private PropData makePropData(int i) {
		PropData propData = new PropData();
		propData.setItemId("510");
		propData.setName("夺命刀"+i);
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setDuration(1003);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}
	
	private BuffToolType makeBuffTool(int i) {
		if ( i >= 0 && i<BuffToolType.values().length ) {
			return BuffToolType.values()[i];
		} else {
			return BuffToolType.Recover;
		}
	}
	
	private List<XinqiMessage> loginUser(String userName) throws Exception {
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
				
		return list;
	}
	
	private List<Message> loginUser2(String userName) throws Exception {
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, null);
		
		BseShop bseShop = null;
		BseChargeList bseCharge = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseShop ) {
				bseShop = (BseShop)xinqi.payload;
			} else if ( xinqi.payload instanceof BseChargeList ) {
				bseCharge = (BseChargeList)xinqi.payload;
			}
		}
		assertNotNull(bseShop);
		assertNotNull(bseCharge);
		
		List<ShopData> shopDataList = bseShop.getShopsList();
		/*
			[id: "223"
			type: 139
			info: "\347\213\270\347\214\253\345\201\207\351\235\242"
			propInfoId: "10019"
			level: 0
			goldTye: 4
			buyPrices {
			  price: 27
			  validTimes: 10
			}
			buyPrices {
			  price: 54
			  validTimes: 30
			}
			buyPrices {
			  price: 135
			  validTimes: 90
			}
			buyPrices {
			  price: 405
			  validTimes: 900
			}
			297	21001	水神石炼化符
		 */
		ShopData shopData = null;
		for ( ShopData sd : shopDataList ) {
			if ( sd.getId().equals("297") ) {
				shopData = sd;
				break;
			}
		}

		List<Message> returnMessage = new ArrayList<Message>();
		returnMessage.add(shopData);
		return returnMessage;
	}
}
