package com.xinqihd.sns.gameserver.reward;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.RewardLevelPojo;
import com.xinqihd.sns.gameserver.config.RewardPojo;
import com.xinqihd.sns.gameserver.config.RewardPojoType;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.util.DateUtil;

public class RewardManagerTest {
	
	User user = null;
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
		UserManager.getInstance().removeUser(userName);
		DateUtil.resetInternalDate();
		
		user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId(userName));
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setGender(Gender.MALE);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGenerateRandomReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		ArrayList<Reward> loginRewards = RewardManager.getInstance().generateRewardsFromScript(
				user, 44, ScriptHook.USER_LOGIN_REWARD);
		
		for ( Reward r : loginRewards ) {
			System.out.println(r);
			if ( r.getType() == RewardType.WEAPON ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(
						r.getId());
				System.out.println(weapon.getName());
			}
		}
		assertEquals(44, loginRewards.size());
	}
	
	@Test
	public void testGenerateRandomRewardForItem() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Set<RewardType> excludeTypes = new HashSet<RewardType>();
		excludeTypes.add(RewardType.GOLDEN);
		excludeTypes.add(RewardType.EXP);
		excludeTypes.add(RewardType.STONE);
		excludeTypes.add(RewardType.YUANBAO);
		excludeTypes.add(RewardType.WEAPON);
		ArrayList<Reward> rewards = RewardManager.getInstance().generateRandomRewards(user, 100, excludeTypes);
		
		for ( Reward r : rewards ) {
			ItemPojo item  = ItemManager.getInstance().getItemById(r.getPropId());
			if ( item != null ) {
				System.out.println("" + item.getName() + ", " + item.getLevel());
				assertEquals(Constant.EMPTY, item.getScript());
			} else {
				System.out.println("type:" + r.getType() + ", propId: " + r.getPropId() + ", count:" + r.getPropCount());
				assertEquals(RewardType.GOLDEN, r.getType());
			}
		}
	}
	
	@Test
	public void testGenerateRandomRewardForWeapon() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Set<RewardType> excludeTypes = new HashSet<RewardType>();
		excludeTypes.add(RewardType.GOLDEN);
		excludeTypes.add(RewardType.EXP);
		excludeTypes.add(RewardType.STONE);
		excludeTypes.add(RewardType.YUANBAO);
		excludeTypes.add(RewardType.ITEM);
		ArrayList<Reward> rewards = RewardManager.getInstance().generateRandomRewards(user, 100, excludeTypes);
		
		for ( Reward r : rewards ) {
			WeaponPojo item  = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(r.getTypeId(), user.getLevel());
			if ( item != null ) {
				System.out.println("" + item.getName() + ", " + item.getUserLevel());
				assertTrue("No weapon", item.getSlot() != EquipType.WEAPON);
				assertTrue("No suit", item.getSlot() != EquipType.SUIT);
			} else {
				System.out.println("type:" + r.getType() + ", propId: " + r.getPropId() + ", count:" + r.getPropCount());
				assertEquals(RewardType.GOLDEN, r.getType());
			}
		}
	}
	
	@Test
	public void testGenerateRandomRewardNoMedalAndVoucher() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		ArrayList<Reward> rewards = RewardManager.getInstance().
				generateRandomRewards(user, 1000, null);
		
		for ( Reward r : rewards ) {
			assertTrue("No medal", r.getType() != RewardType.MEDAL);
			assertTrue("No voucher", r.getType() != RewardType.VOUCHER);
		}
	}
	
	@Test
	public void testGenerateGoldenReward() {
		RewardManager manager = RewardManager.getInstance();

		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		for ( int i=0; i<1000; i++ ) {
			Reward r = manager.getRewardGolden(null);
			Integer count = countMap.get(r.getPropCount());
			if ( count != null ) {
				countMap.put(r.getPropCount(), count+1);
			} else {
				countMap.put(r.getPropCount(), 1);
			}
			assertEquals(RewardType.GOLDEN, r.getType());
		}
		System.out.println(countMap);
	}

	@Test
	public void testProcessLoginReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0
		current.set(2012, 1, 15, 11, 38, 0);
		int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testProcessLoginRewardTwoDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		DateUtil.resetInternalDate();
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0
		current.set(2012, 1, 15, 11, 38, 0);
		int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login second day
		current.set(2012, 1, 16, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(2, user.getContinuLoginTimes());
		assertEquals(2, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testProcessLoginRewardThreeDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		DateUtil.resetInternalDate();
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0
		current.set(2012, 1, 15, 11, 38, 0);
		int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login second day
		current.set(2012, 1, 16, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(2, user.getContinuLoginTimes());
		assertEquals(2, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login third day
		current.set(2012, 1, 17, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(3, user.getContinuLoginTimes());
		assertEquals(3, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login fourth day
		current.set(2012, 1, 18, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(4, user.getContinuLoginTimes());
		assertEquals(4, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		for ( int i=0; i<10; i++ ) {
			current.set(2012, 1, 19+i, 11, 38, 0);
			remainTimes = manager.processLoginReward(user, current.getTimeInMillis());			
		}
		
		int max = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.USER_ONLINE_REWARD_MAX, 7);
		assertEquals(max, user.getContinuLoginTimes());
		assertEquals(max, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testProcessLoginRewardBrokenDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0
		current.set(2012, 1, 15, 11, 38, 0);
		int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//broken first day
		current.set(2012, 1, 17, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testProcessLoginRewardBrokenSecondDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		DateUtil.resetInternalDate();
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0
		current.set(2012, 1, 15, 11, 38, 0);
		int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login second day
		current.set(2012, 1, 16, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(2, user.getRemainLotteryTimes());
		assertEquals(2, user.getContinuLoginTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
		
		//Login third day
		current.set(2012, 1, 18, 11, 38, 0);
		remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
		assertEquals(1, user.getRemainLotteryTimes());
		assertEquals(1, user.getContinuLoginTimes());
		assertEquals(remainTimes, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testProcessLoginRewardRepeat() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		DateUtil.resetInternalDate();
		Calendar current = Calendar.getInstance();
		int max = 10;
		//2012-2-15 11:38:0 
		current.set(2012, 1, 15, 11, 38, 0);
		for ( int i=0; i<max; i++ ) {
			int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
			assertEquals(1, user.getRemainLotteryTimes());
			assertEquals(remainTimes, user.getRemainLotteryTimes());
		}
		
		//Login second day for many times
		current.set(2012, 1, 16, 11, 38, 0);
		for ( int i=0; i<max; i++ ) {
			int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
			assertEquals(2, user.getRemainLotteryTimes());
			assertEquals(remainTimes, user.getRemainLotteryTimes());
		}
		
		//Login third day for many times
		current.set(2012, 1, 18, 11, 38, 0);
		for ( int i=0; i<max; i++ ) {
			int remainTimes = manager.processLoginReward(user, current.getTimeInMillis());
			assertEquals(1, user.getRemainLotteryTimes());
			assertEquals(remainTimes, user.getRemainLotteryTimes());
		}
	}
	
	@Test
	public void testTakeLoginReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//2012-2-15 11:38:0 
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		boolean success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(false, success);
		assertEquals(0, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testTakeLoginRewardTwoDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		DateUtil.resetInternalDate();
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		boolean success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		
		//Second day
		current.set(2012, 1, 16, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(1, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		
		//Third day
		current.set(2012, 1, 17, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(2, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(1, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(false, success);
		assertEquals(0, user.getRemainLotteryTimes());
	}
	
	@Test
	public void testTakeLoginReward10Days() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		DateUtil.resetInternalDate();
		
		Calendar current = Calendar.getInstance();
		
		//First day
		for ( int i=1; i<=20; i++ ) {
			current.add(Calendar.DAY_OF_MONTH, 1);
			long currentTime = current.getTimeInMillis();
			boolean success = manager.takeLoginReward(user, currentTime);
			assertEquals(true, success);
			//assertEquals(1, user.getRemainLotteryTimes());
			System.out.println(user.getContinuLoginTimes());
			assertEquals(Math.min(i, 7), user.getContinuLoginTimes());
		}
	}
	
	@Test
	public void testTakeLoginRewardBrokenDays() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		DateUtil.resetInternalDate();
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		boolean success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		
		//Second day
		current.set(2012, 1, 16, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(1, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		
		//Third day broken
		current.set(2012, 1, 18, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(true, success);
		assertEquals(0, user.getRemainLotteryTimes());
		success = manager.takeLoginReward(user, currentTime);
		assertEquals(false, success);
		assertEquals(0, user.getRemainLotteryTimes());
	}

	@Test
	public void testGetLoginRewardKeyName() {
		RewardManager manager = RewardManager.getInstance();
		String key = manager.getLoginRewardKeyName(userName);
		assertEquals("reward:login:"+userName, key);
	}

	@Test
	public void testProcessDailyMarkRewardFirst() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String setName = manager.getDailyMarkRewardKeyName(userName);
		String actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-02", actualCurrentMonth);
		String actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals(null, actualCurrentDate);
		String actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("0", actualTotalCount);
		String actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals(null, actualMarkArray);
		
		assertEquals(15, dailyMark.getDayOfMonth());
		assertEquals(0,  dailyMark.getTotalCount());
		assertEquals("2012-02",  dailyMark.getCurrentMonth());
		assertEquals(0,  dailyMark.getMarkArray().size());
		assertNull(dailyMark.getDailyMark());
		
		//Test again
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		assertEquals(15, dailyMark.getDayOfMonth());
		assertEquals(0,  dailyMark.getTotalCount());
		assertEquals("2012-02",  dailyMark.getCurrentMonth());
		assertEquals(0,  dailyMark.getMarkArray().size());
		assertNull(dailyMark.getDailyMark());
	}
	
	@Test
	public void testProcessDailyMarkReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String setName = manager.getDailyMarkRewardKeyName(userName);
		String actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-02", actualCurrentMonth);
		String actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals("2012-02-15", actualCurrentDate);
		String actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("1", actualTotalCount);
		String actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals("0,0,0,0,0,0,0,0,0,0,0,0,0,0,1", actualMarkArray);
		
		for ( int i=0; i<4; i++ ) {
			current.set(2012, 1, 16+i, 11, 38, 0);
			currentTime = current.getTimeInMillis();
			dailyMark = manager.processDailyMarkReward(user, currentTime);
			manager.takeDailyMarkReward(user, currentTime);
			assertNotNull(dailyMark);
			assertNull(dailyMark.getDailyMark());
		}
		
		//Six days
		current.set(2012, 1, 20, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);
		//Requery again
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		assertEquals(20, dailyMark.getDayOfMonth());
		assertEquals(6,  dailyMark.getTotalCount());
		assertEquals("2012-02",  dailyMark.getCurrentMonth());
		assertEquals(20,  dailyMark.getMarkArray().size());
		assertNotNull(dailyMark.getDailyMark());
		
		//Test again
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		assertEquals(20, dailyMark.getDayOfMonth());
		assertEquals(6,  dailyMark.getTotalCount());
		assertEquals("2012-02",  dailyMark.getCurrentMonth());
		assertEquals(20,  dailyMark.getMarkArray().size());
		assertNotNull(dailyMark.getDailyMark());
		
		//Test again
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		assertEquals(20, dailyMark.getDayOfMonth());
		assertEquals(6,  dailyMark.getTotalCount());
		assertEquals("2012-02",  dailyMark.getCurrentMonth());
		assertEquals(20,  dailyMark.getMarkArray().size());
		assertNotNull(dailyMark.getDailyMark());
		
		actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-02", actualCurrentMonth);
		actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals("2012-02-20", actualCurrentDate);
		actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("6", actualTotalCount);
		actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals("0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1", actualMarkArray);
	}
	
	@Test
	public void testProcessDailyMarkRewardNextMonth() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 2, 31, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String setName = manager.getDailyMarkRewardKeyName(userName);
		String actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-03", actualCurrentMonth);
		String actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals(null, actualCurrentDate);
		String actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("0", actualTotalCount);
		String actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals(null, actualMarkArray);
		
		//Next month
		current.set(2012, 3, 1, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		
		actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-04", actualCurrentMonth);
		actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals("2012-04-01", actualCurrentDate);
		actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("1", actualTotalCount);
		actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals("1", actualMarkArray);
	}
	
	@Test
	public void testProcessDailyMarkRewardTtl() throws Exception {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 2, 31, 23, 59, 59);
		long currentTime = current.getTimeInMillis();
		
		DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
		assertNotNull(dailyMark);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String setName = manager.getDailyMarkRewardKeyName(userName);
		String actualCurrentMonth = jedisDB.hget(setName, "currentmonth");
		assertEquals("2012-03", actualCurrentMonth);
		String actualCurrentDate = jedisDB.hget(setName, "currentdate");
		assertEquals(null, actualCurrentDate);
		String actualTotalCount = jedisDB.hget(setName, "totalcount");
		assertEquals("0", actualTotalCount);
		String actualMarkArray = jedisDB.hget(setName, "markarray");
		assertEquals(null, actualMarkArray);
		
		Thread.sleep(2000);
		
		assertEquals(false, jedisDB.exists(setName));
	}
	
	@Test
	public void testProcessDailyMarkRewardRepeat() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
		manager.takeDailyMarkReward(user, currentTime);

		current.set(2012, 1, 16, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		for ( int i=0; i<4; i++ ) {
			dailyMark = manager.processDailyMarkReward(user, currentTime);
			manager.takeDailyMarkReward(user, currentTime);
			dailyMark = manager.processDailyMarkReward(user, currentTime);
			assertNotNull(dailyMark);
			assertEquals(16, dailyMark.getDayOfMonth());
			assertEquals(2,  dailyMark.getTotalCount());
			assertEquals("2012-02",  dailyMark.getCurrentMonth());
			assertEquals(16,  dailyMark.getMarkArray().size());
			assertNull(dailyMark.getDailyMark());
		}
		String key = manager.getDailyMarkRewardKeyName(userName);
		manager.takeDailyMarkReward(user, currentTime);
		Jedis jedisDB = JedisFactory.getJedisDB();
		String totalCount = jedisDB.hget(key, "totalcount");
		assertEquals("2", totalCount);
	}
	
	@Test
	public void testTakeDailyMarkReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		boolean success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
		
		for ( int i=0; i<4; i++ ) {
			current.set(2012, 1, 16+i, 11, 38, 0);
			currentTime = current.getTimeInMillis();
			success = manager.takeDailyMarkReward(user, currentTime);
			assertEquals(false, success);
		}
		
		//Six days
		current.set(2012, 1, 20, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(true, success);
	}
	
	@Test
	public void testTakeDailyMarkRewardNormalFlow() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		//The call of processDailyMarkReward should be no side-effect
		DailyMarkReward reward = manager.processDailyMarkReward(user, currentTime);
		assertEquals(false, reward.isTodayMarked());
		reward = manager.processDailyMarkReward(user, currentTime);
		assertEquals(false, reward.isTodayMarked());
		//After the user mark tody, the processDailyMarkReward should be marked too.
		boolean success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
		reward = manager.processDailyMarkReward(user, currentTime);
		assertEquals(true, reward.isTodayMarked());
		
		for ( int i=0; i<4; i++ ) {
			current.set(2012, 1, 16+i, 11, 38, 0);
			currentTime = current.getTimeInMillis();
			success = manager.takeDailyMarkReward(user, currentTime);
			assertEquals(false, success);
		}
		
		//Six days
		current.set(2012, 1, 20, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(true, success);
	}
	
	@Test
	public void testTakeDailyMarkRewardAndRequery() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		boolean success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
		
		for ( int i=0; i<4; i++ ) {
			current.set(2012, 1, 16+i, 11, 38, 0);
			currentTime = current.getTimeInMillis();
			success = manager.takeDailyMarkReward(user, currentTime);
			assertEquals(false, success);
		}
		
		//Six days
		current.set(2012, 1, 20, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(true, success);
		
		//take again
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
		
		//query again
	  DailyMarkReward dailyMark = manager.processDailyMarkReward(user, currentTime);
	  assertNotNull(dailyMark);
	}
	
	@Test
	public void testTakeDailyMarkRewardRepeat() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day
		current.set(2012, 1, 15, 11, 38, 0);
		long currentTime = current.getTimeInMillis();
		
		boolean success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
		
		for ( int i=0; i<4; i++ ) {
			current.set(2012, 1, 16+i, 11, 38, 0);
			currentTime = current.getTimeInMillis();
			success = manager.takeDailyMarkReward(user, currentTime);
			assertEquals(false, success);
		}
		
		//Six days
		current.set(2012, 1, 20, 11, 38, 0);
		currentTime = current.getTimeInMillis();
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(true, success);
		
		//Repeat
		success = manager.takeDailyMarkReward(user, currentTime);
		assertEquals(false, success);
	}
	
	@Test
	public void testProcessOnlineReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day: 11:38 
		current.set(2012, 1, 15, 11, 30, 0);
		long currentTimeMillis = current.getTimeInMillis();
		
		OnlineReward onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(0, onlineReward.getStepId());
		//next step: 14:30
		assertEquals(3*60*60, onlineReward.getRemainSeconds());
		assertEquals(4, onlineReward.getRewards().size());
		
		//test again
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(0, onlineReward.getStepId());
		//next step: 14:30
		assertEquals(3*60*60, onlineReward.getRemainSeconds());
		assertEquals(4, onlineReward.getRewards().size());
		
		//update the time
		current.set(2012, 1, 15, 12, 30, 0);
		currentTimeMillis = current.getTimeInMillis();
		//test again
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(0, onlineReward.getStepId());
		//next step: 14:30
		assertEquals(2*60*60, onlineReward.getRemainSeconds());
		assertEquals(4, onlineReward.getRewards().size());
	}
	
	@Test
	public void testProcessOnlineRewardFinishAll() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day: 23:30 
		current.set(2012, 1, 15, 23, 30, 0);
		long currentTimeMillis = current.getTimeInMillis();
		
		OnlineReward onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(null, onlineReward);
		boolean success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
	}
	
	@Test
	public void testTakeOnlineReward() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day: 23:30 
		current.set(2012, 1, 15, 23, 30, 0);
		long currentTimeMillis = current.getTimeInMillis();
		OnlineReward onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(null, onlineReward);
		
		//next day 0:10
		current.set(2012, 1, 16, 0, 10, 0);
		currentTimeMillis = current.getTimeInMillis();
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(0, onlineReward.getStepId());
		assertEquals(20*60, onlineReward.getRemainSeconds());
		assertTrue(onlineReward.getRewards().size()>0);
		
		//the reward is not ready
		current.set(2012, 1, 16, 0, 20, 0);
		currentTimeMillis = current.getTimeInMillis();
		boolean success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		
		//the reward is ready
		current.set(2012, 1, 16, 0, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
	}
	
	@Test
	public void testTakeOnlineRewardNormalFlow() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		
		Calendar current = Calendar.getInstance();
		//First day: 23:30 
		current.set(2012, 1, 15, 23, 30, 0);
		long currentTimeMillis = current.getTimeInMillis();
		OnlineReward onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(null, onlineReward);
		
		//next day 0:10
		current.set(2012, 1, 16, 0, 10, 0);
		currentTimeMillis = current.getTimeInMillis();
		boolean success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		
		//next day 0:40
		current.set(2012, 1, 16, 0, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		//repeat
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(1, onlineReward.getStepId());
		assertEquals("07:30", onlineReward.getTimeClock());
		
		//next day 07:40
		current.set(2012, 1, 16, 7, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(2, onlineReward.getStepId());
		assertEquals("11:30", onlineReward.getTimeClock());
		
		//next day 11:40
		current.set(2012, 1, 16, 11, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(3, onlineReward.getStepId());
		assertEquals("14:30", onlineReward.getTimeClock());
		
		//next day 16:40
		current.set(2012, 1, 16, 16, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(4, onlineReward.getStepId());
		assertEquals("18:30", onlineReward.getTimeClock());
		
		//next day 21:40
		current.set(2012, 1, 16, 21, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(5, onlineReward.getStepId());
		assertEquals("22:30", onlineReward.getTimeClock());
		
		//next day 23:40
		current.set(2012, 1, 16, 23, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(6, onlineReward.getStepId());
		assertEquals(null, onlineReward.getTimeClock());
		//try again
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(6, onlineReward.getStepId());
		assertEquals(null, onlineReward.getTimeClock());
		
		//next day 0:20
		current.set(2012, 1, 17, 0, 20, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(0, onlineReward.getStepId());
		assertEquals("00:30", onlineReward.getTimeClock());
		
		//next day 0:40
		current.set(2012, 1, 17, 0, 40, 0);
		currentTimeMillis = current.getTimeInMillis();
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(true, success);
		success = manager.takeOnlineReward(user, currentTimeMillis);
		assertEquals(false, success);
		onlineReward = manager.processOnlineReward(user, currentTimeMillis);
		assertEquals(1, onlineReward.getStepId());
		assertEquals("07:30", onlineReward.getTimeClock());
	}
	
	@Test
	public void testPickRewardCount() {		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		Reward reward = new Reward();
		reward.setPropId("20005");
		reward.setPropCount(2);
		reward.setPropLevel(1);
		reward.setType(RewardType.ITEM);
		rewards.add(reward);
		manager.pickReward(user, rewards, StatAction.ProduceOnlineReward);
		
		PropData propData = user.getBag().getOtherPropData(20);
		assertEquals("20005", propData.getItemId());
		assertEquals(2, propData.getCount());
	}
	
	@Test
	public void testPickRewardWeaponLevel() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		/*
		 * rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 12001	榴弹炮	5	1	0
		 */
		Reward reward = new Reward();
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		reward.setPropId(weapon.getId());
		reward.setPropCount(1);
		reward.setPropLevel(5);
		reward.setType(RewardType.WEAPON);
		reward.setPropIndate(30);
		rewards.add(reward);
		
		PropData expect = weapon.toPropData(30, WeaponColor.WHITE);
		
		manager.pickReward(user, rewards, StatAction.ProduceOnlineReward);
		
		PropData propData = user.getBag().getOtherPropData(20);
		System.out.println(propData.toDetailString());
		
		assertEquals(UserManager.basicWeaponItemId, propData.getItemId());
		//level 	 5 	 attackLev 	 250 	 defendLev 	 60
		assertEquals(5, propData.getLevel());
		assertEquals(1, propData.getCount());
		assertTrue( "should >"+expect.getAttackLev(), 
				propData.getAttackLev()>expect.getAttackLev());
		assertTrue( "should =="+expect.getDefendLev(), 
				propData.getDefendLev()==expect.getDefendLev());
	}
	
	@Test
	public void testOpenItemBoxWithWeapon() {
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * itemId itemName itemLevel script 	 count 	 q 	rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 25103	天翼之戒首饰礼包		0	PackageBox	1	1	14001	天翼之戒	2	4	0
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25103");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(4, bag.getCurrentCount());
		
		//The '0' is the original box, it is deleted
		//2570 黑铁●天翼之戒
		/*
			780	黑铁●天翼之戒
			781	青铜●天翼之戒
			782	赤钢●天翼之戒
			783	白银●天翼之戒
			784	黄金●天翼之戒
			785	琥珀●天翼之戒
			786	翡翠●天翼之戒
			787	水晶●天翼之戒
			788	钻石●天翼之戒
			789	神圣●天翼之戒
		 */
		PropData newWeapon = bag.getOtherPropDatas().get(1);
		assertNotNull(newWeapon);
		assertEquals("780", newWeapon.getItemId());
		assertEquals(2, newWeapon.getLevel());
		System.out.println(newWeapon);
	}
	
	@Test
	public void testOpenItemBoxWithRandomBoxChat() {
		user.setLevel(20);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * itemId itemName itemLevel script 	 count 	 q 	rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 25811	武器宝盒Lv4	WeaponLv4	打开可随机获得强化等级+4的普通武器一把
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25811");
		box.setBroadcast(true);
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		////[PropData [itemId=25029, name=升级奖励Lv19, pew=20]
		bag.removeOtherPropDatas(20);
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getOtherPropDatas().size());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(1, bag.getCurrentCount());
		
		PropData newWeapon = bag.getOtherPropDatas().get(1);
		assertNotNull(newWeapon);
		System.out.println(newWeapon);
	}
	
	
	@Test
	public void testOpenItemBoxWithWeaponWith20Level() {
		user.setLevel(20);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * itemId itemName itemLevel script 	 count 	 q 	rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 25103	天翼之戒首饰礼包		0	PackageBox	1	1	14001	天翼之戒	2	4	0
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25103");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		////[PropData [itemId=25029, name=升级奖励Lv19, pew=20]
		bag.removeOtherPropDatas(20);
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getOtherPropDatas().size());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(4, bag.getCurrentCount());
		
		//The '0' is the original box, it is deleted
		//2570 黑铁●天翼之戒
		/*
			780	黑铁●天翼之戒
			781	青铜●天翼之戒
			782	赤钢●天翼之戒
			783	白银●天翼之戒
			784	黄金●天翼之戒
			785	琥珀●天翼之戒
			786	翡翠●天翼之戒
			787	水晶●天翼之戒
			788	钻石●天翼之戒
			789	神圣●天翼之戒
		 */
		PropData newWeapon = bag.getOtherPropDatas().get(1);
		assertNotNull(newWeapon);
		assertEquals("782", newWeapon.getItemId());
		assertEquals(2, newWeapon.getLevel());
		System.out.println(newWeapon);
	}
	
	@Test
	public void testOpenItemBoxWithWeapon100Level() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setLevel(LevelManager.MAX_LEVEL);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * itemId itemName itemLevel script 	 count 	 q 	rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 25103	天翼之戒首饰礼包		0	PackageBox	1	1	14001	天翼之戒	2	4	0
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25103");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getOtherPropDatas().size());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(4, bag.getCurrentCount());
		
		//The '0' is the original box, it is deleted
		//2570 黑铁●天翼之戒
		/*
			780	黑铁●天翼之戒
			781	青铜●天翼之戒
			782	赤钢●天翼之戒
			783	白银●天翼之戒
			784	黄金●天翼之戒
			785	琥珀●天翼之戒
			786	翡翠●天翼之戒
			787	水晶●天翼之戒
			788	钻石●天翼之戒
			789	神圣●天翼之戒
		 */
		PropData newWeapon = bag.getOtherPropDatas().get(1);
		assertNotNull(newWeapon);
		assertEquals("78", newWeapon.getItemId().substring(0, 2));
		assertEquals(2, newWeapon.getLevel());
		System.out.println(newWeapon);
	}
	
	@Test
	public void testOpenItemBoxWithGolden() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25008" typeid="25008" lv="0" icon="Baoxiang0001" name="金币盒" 
			info="打开可以获得2000金币" script="PackageBox" q="1" count="1">
			<reward type="GOLDEN" id="-1" level="0" count="2000" indate="0"/>
		</item>
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25008");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		int origGolden = user.getGolden();
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(0, bag.getCurrentCount());
		
		assertEquals(origGolden+100, user.getGolden());
	}
	
	@Test
	public void testOpenItemBoxWithLevelReach() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25011" typeid="25011" lv="1" icon="Baoxiang0001" name="升级奖励Lv1" 
			info="对玩家努力升级的奖赏，VIP玩家会获得双倍奖励，达到1级可以打开, 包含100礼金、1个2级强化石。" 
		ITEM	20022		0	1	0	0	100%
		GOLDEN	-1		0	500	0	0	100%
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25011");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.setOtherPropDataAtPew(propData, 60);
		assertEquals(1, bag.getCurrentCount());
		
		int original = user.getGolden();
		
		//open the box
		user.setLevel(2);
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(original+5, user.getGolden());
		
		//One is the reward item, the other is user.setLevel sent gift box.
		assertEquals(2, bag.getCurrentCount());
		PropData newPropData = bag.getOtherPropDatas().get(1);
		assertNotNull(newPropData);
		assertEquals("20022", newPropData.getItemId());
		assertEquals(2, newPropData.getLevel());
		
		System.out.println(newPropData);
	}
	
	@Test
	public void testOpenItemBoxWithLevelNotReach() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25014" typeid="25011" lv="4" icon="Baoxiang0001" 
			name="升级奖励Lv4" info="对玩家努力升级的奖赏，VIP玩家会获得双倍奖励，达到4级可以打开，包含100礼金、1个2级强化石。" script="LevelUpBox" q="1" count="1">
			  <reward type="VOUCHER" id="-1" level="0" count="100" indate="0"/>
		    <reward type="ITEM" id="20022" level="0" count="1" indate="0"/>
		</item>
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25014");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		int original = user.getVoucher();
		
		//open the box
		user.setLevel(1);
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.LEVEL_FAIL, pickResult);
		assertEquals(original, user.getVoucher());
		//The box is not removed.
		assertEquals(1, bag.getCurrentCount());
	}
	
	@Test
	public void testOpenItemBoxWithRandomBox() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25801" typeid="25801" lv="0" icon="GuildPack0001" name="一级功勋礼包" 
			info="消耗200功勋购买，一定机率会开到小金币箱X1、幸运符15%，还有机率会获得生命值加15%的项链
			（不可续费）哦" script="RandomBox" q="10" count="1">
		    <reward type="ITEM" id="25008" level="0" count="1000" indate="0"/>
		    <reward type="ITEM" id="24002" level="0" count="1" indate="0"/>
		    <reward type="WEAPON" id="18002" level="0" count="1" indate="60"/>
		</item>		
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25801");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		int original = user.getVoucher();
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		
		assertEquals(1, bag.getCurrentCount());
		//The '0' is the original box, it is deleted
		PropData newPropData = bag.getOtherPropDatas().get(1);
		assertNotNull(newPropData);
		System.out.println(newPropData);
	}
	
	@Test
	public void testOpenItemBoxWithMultiCount() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.getBag().removeOtherPropDatas(20);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * 25069 1元限量礼包 PackageBox
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25069");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		
		//The '0' is the original box, it is deleted
		PropData ring = null;
		int ringCount = 0;
		for ( int i=0; i<bag.getCurrentCount(); i++ ) {
			PropData newPropData = bag.getOtherPropDatas().get(i);
			if ( newPropData != null && newPropData.getItemId().equals("860") ) {
				ring = newPropData;
				ringCount++;
			}
		}
		assertNotNull(ring);
		assertEquals(1, ring.getCount());
		assertEquals(4, ringCount);
	}
	
	@Test
	public void testOpenItemBoxWithDoubleExp() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25901" typeid="25901" lv="0" icon="Shuangbeijingyanka" name="双倍经验卡" 
		info="使用后,您的战斗可一直获得双倍经验,直到退出游戏为止。" script="DoubleExpBox" q="1" 
		count="1"/>
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25901");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getCurrentCount());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);

		assertEquals(2.0, user.getExpRate(), 0.01);
		assertEquals(0, bag.getCurrentCount());
	}
	
	@Test
	public void testOpenItemBoxWithBagFull() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25063" typeid="25063" lv="0" icon="Qianghuaxiaolibao" name="强化小礼包" 
		info="内含3级强化石X3 幸运符25%X2 神恩符X2 " script="PackageBox" q="1" count="1">
		    <reward type="ITEM" id="20023" level="0" count="3" indate="0"/>
		    <reward type="ITEM" id="24004" level="0" count="2" indate="0"/>
		    <reward type="ITEM" id="24001" level="0" count="2" indate="0"/>
		</item>		
		 */
		Bag bag = user.getBag();
		int bagCount = bag.getMaxCount()-2;
		for ( int i=0; i<bagCount; i++ ) {
			bag.addOtherPropDatas(new PropData());
		}
		assertEquals(bagCount, bag.getCurrentCount());
		
		ItemPojo box = ItemManager.getInstance().getItemById("25063");
		PropData propData = box.toPropData();
		//Put it into user's bag
		bag.addOtherPropDatas(propData);
		assertEquals(bagCount+1, bag.getCurrentCount());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.BAG_FULL, pickResult);
		assertEquals(bagCount+1, bag.getCurrentCount());
	}
	
	@Test
	public void testOpenItemBoxWithoutKey() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25817" typeid="25817" lv="0" icon="Zhinvlihe" name="织女宝箱" 
			info="象征着织女姐姐的甜蜜祝福，只要打开宝箱便可获得各种套装、翅膀、宝石等，更有机会获得传说级武器-白虎牙，可在活动期间通过公会捐献获得！" script="RandomBox" q="2" count="1">
			<condition type="ITEM" id="25818" count="1"/>
		    <reward type="ITEM" id="20005" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20010" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20015" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20020" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20025" level="0" count="1" indate="0"/>
		    <reward type="WEAPON" id="40002" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40020" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40007" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40005" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40017" level="0" count="1" indate="60"/>		
		    <reward type="WEAPON" id="40001" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40016" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="4007" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="4002" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="4003" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="12013" level="0" count="1" indate="30"/>
		</item>					
			
		<item id="25818" typeid="25818" lv="0" icon="NiuLangKey" name="牛郎钥匙" 
			info="牛郎哥哥家的钥匙，唯有这把钥匙才能打开织女姐姐留下的宝箱哦！" />	
		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25817");
		PropData propData = box.toPropData();
		//Put 织女宝箱 into user's bag
		bag.addOtherPropDatas(propData);
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.CONDITION_FAIL, pickResult);
		assertEquals(1, bag.getCurrentCount());
	}
	
	@Test
	public void testOpenItemBoxWithKey() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		<item id="25817" typeid="25817" lv="0" icon="Zhinvlihe" name="织女宝箱" 
			info="象征着织女姐姐的甜蜜祝福，只要打开宝箱便可获得各种套装、翅膀、宝石等，更有机会获得传说级武器-白虎牙，可在活动期间通过公会捐献获得！" script="RandomBox" q="2" count="1">
			<condition type="ITEM" id="25818" count="1"/>
		    <reward type="ITEM" id="20005" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20010" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20015" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20020" level="0" count="1" indate="0"/>
		    <reward type="ITEM" id="20025" level="0" count="1" indate="0"/>
		    <reward type="WEAPON" id="40002" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40020" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40007" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40005" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40017" level="0" count="1" indate="60"/>		
		    <reward type="WEAPON" id="40001" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="40016" level="0" count="1" indate="60"/>	
		    <reward type="WEAPON" id="4007" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="4002" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="4003" level="0" count="1" indate="60"/>
		    <reward type="WEAPON" id="12013" level="0" count="1" indate="30"/>
		</item>					
			
		<item id="25818" typeid="25818" lv="0" icon="NiuLangKey" name="牛郎钥匙" 
			info="牛郎哥哥家的钥匙，唯有这把钥匙才能打开织女姐姐留下的宝箱哦！" />	
		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25817");
		ItemPojo key = ItemManager.getInstance().getItemById("25818");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		//Put 织女宝箱 into user's bag
		bag.addOtherPropDatas(propData);
		bag.addOtherPropDatas(keyData);
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(1, bag.getCurrentCount());
	}
	
	@Test
	public void testOpenItemBoxWithMultiKey() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
			25808	阿波罗神罐	AboluoShenhu	
			太阳神遗留之物，打开后有几率获得阿波罗神戒、阿波罗神镯、
			+8武器、五级合成神石、五级强化石等宝物，可通过战斗结束后翻牌或者商城购买获得！	
			
			25808	0	script.box.RandomBox	5.0	
			[ { "class" : "Reward" , "id" : "20005" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : Reward" , "id" : "20010" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "Reward" , "id" : "20015" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20020" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20025" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "277" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "278" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "58" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "65" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "61" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "57" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "62" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "66" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "63" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "60" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false}]	false	
			
			[ { "class" : "RewardCondition" , 
			"id" : "25809" , "count" : 4 , "rewardType" : "ITEM"}]

		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25808");
		ItemPojo key = ItemManager.getInstance().getItemById("25809");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		//Put 阿波罗神罐 into user's bag
		bag.addOtherPropDatas(propData);
		//Put 阿波罗神锤 x4 into user's bag
		bag.addOtherPropDatas(keyData.clone());
		bag.addOtherPropDatas(keyData.clone());
		bag.addOtherPropDatas(keyData.clone());
		bag.addOtherPropDatas(keyData.clone());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		List<PropData> list = bag.getOtherPropDatas();
		for ( PropData pd : list ) {
			if (pd != null) {
				assertTrue( !"25808".equals(pd.getItemId()) && !"25809".equals(pd.getItemId()));
			}
		}
	}
	
	@Test
	public void testOpenItemBoxWithMultiKeyInOneItem() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
			25808	阿波罗神罐	AboluoShenhu	
			太阳神遗留之物，打开后有几率获得阿波罗神戒、阿波罗神镯、
			+8武器、五级合成神石、五级强化石等宝物，可通过战斗结束后翻牌或者商城购买获得！	
			
			25808	0	script.box.RandomBox	5.0	
			[ { "class" : "Reward" , "id" : "20005" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : Reward" , "id" : "20010" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "Reward" , "id" : "20015" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20020" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20025" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "277" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "278" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "58" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "65" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "61" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "57" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "62" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "66" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "63" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "60" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false}]	false	
			
			[ { "class" : "RewardCondition" , 
			"id" : "25809" , "count" : 4 , "rewardType" : "ITEM"}]

		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25808");
		ItemPojo key = ItemManager.getInstance().getItemById("25809");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		keyData.setCount(4);
		//Put 阿波罗神罐 into user's bag
		bag.addOtherPropDatas(propData);
		//Put 阿波罗神锤 x4 into user's bag
		bag.addOtherPropDatas(keyData);
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		List<PropData> list = bag.getOtherPropDatas();
		for ( PropData pd : list ) {
			if (pd != null) {
				assertTrue( !"25808".equals(pd.getItemId()) && !"25809".equals(pd.getItemId()));
			}
		}
	}
	
	@Test
	public void testOpenItemBoxWithMultiKeyInOneItemMore() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
			25808	阿波罗神罐	AboluoShenhu	
			太阳神遗留之物，打开后有几率获得阿波罗神戒、阿波罗神镯、
			+8武器、五级合成神石、五级强化石等宝物，可通过战斗结束后翻牌或者商城购买获得！	
			
			25808	0	script.box.RandomBox	5.0	
			[ { "class" : "Reward" , "id" : "20005" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : Reward" , "id" : "20010" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "Reward" , "id" : "20015" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20020" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20025" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "277" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "278" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "58" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "65" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "61" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "57" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "62" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "66" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "63" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "60" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false}]	false	
			
			[ { "class" : "RewardCondition" , 
			"id" : "25809" , "count" : 4 , "rewardType" : "ITEM"}]

		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25808");
		ItemPojo key = ItemManager.getInstance().getItemById("25809");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		keyData.setCount(5);
		//Put 阿波罗神罐 into user's bag
		bag.addOtherPropDatas(propData);
		//Put 阿波罗神锤 x4 into user's bag
		bag.addOtherPropDatas(keyData);
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		List<PropData> list = bag.getOtherPropDatas();
		for ( PropData pd : list ) {
			if (pd != null) {
				assertTrue( !"25808".equals(pd.getItemId()) );
				if ( "25809".equals(pd.getItemId()) ) {
					assertEquals(1, pd.getCount());
				}
			}
		}
	}
	
	@Test
	public void testOpenItemBoxWithMultiKeyInTwoItem() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
			25808	阿波罗神罐	AboluoShenhu	
			太阳神遗留之物，打开后有几率获得阿波罗神戒、阿波罗神镯、
			+8武器、五级合成神石、五级强化石等宝物，可通过战斗结束后翻牌或者商城购买获得！	
			
			25808	0	script.box.RandomBox	5.0	
			[ { "class" : "Reward" , "id" : "20005" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : Reward" , "id" : "20010" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "Reward" , "id" : "20015" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20020" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20025" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "277" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "278" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "58" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "65" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "61" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "57" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "62" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "66" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "63" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "60" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false}]	false	
			
			[ { "class" : "RewardCondition" , 
			"id" : "25809" , "count" : 4 , "rewardType" : "ITEM"}]

		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25808");
		ItemPojo key = ItemManager.getInstance().getItemById("25809");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		keyData.setCount(2);
		PropData keyData2 = keyData.clone();
		keyData2.setCount(3);
		//Put 阿波罗神罐 into user's bag
		bag.addOtherPropDatas(propData);
		//Put 阿波罗神锤 x4 into user's bag
		bag.addOtherPropDatas(keyData);
		bag.addOtherPropDatas(keyData2);
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		List<PropData> list = bag.getOtherPropDatas();
		for ( PropData pd : list ) {
			if (pd != null) {
				assertTrue( !"25808".equals(pd.getItemId()) );
				if ( "25809".equals(pd.getItemId()) ) {
					assertEquals(1, pd.getCount());
				}
			}
		}
	}
	
	@Test
	public void testOpenItemBoxWithMultiKeyNotEnough() {
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
			25808	阿波罗神罐	AboluoShenhu	
			太阳神遗留之物，打开后有几率获得阿波罗神戒、阿波罗神镯、
			+8武器、五级合成神石、五级强化石等宝物，可通过战斗结束后翻牌或者商城购买获得！	
			
			25808	0	script.box.RandomBox	5.0	
			[ { "class" : "Reward" , "id" : "20005" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : Reward" , "id" : "20010" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "Reward" , "id" : "20015" , "typeId" :  null  , 
			"type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , 
			"x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , 
			{ "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20020" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "20025" , "typeId" :  null  , "type" : "ITEM" , "level" : 0 , "count" : 1 , "indate" : 0 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "277" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "278" , "type" : "WEAPON" , "level" : 0 , "count" : 1 , "indate" : 60 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "58" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "65" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "61" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "57" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "62" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "66" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "63" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false} , { "class" : "com.xinqihd.sns.gameserver.reward.Reward" , "id" : "-1" , "typeId" : "60" , "type" : "WEAPON" , "level" : 8 , "count" : 1 , "indate" : 30 , "x" : 0 , "y" : 0 , "color" : 0 , "isBroadcast" : false}]	false	
			
			[ { "class" : "RewardCondition" , 
			"id" : "25809" , "count" : 4 , "rewardType" : "ITEM"}]

		 */
		Bag bag = user.getBag();
		
		ItemPojo box = ItemManager.getInstance().getItemById("25808");
		ItemPojo key = ItemManager.getInstance().getItemById("25809");
		PropData propData = box.toPropData();
		PropData keyData = key.toPropData();
		//Put 阿波罗神罐 into user's bag
		bag.addOtherPropDatas(propData);
		//Put 阿波罗神锤 x4 into user's bag
		bag.addOtherPropDatas(keyData.clone());
		bag.addOtherPropDatas(keyData.clone());
		bag.addOtherPropDatas(keyData.clone());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.CONDITION_FAIL, pickResult);
		assertEquals(4, bag.getCurrentCount());
	}
	
	@Test
	public void testGenerateRewards() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(9);
		
		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.
				generateRandomRewards(user, 16, null);
		for ( Reward reward: list ) {
			System.out.println(reward);
		}
	}
	
	@Test
	public void testGenerateRewardItems() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(9);
		
		Set<RewardType> excludes = EnumSet.allOf(RewardType.class);
		excludes.remove(RewardType.ITEM);

		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.
				generateRandomRewards(user, 100, excludes);
		for ( Reward reward: list ) {
			ItemPojo item = ItemManager.getInstance().getItemById(reward.getPropId());
			if ( item != null ) {
				System.out.println(item.getName());
			}
		}
	}
	
	@Test
	public void testGenerateRewardStones() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(9);
		
		Set<RewardType> excludes = EnumSet.allOf(RewardType.class);
		excludes.remove(RewardType.STONE);

		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.
				generateRandomRewards(user, 16, excludes);
		for ( Reward reward: list ) {
			String typeId = reward.getPropId();
			int level = reward.getLevel();
			ItemPojo item = ItemManager.getInstance().getItemByTypeIdAndLevel(typeId, level);
			System.out.println(typeId+":"+level+":"+item);
			assertNotNull(item);
		}
	}
	
	@Test
	public void testGenerateRewardStonesHighLevel() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(30);
		
		Set<RewardType> excludes = EnumSet.allOf(RewardType.class);
		excludes.remove(RewardType.STONE);

		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.
				generateRandomRewards(user, 16, excludes);
		for ( Reward reward: list ) {
			String typeId = reward.getPropId();
			int level = reward.getLevel();
			ItemPojo item = ItemManager.getInstance().getItemByTypeIdAndLevel(typeId, level);
			System.out.println(typeId+":"+level+":"+item);
			assertNotNull(item);
		}
	}
	
	@Test
	public void testGenerateRewardsWithoutExp() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(9);
		
		Set<RewardType> excludes = EnumSet.noneOf(RewardType.class);
		excludes.add(RewardType.EXP);
		
		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.
				generateRandomRewards(user, 16, excludes);
		for ( Reward reward: list ) {
			System.out.println(reward);
		}
		assertEquals(16, list.size());
	}
	
	@Test
	public void testGenerateRewardsWeaponColor() {
		EnumSet<RewardType> excludeSet = EnumSet.allOf(RewardType.class);
		excludeSet.remove(RewardType.WEAPON);
		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.generateRandomRewards(user, 10, excludeSet);
		Reward colorReward = null;
		for ( Reward reward: list ) {
			if ( reward.getPropColor().ordinal()>0 ) {
				colorReward = reward;
				break;
			}
		}
		System.out.println(colorReward);
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		rewards.add(colorReward);
		RewardManager.getInstance().pickRewardWithResult(
				user, rewards, StatAction.BattleReward);
	}
	
	@Test
	public void testGenerateRewardsWeapon() {
		int max = 1000;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		user.setIsvip(true);
		for ( int i=0; i<max; i++ ) {
			Reward reward = RewardManager.generateRandomWeapon(user);
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(reward.getId());
			WeaponColor color = reward.getPropColor();
			String key = weapon.getSlot()+":"+color;
			Integer value = map.get(key);
			if ( value == null ) {
				value = 1;
				map.put(key, value);
			} else {
				map.put(key, value.intValue()+1);
			}
		}
		user.setIsvip(false);
		for ( String key: map.keySet() ) {
			System.out.println(key+" : " + map.get(key) );
		}
	}
	
	@Test
	public void testGenerateRewardsGender() {
		User user = new User();
		user.setGender(Gender.MALE);
		user.setLevel(9);
		
		Set<RewardType> excludes = EnumSet.allOf(RewardType.class);
		excludes.remove(RewardType.WEAPON);
		
		ArrayList<Reward> list = (ArrayList<Reward>)RewardManager.generateRandomRewards(user, 100, excludes);
		for ( Reward reward: list ) {
			if ( reward.getType() != RewardType.WEAPON ) continue;
			WeaponPojo weapon = EquipManager.getInstance().getWeaponByTypeNameAndUserLevel(reward.getTypeId(), user.getLevel());
			assertEquals(Gender.ALL, weapon.getSex());
		}
	}

	@Test
	public void testConvertRewardToPropData() {
		Reward reward = RewardManager.getInstance().getWeaponReward(EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId), 11, 0, false);
		PropData propData = RewardManager.getInstance().convertRewardWeaponToPropData(reward, user);
		assertEquals(11, propData.getLevel());
	}
	
	public void testAddReward() {
		RewardPojo rewardPojo = new RewardPojo();
		rewardPojo.set_id("1");
		rewardPojo.setStartMillis(0l);
		rewardPojo.setEndMillis(0l);
		rewardPojo.setName("测试项目");
		rewardPojo.setRatio(50);
		rewardPojo.addInclude(RewardPojoType.BATTLE_REWARD);
		rewardPojo.addInclude(RewardPojoType.BATTLE_BOX_PICK);
		ItemPojo itemPojo = ItemManager.getInstance().getItemById(ItemManager.strengthStoneId);
		rewardPojo.setReward(RewardManager.getRewardItem(itemPojo));

		RewardManager.getInstance().addRewardPojo(rewardPojo);

		RewardManager.getInstance().reload();
	}
	
	@Test
	public void testAddRewardLevelPojo() {
		//Item
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		for ( ItemPojo item : items ) {
			RewardLevelPojo pojo = new RewardLevelPojo();
			pojo.set_id(item.getId());
			boolean enabled = true;
			if ( !item.isCanBeRewarded() ) {
				enabled = false;
			}
			pojo.setName(item.getName());
			pojo.setEnabled(enabled);
			pojo.setMinLevel(1);
			pojo.setMaxLevel(LevelManager.MAX_LEVEL);
			RewardManager.getInstance().addRewardLevelPojo(pojo);
		}
		//Weapons
		for ( EquipType slot : EquipType.values() ) {
			Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeaponsBySlot(slot);
			if ( weapons == null ) {
				continue;
			}
			ArrayList<WeaponPojo> sortWeapons = new ArrayList<WeaponPojo>();
			for ( WeaponPojo weapon : weapons ) {
				if ( weapon.getUserLevel() != 0 ) {
					continue; 
				}
				sortWeapons.add(weapon);
			}
			Collections.sort(sortWeapons, new Comparator<WeaponPojo>() {
				@Override
				public int compare(WeaponPojo o1, WeaponPojo o2) {
					int powerDiff = o1.getPower() - o2.getPower();
					if ( powerDiff == 0 ) {
						return o1.compareTo(o2);
					} else {
						return powerDiff;
					}
				}

			});
			int count = 0; 
			int totalSize = sortWeapons.size();
			int limit1 = (int)Math.round(totalSize * 0.3);
			int limit2 = (int)Math.round(totalSize * 0.7 / 9);
			for ( WeaponPojo weapon : sortWeapons ) {
				if ( weapon.getUserLevel() != 0 ) {
					continue; 
				}
				count++;
				RewardLevelPojo pojo = new RewardLevelPojo();
				pojo.set_id(weapon.getId());
				boolean enabled = true;
				if ( weapon.isUsedByBoss() ) {
					enabled = false;
				}
				pojo.setName(weapon.getName());
				pojo.setEnabled(enabled);
				/**
				 * 30%的基础武器在10级内解锁
				 */
				if ( count <= limit1 ) {
					pojo.setMinLevel(1);
				} else {
					int levelIndex = Math.round((count*1.0f-limit1)/limit2);
					pojo.setMinLevel( 10 * levelIndex );
				}
				pojo.setMaxLevel(LevelManager.MAX_LEVEL);
				System.out.println(pojo+","+weapon.getPower()+","+
						EquipCalculator.calculateWeaponPower(weapon));
				RewardManager.getInstance().addRewardLevelPojo(pojo);
			}
		}
	}
}
