package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;

public class GameResourceManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetGameResources() {
		HashMap<String, HashMap<Locale, String>> maps = GameResourceManager.getInstance().getGameResources();
		for ( HashMap<Locale, String> localeMap : maps.values() ) {
			System.out.println(localeMap);
		}
		assertTrue(maps.size()>0);
	}
	
	@Test
	public void testGetGameResourcesAllLocales() {
		Set<Locale> locales = GameResourceManager.getInstance().getAllLocales();
		for ( Locale locale : locales ) {
			System.out.println(locale);
		}
		assertTrue(locales.size()>0);
	}

	@Test
	public void testGetGameResource() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		String res = GameResourceManager.getInstance().getGameResource("equipments_new_name_"+weapon.getId(), 
				Locale.TRADITIONAL_CHINESE, "haha");
		assertEquals("黑鐵●榴弹炮", res);
	}

	@Test
	public void testGetGameResourceNoLocale() {
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		String res = GameResourceManager.getInstance().getGameResource("equipments_new_name_"+weapon.getId(), 
				Locale.CANADA_FRENCH, "haha");
		assertEquals("haha", res);
	}
	
}
