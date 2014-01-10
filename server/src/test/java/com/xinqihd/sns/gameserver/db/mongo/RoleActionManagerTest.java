package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class RoleActionManagerTest {

	private String roleName = "test-001";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSetRoleActionBuyCount() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setYuanbaoSimple(100000);
		user.setIsvip(true);
		user.setViplevel(10);
		Calendar cal = Calendar.getInstance();
		
		//buyCount, buyPrice, buyValue, buyTimes
		int roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(0, roleAction);
		
		boolean success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		assertTrue(success);
		
		//Exhaust the buyCount
		while ( success ) {
			success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		}
		
		//should add new roleAction to substract from the base value 0
		roleAction = StringUtil.toInt(jedis.hget(keyName, "roleaction"), -1);
		assertEquals(-4300, roleAction);
		
		//Reset the roleAction
		manager.setRoleActionBuyCount(user, 100, cal.getTimeInMillis());
		
		//Add roleaction again
		user.setYuanbaoSimple(100000);
		success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		assertTrue("Should allow to buy new", success);
		
		roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(-4400, roleAction);
		
		int[] ints = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), true);
	}

	@Test
	public void testAddRoleActionPoint() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		Calendar cal = Calendar.getInstance();
		int roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(0, roleAction);
		
		cal.add(Calendar.HOUR_OF_DAY, 3);
		int gain = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_HOURLY, 5);
		roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(0, roleAction);
		
		manager.consumeRoleActionPoint(user, 10, cal.getTimeInMillis());
		
	}

	@Test
	public void testQueryActionLimit() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoSimple(100);

		Calendar cal = Calendar.getInstance();
		manager.setRoleActionBuyCount(user, 1, cal.getTimeInMillis());
		
		int[] result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		
		//Exhaust the roleaction points
		boolean success = true;
		while ( success ) {
			success = manager.consumeRoleActionPoint(user, 1, cal.getTimeInMillis());
		}
		
		//FIELD_ROLEACTION_KEY
		int roleAction = StringUtil.toInt(jedis.hget(keyName, "roleaction"), -1);
		assertEquals(100, roleAction);
		
		//Wait for roleaction to grow
		cal.add(Calendar.HOUR_OF_DAY, 2);
		roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(60, roleAction);
		
		//Buy new roleaction point
		success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		assertTrue(success);
		
		roleAction = StringUtil.toInt(jedis.hget(keyName, "roleaction"), -1);
		assertEquals(-40, roleAction);
		
		result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		int buyCount = result[0];
		int buyTimes = result[3];
		assertEquals(0, buyCount);
		assertEquals(1, buyTimes);
		
		success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		assertTrue(!success);
		
		result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		buyCount = result[0];
		buyTimes = result[3];
		assertEquals(0, buyCount);
		assertEquals(1, buyTimes);
		
		//To the next day
		cal.add(Calendar.DAY_OF_MONTH, 1);
		result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		buyCount = result[0];
		buyTimes = result[3];
		assertEquals(0, buyCount);
		assertEquals(0, buyTimes);
	}

	@Test
	public void testAddRoleActionPassHours() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		int[] result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		
		//Exhaust the roleaction points
		boolean success = true;
		while ( success ) {
			success = manager.consumeRoleActionPoint(user, 1, cal.getTimeInMillis());
		}
		
		//FIELD_ROLEACTION_KEY
		int roleAction = StringUtil.toInt(jedis.hget(keyName, "roleaction"), -1);
		assertEquals(100, roleAction);
		
		//Wait for roleaction to grow
		cal.add(Calendar.HOUR_OF_DAY, 2);
		
		//Add roleaction again
		success = manager.consumeRoleActionPoint(user, 1, cal.getTimeInMillis());
		assertTrue("Should allow to add new", success);
		
		roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(61, roleAction);
	}
	
	@Test
	public void testAddRoleActionOverMidNight() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		
		//Set the time before midnight.
		cal.set(Calendar.HOUR_OF_DAY, 23);
		
		//Use the roleaction points
		boolean success = true;
		for ( int i=0; i<10; i++ ) {
			success = manager.consumeRoleActionPoint(user, 2, cal.getTimeInMillis());
		}
		
		int actual = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(20, actual);
		
		//Wait for next day
		cal.add(Calendar.HOUR_OF_DAY, 2);
		System.out.println(cal.getTime());
		
		//User keeps gaming without logout
		success = manager.consumeRoleActionPoint(user, 2, cal.getTimeInMillis());
		assertTrue("Should allow to add new", success);
		
		actual = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(2, actual);
	}
	
	@Test
	public void testAddRoleActionGrowLimit() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 1);
		System.out.println(cal.getTime());
		
		int[] result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		
		//Use one action
		boolean success = manager.consumeRoleActionPoint(user, 2, cal.getTimeInMillis());
		assertTrue(success);
				
		//Wait for roleaction to grow
		cal.add(Calendar.HOUR_OF_DAY, 14);
		System.out.println(cal.getTime());
		
		//Add roleaction again
		int roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(0, roleAction);
	}
	
	@Test
	public void testAddRoleActionCrossDays() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		int[] result = manager.queryRoleActionLimit(user, cal.getTimeInMillis(), false);
		System.out.println(Arrays.toString(result));
		
		//Exhaust the roleaction points
		boolean success = true;
		while ( success ) {
			success = manager.consumeRoleActionPoint(user, 1, cal.getTimeInMillis());
		}
		
		//FIELD_ROLEACTION_KEY
		int roleAction = StringUtil.toInt(jedis.hget(keyName, "roleaction"), -1);
		assertEquals(100, roleAction);
		
		//Wait for next day
		cal.add(Calendar.DAY_OF_MONTH, 1);
		
		roleAction = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		assertEquals(0, roleAction);
	}
	
	@Test
	public void testUpdateRoleActionPointIfChanged() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);

		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		
		Calendar cal = Calendar.getInstance();
		
		boolean checked = manager.updateRoleActionPointIfChanged(user, cal.getTimeInMillis());
		//should not change.
		assertEquals(false, checked);

		//consume 10 points
		boolean success = manager.consumeRoleActionPoint(user, 10, cal.getTimeInMillis());
		assertTrue(success);
		
		//Wait 1 hour
		cal.add(Calendar.HOUR_OF_DAY, 1);
		checked = manager.updateRoleActionPointIfChanged(user, cal.getTimeInMillis());
		//should changed now
		assertEquals(true, checked);
	}
	
	@Test
	public void testUserLevelUpRoleActionAdd10() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);

		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		
		int increaseLevelup = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_LEVELUP, 50);
		
		Calendar cal = Calendar.getInstance();

		//consume 10 points
		boolean success = manager.consumeRoleActionPoint(user, increaseLevelup+10, cal.getTimeInMillis());
		assertTrue(success);
		
		boolean checked = manager.userLevelUpRoleActionGrow(user, cal.getTimeInMillis(), false);
		//should changed now
		assertEquals(true, checked);
		
		int current = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		
		assertEquals(10, current);
	}
	
	@Test
	public void testUserLevelUpRoleActionSub10() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);

		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		
		int increaseLevelup = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.ROLE_ACTION_GAIN_LEVELUP, 50);
		
		Calendar cal = Calendar.getInstance();

		//consume 10 points
		boolean success = manager.consumeRoleActionPoint(user, increaseLevelup-10, cal.getTimeInMillis());
		assertTrue(success);
		
		boolean checked = manager.userLevelUpRoleActionGrow(user, cal.getTimeInMillis(), false);
		//should changed now
		assertEquals(true, checked);
		
		int current = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		
		assertEquals(0, current);
	}
	
	@Test
	public void testUserLevelUpRoleActionBuy() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);

		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		user.setIsvip(true);
		user.setViplevel(10);
		
		Calendar cal = Calendar.getInstance();

		//consume 10 points
		boolean success = manager.buyRoleActionPoint(user, cal.getTimeInMillis());
		assertTrue(success);
		
		boolean checked = manager.userLevelUpRoleActionGrow(user, cal.getTimeInMillis(), false);
		//should changed now
		assertEquals(false, checked);
		
		int current = manager.getRoleActionPoint(user, cal.getTimeInMillis());
		
		assertTrue(current<0);
	}
	
	@Test
	public void testConsumeLastPoint() {
		Jedis jedis = JedisFactory.getJedisDB();
		RoleActionManager manager = RoleActionManager.getInstance();
		String keyName = RoleActionManager.getInstance().getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);

		User user = new User();
		user.setUsername(roleName);
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		
		Calendar cal = Calendar.getInstance();
		
		int point = user.getRoleTotalAction();

		//consume nearly all points but 5
		boolean success = manager.consumeRoleActionPoint(user, point-5, cal.getTimeInMillis());
		assertTrue(success);
		
		//consume the last 5 points. 
		//user's roleaction should be 0 now
		success = manager.consumeRoleActionPoint(user, 5, cal.getTimeInMillis());
		assertTrue(success);
		success = manager.checkUserHasRoleActionPoint(user);
		assertTrue(success);
		
		//user's roleaction should be < 0 now
		success = manager.consumeRoleActionPoint(user, 5, cal.getTimeInMillis());
		assertFalse(success);
		success = manager.checkUserHasRoleActionPoint(user);
		assertFalse(success);
		
	}
}
