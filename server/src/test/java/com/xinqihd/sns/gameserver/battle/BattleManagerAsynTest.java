package com.xinqihd.sns.gameserver.battle;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.Message;
import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseAskRoundOver.BseAskRoundOver;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleAttack.BseRoleAttack;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.session.MessageQueue;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

/**
 * Test the distributed version of roleAttack
 * @author wangqi
 *
 */
public class BattleManagerAsynTest {

	String expectRpcServerId = "localhost:3445";

	public BattleManagerAsynTest() {
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
		
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.battle_distributed, "true");
		
		
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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRoleAttackWithoutFeedback() throws Exception {

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
		final ArrayList<Message> msgList = new ArrayList<Message>();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add((Message)msg);
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
		long timeOut = System.currentTimeMillis() + 10000;
		Message xinqi = null;
		while ( System.currentTimeMillis() < timeOut) {
			int size = msgList.size();
			for ( int i=0; i<size; i++ ) {
				Object xm = msgList.get(i);
				if ( xm instanceof BseAskRoundOver ) {
					xinqi = (Message)xm;
					break;
				}
			}
			Thread.sleep(200);
		}
		assertTrue( xinqi != null );

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
	}
	
	@Test
	public void testRoleAttackWithFeedback() throws Exception {

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
		final ArrayList<Message> msgList = new ArrayList<Message>();
		MessageQueue queue = createMock(MessageQueue.class);
		queue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				Object msg = getCurrentArguments()[1];
				msgList.add((Message)msg);
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
		
		Thread.sleep(1000);
		
		BulletTrack[] tracks = new BulletTrack[1];
		tracks[0] = new BulletTrack();
		battleManager.bulletTrack(bUser1.getUserSessionKey(), tracks, battle.getRoundCount(), null);

		// System.out.println(msgList);
		// 2 roleAttack messages, 2 roundStart messages.
		long timeOut = System.currentTimeMillis() + 10000;
		Message xinqi = null;
		while ( System.currentTimeMillis() < timeOut) {
			int size = msgList.size();
			for ( int i=0; i<size; i++ ) {
				Object xm = msgList.get(i);
				if ( xm instanceof BseRoleAttack ) {
					xinqi = (Message)xm;
					break;
				}
			}
			Thread.sleep(200);
		}
		assertTrue( xinqi != null );

		verify(queue);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
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
