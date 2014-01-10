package script;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;

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
import com.xinqihd.sns.gameserver.forge.ComposeStatus;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;
/**
		<item id="21001" typeid="21001" lv="0" icon="Prop0012" name="水神石炼化符" info="炼化水神石的必需品。" />
		<item id="21002" typeid="21001" lv="0" icon="Prop0013" name="土神石炼化符" info="炼化土神石的必需品。" />
		<item id="21003" typeid="21001" lv="0" icon="Prop0010" name="风神石炼化符" info="炼化风神石的必需品。" />
		<item id="21004" typeid="21001" lv="0" icon="Prop0011" name="火神石炼化符" info="炼化火神石的必需品。" />
		
 * @author wangqi
 *
 */
public class CraftComposeItemTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComposeStone() {
		int i=0;
		User user = UserManager.getInstance().createDefaultUser();
		ItemPojo itemPojo = ItemManager.getInstance().getItemById("21001");
		PropData luckFuncStone = itemPojo.toPropData();
		luckFuncStone.setPew(24);
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			PropData propData = makeStone(20, 1);
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, makeStone(21, 1), makeStone(22, 1), makeStone(23, 1), 
							luckFuncStone});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.FAILURE ) {
				assertEquals(null, newPropData);
//				assertEquals(propData.getAttackLev(), newPropData.getAttackLev());
//				assertEquals(propData.getDefendLev(), newPropData.getDefendLev());
//				assertEquals(propData.getAgilityLev(), newPropData.getAgilityLev());
//				assertEquals(propData.getLuckLev(), newPropData.getLuckLev());
//				assertEquals(propData.getLevel(), newPropData.getLevel());
			} else if ( status == ComposeStatus.SUCCESS ) {
				//	<item id="20002" typeid="20001" lv="2" icon="GreenStoneLv2" name="水神石Lv2"
				assertEquals("20002", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(2,  newPropData.getLevel());
				assertEquals("水神石Lv2",  newPropData.getName());
				itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(2,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				System.out.println("try: " + i);
				System.out.println(newPropData);
				System.out.println(newPropData.getPojo());
				break;
			} else if ( status == ComposeStatus.UNCOMPOSABLE ) {
				assertTrue( status != ComposeStatus.UNCOMPOSABLE );
				break;
			}
		}
	}
	
	@Test
	public void testComposeStoneUncomposable() {
		int i=0;
		User user = UserManager.getInstance().createDefaultUser();
		
		PropData propData = makeStone(20, 5);
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
						propData, makeStone(21, 5), makeStone(22, 5), makeStone(23, 5)});
		
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		ComposeStatus status = (ComposeStatus)result.getResult().get(0);
		PropData newPropData = (PropData)result.getResult().get(1);
		
		assertEquals(ComposeStatus.UNCOMPOSABLE, status);
		assertEquals(null, newPropData);
	}
	
	public void testComposeRing() {
		int i=0;
		User user = UserManager.getInstance().createDefaultUser();
		
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			PropData propData = makeRing(20, 1);
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, makeRing(21, 1), makeRing(22, 1), makeRing(23, 1)});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.FAILURE ) {
				assertEquals(null, newPropData);
//				assertEquals(propData.getAttackLev(), newPropData.getAttackLev());
//				assertEquals(propData.getDefendLev(), newPropData.getDefendLev());
//				assertEquals(propData.getAgilityLev(), newPropData.getAgilityLev());
//				assertEquals(propData.getLuckLev(), newPropData.getLuckLev());
//				assertEquals(propData.getLevel(), newPropData.getLevel());
			} else if ( status == ComposeStatus.SUCCESS ) {
				assertEquals("830", newPropData.getItemId());
				/*
				assertEquals(14, newPropData.getAttackLev());
				assertEquals(14, newPropData.getDefendLev());
				assertEquals(14, newPropData.getLuckLev());
				assertEquals(14, newPropData.getAgilityLev());
				*/
				assertEquals(1,  newPropData.getLevel());
				assertEquals("黑铁●阿波罗神戒",  newPropData.getName());
				WeaponPojo weaponPojo = (WeaponPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), weaponPojo.getName());
				assertEquals(newPropData.getItemId(), weaponPojo.getId());
				System.out.println("try: " + i);
				System.out.println(newPropData);
				System.out.println(newPropData.getPojo());
				break;
			}
		}
	}
	
	@Test
	public void testComposeRingUncomposable() {
		int i=0;
		User user = UserManager.getInstance().createDefaultUser();
		
		PropData propData = makeWeapon(20, 5);
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
						propData, makeWeapon(21, 5), makeWeapon(22, 5), makeWeapon(23, 5)});
		
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		ComposeStatus status = (ComposeStatus)result.getResult().get(0);
		PropData newPropData = (PropData)result.getResult().get(1);
		
		assertEquals(ComposeStatus.UNCOMPOSABLE, status);
		assertEquals(null, newPropData);
	}
	
	@Test
	public void testComposeNotSameItem() {
		int i=0;
		User user = UserManager.getInstance().createDefaultUser();
		
		PropData propData = makeWeapon(20, 5);
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
						propData, makeStone(21, 5), makeStone(22, 5), makeStone(23, 5)});
		
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		ComposeStatus status = (ComposeStatus)result.getResult().get(0);
		PropData newPropData = (PropData)result.getResult().get(1);
		
		assertEquals(ComposeStatus.UNCOMPOSABLE, status);
		assertEquals(null, newPropData);
	}

	@Test
	public void testTaskWithComposeFireLv2() throws Exception {
		int i=0;
		String username = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(username));
		user.setUsername(username);
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//149	登陆有奖相送
		TaskPojo task1 = manager.getTaskById("149");
		//115	合成2级火神石到装备上
		TaskPojo task2 = manager.getTaskById("115");
		tasks.add(task1);
		tasks.add(task2);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		//20016 火神石Lv1
		PropData propData = makeStone(20, "20004", 1);
		while (true ) {
			list.clear();
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, 
							makeStone(21, "20004", 1), 
							makeStone(22, "20004", 1), 
							makeStone(23, "20004", 1),
							//火神石炼化符
							makeFuncStone(24, "21004")
							});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.SUCCESS ) {
				//<item id="20017" typeid="20004" lv="2" icon="RedStoneLv2" name="火神石Lv2"
				assertEquals("20017", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(2,  newPropData.getLevel());
				assertEquals("火神石Lv2",  newPropData.getName());
				ItemPojo itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(2,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				
				System.out.println(list);
				Thread.sleep(200);
				//115任务是将火神石合成都装备上，所以这里不会解锁
				/*
				assertEquals(1, list.size());
				XinqiMessage msg = list.get(0);
				assertTrue(msg.payload instanceof BseModiTask);
				*/
				break;
			} else if ( status == ComposeStatus.UNCOMPOSABLE ) {
				assertTrue( status != ComposeStatus.UNCOMPOSABLE );
				break;
			}
		}
	}
	
	@Test
	public void testTaskWithComposeFireLv3() throws Exception {
		int i=0;
		String username = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//149	登陆有奖相送
		TaskPojo task1 = manager.getTaskById("149");
		//115	合成1个3级火神石
		TaskPojo task2 = manager.getTaskById("115");
		task2.setStep(1);
		task2.setCondition1(3);
		tasks.add(task1);
		tasks.add(task2);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		PropData propData = makeStone(20, "20004", 1);
		//Test if level 2 stone trigger the task
		while (true ) {
			list.clear();
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, 
							makeStone(21, "20004", 1), 
							makeStone(22, "20004", 1), 
							makeStone(23, "20004", 1),
							//火神石炼化符
							makeFuncStone(24, "21004")
							});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.SUCCESS ) {
				assertEquals("20017", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(2,  newPropData.getLevel());
				assertEquals("火神石Lv2",  newPropData.getName());
				ItemPojo itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(2,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				
				System.out.println(list);
				assertEquals(0, list.size());
				break;
			} else if ( status == ComposeStatus.UNCOMPOSABLE ) {
				assertTrue( status != ComposeStatus.UNCOMPOSABLE );
				break;
			}
		}
		propData = makeStone(20, "20004", 2);
		//Test level 3 stone
		while (true ) {
			list.clear();
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, 
							makeStone(21, "20004", 2), 
							makeStone(22, "20004", 2), 
							makeStone(23, "20004", 2),
							//火神石炼化符
							makeFuncStone(24, "21004")
							});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.SUCCESS ) {
				assertEquals("20018", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(3,  newPropData.getLevel());
				assertEquals("火神石Lv3",  newPropData.getName());
				ItemPojo itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(3,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				
				Thread.currentThread().sleep(200);
				System.out.println(list);
				/*
				assertEquals(1, list.size());
				XinqiMessage msg = list.get(0);
				assertTrue(msg.payload instanceof BseModiTask);
				*/
				break;
			}
		}
	}
	
	@Test
	public void testTaskWithCompose2FireLv3() throws InterruptedException {
		int i=0;
		String username = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(username);
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//149	登陆有奖相送
		TaskPojo task1 = manager.getTaskById("149");
		//115	合成1个3级火神石
		TaskPojo task2 = manager.getTaskById("115");
		task2.setStep(2);
		task2.setCondition1(3);
		tasks.add(task1);
		tasks.add(task2);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		PropData propData = makeStone(20, "20004", 2);
		//Test if level 2 stone trigger the task
		while (true ) {
			list.clear();
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, 
							makeStone(21, "20004", 2), 
							makeStone(22, "20004", 2), 
							makeStone(23, "20004", 2),
							//火神石炼化符
							makeFuncStone(24, "21004")
							});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.SUCCESS ) {
				assertEquals("20018", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(3,  newPropData.getLevel());
				assertEquals("火神石Lv3",  newPropData.getName());
				ItemPojo itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(3,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				
				Thread.sleep(200);
				System.out.println(list);
				//BseModiTask
				/*
					<task id="115" name="合成2级火神石" taskTarget="在装备上合成一个2级火神石" step="1" level="2" seq="23" userLevel="10" desc="合成石可以增加装备自身的属性，尝试着在装备上面合成1个2级火神石。" parent="1" exp="4600" gold="500" ticket="20" gongxun="0" caifu="0" script="script.task.CraftComposeFire">
						<award type="item" id="20001" lv="2" sex="3" count="3" indate="2400000" />
					</task>
				 */
				Thread.sleep(200);
				//assertEquals(1, list.size());
				break;
			} else if ( status == ComposeStatus.UNCOMPOSABLE ) {
				assertTrue( status != ComposeStatus.UNCOMPOSABLE );
				break;
			}
		}
		//Test level 3 stone
		while (true ) {
			list.clear();
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.CRAFT_COMPOSE_ITEM, user, new Object[]{
							propData, 
							makeStone(20, "20004", 2), 
							makeStone(20, "20004", 2), 
							makeStone(20, "20004", 2),
						  //火神石炼化符
							makeFuncStone(24, "21004")
							});
			
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			
			ComposeStatus status = (ComposeStatus)result.getResult().get(0);
			PropData newPropData = (PropData)result.getResult().get(1);
			
			if ( status == ComposeStatus.SUCCESS ) {
				assertEquals("20018", newPropData.getItemId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(3,  newPropData.getLevel());
				assertEquals("火神石Lv3",  newPropData.getName());
				ItemPojo itemPojo = (ItemPojo)newPropData.getPojo();
				assertEquals(newPropData.getName(), itemPojo.getName());
				assertEquals(3,  itemPojo.getLevel());
				assertEquals(newPropData.getItemId(), itemPojo.getId());
				
				Thread.currentThread().sleep(200);
				/*
				System.out.println(list);
				assertEquals(1, list.size());
				XinqiMessage msg = list.get(0);
				assertTrue(msg.payload instanceof BseModiTask);
				*/
				break;
			} else if ( status == ComposeStatus.UNCOMPOSABLE ) {
				assertTrue( status != ComposeStatus.UNCOMPOSABLE );
				break;
			}
		}
	}
	
	/**
	 * Make a fake PropData
	 * @param i
	 * @return
	 */
	private PropData makeStone(int pew, int level) {
		ItemPojo itemPojo = ItemManager.getInstance().getItemById((20000+level)+"");
		PropData propData = itemPojo.toPropData();
		return propData;
	}
	
	private PropData makeFuncStone(int pew, String itemId) {
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(itemId);
		PropData propData = itemPojo.toPropData();
		propData.setPew(pew);
		return propData;
	}
	
	private PropData makeStone(int pew, String type, int level) {
		String id = ItemPojo.toId(type, level);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(id);
		PropData propData = itemPojo.toPropData();
		return propData;
	}
	
	/**
	 * Make a fake PropData
	 * 
	 * <jewelry index="417" id="14006" quality="3" s_name="阿波罗神戒" equip_type="141" 
	 * 	add_attack="10" add_defend="10" add_agility="10" add_luck="10" add_blood="0" 
	 *  add_thew="0" add_damage="0" add_skin="0" blood_percent="0" sex="2" unused1="-1" 
	 *  unused2="0" unused3="0" indate1="168" indate2="720" indate3="100000" sign="6" 
	 *  lv="0" autoDirection="0" sAutoDirection="0" specialAction="0" radius="0" 
	 *  sRadius="0" expBlend="" expSe="0" power="0" autoDestory="0" bullet="" 
	 *  icon="AboluoShenjie" name="阿波罗神戒" info="太阳神阿波罗所佩戴的戒指，蕴含着无尽的神力！"
	 *   Bubble="" slot="jewelry" >
	 * 
	 * 2770	黑铁●阿波罗神戒
	 * 
	 * @param i
	 * @return
	 */
	private PropData makeRing(int pew, int level) {
		WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById("830");
		PropData propData = weaponPojo.toPropData(30, WeaponColor.WHITE);
		return propData;
	}
	
	private PropData makeWeapon(int pew, int level) {
		WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weaponPojo.toPropData(30, WeaponColor.WHITE);
		return propData;
	}
}
