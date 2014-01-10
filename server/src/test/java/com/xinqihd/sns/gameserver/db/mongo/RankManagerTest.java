package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager.Field;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class RankManagerTest {

	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetRankSetName() {
		String zsetName = RankManager.getRankSetName(null, RankFilterType.TOTAL, 
				RankScoreType.POWER);
		assertEquals("RANK:GLOBAL:TOTAL:POWER", zsetName);
	}
	
	@Test
	public void testCalculateSecondsTotal() {
		Calendar current = Calendar.getInstance();
		int seconds = RankManager.calculateExpireSecond(RankFilterType.TOTAL, current);
		assertEquals(Integer.MAX_VALUE, seconds);
	}

	@Test
	public void testCalculateSecondsDaily() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 9, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = RankManager.calculateExpireSecond(RankFilterType.DAILY, current);

		Calendar expect = Calendar.getInstance();
		expect.set(2012, 1, 10, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsDaily2() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 O:0:0
		current.set(2012, 1, 9, 0, 0, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = RankManager.calculateExpireSecond(RankFilterType.DAILY, current);

		Calendar expect = Calendar.getInstance();
		expect.set(2012, 1, 10, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsMonthly() {
		DateUtil.resetInternalDate();
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 9, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = RankManager.calculateExpireSecond(RankFilterType.MONTHLY, current);
		
		Calendar expect = Calendar.getInstance();
		expect.set(2012, 2, 1, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsMonthly2() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 29, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = RankManager.calculateExpireSecond(RankFilterType.MONTHLY, current);
		
		Calendar expect = Calendar.getInstance();
		expect.set(2012, 2, 1, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testStoreDataInZSet() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String zsetName = RankManager.getRankSetName(null, RankFilterType.TOTAL, 
				RankScoreType.POWER);
		RankManager manager = RankManager.getInstance();
		String[] members = {"001", "002", "003", "004"};
		int[] scores =     { 5000, 4000, 6000, 2000, 1000};
		
		for ( int i=0; i<members.length; i++ ) {
			User user = new User();
			user.setUsername(members[i]);
			boolean result = manager.storeDataInZSet(
					zsetName, user, scores[i], RankType.GLOBAL, RankScoreType.POWER, 
					RankFilterType.FIVE_SECONDS, System.currentTimeMillis());
			assertEquals(true, result);
		}
		assertTrue( ((Long)jedisDB.ttl(zsetName)).intValue() <= 5 );
		Set<String> memberSet = jedisDB.zrevrange(zsetName, 0, -1);
		assertEquals(members.length, memberSet.size());
		assertEquals("003", memberSet.iterator().next());
		System.out.println(memberSet);
		
//		Thread.sleep(6000);
//		assertFalse(jedisDB.exists(zsetName));
	}
	
	@Test
	public void testQueryDataInZSet() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String zsetName = RankManager.getRankSetName(null, RankFilterType.TOTAL, 
				RankScoreType.POWER);
		RankManager manager = RankManager.getInstance();
		String[] members = {"001", "002", "003", "004"};
		int[] scores =     { 5000, 4000, 6000, 2000, 1000};
		
		for ( int i=0; i<members.length; i++ ) {
			User user = new User();
			user.setUsername(members[i]);
			boolean result = manager.storeDataInZSet(zsetName, 
					user, scores[i], RankType.GLOBAL, RankScoreType.POWER, 
					RankFilterType.FIVE_SECONDS, System.currentTimeMillis());
			assertEquals(true, result);
		}
		
		Set<String> memberSet = jedisDB.zrevrange(zsetName, 0, -1);
		System.out.println(memberSet);
		assertEquals(members.length, memberSet.size());
		int rank = manager.queryDataRankInZSet(zsetName, members[2]);
		assertEquals(1, rank);
		rank = manager.queryDataRankInZSet(zsetName, members[0]);
		assertEquals(2, rank);
	}
	
	@Test
	public void testQueryDataInZSetEmpty() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String zsetName = RankManager.getRankSetName(null, RankFilterType.TOTAL, 
				RankScoreType.POWER);
		RankManager manager = RankManager.getInstance();

		int rank = manager.queryDataRankInZSet(zsetName, "non-exist-memeber");
		System.out.println(rank);
		
		assertEquals(0, rank);
	}
	
	@Test
	public void testStoreRankData() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			
			rankManager.storeGlobalRankData(users[i], RankScoreType.POWER, System.currentTimeMillis());
		}
		
		Collection<RankUser> rankUsers = rankManager.
				getAllRankUsers(users[0], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER, 
						0, 5, null);
		
		for ( RankUser user : rankUsers ) {
			System.out.println(user);
		}
		
		assertEquals(6, rankUsers.size());
		
		int index = 9;
		for ( RankUser user : rankUsers ) {
			assertEquals("test-00"+(index--), user.getBasicUser().getUsername());
		}
	}
	
	@Test
	public void testStoreBossRankData() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			
			rankManager.storeBossHurtRankData(users[i], "ice_boss", i*100, System.currentTimeMillis());
		}
		
		Collection<RankUser> rankUsers = rankManager.
				getAllRankUsers(users[0], RankType.PVE, RankFilterType.TOTAL,
						RankScoreType.PVE, 0, 5, "ice_boss");
		
		for ( RankUser user : rankUsers ) {
			System.out.println(user);
		}
		
		assertEquals(6, rankUsers.size());
		
		int index = 9;
		for ( RankUser user : rankUsers ) {
			assertEquals("test-00"+(index--), user.getBasicUser().getUsername());
		}
	}
	
	@Test
	public void testQueryCurrentRankUser() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setYuanbao( i * 100 );			
		}
		
		Collection<RankUser> rankUsers = rankManager.
				getAllRankUsers(users[0], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, 
						0, 5, null);
		
		for ( RankUser user : rankUsers ) {
			System.out.println(user);
		}
		
		RankUser rankUser = rankManager.queryUserCurrentRank(users[3], 
				RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		System.out.println("total rank: " + rankUser.getRank());
		System.out.println("total score: " + rankUser.getScore());
		rankUser = rankManager.queryUserCurrentRank(users[3], 
				RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
		System.out.println("daily rank: " + rankUser.getRank());
		System.out.println("daily score: " + rankUser.getScore());
		rankUser = rankManager.queryUserCurrentRank(users[3], 
				RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
		System.out.println("monthly rank: " + rankUser.getRank());
		System.out.println("monthly score: " + rankUser.getScore());

		int index = 9;
		for ( RankUser user : rankUsers ) {
			assertEquals("test-00"+(index--), user.getBasicUser().getUsername());
		}
	}
	
	@Test
	public void testStoreUserYuanbaoRank() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User user = prepareUser("test-001");
		//Should store the highest history rank
		user.setYuanbao(10000);
		RankUser totalRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		assertEquals(1, totalRankUser.getRank());
		assertEquals(10000, totalRankUser.getScore());
		RankUser monthlyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
		assertEquals(1, monthlyRankUser.getRank());
		assertEquals(10000, monthlyRankUser.getScore());
		RankUser dailyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
		assertEquals(1, dailyRankUser.getRank());
		assertEquals(10000, dailyRankUser.getScore());
		
		//Deduce the yuanbao. The rank should not change
		user.setYuanbao(1000);
		totalRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		assertEquals(1, totalRankUser.getRank());
		assertEquals(10000, totalRankUser.getScore());
		monthlyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
		assertEquals(1, monthlyRankUser.getRank());
		assertEquals(10000, monthlyRankUser.getScore());
		dailyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
		assertEquals(1, dailyRankUser.getRank());
		assertEquals(10000, dailyRankUser.getScore());
		
		//Change the daily record
		Jedis jedisDB = JedisFactory.getJedisDB();
		String zsetName = rankManager.getRankSetName(null, RankFilterType.DAILY, RankScoreType.WEALTH);
		jedisDB.zadd(zsetName, 1000, user.getUsername());
		user.setYuanbao(2000);
		
		totalRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		assertEquals(1, totalRankUser.getRank());
		assertEquals(10000, totalRankUser.getScore());
		monthlyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
		assertEquals(1, monthlyRankUser.getRank());
		assertEquals(10000, monthlyRankUser.getScore());
		dailyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
		assertEquals(1, dailyRankUser.getRank());
		assertEquals(2000, dailyRankUser.getScore());
		
		//Change the monthly record
		zsetName = rankManager.getRankSetName(null, RankFilterType.MONTHLY, RankScoreType.WEALTH);
		jedisDB.zadd(zsetName, 1000, user.getUsername());
		user.setYuanbao(5000);
		
		totalRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH, null);
		assertEquals(1, totalRankUser.getRank());
		assertEquals(10000, totalRankUser.getScore());
		monthlyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.MONTHLY, RankScoreType.WEALTH, null);
		assertEquals(1, monthlyRankUser.getRank());
		assertEquals(5000, monthlyRankUser.getScore());
		dailyRankUser = rankManager.queryUserCurrentRank(user, RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.WEALTH, null);
		assertEquals(1, dailyRankUser.getRank());
		assertEquals(5000, dailyRankUser.getScore());
	}
	
	@Test
	public void testStoreRankDataPerformance() throws Exception {
		final RankManager rankManager = RankManager.getInstance();
		final int times = 10;
		final Random random = new Random();
		TestUtil.doPerform(new Runnable() {
			public void run() {
				User user = new User();
				int r = random.nextInt();
				user.setUsername("test-"+r);
				rankManager.storeGlobalRankData(user, RankScoreType.POWER, System.currentTimeMillis());
			}
		}, "Store", times);
		
	}

	
	@Test
	public void testStoreRankDataMultiData() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[5];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 10 );
			users[i].updatePowerRanking();
			
			users[i].setLevel(i*20);
			users[i].setYuanbao(i*30);
			users[i].setMedal(i*40);
			
			rankManager.storeGlobalRankData(users[i], RankScoreType.POWER, System.currentTimeMillis());
			rankManager.storeGlobalRankData(users[i], RankScoreType.KILL, System.currentTimeMillis());
			rankManager.storeGlobalRankData(users[i], RankScoreType.MEDAL, System.currentTimeMillis());
			rankManager.storeGlobalRankData(users[i], RankScoreType.WEALTH, System.currentTimeMillis());
		}
		
		{//power
			Collection<RankUser> rankUsers = rankManager.
					getAllRankUsers(users[0], RankType.GLOBAL, 
							RankFilterType.TOTAL, RankScoreType.POWER, 
							0, 5, null);
			
			for ( RankUser user : rankUsers ) {
				System.out.println(user);
			}
			
			assertEquals(5, rankUsers.size());
		}
		{//wealth
			Collection<RankUser> rankUsers = rankManager.
					getAllRankUsers(users[0], RankType.GLOBAL, 
							RankFilterType.TOTAL, RankScoreType.WEALTH, 
							0, 5, null);
			
			for ( RankUser user : rankUsers ) {
				System.out.println(user);
			}
			
			assertEquals(5, rankUsers.size());
		}
		{//medal
			Collection<RankUser> rankUsers = rankManager.
					getAllRankUsers(users[0], RankType.GLOBAL, 
							RankFilterType.TOTAL, RankScoreType.MEDAL, 
							0, 5, null);
			
			for ( RankUser user : rankUsers ) {
				System.out.println(user);
			}
			
			assertEquals(5, rankUsers.size());
		}
		{//level
			Collection<RankUser> rankUsers = rankManager.
					getAllRankUsers(users[0], RankType.GLOBAL, 
							RankFilterType.TOTAL, RankScoreType.KILL, 
							0, 5, null);
			
			for ( RankUser user : rankUsers ) {
				System.out.println(user);
			}
			
			assertEquals(5, rankUsers.size());
		}
		
	}
	
	@Test
	public void testGetCurrentUserRank() throws Exception {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			users[i].updatePowerRanking();
			
			RankUser rankUser = rankManager.getCurrentRankUser(
					users[0], RankType.GLOBAL, RankFilterType.DAILY, RankScoreType.POWER);
			
			System.out.println("rank: " + rankUser.getRank());
			System.out.println("rank change: " + rankUser.getRankChange());
		}
		
		RankUser rankUser = rankManager.getCurrentRankUser(
				users[0], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		
		assertNotNull(rankUser);
		/**
		 * 10 not 9 because the default create have one place.
		 */
		assertEquals(10, rankUser.getRank());
		assertEquals(9, rankUser.getRankChange());
		
		//Make him the first one
		users[0].setPower( 10000 );
		users[0].updatePowerRanking();
		
		rankUser = rankManager.getCurrentRankUser(
				users[0], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertEquals(1, rankUser.getRank());
		assertEquals(-9, rankUser.getRankChange());
		
		System.out.println(rankUser);
		assertEquals("test-000", rankUser.getBasicUser().getUsername());
	}
	
	@Test
	public void testGetUserSpecificDataKey() throws Exception {
		String userName = "test-001";
		User user = new User();
		user.setUsername(userName);
		String keyName = RankManager.getUserSpecificDataKeyName(userName);
		assertEquals("RANK:"+userName, keyName);
	}
	
	@Test
	public void testStoreUserSpecificData() throws Exception {
		RankManager manager = RankManager.getInstance();
		String userName = "test-001";
		User user = new User();
		user.setUsername(userName);
		Calendar cal = Calendar.getInstance();
		String value = DateUtil.getYesterday(cal.getTimeInMillis());
		//Test if it will cause an exception
		String actual = manager.queryUserSpecificData(userName, RankScoreType.POWER, Field.LAST_DAY_RANK);
		assertEquals(null, actual);
		
		manager.storeUserSpecificData(userName, RankScoreType.POWER, Field.LAST_DAY_RANK, value);
		//Test if it is matched.
		actual = manager.queryUserSpecificData(userName, RankScoreType.POWER, Field.LAST_DAY_RANK);
		
		assertEquals(value, actual);
		
		//Test clear
		manager.clearUserSpecificData(userName);
		actual = manager.queryUserSpecificData(userName, RankScoreType.POWER, Field.LAST_DAY_RANK);
		assertEquals(null, actual);
	}
	
	/**
	 * This test case is disabled because the 'User.setPower' is already
	 * called "#storeGlobalRankData" method and the current time is set 
	 * to System.currentTimeMillis(). It cannot be simulated.
	 * 
	 * @throws Exception
	 */
	public void testStoreTotalRankDataAndQuery() throws Exception {
		RankManager manager = RankManager.getInstance();
		User[] users = new User[10];
		
		Calendar cal = Calendar.getInstance();
		//2012-2-10 14:13
		cal.set(2012, 1, 10, 14, 31, 0);
		
		//Generate the top list
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			
			manager.storeGlobalRankData(users[i], RankScoreType.POWER, cal.getTimeInMillis());
		}
		
		//Insert mine power
		User mine = prepareUser("wangqi");
		manager.clearUserSpecificData(mine.getUsername());
		
		mine.setPower( 50 );
		manager.storeGlobalRankData(mine, RankScoreType.POWER, cal.getTimeInMillis());
		String zsetname = RankManager.getRankSetName(null, RankFilterType.TOTAL, 
				RankScoreType.POWER);
		int myRank = manager.queryUserCurrentRank(zsetname, mine);
		//The last rank user.
		assertEquals(users.length, myRank);
		
		//Check my last rank in the same day
		int totalLastRank = manager.queryUserPassDayRank(RankFilterType.TOTAL, RankScoreType.POWER, mine.getUsername());
		int dailyLastRank = manager.queryUserPassDayRank(RankFilterType.DAILY, RankScoreType.POWER, mine.getUsername());
		int monthLastRank = manager.queryUserPassDayRank(RankFilterType.MONTHLY, RankScoreType.POWER, mine.getUsername());
		assertEquals(0, totalLastRank);
		assertEquals(0, dailyLastRank);
		assertEquals(0, monthLastRank);
		
		//Update my power in the same day
		mine.setPower( 500 );
		manager.storeGlobalRankData(mine, RankScoreType.POWER, cal.getTimeInMillis());
		totalLastRank = manager.queryUserPassDayRank(RankFilterType.TOTAL, RankScoreType.POWER, mine.getUsername());
		dailyLastRank = manager.queryUserPassDayRank(RankFilterType.DAILY, RankScoreType.POWER, mine.getUsername());
		monthLastRank = manager.queryUserPassDayRank(RankFilterType.MONTHLY, RankScoreType.POWER, mine.getUsername());
		assertEquals(10, totalLastRank);
		assertEquals(0, dailyLastRank);
		assertEquals(0, monthLastRank);
		myRank = manager.queryUserCurrentRank(zsetname, mine);
		assertEquals(5, myRank);
		
		//Update my power in the next day
		cal.add(Calendar.DAY_OF_MONTH, 1);
		mine.setPower( 1500 );
		manager.storeGlobalRankData(mine, RankScoreType.POWER, cal.getTimeInMillis());
		totalLastRank = manager.queryUserPassDayRank(RankFilterType.TOTAL, RankScoreType.POWER, mine.getUsername());
		dailyLastRank = manager.queryUserPassDayRank(RankFilterType.DAILY, RankScoreType.POWER, mine.getUsername());
		monthLastRank = manager.queryUserPassDayRank(RankFilterType.MONTHLY, RankScoreType.POWER, mine.getUsername());
		assertEquals(5, totalLastRank);
		assertEquals(5, dailyLastRank);
		assertEquals(0, monthLastRank);
		myRank = manager.queryUserCurrentRank(zsetname, mine);
		assertEquals(1, myRank);
		
		//Update my power in the next month
		cal.add(Calendar.MONTH, 1);
		mine.setPower( 800 );
		manager.clearUserSpecificData(mine.getUsername());
		manager.storeGlobalRankData(mine, RankScoreType.POWER, cal.getTimeInMillis());
		totalLastRank = manager.queryUserPassDayRank(RankFilterType.TOTAL, RankScoreType.POWER, mine.getUsername());
		dailyLastRank = manager.queryUserPassDayRank(RankFilterType.DAILY, RankScoreType.POWER, mine.getUsername());
		monthLastRank = manager.queryUserPassDayRank(RankFilterType.MONTHLY, RankScoreType.POWER, mine.getUsername());
		assertEquals(1, totalLastRank);
		assertEquals(1, dailyLastRank);
		assertEquals(1, monthLastRank);
		myRank = manager.queryUserCurrentRank(zsetname, mine);
		assertEquals(2, myRank);
		
		Collection<RankUser> rankUsers = manager.
				getAllRankUsers(mine, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER, 
						0, 5, null);
		
		for ( RankUser user : rankUsers ) {
			System.out.println(user);
		}
		
		assertEquals(6, rankUsers.size());
	}
	
	@Test
	public void testSendRankNotify() {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			users[i].updatePowerRanking();
		}
		
		RankUser rankUser = rankManager.getCurrentRankUser(
				users[2], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertEquals(8, rankUser.getRank());
		
		users[2].setPower( users[2].getPower() + 600 );
		
		rankUser = rankManager.getCurrentRankUser(
				users[8], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertEquals(2, rankUser.getRank());
	}
	
	@Test
	public void testSendRankNotifyWealth() {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setYuanbao( i * 100 );
		}
		
		RankUser rankUser = rankManager.getCurrentRankUser(
				users[2], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH);
		assertEquals(8, rankUser.getRank());
		
		users[2].setYuanbao( users[2].getPower() + 600 );
		
		rankUser = rankManager.getCurrentRankUser(
				users[8], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.WEALTH);
		assertEquals(2, rankUser.getRank());
	}
	
	@Test
	public void testSendRankNotifyDown() {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			users[i].updatePowerRanking();
		}
		
		RankUser rankUser = rankManager.getCurrentRankUser(
				users[8], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertEquals(2, rankUser.getRank());
		
		users[8].setPower( users[8].getPower() - 600 );
		users[8].updatePowerRanking();
		
		rankUser = rankManager.getCurrentRankUser(
				users[8], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertEquals(7, rankUser.getRank());

//		System.out.println(message);
		
//		assertTrue(message.startsWith("哎呀"));
	}
	
	@Test
	public void testGetTotalRankUserCount() {
		RankManager manager = RankManager.getInstance();
		
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			
			manager.storeGlobalRankData(
					users[i], RankScoreType.POWER, System.currentTimeMillis());
		}
		int count = manager.getTotalRankUserCount(null, RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER);
		assertTrue(count>=10);
	}
	
	@Test
	public void testGetAllRankUsers() {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			users[i].updatePowerRanking();
		}
		
		Collection<RankUser> rankUsers = rankManager.getAllRankUsers(
				users[2], RankType.GLOBAL, RankFilterType.TOTAL, RankScoreType.POWER, 0, -1, null);
		assertTrue(rankUsers.size()>=10);
		
		for ( RankUser rankUser : rankUsers ) {
			System.out.println(rankUser);
		}
	}
	
	@Test
	public void testGetAllRankUsersFriend() {
		RankManager rankManager = RankManager.getInstance();
		User[] users = new User[10];
		
		for (int i=0; i<users.length; i++) {
			users[i] = prepareUser("test-00"+i);
			users[i].setPower( i * 100 );
			users[i].updatePowerRanking();
		}
		
		Relation relation = new Relation();
		relation.setType(RelationType.FRIEND);
		for (int i=0; i<=6; i+=2) {
			People p = new People();
			p.setUsername("test-00"+i);
			relation.addPeople(p);
		}
		users[5].addRelation(relation);
		
		Collection<RankUser> rankUsers = rankManager.getAllRankUsers(
				users[5], RankType.FRIEND, RankFilterType.TOTAL, RankScoreType.POWER, 0, -1, null);
		assertEquals(5, rankUsers.size());
		
		int i = 1;
		for ( RankUser rankUser : rankUsers ) {
			System.out.println(rankUser);
			assertEquals(i++, rankUser.getRank());
		}
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
