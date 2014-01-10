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
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class SellGoodTest {

	String username = "test-001";
	//9544	2920	黑铁●海盗船长之帽
	private String equipId2 = "2920";
	private String goodId2 = "9546";
	private Integer goodId2Int = 9546;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWithOutCond1() throws Exception {
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
		task.setStep(1);
		task.setScript(ScriptHook.TASK_SELL_GOOD.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("二道贩子");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//Sell goods
		Bag bag = user.getBag();
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId2);
		PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
		bag.addOtherPropDatas(propData);
		Thread.sleep(200);
		list.clear();
		
		ShopManager shopManager = ShopManager.getInstance();
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		
		Thread.sleep(200);
		System.out.println(list);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
	}

	@Test
	public void testWithCond1() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);

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
		task.setStep(1);
		task.setScript(ScriptHook.TASK_SELL_GOOD.getHook());
		task.setCondition1(2920);
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("二道贩子");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//Sell goods
		Bag bag = user.getBag();
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId2);
		PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
		bag.addOtherPropDatas(propData);
		Thread.sleep(200);
		list.clear();
		
		ShopManager shopManager = ShopManager.getInstance();
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		
		Thread.sleep(200);
		System.out.println(list);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
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
