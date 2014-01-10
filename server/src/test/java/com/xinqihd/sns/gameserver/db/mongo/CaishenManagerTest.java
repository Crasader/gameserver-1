package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

public class CaishenManagerTest {

	private String roleName = "test-001";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testQueryCaishenPrayNormal() {
		Jedis jedis = JedisFactory.getJedisDB();
		CaishenManager manager = CaishenManager.getInstance();
		String keyName = CaishenManager.getInstance().
				getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setRoleName(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		int[] results = manager.queryCaishenPrayInfo(user, cal.getTimeInMillis(), false);
	  //当日可用的购买次数
		int buyCount = results[0];
		//购买的价格
		int buyPrice = results[1];
		//每次购买增加的金币数量
		int buyValue = results[2];
		//当日已经购买的次数，用来计算价格
		int buyTimes = results[3];
		System.out.println(Arrays.toString(results));
		assertTrue(buyCount==1);
	}
	
	@Test
	public void testQueryCaishenPrayNormalPray() {
		Jedis jedis = JedisFactory.getJedisDB();
		CaishenManager manager = CaishenManager.getInstance();
		String keyName = CaishenManager.getInstance().
				getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		SecureLimitManager.getInstance().setDisableSecureChecking(true);
		
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		boolean success = manager.prayGolden(user, cal.getTimeInMillis());
		assertTrue("should allow to pray", success);
		assertTrue(user.getGolden()>0);
		
		success = manager.prayGolden(user, cal.getTimeInMillis());
		assertTrue("should disallow to pray", !success);
	}

	@Test
	public void testQueryCaishenPrayNormalPrayCrossDay() {
		Jedis jedis = JedisFactory.getJedisDB();
		CaishenManager manager = CaishenManager.getInstance();
		String keyName = CaishenManager.getInstance().
				getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		user.setRoleTotalAction(100);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		boolean success = manager.prayGolden(user, cal.getTimeInMillis());
		assertTrue("should allow to pray", success);

		success = manager.prayGolden(user, cal.getTimeInMillis());
		assertTrue("should disallow to pray", !success);
		
		cal.add(Calendar.DAY_OF_MONTH, 1);
		success = manager.prayGolden(user, cal.getTimeInMillis());
		assertTrue("should allow to pray next day", success);
	}
	
	@Test
	public void testQueryCaishenPrayVIP() {
		Jedis jedis = JedisFactory.getJedisDB();
		CaishenManager manager = CaishenManager.getInstance();
		String keyName = CaishenManager.getInstance().
				getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.setIsvip(true);
		user.setViplevel(7);
		user.setRoleName(roleName);
		user.setYuanbaoFreeSecure(100);
		Calendar cal = Calendar.getInstance();
		int[] results = manager.queryCaishenPrayInfo(user, cal.getTimeInMillis(), false);
	  //当日可用的购买次数
		int buyCount = results[0];
		//购买的价格
		int buyPrice = results[1];
		//每次购买增加的金币数量
		int buyValue = results[2];
		//当日已经购买的次数，用来计算价格
		int buyTimes = results[3];
		System.out.println(Arrays.toString(results));
		assertTrue(buyCount>10);
	}
	
	@Test
	public void testQueryCaishenPrayVIPPrice() {
		Jedis jedis = JedisFactory.getJedisDB();
		CaishenManager manager = CaishenManager.getInstance();
		String keyName = CaishenManager.getInstance().
				getDailyLimitManager().getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setUsername(roleName);
		user.setIsvip(true);
		user.setViplevel(7);
		user.setRoleName(roleName);
		user.setYuanbaoFreeSecure(10000);
		Calendar cal = Calendar.getInstance();
		int[] results = manager.queryCaishenPrayInfo(user, cal.getTimeInMillis(), false);
	  //当日可用的购买次数
		int buyCount = results[0];
		//购买的价格
		int buyPrice = results[1];
		//每次购买增加的金币数量
		int buyValue = results[2];
		//当日已经购买的次数，用来计算价格
		int buyTimes = results[3];
		
		assertTrue("VIP should has more than one changes to pray.", buyCount>0);
		int totalGolden = 0;
		for ( int i=0; i<buyCount; i++ ) {
			boolean success = manager.prayGolden(user, cal.getTimeInMillis());
			assertTrue(success);
			results = manager.queryCaishenPrayInfo(user, cal.getTimeInMillis(), false);
			//当日可用的购买次数
			int nextBuyCount = results[0];
			//购买的价格
			int nextBuyPrice = results[1];
			//每次购买增加的金币数量
			int nextBuyValue = results[2];
			//print
			System.out.println(Arrays.toString(results));
			System.out.println("yuanbao: "+user.getYuanbao());
			assertTrue(nextBuyCount<buyCount);
			assertTrue(nextBuyPrice>=buyPrice);
			totalGolden+=nextBuyValue;
		}
		System.out.println("totalGolden="+totalGolden);
	}
}
