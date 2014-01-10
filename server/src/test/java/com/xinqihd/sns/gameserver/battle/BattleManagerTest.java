package com.xinqihd.sns.gameserver.battle;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceAskRoundOver.BceAskRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleChangeDirection.BceRoleChangeDirection;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleMove.BceRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBceRolePower.BceRolePower;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleUseTool.BceRoleUseTool;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleOver.BseBattleOver;
import com.xinqihd.sns.gameserver.proto.XinqiBseDead.BseDead;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleChangeDirection.BseRoleChangeDirection;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleMove.BseRoleMove;
import com.xinqihd.sns.gameserver.proto.XinqiBseRolePower.BseRolePower;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.MessageQueue;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BattleManagerTest {
	
	static {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_distributed, "false");
	}

	String expectRpcServerId = "localhost:3445";

	public BattleManagerTest() {
		
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_GAME_SERVERID, expectRpcServerId);
		GlobalConfig.getInstance().overrideProperty("zookeeper.root",
				"/snsgame/babywar");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
			
		AbstractTest test = new AbstractTest();
		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		GameContext.getTestInstance().reloadContext();
		BattleDataLoader4Bitmap.loadBattleMaps();
		BattleDataLoader4Bitmap.loadBattleBullet();
	}

	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_distributed, "false");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateBattle() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		System.out.println(bUser1.getPosX() + "," + bUser1.getPosY());
		System.out.println(bUser2.getPosX() + "," + bUser2.getPosY());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();
		String rpcServerId = jedis.hget(user1.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY);
		assertEquals(expectRpcServerId, rpcServerId);
		rpcServerId = jedis.hget(user2.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY);
		assertEquals(expectRpcServerId, rpcServerId);

		assertEquals(expectRpcServerId, jedis.hget(battle.getBattleSessionKey()
				.toString(), Constant.RPC_SERVER_KEY));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testBattleOver() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		ArrayList<XinqiMessage> list1 = new ArrayList<XinqiMessage>();
		IoSession session1 = TestUtil.createIoSession(list1);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList<XinqiMessage> list2 = new ArrayList<XinqiMessage>();
		IoSession session2 = TestUtil.createIoSession(list2);
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();
		
		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		
		//Check all the users become friends
		Collection<BattleUser> battleUsers = battle.getBattleUserMap().values();
		for ( BattleUser battleUser : battleUsers ) {
			User user = battleUser.getUser();
			Relation relation = user.getRelation(RelationType.RIVAL);
			assertNotNull(relation);
			Collection<People> people = relation.listPeople();
			for ( People p : people ) {
				UserId id = p.getId();
				assertNotNull(id);
				System.out.println( user.getUsername() + " become friend with " + p.getUsername() );
			}
		}

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();
		String rpcServerId = null;

		rpcServerId = new String(jedis.hget(user1.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);
		rpcServerId = new String(jedis.hget(user2.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);

		SessionKey battleKey = null;
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user1
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user2
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);

		assertEquals(expectRpcServerId, jedis.hget(battle.getBattleSessionKey()
				.toString(), Constant.RPC_SERVER_KEY));

		// Test findBattleByUserSessionKey
		Battle actualBattle1 = battleManager.findBattleByUserSessionKey(user1
				.getSessionKey());
		assertEquals(battle, actualBattle1);
		Battle actualBattle2 = battleManager.findBattleByUserSessionKey(user2
				.getSessionKey());
		assertEquals(battle, actualBattle2);

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		// System.out.println(msgList);
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Battle Over
		msgList.clear();

		// battleManager.battleEnd(battle, BattleStatus.ROOM_IN_DRAW);
		battleManager.roleDead(user1.getSessionKey() , null);

		// System.out.println(msgList);
		assertEquals(5, msgList.size());
		BseDead bseDead = (BseDead) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), bseDead.getSessionID());
		BseBattleOver battleOver = (BseBattleOver) msgList.get(3);
		assertEquals(BattleCamp.LEFT.id(), battleOver.getWinCamp());

		verify(queue);

		assertNull(jedis.hget(user1.getSessionKey().toString(),
				Battle.BATTLE_SERVER_KEY));
		assertNull(jedis.hget(user2.getSessionKey().toString(),
				Battle.BATTLE_SERVER_KEY));
		assertNull(jedis.hget(user1.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));
		assertTrue(!jedis.exists(battle.getBattleSessionKey().toString()));
		assertNull(jedis.hget(user2.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleOverWithTaskHook() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		ArrayList userList1 = new ArrayList();
		IoSession session1 = TestUtil.createIoSession(userList1);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList userList2 = new ArrayList();
		IoSession session2 = TestUtil.createIoSession(userList2);
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//15	激战到底I	10	1	2	7	TASK_DAILY	完成任意战斗	script.task.AnyCombat
		TaskPojo task = TaskManager.getInstance().getTaskById("15");
		task.setStep(1);
		tasks.add(task);
		
		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		bUser1.getUser().addTasks(tasks);
		bUser1.getUser().setSession(session1);
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());
		bUser2.getUser().addTasks(tasks);
		bUser2.getUser().setSession(session2);

		// User1 and User2 are stage ready.
		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());
		
		Thread.currentThread().sleep(200);
		userList1.clear();
		userList2.clear();

		battleManager.roleDead(user1.getSessionKey() , null);

		Thread.currentThread().sleep(200);
		
		//The battle is over.
		System.out.println("userList1: " + userList1);
		System.out.println("userList2: " + userList2);
		
		XinqiMessage xinqi = null;
		
		/**
		 * BseRoleInfo
		 * BseBattleOver
		 * BseModiTask
		 */
		assertEquals(3, userList1.size());
		xinqi = (XinqiMessage)userList1.get(2);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
		
		/**
		 * BseDead
		 * BseRoleInfo
		 * BseBattleOver
		 * BseModiTask
		 */
		assertEquals(4, userList2.size());
		xinqi = (XinqiMessage)userList2.get(3);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
		
		Jedis jedis = JedisFactory.getJedis();

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testBattleOverWithAnyCombatWin() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		ArrayList userList1 = new ArrayList();
		IoSession session1 = TestUtil.createIoSession(userList1);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList userList2 = new ArrayList();
		IoSession session2 = TestUtil.createIoSession(userList2);
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//15	激战到底I	10	1	2	7	TASK_DAILY	完成任意战斗	script.task.AnyCombat
		TaskPojo task = TaskManager.getInstance().getTaskById("15");
		task.setStep(1);
		task.setScript(ScriptHook.TASK_ANY_COMBAT_WIN.getHook());
		tasks.add(task);
		
		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		bUser1.getUser().addTasks(tasks);
		bUser1.getUser().setSession(session1);
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());
		bUser2.getUser().addTasks(tasks);
		bUser2.getUser().setSession(session2);

		// User1 and User2 are stage ready.
		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());
		
		Thread.currentThread().sleep(200);
		
		userList1.clear();
		userList2.clear();

		battleManager.roleDead(user1.getSessionKey() , null);

		//The battle is over.
		System.out.println("userList1: " + userList1);
		System.out.println("userList2: " + userList2);
		
		Thread.currentThread().sleep(200);
		
		XinqiMessage xinqi = null;
		
		/**
		 * BseRoleInfo
		 * BseBattleOver
		 */
		assertEquals(2, userList1.size());
		
		/**
		 * BseDead
		 * BseRoleInfo
		 * BseBattleOver
		 * BseModiTask
		 */
		assertEquals(4, userList2.size());
		xinqi = (XinqiMessage)userList2.get(3);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
		
		Jedis jedis = JedisFactory.getJedis();

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleOverWithBeatUsers() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		ArrayList userList1 = new ArrayList();
		IoSession session1 = TestUtil.createIoSession(userList1);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList userList2 = new ArrayList();
		IoSession session2 = TestUtil.createIoSession(userList2);
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//15	激战到底I	10	1	2	7	TASK_DAILY	完成任意战斗	script.task.AnyCombat
		TaskPojo task = TaskManager.getInstance().getTaskById("15");
		task.setStep(1);
		task.setScript(ScriptHook.TASK_BEAT_USERS.getHook());
		tasks.add(task);
		
		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		bUser1.getUser().addTasks(tasks);
		bUser1.getUser().setSession(session1);
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());
		bUser2.getUser().addTasks(tasks);
		bUser2.getUser().setSession(session2);

		// User1 and User2 are stage ready.
		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());
		
		Thread.currentThread().sleep(200);
		
		userList1.clear();
		userList2.clear();

		battleManager.roleDead(user1.getSessionKey() , null);

		//The battle is over.
		System.out.println("userList1: " + userList1);
		System.out.println("userList2: " + userList2);
		
		XinqiMessage xinqi = null;
		
		Thread.currentThread().sleep(200);
		
		assertEquals(2, userList1.size());
		
		assertEquals(4, userList2.size());
		xinqi = (XinqiMessage)userList2.get(3);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
		
		Jedis jedis = JedisFactory.getJedis();

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleOverWithSingleCombatWin() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		ArrayList userList1 = new ArrayList();
		IoSession session1 = TestUtil.createIoSession(userList1);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList<XinqiMessage> userList2 = new ArrayList<XinqiMessage>();
		IoSession session2 = TestUtil.createIoSession(userList2);
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//15	激战到底I	10	1	2	7	TASK_DAILY	完成任意战斗	script.task.AnyCombat
		TaskPojo task = TaskManager.getInstance().getTaskById("15");
		task.setStep(1);
		task.setScript(ScriptHook.TASK_SINGLE_COMBAT_WIN.getHook());
		tasks.add(task);
		
		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		bUser1.getUser().addTasks(tasks);
		bUser1.getUser().setSession(session1);
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());
		bUser2.getUser().addTasks(tasks);
		bUser2.getUser().setSession(session2);

		// User1 and User2 are stage ready.
		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());
		
		userList1.clear();
		userList2.clear();

		battleManager.roleDead(user1.getSessionKey() , null);

		//The battle is over.
		System.out.println("userList1: " + userList1);
		System.out.println("userList2: " + userList2);
		
		Thread.currentThread().sleep(200);
		
		XinqiMessage xinqi = null;
		
		assertTrue(userList1.size()>=2);
		
		assertTrue(userList2.size()>=4);
		for ( XinqiMessage msg : userList2 ) {
			if ( msg.payload instanceof BseModiTask ) {
				return;
			}
		}
		fail("Do not find BseModiTask");
		
		Jedis jedis = JedisFactory.getJedis();

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleEnd() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();
		String rpcServerId = null;

		rpcServerId = new String(jedis.hget(user1.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);
		rpcServerId = new String(jedis.hget(user2.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);

		SessionKey battleKey = null;
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user1
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user2
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);

		assertEquals(expectRpcServerId, jedis.hget(battle.getBattleSessionKey()
				.toString(), Constant.RPC_SERVER_KEY));

		// Test findBattleByUserSessionKey
		Battle actualBattle1 = battleManager.findBattleByUserSessionKey(user1
				.getSessionKey());
		assertEquals(battle, actualBattle1);
		Battle actualBattle2 = battleManager.findBattleByUserSessionKey(user2
				.getSessionKey());
		assertEquals(battle, actualBattle2);

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		// System.out.println(msgList);
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Battle Over
		msgList.clear();

		// battleManager.battleEnd(battle, BattleStatus.ROOM_IN_DRAW);
		battleManager.roleDead(user1.getSessionKey() , null);

		// System.out.println(msgList);
		assertEquals(5, msgList.size());
		BseDead bseDead = (BseDead) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), bseDead.getSessionID());
		BseBattleOver battleOver = (BseBattleOver) msgList.get(3);
		assertEquals(BattleCamp.LEFT.id(), battleOver.getWinCamp());

		// Make the battle end
		battleManager.pickReward(user1, new int[] { 0 });

		battleManager.battleEnd(battle);
		
		verify(queue);

		assertEquals(null,
				jedis.hget(user1.getSessionKey().toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(null,
				jedis.hget(user2.getSessionKey().toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(null, jedis.hget(user1.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));
		assertEquals(null, jedis.hget(user2.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));
		assertEquals(null, jedis.get(battle.getBattleSessionKey().toString()));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testBattleOverAndRematch() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();
		String rpcServerId = null;

		rpcServerId = new String(jedis.hget(user1.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);
		rpcServerId = new String(jedis.hget(user2.getSessionKey()
				.toString(), Battle.BATTLE_SERVER_KEY));
		assertEquals(expectRpcServerId, rpcServerId);

		SessionKey battleKey = null;
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user1
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);
		battleKey = SessionKey.createSessionKeyFromHexString(jedis.hget(user2
				.getSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(battle.getBattleSessionKey(), battleKey);

		assertEquals(expectRpcServerId, jedis.hget(battle.getBattleSessionKey()
				.toString(), Constant.RPC_SERVER_KEY));

		// Test findBattleByUserSessionKey
		Battle actualBattle1 = battleManager.findBattleByUserSessionKey(user1
				.getSessionKey());
		assertEquals(battle, actualBattle1);
		Battle actualBattle2 = battleManager.findBattleByUserSessionKey(user2
				.getSessionKey());
		assertEquals(battle, actualBattle2);

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createNiceMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		// System.out.println(msgList);
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Battle Over
		msgList.clear();

		// battleManager.battleEnd(battle, BattleStatus.ROOM_IN_DRAW);
		battleManager.roleDead(user1.getSessionKey() , null);

		System.out.println(msgList);
		//BseDead, BseRoleInfo x 2, BseBattleOver x 2
		assertEquals(5, msgList.size());
		BseDead bseDead = (BseDead) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), bseDead.getSessionID());
		BseBattleOver battleOver = (BseBattleOver) msgList.get(3);
		assertEquals(BattleCamp.LEFT.id(), battleOver.getWinCamp());

		// Rematch now
		// User1 and User2 press "ready start"
		msgList.clear();
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());
		battle = battles.iterator().next();

//		System.out.println(msgList);

		verify(queue);

		assertNotNull(jedis.hget(user1.getSessionKey().toString(),
				Battle.BATTLE_SERVER_KEY));
		assertNotNull(jedis.hget(user2.getSessionKey().toString(),
				Battle.BATTLE_SERVER_KEY));
		assertNotNull(jedis.hget(user1.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));
		assertNotNull(jedis.hget(user2.getSessionKey().toString(),
				Battle.BATTLE_SESSION_KEY));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testRoleAttack() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUserBag(user1, true);
		manager.saveUser(user2, true);
		manager.saveUserBag(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		// Change the battleMap
		ArrayList<BattleBitSetMap> battleMaps = BattleDataLoader4Bitmap.getBattleMapList();
		BattleBitSetMap map03 = null;
		for (BattleBitSetMap bm : battleMaps) {
			if (bm.getMapId().equals("0")) {
				map03 = bm;
				break;
			}
		}
		assertNotNull(map03);
		TestUtil.setPrivateFieldValue("battleMap", battle, map03);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// System.out.println(msgList);
		// Check if roundStart is called
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));
		// userPos: (990, 37), angle: 93, power: 83, dir: 1
		// Role Attack
		msgList.clear();
		BceRoleAttack.Builder bceRoleAttack = BceRoleAttack.newBuilder();
		bceRoleAttack.setAngle(150000);
		bceRoleAttack.setAtkAngle(bceRoleAttack.getAngle());
		bceRoleAttack.setDirection(RoleDirection.RIGHT.ordinal());
		bceRoleAttack.setPower(30);
		int userx = 1380;
		int usery = 697;
		bceRoleAttack.setUserx(userx);
		bceRoleAttack.setUsery(usery);
		bUser1.setPosX(userx);
		bUser1.setPosY(usery);
		bUser2.setPosX(userx);
		bUser2.setPosY(usery);
		battleManager.roleAttack(bUser1.getUserSessionKey(), bceRoleAttack.build());

		// System.out.println(msgList);
		// 2 roleAttack messages, 2 roundStart messages.
		Thread.sleep(200);
		assertTrue(msgList.size() > 0);
		// BseRoleAttack bseRoleAttack = (BseRoleAttack)msgList.get(0);

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testBattleOverAudit() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUserBag(user1, true);
		manager.saveUser(user2, true);
		manager.saveUserBag(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		// Change the battleMap
		ArrayList<BattleBitSetMap> battleMaps = BattleDataLoader4Bitmap.getBattleMapList();
		BattleBitSetMap map03 = null;
		for (BattleBitSetMap bm : battleMaps) {
			if (bm.getMapId().equals("1")) {
				map03 = bm;
				break;
			}
		}
		assertNotNull(map03);
		TestUtil.setPrivateFieldValue("battleMap", battle, map03);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));
		// userPos: (990, 37), angle: 93, power: 83, dir: 1
		// Role Attack
		msgList.clear();

		// Move the other user to the attack point.
		bUser2.setPosX(561);
		bUser2.setPosY(64);

		BceRoleAttack.Builder bceRoleAttack = BceRoleAttack.newBuilder();
		bceRoleAttack.setAngle(80);
		bceRoleAttack.setAtkAngle(80);
		bceRoleAttack.setDirection(RoleDirection.RIGHT.ordinal());
		bceRoleAttack.setPower(0);
		bceRoleAttack.setUserx(520);
		bceRoleAttack.setUsery(55);
		bUser2.setBlood(0);
		battleManager.roleAttack(bUser1.getUserSessionKey(), bceRoleAttack.build());

		// System.out.println(msgList);
		Thread.sleep(200);
		assertTrue(msgList.size()>=2);
		BseRoleAttack bseRoleAttack = (BseRoleAttack) msgList.get(0);

		battle.battleOver(BattleStatus.ROOM_LEFT_WIN);
		Collection<BattleUser> battleUsers = battle.getBattleUserMap().values();
		BattleUser winner = null;
		for (BattleUser bUser : battleUsers) {
			if ( bUser.getTotalExp() > 0 ) {
				winner = bUser;
			}
			System.out.println("bUser totalExpDelta: " + bUser.getTotalExp());
		}
		//assertNotNull(winner);
		//assertEquals(winner.getTotalExp(), winner.getUser().getExp());

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testBattleChecker() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		// Change the battleMap
		ArrayList<BattleBitSetMap> battleMaps = BattleDataLoader4Bitmap.getBattleMapList();
		BattleBitSetMap map03 = null;
		for (BattleBitSetMap bm : battleMaps) {
			if (bm.getMapId().equals("12")) {
				map03 = bm;
				break;
			}
		}
		assertNotNull(map03);
		TestUtil.setPrivateFieldValue("battleMap", battle, map03);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Make one user offline
		jedis.del(user1.getSessionKey().toString());

		// Call battle checker
		msgList.clear();
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		battleManager.init();
		Thread.sleep(3000);

		//Now the checker will not send BattleOver, instead, it call BattleEnd
		//wangqi 2012-3-2
		//System.out.println(msgList.get(0).getClass());
		// BseRoleInfo, BseBattleover
		//assertEquals(2, msgList.size());
		//assertTrue(msgList.get(1) instanceof BseBattleOver);
		// BseRoleAttack bseRoleAttack = (BseRoleAttack)msgList.get(0);
		assertFalse(jedis.exists(battle.getBattleSessionKey().toString()));

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleCheckerWithRoundOwnerTimeout() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUserBag(user1, true);
		manager.saveUser(user2, true);
		manager.saveUserBag(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		// Change the battleMap
		ArrayList<BattleBitSetMap> battleMaps = BattleDataLoader4Bitmap.getBattleMapList();
		BattleBitSetMap map03 = null;
		for (BattleBitSetMap bm : battleMaps) {
			if (bm.getMapId().equals("12")) {
				map03 = bm;
				break;
			}
		}
		assertNotNull(map03);
		TestUtil.setPrivateFieldValue("battleMap", battle, map03);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Make one user offline
		jedis.del(user1.getSessionKey().toString());

		// Call battle checker
		msgList.clear();
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.BATTLE_USER_MAX_IDLE, 2);

		battleManager.init();
		Thread.sleep(3000);
		
		//Now the checker will not send BattleOver, instead, it call BattleEnd
		//wangqi 2012-3-2
		//System.out.println(msgList.get(0).getClass());
		// 2 roleAttack messages, 2 roundStart messages.
		//assertTrue(msgList.size()>=1);
		//assertTrue(msgList.get(1) instanceof BseBattleOver);
		// BseRoleAttack bseRoleAttack = (BseRoleAttack)msgList.get(0);
		assertFalse(jedis.exists(battle.getBattleSessionKey().toString()));

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testCheckBattleUsers() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		// Now the battle is created.
		Battle battle = battles.iterator().next();
		BattleStatus status = battle.checkBattleUsers();
		assertEquals(BattleStatus.NORMAL, status);

		// An user is offline
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(user1.getSessionKey().toString());
		status = battle.checkBattleUsers();
		assertEquals(BattleStatus.ROOM_LEFT_WIN, status);

		// Clean system.

		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testCheckBattleUsersMultiThread() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		// Now the battle is created.
		final Battle battle = battles.iterator().next();

		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				BattleStatus status = battle.checkBattleUsers();
				assertEquals(BattleStatus.NORMAL, status);
			}
		}, "CheckBattleUsers", 100, 5);

		// Clean system.
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testCheckBattleUsersLeavingRoom() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		// Now the battle is created.
		Battle battle = battles.iterator().next();
		BattleStatus status = battle.checkBattleUsers();
		assertEquals(BattleStatus.NORMAL, status);

		// An user is leaving
		RoomManager.getInstance().leaveRoom(user1.getSessionKey());
		status = battle.checkBattleUsers();
		assertEquals(BattleStatus.ROOM_LEFT_WIN, status);

		// Clean system.
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testBattleCheckerWithMaxTimeout() throws Exception {
		GameDataManager.getInstance().overrideRuntimeValue(
				GameDataKey.BATTLE_MAX_SECONDS, 1);
		
		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUserBag(user1, true);
		manager.saveUser(user2, true);
		manager.saveUserBag(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();
		assertTrue(battle.getBattleCreatedTimestamp()>0);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser bUser1 = battleUserMap.get(user1.getSessionKey());
		BattleUser bUser2 = battleUserMap.get(user2.getSessionKey());

		assertEquals(user1.getSessionKey(), bUser1.getUserSessionKey());
		assertEquals(user1.getBlood(), bUser1.getBlood());
		assertEquals(user1.getTkew(), bUser1.getThew());

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Check if roundStart is called
		assertEquals(2, msgList.size());
		BseRoundStart roundStart = (BseRoundStart) msgList.get(0);
		assertNotNull(roundStart.getUserId(0));
		assertNotNull(roundStart.getUserId(1));

		// Make one user offline
		jedis.del(user1.getSessionKey().toString());

		// Call battle checker
		msgList.clear();
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.BATTLE_USER_MAX_IDLE, 2);

		battleManager.init();
		Thread.sleep(1500);
		
		assertFalse(jedis.exists(battle.getBattleSessionKey().toString()));

		verify(queue);
		
		BseBattleOver battleOver = null;
		for ( int i = 0; i<msgList.size(); i++ ) {
			Object obj = msgList.get(i);
			if ( obj instanceof BseBattleOver ) {
				battleOver = (BseBattleOver)obj;
			}
		}
		assertNotNull(battleOver);
		//assertEquals(BattleCamp.BOTH.id(), battleOver.getWinCamp());

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testRoleMove() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Test roleMove
		msgList.clear();
		BceRoleMove move = BceRoleMove.newBuilder().setX(10).setY(15).build();
		battleManager.roleMove(user1.getSessionKey(), move);

		System.out.println(msgList);
		assertEquals(1, msgList.size());
		BseRoleMove roleMove = (BseRoleMove) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), roleMove.getSessionId());
		assertEquals(10, roleMove.getX());
		assertEquals(15, roleMove.getY());

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testRolePower() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Test roleMove
		msgList.clear();
		BceRolePower power = BceRolePower.newBuilder()
				.setSessionID(user1.getSessionKey().toString()).setFull(true).build();
		battleManager.rolePower(user1.getSessionKey(), power);

		assertEquals(1, msgList.size());
		BseRolePower rolePower = (BseRolePower) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), rolePower.getSessionID());
		assertEquals(true, rolePower.getFull());

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testChangeDirection() throws Exception {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		// User1 and User2 press "ready start"
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		Jedis jedis = JedisFactory.getJedis();

		// User1 and User2 are stage ready.
		// Test roundStart
		final ArrayList msgList = new ArrayList();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add(msg);
				return null;
			}
		}).anyTimes();
		TestUtil.setPrivateFieldValue("messageQueue", GameContext.getInstance(),
				queue);
		replay(queue);

		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());

		// Test roleMove
		msgList.clear();
		BceRoleChangeDirection.Builder builder = BceRoleChangeDirection
				.newBuilder();
		builder.setSessionId(user1.getSessionKey().toString());
		builder.setDirection(RoleDirection.RIGHT.ordinal());
		battleManager.changeDirection(user1.getSessionKey(), builder.build());

		System.out.println(msgList);
		assertEquals(1, msgList.size());
		BseRoleChangeDirection message = (BseRoleChangeDirection) msgList.get(0);
		assertEquals(user1.getSessionKey().toString(), message.getSessionId());
		assertEquals(RoleDirection.RIGHT.ordinal(), message.getDirection());

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testRoundStart() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();
		BattleManager battleManager = BattleManager.getInstance();
		battleManager.init();
		
		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		ArrayList<XinqiMessage> user1List = new ArrayList<XinqiMessage>();
		IoSession session1 = TestUtil.createIoSession(user1List);
		
		GameContext.getInstance().registerUserSession(session1, user1, null);

		ArrayList user2List = new ArrayList();
		IoSession session2 = TestUtil.createIoSession(user2List);
		
		GameContext.getInstance().registerUserSession(session2, user2, null);

		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);
		
		int time = 0;
		while ( user1List.size() <= 0 && time<20) {
			Thread.currentThread().sleep(50);
			time++;
		}
		user1List.clear();
		user2List.clear();
		
		battleManager.stageReady(user1.getSessionKey());
		battleManager.stageReady(user2.getSessionKey());
		
		time = 0;
		while ( user1List.size() <= 0 && time<20) {
			Thread.currentThread().sleep(50);
			time++;
		}
		Thread.currentThread().sleep(200);
		
		//Now we will receive the round start message.
		User roundUser = null;
		assertTrue(user1List.size()>=1);
		for ( XinqiMessage xinqi : user1List ) {
			if ( xinqi.payload instanceof BseRoundStart ) {
				BseRoundStart roundStart = (BseRoundStart)xinqi.payload;
				if ( roundStart.getSessionId().equals(user1.getSessionKey().toString()) ) {
					System.out.println("User1 will round start");
					roundUser = user1;
				} else {
					System.out.println("User2 will round start");
					roundUser = user2;
				}

			}
		}
		assertNotNull("Do not find BseRoundStart", roundUser);
		
		user1List.clear();
		user2List.clear();
		
		//Make the roundUser offline.
		jedis.del(roundUser.getSessionKey().toString());
		
		//Wait until the battleOver is sent
		Thread.currentThread().sleep(2000);
		
		System.out.println(user2List);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		//When battleOver is called, the battleEnd is called too
		//And the battle will be removed.
		assertEquals(0, battles.size());

//		Battle battle = battles.iterator().next();

		// Clean system.
//		jedis.del(battle.getBattleSessionKey().toString());
//		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
//			jedis.del(bUser.getUserSessionKey().toString());	
//		}
	}

	@Test
	public void testPickReward() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);
		
		battleManager.roleDead(user1.getSessionKey() , null);

		Collection<Battle> battles = battleManager.findAllBattles();
		// Thread.sleep(Long.MAX_VALUE);
		// The battle is end
		assertEquals(0, battles.size());

//		Battle battle = battles.iterator().next();
//
//		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
//		assertEquals(2, battleUserMap.size());

		// Generate the battle rewards
		int slot = 26;
//		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
//				.next();
		ScriptManager scriptManager = ScriptManager.getInstance();
		ScriptResult result = scriptManager.runScript(ScriptHook.BATTLE_REWARD,
				user1, slot);
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		ArrayList<Reward> rewardList = (ArrayList<Reward>) result.getResult();
		for ( Reward r : rewardList ) {
			System.out.println(r.getId()+", "+r.getPropCount());
		}
		assertEquals(slot, rewardList.size());
		Bag bag = user1.getBag();
		
		//Only the basic weapons
		assertEquals(1, bag.getOtherPropDatas().size());
		battleManager.pickReward(user1, new int[] { 1, 3, 5, 7 });

		// Clean system.
//		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}

	@Test
	public void testPickReward2() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		// 容错测试: Null
		battleManager.pickReward(battleUser.getUser(), new int[] { 1 });
		assertEquals(50, battleUser.getUser().getExp());

		battleUser.getUser().setBattleRewards(rewardList);
		// 容错测试: ArrayOutofIndex
		battleManager.pickReward(battleUser.getUser(), new int[] { 1 });

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testPickRewardEveryone() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();
		//Start the checker.
		battleManager.init();

		Battle battle = makeReadyBattle(battleManager);

		//An user is dead, battle is over.
		BattleUser deadUser = battle.getBattleUserMap().values().iterator().next();
		battleManager.roleDead(deadUser.getUserSessionKey(), null);
		
		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();
		Reward reward = new Reward();
		reward.setPropId("-1");
		reward.setPropCount(100);
		reward.setType(RewardType.EXP);
		rewardList.add(reward);
		
		ArrayList<BattleUser> users = new ArrayList<BattleUser>(battle.getBattleUserMap().values());
		for (Iterator iterator = users.iterator(); 
				iterator.hasNext();) {
			//Wait the checker to run.
			Thread.sleep(200);
			
			BattleUser battleUser = (BattleUser) iterator.next();
			User user = battleUser.getUser();
			user.setExp(50);
			user.setBattleRewards(rewardList);
					
			user.setExp(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertTrue(actualUser.getExp()==150 || actualUser.getLevel()>1);
			User expectUser = user;
			assertEquals(expectUser.getExp(), actualUser.getExp());
			assertEquals(expectUser.getLevel(), actualUser.getLevel());
		}
		
		Thread.sleep(2000);
		//Check the battle is ended
		assertEquals(0, battleManager.findAllBattles().size());
		assertEquals(false, jedis.exists(battle.getBattleRoom().getSessionKey().toString()));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testSendChatMessage() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();
		//Start the checker.
		battleManager.init();

		Battle battle = makeReadyBattle(battleManager);
		BattleUser battleUser = battle.getBattleUserMap().values().iterator().next();

		battleManager.sendChatToAllUsers(battleUser.getUserSessionKey(), "Hello World");
		
		// Clean system.
		ArrayList<BattleUser> userList = new ArrayList(battle.getBattleUserMap().values());
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : userList ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testPickRewardOffline() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();
		//Start the checker.
		battleManager.init();

		Battle battle = makeReadyBattle(battleManager);

		//An user is dead, battle is over.
		BattleUser deadUser = battle.getBattleUserMap().values().iterator().next();
		battleManager.roleDead(deadUser.getUserSessionKey(), null);
		
		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();
		Reward reward = new Reward();
		reward.setPropId("-1");
		reward.setPropCount(100);
		reward.setType(RewardType.EXP);
		rewardList.add(reward);
		
		ArrayList<BattleUser> users = new ArrayList<BattleUser>(battle.getBattleUserMap().values());
		//The users become offline.
		for (BattleUser battleUser : users ) {
			jedis.del(battleUser.getUserSessionKey().toString());
		}
		
		Thread.sleep(2000);
		//Check the battle is ended
		assertEquals(0, battleManager.findAllBattles().size());
		assertEquals(false, jedis.exists(battle.getBattleRoom().getSessionKey().toString()));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testPickRewardExpires() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_checker_seconds, "1");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_max_live_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();
		//Start the checker.
		battleManager.init();

		Battle battle = makeReadyBattle(battleManager);

		//An user is dead, battle is over.
		BattleUser deadUser = battle.getBattleUserMap().values().iterator().next();
		battleManager.roleDead(deadUser.getUserSessionKey(), null);
		
		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		//Wait until timeout
		Thread.sleep(2000);
		//Check the battle is ended
		assertEquals(0, battleManager.findAllBattles().size());
		assertEquals(false, jedis.exists(battle.getBattleRoom().getSessionKey().toString()));

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardExp() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Exp
		{
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId("-1");
			reward.setPropCount(100);
			reward.setType(RewardType.EXP);
			rewardList.add(reward);
		
			battleUser.getUser().setExp(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertTrue(150 == actualUser.getExp() || actualUser.getLevel()>1);
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardGolden() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Golden
		{
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId("-1");
			reward.setPropCount(100);
			reward.setType(RewardType.GOLDEN);
			rewardList.add(reward);
		
			battleUser.getUser().setGolden(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertEquals(150, actualUser.getGolden());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUser().getSessionKey().toString());	
		}
	}

	@Test
	public void testPickRewardMedal() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Medal
		{
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId("-1");
			reward.setPropCount(100);
			reward.setType(RewardType.MEDAL);
			rewardList.add(reward);
		
			battleUser.getUser().setMedal(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertEquals(150, actualUser.getMedal());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardVoucher() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Voucher
		{
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId("-1");
			reward.setPropCount(100);
			reward.setType(RewardType.VOUCHER);
			rewardList.add(reward);
		
			battleUser.getUser().setVoucher(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertEquals(150, actualUser.getVoucher());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardYuanbao() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Yuanbao
		{
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId("-1");
			reward.setPropCount(100);
			reward.setType(RewardType.YUANBAO);
			rewardList.add(reward);
		
			battleUser.getUser().setYuanbao(50);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			assertEquals(150, actualUser.getYuanbao());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardItem() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);

		//Item
		{
			rewardList.clear();
			Reward reward = new Reward();
			//<item id="20001" lv="1" icon="GreenStoneLv1" name="水神石Lv1" info="与装备合成后提高幸运属性10点" />
			reward.setPropId("20001");
			reward.setPropCount(1);
			reward.setType(RewardType.ITEM);
			rewardList.add(reward);
		
			Bag bag = battleUser.getUser().getBag();
			bag.removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			UserManager.getInstance().queryUserBag(actualUser);
			assertEquals(reward.getPropId(), actualUser.getBag().getOtherPropData(Bag.BAG_WEAR_COUNT).getItemId());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testPickRewardWeapon() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		battleUser.getUser().setExp(50);
		battleUser.getUser().setBattleRewards(rewardList);
		
		//Weapon
		{
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
			rewardList.clear();
			Reward reward = new Reward();
			reward.setPropId(weapon.getTypeName());
			reward.setPropCount(1);
			reward.setPropLevel(3);
			reward.setPropColor(WeaponColor.WHITE);
			reward.setPropIndate(25);
			reward.setType(RewardType.WEAPON);
			rewardList.add(reward);
		
			Bag bag = battleUser.getUser().getBag();
			bag.removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
			battleManager.pickReward(battleUser.getUser(), new int[]{0});
			
			//Query user
			User actualUser = UserManager.getInstance().queryUser(battleUser.getUser().get_id());
			UserManager.getInstance().queryUserBag(actualUser);
			assertEquals(reward.getPropId(), actualUser.getBag().getOtherPropData(Bag.BAG_WEAR_COUNT).getItemId());
			assertEquals(reward.getPropCount(), actualUser.getBag().getOtherPropData(Bag.BAG_WEAR_COUNT).getCount());
			assertEquals(reward.getPropLevel(), actualUser.getBag().getOtherPropData(Bag.BAG_WEAR_COUNT).getLevel());
			assertEquals(reward.getPropColor(), actualUser.getBag().getOtherPropData(Bag.BAG_WEAR_COUNT).getColor());
		}

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testLeaveRoom() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();

		//User left battle
		battleManager.leaveBattle(battleUser.getUserSessionKey());

		assertEquals(1, battle.getBattleUserMap().size());
		assertEquals(false, jedis.hexists(battleUser.getUserSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(false, jedis.hexists(battleUser.getUserSessionKey().toString(), Battle.BATTLE_SERVER_KEY));
		
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testLeaveRoomOffline() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		Battle battle = makeReadyBattle(battleManager);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		ArrayList<Reward> rewardList = new ArrayList<Reward>();

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();

		//User is offline now.
		jedis.del(battleUser.getUserSessionKey().toString());
		
		//User left battle
		//The user's session is deleted, it cannot find battle object
		battleManager.leaveBattle(battleUser.getUserSessionKey());
		assertEquals(2, battle.getBattleUserMap().size());
		//Use this method again
		battle.leaveBattle(battleUser.getUserSessionKey());
		
		assertEquals(1, battle.getBattleUserMap().size());
		assertEquals(false, jedis.hexists(battleUser.getUserSessionKey().toString(), Battle.BATTLE_SESSION_KEY));
		assertEquals(false, jedis.hexists(battleUser.getUserSessionKey().toString(), Battle.BATTLE_SERVER_KEY));
		
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testMoreThanTwoUserMatch() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");
		manager.removeUser("test003");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}
		
		User user3 = manager.createDefaultUser();
		user3.set_id(new UserId("test003"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user3.setUsername("test003");
		for (int i = 0; i < 1; i++) {
			user3.addTool(makeBuffTool(i));
		}

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);
		manager.saveUser(user3, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);

		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);
		
		IoSession session3 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session3, user3);

		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user3, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);
		roomManager.readyStart(user3.getSessionKey(), true);
		
		Collection<Battle> battleCol = battleManager.findAllBattles();
		assertEquals(1, battleCol.size());
		
		Battle battle = battleCol.iterator().next();
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testUseToolHurtAdd10() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle2(battleManager, list, BuffToolType.Ice);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		int beforeThew = battleUser.getThew();
		int beforeDelay = battleUser.getDelay();
		
		BceRoleUseTool.Builder builder = BceRoleUseTool.newBuilder();
		builder.setSlot(BuffToolIndex.HurtAdd10.ordinal());
		
		battleManager.roleUseTool(battleUser.getUserSessionKey(), builder.build());
				
		assertTrue("use threw", battleUser.getThew() < beforeThew );
		assertTrue("add delay", battleUser.getDelay() > beforeDelay );
		assertTrue("has hurtAdd10", battleUser.getTools().contains(BuffToolType.HurtAdd10) );
		
		list.clear();
		
		BceRoleAttack.Builder attack = BceRoleAttack.newBuilder();
		attack.setAngle(30000);
		attack.setAtkAngle(30000);
		attack.setDirection(1);
		attack.setPower(30);
		attack.setUserx(120);
		attack.setUsery(20);
		battleManager.roleAttack(battleUser.getUserSessionKey(), attack.build());
		
		Thread.sleep(200);
		System.out.println(list);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testUseToolHurtIce() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle2(battleManager, list, BuffToolType.Ice);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		Iterator iter = battle.getBattleUserMap().values().iterator();
		BattleUser battleUser1 = (BattleUser)iter.next();
		battleUser1.setDelay(0);
		BattleUser battleUser2 = (BattleUser)iter.next();
		battleUser2.setDelay(10000);
		
		int beforeThew = battleUser1.getThew();
		int beforeDelay = battleUser1.getDelay();
		
		BceRoleUseTool.Builder builder = BceRoleUseTool.newBuilder();
		builder.setSlot(BuffToolIndex.UserTool1.ordinal());
		
		battleManager.roleUseTool(battleUser1.getUserSessionKey(), builder.build());
				
		assertTrue("use threw", battleUser1.getThew() < beforeThew );
		assertTrue("add delay", battleUser1.getDelay() > beforeDelay );
		assertTrue("has Iced",  battleUser1.getTools().contains(BuffToolType.Ice) );
		assertFalse("The tool should be deleted.", battleUser1.getUser().getTools().contains(BuffToolType.Ice));
		
		list.clear();
		/*
		BceRoleAttack.Builder attack = BceRoleAttack.newBuilder();
		attack.setAngle(30000);
		attack.setAtkAngle(30000);
		attack.setDirection(1);
		attack.setPower(5);
		attack.setUserx(120);
		attack.setUsery(20);
		battleManager.roleAttack(battleUser1.getUserSessionKey(), attack.build());
		 	*/
		list.clear();
		battleManager.roundOver(battleUser1.getUserSessionKey(), BceAskRoundOver.newBuilder().build());
				
		Thread.sleep(800);
		
		BseRoundStart roundStart = getBseRoundStart(list);
		assertNotNull(roundStart);
		assertEquals(battleUser2.getUserSessionKey().toString(), 
				roundStart.getSessionId());

		assertTrue(battleUser1.containStatus(RoleStatus.ICED));
		assertEquals(1, battleUser1.getFrozenStartRound());
		assertEquals(-1, battleUser2.getFrozenStartRound());
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testUseToolIce2() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle2(battleManager, list, BuffToolType.Ice);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		int beforeThew = battleUser.getThew();
		int beforeDelay = battleUser.getDelay();
		
		User actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		assertEquals(BuffToolType.Ice, actualUser.getTools().get(0));
		assertEquals(1, actualUser.getCurrentToolCount());
		
		BceRoleUseTool.Builder builder = BceRoleUseTool.newBuilder();
		builder.setSlot(BuffToolIndex.UserTool1.ordinal());
		
		battleManager.roleUseTool(battleUser.getUserSessionKey(), builder.build());
				
		assertTrue("use threw", battleUser.getThew() < beforeThew );
		assertTrue("add delay", battleUser.getDelay() > beforeDelay );
		assertTrue("has iced", battleUser.getTools().contains(BuffToolType.Ice) );
		assertTrue(battleUser.containStatus(RoleStatus.ICED));

		list.clear();
		
		int round = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TOOL_ICED_VALUE, 3);
		for ( int i=0; i<round; i++ ) {
			battle.roundStart();
			assertEquals(RoleStatus.ICED.toUserModeBit(), battleUser.convertStatusToUserBit());
		}
		battle.roundStart();
		assertEquals(RoleStatus.NORMAL.toUserModeBit(), battleUser.convertStatusToUserBit());
		
		actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		assertEquals(null, actualUser.getTools().get(0));
		assertEquals(0, actualUser.getCurrentToolCount());
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testUseToolHidden() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle2(battleManager, list, BuffToolType.Hidden);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		int beforeThew = battleUser.getThew();
		int beforeDelay = battleUser.getDelay();
		
		User actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		assertEquals(BuffToolType.Hidden, actualUser.getTools().get(0));
		assertEquals(1, actualUser.getCurrentToolCount());
		
		BceRoleUseTool.Builder builder = BceRoleUseTool.newBuilder();
		builder.setSlot(BuffToolIndex.UserTool1.ordinal());
		
		battleManager.roleUseTool(battleUser.getUserSessionKey(), builder.build());
				
		assertTrue("use threw", battleUser.getThew() < beforeThew );
		assertTrue("add delay", battleUser.getDelay() > beforeDelay );
		assertTrue("has hidden", battleUser.getTools().contains(BuffToolType.Hidden) );
		assertTrue(battleUser.containStatus(RoleStatus.HIDDEN));

		list.clear();
		
		int round = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.TOOL_HIDDEN_VALUE, 3);
		for ( int i=0; i<round; i++ ) {
			battle.roundStart();
			assertEquals(RoleStatus.HIDDEN.toUserModeBit(), battleUser.convertStatusToUserBit());
		}
		battle.roundStart();
		assertEquals(RoleStatus.NORMAL.toUserModeBit(), battleUser.convertStatusToUserBit());
		
		actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		assertEquals(null, actualUser.getTools().get(0));
		assertEquals(0, actualUser.getCurrentToolCount());
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testUseToolPower() throws Exception {
		Jedis jedis = JedisFactory.getJedis();

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle2(battleManager, list, BuffToolType.Energy);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		int beforeThew = battleUser.getThew();
		int beforeDelay = battleUser.getDelay();
		
		User actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		
		BceRoleUseTool.Builder builder = BceRoleUseTool.newBuilder();
		builder.setSlot(BuffToolType.POWER.ordinal());
		
		battleManager.roleUseTool(battleUser.getUserSessionKey(), builder.build());
				
		assertEquals("use threw", beforeThew, battleUser.getThew() );
		assertEquals("add delay", beforeDelay, battleUser.getDelay() );

		list.clear();
		
		battle.roundStart();
		assertEquals(RoleStatus.NORMAL.toUserModeBit(), battleUser.convertStatusToUserBit());
		
		actualUser = UserManager.getInstance().queryUser(
				battleUser.getUser().get_id());
		
		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}
	
	@Test
	public void testChooseBattleMap() {
		Battle battle = makeReadyBattle(BattleManager.getInstance());
		String left = "1";
		String right = "2";
		for ( int i=0; i<10; i++ ) {
			BattleBitSetMap map = battle.chooseBattleMap(left, right); 
			if ( !map.getMapId().equals(left) && !map.getMapId().equals(right) ) {
				fail("mapid = "+map.getMapId());
			}
		}
	}
	
	@Test
	public void testChooseBattleMapNullLeft() {
		Battle battle = makeReadyBattle(BattleManager.getInstance());
		String left = null;
		String right = "2";
		for ( int i=0; i<10; i++ ) {
			BattleBitSetMap map = battle.chooseBattleMap(left, right); 
			if ( !map.getMapId().equals(right) ) {
				fail("mapid = "+map.getMapId());
			}
		}
	}
	
	@Test
	public void testChooseBattleMapBothNull() {
		Battle battle = makeReadyBattle(BattleManager.getInstance());
		String left = null;
		String right = null;
		BattleBitSetMap map = battle.chooseBattleMap(left, right); 
		for ( int i=0; i<10; i++ ) {
			assertNotNull(map);
		}
	}
	
	@Test
	public void testChooseBattleMapInvalid() {
		Battle battle = makeReadyBattle(BattleManager.getInstance());
		String left = "1923923";
		String right = "23423423";
		BattleBitSetMap map = battle.chooseBattleMap(left, right); 
		for ( int i=0; i<10; i++ ) {
			assertNotNull(map);
		}
	}
	
	// ------------------------------------------------- TOOLS

	private Battle makeReadyBattle(BattleManager battleManager) {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(makeBuffTool(i));
		}

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		return battle;
	}

	private Battle makeReadyBattle2(BattleManager battleManager, ArrayList list, 
			BuffToolType tool) {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(tool);
		}

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(tool);
		}

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		return battle;
	}
	
	private BseRoundStart getBseRoundStart(List list) {
		BseRoundStart roundStart = null;
		for ( Object obj : list ) {
			if ( obj instanceof BseRoundStart ) {
				roundStart = (BseRoundStart)obj;
			} else if ( obj instanceof XinqiMessage ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseRoundStart ) {
					roundStart = (BseRoundStart)xinqi.payload;
				}
			}
		}
		return roundStart;
	}
	
	private Bag makeBag(User user, int count) {
		Bag bag = new Bag();
		// bag.set_id(user.get_id());
		// bag.setParentUser(user);
		for (int i = 0; i < count; i++) {
			bag.addOtherPropDatas(makePropData(1000 + i));
		}
		bag.wearPropData(Constant.BAG_WEAR_COUNT + 0,
				PropDataEquipIndex.WEAPON.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT + 1,
				PropDataEquipIndex.RING1.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT + 2,
				PropDataEquipIndex.RING2.index());
		return bag;
	}

	/**
	 * Make a fake PropData
	 * 
	 * @param i
	 * @return
	 */
	private PropData makePropData(int i) {
		PropData propData = new PropData();
		propData.setItemId("510");
		propData.setName("夺命刀" + i);
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}
	
	/**
	 * Make a fake PropData
	 * 
	 * @param i
	 * @return
	 */
	private BuffToolType makeBuffTool(int i) {
		if ( i >= 0 && i<BuffToolType.values().length ) {
			return BuffToolType.values()[i];
		} else {
			return BuffToolType.Recover;
		}
	}
}
