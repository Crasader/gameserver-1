package script.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.TaskPojo.Award;
import com.xinqihd.sns.gameserver.config.TaskType;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class LoginDateTest {

	String username = "test-001";
	
	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWithDay1() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);

		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 goledn
		task.setId("10000");
		task.setStep(7);
		task.setScript(ScriptHook.TASK_LOGIN_DATE.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("连续登陆");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//do mark today
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();		
		RewardManager.getInstance().takeDailyMarkReward(user, currentTime);
		
		//assertEquals(1, dailyMark.getTotalCount());

		Thread.sleep(200);
		System.out.println(list);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
		BseFinishAchievement ach = (BseFinishAchievement)xinqi.payload;
		assertEquals(14, ach.getPercent());
	}
	
	@Test
	public void testWithDay7() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);

		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 goledn
		task.setId("10000");
		task.setStep(7);
		task.setScript(ScriptHook.TASK_LOGIN_DATE.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("连续登陆");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//do mark today
		Calendar current = Calendar.getInstance();
		//7 days
		for ( int i=0; i<7; i++ ) {
			current.set(2012, 1, 15+i, 11, 38, 0);
			long currentTime = current.getTimeInMillis();		
			RewardManager.getInstance().takeDailyMarkReward(user, currentTime);
		}
		
		//assertEquals(1, dailyMark.getTotalCount());

		Thread.sleep(200);
		System.out.println(list);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				BseFinishAchievement bse = (BseFinishAchievement)msg.payload;
				if ( bse.getPercent() == 100 ) {
					xinqi = msg;
				}
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
		BseFinishAchievement ach = (BseFinishAchievement)xinqi.payload;
		assertEquals(100, ach.getPercent());
	}
	
	@Test
	public void testWithDay7Uncontinuous() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);

		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 goledn
		task.setId("10000");
		task.setStep(7);
		task.setScript(ScriptHook.TASK_LOGIN_DATE.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("连续登陆");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//do mark today
		Calendar current = Calendar.getInstance();
		//7 days
		for ( int i=0; i<7; i++ ) {
			current.set(2012, 1, 15+i, 11, 38, 0);
			long currentTime = current.getTimeInMillis();
			if ( i == 5 ) {
				list.clear();
				continue;
			}
			RewardManager.getInstance().takeDailyMarkReward(user, currentTime);
		}
		
		//assertEquals(1, dailyMark.getTotalCount());

		Thread.sleep(200);
		System.out.println(list);
		
		XinqiMessage xinqi = list.get(list.size()-1);
		assertNotNull("BseFinishAchievement", xinqi);
		BseFinishAchievement ach = (BseFinishAchievement)xinqi.payload;
		assertEquals(14, ach.getPercent());
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
