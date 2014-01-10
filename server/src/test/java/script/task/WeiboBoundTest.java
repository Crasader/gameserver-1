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
import com.xinqihd.sns.gameserver.config.WeiboOpType;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.WeiboManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceWeibo.BceWeibo;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

/**
 * Test the WeiboBound task
 * @author wangqi
 *
 */
public class WeiboBoundTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWeiboBound() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_BOUND);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.BOUND.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboAnyType() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_ANYTYPE);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.ACHIEVEMENT.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboAchievement() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_ACHIEVEMENT);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.ACHIEVEMENT.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboCombat() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_COMBAT);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.COMBAT.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboForge() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_FORGE);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.FORGE.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboLevelup() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_LEVELUP);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.LEVELUP.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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
	
	@Test
	public void testWeiboRanking() throws Exception {
		String username = "test-001";
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		User user = prepareUser(username, list, ScriptHook.TASK_WEIBO_RANKING);
				
		//This call will finish the task
		BceWeibo.Builder builder = BceWeibo.newBuilder();
		builder.setAccount(username);
		builder.setOptype(WeiboOpType.RANKING.ordinal());
		builder.setWeibo("sina");
		WeiboManager.getInstance().processWeibo(user, builder.build());
		
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

	private User prepareUser(String username, ArrayList<XinqiMessage> list, ScriptHook hook) {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		user.setRoleName(username);
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
		task.setStep(1);
		task.setScript(hook.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("微博");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		return user;
	}
	
}
