package com.xinqihd.sns.gameserver.db;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class UserManagerTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		GameContext gameContext = GameContext.getTestInstance();
		gameContext.reloadContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateDefaultUser() {
		UserManager manager = UserManager.getInstance();
		User user = manager.createDefaultUser();
		List<PropData> weapons = user.getBag().getWearPropDatas();
		PropData weapon = weapons.get(PropDataEquipIndex.WEAPON.index());
		assertEquals(UserManager.basicWeaponItemId, weapon.getItemId());
		WeaponPojo weaponPojo = GameContext.getInstance().getEquipManager().getWeaponById(UserManager.basicWeaponItemId);
		assertEquals(weaponPojo.getName(),weapon.getName());
		assertEquals(Integer.MAX_VALUE,weapon.getPropIndate());
		assertEquals(1,weapon.getCount());
		assertEquals(0,weapon.getLevel());
		assertEquals(weaponPojo.getAddAttack(),weapon.getAttackLev());
		assertEquals(weaponPojo.getAddDefend(),weapon.getDefendLev());
		assertEquals(weaponPojo.getAddAgility(),weapon.getAgilityLev());
		assertEquals(weaponPojo.getAddLuck(),weapon.getLuckLev());
		assertEquals(-1,weapon.getSign());
		assertEquals(PropDataValueType.GAME,weapon.getValuetype());
		assertEquals(true,weapon.isBanded());
		assertEquals(1,weapon.getDuration());
		assertEquals(PropDataEquipIndex.WEAPON.index(),weapon.getPew());
		
		assertEquals(1, user.getBag().getCurrentCount());
	}
	
	/*
	@Test
	public void testSerializeUser() throws Exception {
		UserManager manager = UserManager.getInstance();
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("test001");
		
		byte[] bytes = UserManager.getInstance().serializeUser(user);
		User actualUser = UserManager.getInstance().deserializeUser(bytes);
		System.out.println(actualUser);
	}

	@Test
	public void testDeserializeUser() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("/user.bin");
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int ch = bis.read();
		while ( ch != -1 ) {
			baos.write(ch);
			ch = bis.read();
		}
		User user = UserManager.getInstance().deserializeUser(baos.toByteArray());
		System.out.println(user);
	}
	*/
}
