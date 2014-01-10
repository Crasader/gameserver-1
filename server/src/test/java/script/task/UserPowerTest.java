package script.task;

import static org.junit.Assert.*;

import java.util.ArrayList;
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
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class UserPowerTest {

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
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 yuanbao
		task.setId("10000");
		task.setStep(300000);
		task.setScript(ScriptHook.TASK_USER_POWER.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("超级赛亚人");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//This call will finish the task
		user.setPower(300001);
		Thread.sleep(200);
		System.out.println(list);
		//Total 5 BseModiTask: step from 1 to 5
//		assertEquals(2, list.size());
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
	}

}
