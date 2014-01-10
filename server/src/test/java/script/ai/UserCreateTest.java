package script.ai;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class UserCreateTest {
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateUser() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setExp(1000);
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(28, WeaponColor.WHITE); 
		PropData newPropData = (PropData)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.WEAPON_LEVEL_UPGRADE, propData, 2);
		user.getBag().addOtherPropDatas(newPropData);
		user.getBag().wearPropData(newPropData.getPew(), PropDataEquipIndex.WEAPON.index());
		
		System.out.println(user);
		
		User aiUser = (User)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.AI_USER_CREATE, user);
		
		System.out.println(aiUser);
		
		assertNotNull(aiUser.get_id());
		assertEquals(true, aiUser.isAI());
		assertEquals(false, user.isAI());
		assertEquals(user.getLevel(), aiUser.getLevel());
//		assertEquals(user.getExp(), aiUser.getExp());
//		assertEquals(user.getBlood(), aiUser.getBlood());
		assertTrue(user.getPower()*1.0/aiUser.getPower()<=2);
		PropData expectPropData = user.getBag().getWearPropDatas().get(17);
		PropData actualPropData = aiUser.getBag().getWearPropDatas().get(17);
		assertEquals(expectPropData.getLevel(), actualPropData.getLevel());
		
		System.out.println("my attack:"+user.getAttack() + ", ai attack:" + aiUser.getAttack());
		System.out.println("my defend:"+user.getDefend() + ", ai defend:" + aiUser.getDefend());
		System.out.println("my agility:"+user.getAgility() + ", ai agility:" + aiUser.getAgility());
		System.out.println("my luck:"+user.getLuck() + ", ai luck:" + aiUser.getLuck());
		System.out.println("my damage:"+user.getDamage() + ", ai damage:" + aiUser.getDamage());
		System.out.println("my skin:"+user.getSkin() + ", ai skin:" + aiUser.getSkin());
		System.out.println("my power:"+user.getPower() + ", ai power:" + aiUser.getPower());
	}

	@Test
	public void testCreateUserHighLevel() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setExp(20000);
		System.out.println("level: " + user.getLevel());
		assertTrue("user.getLevel()>20", user.getLevel()>20);
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(
				Integer.parseInt(UserManager.basicWeaponItemId)/100+"", user.getLevel());
		PropData propData = weapon.toPropData(28, WeaponColor.WHITE);
		PropData newPropData = (PropData)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.WEAPON_LEVEL_UPGRADE, propData, 2);
		user.getBag().addOtherPropDatas(newPropData);
		user.getBag().wearPropData(newPropData.getPew(), PropDataEquipIndex.WEAPON.index());
		
		System.out.println(user);
		
		User aiUser = (User)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.AI_USER_CREATE, user);
		
		System.out.println(aiUser);
		
		assertNotNull(aiUser.get_id());
		assertEquals(true, aiUser.isAI());
		assertEquals(false, user.isAI());
//		assertEquals(user.getExp(), aiUser.getExp());
//		assertEquals(user.getBlood(), aiUser.getBlood());
		assertTrue(user.getPower()*1.0/aiUser.getPower()<=2);
		PropData expectPropData = user.getBag().getWearPropDatas().get(17);
		PropData actualPropData = aiUser.getBag().getWearPropDatas().get(17);
		assertEquals(expectPropData.getLevel(), actualPropData.getLevel());
		assertEquals(expectPropData.getName().substring(0, 2), actualPropData.getName().substring(0, 2));
		
		System.out.println("my attack:"+user.getAttack() + ", ai attack:" + aiUser.getAttack());
		System.out.println("my defend:"+user.getDefend() + ", ai defend:" + aiUser.getDefend());
		System.out.println("my agility:"+user.getAgility() + ", ai agility:" + aiUser.getAgility());
		System.out.println("my luck:"+user.getLuck() + ", ai luck:" + aiUser.getLuck());
		System.out.println("my damage:"+user.getDamage() + ", ai damage:" + aiUser.getDamage());
		System.out.println("my skin:"+user.getSkin() + ", ai skin:" + aiUser.getSkin());
		System.out.println("my power:"+user.getPower() + ", ai power:" + aiUser.getPower());
	}

}
