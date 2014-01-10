package script.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.Field;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class AnyCombatTest {

	String username = "test-001";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		//taskid	name	userlevel	sequence	step	level	type	action	script
		//15	激战到底I		10	1	2	7	TASK_DAILY	完成任意战斗	script.task.AnyCombat
		int step = 5;
		TaskPojo task = manager.getTaskById("15");
		task.setStep(step);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		user.putUserData(RoomManager.ROOM_TYPE_KEY, RoomType.SINGLE_ROOM);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		for (int i=0; i<step-1; i++ ) {
			manager.processUserTasks(user, TaskHook.COMBAT, 
					Boolean.TRUE, 2, RoomType.SINGLE_ROOM);
			String currentStep = manager.queryTaskSpecificData(user, task.getId(), Field.STEP);
			assertEquals(""+(i+1), currentStep);
		}
		//This call will finish the task
		manager.processUserTasks(user, TaskHook.COMBAT,
				Boolean.TRUE, 2, RoomType.SINGLE_ROOM);
		Thread.sleep(200);
		System.out.println(list);
		//Total 5 BseModiTask: step from 1 to 5
		assertEquals(5, list.size());
		XinqiMessage xinqi = (XinqiMessage)list.get(0);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
	}

}
