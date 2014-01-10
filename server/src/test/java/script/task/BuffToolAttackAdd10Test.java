package script.task;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BuffToolAttackAdd10Test {
	
	String username = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() throws Exception {
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
		
		//94	进阶！熟练使用技能I	10	21	10	20	TASK_DAILY	使用伤害增益10%技能	script.task.BuffToolAttackAdd10
		TaskPojo taskPojo = manager.getTaskById("94");
		ArrayList<TaskPojo> taskList = new ArrayList<TaskPojo>();
		taskList.add(taskPojo);
		//106	'pow'制胜的秘诀	10	3	10	7	TASK_ACTIVITY	战斗中使用大招技能
		taskList.add(manager.getTaskById("106"));
		user.addTasks(taskList);
		
		//Call the TaskHook
		int step = taskPojo.getStep();
		for ( int i=0; i<step; i++ ) {
			TaskManager.getInstance().processUserTasks(user, 
				TaskHook.USE_TOOL, BuffToolType.HurtAdd10);
		}
		
		Thread.sleep(200);
		System.out.println(list);
		//Total 5 BseModiTask: step from 1 to 5
		assertEquals(10, list.size());
		XinqiMessage xinqi = (XinqiMessage)list.get(list.size()-1);
		assertTrue("BseModiTask", xinqi.payload instanceof BseModiTask);
		BseModiTask modiTask = (BseModiTask)xinqi.payload;
		assertEquals(10, modiTask.getStep());
	}

}
