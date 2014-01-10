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
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.Field;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.forge.CraftManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseFinishAchievement.BseFinishAchievement;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class StrengthWeaponTest {

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
		user.setRoleName(username);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setGolden(99999);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		UserManager.getInstance().removeUser(user.getUsername());
		UserManager.getInstance().saveUser(user, true);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 yuanbao
		task.setId("10000");
		task.setCondition1(5);
		task.setStep(1);
		task.setScript(ScriptHook.TASK_STRENGTH_WEAPON.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("强化5级");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//This call to strength to level 1
		user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
		CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
		Thread.sleep(200);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNull("BseFinishAchievement", xinqi);
		
		//try to strength to level 5
		for ( int i=0; i<1000; i++ ) {
			list.clear();
			user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
			CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
			Thread.sleep(200);
			if ( user.getBag().getWearPropDatas().get(17).getLevel()>=5 ) {
				break;
			}
		}
		
		xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
		BseFinishAchievement achi = (BseFinishAchievement)xinqi.payload;
		assertEquals(100, achi.getPercent());

	}
	
	@Test
	public void testCount() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		user.setRoleName(username);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setGolden(99999);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		UserManager.getInstance().removeUser(user.getUsername());
		UserManager.getInstance().saveUser(user, true);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 yuanbao
		task.setId("10000");
		task.setCondition1(5);
		task.setStep(8);
		task.setScript(ScriptHook.TASK_STRENGTH_WEAPON.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("强化5级");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//This call to strength to level 1
		user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
		CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
		Thread.sleep(200);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNull("BseFinishAchievement", xinqi);
		
		//try to strength to level 5
		for ( int i=0; i<1000; i++ ) {
			list.clear();
			user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
			CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
			Thread.sleep(200);
			if ( user.getBag().getWearPropDatas().get(17).getLevel()>=5 ) {
				break;
			}
		}
		
		xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNotNull("BseFinishAchievement", xinqi);
		BseFinishAchievement achi = (BseFinishAchievement)xinqi.payload;
		assertEquals(13, achi.getPercent());
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = TaskManager.getTaskDataKey(user, Field.DATA);
		String value = jedisDB.hget(key, "10000");
		System.out.println(key+"="+value);
	}
	
	@Test
	public void testCountWithSameWeapon() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		user.setRoleName(username);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setGolden(99999);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		UserManager.getInstance().removeUser(user.getUsername());
		UserManager.getInstance().saveUser(user, true);
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		
		TaskPojo task = new TaskPojo();
		//2000 yuanbao
		task.setId("10000");
		//需要强化一个装备达到2级，总共3次
		//当一个装备强化到5级时，不应该触发这个成就
		task.setCondition1(2);
		task.setStep(3);
		task.setScript(ScriptHook.TASK_STRENGTH_WEAPON.getHook());
		task.setType(TaskType.TASK_ACHIVEMENT);
		task.setName("强化5级");
		Award award = new Award();
		award.id = "-1";
		award.type = Constant.ACHIEVEMENT;
		task.addAward(award);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(task);
		user.addTasks(tasks);
		TaskManager.getInstance().setTaskById(task);
		
		//This call to strength to level 1
		user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
		CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
		Thread.sleep(200);
		
		XinqiMessage xinqi = null;
		for ( XinqiMessage msg : list ) {
			if (msg.payload instanceof BseFinishAchievement) {
				xinqi = msg;
			}
		}
		assertNull("BseFinishAchievement", xinqi);
		
		//try to strength to level 5
		for ( int i=0; i<1000; i++ ) {
			list.clear();
			user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
			CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
			Thread.sleep(200);
			if ( user.getBag().getWearPropDatas().get(17).getLevel()>=6 ) {
				break;
			}
		}
				
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = TaskManager.getTaskDataKey(user, Field.STEP);
		String value = jedisDB.hget(key, "10000");
		assertEquals("1", value);
		
		//再次强化两个装备
		//try to strength to level 5
		PropData wearedProp = user.getBag().getWearPropDatas().get(17);
		wearedProp.setLevel(1);
		user.getBag().setWearPropData(wearedProp, 17);
		for ( int i=0; i<1000; i++ ) {
			list.clear();
			user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
			CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
			Thread.sleep(200);
			if ( user.getBag().getWearPropDatas().get(17).getLevel()>=3 ) {
				break;
			}
		}
		key = TaskManager.getTaskDataKey(user, Field.STEP);
		value = jedisDB.hget(key, "10000");
		assertEquals("2", value);
		
		wearedProp.setLevel(1);
		user.getBag().setWearPropData(wearedProp, 17);
		for ( int i=0; i<1000; i++ ) {
			list.clear();
			user.setBag(makeBagWithStrengthStone(user, 1, 5, 2));
			CraftManager.getInstance().forgeEquip(user, 17, new int[]{20, 21, 22, 23, 24});
			Thread.sleep(200);
			BseFinishAchievement achi = null;
			for ( XinqiMessage msg : list ) {
				if (msg.payload instanceof BseFinishAchievement) {
					achi = (BseFinishAchievement)msg.payload;
				}
			}
			if ( achi != null ) {
				assertEquals(100, achi.getPercent());
				break;
			}
		}
		key = TaskManager.getTaskDataKey(user, Field.STEP);
		value = jedisDB.hget(key, "10000");
		//The task data is removed.
		assertEquals(null, value);

	}

	/**
	 * 
	 * @return
	 */
	private Bag makeBagWithStrengthStone(User user, 
			int count, int stoneLevel, int luckyStone) {
		
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);

		//Strength stones - up to 3
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20005_"+level);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById((20020+stoneLevel)+"");
		bag.addOtherPropDatas(itemPojo.toPropData(count));
		bag.addOtherPropDatas(itemPojo.toPropData(count));
		bag.addOtherPropDatas(itemPojo.toPropData(count));

		//God stone 
		ItemPojo godPojo = ItemManager.getInstance().getItemById("24001");
		PropData propData = godPojo.toPropData();
		bag.addOtherPropDatas(propData);
		
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
