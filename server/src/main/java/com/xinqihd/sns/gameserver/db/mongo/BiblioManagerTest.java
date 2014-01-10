package com.xinqihd.sns.gameserver.db.mongo;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserBiblio;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.session.SessionKey;

import static org.junit.Assert.*;

public class BiblioManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetPowerList() {
		HashMap<String, Integer> list = BiblioManager.getInstance().getPowerList();
		for( Map.Entry<String,Integer> entry : list.entrySet() ) {
			System.out.println(toWeaponName(entry.getKey())+":"+entry.getValue());
		}
	}

	public String toWeaponName(String weaponType) {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(weaponType, 0);
		return weapon.getName();
	}
	
	/**
	 * Test save the biblio
	 */
	@Test
	public void testSaveBiblio() {
		String roleName = "test001";
		User user = new User();
		UserId userId = new UserId(roleName);
		user.set_id(userId);
		user.setUsername(roleName);
		user.setRoleName(roleName);
		UserManager manager = UserManager.getInstance();
		manager.removeUser(roleName);
		manager.saveUser(user, true);
		BiblioManager.getInstance().removeUserBiblio(userId);
		
		BiblioManager.getInstance().scanUserBag(user);
		UserBiblio biblio = user.getBiblio();
		int count = 1;
		int startIndex = 0;
		for ( int i=0; i<count; i++ ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(String.valueOf(startIndex));
			while ( weapon == null ) {
				weapon = EquipManager.getInstance().getRandomWeapon(0, EquipType.WEAPON, 1);
			}
			PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
			BiblioManager.getInstance().addBiblio(user, propData);
			startIndex+=10;
		}
		BiblioManager.getInstance().saveUserBiblio(biblio);
		
		UserBiblio actualBibilo = BiblioManager.getInstance().queryUserBiblio(userId);
		assertEquals(biblio, actualBibilo);
		assertEquals("0", actualBibilo.getWeaponId("0"));
		
		//update the weapon
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById("1");
		PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
		BiblioManager.getInstance().addBiblio(user, propData);
		//BiblioManager.getInstance().saveUserBiblio(biblio);
		actualBibilo = BiblioManager.getInstance().queryUserBiblio(userId);
		assertEquals("1", actualBibilo.getWeaponId("0"));
	}

	/**
	 * Test save the biblio
	 */
	@Test
	public void testUpdateBiblio() {
		String roleName = "test001";
		User user = new User();
		UserId userId = new UserId(roleName);
		user.set_id(userId);
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager manager = UserManager.getInstance();
		manager.removeUser(roleName);
		manager.saveUser(user, true);
		BiblioManager.getInstance().removeUserBiblio(userId);
		
		BiblioManager.getInstance().scanUserBag(user);
		UserBiblio biblio = user.getBiblio();
		int count = 10;
		int startIndex = 0;
		for ( int i=0; i<count; i++ ) {
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(String.valueOf(startIndex));
			while ( weapon == null ) {
				weapon = EquipManager.getInstance().getRandomWeapon(0, EquipType.WEAPON, 1);
			}
			PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
			BiblioManager.getInstance().addBiblio(user, propData);
			startIndex+=10;
		}
		
		UserBiblio actualBibilo = BiblioManager.getInstance().queryUserBiblio(userId);
		assertEquals(biblio, actualBibilo);
		assertEquals("0", actualBibilo.getWeaponId("0"));
		
		//update the weapon
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById("200");
		PropData propData = weapon.toPropData(30, WeaponColor.WHITE);
		user.getBag().addOtherPropDatas(propData);
		
		//BiblioManager.getInstance().saveUserBiblio(biblio);
		
		//Try another scanbag action
		BiblioManager.getInstance().scanUserBag(user);
		actualBibilo = BiblioManager.getInstance().queryUserBiblio(userId);
		assertEquals(11, actualBibilo.getBiblio().size());
	}
}
