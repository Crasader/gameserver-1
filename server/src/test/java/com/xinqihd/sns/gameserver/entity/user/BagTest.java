package com.xinqihd.sns.gameserver.entity.user;

import static com.xinqihd.sns.gameserver.entity.user.Bag.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BagTest {
	
	private int BAG_MAX_COUNT = 20; 

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSetOtherPropDataAtPew() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 5;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		for ( int i=count-1; i>=0; i--) {
			bag.setOtherPropDataAtPew(makePropData(10000+i), BAG_WEAR_COUNT+i);
		}
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertTrue(bag.clearGeneralChangeFlag());
		assertEquals(5, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			assertNotNull(propData);
			assertTrue(propData.getPew() >= Bag.BAG_WEAR_COUNT);
		}
	}
	
	@Test
	public void testSetOtherPropDataAtPewNull() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 5;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		for ( int i=count-1; i>=0; i--) {
			bag.setOtherPropDataAtPew(null, BAG_WEAR_COUNT+i);
		}
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertTrue(bag.clearGeneralChangeFlag());
		assertEquals(5, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			assertNull(propData);
		}
	}
	
	@Test
	public void testSetOtherPropDataAtZero() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		boolean result = bag.setOtherPropDataAtPew(makePropData(10000), BAG_WEAR_COUNT);
		assertEquals(true, result);
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(1, propDataList.size());
		assertTrue(bag.clearGeneralChangeFlag());
		assertEquals(1, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			assertNotNull(propData);
			assertTrue(propData.getPew() >= Bag.BAG_WEAR_COUNT);
		}
	}
	
	@Test
	public void testSetOtherPropDataAtPewExceedCount() {
		User user = UserManager.getInstance().createDefaultUser();
		Bag bag = new Bag();
		boolean result = bag.setOtherPropDataAtPew(makePropData(10000), 
				BAG_WEAR_COUNT+bag.getMaxCount());
		assertTrue(!result);
	}

	@Test
	public void testAddOtherPropDatas() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 5;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertTrue(bag.clearGeneralChangeFlag());
		assertEquals(5, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			assertEquals(BAG_WEAR_COUNT+i, propData.getPew());
		}
	}
	
	@Test
	public void testBagCount() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 5;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		assertEquals(60, bag.getMaxCount());
		
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertTrue(bag.clearGeneralChangeFlag());
		assertEquals(5, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		assertEquals(60, bag.getMaxCount());
		assertEquals(count, bag.getCurrentCount());
		
		//Remove a propData from bag
		bag.wearPropData(20, PropDataEquipIndex.WEAPON.index());
		assertEquals(count-1, bag.getCurrentCount());
	}
	
	@Test
	public void testBagCountStoreAndRetrieve() {
		String userName = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.setRoleName(userName);
		user.setUsername(userName);
		user.set_id(new UserId(userName));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		UserManager.getInstance().saveUserBag(user, true);
		Bag actualBag = UserManager.getInstance().queryUserBag(user);
		assertEquals(60, actualBag.getMaxCount());
		assertEquals(1, actualBag.getCurrentCount());
		
		int count = 5;
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		assertEquals(60, bag.getMaxCount());
		
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		
		//Save user's bag
		UserManager.getInstance().saveUserBag(user, false);
		
		actualBag = UserManager.getInstance().queryUserBag(user);
		assertEquals(60, actualBag.getMaxCount());
		assertEquals(6, actualBag.getCurrentCount());
		
		List<PropData> propDataList = actualBag.getOtherPropDatas();
		assertEquals(count+1, propDataList.size());

		
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData propData = propDataList.get(i);
			assertEquals(BAG_WEAR_COUNT+i, propData.getPew());
		}
	}
	
	@Test
	public void testAddOtherPropDataTooManyItems() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 60;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		assertTrue(bag.clearGeneralChangeFlag());
		assertArrayEquals(new int[0], changeFlagToIntArray(bag.clearMarkedChangeFlag()));
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		boolean success = bag.addOtherPropDatas(makePropData(10000+71));
		assertTrue(!success);
	}
	
	@Test
	public void testAddOtherPropDatas2() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		PropData propData = makePropData(10000);
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(propData);
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(3, propDataList.size());
		assertEquals(1, propData.getCount());
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(3, flags.length);
		Arrays.sort(flags);
		assertEquals(BAG_WEAR_COUNT, flags[0]);
		assertEquals(BAG_WEAR_COUNT+1, flags[1]);
		assertEquals(BAG_WEAR_COUNT+2, flags[2]);
		
		assertEquals(0, changeFlagToIntArray(bag.clearMarkedChangeFlag()).length);
		
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData pd = propDataList.get(i);
			assertTrue( pd.getPew() == BAG_WEAR_COUNT || pd.getPew() == BAG_WEAR_COUNT + 1 ||
					pd.getPew() == BAG_WEAR_COUNT + 2);
		}
	}
	
	@Test
	public void testAddOtherPropDatas3() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		PropData propData = makePropData(10000);
		bag.addOtherPropDatas(makePropData(20000));
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(makePropData(20001));
		bag.addOtherPropDatas(propData);
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(4, propDataList.size());
		assertEquals(1, propData.getCount());
		assertEquals(BAG_WEAR_COUNT+1, getOtherPropDataIndex(propData, propDataList));
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(4, flags.length);
		Arrays.sort(flags);
		int[] expectFlags = new int[]{BAG_WEAR_COUNT, BAG_WEAR_COUNT+1, BAG_WEAR_COUNT+2, BAG_WEAR_COUNT+3};
		assertArrayEquals(expectFlags, flags);
		
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData pd = propDataList.get(i);
			assertTrue("pd.pew: " + pd.getPew(), 
					pd.getPew() >= BAG_WEAR_COUNT && pd.getPew() < BAG_WEAR_COUNT + 4);
		}
	}
	
	@Test
	public void testAddOtherPropDatasEmptyIndex() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		bag.addOtherPropDatas(makePropData(20000));
		bag.addOtherPropDatas(makePropData(20001));
		bag.addOtherPropDatas(makePropData(20002));
		//Remove the middle one.
		bag.removeOtherPropDatas(Bag.BAG_WEAR_COUNT+1);
		//Add a new one
		PropData propData = makePropData(20003);
		
		bag.clearGeneralChangeFlag();
		bag.clearMarkedChangeFlag();
		
		//Add a new one into an old empty position
		bag.addOtherPropDatas(propData);
		
		assertEquals(Bag.BAG_WEAR_COUNT+1, propData.getPew());
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(3, propDataList.size());
		assertEquals(propData, propDataList.get(1));
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(1, flags.length);
		int[] expectFlags = new int[]{BAG_WEAR_COUNT+1};
		assertArrayEquals(expectFlags, flags);
		
	}

	@Test
	public void testRemoveOtherPropDatas() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 3;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		PropData[] propDatas = bag.getOtherPropDatas().toArray(new PropData[0]);
		
		//Remove a propData
		bag.removeOtherPropDatas(BAG_WEAR_COUNT + 0);
		assertEquals(-1, propDatas[0].getPew());
		List<PropData> propDataList = bag.getOtherPropDatas();
		//The total length is unchanged.
		assertEquals(count, propDataList.size());
		assertEquals(null, bag.getOtherPropData(BAG_WEAR_COUNT));
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData pd = propDataList.get(i);
			if ( pd != null ) {
				assertEquals(BAG_WEAR_COUNT+i, pd.getPew());
			}
		}
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(3, flags.length);
		Arrays.sort(flags);
		int[] expectFlags = new int[]{BAG_WEAR_COUNT, BAG_WEAR_COUNT+1, BAG_WEAR_COUNT+2};
		assertArrayEquals(expectFlags, flags);
		
		bag.removeOtherPropDatas(BAG_WEAR_COUNT + 1);
		propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertEquals(null, bag.getOtherPropData(BAG_WEAR_COUNT+1));
		
		bag.removeOtherPropDatas(BAG_WEAR_COUNT + 2);
		propDataList = bag.getOtherPropDatas();
		assertEquals(count, propDataList.size());
		assertEquals(null, bag.getOtherPropData(BAG_WEAR_COUNT+2));
		
		//Delete an non-exist element
		bag.removeOtherPropDatas(999);
		propDataList = bag.getOtherPropDatas();
		assertEquals(3, propDataList.size());
		
		assertTrue(bag.clearGeneralChangeFlag());
		flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(2, flags.length);
		Arrays.sort(flags);
		expectFlags = new int[]{BAG_WEAR_COUNT+1, BAG_WEAR_COUNT+2};
		assertArrayEquals(expectFlags, flags);
	}
	
	@Test
	public void testRemoveOtherPropDatas2() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		PropData propData = makePropData(10000);
		bag.addOtherPropDatas(makePropData(9999));
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(propData);
		Collection<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(4, propDataList.size());
		
		bag.removeOtherPropDatas(BAG_WEAR_COUNT+3);
		assertEquals(-1, propData.getPew());
		propDataList = bag.getOtherPropDatas();
		assertEquals(4, propDataList.size());
		assertEquals(0, propData.getCount());
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(4, flags.length);
		Arrays.sort(flags);
		int[] expectFlags = new int[]{BAG_WEAR_COUNT, BAG_WEAR_COUNT+1,
				BAG_WEAR_COUNT+2, BAG_WEAR_COUNT+3};
		assertArrayEquals(expectFlags, flags);
	}
	
	@Test
	public void testRemoveOtherPropDatas3() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		PropData propData = makePropData(10000);
		bag.addOtherPropDatas(makePropData(9999));
		bag.addOtherPropDatas(propData);
		
		Collection<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(2, propDataList.size());
		
		bag.removeOtherPropDatas(BAG_WEAR_COUNT+1);
		
		assertEquals(-1, propData.getPew());
		propDataList = bag.getOtherPropDatas();
		assertEquals(2, propDataList.size());
		assertEquals(0, propData.getCount());
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(2, flags.length);
		Arrays.sort(flags);
		int[] expectFlags = new int[]{BAG_WEAR_COUNT, BAG_WEAR_COUNT+1};
		assertArrayEquals(expectFlags, flags);
	}
	
	@Test
	public void testRemoveWearPropDatas1() {
		User user = UserManager.getInstance().createDefaultUser();
		
		PropDataEquipIndex[] indexes = PropDataEquipIndex.values();
		
		int count = BAG_MAX_COUNT;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		for ( int i=0; i<indexes.length; i++ ) {
			assertTrue(bag.wearPropData(BAG_WEAR_COUNT+i, indexes[i].index()));
		}
		bag.clearGeneralChangeFlag();
		changeFlagToIntArray(bag.clearMarkedChangeFlag());
		
		bag.removeWearPropDatas(PropDataEquipIndex.WEAPON);
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(1, flags.length);
		assertEquals(PropDataEquipIndex.WEAPON.index(), flags[0]);
		
		int deleteIndex = BAG_WEAR_COUNT + count/2;
		//Delete an non-exist item.
		bag.removeOtherPropDatas(deleteIndex);
		assertTrue(!bag.clearGeneralChangeFlag());
		flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(0, flags.length);
	}
	
	@Test
	public void testGetNotExistIndex() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int count = 3;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		PropData[] propDatas = bag.getOtherPropDatas().toArray(new PropData[0]);
		assertEquals(null, bag.getOtherPropData(999));
	}

	@Test
	public void testWearPropData() {
		User user = UserManager.getInstance().createDefaultUser();
		
		PropData propData = makePropData(10000);
		PropDataEquipIndex[] indexes = PropDataEquipIndex.values();
		
		int count = BAG_MAX_COUNT;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		for ( int i=0; i<indexes.length; i++ ) {
			assertTrue(bag.wearPropData(BAG_WEAR_COUNT+i, indexes[i].index()));
		}
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(BAG_MAX_COUNT+BAG_WEAR_COUNT, flags.length);
		Arrays.sort(flags);
		int[] expectFlags = new int[BAG_MAX_COUNT+BAG_WEAR_COUNT];
		for ( int i=0; i<BAG_WEAR_COUNT; i++ ) {
			expectFlags[i] = i;
		}
		for ( int i=BAG_WEAR_COUNT; i<BAG_WEAR_COUNT+BAG_MAX_COUNT; i++ ) {
			expectFlags[i] = BAG_WEAR_COUNT + i - BAG_WEAR_COUNT;
		}
		Arrays.sort(expectFlags);
		assertArrayEquals(expectFlags, flags);
		
		List<PropData> wearDataList = bag.getWearPropDatas();
		for ( int i=0; i<wearDataList.size(); i++ ) {
			PropData pd = wearDataList.get(i);
			assertEquals(i, pd.getPew());
		}
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		for ( int i=0; i<propDataList.size(); i++ ) {
			PropData pd = propDataList.get(i);
			if ( pd != null ) {
				assertEquals(BAG_WEAR_COUNT+i, pd.getPew());
			}
		}
	}

	@Test
	public void testWearPropData2() {
		User user = UserManager.getInstance().createDefaultUser();
		
		PropData propData = makePropData(10000);
		PropDataEquipIndex[] indexes = PropDataEquipIndex.values();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<indexes.length; i++ ) {
			assertTrue(!bag.wearPropData(BAG_WEAR_COUNT+i, indexes[i].index()));
		}
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(0, flags.length);
	}
	
	@Test
	public void testWearExpiredPropData() {
		User user = UserManager.getInstance().createDefaultUser();
		
		int oldPower = user.getPower();
		
		Bag bag = user.getBag();
		PropData weapon = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		weapon.setExpire(true);
		
		boolean success = bag.wearPropData(PropDataEquipIndex.WEAPON.index(), Bag.BAG_WEAR_COUNT);
		assertEquals(true, success);
		int power = user.getPower();
		assertEquals(oldPower, power);
		
		success = bag.wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		assertEquals(false, success);
		power = user.getPower();
		assertEquals(oldPower, power);
	}
	
	@Test
	public void testUnWearPropDataNull() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		bag.wearPropData(PropDataEquipIndex.BUBBLE.index(), 23);
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(0, flags.length);
	}
	
	@Test
	public void testUnWearPropData() {
		User user = UserManager.getInstance().createDefaultUser();

		PropData propData = makePropData(10000);
		
		int count = 10;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		
		//Wear three propData
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+0, PropDataEquipIndex.CLOTH.index()));
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+1, PropDataEquipIndex.WING.index()));
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+2, PropDataEquipIndex.WEAPON.index()));
		
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(10+3, flags.length);
				
		List<PropData> wearDataList = bag.getWearPropDatas();
		PropData pd = wearDataList.get(PropDataEquipIndex.CLOTH.index());
		assertEquals(PropDataEquipIndex.CLOTH.index(), pd.getPew());
		pd = wearDataList.get(PropDataEquipIndex.WING.index());
		assertEquals(PropDataEquipIndex.WING.index(), pd.getPew());
		pd = wearDataList.get(PropDataEquipIndex.WEAPON.index());
		assertEquals(PropDataEquipIndex.WEAPON.index(), pd.getPew());
		
		//Unwear a propData
		int otherPropSize = bag.getOtherPropDatas().size();
		PropData expected = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertTrue(bag.wearPropData(PropDataEquipIndex.WEAPON.index(), -1));
		assertNull(bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index()));
		assertEquals(otherPropSize, bag.getOtherPropDatas().size());
		PropData actual = bag.getOtherPropDatas().get(0);
		assertEquals(expected, actual);
		assertEquals(BAG_WEAR_COUNT, actual.getPew());
		
		flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		Arrays.sort(flags);
//		System.out.println(Arrays.toString(flags));
		assertEquals(PropDataEquipIndex.WEAPON.index(), flags[0]);
		assertEquals(BAG_WEAR_COUNT, flags[1]);
	}
	
	@Test
	public void testUnWearPropDataFull() {
		User user = UserManager.getInstance().createDefaultUser();

		PropData propData = makePropData(10000);
		
		int count = 70;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		bag.setWearPropData(propData.clone(), 17);
		
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		
		//unwear to bag
		assertFalse(bag.wearPropData(PropDataEquipIndex.WEAPON.index(), -1));
		
		List<PropData> wearDataList = bag.getWearPropDatas();
		PropData pd = wearDataList.get(PropDataEquipIndex.WEAPON.index());
		assertEquals(PropDataEquipIndex.WEAPON.index(), pd.getPew());

	}
	
	@Test
	public void testMovePropDataFromBagToBody() {
		String userName = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevelSimple(12);
		String newWeaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("1");
		PropData newPropData = EquipManager.getInstance().
				getWeaponById(newWeaponId).toPropData(1, WeaponColor.WHITE);
		
		Bag bag = user.getBag();
		bag.removeOtherPropDatas(BAG_WEAR_COUNT);
		bag.addOtherPropDatas(newPropData);
		
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		int oldAgility = user.getAgility();
		int oldLucky = user.getLuck();
		int oldPower = user.getPower();
		
		int currentCount = bag.getCurrentCount();
		bag.movePropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		int newCount = bag.getCurrentCount();
		assertEquals(currentCount, newCount);
		
		int newAttack = user.getAttack();
		int newDefend = user.getDefend();
		int newAgility = user.getAgility();
		int newLucky = user.getLuck();
		int newPower = user.getPower();
		
		assertTrue(newAttack+">"+oldAttack, newAttack>oldAttack);
		assertTrue(newDefend+">"+oldDefend, newDefend>oldDefend);
		assertTrue(newAgility+">"+oldAgility, newAgility>oldAgility);
		assertTrue(newLucky+">"+oldLucky, newLucky>oldLucky);
		//assertTrue(newPower+">"+oldPower, newPower>oldPower);
		
		assertEquals("Bag have the old weapon", 1, bag.getCurrentCount());
		PropData oldPropData = bag.getOtherPropData(Bag.BAG_WEAR_COUNT);
		assertEquals(UserManager.basicWeaponItemId, oldPropData.getItemId());
		
		assertEquals(Bag.BAG_WEAR_COUNT, oldPropData.getPew());
		assertEquals(PropDataEquipIndex.WEAPON.index(), newPropData.getPew());
		assertEquals(1, newPropData.getCount());
		assertEquals(1, oldPropData.getCount());
		
		//Change again
		bag.movePropData(PropDataEquipIndex.WEAPON.index(), Bag.BAG_WEAR_COUNT);
		newCount = bag.getCurrentCount();
		assertEquals(currentCount, newCount);
		
		newPropData = bag.getOtherPropData(Bag.BAG_WEAR_COUNT);
		oldPropData = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertNotNull(null, oldPropData);
		assertEquals(Bag.BAG_WEAR_COUNT, newPropData.getPew());
		
		int newAttack2 = user.getAttack();
		int newDefend2 = user.getDefend();
		int newAgility2 = user.getAgility();
		int newLucky2 = user.getLuck();
		int newPower2 = user.getPower();
		
		assertTrue(newAttack2>oldAttack && newAttack2<newAttack);
		assertTrue(newDefend2>oldDefend && newDefend2<newDefend);
		assertTrue(newAgility2>oldAgility && newAgility2<newAgility);
		assertTrue(newLucky2>oldLucky && newLucky2<newLucky);
		//assertTrue(oldPower+">"+newPower, oldPower>newPower);
	}
	
	@Test
	public void testMovePropDataFromBagToBodyWithFullBag() {
		String userName = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevelSimple(12);
		String newWeaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("1");
		PropData newPropData = EquipManager.getInstance().
				getWeaponById(newWeaponId).toPropData(1, WeaponColor.WHITE);
		
		Bag bag = user.getBag();
		bag.removeOtherPropDatas(BAG_WEAR_COUNT);
		bag.addOtherPropDatas(newPropData);
		/**
		 * Make the bag full
		 */
		PropData giftPropData = ItemManager.getInstance().getItemById(
				UserManager.getInstance().basicUserGiftBoxId).toPropData();
		for ( int i=1; i<bag.getMaxCount(); i++ ) {
			bag.addOtherPropDatas(giftPropData.clone());
		}
		assertEquals(bag.getMaxCount(), bag.getCurrentCount());
		
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		int oldAgility = user.getAgility();
		int oldLucky = user.getLuck();
		int oldPower = user.getPower();
		
		bag.movePropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		
		int newAttack = user.getAttack();
		int newDefend = user.getDefend();
		int newAgility = user.getAgility();
		int newLucky = user.getLuck();
		int newPower = user.getPower();
		
		assertTrue(newAttack+">"+oldAttack, newAttack>oldAttack);
		assertTrue(newDefend+">"+oldDefend, newDefend>oldDefend);
		assertTrue(newAgility+">"+oldAgility, newAgility>oldAgility);
		assertTrue(newLucky+">"+oldLucky, newLucky>oldLucky);
		assertTrue(newPower+">"+oldPower, newPower>oldPower);
		
		PropData oldPropData = bag.getOtherPropData(Bag.BAG_WEAR_COUNT);
		assertEquals(UserManager.basicWeaponItemId, oldPropData.getItemId());
		
		assertEquals(Bag.BAG_WEAR_COUNT, oldPropData.getPew());
		assertEquals(PropDataEquipIndex.WEAPON.index(), newPropData.getPew());
		
		for ( int i=1; i<bag.getMaxCount(); i++ ) {
			assertEquals(giftPropData.getItemId(), bag.getOtherPropDatas().get(i).getItemId());
		}
	}
	
	@Test
	public void testMovePropDataFromBodyToBagNormal() {
		String userName = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = user.getBag();
		//Remove the gift
		bag.removeOtherPropDatas(BAG_WEAR_COUNT);
		
		assertEquals(0, bag.getCurrentCount());
		
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		int oldAgility = user.getAgility();
		int oldLucky = user.getLuck();
		int oldPower = user.getPower();
		
		//Unwear the weapon
		boolean success = bag.movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		assertEquals(true, success);
		assertEquals(1, bag.getCurrentCount());
		
		int newAttack = user.getAttack();
		int newDefend = user.getDefend();
		int newAgility = user.getAgility();
		int newLucky = user.getLuck();
		int newPower = user.getPower();
		
		assertTrue(newAttack+"<"+oldAttack, newAttack<oldAttack);
		assertTrue(newDefend+"<"+oldDefend, newDefend<oldDefend);
		assertTrue(newAgility+"<"+oldAgility, newAgility<oldAgility);
		assertTrue(newLucky+"<"+oldLucky, newLucky<oldLucky);
		assertTrue(newPower+"<"+oldPower, newPower<oldPower);
		
		PropData oldPropData = bag.getOtherPropData(Bag.BAG_WEAR_COUNT);
		assertEquals(UserManager.basicWeaponItemId, oldPropData.getItemId());
		
		assertEquals(Bag.BAG_WEAR_COUNT, oldPropData.getPew());
		assertEquals(null, bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index()));
		
	}
	
	@Test
	public void testMovePropDataFromBodyToBagWithFull() {
		String userName = "test-001";
		User user = UserManager.getInstance().createDefaultUser();
		String newWeaponId = UserManager.basicWeaponItemId.substring(0, 2).concat("1");
		PropData newPropData = EquipManager.getInstance().
				getWeaponById(newWeaponId).toPropData(1, WeaponColor.WHITE);
		
		Bag bag = user.getBag();
		//Remove the gift
		bag.removeOtherPropDatas(BAG_WEAR_COUNT);
		
		/**
		 * Make the bag full
		 */
		PropData giftPropData = ItemManager.getInstance().getItemById(
				UserManager.getInstance().basicUserGiftBoxId).toPropData();
		for ( int i=0; i<bag.getMaxCount(); i++ ) {
			bag.addOtherPropDatas(giftPropData.clone());
		}
		assertEquals(bag.getMaxCount(), bag.getCurrentCount());
		
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		int oldAgility = user.getAgility();
		int oldLucky = user.getLuck();
		int oldPower = user.getPower();
		
		//Unwear the weapon
		boolean success = bag.movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		assertEquals(false, success);
		
		int newAttack = user.getAttack();
		int newDefend = user.getDefend();
		int newAgility = user.getAgility();
		int newLucky = user.getLuck();
		int newPower = user.getPower();
		
		assertTrue(newAttack+"=="+oldAttack, newAttack==oldAttack);
		assertTrue(newDefend+"=="+oldDefend, newDefend==oldDefend);
		assertTrue(newAgility+"=="+oldAgility, newAgility==oldAgility);
		assertTrue(newLucky+"=="+oldLucky, newLucky==oldLucky);
		assertTrue(newPower+"=="+oldPower, newPower==oldPower);
		
		assertEquals(UserManager.basicWeaponItemId, 
				bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index()).getItemId());
		
		for ( int i=0; i<bag.getMaxCount(); i++ ) {
			assertEquals(giftPropData.getItemId(), bag.getOtherPropDatas().get(i).getItemId());
		}
	}
	
	//Test movePropData from null to slot Weapon not null
	//Test movePropData from slot Weapon not null to null
	
	@Test
	public void testWearAndUnWearToUserPower() {
		String userName = "test-001";
		User user = prepareUser(userName);

		PropData propData = makePropData(10000);
		
		int count = 10;
		Bag bag = user.getBag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		for ( int i=0; i<count; i++) {
			PropData pd = makePropData(10000+i);
			pd.setPower(1000);
			bag.addOtherPropDatas(pd);
		}
		//Unwear the basic weapon
		boolean result = bag.wearPropData(PropDataEquipIndex.WEAPON.index(), -1);
		assertTrue(result);

		int power = user.getPower();
		int oldPower = power;
		//assertEquals(""+power, 72, power);
		
		//Wear three propData
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+1, PropDataEquipIndex.WING.index()));
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+2, PropDataEquipIndex.WEAPON.index()));
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT+3, PropDataEquipIndex.CLOTH.index()));
		
		power = user.getPower();
		//The new power value
		assertTrue(power+">"+oldPower, power>oldPower);
		
		//Unwear the propData
		assertTrue(bag.wearPropData(PropDataEquipIndex.WING.index(), BAG_WEAR_COUNT+1));
		assertTrue(bag.wearPropData(PropDataEquipIndex.WEAPON.index(), BAG_WEAR_COUNT+2));
		assertTrue(bag.wearPropData(PropDataEquipIndex.CLOTH.index(), BAG_WEAR_COUNT+3));
		
		//Recover the old power
		power = user.getPower();
		assertEquals(oldPower, power);
		assertTrue(user.getAttack()>0);
		assertTrue(user.getDefend()==0);
		assertTrue(user.getAgility()==0);
		assertTrue(user.getLuck()==0);
	}
	
	@Test
	public void testSetChangeFlag() {
		User user = UserManager.getInstance().createDefaultUser();
		
		PropDataEquipIndex[] indexes = PropDataEquipIndex.values();
		
		int count = BAG_MAX_COUNT;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<11; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		for ( int i=0; i<11; i++ ) {
			assertTrue(bag.wearPropData(BAG_WEAR_COUNT+i, indexes[i].index()));
		}
		bag.clearGeneralChangeFlag();
		changeFlagToIntArray(bag.clearMarkedChangeFlag());
		
		List<PropData> wearDataList = bag.getWearPropDatas();
		List<PropData> propDataList = bag.getOtherPropDatas();
		
		//case 1
		PropData pd = wearDataList.get(10);
		assertEquals(10, pd.getPew());
		pd.setAgilityLev(2);
		pd.setAttackLev(2);
		pd.setDefendLev(2);
		pd.setLuckLev(2);
		pd.setLevel(2);
		
		bag.setChangeFlagOnItem(pd);
		pd = wearDataList.get(10);
		assertEquals(2, pd.getLevel());
		assertTrue(bag.clearGeneralChangeFlag());
		int[] flags = changeFlagToIntArray(bag.clearMarkedChangeFlag());
		assertEquals(1, flags.length);
		assertEquals(10, flags[0]);
	}
	
	@Test
	public void testTidyUserBag() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		PropData propData1 = makePropData(UserManager.basicUserGiftBoxId);
		PropData propData2 = makePropData(10001);
		PropData propData3 = makePropData(10002);
		PropData propData4 = makePropData(UserManager.basicUserGiftBoxId);
		propData1.setWeapon(false);
		propData2.setWeapon(true);
		propData3.setWeapon(true);
		propData4.setWeapon(false);
		
		bag.addOtherPropDatas(propData1);
		bag.addOtherPropDatas(propData2);
		bag.addOtherPropDatas(propData3);
		bag.addOtherPropDatas(propData4);
		
		assertEquals(4, bag.getCurrentCount());
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		
		assertEquals(4, propDataList.size());
		assertEquals(1, propDataList.get(0).getCount());
		assertEquals(1, propDataList.get(3).getCount());
		
		bag.tidyUserBag();
		
		propDataList = bag.getOtherPropDatas();
		
		assertEquals(3, propDataList.size());
		assertEquals(2, propDataList.get(2).getCount());
		assertEquals(BAG_WEAR_COUNT+0, propDataList.get(0).getPew());
		assertEquals(BAG_WEAR_COUNT+1, propDataList.get(1).getPew());
		assertEquals(BAG_WEAR_COUNT+2, propDataList.get(2).getPew());
		
		assertEquals(propDataList.size(), bag.getCurrentCount());
	}
	
	@Test
	public void testTidyUserBagWithNull() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		PropData propData1 = makePropData(20001);
		PropData propData2 = makePropData(20002);
		PropData propData3 = null;
		PropData propData4 = makePropData(20001);
		
		bag.addOtherPropDatas(propData1);
		bag.addOtherPropDatas(propData2);
		bag.addOtherPropDatas(propData3);
		bag.addOtherPropDatas(propData4);
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		
		assertEquals(3, propDataList.size());
		assertEquals(1, propDataList.get(0).getCount());
		assertEquals(1, propDataList.get(2).getCount());
		assertEquals(3, bag.getCurrentCount());
		
		bag.tidyUserBag();
		
		propDataList = bag.getOtherPropDatas();
		
		assertEquals(2, propDataList.size());
		assertEquals(2, propDataList.get(0).getCount());
		assertEquals(BAG_WEAR_COUNT+0, propDataList.get(0).getPew());
		assertEquals(BAG_WEAR_COUNT+1, propDataList.get(1).getPew());
		
		assertEquals(propDataList.size(), bag.getCurrentCount());
	}
	
	@Test
	public void testTidyUserBagWithCount() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		PropData propData1 = makePropData(20001);
		propData1.setCount(15);
		PropData propData2 = makePropData(20002);
		PropData propData3 = makePropData(20003);
		PropData propData4 = makePropData(20001);
		propData4.setCount(5);
		PropData propData5 = makePropData(20001);
		propData5.setCount(6);
		
		bag.addOtherPropDatas(propData1);
		bag.addOtherPropDatas(propData2);
		bag.addOtherPropDatas(propData3);
		bag.addOtherPropDatas(propData4);
		bag.addOtherPropDatas(propData5);
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		assertEquals(5, bag.getCurrentCount());
		assertEquals(5, propDataList.size());
	
		assertEquals(15, propDataList.get(0).getCount());
		assertEquals(5, propDataList.get(3).getCount());
		assertEquals(6, propDataList.get(4).getCount());
		
		bag.tidyUserBag();
		
		propDataList = bag.getOtherPropDatas();
		
		assertEquals(3, propDataList.size());
		assertEquals(BAG_WEAR_COUNT+0, propDataList.get(0).getPew());
		assertEquals(BAG_WEAR_COUNT+1, propDataList.get(1).getPew());
		assertEquals(BAG_WEAR_COUNT+2, propDataList.get(2).getPew());
		
		assertEquals(3, bag.getCurrentCount());
		
		PropData countPropData = null;
		for ( PropData pd : propDataList ) {
			if ( pd.getItemId().equals("20001") ) {
				countPropData = pd;
				break;
			}
		}
		assertNotNull(countPropData);
		assertEquals(26, countPropData.getCount());
	}
	
	@Test
	public void testTidyUserBagWithEquips() {
		User user = UserManager.getInstance().createDefaultUser();
		
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		
		PropData weapon1 = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).toPropData(10, WeaponColor.WHITE);
		PropData weapon2 = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId).toPropData(10, WeaponColor.WHITE);
		
		PropData item1 = ItemManager.getInstance().getItemById(UserManager.basicUserGiftBoxId).toPropData();
		PropData item2 = ItemManager.getInstance().getItemById(UserManager.basicUserGiftBoxId).toPropData(4);
				
		bag.addOtherPropDatas(weapon1);
		bag.addOtherPropDatas(weapon2);
		bag.addOtherPropDatas(item1);
		bag.addOtherPropDatas(item2);
		
		List<PropData> propDataList = bag.getOtherPropDatas();
		
		assertEquals(4, propDataList.size());
		assertEquals(1, propDataList.get(0).getCount());
		assertEquals(4, propDataList.get(3).getCount());
		
		bag.tidyUserBag();
		
		propDataList = bag.getOtherPropDatas();
		
		assertEquals(3, propDataList.size());
		
		PropData actualWeapon1 = propDataList.get(0);
		PropData actualWeapon2 = propDataList.get(1);
		PropData actualItem = propDataList.get(2);
		
		assertEquals(BAG_WEAR_COUNT+0, actualWeapon1.getPew());
		assertEquals(BAG_WEAR_COUNT+1, actualWeapon2.getPew());
		assertEquals(BAG_WEAR_COUNT+2, actualItem.getPew());
		
		assertEquals(1, actualWeapon1.getCount());
		assertEquals(1, actualWeapon2.getCount());
		assertEquals(5, actualItem.getCount());
	}
	
	@Test
	public void testTaskWearCloth() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		TaskManager manager = TaskManager.getInstance();
		manager.deleteUserTasks(user);
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		//4	装备新衣服	5	6	1	0	TASK_MAIN	装备一件衣服	script.task.WearClothes
		TaskPojo task1 = manager.getTaskById("4");
		tasks.add(task1);
		user.addTasks(tasks);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		//2030	黑铁●简约时尚
		PropData propData = EquipManager.getInstance().getWeaponById("2330").toPropData(0, WeaponColor.WHITE);
		
		int count = BAG_MAX_COUNT;
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		bag.addOtherPropDatas(propData);
		
		assertTrue(bag.wearPropData(BAG_WEAR_COUNT, PropDataEquipIndex.CLOTH.index()));
		
		Thread.currentThread().sleep(200);
		
		System.out.println(list);
		assertTrue(list.size()>=2);
		// 0 : BseSysMessage
		XinqiMessage message = list.get(1);
		assertTrue(message.payload instanceof BseModiTask);
	}
	
	/**
	 * Make a fake bag.
	 * @param user
	 * @return
	 */
	private Bag makeBag2(User user, int count) {
		Bag bag = new Bag();
		bag.setUserd(user.get_id());
		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(10000+i));
		}
		return bag;
	}
	
	/**
	 * Make a fake PropData
	 * @param i
	 * @return
	 */
	private PropData makePropData(int i) {
		PropData propData = new PropData();
		propData.setItemId(""+i);
		propData.setName("夺命刀-"+i);
		propData.setBanded(true);
		propData.setWeaponColor(WeaponColor.WHITE);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setDuration(1003);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}
	
	private PropData makePropData(String id) {
		PropData propData = new PropData();
		propData.setItemId(id);
		propData.setName("夺命刀-"+id);
		propData.setBanded(true);
		propData.setWeaponColor(WeaponColor.WHITE);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setDuration(1003);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}

	private int[] changeFlagToIntArray(Set<Integer> set) {
		int[] flags = new int[set.size()];
		Iterator<Integer> iter = set.iterator();
		int count = 0;
		while (iter.hasNext()) {
			flags[count++] = iter.next();
		}
		return flags;
	}
	
	private int getOtherPropDataIndex(PropData propData, List<PropData> propDataList) {
		int i=0;
		for ( PropData pd : propDataList) {
			if ( propData.equals(pd) ) {
				return BAG_WEAR_COUNT + i;
			}
			i++;
		}
		return -1;
	}
	
	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
