package script;

import static org.junit.Assert.*;

import java.util.List;

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
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class WeaponDepreciateTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		User user = prepareUser("test-001");
		//Add equipments 
		// id="12001" quality="2" s_name="榴弹炮"
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		//Replace the default weapon
		user.getBag().setOtherPropDataAtPew(weapon.toPropData(10, WeaponColor.WHITE), 20);
		//id="1003" quality="2" s_name="骨头耳坠"
		WeaponPojo decoration = EquipManager.getInstance().getWeaponById("1090");
		user.getBag().addOtherPropDatas(decoration.toPropData(10, WeaponColor.WHITE));
		//id="2001" quality="2" s_name="典雅帅气"
		WeaponPojo hair = EquipManager.getInstance().getWeaponById("1320");
		user.getBag().addOtherPropDatas(hair.toPropData(10, WeaponColor.WHITE));
		
		System.out.println(user.getBag().getOtherPropDatas());
		
		//Wear them
		user.getBag().wearPropData(20, PropDataEquipIndex.WEAPON.index());
		user.getBag().wearPropData(21, PropDataEquipIndex.BRACELET1.index());
		user.getBag().wearPropData(22, PropDataEquipIndex.HAIR.index());
		
		//Call the script
		ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.WEAPON_DEPRECIATE, user);
		assertEquals(ScriptResult.Type.SUCCESS, result.getType());

		//Check result
		User actual = UserManager.getInstance().queryUser("test-001");
		UserManager.getInstance().queryUserBag(actual);
		
		List<PropData> list = actual.getBag().getWearPropDatas();
		PropData prop1 = list.get(PropDataEquipIndex.WEAPON.index());
		assertEquals(weapon.getId(), prop1.getItemId());
		assertEquals(0, prop1.getPropUsedTime());
		PropData prop2 = list.get(PropDataEquipIndex.BRACELET1.index());
		assertEquals(decoration.getId(), prop2.getItemId());
		assertEquals(0, prop2.getPropUsedTime());
		PropData prop3 = list.get(PropDataEquipIndex.HAIR.index());
		assertEquals(hair.getId(), prop3.getItemId());
		assertEquals(0, prop3.getPropUsedTime());
		
	}

	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
