package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import script.UserLevelUpgrade;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoUserManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseSysMessage.BseSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class UserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void testToBseRoleInfo() {
		User user = UserManager.getInstance().createDefaultUser();
		BseRoleInfo roleInfo = user.toBseRoleInfo();
		assertEquals(null, roleInfo);
		
		user.setUsername("test-001");
		user.set_id(new UserId("test-001"));
		roleInfo = user.toBseRoleInfo();
		assertNotNull(roleInfo);
	}

	@Test
	public void testToBseRoleBattleInfo() {
		User user = UserManager.getInstance().createDefaultUser();
		BseRoleBattleInfo roleInfo = user.toBseRoleBattleInfo();
		assertNotNull(roleInfo);
	}
	
	@Test
	public void testSetExp150() {
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setExp(50);
		TaskManager taskManager = TaskManager.getInstance();
		taskManager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//6	勇往直前LV5
		TaskPojo task = taskManager.getTaskById("6");
		//Upgrade to level 2 will trigger the script
		task.setCondition1(2);
		tasks.add(task);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		
		System.out.println("User original data: ");
		System.out.println("Level: " + user.getLevel());
		System.out.println("attack: " + user.getAttack());
		System.out.println("defend: " + user.getDefend());
		System.out.println("agility: " + user.getAgility());
		System.out.println("luck: " + user.getLuck());
		System.out.println("damage: " + user.getDamage());
		System.out.println("power: " + user.getPower());
		System.out.println("skin: " + user.getSkin());
		System.out.println("exp: " + user.getExp());
		
		//Save the user
		user.setExp(150);
		manager.saveUser(user, true);
		User actual = manager.queryUser(userName);
		assertEquals(user.getLevel(), actual.getLevel());
		assertEquals(user.getAttack(), actual.getAttack());
		assertEquals(user.getDefend(), actual.getDefend());
		assertEquals(user.getAgility(), actual.getAgility());
		assertEquals(user.getLuck(), actual.getLuck());
		assertEquals(user.getDamage(), actual.getDamage());
		assertEquals(user.getPower(), actual.getPower());
		assertEquals(user.getSkin(), actual.getSkin());
		assertEquals(user.getExp(), actual.getExp());
		
		System.out.println("User final data: ");
		System.out.println("Level: " + user.getLevel());
		System.out.println("attack: " + user.getAttack());
		System.out.println("defend: " + user.getDefend());
		System.out.println("agility: " + user.getAgility());
		System.out.println("luck: " + user.getLuck());
		System.out.println("damage: " + user.getDamage());
		System.out.println("power: " + user.getPower());
		System.out.println("skin: " + user.getSkin());
		System.out.println("exp: " + user.getExp());
	}

	@Test
	public void testSetExp() throws Exception {
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);

		TaskManager taskManager = TaskManager.getInstance();
		taskManager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//6	勇往直前LV5
		TaskPojo task = taskManager.getTaskById("6");
		//Upgrade to level 2 will trigger the script
		task.setCondition1(2);
		tasks.add(task);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		System.out.println("User original data: ");
		System.out.println("Level: " + user.getLevel());
		System.out.println("attack: " + user.getAttack());
		System.out.println("defend: " + user.getDefend());
		System.out.println("agility: " + user.getAgility());
		System.out.println("luck: " + user.getLuck());
		System.out.println("damage: " + user.getDamage());
		System.out.println("power: " + user.getPower());
		System.out.println("skin: " + user.getSkin());
		System.out.println("exp: " + user.getExp());
		
		user.setExp(10);
		assertEquals(0, list.size());
		
		user.setExp(6000);
		Thread.currentThread().sleep(200);
		
		assertTrue(list.size()>=1);
		boolean success = false;
		for ( XinqiMessage message : list ) {
			if ( message.payload instanceof BseModiTask ) {
				success = true;
				break;
			}
		}
		assertTrue(success);
		
		//Save the user
		manager.saveUser(user, true);
		User actual = manager.queryUser(userName);
		assertEquals(user.getLevel(), actual.getLevel());
		assertEquals(user.getAttack(), actual.getAttack());
		assertEquals(user.getDefend(), actual.getDefend());
		assertEquals(user.getAgility(), actual.getAgility());
		assertEquals(user.getLuck(), actual.getLuck());
		assertEquals(user.getDamage(), actual.getDamage());
		assertEquals(user.getPower(), actual.getPower());
		assertEquals(user.getSkin(), actual.getSkin());
		assertEquals(user.getExp(), actual.getExp());
		
		System.out.println("User final data: ");
		System.out.println("Level: " + user.getLevel());
		System.out.println("attack: " + user.getAttack());
		System.out.println("defend: " + user.getDefend());
		System.out.println("agility: " + user.getAgility());
		System.out.println("luck: " + user.getLuck());
		System.out.println("damage: " + user.getDamage());
		System.out.println("power: " + user.getPower());
		System.out.println("skin: " + user.getSkin());
		System.out.println("exp: " + user.getExp());
	}
	
	@Test
	public void testSetExpNoNegative() throws Exception {
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		LevelPojo level1 = LevelManager.getInstance().getLevel(1);
		LevelPojo level2 = LevelManager.getInstance().getLevel(2);
		int maxExp = level1.getExp() + level2.getExp() ;
		
		user.setExp(maxExp+1);
		assertEquals(3, user.getLevel());
		assertEquals(1, user.getExp());
	}
	
	@Test
	public void testSetExpForLevel1() throws Exception {
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);

		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		UserManager.getInstance().saveUser(user, true);
		
		LevelPojo level1 = LevelManager.getInstance().getLevel(1);
		int maxExp = level1.getExp();
		
		user.setExp(maxExp);
		assertEquals(2, user.getLevel());
		assertEquals(0, user.getExp());
		
		UserManager.getInstance().saveUser(user, false);
		User actualUser = UserManager.getInstance().queryUser(userName);
		assertEquals(2, actualUser.getLevel());
		assertEquals(0, actualUser.getExp());
	}
	
	@Test
	public void testSetExpForLevelSave() throws Exception {
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		LevelPojo level1 = LevelManager.getInstance().getLevel(1);
		int maxExp = level1.getExp()/2;
		
		user.setExp(maxExp);
		assertEquals(1, user.getLevel());
		assertEquals(maxExp, user.getExp());
	}
	
	@Test
	public void testSetMaxExp() throws Exception {
		int maxExp = 0;
		int lastLevelExp = 0;
		Collection<LevelPojo> levels = LevelManager.getInstance().getLevels();
		int count = 0;
		for ( LevelPojo level : levels ) {
			maxExp += level.getExp();
		}
		LevelPojo lastLevel = LevelManager.getInstance().getLevel(LevelManager.MAX_LEVEL);
		lastLevelExp = lastLevel.getExp();
		System.out.println("maxExp: " + maxExp + ", lastLevelExp: " + lastLevelExp);
		
		String userName = "test-001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		user.setExp(maxExp);
		assertEquals(LevelManager.MAX_LEVEL, user.getLevel());
		assertEquals(0, user.getExp());
		//add exp again.
		user.setExp(maxExp);
		assertEquals(0, user.getExp());
		
		System.out.println("User final data: ");
		System.out.println("Level: " + user.getLevel());
		System.out.println("attack: " + user.getAttack());
		System.out.println("defend: " + user.getDefend());
		System.out.println("agility: " + user.getAgility());
		System.out.println("luck: " + user.getLuck());
		System.out.println("damage: " + user.getDamage());
		System.out.println("power: " + user.getPower());
		System.out.println("skin: " + user.getSkin());
		System.out.println("exp: " + user.getExp());
	}
	
	@Test
	public void testToolCountSave() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		//Set the tool count
		user.setMaxToolCount(11);
		
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		User actual = manager.queryUser(userName);
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		//Set the current tool count
		user.setCurrentToolCount(9);
		manager.saveUser(user, false);
		actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
	}
	
	@Test
	public void testAddBuffToolTypeForNewUser() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		//Add a new BuffToolType
		user.addTool(BuffToolType.Recover);
		assertEquals(1, user.getCurrentToolCount());
				
		//Save as a new user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, true);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(BuffToolType.Recover, tools.get(0));
		
	}
	
	@Test
	public void testAddBuffToolTypeForUpdateUser() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		//Add a new BuffToolType
		user.addTool(BuffToolType.Recover);
		assertEquals(1, user.getCurrentToolCount());
				
		//Save user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(BuffToolType.Recover, tools.get(0));
		
	}
	
	@Test
	public void testAddBuffToolTypeMaxTimes() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		int maxCount = user.getMaxToolCount();
		
		//Add a new BuffToolType
		for ( int i=0; i<maxCount*2; i++ ) {
			user.addTool(BuffToolType.Recover);
		}
		assertEquals(3, user.getCurrentToolCount());
				
		//Save user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(BuffToolType.Recover, tools.get(0));
		
	}
	
	@Test
	public void testSetBuffToolType() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		int maxCount = user.getMaxToolCount() - 1;
		
		//Set a new BuffToolType
		user.setTool(maxCount, BuffToolType.Recover);
		assertEquals(1, user.getCurrentToolCount());
		
		//Set another BuffToolType on the same position
		user.setTool(maxCount, BuffToolType.Atom);
		assertEquals(1, user.getCurrentToolCount());
		
		//Set another BuffToolType on the first position
		user.setTool(0, BuffToolType.Hidden);
		assertEquals(2, user.getCurrentToolCount());

		//Save user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(BuffToolType.Hidden, tools.get(0));
		assertEquals(BuffToolType.Atom, tools.get(maxCount));
	}
	
	@Test
	public void testRemoveBuffToolType() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		int maxCount = user.getMaxToolCount();
		
		//Add a new BuffToolType
		for ( int i=0; i<maxCount; i++ ) {
			user.addTool(BuffToolType.Recover);
		}
		assertEquals(3, user.getCurrentToolCount());

		//Now remove one
		user.removeTool(1);
				
		//Save user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(null, tools.get(1));
	}
	
	@Test
	public void testAddBuffToolTypeAndRemove() {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		int maxCount = user.getMaxToolCount();
		
		//Add a new BuffToolType
		for ( int i=0; i<maxCount; i++ ) {
			user.addTool(BuffToolType.Recover);
		}
		assertEquals(3, user.getCurrentToolCount());

		//Now remove one
		user.removeTool(1);
		
		//Add a new one
		boolean result = user.addTool(BuffToolType.Atom);
		assertTrue("Add a new BuffToolType", result);
		assertEquals(BuffToolType.Atom, user.getTools().get(1));
		
		//Save user
		UserManager manager = UserManager.getInstance();
		manager.saveUser(user, false);
		
		//Query user
		User actual = manager.queryUser(userName);
		assertEquals(user.getCurrentToolCount(), actual.getCurrentToolCount());
		assertEquals(user.getMaxToolCount(), actual.getMaxToolCount());
		
		List<BuffToolType> tools = actual.getTools();
		assertEquals(BuffToolType.Atom, tools.get(1));
	}
	
	@Test
	public void testUserLevelGiftBox() {
		User user = prepareUser("test-001");
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		//User's level is not changed.
		user.setLevel(1);
		Bag bag = user.getBag();
		assertEquals(0, bag.getCurrentCount());
		
		user.setLevel(2);
		assertEquals(1, bag.getCurrentCount());
		PropData propData = bag.getOtherPropData(20);
		System.out.println(propData);
		/**
		<item id="25011" typeid="25011" lv="1" icon="Baoxiang0001" name="升级奖励Lv1" 
		info="对玩家努力升级的奖赏，VIP玩家会获得双倍奖励，达到1级可以打开, 包含100礼金、1个2级强化石。"
		 script="LevelUpBox" q="1" count="1">
		  <reward type="VOUCHER" id="-1" level="0" count="100" indate="0"/>
		  <reward type="ITEM" id="20022" level="0" count="1" indate="0"/>
		</item>
		 */
		assertEquals("25011", propData.getItemId());
		
		//Try to open it
		int oldGolden = user.getGolden();
		RewardManager.getInstance().openItemBox(user, 20);
		assertTrue( user.getGolden() > oldGolden );
		assertEquals(1, bag.getCurrentCount());
	}
	
	@Test
	public void testUserLevelGiftBoxSave() {
		User user = prepareUser("test-001");
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		//User's level is not changed.
		user.setLevel(1);
		Bag bag = user.getBag();
		assertEquals(0, bag.getCurrentCount());
		
		user.setLevel(2);
		assertEquals(1, bag.getCurrentCount());
		PropData propData = bag.getOtherPropData(20);
		System.out.println(propData);
		/**
		<item id="25011" typeid="25011" lv="1" icon="Baoxiang0001" name="升级奖励Lv1" 
		info="对玩家努力升级的奖赏，VIP玩家会获得双倍奖励，达到1级可以打开, 包含100礼金、1个2级强化石。"
		 script="LevelUpBox" q="1" count="1">
		  <reward type="VOUCHER" id="-1" level="0" count="100" indate="0"/>
		  <reward type="ITEM" id="20022" level="0" count="1" indate="0"/>
		</item>
		 */
		assertEquals("25011", propData.getItemId());
		
		//Query the user from database
		User actualUser = UserManager.getInstance().queryUser("test-001");
		UserManager.getInstance().queryUserBag(actualUser);
		assertEquals(1, actualUser.getBag().getCurrentCount());
	}
	
	@Test
	public void testAddTasks() {
		TaskManager manager = TaskManager.getInstance();
		TreeSet<TaskPojo> mainTasks = manager.getTasksForType(TaskType.TASK_MAIN);
		TreeSet<TaskPojo> dailyTasks = manager.getTasksForType(TaskType.TASK_DAILY);
		int count = 5;
		User user = new User();
		Iterator<TaskPojo> mainIter = mainTasks.iterator();
		Iterator<TaskPojo> dailyIter = dailyTasks.iterator();
		ArrayList<TaskPojo> tasks = new ArrayList<TaskPojo>();
		for ( int i=0; i<count; i++ ) {
			tasks.add(mainIter.next());
			tasks.add(dailyIter.next());
		}
		user.addTasks(tasks);
		assertEquals(count, user.getTasks(TaskType.TASK_MAIN).size());
		assertEquals(count, user.getTasks(TaskType.TASK_DAILY).size());
		assertEquals(count*2, user.getTasks().size());
		user.clearTasks();
		assertEquals(0, user.getTasks(TaskType.TASK_MAIN).size());
		assertEquals(0, user.getTasks(TaskType.TASK_DAILY).size());
		assertEquals(0, user.getTasks().size());

	}
	
	@Test
	public void testSetLevel9() throws Exception {
		String username = "test-001";
		User user = new User();
		user.setUsername(username);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);

		user.setLevel(9);
		
		Thread.sleep(200);
		System.out.println(list);
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseSysMessage ) {
				xinqi = msg;
			}
		}
		assertNotNull("BseSysMessage", xinqi);
		BseSysMessage msg = (BseSysMessage)xinqi.payload;
		assertEquals("恭喜，您已经升到了9级!", msg.getMessage());
		assertEquals(Type.LEVEL_UP, msg.getType());
	}
	
	@Test
	public void testSetLevel90() throws Exception {
		String username = "test-001";
		User user = new User();
		user.setUsername(username);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);

		user.setLevel(90);
		
		Thread.sleep(200);
		System.out.println(list);
		//Total 2 RoleInfo, BseFinishAchievement
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseSysMessage ) {
				xinqi = msg;
			}
		}
		assertNotNull("BseSysMessage", xinqi);
		BseSysMessage msg = (BseSysMessage)xinqi.payload;
		assertEquals("恭喜，您已经升到了90级!, '神圣'类型武器已解锁, 快去更新装备吧", msg.getMessage());
		assertEquals(Type.LEVEL_UP, msg.getType());
	}
	
	public void testMakeUserVIP() {
		//Make the user VIP expire
		String userName = "1111";
		User user = UserManager.getInstance().queryUser(userName);
		user.setIsvip(true);
		user.setVipbdate(new Date(System.currentTimeMillis()));
		user.setVipedate(new Date(System.currentTimeMillis()+86400000*3600));
		UserManager.getInstance().saveUser(user, false);
	}
	
	@Test
	public void testDisplayRoleName() {
		String roleName = "s0001.babywar";
		Pattern pattern = Pattern.compile("^s\\d\\d\\d\\d\\.");
		Matcher matcher = pattern.matcher(roleName);
		String r = matcher.replaceFirst(Constant.EMPTY);
		assertEquals("babywar", roleName);
	}
	
	@Test
	public void recalculateRank() throws Exception {
		String database = "babywar", namespace="server0001", collection="users";
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(
				null, database, namespace, collection, null);
		System.out.println("size: " + list.size());
		Thread.currentThread().sleep(1000);
		for ( DBObject dbObj : list ) {
			User user = ((MongoUserManager)MongoUserManager.getInstance()).
					constructUserObject(dbObj);
			UserManager.getInstance().queryUserBag(user);
			if ( user != null ) {
				int oldPower = user.getPower();
				UserLevelUpgrade.func(new Object[]{user});
				user.updatePowerRanking();
				System.out.println(user.getRoleName()+":"+oldPower+"->"+user.getPower());
			}
		}
	}
	
	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
