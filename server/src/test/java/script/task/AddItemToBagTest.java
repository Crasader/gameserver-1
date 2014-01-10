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
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class AddItemToBagTest {

	String username = "test-001";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddWeapon() throws Exception {
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
		task.setScript(ScriptHook.TASK_ADD_ITEM.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("小化学家");
		task.setCondition1(StringUtil.toInt(UserManager.basicWeaponItemId, 0));
		task.setStep(5);
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//This call will finish the task
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(1, WeaponColor.WHITE);
		assertEquals(1, propData.getCount());
		user.getBag().addOtherPropDatas(propData);
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
		BseFinishAchievement ach = (BseFinishAchievement)xinqi.payload;
		assertTrue("BseFinishAchievement percent="+ach.getPercent(), ach.getPercent()!=100);
		propData.setCount(5);
		user.getBag().addOtherPropDatas(propData);
		Thread.sleep(200);
		System.out.println(list);
		//Total 5 BseModiTask: step from 1 to 5
//		assertEquals(2, list.size());
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		ach = (BseFinishAchievement)xinqi.payload;
		assertEquals("BseFinishAchievement percent="+ach.getPercent(), 100, ach.getPercent());
	}

}
