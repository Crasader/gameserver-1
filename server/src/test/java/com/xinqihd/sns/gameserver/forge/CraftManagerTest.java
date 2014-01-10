package com.xinqihd.sns.gameserver.forge;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.SecureLimitManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataSlot;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddProp.BseAddProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseCompose.BseCompose;
import com.xinqihd.sns.gameserver.proto.XinqiBseForge.BseForge;
import com.xinqihd.sns.gameserver.proto.XinqiBseTransfer.BseTransfer;
import com.xinqihd.sns.gameserver.proto.XinqiPropData;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class CraftManagerTest {
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
		UserManager.getInstance().removeUser(userName);
		SecureLimitManager.getInstance().setDisableSecureChecking(true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComposeItem() throws Exception {
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone(user);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		int i = 0;
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			list.clear();
			manager.composeItem(user, new int[]{21, 22, 23, 24, 25});
			
			Thread.sleep(500);
			
			BseCompose compose = null;
			BseAddProp addProp = null;
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseCompose ) {
					compose = (BseCompose)xinqi.payload;
				} else if ( xinqi.payload instanceof BseAddProp ) {
					addProp = (BseAddProp)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( compose.getResult() == ComposeStatus.SUCCESS.ordinal() ) {
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData newPropData = compose.getNewProp();
				//	<item id="20007" typeid="20002" lv="2" icon="BlackStoneLv2" name="土神石Lv2"
				assertEquals("20007", newPropData.getId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(2, newPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertTrue(bag.getOtherPropData(26) != null);
				assertEquals(26, newPropData.getPropPew());
				
				System.out.println("try: " + i);
				break;
			} else if ( compose.getResult() == ComposeStatus.FAILURE.ordinal()  ) {
				//BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData newPropData = compose.getNewProp();
				assertEquals("", newPropData.getId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(0, newPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertEquals(null, bag.getOtherPropData(26));
				assertEquals(0, newPropData.getPropPew());
				
				System.out.println("try: " + i);
				break;
			} else {
				assertEquals(1, list.size());
				assertEquals(0, compose.getPewsCount());
			}
		}
	}
	
	@Test
	public void testComposeItemPrice() throws Exception {
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone(user);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		ArrayList result = manager.composeItemPriceAndRatio(user, new int[]{21, 22, 23, 24, 25});
		int price = (Integer)result.get(0);
		double ratio = (Double)result.get(1);
		//use those stones
		manager.composeItem(user, new int[]{21, 22, 23, 24, 25});
		
		//Try to compose higher level stones.
		makeBagWithDefendStoneLevel3(user);
		result = manager.composeItemPriceAndRatio(user, new int[]{21, 22, 23, 24, 25});
		int level3Price = (Integer)result.get(0);
		double level3Ratio = (Double)result.get(1);
		assertTrue(level3Price+">"+price, level3Price>price);
		assertTrue(level3Ratio+"=="+ratio, level3Ratio==ratio);
		
		System.out.println(result);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testForgeItem() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(10000);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone2(user, 0, 1, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		int i = 0;
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			list.clear();
			manager.forgeEquip(user, 20, new int[]{21, 22, 23, 24, 25});
			
			Thread.sleep(500);
			
			BseForge forge = null;
			BseAddProp addProp = null;
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseForge ) {
					forge = (BseForge)xinqi.payload;
				} else if ( xinqi.payload instanceof BseAddProp ) {
					addProp = (BseAddProp)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( forge.getResult() == 1 ) {
				//BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData xinqiPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, xinqiPropData.getId());
				//Only displays the added value
				assertEquals(1, xinqiPropData.getAttackLev());
				assertEquals(7, xinqiPropData.getDamageLev());
				assertEquals(0, xinqiPropData.getAgilityLev());
				assertEquals(0, xinqiPropData.getLuckLev());
				assertEquals(0, xinqiPropData.getDefendLev());
				assertEquals(0, xinqiPropData.getSkinLev());
				assertEquals(1, xinqiPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertEquals(null, bag.getOtherPropData(25));
				
				System.out.println("try: " + i);
				break;
			} else {
				assertEquals(1, list.size());
				assertEquals(0, forge.getOtherPewsCount());
			}
		}
	}

	@Test
	public void testForgeItemWearing() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone3(user, 0, 4, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		int i = 0;
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			list.clear();
			manager.forgeEquip(user, PropDataEquipIndex.WEAPON.index(), 
					new int[]{20, 21, 22, 23, 24});
			
			Thread.sleep(500);
			
			BseForge forge = null;
			BseAddProp addProp = null;
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseForge ) {
					forge = (BseForge)xinqi.payload;
				} else if ( xinqi.payload instanceof BseAddProp ) {
					addProp = (BseAddProp)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( forge.getResult() == 1 ) {
				//BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData newPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, newPropData.getId());
				//Only displays the added value
				assertEquals(1, newPropData.getAttackLev());
				assertEquals(1, newPropData.getDamageLev());
				assertEquals(0, newPropData.getAgilityLev());
				assertEquals(0, newPropData.getLuckLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(0, newPropData.getSkinLev());
				assertEquals(1, newPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				PropData weapon = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
				assertEquals(1, weapon.getLevel());
				assertEquals(null, bag.getOtherPropData(20));
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				
				System.out.println("try: " + i);
				break;
			} else {
				assertEquals(1, list.size());
				assertEquals(0, forge.getOtherPewsCount());
			}
		}
	}
	
	@Test
	public void testForgeItemWearingPriceAndRatio() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone3(user, 0, 4, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		ArrayList result = manager.forgeEquipPriceAndRatio(user, PropDataEquipIndex.WEAPON.index(), 
				new int[]{20, 21, 22, 23, 24});
		int price = (Integer)result.get(0);
		double ratio = (Double)result.get(1);
		System.out.println("price="+price+",ratio="+ratio);
		//Use those stones
		manager.forgeEquip(user, PropDataEquipIndex.WEAPON.index(), 
				new int[]{20, 21, 22, 23, 24});
	}
	
	@Test
	public void testForgeItemWearingAndManyStones() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone3(user, 0, 100, 4, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		int i = 0;
		for ( i=0; i<Integer.MAX_VALUE; i++ ) {
			list.clear();
			manager.forgeEquip(user, PropDataEquipIndex.WEAPON.index(), 
					new int[]{20, 21, 22, 23, 24});
			
			Thread.sleep(500);
			
			BseForge forge = null;
			BseAddProp addProp = null;
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseForge ) {
					forge = (BseForge)xinqi.payload;
				} else if ( xinqi.payload instanceof BseAddProp ) {
					addProp = (BseAddProp)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( forge.getResult() == 1 ) {
				//BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData xinqiPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, xinqiPropData.getId());
				//The base attackLev value '9' is substracted from new value
				//So the current attackLev is '0'
				assertEquals(1, xinqiPropData.getAttackLev());
				assertEquals(0, xinqiPropData.getDefendLev());
				assertEquals(1, xinqiPropData.getDamageLev());
				assertEquals(0, xinqiPropData.getSkinLev());
				assertEquals(1, xinqiPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				PropData weapon = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
				assertEquals(1, weapon.getLevel());
				assertEquals(99, bag.getOtherPropData(20).getCount());
				assertEquals(99, bag.getOtherPropData(21).getCount());
				assertEquals(99, bag.getOtherPropData(22).getCount());
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				
				System.out.println("try: " + i);
				break;
			} else {
				assertEquals(1, list.size());
				assertEquals(0, forge.getOtherPewsCount());
			}
		}
		
	}
	
	@Test
	public void testForgeItemToHighLevel() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone2(user, 8, 1, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		int i = 0;
		for ( i=0; i<1; i++ ) {
			list.clear();
			
			manager.forgeEquip(user, 20, new int[]{21, 22, 23, 24, 25});
			Thread.sleep(500);
			
			BseForge forge = null;
			
			for ( Object obj : list ) {
				XinqiMessage xinqi = (XinqiMessage)obj;
				if ( xinqi.payload instanceof BseForge ) {
					forge = (BseForge)xinqi.payload;
				}
			}
			//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
			if ( forge.getResult() == ForgeStatus.SUCCESS.ordinal() ) {
				assertEquals(1, list.size());
				
				XinqiPropData.PropData newPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, newPropData.getId());
				assertTrue(newPropData.getAttackLev()>250);
				assertTrue(newPropData.getDefendLev()>80);
				assertEquals(9, newPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertEquals(null, bag.getOtherPropData(25));
			} else if ( forge.getResult() == ForgeStatus.FAILURE.ordinal() ) {
			  //BseRoleInfo + BseCompose
				assertTrue(list.size()>=2);
				
				XinqiPropData.PropData newPropData = forge.getUpdateProp();
				assertEquals(UserManager.basicWeaponItemId, newPropData.getId());
				assertEquals(0, newPropData.getAttackLev());
				assertEquals(0, newPropData.getDefendLev());
				assertEquals(8, newPropData.getLevel());
				
				//Check bag
				User actualUser = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(actualUser);
				bag = actualUser.getBag();
				assertTrue(bag.getOtherPropData(20) != null);
				assertEquals(null, bag.getOtherPropData(21));
				assertEquals(null, bag.getOtherPropData(22));
				assertEquals(null, bag.getOtherPropData(23));
				assertEquals(null, bag.getOtherPropData(24));
				assertEquals(null, bag.getOtherPropData(25));
			} else {
				break;
			}
			
			bag.cleanOtherPropDatas();
			makeBagWithStone2(user, 8, 1, 1);
			UserManager.getInstance().saveUser(user, true);
			UserManager.getInstance().saveUserBag(user, true);
		}		
	}

	@Test
	public void testTransferItem() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setAddAttack(10);
		srcPojo.setAddDefend(10);
		srcPojo.setAddAgility(10);
		srcPojo.setAddLuck(10);
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById("510").clone();
		targetPojo.setAddAttack(100);
		targetPojo.setAddDefend(100);
		targetPojo.setAddAgility(100);
		targetPojo.setAddLuck(100);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30000, WeaponColor.BLUE);
		PropData tarProp = targetPojo.toPropData(30000, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.transferEquip(user, 20, 21);
		
		Thread.sleep(500);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		if ( transfer.getResult() == TransferStatus.SUCCESS.ordinal() ) {
			//BseRoleInfo + BseCompose
			//assertTrue(list.size()>=2);
			
			assertEquals(UserManager.basicWeaponItemId, srcProp.getItemId());
			assertEquals(10, srcProp.getAttackLev());
			assertEquals(13, srcProp.getDefendLev());
			assertEquals(13, srcProp.getAgilityLev());
			assertEquals(13, srcProp.getLuckLev());
			assertEquals(0, srcProp.getLevel());
			
			assertEquals("510", tarProp.getItemId());
			assertEquals(158, tarProp.getAttackLev());
			assertEquals(125, tarProp.getDefendLev());
			assertEquals(125, tarProp.getAgilityLev());
			assertEquals(125, tarProp.getLuckLev());
			assertEquals(4, tarProp.getLevel());
			assertEquals(true, tarProp.containEnhanceValue(PropDataEnhanceType.STRENGTH));
			
			//Check bag
			User actualUser = UserManager.getInstance().queryUser(userId);
			UserManager.getInstance().queryUserBag(actualUser);
			
			bag = actualUser.getBag();
			PropData srcP = bag.getOtherPropData(20);
			assertEquals(UserManager.basicWeaponItemId, srcP.getItemId());
			assertEquals(10, srcP.getAttackLev());
			assertEquals(13, srcP.getDefendLev());
			assertEquals(13, srcP.getAgilityLev());
			assertEquals(13, srcP.getLuckLev());
			assertEquals(0, srcP.getLevel());
			
			PropData tarP = bag.getOtherPropData(21);
			assertEquals("510", tarP.getItemId());
			assertEquals(158, tarP.getAttackLev());
			assertEquals(125, tarP.getDefendLev());
			assertEquals(125, tarP.getAgilityLev());
			assertEquals(125, tarP.getLuckLev());
			assertEquals(4, tarP.getLevel());
			assertEquals(true, tarP.containEnhanceValue(PropDataEnhanceType.STRENGTH));
			
		} else {
			System.out.println(list);
//			assertEquals(1, list.size());
			assertEquals(null, transfer.getSrcEquip());
			assertEquals(null, transfer.getTarEquip());
		}
		
	}
	
	@Test
	public void testTransferItemPrice() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setAddAttack(10);
		srcPojo.setAddDefend(10);
		srcPojo.setAddAgility(10);
		srcPojo.setAddLuck(10);
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById("510").clone();
		targetPojo.setAddAttack(100);
		targetPojo.setAddDefend(100);
		targetPojo.setAddAgility(100);
		targetPojo.setAddLuck(100);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30000, WeaponColor.BLUE);
		PropData tarProp = targetPojo.toPropData(30000, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		ArrayList result = manager.transferEquipPriceAndRatio(user, 20, 21);
		int price = (Integer)result.get(0);
		double ratio = (Double)result.get(1);
		System.out.println("price="+price+", ratio="+ratio);
	}
	
	@Test
	public void testTransferItemDiffLevelNoVip() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(1000);
		user.setYuanbao(100);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		int nextLevel = StringUtil.toInt(UserManager.basicWeaponItemId, 0)+1;
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById(""+nextLevel);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30, WeaponColor.BLUE);
		PropData tarProp = targetPojo.toPropData(30, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.transferEquip(user, 20, 21);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "craft.transfer", 1);
		
		Thread.sleep(500);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		if ( transfer.getResult() == TransferStatus.FAILURE.ordinal() ) {
			System.out.println(list);
			assertEquals(1000, user.getGolden());
		}
	}
	
	@Test
	public void testTransferItemDiffLevelVip() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(1000000);
		user.setYuanbao(100);
		user.setIsvip(true);
		user.setViplevel(10);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		int nextLevel = StringUtil.toInt(UserManager.basicWeaponItemId, 0)+1;
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById(""+nextLevel);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30, WeaponColor.BLUE);
		PropData tarProp = targetPojo.toPropData(30, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.transferEquip(user, 20, 21);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "craft.transfer", 1);
		
		Thread.sleep(500);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		if ( transfer.getResult() == TransferStatus.FAILURE.ordinal() ) {
			System.out.println(list);
			assertEquals(1000, user.getGolden());
		}
	}
	
	@Test
	public void testTransferItemDiffLevelColor() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(1000);
		user.setYuanbao(100);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		int nextLevel = StringUtil.toInt(UserManager.basicWeaponItemId, 0)+1;
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById(""+nextLevel);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30, WeaponColor.WHITE);
		PropData tarProp = targetPojo.toPropData(30, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.transferEquip(user, 20, 21);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "craft.transfer", 1);
		
		Thread.sleep(500);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		if ( transfer.getResult() == TransferStatus.FAILURE.ordinal() ) {
			System.out.println(list);
			assertEquals(1000, user.getGolden());
		}
	}
	
	@Test
	public void testTransferItemWearing() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);

		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).clone();
		srcPojo.setAddAttack(10);
		srcPojo.setAddDefend(10);
		srcPojo.setAddAgility(10);
		srcPojo.setAddLuck(10);
		srcPojo.setLv(4);
		
		//510	黑铁●小蝎子
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById("510").clone();
		targetPojo.setAddAttack(100);
		targetPojo.setAddDefend(100);
		targetPojo.setAddAgility(100);
		targetPojo.setAddLuck(100);
		targetPojo.setLv(0);
		
		PropData srcProp = srcPojo.toPropData(30000, WeaponColor.BLUE);
		PropData tarProp = targetPojo.toPropData(30000, WeaponColor.BLUE);
		bag.addOtherPropDatas(srcProp);
		bag.addOtherPropDatas(tarProp);
		bag.wearPropData(20, PropDataEquipIndex.WEAPON.index());
		int oldPower = user.getPower();
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.transferEquip(user, PropDataEquipIndex.WEAPON.index(), 21);
		
		Thread.sleep(500);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		if ( transfer.getResult() == TransferStatus.SUCCESS.ordinal() ) {
			////BseRoleInfo + BseCompose
			//assertTrue(list.size()>=2);
			int newPower = user.getPower();
			assertTrue("Transfer the level from wearing to bag: "
					+newPower+"<"+oldPower, newPower<oldPower);
			
			assertEquals(UserManager.basicWeaponItemId, srcProp.getItemId());
			assertEquals(10, srcProp.getAttackLev());
			assertEquals(13, srcProp.getDefendLev());
			assertEquals(13, srcProp.getAgilityLev());
			assertEquals(13, srcProp.getLuckLev());
			assertEquals(0, srcProp.getLevel());
			
			assertEquals("510", tarProp.getItemId());
			assertEquals(158, tarProp.getAttackLev());
			assertEquals(125, tarProp.getDefendLev());
			assertEquals(125, tarProp.getAgilityLev());
			assertEquals(125, tarProp.getLuckLev());
			assertEquals(4, tarProp.getLevel());
			assertEquals(true, tarProp.containEnhanceValue(PropDataEnhanceType.STRENGTH));
			
			//Check bag
			User actualUser = UserManager.getInstance().queryUser(userId);
			UserManager.getInstance().queryUserBag(actualUser);
			
			bag = actualUser.getBag();
			PropData srcP = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
			assertEquals(UserManager.basicWeaponItemId, srcP.getItemId());
			assertEquals(10, srcP.getAttackLev());
			assertEquals(13, srcP.getDefendLev());
			assertEquals(13, srcP.getAgilityLev());
			assertEquals(13, srcP.getLuckLev());
			assertEquals(0, srcP.getLevel());
			
			PropData tarP = bag.getOtherPropData(21);
			assertEquals("510", tarP.getItemId());
			assertEquals(158, tarP.getAttackLev());
			assertEquals(125, tarP.getDefendLev());
			assertEquals(125, tarP.getAgilityLev());
			assertEquals(125, tarP.getLuckLev());
			assertEquals(4, tarP.getLevel());
			assertEquals(true, tarProp.containEnhanceValue(PropDataEnhanceType.STRENGTH));
			
		} else {
			System.out.println(list);
//			assertEquals(1, list.size());
			assertEquals(null, transfer.getSrcEquip());
			assertEquals(null, transfer.getTarEquip());
		}
	}
	
	@Test
	public void testComposeItemStone() throws Exception {
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(10000);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone(user);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		Thread.sleep(500);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.composeItem(user, new int[]{21, 22, 23, 24, 25});
		
		Thread.sleep(500);
		
		BseCompose compose = null;
		BseAddProp addProp = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseCompose ) {
				compose = (BseCompose)xinqi.payload;
			} else if ( xinqi.payload instanceof BseAddProp ) {
				addProp = (BseAddProp)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		assertEquals(ComposeStatus.SUCCESS.ordinal(), compose.getResult());
		/**
		 * BseRoleInfo
		 * BseAddProp
		 * BseCompose
		 */
	  System.out.println(list);
		//assertEquals(list.size());
		
		XinqiPropData.PropData newPropData = compose.getNewProp();
		//20007 土神石Lv2
		assertEquals("20007", newPropData.getId());
		
		//Check bag
		User actualUser = UserManager.getInstance().queryUser(userId);
		UserManager.getInstance().queryUserBag(actualUser);
		bag = actualUser.getBag();
		assertTrue(bag.getOtherPropData(20) != null);
		assertEquals(26, newPropData.getPropPew());
	}
	
	@Test
	public void testForgeEquipWithoutMoney() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(10);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		Bag bag = makeBagWithStone2(user, 0, 1, 1);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		list.clear();
		manager.forgeEquip(user, 20, new int[]{21, 22, 23, 24, 25});
		
		Thread.sleep(500);
		
		BseForge forge = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseForge ) {
				forge = (BseForge)xinqi.payload;
			}
		}

		assertEquals(ForgeStatus.NO_MONEY.ordinal(), forge.getResult());
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		assertTrue(list.size()+"", list.size()>=2);
		assertEquals(0, forge.getOtherPewsCount());
	}

	@Test
	public void testTransferItemWithoutMoney() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGolden(5);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		//The src weapon
		WeaponPojo srcPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		srcPojo.setAddAttack(500);
		srcPojo.setAddAgility(600);
		srcPojo.setAddDefend(700);
		srcPojo.setAddLuck(800);
		srcPojo.setLv(4);
		
		WeaponPojo targetPojo = EquipManager.getInstance().getWeaponById("510");
		targetPojo.setAddAttack(100);
		targetPojo.setAddAgility(200);
		targetPojo.setAddDefend(300);
		targetPojo.setAddLuck(400);
		targetPojo.setLv(0);
		
		bag.addOtherPropDatas(srcPojo.toPropData(30000, WeaponColor.BLUE));
		bag.addOtherPropDatas(targetPojo.toPropData(30000, WeaponColor.BLUE));
		
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		
		CraftManager manager = CraftManager.getInstance();
		
		Thread.sleep(300);
		list.clear();
		manager.transferEquip(user, 20, 21);
		
		Thread.sleep(200);
			
		BseTransfer transfer = null;
		for ( Object obj : list ) {
			XinqiMessage xinqi = (XinqiMessage)obj;
			if ( xinqi.payload instanceof BseTransfer ) {
				transfer = (BseTransfer)xinqi.payload;
			}
		}
		//0：无法熔炼；1：熔炼成功；2：熔炼失败 3:操作异常
		assertNotNull(transfer);
		assertEquals(TransferStatus.FAILURE.ordinal(), transfer.getResult());
	}
	
	@Test
	public void testEnhanceMapValueAfterStrengthAndForge() {
		String weaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("9");
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		PropData propData = weapon.toPropData(10, WeaponColor.WHITE);
		
		//Strength to level 2
		EquipCalculator.weaponUpLevel(propData, 2);
		//Forge with four types stone
		/*
				{ GameDataKey.CRAFT_STONE_LUCK, 20001},
				{ GameDataKey.CRAFT_STONE_DEFEND, 20002},
				{ GameDataKey.CRAFT_STONE_AGILITY, 20003},
				{ GameDataKey.CRAFT_STONE_ATTACK, 20004},
		 */
		String[] stones = {"20004", "20004", "20004", "20004"};
		for ( String stoneType : stones ) {
			PropDataSlot slot = new PropDataSlot();
			slot.setSlotType(PropDataEnhanceField.ATTACK);
			EquipCalculator.calculateForgeData(propData, 5, stoneType, slot);
		}
		
		com.xinqihd.sns.gameserver.proto.XinqiPropData.PropData xinqiPropData = 
				propData.toXinqiPropData();
		int level = xinqiPropData.getLevel();
		int attack = xinqiPropData.getAttackLev();
		int defend = xinqiPropData.getDefendLev();
		int agility = xinqiPropData.getAgilityLev();
		int lucky = xinqiPropData.getLuckLev();
		int damage = xinqiPropData.getDamageLev();
		int skin = xinqiPropData.getSkinLev();
		
		assertEquals(2, level);
//		System.out.println("base attack:" + weapon.getAddAttack() + " +"+attack+"="+propData.getAttackLev());
//		System.out.println("base defend:" + weapon.getAddDefend() + " +"+defend+"="+propData.getDefendLev());
//		System.out.println("base agility:" + weapon.getAddAgility() + " +"+agility+"="+propData.getAgilityLev());
//		System.out.println("base lucky:" + weapon.getAddLuck() + " +"+lucky+"="+propData.getLuckLev());
//		System.out.println("base damage:" + weapon.getAddDamage() + " +"+damage+"="+propData.getDamageLev());
//		System.out.println("base skin:" + weapon.getAddSkin() + " +"+skin+"="+propData.getSkinLev());
		
		assertTrue("base attack:" + weapon.getAddAttack() + " +"+attack+"="+propData.getAttackLev(),
				propData.getAttackLev()>weapon.getAddAttack());
		assertTrue("base defend:" + weapon.getAddDefend() + " +"+defend+"="+propData.getDefendLev(),
				propData.getDefendLev()==weapon.getAddDefend());
		assertTrue("base agility:" + weapon.getAddAgility() + " +"+agility+"="+propData.getAgilityLev(),
				propData.getAgilityLev()==weapon.getAddAgility());
		assertTrue("base lucky:" + weapon.getAddLuck() + " +"+lucky+"="+propData.getLuckLev(),
				propData.getLuckLev()==weapon.getAddLuck());
		assertTrue("base damage:" + weapon.getAddDamage() + " +"+damage+"="+propData.getDamageLev(),
				propData.getDamageLev()>weapon.getAddDamage());
		assertTrue("base skin:" + weapon.getAddSkin() + " +"+skin+"="+propData.getSkinLev(),
				propData.getSkinLev() == weapon.getAddSkin());

	}
	
	@Test
	public void testComposeColorWeapon() {
		//508	钻石●大蝎子
		String weaponId = "508";
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		PropData propData = weapon.toPropData(100, WeaponColor.WHITE);
		Bag bag = composeColorWeapon(propData);
		PropData colorWeapon = null;
		boolean success = false;
		for ( PropData p : bag.getOtherPropDatas() ) {
			if ( p == null ) continue;
			System.out.println(p);
			if ( p.getWeaponColor() == WeaponColor.GREEN ) {
				if ( p.getItemId().equals(weaponId) ) {
					success = true;
				}
			}
		}
		if ( success ) {
			assertEquals(1, bag.getCurrentCount());
		} else {
			assertEquals(0, bag.getCurrentCount());
		}
		//assertTrue(success);
	}
	
	@Test
	public void testComposeColorWeaponPink() {
		//508	钻石●大蝎子
		String weaponId = "508";
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
		PropData propData = weapon.toPropData(100, WeaponColor.PINK);
		Bag bag = composeColorWeapon(propData);
		PropData colorWeapon = null;
		boolean success = false;
		for ( PropData p : bag.getOtherPropDatas() ) {
			if ( p == null ) continue;
			System.out.println(p);
			if ( p.getWeaponColor() == WeaponColor.ORGANCE ) {
				if ( p.getItemId().equals(weaponId) ) {
					success = true;
				}
			}
		}
		if ( success ) {
			assertEquals(1, bag.getCurrentCount());
		} else {
			assertEquals(0, bag.getCurrentCount());
		}
		//assertTrue(success);
	}
	
	@Test
	public void testComposeWeaponNormal() {
		//508	钻石●大蝎子
		Bag bag = composeEquip(true, 1);
		boolean success = false;
		PropData propData = null;
		for ( PropData p : bag.getOtherPropDatas() ) {
			if ( p == null ) continue;
			if ( p.isWeapon() ) {
				propData = p;
			}
		}
		assertTrue(propData!=null);
		System.out.println("output: "+propData);
		//assertTrue(success);
	}
	
	@Test
	public void testComposeWeaponAdvance() {
		//508	钻石●大蝎子
		Bag bag = composeEquip(true, 2);
		boolean success = false;
		PropData propData = null;
		for ( PropData p : bag.getOtherPropDatas() ) {
			if ( p == null ) continue;
			System.out.println(propData);
			if ( p.isWeapon() ) {
				propData = p;
			}
		}
		assertTrue(propData!=null);
		WeaponPojo weapon = (WeaponPojo)propData.getPojo();
		assertEquals(2, weapon.getQuality());
		System.out.println("output: "+propData);
		//assertTrue(success);
	}
	
	@Test
	public void printComposeEquipOrWeaponPrice() {
		
	}
	
	/**
	 * 打印合成的数值变化范围
	 */
	@Test
	public void printComposeItemDataRange() {
		//508	钻石●大蝎子
		String weaponId = "508";
		StringBuilder buf = new StringBuilder(2000);
		for ( WeaponColor color : WeaponColor.values() ) {
			for ( int fireStoneLevel=1; fireStoneLevel<=5; fireStoneLevel++ ) {				
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(weaponId);
				PropData weaponPropData = weapon.toPropData(100, color);
				int baseAttack = weapon.getAddAttack();
				int oldAttack = weaponPropData.getAttackLev();
				weaponPropData = forgeWeaponWithFireStone(weaponPropData, fireStoneLevel);
				int newAttack = weaponPropData.getAttackLev();
				int forgeAttack = weaponPropData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceField.ATTACK);
				buf.append("stone:"+fireStoneLevel+"; "+weaponPropData+"; base:"+baseAttack+"; oldAttack:"+oldAttack+"; newAttack:"+newAttack+"; forgeAttack:"+forgeAttack).append("\n");
			}
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * 打印各等级强化石强化武器的价格和成功率
	 * @throws Exception
	 */
	@Test
	public void printForgePrice() throws Exception {
		String userName = "test-001";
		UserId userId = new UserId(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(Integer.MAX_VALUE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		UserManager.getInstance().removeUser(userId);
		
		user.set_id(userId);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		Bag bag = user.getBag();
		/**
			20021	强化石Lv1
			20022	强化石Lv2
			20023	强化石Lv3
			20024	强化石Lv4
			20025	强化石Lv5
		 */
		//String[] stoneIds = new String[]{"20021", "20022", "20023", "20024", "20025"};
		String[] stoneIds = new String[]{"20025"};
		ItemPojo item = ItemManager.getInstance().getItemById("20025");
		PropData stone = item.toPropData();
		bag.setOtherPropDataAtPew(stone, 21);
		CraftManager manager = CraftManager.getInstance();
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		StringBuilder buf = new StringBuilder();
		for ( WeaponPojo weapon : weapons ) {
			if ( weapon.getSlot() != EquipType.WEAPON ) continue;
			for ( int level=0; level<12; level++ ) {
				for ( String stoneId : stoneIds ) {
					weapon.setLv(level);
					bag.setOtherPropDataAtPew(weapon.toPropData(30, WeaponColor.WHITE), 20);
					ArrayList result = manager.forgeEquipPriceAndRatio(user, 20, new int[]{21});
					int price = (Integer)result.get(0);
					double ratio = (Double)result.get(1);
					ItemPojo it = ItemManager.getInstance().getItemById(stoneId);
					buf.append(it.getName()+"\t"+weapon.getId()+"\t"+weapon.getName()+"\t"+weapon.getLv()+"\t"+price+"\t"+ratio+"\n");
				}
			}
		}
		System.out.println(buf.toString());
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private PropData forgeWeaponWithFireStone(PropData weaponPropData, int fireStoneLevel) {
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(Integer.MAX_VALUE);
		UserManager.getInstance().removeUser(userName);
		
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		//add weapon
		bag.addOtherPropDatas(weaponPropData);
		/**
			20016	火神石Lv1
			20017	火神石Lv2
			20018	火神石Lv3
			20019	火神石Lv4
			20020	火神石Lv5
		 */
		int stoneLevel = 20015 + fireStoneLevel;
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(String.valueOf(stoneLevel));
		PropData stone = itemPojo.toPropData();
		bag.addOtherPropDatas(stone);
		
		int weaponPew = weaponPropData.getPew();
		CraftManager.getInstance().forgeEquip(user, weaponPropData.getPew(), new int[]{stone.getPew()});
		weaponPropData = bag.getOtherPropData(weaponPew);
		
		return weaponPropData;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag composeEquip(boolean weapon, int quality) {
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(Integer.MAX_VALUE);
		UserManager.getInstance().removeUser(userName);
		
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		/**
		 * 
			26008	武器熔炼符
			26009	装备熔炼符
			26010	精良武器熔炼符
			26011	精良装备熔炼符
		 *
		 */
		String itemId = "26008";
		if ( weapon && quality == 1 ) {
			itemId = "26008";
		} else if ( weapon && quality == 2 ) {
			itemId = "26010";
		} else if ( !weapon && quality == 1 ) {
			itemId = "26009";
		} else if ( !weapon && quality == 2 ) {
			itemId = "26011";
		}
		
		ItemPojo colorFuncItem = ItemManager.getInstance().getItemById(itemId);
		PropData stone = colorFuncItem.toPropData();
		bag.addOtherPropDatas(stone);
		
		//add weapon
		for ( int i=0; i<4; i++ ) {
			WeaponPojo weaponPojo = EquipManager.getInstance().getRandomWeapon(user, EquipType.DECORATION, 1);
			System.out.println("input: " + weaponPojo.getName());
			bag.addOtherPropDatas(weaponPojo.toPropData(10, WeaponColor.WHITE));
		}
		
		CraftManager.getInstance().composeItem(
				user, new int[]{20, 21, 22, 23, 24});
		
		return bag;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag composeColorWeapon(PropData weaponPropData) {
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(Integer.MAX_VALUE);
		UserManager.getInstance().removeUser(userName);
		
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		/**
			26004	绿色熔炼符
			26005	蓝色熔炼符
			26006	粉色熔炼符
			26007	橙色熔炼符
		 */
		String colorItemId = "26004";
		switch ( weaponPropData.getWeaponColor() ) {
			case WHITE:
				colorItemId = "26004";
				break;
			case GREEN:
				colorItemId = "26005";
				break;
			case BLUE:
				colorItemId = "26006";
				break;
			case PINK:
				colorItemId = "26007";
				break;
		}
		ItemPojo colorFuncItem = ItemManager.getInstance().getItemById(colorItemId);
		PropData stone = colorFuncItem.toPropData();
		bag.addOtherPropDatas(stone);
		
		//add weapon
		bag.addOtherPropDatas(weaponPropData.clone());
		bag.addOtherPropDatas(weaponPropData.clone());
		bag.addOtherPropDatas(weaponPropData.clone());
		bag.addOtherPropDatas(weaponPropData.clone());
		
		int weaponPew = weaponPropData.getPew();
		
		CraftManager.getInstance().composeItem(
				user, new int[]{20, 21, 22, 23, 24});
		
		weaponPropData = bag.getOtherPropData(weaponPew);
		
		return bag;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBagWithStone(User user) {
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		//id="20006" typeid="20002" lv="1" icon="BlackStoneLv1" name="土神石Lv1" 
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20002_1");
		ItemPojo itemPojo = ItemManager.getInstance().getItemById("20006");
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		//id="21002" typeid="21001" lv="0" icon="Prop0013" name="土神石炼化符"
		ItemPojo funcPojo = ItemManager.getInstance().getItemById("21002");
		bag.addOtherPropDatas(funcPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		return bag;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBagWithDefendStoneLevel3(User user) {
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		//id="20006" typeid="20002" lv="1" icon="BlackStoneLv1" name="土神石Lv1" 
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20002_1");
		ItemPojo itemPojo = ItemManager.getInstance().getItemById("20008");
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		//id="21002" typeid="21001" lv="0" icon="Prop0013" name="土神石炼化符"
		ItemPojo funcPojo = ItemManager.getInstance().getItemById("21002");
		bag.addOtherPropDatas(funcPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		return bag;
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBagWithStone2(User user, int weaponLevel, int level, int luckyStone) {
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		//The weapon
		WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		weaponPojo.setLv(weaponLevel);
		bag.addOtherPropDatas(weaponPojo.toPropData(30000, WeaponColor.BLUE));
		
		//Strength stones - up to 3
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20005_"+level);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById((20020+level)+"");
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());

		//God stone 
		ItemPojo godPojo = ItemManager.getInstance().getItemById("24001");
		bag.addOtherPropDatas(godPojo.toPropData());
		
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
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBagWithStone3(User user, int weaponLevel, int level, int luckyStone) {
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);

		//Strength stones - up to 3
//		ItemPojo itemPojo = ItemManager.getInstance().getItemById("item_20005_"+level);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById((20020+level)+"");
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());
		bag.addOtherPropDatas(itemPojo.toPropData());

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
	
	private Bag makeBagWithStone3(User user, int weaponLevel, int count, 
			int stoneLevel, int luckyStone) {
		
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
