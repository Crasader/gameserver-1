package script;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.forge.ForgeStatus;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class CraftForgeEquipTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStrengthEquip() {
		//榴弹炮
		User user = UserManager.getInstance().createDefaultUser();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		expect.setLevel(5);
		int attack = expect.getAttackLev();
		
		ItemPojo[] items = new ItemPojo[]{
			//20005 强化石Lv5
			//	<item id="20025" typeid="20005" lv="5" icon="StrengthStoneLv5" name="强化石Lv5" 
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			//	24002 幸运符+15%
			//  24004 幸运符+25%
			ItemManager.getInstance().getItemById("24004"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" attack="+attack);
		System.out.println(equipData+" attack="+equipData.getAttackLev());
	}
	
	@Test
	public void testStrengthEquipMaxLevel() {
		//榴弹炮
		User user = UserManager.getInstance().createDefaultUser();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		expect.setLevel(12);
		int attack = expect.getAttackLev();
		
		ItemPojo[] items = new ItemPojo[]{
			//20005 强化石Lv5
			//	<item id="20025" typeid="20005" lv="5" icon="StrengthStoneLv5" name="强化石Lv5" 
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			ItemManager.getInstance().getItemById("20025"),
			//	24002 幸运符+15%
			//  24004 幸运符+25%
			ItemManager.getInstance().getItemById("24004"),
			//  24005 必成符
			ItemManager.getInstance().getItemById("24005"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		PropData equipData = (PropData)list.get(1);
		assertEquals(ForgeStatus.UNFORGABLE, status);
		System.out.println(expect+" attack="+attack);
		System.out.println(equipData+" attack="+equipData.getAttackLev());
	}

	@Test
	public void testStrengthEquipFailure() {
		User user = UserManager.getInstance().createDefaultUser();
		
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		expect.setAttackLev(200);
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.WEAPON_LEVEL_UPGRADE, expect, 5);
		int attack = expect.getAttackLev();
		int level = expect.getLevel();
		
		ItemPojo[] items = new ItemPojo[]{
			//20021 20005 强化石Lv1
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
		}
		result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.FAILURE, status);
		PropData equipData = (PropData)list.get(1);
		assertEquals(12, equipData.getLevel());
	}
	
	@Test
	public void testStrengthEquipFailureNoDownGrade() {
		User user = UserManager.getInstance().createDefaultUser();
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		expect.setLevel(4);
		int attack = expect.getAttackLev();
		
		ItemPojo[] items = new ItemPojo[]{
			//20005 强化石Lv1
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.FAILURE, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" attack="+attack);
		System.out.println(equipData+" attack="+equipData.getAttackLev());
		assertEquals(attack, equipData.getAttackLev());
	}
	
	@Test
	public void testStrengthEquipFailureNoDownGradeForVIP() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setIsvip(true);
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		expect.setLevel(8);
		int attack = expect.getAttackLev();
		
		ItemPojo[] items = new ItemPojo[]{
			//20005 强化石Lv1
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
			ItemManager.getInstance().getItemById("20021"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.FAILURE, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" attack="+attack);
		System.out.println(equipData+" attack="+equipData.getAttackLev());
		assertEquals(8, equipData.getLevel());
	}
	
	@Test
	public void testForgeEquipLuck() {
		User user = UserManager.getInstance().createDefaultUser();
		
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int oluck = expect.getLuckLev();
		
		//20001 水神石Lv1 与装备合成后提高幸运属性
		ItemPojo[] items = new ItemPojo[]{
			ItemManager.getInstance().getItemById("20001"),	
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(5);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" luck="+oluck);
		System.out.println(equipData+" luck="+equipData.getLuckLev());
		assertTrue(equipData.getLuckLev()>oluck);
	}
	
	@Test
	public void testForgeEquipDefend() {
		User user = UserManager.getInstance().createDefaultUser();
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int defend = expect.getDefendLev();
		
		//20002 土神石
		ItemPojo[] items = new ItemPojo[]{
			ItemManager.getInstance().getItemById("20006"),	
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(5);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" defend="+defend);
		System.out.println(equipData+" defend="+equipData.getDefendLev());
		assertTrue(equipData.getDefendLev()>defend);
	}
	
	@Test
	public void testForgeEquipAgility() {
		User user = UserManager.getInstance().createDefaultUser();
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int agility = expect.getAgilityLev();
		
		//20003 风神石
		ItemPojo[] items = new ItemPojo[]{
			ItemManager.getInstance().getItemById("20011"),	
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(5);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" agility="+agility);
		System.out.println(equipData+" agility="+equipData.getAgilityLev());
		assertTrue(equipData.getAgilityLev()>agility);
	}
	
	@Test
	public void testForgeEquipAttack() {
		User user = UserManager.getInstance().createDefaultUser();
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int attack = expect.getAttackLev();
		
		//20004 火神石
		ItemPojo[] items = new ItemPojo[]{
			ItemManager.getInstance().getItemById("20016"),	
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(5);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user, 
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" attack="+attack);
		System.out.println(equipData+" attack="+equipData.getAttackLev());
		System.out.println(equipData+" power="+equipData.getPower());
		assertTrue(equipData.getAttackLev()>attack);
		assertTrue(equipData.getPower()>expect.getPower());
	}
	
	@Test
	public void testForgeEquipLuckWithLuckyStone() {
		User user = UserManager.getInstance().createDefaultUser();
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int oluck = expect.getLuckLev();
		
		ItemPojo[] items = new ItemPojo[]{
			//20001 水神石Lv1 与装备合成后提高幸运属性
			ItemManager.getInstance().getItemById("20001"),
			//	24002 幸运符+15%
			//  24004 幸运符+25%
			ItemManager.getInstance().getItemById("24004"),
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(5);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		List list = result.getResult();
		ForgeStatus status = (ForgeStatus)list.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)list.get(1);
		System.out.println(expect+" luck="+oluck);
		System.out.println(equipData+" luck="+equipData.getLuckLev());
		assertTrue(equipData.getLuckLev()>oluck);
	}
	
	/**
	 * 9	装备合成	8	12	1	0	TASK_MAIN	成功将1级水神石合成到装备上
	 */
	@Test
	public void testForgeEquipLuckWithTask() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//9	成功将1级水神石合成到装备上
		TaskPojo task1 = manager.getTaskById("9");
		//115	合成2级火神石
		TaskPojo task2 = manager.getTaskById("115");
		tasks.add(task1);
		tasks.add(task2);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.set_id(new UserId(user.getUsername()));
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		//榴弹炮
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
		int oluck = expect.getLuckLev();
		
		//20001 水神石Lv1 与装备合成后提高幸运属性
		ItemPojo[] items = new ItemPojo[]{
			ItemManager.getInstance().getItemById("20001"),	
		};
		PropData[] stones = new PropData[items.length];
		for ( int i=0; i<stones.length; i++ ) {
			stones[i] = items[i].toPropData();
			stones[i].setLevel(1);
		}
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_FORGE_EQUIP, user,
				new Object[]{expect, stones});
		
		List resultList = result.getResult();
		ForgeStatus status = (ForgeStatus)resultList.get(0);
		assertEquals(ForgeStatus.SUCCESS, status);
		PropData equipData = (PropData)resultList.get(1);
		System.out.println(expect+" luck="+oluck);
		System.out.println(equipData+" luck="+equipData.getLuckLev());
		assertTrue(equipData.getLuckLev()>oluck);
		
		Thread.currentThread().sleep(500);
		System.out.println(list);
		assertEquals(1, list.size());
		XinqiMessage message = list.get(0);
		assertTrue(message.payload instanceof BseModiTask);
	}
	
	/**
	 * 23	强化衣服到1级	10	1	1	1	TASK_SUB	script.task.StrengthClothes	强化衣服达到LV1
	 */
	@Test
	public void testStrengthClothes() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername("test-001");
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//2	强化衣服到1级
		TaskPojo task1 = manager.getTaskById("2");
		//115	合成2级火神石
		TaskPojo task2 = manager.getTaskById("115");
		tasks.add(task1);
		tasks.add(task2);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.set_id(new UserId(user.getUsername()));
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		//test 榴弹炮 first
		{
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
			PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
			expect.setLevel(0);
			int attack = expect.getAttackLev();
			
			ItemPojo[] items = new ItemPojo[]{
				//20005 强化石Lv5
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				//	24002 幸运符+15%
				//  24004 幸运符+25%
				ItemManager.getInstance().getItemById("24004"),
			};
			PropData[] stones = new PropData[items.length];
			for ( int i=0; i<stones.length; i++ ) {
				stones[i] = items[i].toPropData();
			}
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_FORGE_EQUIP, user,
					new Object[]{expect, stones});
			List resultList = result.getResult();
			ForgeStatus status = (ForgeStatus)resultList.get(0);
			PropData equipData = (PropData)resultList.get(1);
			System.out.println(expect+" attack="+attack);
			System.out.println(equipData+" attack="+equipData.getAttackLev());
			
			System.out.println(list);
			assertEquals(1, list.size());
		}
		//2030	黑铁●简约时尚
		//test clothes id="6001" s_name="简约时尚"
		{
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById("2030");
			PropData expect = weapon.toPropData(1, WeaponColor.WHITE);
			expect.setLevel(0);
			int attack = expect.getAttackLev();
			
			ItemPojo[] items = new ItemPojo[]{
				//20005 强化石Lv5
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				ItemManager.getInstance().getItemById("20025"),
				//	24002 幸运符+15%
				//  24004 幸运符+25%
				ItemManager.getInstance().getItemById("24004"),
			};
			PropData[] stones = new PropData[items.length];
			for ( int i=0; i<stones.length; i++ ) {
				stones[i] = items[i].toPropData();
			}
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_FORGE_EQUIP, user,
					new Object[]{expect, stones});
			List resultList = result.getResult();
			ForgeStatus status = (ForgeStatus)resultList.get(0);
			PropData equipData = (PropData)resultList.get(1);
			System.out.println(expect+" attack="+attack);
			System.out.println(equipData+" attack="+equipData.getAttackLev());
			
			Thread.currentThread().sleep(500);
			System.out.println(list);
			assertEquals(1, list.size());
			XinqiMessage message = list.get(0);
			assertTrue(message.payload instanceof BseModiTask);
		}
	}
}
