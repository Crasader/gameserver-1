package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager.UserLoginStatus;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.User.Location;
import com.xinqihd.sns.gameserver.entity.user.UserChangeFlag;
import com.xinqihd.sns.gameserver.entity.user.UserId;

public class MongoUserManagerTest extends AbstractTest {
	
	UserManager manager = null;

	@Before
	public void setUp() throws Exception {
//		MongoDBUtil.dropCollection("testdb", null, "users");
//		MongoDBUtil.dropCollection("testdb", null, "bags");
//		MongoDBUtil.dropCollection("testdb", null, "relations");
//		manager = new MongoUserManager("testdb", null, true);
		manager = MongoUserManager.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testClearFlag() throws Exception {
		User user = manager.createDefaultUser();
		user.setAbtest("abtest");
		UserChangeFlag[] flags = user.clearModifiedFlag();
		assertEquals(9, flags.length);
		
		flags = user.clearModifiedFlag();
		assertEquals(0, flags.length);
	}
	
	@Test
	public void testCheckUserName() {
		String userName = "test001";
		assertTrue(!manager.checkUserNameExist(userName));
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		assertTrue(manager.checkUserNameExist(userName));
		
	}

	@Test
	public void testSaveUser() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
	}
	
	@Test
	public void testSaveUser2() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		//Save it without modifing user. Check what happens
		assertTrue(!manager.saveUser(user, false));
		
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
	}
	
	@Test
	public void testSaveUserGuest() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		user.setGuest(true);
		user.setTutorial(true);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		//Save it without modifing user. Check what happens
		assertTrue(!manager.saveUser(user, false));
		
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
		assertEquals(true, actual.isGuest());
		assertEquals(true, actual.isTutorial());
		
		//Test update
		user.setGuest(false);
		user.setTutorial(false);
		manager.saveUser(user, false);
		actual = manager.queryUser("test001");
		assertEquals(false, actual.isGuest());
		assertEquals(false, actual.isTutorial());
	}
	
	@Test
	public void testSaveUserUuid() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		user.setUuid("uuid");
		user.setVerifiedEmail(true);
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		//Save it without modifing user. Check what happens
		assertTrue(!manager.saveUser(user, false));
		
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
		assertEquals(true, actual.isVerifiedEmail());
		assertEquals("uuid", actual.getUuid());
		
		//Test update
		user.setVerifiedEmail(false);
		user.setUuid("iduu");
		manager.saveUser(user, false);
		actual = manager.queryUser("test001");
		assertEquals(false, actual.isVerifiedEmail());
		assertEquals("iduu", actual.getUuid());
	}
	
	@Test
	public void testSaveUserForNewAddFields() throws Exception {
		manager.removeUser("test001");
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		user.setTotalKills(1000);
		user.setScreen("960x640");
		user.setVerifiedEmail(true);
		user.setDeviceToken("740f4707bebcf74f9b7c25d48e3358945f6aa01da5ddb387462c7eaf61bb78ad");
		user.setTutorialMark(1);
		user.setRoleTotalAction(1000);
		user.setChargedYuanbao(100);
		user.setChargeCount(1);
		user.setAccountName("test111");
		user.addWeiboToken("sina", "1234234213423423");
		user.setGuildId("guildid1");
		//user.addValueMap(PropDataEnhanceType.Field.ATTACK, 300);
		
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		//Save it without modifing user. Check what happens
		assertTrue(!manager.saveUser(user, false));
		
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
		assertEquals(1000, actual.getTotalKills());
		assertEquals("960x640", actual.getScreen());
		assertEquals(user.getDeviceToken(), actual.getDeviceToken());
		assertEquals(user.getTutorialMark(), actual.getTutorialMark());
		assertEquals(user.getRoleTotalAction(), actual.getRoleTotalAction());
		assertEquals(user.getChargedYuanbao(), actual.getChargedYuanbao());
		assertEquals(user.getChargeCount(), actual.getChargeCount());
		assertEquals("test111", actual.getAccountName());
		assertEquals(1, actual.getWeiboTokenMap().size());
		assertEquals("1234234213423423", actual.getWeiboTokenMap().get("sina"));
		assertEquals(user.getGuildId(), actual.getGuildId());
		//assertEquals(user.getValueMapFieldValue(PropDataEnhanceType.Field.ATTACK), 
		//		actual.getValueMapFieldValue(PropDataEnhanceType.Field.ATTACK));
		
		//Test update
		user.setTotalKills(1001);
		user.setRoleTotalAction(50);
		user.setChargedYuanbao(200);
		user.setChargeCount(2);
		user.setAccountName("test112");
		user.addWeiboToken("qq", "qqtoken");
		user.setGuildId("guildid2");
		//user.addValueMap(PropDataEnhanceType.Field.DEFEND, 300);
		
		manager.saveUser(user, false);
		actual = manager.queryUser("test001");
		assertEquals(1001, actual.getTotalKills());
		assertEquals("960x640", actual.getScreen());
		assertEquals(user.getRoleTotalAction(), actual.getRoleTotalAction());
		assertEquals(user.getChargedYuanbao(), actual.getChargedYuanbao());
		assertEquals(user.getChargeCount(), actual.getChargeCount());
		assertEquals(2, actual.getWeiboTokenMap().size());
		assertEquals("test112", actual.getAccountName());
		assertEquals("qqtoken", actual.getWeiboTokenMap().get("qq"));
		assertEquals(user.getGuildId(), actual.getGuildId());
		/*
		assertEquals(user.getValueMapFieldValue(PropDataEnhanceType.Field.ATTACK), 
				actual.getValueMapFieldValue(PropDataEnhanceType.Field.ATTACK));
		assertEquals(user.getValueMapFieldValue(PropDataEnhanceType.Field.DEFEND), 
				actual.getValueMapFieldValue(PropDataEnhanceType.Field.DEFEND));
				*/
	}
	
	@Test
	public void testQueryBasicUser() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		BasicUser actual = manager.queryBasicUser("test001");
		assertBasicUserEqual(user, actual);
	}
	
	@Test
	public void testQueryBasicUserById() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		BasicUser actual = manager.queryBasicUser(userId);
		assertBasicUserEqual(user, actual);
	}
	
	@Test
	public void testQueryUserByRoleName() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		User actual = manager.queryUserByRoleName("nick001");
		assertUserEqual(user, actual, false);
		
		boolean exist = manager.checkRoleNameExist("nick001");
		assertTrue(exist);
		exist = manager.checkRoleNameExist("nick002");
		assertTrue(!exist);
	}
	
	@Test
	public void testQueryUserById() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		
		User actual = manager.queryUser(userId);
		assertUserEqual(user, actual, false);
	}
	
	@Test
	public void testQueryUserById2() throws Exception {
		try {
			User actual = manager.queryUser(new UserId("test001"));
			assertNull(actual);
		} catch (Exception e) {
			fail("Not found user cannot throw exception.");
		}
	}
	
	@Test
	public void testQueryUserByName() throws Exception {
		try {
			User actual = manager.queryUser("test001");
			assertNull(actual);
		} catch (Exception e) {
			fail("Not found user cannot throw exception.");
		}
	}
	
	@Test
	public void testQueryUserBag() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		user.setBag(makeBag(user, 3));
		user.getBag().addOtherPropDatas(makePropData(1000));
		manager.removeUser("test001");
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		User actual = manager.queryUser("test001");
		Bag actualBag = manager.queryUserBag(actual);
		assertNotNull(actualBag);
		assertEquals(60, actualBag.getMaxCount());
		assertEquals(actual.getBag(), actualBag);
	}
	
	@Test
	public void testSaveUserSomeFields() throws Exception {
		User user = manager.createDefaultUser();
		user.set_id(new UserId("test001"));
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		manager.saveUser(user, true);
		User actual = manager.queryUser("test001");
		assertUserEqual(user, actual, false);
		
		//Test single field
		user.setUsername("test002");
		saveAndTest("test002", manager, user);
		
		user.setRoleName("role002");
		saveAndTest("test002", manager, user);
		
		user.setPassword("pass002");
		saveAndTest("test002", manager, user);
		
		user.setGolden(999);
		saveAndTest("test002", manager, user);
		
		user.setYuanbao(999);
		saveAndTest("test002", manager, user);
		
		user.setVoucher(9999);
		saveAndTest("test002", manager, user);

	  int i=0;
		user.setMedal(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setBlood(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setExp(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setLevel(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setPower(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setDamage(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setAttack(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setDefend(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setAgility(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setLuck(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setSkin(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setTkew(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setIsvip(true);
		saveAndTest("test002", manager, user);
		user.setViplevel(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setVipbdate(new Date());
		saveAndTest("test002", manager, user);
		user.setVipedate(new Date());
		saveAndTest("test002", manager, user);
		user.setVipexp(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setConfigEffectSwitch(false);
		saveAndTest("test002", manager, user);
		user.setConfigEffectVolume(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setConfigHideGlass(true);
		saveAndTest("test002", manager, user);
		user.setConfigHideHat(true);
		saveAndTest("test002", manager, user);
		user.setConfigHideSuite(true);
		saveAndTest("test002", manager, user);
		user.setConfigLeadFinish(true);
		saveAndTest("test002", manager, user);
		user.setConfigMusicSwitch(true);
		saveAndTest("test002", manager, user);
		user.setConfigMusicVolume(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setEmail("test@xinqihd.com");
		saveAndTest("test002", manager, user);
		user.setIconurl("http://icon.url");
		saveAndTest("test002", manager, user);
		user.setClient("iphoneos");
		saveAndTest("test002", manager, user);
		user.setGender(Gender.FEMALE);
		saveAndTest("test002", manager, user);
		user.setCountry("china");
		saveAndTest("test002", manager, user);
		user.setCdate(new Date());
		saveAndTest("test002", manager, user);
		user.setLdate(new Date());
		saveAndTest("test002", manager, user);
		user.setTotalmin(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setWins(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setWinOdds(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setFailcount(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setBattleCount(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setChannel("test");
		saveAndTest("test002", manager, user);
		user.setContinuLoginTimes(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setRemainLotteryTimes(1000+(i++));
		saveAndTest("test002", manager, user);
		user.setAbtest("abtest");
		saveAndTest("test002", manager, user);
		user.setTotalKills(511);
		saveAndTest("test002", manager, user);
		Location loc = new Location();
		loc.x = 1005;
		loc.y = 1006;
		user.setLocation(loc);
		saveAndTest("test002", manager, user);
		
		PropData newTool = makePropData(1000+(i++));
		newTool.setName("无敌剑");
		user.addTool(BuffToolType.Recover);
		saveAndTest("test002", manager, user);
	}
	
	@Test
	public void testSaveBag() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 3));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		User actual = manager.queryUser(userId);
		assertUserEqual(user, actual, false);
		
		Bag bag = manager.queryUserBag(user);
		assertBagEquals(user.getBag(), bag);
	}
		
	@Test
	public void testSaveBag2() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 6));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		Bag bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		
		//Test modify weapon
		PropData weapon = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		weapon.setLevel(999);
		bag.setChangeFlagOnItem(weapon);
		manager.saveUserBag(user, true);
		bag = manager.queryUserBag(user);
		PropData weapon2 = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertEquals(weapon.getLevel(), weapon2.getLevel());
	}
	
	@Test
	public void testSaveBag3() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 6));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		Bag bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		
		//Test modify otherPropData
		int modifyIndex = bag.getOtherPropDatas().size()/2;
		PropData weapon = bag.getOtherPropDatas().get(modifyIndex);
		weapon.setLevel(999);
		bag.setChangeFlagOnItem(weapon);
		manager.saveUserBag(user, true);
		bag = manager.queryUserBag(user);
		PropData weapon2 = bag.getOtherPropDatas().get(modifyIndex);
		assertEquals(weapon.getLevel(), weapon2.getLevel());
	}
	
	@Test
	public void testSaveBag4() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 6));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		Bag bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		
		//Test delete wearPropData & otherPropData
		int expectLength = bag.getOtherPropDatas().size();
		int modifyIndex = expectLength/2;
		PropData weapon = bag.getOtherPropDatas().get(modifyIndex);
		bag.removeOtherPropDatas(modifyIndex + bag.BAG_WEAR_COUNT);
		manager.saveUserBag(user, true);
		
		bag = manager.queryUserBag(user);
		DBObject actual = MongoDBUtil.queryFromMongo(MongoDBUtil.createDBObject(), "testdb", null, 
				"bags", MongoDBUtil.createDBObject("items", Constant.ONE));
		DBObject items = (DBObject)actual.get("items");
		int actualLength = items.keySet().size();
		assertEquals(expectLength, actualLength);
		PropData weapon2 = bag.getOtherPropDatas().get(modifyIndex);
		assertEquals(null, weapon2);
	}
	
	@Test
	public void testSaveBag5() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 6));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		Bag bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		
		//Test delete otherPropData
		int expectLength = bag.getOtherPropDatas().size();
		int modifyIndex = 0;
		bag.removeOtherPropDatas(modifyIndex+Bag.BAG_WEAR_COUNT);
		manager.saveUserBag(user, true);
		
		bag = manager.queryUserBag(user);
		DBObject actual = MongoDBUtil.queryFromMongo(MongoDBUtil.createDBObject(), "testdb", null, 
				"bags", MongoDBUtil.createDBObject("items", Constant.ONE));
		DBObject items = (DBObject)actual.get("items");
		int actualLength = items.keySet().size();
		assertEquals(expectLength, actualLength);
		PropData weapon2 = bag.getOtherPropDatas().get(modifyIndex);
		assertEquals(null, weapon2);
	}
	
	@Test
	public void testSaveBag6() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		user.setBag(makeBag(user, 6));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		Bag bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		
		//Test delete otherPropData
		int expectLength = bag.getOtherPropDatas().size();
		int modifyIndex = expectLength - 1;
		PropData weapon = bag.getOtherPropDatas().get(modifyIndex);
		bag.removeOtherPropDatas(modifyIndex+Bag.BAG_WEAR_COUNT);
		manager.saveUserBag(user, true);
		
		bag = manager.queryUserBag(user);
		DBObject actual = MongoDBUtil.queryFromMongo(MongoDBUtil.createDBObject(), "testdb", null, 
				"bags", MongoDBUtil.createDBObject("items", Constant.ONE));
		DBObject items = (DBObject)actual.get("items");
		int actualLength = items.keySet().size();
		assertEquals(expectLength, actualLength);
		PropData weapon2 = bag.getOtherPropDatas().get(modifyIndex-1);
		assertTrue(!weapon.getName().equals(weapon2.getName()));
	}
	
	@Test
	public void testSaveBag7() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test-997");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.addRelation(makeRelation(user));
		
		int expectOtherPropSize = 5;
		Bag bag = new Bag();
		for ( int i=0; i<expectOtherPropSize; i++) {
			bag.addOtherPropDatas(makePropData(1000+i));
		}
		bag.wearPropData(Constant.BAG_WEAR_COUNT+0, PropDataEquipIndex.WEAPON.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+1, PropDataEquipIndex.RING1.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+2, PropDataEquipIndex.RING2.index());
		user.setBag(bag);
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		assertEquals(5, bag.getOtherPropDatas().size());
		
		//unwear something
		bag.wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
		manager.saveUserBag(user, true);
		
		Bag actualBag = manager.queryUserBag(user);
		assertEquals(5, actualBag.getOtherPropDatas().size());
		assertNull(actualBag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index()));
		
		//Test delete otherPropData
		int expectLength = bag.getOtherPropDatas().size();
		int modifyIndex = expectLength - 1;
		PropData weapon = bag.getOtherPropDatas().get(modifyIndex);
		bag.removeOtherPropDatas(modifyIndex+Bag.BAG_WEAR_COUNT);
		user.setBag(bag);
		manager.saveUserBag(user, true);
		
		bag = manager.queryUserBag(user);
		DBObject actual = MongoDBUtil.queryFromMongo(MongoDBUtil.createDBObject(), "testdb", null, 
				"bags", MongoDBUtil.createDBObject("items", Constant.ONE));
		DBObject items = (DBObject)actual.get("items");
		int actualLength = items.keySet().size();
		assertEquals(expectLength, actualLength);
		PropData weapon2 = bag.getOtherPropDatas().get(modifyIndex-1);
		assertTrue(!weapon.getName().equals(weapon2.getName()));
	}
	
	@Test
	public void testSaveBagPropDataUsedTime() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test-997");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		PropData propData = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).toPropData(5, WeaponColor.WHITE);		
		
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		assertEquals(1, bag.getOtherPropDatas().size());
		assertEquals(5, bag.getOtherPropDatas().get(0).getPropIndate());
		assertEquals(0, bag.getOtherPropDatas().get(0).getPropUsedTime());
		
		propData.setPropUsedTime(2);
		bag.markChangeFlag(Bag.BAG_WEAR_COUNT);
		
		manager.saveUserBag(user, false);
		Bag actualBag = manager.queryUserBag(user);
		assertEquals(1, bag.getOtherPropDatas().size());
		assertEquals(5, bag.getOtherPropDatas().get(0).getPropIndate());
		assertEquals(2, bag.getOtherPropDatas().get(0).getPropUsedTime());
						
	}
	
	@Test
	public void testSaveWearingPropDataUsedTime() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test-997");
		user.set_id(userId);
		user.setUsername("test001");
		user.setChannel("testSaveUser");
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
//		UserManager.getInstance().saveUserBag(user, false);
		
		PropData propData = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).toPropData(5, WeaponColor.WHITE);		
		propData.setPropIndate(10);
		propData.setPropUsedTime(1);
		
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		bag.wearPropData(Bag.BAG_WEAR_COUNT, 17);
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		bag = user.getBag();
		assertTrue(!bag.clearGeneralChangeFlag());
		assertEquals(0, bag.clearMarkedChangeFlag().size());
		PropData actualProp = bag.getWearPropDatas().get(17);
		assertNotNull( actualProp );
		assertEquals(10, actualProp.getPropIndate());
		assertEquals(1, actualProp.getPropUsedTime());
		
		propData.setPropUsedTime(2);
		bag.setWearPropData(propData, 17);
		
		manager.saveUserBag(user, false);
		
		Bag actualBag = manager.queryUserBag(user);
		actualProp = actualBag.getWearPropDatas().get(17);
		assertNotNull( actualProp );
		assertEquals(10, actualProp.getPropIndate());
		assertEquals(2, actualProp.getPropUsedTime());
						
	}
	
	@Test
	public void testSaveRelation() throws Exception {
		UserId userId = new UserId("test001");
		ArrayList<Relation> rlist = new ArrayList<Relation>(RelationType.values().length);
		rlist.add(makeRelation(3, RelationType.FRIEND, userId));
		People expect = rlist.get(0).findPeopleByUserName("test-1");
		manager.saveUserRelation(rlist);
		
		User user = new User();
		user.set_id(userId);
		user = manager.queryUserRelation(user);
		
		Thread.currentThread().sleep(200);
		
		assertTrue(user.getRelations().size()>0);
		assertEquals(3, user.getRelation(RelationType.FRIEND).listPeople().size());
		assertEquals(0, user.getRelation(RelationType.FRIEND).clearChangeMark().size());
		
		People actual = user.getRelation(RelationType.FRIEND).findPeopleByUserName("test-1");
		assertEquals(expect.getId(), actual.getId());
		assertEquals(expect.getUsername(), actual.getUsername());
		assertEquals(expect.getWin(), actual.getWin());
		assertEquals(expect.getLose(), actual.getLose());
	}
	
	@Test
	public void testSaveRelation2() {
		User user = new User();
		user.set_id(new UserId("test001"));
		
		for ( int i=0; i<RelationType.values().length; i++ ) {
			user.addRelation(makeRelation(3, RelationType.values()[i],user.get_id()));
		}
		manager.saveUserRelation(user.getRelations());
		
		//Delete a user
		People p = makePeople("test-1");
		assertTrue( user.getRelation(RelationType.FRIEND).removePeople(p) != null );
		manager.saveUserRelation(user.getRelations());

		user = manager.queryUserRelation(user);
		assertEquals(5, user.getRelations().size());
		assertEquals(2, user.getRelation(RelationType.FRIEND).listPeople().size());
		assertEquals(3, user.getRelation(RelationType.BLACKLIST).listPeople().size());
		assertEquals(3, user.getRelation(RelationType.GUILD).listPeople().size());
		assertEquals(3, user.getRelation(RelationType.RECENT).listPeople().size());
		assertEquals(3, user.getRelation(RelationType.RIVAL).listPeople().size());
		assertEquals(0, user.getRelation(RelationType.FRIEND).clearChangeMark().size());
	}
	
	@Test
	public void testSaveRelation3() {
		UserId userId = new UserId("test001");
		ArrayList<Relation> rlist = new ArrayList<Relation>(RelationType.values().length);
		rlist.add(makeRelation(3, RelationType.FRIEND, userId));
		manager.saveUserRelation(rlist);
		//Delete a user
		People p = makePeople("test-1");
		assertTrue( rlist.get(0).removePeople(p) != null );
		manager.saveUserRelation(rlist);
	}
	
	@Test
	public void testSaveFriendWinOrLost() {
		UserId userId = new UserId("test-001");		
		manager.removeUser(userId);
		
		User user = manager.createDefaultUser();
		user.set_id(userId);
		user.setRoleName("test-001");
		user.setUsername("test-001");
		manager.saveUser(user, true);

		ArrayList<Relation> rlist = new ArrayList<Relation>(RelationType.values().length);
		rlist.add(makeRelation(3, RelationType.FRIEND, userId));
		manager.saveUserRelation(rlist);
		manager.queryUserRelation(user);
		
		User friend = new User();
		String roleName = "test-0";
		friend.setRoleName(roleName);
		manager.saveFriendWinOrLose(user, friend, true);
		
		//Verity
		User actualUser = manager.queryUser(userId);
		actualUser = manager.queryUserRelation(actualUser);
		Relation relation = actualUser.getRelation(RelationType.FRIEND);
		People p = relation.findPeopleByUserName("test-0");
		assertEquals(1, p.getWin());
		assertEquals(0, p.getLose());
	}
	
	@Test
	public void testSaveFriendWinOrLostMutil() {
		UserId userId = new UserId("test-001");		
		manager.removeUser(userId);
		
		User user = manager.createDefaultUser();
		user.set_id(userId);
		user.setRoleName("test-001");
		user.setUsername("test-001");
		manager.saveUser(user, true);

		ArrayList<Relation> rlist = new ArrayList<Relation>(RelationType.values().length);
		rlist.add(makeRelation(3, RelationType.FRIEND, userId));
		rlist.add(makeRelation(3, RelationType.RIVAL, userId));
		manager.saveUserRelation(rlist);
		manager.queryUserRelation(user);
		
		User friend = new User();
		String roleName = "test-0";
		friend.setRoleName(roleName);
		Map<RelationType, Collection<People>> people =
				manager.saveFriendWinOrLose(user, friend, true);
		assertEquals(RelationType.values().length, people.size());
		assertEquals(1, people.get(RelationType.FRIEND).size());
		assertEquals(1, people.get(RelationType.RIVAL).size());
		
		//Verity
		User actualUser = manager.queryUser(userId);
		actualUser = manager.queryUserRelation(actualUser);
		Relation relation = actualUser.getRelation(RelationType.FRIEND);
		People p = relation.findPeopleByUserName("test-0");
		assertEquals(1, p.getWin());
		assertEquals(0, p.getLose());
		
		relation = actualUser.getRelation(RelationType.RIVAL);
		p = relation.findPeopleByUserName("test-0");
		assertEquals(1, p.getWin());
		assertEquals(0, p.getLose());

	}
	
	@Test
	public void testSaveUserLoginStatus() {
		UserId userId = new UserId("test-001");
		
		manager.removeUser(userId);
		
		User user = manager.createDefaultUser();
		user.set_id(userId);
		user.setUsername("test-001");
		manager.saveUser(user, true);
		
		User actualUser = manager.queryUser(userId);
		assertNotNull(actualUser);
		assertEquals(UserLoginStatus.NORMAL, actualUser.getLoginStatus());
		assertEquals(Constant.EMPTY,actualUser.getLoginStatusDesc());
		
		//Change the status
		user.setLoginStatus(UserLoginStatus.PAUSE);
		user.setLoginStatusDesc("paused");
		manager.saveUser(user, false);
		
		actualUser = manager.queryUser(userId);
		assertNotNull(actualUser);
		assertEquals(UserLoginStatus.PAUSE, actualUser.getLoginStatus());
		assertNotNull("paused", actualUser.getLoginStatusDesc());
	}
	
	@Test
	public void testUnlockGameFunction() {
		UserId userId = new UserId("test-001");
		User user = new User();
		user.set_id(userId);
		user.setUsername("test-001");
		user.setRoleName(user.getUsername());
		manager.removeUser(userId);
		
		//SINGLE_ROOM is auto unlocked.
		Collection<Unlock> unlocks = manager.queryUserUnlock(user);
		assertEquals(1, unlocks.size());
		
		Unlock unlock = new Unlock();
		unlock.setId(user.get_id());
		unlock.setFuncType(GameFuncType.Room);
		unlock.setFuncValue(RoomType.SINGLE_ROOM.ordinal());
		
		manager.addUserNewUnlock(user, unlock);
		assertEquals(1, unlocks.size());
		assertEquals(1, user.getUnlocks().size());
		
		//Query from database
		User actualUser = manager.queryUser(userId);
		manager.queryUserUnlock(actualUser);
		assertEquals(1, unlocks.size());
		assertEquals(unlock, unlocks.iterator().next());
	}
	
	@Test
	public void testUnlockGameFunctionAgain() {
		UserId userId = new UserId("test-001");
		User user = new User();
		user.set_id(userId);
		user.setUsername("test-001");
		user.setRoleName(user.getUsername());
		manager.removeUser(userId);
		
		Collection<Unlock> unlocks = manager.queryUserUnlock(user);
		assertEquals(1, unlocks.size());
		
		Unlock unlock = new Unlock();
		unlock.setId(user.get_id());
		unlock.setFuncType(GameFuncType.Room);
		unlock.setFuncValue(RoomType.SINGLE_ROOM.ordinal());
		
		boolean success = manager.addUserNewUnlock(user, unlock);
		assertEquals(false, success);
		
		manager.addUserNewUnlock(user, unlock);
		manager.addUserNewUnlock(user, unlock);
		manager.addUserNewUnlock(user, unlock);
		success = manager.addUserNewUnlock(user, unlock);
		assertEquals(false, success);

		assertEquals(1, unlocks.size());

		//Query from database
		User actualUser = manager.queryUser(userId);
		manager.queryUserUnlock(actualUser);
		assertEquals(1, unlocks.size());
		assertEquals(unlock, unlocks.iterator().next());
	}
	
	@Test
	public void testRemoveUser() {
		UserId userId = new UserId("test-001");
		
		manager.removeUser(userId);
		
		User user = manager.createDefaultUser();
		user.set_id(userId);
		user.setUsername("test-001");
		
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		user.setBag(makeBag(user, 3));
		user.addRelation(makeRelation(user));
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		manager.saveUserRelation(user.getRelations());
		
		User expect = manager.queryUser(userId);
		manager.queryUserBag(expect);
		manager.queryUserRelation(expect);
		
		assertEquals(60, expect.getBag().getMaxCount());
		assertEquals(1, expect.getRelations().size());
		
		manager.removeUser(user.getUsername());
		
		User actual = manager.queryUser(userId);
		assertNull(actual);
		actual = manager.queryUserRelation(user);
		assertNull(actual);
		Bag bag = manager.queryUserBag(user);
		assertEquals(null, bag);
	}
	
	@Test
	public void testRemoveUser2() {
		manager.removeUser(new UserId("not-exist-id"));
		manager.removeUser("not-exist-name");
	}
	
	@Test
	public void testUserConvert() throws Exception {
		User user = manager.createDefaultUser();
		UserId userId = new UserId("test001");
		user.set_id(userId);
		user.setUsername("test001");
		user.setRoleName("nick001");
		user.setChannel("testSaveUser");
		for ( int i=0; i<1; i++ ) {
			user.addTool(makeBuffTool(i));
		}
		Bag bag = user.getBag();
		WeaponPojo wearedWeapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData weared = wearedWeapon.toPropData(30, WeaponColor.PINK);
		weared.setLevel(5);
		weared.setEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.ATTACK, 200);
		bag.setWearPropData(weared, 17);
		bag.addOtherPropDatas(weared.clone());
		
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		String userStr = manager.convertUserToString(user);
		System.out.println(userStr);
		User actualUser = manager.convertStringToUser(userStr);
		assertUserEqual(user, actualUser, false);
		
		String bagStr = manager.convertBagToString(bag);
		System.out.println(bagStr);
		Bag actualBag = manager.convertStringToBag(bagStr);	
		assertBagEquals(bag, actualBag);
	}
		
	private Relation makeRelation(User user) {
		Relation relation = new Relation();
		relation.set_id(user.get_id());
		relation.setParentUser(user);
		relation.setType(RelationType.FRIEND);
		People people = new People();
		people.setMyId(new UserId("myId"));
		people.setUsername("我是张三");
		people.setWin(99);
		people.setLose(1);
		relation.addPeople(people);
		return relation;
	}
	
	private Relation makeRelation(int count, RelationType type, UserId userId) {
		Relation relation = new Relation();
		relation.set_id(userId);
		relation.setType(type);
		for ( int i=0; i<count; i++ ) {
			String userName = "test-" + i;
			relation.addPeople(makePeople(userName));
		}
		return relation;
	}
	
	private People makePeople(String userName) {
		UserId userId = new UserId(userName);
		People p = new People();
		p.setId(userId);
		p.setMyId(new UserId("myId"));
		p.setUsername(userName);
		p.setWin(0);
		p.setLose(0);
		return p;
	}
	
	private void saveAndTest(String userName, UserManager manager, User expect) throws Exception {
		manager.saveUser(expect, false);
		User actual = manager.queryUser("test002");
		assertUserEqual(expect, actual, false);
	}
	
	private void assertUserEqual(Object expceted, Object actual, boolean compareBagAndRelation) throws Exception {
		Field[] fields = User.class.getDeclaredFields();
		for ( int i=0; i<fields.length; i++ ) {
			fields[i].setAccessible(true);
			int modifier = fields[i].getModifiers();
			if ( Modifier.isStatic(modifier) || Modifier.isTransient(modifier) ) {
				continue;
			}
			Object expectValue = fields[i].get(expceted);
			Object actualValue = fields[i].get(actual);
			if ( fields[i].getName().equals("bag") ) {
//				Bag expectBag = (Bag)expectValue;
//				Bag actualBag = (Bag)actualValue;
//				if ( compareBagAndRelation ) {
//					assertArrayEquals(expectBag.getOtherPropDatas().toArray(), actualBag.getOtherPropDatas().toArray());
//					assertArrayEquals(expectBag.getWearPropDatas().toArray(), actualBag.getWearPropDatas().toArray());
//				} else {
//					assertTrue(actualBag.getOtherPropDatas().size()==0);
//					assertTrue(actualBag.getWearPropDatas().size()==0);
//				}
			} else if ( fields[i].getName().equals("tools") ) {
				List<PropData> expectTools = (List<PropData>)expectValue;
				List<PropData> actualTools = (List<PropData>)actualValue;
				assertArrayEquals(expectTools.toArray(), actualTools.toArray());
			} else if ( fields[i].getName().equals("changeFields") ) {
				//ignore it.
			} else if ( fields[i].getName().equals("relations") ) {
				EnumMap<RelationType, Relation> expectRel = (EnumMap<RelationType, Relation>)expectValue;
				EnumMap<RelationType, Relation> actualRel = (EnumMap<RelationType, Relation>)actualValue;
				if ( compareBagAndRelation ) {
					assertArrayEquals(expectRel.values().toArray(), actualRel.values().toArray());
				} else {
					assertTrue(actualRel.size()==0);
				}
			} else {
				System.out.println("field: " + fields[i].getName());
				assertEquals(expectValue, actualValue);
			}
		}
	}
	
	private void assertBagEquals(Bag expectBag, Bag actualBag) throws Exception {
		assertArrayEquals(expectBag.getWearPropDatas().toArray(), 
				actualBag.getWearPropDatas().toArray());
		
		int expectLength = expectBag.getOtherPropDatas().size();
		int actualLength = actualBag.getOtherPropDatas().size();
		Object[] expectArray = expectBag.getOtherPropDatas().toArray();
		Object[] actualArray = actualBag.getOtherPropDatas().toArray();
		if ( expectLength ==  actualLength ) {
			assertArrayEquals(expectArray,  actualArray);			
		} else if ( expectLength > actualLength ) {
			for ( int i=0; i<actualLength; i++ ) {
				assertEquals(expectArray[i], actualArray[i]);
			}
			for ( int i=actualLength; i<expectLength; i++ ) {
				assertEquals(null, expectArray[i]);
			}
		} else if ( expectLength < actualLength ) {
			for ( int i=0; i<expectLength; i++ ) {
				assertEquals(expectArray[i], actualArray[i]);
			}
			for ( int i=expectLength; i<expectLength; i++ ) {
				assertEquals(null, actualArray[i]);
			}
		}
	}
	
	private void assertBasicUserEqual(Object expected, Object actual) throws Exception {
		HashMap<String, Field> expectFields = new HashMap<String, Field>();
		if ( expected instanceof User ) {
			Field[] fields = User.class.getDeclaredFields();
			for ( int i=0; i<fields.length; i++ ) {
				fields[i].setAccessible(true);
				expectFields.put(fields[i].getName(), fields[i]);
			}
		}
		Field[] fields = BasicUser.class.getDeclaredFields();
		for ( int i=0; i<fields.length; i++ ) {
			fields[i].setAccessible(true);
			int modifier = fields[i].getModifiers();
			if ( Modifier.isStatic(modifier) || Modifier.isTransient(modifier) ) {
				continue;
			}
			Field expectField = expectFields.get(fields[i].getName());
			System.out.println(fields[i].getName()+"="+expectField);
			if ( expectField == null ) continue;
			Object expectValue = expectField.get(expected);
			Object actualValue = fields[i].get(actual);
			if ( fields[i].getName().equals("changeFields") ) {
				//ignore it.
			} else {
//				System.out.println("field: " + fields[i].getName());
				if ( expectValue != null && actualValue != null ) {
					assertEquals(fields[i].getName(), expectValue, actualValue);
				}
			}
		}
	}
		
	private Bag makeBag(User user, int count) {
		Bag bag = new Bag();
//		bag.set_id(user.get_id());
//		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(1000+i));
		}
		bag.wearPropData(Constant.BAG_WEAR_COUNT+0, PropDataEquipIndex.WEAPON.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+1, PropDataEquipIndex.RING1.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+2, PropDataEquipIndex.RING2.index());
		return bag;
	}
	
	/**
	 * Make a fake PropData
	 * @param i
	 * @return
	 */
	private PropData makePropData(int i) {
		PropData propData = new PropData();
		propData.setItemId("510");
		propData.setName("夺命刀"+i);
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}

	private BuffToolType makeBuffTool(int i) {
		if ( i >= 0 && i<BuffToolType.values().length ) {
			return BuffToolType.values()[i];
		} else {
			return BuffToolType.Recover;
		}
	}
}
