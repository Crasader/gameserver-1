package com.xinqihd.sns.gameserver;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TipPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class GameContextTest extends AbstractTest {
	
	
	int max = 10000;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		GlobalConfig.getInstance().overrideProperty("zookeeper.root", "/snsgame/babywar");
		//ZooKeeperFactory.getInstance(connectString);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetEquipPojo() {
		GameContext gameContext = GameContext.getInstance();
		gameContext.reloadContext();
		Collection<WeaponPojo> weapons = gameContext.getEquipManager().getWeapons();
		assertNotNull("equipPojo should not be null", weapons);
	}


	@Test
	public void testIoSessionAttr() {
		
		final HashMap<Object, Object> attrs = new HashMap<Object, Object>();
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object key = getCurrentArguments()[0];
				Object value = getCurrentArguments()[1];
				attrs.put(key, value);
				return value;
			}
		}).times(2);
		expect(session.removeAttribute(anyObject())).andAnswer(new IAnswer<Boolean>() {
			@Override
			public Boolean answer() throws Throwable {
				Object key = getCurrentArguments()[0];
				attrs.remove(key);
				return true;
			}
		}).times(2);
		expect(session.getAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object key = getCurrentArguments()[0];
				return attrs.get(key);
			}
		}).anyTimes();
		
		replay(session);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		GameContext.getTestInstance().registerUserSession(session, user, null);
		assertNotNull(attrs.get(Constant.USER_KEY));
		assertNotNull(attrs.get(Constant.SESSION_KEY));
		assertNotNull(GameContext.getInstance().findLocalUserByIoSession(session));
		
		GameContext.getInstance().deregisterUserByIoSession(session);
		assertNull(attrs.get(Constant.USER_KEY));
		assertNull(attrs.get(Constant.SESSION_KEY));
		
		assertNull(GameContext.getInstance().findLocalUserByIoSession(session));
		
		verify(session);
	}
	
	@Test
	public void testQueryUserBySession() {
		GameContext gameContext = GameContext.getInstance();
		gameContext.reloadContext();
		
		User user = UserManager.getInstance().createDefaultUser();
		UserManager.getInstance().removeUser("test-001");
		
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		assertTrue(UserManager.getInstance().saveUser(user, true));
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).andReturn(null);
		
		replay(session);
		
		gameContext.registerUserSession(session, user, null);
		
		User actual = gameContext.findGlobalUserBySessionKey(user.getSessionKey());
		
		assertEquals(user.get_id(), actual.get_id());
		
		verify(session);
	}
	
	@Test
	public void testRegisterAndDeregister() throws Exception {
		//Set Redis Session timeout
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.session_timeout_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();
		JedisUtil.deleteAllKeys();
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		userManager.removeUser("test-001");
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		userManager.saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		GameContext context = GameContext.getInstance();
		context.registerUserSession(session, user, null);
		List<SessionKey> userSessionKeys = context.findAllOnlineUsers();
		assertEquals(1, userSessionKeys.size());
		SessionKey userSessionKey = userSessionKeys.get(0);
		assertEquals(user, context.findLocalUserBySessionKey(userSessionKey));
		
		context.deregisterUserByIoSession(session);
		//Make sure no memory leak here
		assertEquals(null, context.findLocalUserBySessionKey(userSessionKey));
		userSessionKeys = context.findAllOnlineUsers();
		assertEquals(1, userSessionKeys.size());
		//Wait until jedis session timeout
//		Thread.currentThread().sleep(2000);
		while ( context.findAllOnlineUsers().size() > 0 ) {
			Thread.currentThread().sleep(100);
		}
		userSessionKeys = context.findAllOnlineUsers();
		assertEquals(0, userSessionKeys.size());

	}
	
	@Test
	public void testRegisterAndDeregisterAgain() throws Exception {
		//Set Redis Session timeout
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.session_timeout_seconds, "10");
		
		Jedis jedis = JedisFactory.getJedis();
		JedisUtil.deleteAllKeys();
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		userManager.removeUser("test-001");
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		userManager.saveUser(user, true);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		GameContext context = GameContext.getInstance();
		context.registerUserSession(session, user, null);
		List<SessionKey> userSessionKeys = context.findAllOnlineUsers();
		assertEquals(1, userSessionKeys.size());
		SessionKey userSessionKey = userSessionKeys.get(0);
		assertEquals(user, context.findLocalUserBySessionKey(userSessionKey));
		
		context.deregisterUserByIoSession(session);
		//Make sure no memory leak here
		assertEquals(null, context.findLocalUserBySessionKey(userSessionKey));
		
		//Register again
		context.registerUserSession(session, user, userSessionKey);
		
		userSessionKeys = context.findAllOnlineUsers();
		assertEquals(1, userSessionKeys.size());
		assertEquals(user, context.findLocalUserBySessionKey(userSessionKey));

	}
	
	@Test
	public void testScheduleTask() throws Exception {
		GameContext.getInstance().initContext();
		GameContext.getInstance().scheduleTask(new Runnable() {
			public void run() {
				System.out.println("schedule task after 3 seconds");
			}
		}, 2, TimeUnit.SECONDS);
		Thread.currentThread().sleep(3000);
	}
	
	@Test
	public void testScheduleTaskPressure() throws Exception {
		final int[] counter = new int[1];
		TestUtil.doPerform(new Runnable() {
			public void run() {
				GameContext.getInstance().scheduleTask(new Runnable() {
					public void run() {
						System.out.println("schedule task "+(counter[0]++)+" after 1 seconds");
					}
				}, 1, TimeUnit.SECONDS);				
			}
		}, "schedule tasks", 1000);
		Thread.currentThread().sleep(1500);
	}
	
	public void listAllCharacters() {
		HashMap<Character, Integer> countMap = new HashMap<Character, Integer>();
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		for ( ItemPojo item : items ) {
			statChar(item.getName(), countMap);
			statChar(item.getInfo(), countMap);
		}
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		for ( WeaponPojo weapon : weapons ) {
			statChar(weapon.getName(), countMap);
			statChar(weapon.getInfo(), countMap);
		}
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		for ( TaskPojo task : tasks ) {
			statChar(task.getName(), countMap);
			statChar(task.getDesc(), countMap);
			statChar(task.getTaskTarget(), countMap);
		}
		Collection<TipPojo> tips = TipManager.getInstance().getTips();
		for ( TipPojo tip : tips ) {
			statChar(tip.getTip(), countMap);
		}
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		for ( ShopPojo shop : shops ) {
			statChar(shop.getInfo(), countMap);
		}
		int line = 0;
		for ( Character c : countMap.keySet() ) {
			if ( line++ % 10 == 0 ) {
				System.out.println();
			}
			System.out.print(c + ":" + countMap.get(c));
			System.out.print('\t');
		}
	}
	
	private void statChar(String str, HashMap<Character, Integer> countMap) {
		for ( char c : str.toCharArray() ) {
			Integer count = countMap.get(c);
			if ( count == null ) {
				count = 1;
			} else {
				count = count.intValue() + 1;
			}
			countMap.put(c, count);
		}
	}
}
