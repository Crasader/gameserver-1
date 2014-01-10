package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.Field;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.forge.CraftManager;
import com.xinqihd.sns.gameserver.handler.BceLoginHandler;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBseAchievements.Achievement;
import com.xinqihd.sns.gameserver.proto.XinqiBseAchievements.BseAchievements;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddProp.BseAddProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.proto.XinqiBseForge.BseForge;
import com.xinqihd.sns.gameserver.proto.XinqiBseTask.BseTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseZip.BseZip;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class TaskManagerTest {

	private String userName = "test-001";
	
	@Before
	public void setUp() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Set<String> keys = jedisDB.keys("task:*");
		for ( String key : keys ) {
			jedisDB.del(key);
			System.out.println("Remove key " + key + " from Redis.");
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testStoreTaskData() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		
		String taskId = "100";
		manager.storeTaskSpecificData(user, taskId, TaskManager.Field.STEP, "100");
		String data = manager.queryTaskSpecificData(user, taskId, TaskManager.Field.STEP);
		assertEquals("100", data);
		
		manager.deleteTaskData(user, taskId);
		data = manager.queryTaskSpecificData(user, taskId, TaskManager.Field.STEP);
		assertEquals(null, data);
	}

	@Test
	public void testGetTaskById() {
		TaskPojo pojo = TaskManager.getInstance().getTaskById("1");
		assertNotNull(pojo);
	}
	
	@Test
	public void testBseZip() throws Exception {
		BseZip zip = TaskManager.getInstance().toBseZip();
		byte[] bytes = zip.getPayload().toByteArray();
		FileOutputStream fos = new FileOutputStream(new File("BseTasks.zip"));
		fos.write(bytes);
		fos.close();
	}

	@Test
	public void testGetTasks() {
		Collection<TaskPojo> maps = TaskManager.getInstance().getTasks();
		//now 194 tasks.
		assertTrue(maps.size()>=184);
		System.out.println("taskid"+"\t"+"taskName"+"\t"+
				"userLevel"+"\t"+"sequence"+"\t"+"step"+"\t"+
				"level"+"\t"+"type"+"\t"+"script" +"\t"+
				"target");
		for ( TaskPojo taskPojo : maps ) {
			System.out.println(taskPojo.getId()+"\t"+taskPojo.getName()+"\t"+
					taskPojo.getUserLevel()+"\t"+taskPojo.getSeq()+"\t"+taskPojo.getStep()+"\t"+
					taskPojo.getCondition1()+"\t"+taskPojo.getType()+"\t"+taskPojo.getScript() +"\t"+
					taskPojo.getTaskTarget());
			String script = taskPojo.getScript();
			boolean found = false;
			if ( script == Constant.EMPTY ) {
				continue;
			}
			for ( ScriptHook hook : ScriptHook.values() ) {
				if ( hook.getHook().equals(script) ) {
					found = true;
					break;
				}
			}
			assertTrue("Script: " + script, found);
			
			List<Award> awards = taskPojo.getAwards();
			for ( Award award : awards ) {
				System.out.println("award: " + award.id + "\t" + award.typeId );
			}
		}
	}
		
	@Test
	public void testGetTasksForType() {
		for ( TaskType type : TaskType.values() ) {
			Collection<TaskPojo> maps = TaskManager.getInstance().getTasksForType(type);
			System.out.println("--------- " + type + " -------------");
			for ( TaskPojo taskPojo : maps ) {
				System.out.println(taskPojo.getId()+"\t"+taskPojo.getName()+"\t"+
					taskPojo.getUserLevel()+"\t"+taskPojo.getSeq()+"\t"+taskPojo.getStep()+"\t"+
					taskPojo.getCondition1()+"\t"+taskPojo.getType());
			}
		}
	}
	
	@Test
	public void testGetTasksForLevel() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		
		user.setLevel(10);
		Collection<TaskPojo> tasks = manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE);
		
		System.out.println("taskid"+"\t"+"taskName"+"\t"+
				"userLevel"+"\t"+"sequence"+"\t"+"step"+"\t"+
				"level"+"\t"+"type"+"\t"+"script" +"\t"+
				"target");
		for ( TaskPojo taskPojo : tasks ) {
			System.out.println(taskPojo.getId()+"\t"+taskPojo.getName()+"\t"+
					taskPojo.getUserLevel()+"\t"+taskPojo.getSeq()+"\t"+taskPojo.getStep()+"\t"+
					taskPojo.getCondition1()+"\t"+taskPojo.getType()+"\t"+taskPojo.getScript() +"\t"+
					taskPojo.getTaskTarget());
		}
		
		tasks = manager.acquireFinishedTasks(user);
		
		System.out.println("taskid"+"\t"+"taskName"+"\t"+
				"userLevel"+"\t"+"sequence"+"\t"+"step"+"\t"+
				"level"+"\t"+"type"+"\t"+"script" +"\t"+
				"target");
		for ( TaskPojo taskPojo : tasks ) {
			System.out.println(taskPojo.getId()+"\t"+taskPojo.getName()+"\t"+
					taskPojo.getUserLevel()+"\t"+taskPojo.getSeq()+"\t"+taskPojo.getStep()+"\t"+
					taskPojo.getCondition1()+"\t"+taskPojo.getType()+"\t"+taskPojo.getScript() +"\t"+
					taskPojo.getTaskTarget());
		}
	}
	
	@Test
	public void testAcquireUserLevelUpTasks() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		Jedis jedisDB = JedisFactory.getJedisDB();
		/*
		 * 从'勇往直前lv5'到'勇往直前lv10'已经全部解锁
		 */
		user.setLevel(10);
		Collection<TaskPojo> tasks = manager.acquireUserLevelUpTasks(user, null, null, null);
		/*
		 * [TaskPojo [_id=6, name=勇往直前LV5, step=1, level=5, seq=0, userLevel=4], 
		 * TaskPojo [_id=7,  name=勇往直前LV6, step=1, level=6, seq=0, userLevel=5], 
		 * TaskPojo [_id=11, name=勇往直前LV7, step=1, level=7, seq=0, userLevel=6], 
		 * TaskPojo [_id=12, name=勇往直前LV8, step=1, level=8, seq=0, userLevel=7], 
		 * TaskPojo [_id=13, name=勇往直前LV9, step=1, level=9, seq=0, userLevel=8], 
		 * TaskPojo [_id=14, name=勇往直前LV10, step=1, level=10, seq=0, userLevel=9], 
		 * TaskPojo [_id=32, name=勇往直前LV11, step=1, level=11, seq=0, userLevel=10]]
		 */
		System.out.println(tasks);
		assertEquals(7, tasks.size());
		
		manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE);
		
		user.setLevel(15);
		tasks = manager.acquireUserLevelUpTasks(user, null, null, null);
		System.out.println(tasks);
		assertEquals(5, tasks.size());
	}
	
	/**
	  taskid	name	userlevel	sequence	step	level	type
		101	开始战斗吧	2	1	1	4	TASK_MAIN
		  1	第一次购买	3	2	1	1	TASK_MAIN
		  2	强化武器达到1级	4	3	1	0	TASK_MAIN
		  6	勇往直前LV5		4	4	1	0	TASK_MAIN
		  3	战斗开始		4	5	1	0	TASK_MAIN
	 */
	@Test
	public void testAssignNewTaskWithoutSameScriptType() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		Jedis jedisDB = JedisFactory.getJedisDB();
		
		user.setLevel(1);
		List<TaskPojo> tasks = new ArrayList<TaskPojo>(manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE));
		TaskPojo startCombat = null;
		for ( TaskPojo task : tasks ) {
			if ( task.getId().equals("101") ) {
				startCombat = task;
				break;
			}
		}
		assertNotNull(startCombat);
		
		//Test the same type tasks cannot be displayed together
		user.setLevel(2);
		
		manager.finishTask(user, startCombat.getId());
		
		tasks = new ArrayList<TaskPojo>(manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE));
		TaskPojo combatAgain = null;
		for ( TaskPojo task : tasks ) {
			if ( task.getId().equals("5") ) {
				combatAgain = task;
				break;
			}
		}
		assertNotNull(combatAgain);
		System.out.println(tasks);
		
		TaskPojo taskPojo = tasks.get(0);
		assertEquals("5", combatAgain.getId());
		assertEquals("再次战斗", combatAgain.getName());
		assertEquals(startCombat.getScript(), combatAgain.getScript());
		
	}
	
	@Test
	public void testAssignNewTaskWithLevelUp() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		Jedis jedisDB = JedisFactory.getJedisDB();
		
		user.setLevel(1);
		List<TaskPojo> tasks = new ArrayList<TaskPojo>(manager.assignNewTask(TaskType.TASK_MAIN, user));
		Collection<TaskPojo> todoTasks = manager.acquireTodoTasks(user);
		System.out.println(todoTasks);
		assertEquals(5, tasks.size());
		assertEquals(5, todoTasks.size());
		//[TaskPojo [_id=72, name=新手训练, step=1, level=0, seq=0, userLevel=1], 
		// TaskPojo [_id=279, name=绑定微博帐号吧, step=1, level=0, seq=1, userLevel=1], 
		// TaskPojo [_id=101, name=开始战斗吧, step=1, level=0, seq=2, userLevel=1], 
		// TaskPojo [_id=280, name=第一次分享微博, step=1, level=0, seq=3, userLevel=1], 
		// TaskPojo [_id=100, name=购买元宝物品送大礼, step=1, level=0, seq=4, userLevel=1]]
		System.out.println(tasks);
		
		user.setLevel(11);
		//When user level up, the todo tasks should not be added.
		todoTasks = manager.acquireTodoTasks(user);
		assertEquals(5, todoTasks.size());
		
		//There are no new tasks.
		assertNull(manager.assignNewTask(TaskType.TASK_MAIN, user));
		tasks = new ArrayList<TaskPojo>(user.getTasks(TaskType.TASK_MAIN));
		assertEquals(5, tasks.size());
		
		/**
		 * should be:
		 * 勇往直前LV10
		 * 勇往直前LV11
		 * 勇往直前LV12
		 * 第一次购买
		 * 强化武器达到1级
		 * 
		 */
		assertNull(manager.assignNewTask(TaskType.TASK_MAIN, user));
		tasks = new ArrayList<TaskPojo>(user.getTasks(TaskType.TASK_MAIN));
		System.out.println(tasks);
		if ( tasks != null ) {
			for ( TaskPojo task : tasks ) {
				System.out.println(task.getName());
			}
			assertEquals(5, tasks.size());
		}
	}
	
	@Test
	public void testAssignNewTaskAtLevel4() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		String username = "test-001";
		user.setUsername(username);
		user.setRoleName(username);
		user.set_id(new UserId(username));
		UserManager.getInstance().removeUser("test-001");
		UserManager.getInstance().saveUser(user, true);
		Jedis jedisDB = JedisFactory.getJedisDB();
		
		int level = 5;
		user.setLevel(1);
		manager.assignNewTask(TaskType.TASK_MAIN, user);
		for ( int i=1; i<level; i++) {
			user.setLevel(i);
			List<TaskPojo> tasks = new ArrayList(user.getTasks());
			//Recursivly finish and take rewards of all tasks
			while ( tasks.size() > 0 ) {
				for (TaskPojo task : tasks ) {
					if ( task.getType() == TaskType.TASK_ACHIVEMENT ) continue;
					System.out.println("================="+task);
					manager.finishTask(user, task.getId());
					manager.takeTaskReward(user, task.getId(), 0);
				}
				tasks = new ArrayList(user.getTasks());
			}
			i = user.getLevel();
		}
		
	}
	
	/**
	  taskid	name	usnerlevel	sequence	step	level	type
		101	开始战斗吧	2	1	1	4	TASK_MAIN
		  1	第一次购买	3	2	1	1	TASK_MAIN
		  2	强化武器达到1级	4	3	1	0	TASK_MAIN
		  6	勇往直前LV5		4	4	1	0	TASK_MAIN
		  3	战斗开始		4	5	1	0	TASK_MAIN
	 */
	@Test
	public void testAssignNewTaskWithLevelJump() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		TaskManager manager = TaskManager.getInstance();
		User user = prepareUser("test-001");
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		manager.deleteUserTasks(user);
		
		//Jump from level 1 to level 9
		user.setLevel(9);
		TaskManager.getInstance().assignNewTask(TaskType.TASK_MAIN, user);				
		Collection<TaskPojo> tasks = user.getTasks(TaskType.TASK_MAIN);
		assertEquals(5, tasks.size());
		
		//When user levelup, the old levelup tasks should be marked as finished.
		user.setLevel(10);

//		assertTrue("BseModiTask", list.size()>=1);
		
		ArrayList<TaskPojo> levelUpTasks = new ArrayList<TaskPojo>();
		tasks = manager.acquireFinishedTasks(user);
		assertTrue(tasks.size()>0);
		
		while ( true ) {
			tasks = manager.acquireFinishedTasks(user);
			for ( TaskPojo task : tasks ) {
				System.out.println("'"+task.getName()+"' is automatically finished.");
				levelUpTasks.add(task);
			}
			
			if ( levelUpTasks.size() <= 0 ) {
				break;
			}
			
			//Make sure all the "勇往直前LV" should be already marked as finished.
			String finishedSet = manager.getFinishedSetName(user);
			for ( TaskPojo task : levelUpTasks ) {
				assertTrue(task.getName()+" should be finished.", jedisDB.sismember(
					finishedSet, task.getId()));
				System.out.println(task.getName()+" has been marked finished.");
				
				//Take the reward
				manager.takeTaskReward(user, task.getId(), 0);
			}
			levelUpTasks.clear();
		}
	}
	
	@Test
	public void testAssignNewTaskPerformance() throws Exception {
		final TaskManager manager = TaskManager.getInstance();
		final User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		
		user.setLevel(2);
		TestUtil.doPerform(new Runnable() {
			public void run() {
				manager.assignNewTask(TaskType.TASK_MAIN, user);
			}
		}, "User Level 2", 10);
		
		user.setLevel(12);
		TestUtil.doPerform(new Runnable() {
			public void run() {
				manager.assignNewTask(TaskType.TASK_MAIN, user);
			}
		}, "User Level 12", 10);
	}
	
	@Test
	public void testAcquireTodoTasksEmpty() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevel(0);
		
		List<TaskPojo> expected = new ArrayList<TaskPojo>(manager.assignNewTask(TaskType.TASK_MAIN, user));
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		
		assertEquals(0, actual.size());
	}
	
	@Test
	public void testAcquireTodoTasks() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevel(4);
		
		Collection<TaskPojo> expected = user.getTasks(TaskType.TASK_MAIN);
		int actCount = user.getTasks(TaskType.TASK_ACTIVITY).size();
		int dailyCount = user.getTasks(TaskType.TASK_DAILY).size();
		HashSet<String> scripts = new HashSet<String>();
		for ( TaskPojo task : expected ) {
			assertTrue(!scripts.contains(task.getScript()));
			scripts.add(task.getScript());
		}
		System.out.println(expected);
		
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		
		System.out.println(actual);
		assertEquals(expected.size()+actCount+dailyCount, actual.size());
	}
	
	@Test
	public void testAcquireTodoTaskMaxCount() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevel(4);
		
		List<TaskPojo> expected = manager.assignNewTask(TaskType.TASK_MAIN, user, 2);
		
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		assertEquals(2, actual.size());
		
		//Get next 3
		expected = manager.assignNewTask(TaskType.TASK_MAIN, user, 3);
		assertEquals(3, expected.size());
		actual = manager.acquireTodoTasks(user);
		assertEquals(5, actual.size());
		
		//Should be empty now
		assertEquals(null, manager.assignNewTask(TaskType.TASK_MAIN, user));
		actual = manager.acquireTodoTasks(user);
		assertEquals(5, actual.size());
		
		//finish the script task "开始战斗吧"
		TaskPojo warTask = null;
		for (TaskPojo task : actual) {
			if ( task.getName().equals("开始战斗吧") ) {
				warTask = task;
				break;
			}
		}
		assertNotNull(warTask);
		manager.finishTask(user, warTask.getId());
		
		//完成了'开始战斗吧'这个任务后，同类型的‘再次战斗’才能出现
		manager.assignNewTask(TaskType.TASK_MAIN, user);
		expected = new ArrayList<TaskPojo>(user.getTasks(TaskType.TASK_MAIN));
		ArrayList<TaskPojo> expectedFinished = new ArrayList<TaskPojo>(user.getTaskFinished(TaskType.TASK_MAIN));
		//_id=5, name=再次战斗, step=1, level=0, seq=10, userLevel=2
		System.out.println(expected);
		assertEquals(4, expected.size());
		System.out.println(expectedFinished);
		assertEquals(1, expectedFinished.size());

	}
	
	@Test
	public void testFinishTask() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setLevel(4);
		TaskManager.getInstance().assignNewTask(TaskType.TASK_MAIN, user);
		
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		
		assertEquals(5, actual.size());
		
		//Finish a task
		TaskPojo task = actual.iterator().next();
		//Store task data
		manager.storeTaskSpecificData(user, task.getId(), Field.STEP, 10);
		assertEquals("10", manager.queryTaskSpecificData(user, task.getId(), Field.STEP));
		manager.finishTask(user, task.getId());
		assertEquals(null, manager.queryTaskSpecificData(user, task.getId(), Field.STEP));
		assertTrue( !user.getTasks().contains(task) );
		assertTrue( !user.getTasks(task.getType()).contains(task) );
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task.getId()));
	}
	
	@Test
	public void testTakeTaskRewardWithWeapon() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(4);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		user.setExp(0);
		user.setGolden(0);
		user.setVoucher(0);
		user.setMedal(0);
		user.setYuanbao(0);
		
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		manager.assignNewTask(TaskType.TASK_MAIN, user, 3);
		ArrayList<TaskPojo> actual = new ArrayList<TaskPojo>(manager.acquireTodoTasks(user));
		
		assertEquals(3, actual.size());
		
		//Finish a task
		TaskPojo task = actual.get(1);
		manager.finishTask(user, task.getId());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task.getId()));
		
		assertTrue(task.getExp() == user.getExp() || user.getLevel()>1 );
		assertEquals(task.getGold(), user.getGolden());
		assertEquals(task.getTicket(), user.getVoucher());
		assertEquals(task.getGongxun(), user.getMedal());
		assertEquals(task.getCaifu(), user.getYuanbao());
		
//		assertEquals(1, user.getBag().getOtherPropDatas().size());
	}
	
	@Test
	public void testTakeTaskRewardWeaponByType() throws Exception {
		//Clean old task data
		JedisUtil.cleanRedis();
		
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.setLevel(4);
		user.getBag().removeOtherPropDatas(20);
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone2(user, 0, 1, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager craftManager = CraftManager.getInstance();
		TaskManager manager = TaskManager.getInstance();
		manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE);
		
		int i = 0;
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			list.clear();
			craftManager.forgeEquip(user, 20, new int[]{21, 22, 23, 24, 25});
			
			Thread.sleep(500);
			
			BseForge forge = null;
			BseAddProp addProp = null;
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseForge ) {
					forge = (BseForge)xinqi.payload;
				} else if ( xinqi.payload instanceof BseAddProp ) {
					addProp = (BseAddProp)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( forge.getResult() == 1 ) {
				//BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData newPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, newPropData.getId());
				//For strength action, only the damage is set a new value.
				//The attack and defend will not change. they are affected
				//by forge action instead.
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				int damage = newPropData.getDamageLev();
				int skin = newPropData.getSkinLev();
				assertTrue("damage="+damage, damage>=1);
				assertTrue("skin="+skin, skin>=0);
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertEquals(null, bag.getOtherPropData(25));
				
				//删除旧的道具
				bag = user.getBag();
				bag.removeOtherPropDatas(20);
				System.out.println("try: " + i);
				
				//Find the task 2
				// [_id=8, name=强化武器达到2级, step=1, level=2, seq=3, userLevel=4]
				Collection<TaskPojo> actual = user.getTasks(TaskType.TASK_MAIN);
				//Finish a task
				TaskPojo task2 = null;
				for ( TaskPojo task : actual ) {
					if ( task.getId().equals("8") ) {
						task2 = task;
						break;
					}
				}
				assertNotNull(task2);
				boolean success = manager.finishTask(user, task2.getId());
				assertTrue(success);
				success = manager.takeTaskReward(user, task2.getId(), -1);
				assertTrue(success);
				//任务2的奖励为: 20001水神石
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals("20001", bag.getOtherPropData(20).getItemId());
				break;
			} else {
				assertEquals(1, list.size());
				assertEquals(0, forge.getOtherPewsCount());
			}
		}
		
	}
	
	@Test
	public void testTakeTaskRewardWithItem() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(4);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		user.setExp(0);
		user.setGolden(0);
		user.setVoucher(0);
		user.setMedal(0);
		user.setYuanbao(0);
		
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		manager.assignNewTask(TaskType.TASK_MAIN, user, 5);
		ArrayList<TaskPojo> actual = new ArrayList<TaskPojo>(manager.acquireTodoTasks(user));
		
		assertEquals(5, actual.size());
		TaskPojo task = null;
		for ( TaskPojo t : actual ) {
			if ( t.getId().equals("101") ) {
				task = t;
				break;
			}
		}
		assertNotNull(task);
		//Finish a task
		//3	战斗开始
		assertEquals("101", task.getId());
		manager.finishTask(user, task.getId());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task.getId()));
		
		assertTrue(task.getExp() == user.getExp() || user.getLevel()>1 );
		assertEquals(task.getGold(), user.getGolden());
		assertEquals(task.getTicket(), user.getVoucher());
		assertEquals(task.getGongxun(), user.getMedal());
		assertEquals(task.getCaifu(), user.getYuanbao());
		
//		assertEquals(1, user.getBag().getOtherPropDatas().size());
	}
	
	/**
		<task id="9"  name="装备合成"  taskTarget=" 成功将1级水神石合成到装备上" step="1" 
				level="0" seq="12" userLevel="8" desc="进入铁匠铺，通过合成功能将水神石合成到装备上" 
				parent="1" exp="1500" gold="2000" ticket="70" gongxun="0" caifu="0">
			<award type="item" id="20001" lv="2" sex="3"  count="1"  indate="2400000" />
			<award type="item" id="999999" lv="0" sex="3"  count="40"  indate="2400000" />
		</task>
	 */
	@Test
	public void testTakeTaskRewardWithItem99999() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(8);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		user.setExp(0);
		user.setGolden(0);
		user.setVoucher(0);
		user.setMedal(0);
		user.setYuanbao(0);
		
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		manager.assignNewTask(TaskType.TASK_MAIN, user, Integer.MAX_VALUE);
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		
		//Finish a task
		TaskPojo task = null;
		for ( TaskPojo t : actual ) {
			if ( t.getId().equals("9") ) {
				task = t;
				break;
			}
		}
		//3	战斗开始
		assertEquals("9", task.getId());
		manager.finishTask(user, task.getId());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task.getId()));
		
		assertTrue(task.getExp() == user.getExp() || user.getLevel()>1 );
		assertEquals(task.getGold(), user.getGolden());
		assertEquals(task.getTicket(), user.getVoucher());
		assertEquals(task.getGongxun(), user.getMedal());
		assertEquals(task.getCaifu(), user.getYuanbao());
		
		/*		   
			PropData [itemId=25017, name=升级奖励Lv7, pew=20], 
			PropData [itemId=25018, name=升级奖励Lv8, pew=21], 
			PropData [itemId=25019, name=升级奖励Lv9, pew=22], 
			PropData [itemId=20002, name=水神石Lv2, pew=23], 
			PropData [itemId=999999, name=勋章, pew=24]
		 */
		// 999999 item is removed and the medal is added to user account
		//TaskId 9 medal : 40
		//Medal is cancelled and merged into golden
		assertEquals(3, user.getBag().getOtherPropDatas().size());
		assertEquals(0, user.getMedal());
		assertEquals(200, user.getGolden());
		PropData stone = null;
		for ( int i=0; i<user.getBag().getCurrentCount(); i++ ) {
			PropData p = user.getBag().getOtherPropDatas().get(i);
			if ( p.getItemId().equals("20002") ) {
				//<item id="20001" lv="2" icon="GreenStoneLv2" name="水神石Lv2" info="与装备合成后提高幸运属性20点" />
				stone = p;
			}
		}
		assertNotNull(stone);
	}
	
	@Test
	public void testTakeTaskRewardRepeated() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(4);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		user.setExp(0);
		user.setGolden(0);
		user.setVoucher(0);
		user.setMedal(0);
		user.setYuanbao(0);
		
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		manager.assignNewTask(TaskType.TASK_MAIN, user, 5);
		ArrayList<TaskPojo> actual = new ArrayList<TaskPojo>(manager.acquireTodoTasks(user));
		
		assertEquals(5, actual.size());
		TaskPojo task = null;
		for ( TaskPojo t : actual ) {
			if ( t.getId().equals("101") ) {
				task = t;
				break;
			}
		}
		assertNotNull(task);
		//Finish a task
		//3	战斗开始
		assertEquals("101", task.getId());
		manager.finishTask(user, task.getId());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task.getId()));
		
		for ( int i=0; i<10; i++ ) {
			manager.takeTaskReward(user, task.getId(), 0);
		}
		
		assertTrue(task.getExp() == user.getExp() || user.getLevel()>1 );
		assertEquals(task.getGold(), user.getGolden());
		assertEquals(task.getTicket(), user.getVoucher());
		assertEquals(task.getGongxun(), user.getMedal());
		assertEquals(task.getCaifu(), user.getYuanbao());
		
//		assertEquals(1, user.getBag().getOtherPropDatas().size());
	}
	
	@Test
	public void testRefreshDailyTask() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(10);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
				
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);

		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		//Try to assign new tasks.
		manager.assignNewTask(TaskType.TASK_DAILY, user, 5);

		ArrayList<TaskPojo> actual = new ArrayList<TaskPojo>();
		for ( TaskPojo task : manager.acquireTodoTasks(user) ) {
			if ( task.getType() == TaskType.TASK_DAILY ) {
				actual.add(task);
			} else {
				jedisDB.srem(todoSet, task.getId());
			}
		}
		assertEquals(5, actual.size());
		
		//Finish a task
		TaskPojo task1 = actual.iterator().next();
		manager.finishTask(user, task1.getId());
				
		assertEquals(false, jedisDB.sismember(todoSet, task1.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task1.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task1.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task1.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task1.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task1.getId()));

		//Finish second task but does not take reward
		TaskPojo task2 = actual.iterator().next();
		manager.finishTask(user, task2.getId());
		assertEquals(false, jedisDB.sismember(todoSet, task2.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task2.getId()));
		
		//Store task data
		TaskPojo task3 = actual.iterator().next();
		manager.storeTaskSpecificData(user, task3.getId(), Field.STEP, "hello");
		
		//Now refresh daily task
		manager.refreshDailyTask(user);
		assertEquals(0, jedisDB.scard(todoSet).intValue());
		assertEquals(0, jedisDB.scard(finishedSet).intValue());
		assertEquals(0, jedisDB.scard(awardedSet).intValue());
		assertEquals(null, manager.queryTaskSpecificData(user, task3.getId(), Field.STEP));
	}
	
	@Test
	public void testDeleteUserTasks() {
		TaskManager manager = TaskManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setLevel(10);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
				
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		manager.assignNewTask(TaskType.TASK_DAILY, user, 5);
		Collection<TaskPojo> actual = manager.acquireTodoTasks(user);
		assertEquals(5, actual.size());
		
		//Finish a task
		TaskPojo task1 = actual.iterator().next();
		manager.finishTask(user, task1.getId());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String todoSet = TaskManager.getTodoSetName(user);
		String finishedSet = TaskManager.getFinishedSetName(user);
		String awardedSet = TaskManager.getAwardedSetName(user);
		
		assertEquals(false, jedisDB.sismember(todoSet, task1.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task1.getId()));
		
		//Take reward
		manager.takeTaskReward(user, task1.getId(), 0);
		assertEquals(false, jedisDB.sismember(todoSet, task1.getId()));
		assertEquals(false, jedisDB.sismember(finishedSet, task1.getId()));
		assertEquals(true, jedisDB.sismember(awardedSet, task1.getId()));

		//Finish second task but does not take reward
		TaskPojo task2 = actual.iterator().next();
		manager.finishTask(user, task2.getId());
		assertEquals(false, jedisDB.sismember(todoSet, task2.getId()));
		assertEquals(true, jedisDB.sismember(finishedSet, task2.getId()));
		
		//Store task data
		TaskPojo task3 = actual.iterator().next();
		manager.storeTaskSpecificData(user, task3.getId(), Field.STEP, "hello");
		
		//Now refresh daily task
		manager.deleteUserTasks(user);
		
		assertEquals(0, jedisDB.scard(todoSet).intValue());
		assertEquals(0, jedisDB.scard(finishedSet).intValue());
		assertEquals(0, jedisDB.scard(awardedSet).intValue());
		assertEquals(0, jedisDB.keys("task:"+userName+"*").size());
	}

	@Test
	public void testToBseTask() {
		User user = new User();
		user.setGender(Gender.FEMALE);
		BseTask bseTask = TaskManager.getInstance().toBseTask(user);
		assertTrue(bseTask.getTasksCount()>=184);
		System.out.println(bseTask.getSerializedSize());
	}
	
	@Test
	public void testLuaString() {
		TaskManager manager = TaskManager.getInstance();
		Collection<TaskPojo> tasks = manager.getTasks();
		for ( TaskPojo task : tasks ) {
			System.out.println(task.toLuaString(Locale.CHINESE));
		}
	}
	
	/**
	 * The user upgrade task is moved to BceLoginHandler
	 * @throws Exception
	 */
	@Test
	public void testGetLoginUserTask() throws Exception {
		TaskManager manager = TaskManager.getInstance();
		User user = prepareUser(userName);
		user.setLevel(20);
				
		Date currentDate = new Date();
		if ( user.getLdate().getDate() != currentDate.getDate() ) {
			GameContext.getInstance().getTaskManager().refreshDailyTask(user);
		}
		Collection<TaskPojo> tasks = manager.getUserLoginTasks(user);

		for ( TaskType type : TaskType.values() ) {
			Set<TaskPojo> typeTasks = user.getTasks(type);
			System.out.println("type=" +type);
			if ( type==TaskType.TASK_ACHIVEMENT ||
					type==TaskType.TASK_RANDOM) {
			} else {
				assertTrue(typeTasks.size()<=5);
			}
		}
		
		/*
		 * Previously, the login task is checked in the #getUserLoginTasks method.
		 * Now it is moved into BceLoginHandler, because the ModiTask should be sent
		 * after TaskList. Now we have to call the script manully.
		 * wangqi 2012-5-14
		 */
		//Call the script hook here
		GameContext.getInstance().getTaskManager().processUserTasks(user, 
				TaskHook.LOGIN);
		Collection<TaskPojo> finishedTask = manager.acquireFinishedTasks(user);
		//TaskPojo [_id=149, name=登陆有奖相送
		System.out.println(finishedTask);
		//assertEquals(6, finishedTask.size());
		
		//Get the finished task and take award
		TaskPojo levelTask = null;
		for ( TaskPojo task : user.getTasks() ) {
			if ( task.getName().contains("勇往直前") ) {
				levelTask = task;
				break;
			}
		}
		assertTrue("Find: 勇往直前", levelTask!=null);
		
		int expectTaskNumber = user.getTasks().size();
		for ( TaskPojo task : user.getTasks() ) {
			System.out.println(task.getName());
		}
		System.out.println("now finish tasks and take rewards");
		finishedTask = manager.acquireFinishedTasks(user);
		int expectSize = finishedTask.size();
		manager.finishTask(user, levelTask.getId());
		manager.takeTaskReward(user, levelTask.getId(), 0);
		for ( TaskPojo task : user.getTasks() ) {
			System.out.println(task.getName());
		}
		//Check now
		/**
		 * 领取任务奖励后，将分配‘勇往直前lv10’任务，这个任务会
		 * 被标示为自动完成，因此总任务数还是30
		 */
		finishedTask = manager.acquireFinishedTasks(user);
		assertTrue(finishedTask.size()+"="+expectSize, 
				finishedTask.size()>=expectSize);
		
		//Set the todo task list to user's field.
//		System.out.println(tasks);
		
		//Check the login tasks again
		user = UserManager.getInstance().queryUser(userName);
		tasks = manager.getUserLoginTasks(user);
		for ( TaskType type : TaskType.values() ) {
			Set<TaskPojo> typeTasks = user.getTasks(type);
			System.out.println("type=" +type);
			if ( type==TaskType.TASK_ACHIVEMENT ||
					type==TaskType.TASK_RANDOM) {
			} else {
				assertTrue(typeTasks.size()<=5);
			}
		}
	}
	
	@Test
	public void testUnlockAchievement() throws Exception {
		TaskManager manager = TaskManager.getInstance();
		User user = prepareUser(userName);
		//Clean redis
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.del(TaskManager.getFinishedSetName(user));
		jedisDB.del(TaskManager.getAwardedSetName(user));
		
		//set level
		user.setLevel(20);
		
		//assertEquals(5, manager.acquireFinishedTasks(user).size());
		
		Date currentDate = new Date();
		if ( user.getLdate().getDate() != currentDate.getDate() ) {
			GameContext.getInstance().getTaskManager().refreshDailyTask(user);
		}
		manager.getUserLoginTasks(user);
		Set<TaskPojo> achievements = user.getTasks(TaskType.TASK_ACHIVEMENT);
		assertTrue(achievements.size()>0);
		for ( TaskPojo task : achievements ) {
			task.setBroadcast(true);
			manager.setTaskById(task);
			System.out.println(task);			
		}
		//模拟解锁这个成就
		//200	踏上征途	第一次登录游戏	完成一次登录游戏	0
		XinqiMessage message = new XinqiMessage();
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword(userName);
		BceLogin msg = payload.build();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		BceLoginHandler handler = BceLoginHandler.getInstance();
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		handler.messageProcess(session, message, null);
		Thread.sleep(500);
		BseFinishAchievement finishAchieve = null;
		for ( XinqiMessage xinqi : list ) {
			if (  xinqi.payload instanceof BseFinishAchievement ) {
				BseFinishAchievement fa = (BseFinishAchievement)xinqi.payload;
				if ( fa.getId().equals("200") ) {
					finishAchieve = fa;
					break;
				}
			}
		}
		assertNotNull(finishAchieve);
		
		//login again to test
		list.clear();
		finishAchieve = null;
		handler.messageProcess(session, message, null);
		for ( XinqiMessage xinqi : list ) {
			if (  xinqi.payload instanceof BseFinishAchievement ) {
				BseFinishAchievement fa = (BseFinishAchievement)xinqi.payload;
				if ( fa.getId().equals("229") ) {
					finishAchieve = fa;
					break;
				}
				break;
			}
		}
		assertNull(finishAchieve);
	}
	
	@Test
	public void testGetUserUnlockAchievement() throws Exception {
		TaskManager manager = TaskManager.getInstance();
		User user = prepareUser(userName);
		user.setLevel(20);
		
		//assertEquals(5, manager.acquireFinishedTasks(user).size());
		
		Date currentDate = new Date();
		if ( user.getLdate().getDate() != currentDate.getDate() ) {
			GameContext.getInstance().getTaskManager().refreshDailyTask(user);
		}
		manager.getUserLoginTasks(user);
		Set<TaskPojo> achievements = user.getTasks(TaskType.TASK_ACHIVEMENT);
		assertTrue(achievements.size()>0);
		for ( TaskPojo task : achievements ) {
			task.setBroadcast(true);
			manager.setTaskById(task);
			System.out.println(task);			
		}
		//模拟解锁这个成就
		//229	踏上征途	第一次登录游戏	完成一次登录游戏	0
		XinqiMessage message = new XinqiMessage();
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword(userName);
		BceLogin msg = payload.build();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		BceLoginHandler handler = BceLoginHandler.getInstance();
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		handler.messageProcess(session, message, null);
		Thread.sleep(500);
		BseFinishAchievement finishAchieve = null;
		for ( XinqiMessage xinqi : list ) {
			if (  xinqi.payload instanceof BseFinishAchievement ) {
				BseFinishAchievement fa = (BseFinishAchievement)xinqi.payload;
				if ( fa.getId().equals("200") ) {
					finishAchieve = fa;
					break;
				}
			}
		}
		assertNotNull(finishAchieve);
		
		//login again to test
		List<String> unlockList = manager.getAllUnlockedAchievements(user);
		assertEquals(1, unlockList.size());
		assertEquals("200", unlockList.get(0));
	}
	
	@Test
	public void testCheckAllAwards() {
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		int max = 0;
		for ( TaskPojo task : tasks ) {
			List<Award> awards = task.getAwards();
			if ( max < awards.size() ) {
				max = awards.size();
			}
			for ( Award a : awards ) {
				if ( "item".equals(a.type) ) {
					String id = a.id;
					ItemPojo ip = ItemManager.getInstance().getItemById(id);
					assertNotNull("taskId="+task.getId()+",itemid="+id, ip);
				} else {
					String id = a.id;
					String typeId = String.valueOf(a.typeId);
					assertEquals("taskid="+task.getId(), Constant.ONE_NEGATIVE, id);
					List<WeaponPojo> weapon = EquipManager.getInstance().getWeaponsByTypeName(typeId);
					assertTrue(weapon.size()>0);
				}
			}
		}
		System.out.println("max = " + max);
	}
	
	@Test
	public void testToAchievements() {
		TaskManager manager = TaskManager.getInstance();
		BseAchievements ach = manager.toBseAchievement();
		assertTrue("total achievements: " + ach.getAchievementsCount(), 
				ach.getAchievementsCount()>0);
		HashSet<String> ids = new HashSet<String>();
		for ( int i=0; i<ach.getAchievementsCount(); i++ ) {
			Achievement a = ach.getAchievements(i);
			assertTrue("id duplicate:"+a.getId(), !ids.contains(a.getId()));
			assertNotNull("title:"+a.getTitle(), a.getTitle());
			assertNotNull("info:"+a.getInfo(), a.getInfo());
			//assertNotNull("icon:"+a.getInfo(), a.getIcon());
			assertTrue("score:"+a.getScore(), a.getScore()>0);
			System.out.println(a.getId() );
		}
	}
	
	@Test
	public void testTaskSubSequence() throws Exception {
		TaskManager manager = TaskManager.getInstance();
		User user = prepareUser("test-001");
		user.setLevelSimple(50);
		manager.assignNewTask(TaskType.TASK_SUB, user, Integer.MAX_VALUE);
		while ( true ) {
			Collection<TaskPojo> tasks = new ArrayList<TaskPojo>(user.getTasks(TaskType.TASK_SUB));
			if ( tasks.size() == 0 ) break;
			for ( TaskPojo task : tasks ) {
				System.out.println(task.getId()+","+task.getName());
				manager.finishTask(user, task.getId());
				manager.takeTaskReward(user, task.getId(), 0);
			}
		}
	}
	
	@Test
	public void testCompareTaskPojo() {
		/**
		 * 238使用UserLevelUp，比较特殊
		 */
		TaskPojo firstTask = TaskManager.getInstance().getTaskById("200");
		TaskPojo secondTask = TaskManager.getInstance().getTaskById("238");
		int result = secondTask.compareTo(firstTask);
		assertTrue(result>0);
		result = firstTask.compareTo(secondTask);
		assertTrue(result<0);
		
		Collection<TaskPojo> tasks = TaskManager.getInstance().assignNewTask(
				TaskType.TASK_ACHIVEMENT, new User(), Integer.MAX_VALUE);
		for ( TaskPojo task : tasks ) {
			System.out.println(task.getId()+", "+task.getName());
		}
	}
	
	/**
	 * 当玩家不断升级时，如果累计任务一直不领取，可能造成任务数量超过5个的错误
	 * 测试这一问题
	 * @throws Exception 
	 */
	@Test
	public void testUserLevelUpWithoutAcceptTask() throws Exception {
		User user = prepareUser("test-001");
		TaskManager manager = TaskManager.getInstance();
		for ( int level = 1; level < LevelManager.MAX_LEVEL; level++ ) {
			user.setLevel(level);
			for ( TaskType taskType : TaskType.values() ) {
				manager.assignNewTask(taskType, user);
			}
			for ( TaskType taskType : TaskType.values() ) {
				if ( taskType == TaskType.TASK_ACHIVEMENT ) continue;
				int actualSize = user.getTasks(taskType).size();
				assertTrue("actualSize:"+actualSize+" should be <= 5", actualSize<=5);
			}
			manager.getUserLoginTasks(user);
			for ( TaskType taskType : TaskType.values() ) {
				if ( taskType == TaskType.TASK_ACHIVEMENT ) continue;
				int actualSize = user.getTasks(taskType).size();
				assertTrue("actualSize:"+actualSize+" should be <= 5", actualSize<=5);
			}			
		}
	}
	
	/**
	 * 当玩家不断升级时，如果累计任务一直不领取，可能造成任务数量超过5个的错误
	 * 测试这一问题
	 * @throws Exception 
	 */
	@Test
	public void testUserLevelUpWithFinishedNotAcceptTask() throws Exception {
		User user = prepareUser("test-001");
		TaskManager manager = TaskManager.getInstance();
		for ( int level = 1; level < LevelManager.MAX_LEVEL; level++ ) {
			user.setLevel(level);
			for ( TaskType taskType : TaskType.values() ) {
				manager.assignNewTask(taskType, user);
			}
			for ( TaskType taskType : TaskType.values() ) {
				ArrayList<TaskPojo> tasks = new ArrayList<TaskPojo>(user.getTasks());
				for ( TaskPojo task : tasks ) {
					manager.finishTask(user, task.getId());
					manager.processUserTasks(user, TaskHook.USER_UPGRADE);
//					if ( task.getType() == TaskType.TASK_ACHIVEMENT ) {
//						if ( user.getLevel()>20 ) {
//							manager.takeTaskReward(user, task.getId(), 1);
//						}
//					}
				}
			}
			Jedis jedis = JedisFactory.getJedisDB();
			for ( TaskType taskType : TaskType.values() ) {
				if ( taskType == TaskType.TASK_ACHIVEMENT ) continue;
				int todoCount = user.getTasks(taskType).size();
				int finishCount = user.getTasks(taskType).size();
				int count = todoCount + finishCount;
				assertTrue("user level:"+user.getLevel()+
						", actualSize:"+count+" should be <= 5", count<=5);
			}
			manager.getUserLoginTasks(user);
			for ( TaskType taskType : TaskType.values() ) {
				if ( taskType == TaskType.TASK_ACHIVEMENT ) continue;
				int actualSize = user.getTasks(taskType).size();
				assertTrue("actualSize:"+actualSize+" should be <= 5", actualSize<=5);
			}			
		}
	}
	
	@Test
	public void testSaveTaskPojo() throws Exception {
		String COLL_NAME = "tasks";
		String databaseName = "babywar";
		String namespace = "server0001";
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		for ( DBObject obj : list ) {
			TaskPojo task = (TaskPojo)MongoDBUtil.constructObject(obj);
			MapDBObject dbObj = MongoDBUtil.createMapDBObject(task);
			DBObject query = MongoDBUtil.createDBObject("_id", task.getId());
			MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, "tasks", true);
		}
	}
		
	private User prepareUser(String userName) throws Exception {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		String password = StringUtil.encryptSHA1(userName);
		user.setPassword(password);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBagWithStone2(User user, int weaponLevel, int level, int luckyStone) {
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		//The weapon
		WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		weaponPojo.setLv(weaponLevel);
		PropData propData = weaponPojo.toPropData(30000, WeaponColor.BLUE);
//		propData.setAttackLev(100);
//		propData.setDefendLev(100);
		bag.addOtherPropDatas(propData);
		
		//Strength stones - up to 3
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20005_"+level);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById((20020+level)+"");
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());

		//God stone 
		ItemPojo godPojo = ItemManager.getInstance().getItemById("24001");
		bag.addOtherPropDatas(godPojo.toPropData());
		
		//Lucky stone
		if ( luckyStone == 1) {
			ItemPojo luckyPojo = ItemManager.getInstance().getItemById("24002");
			bag.addOtherPropDatas(luckyPojo.toPropData());
		} else if ( luckyStone == 2) {
			ItemPojo luckyPojo = ItemManager.getInstance().getItemById("24004");
			bag.addOtherPropDatas(luckyPojo.toPropData());
		}
		return bag;
	}
}
