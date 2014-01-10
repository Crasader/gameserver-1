package com.xinqihd.sns.gameserver.boss;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.cron.BossUtil;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;

public class BossManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAddBoss() {
		String id = "ice_boss_easy";
		String bossId = "ice_boss";
		String name = "雪兔";
		String title = "击杀雪兔";
		String desc = "冰冷极地中，雪兔魔抓走了宝贝大陆去采集雪晶的人们，勇士们，去打败雪兔魔救宝贝大陆的子民吧。单次战斗中伤害雪兔2000点血量视为一次挑战成功。";
		String target = "单次战斗中伤害雪兔2000点血量视为一次挑战成功";
		BossType bossType = BossType.WORLD;
		BossWinType bossWinType = BossWinType.KILL_ONE;
		/**
		 * 19 冰天雪地
		 */
		String mapId = "19";
		int blood = 500000;
		int level = 18;
		int width = 100;
		int height = 100;
		int hurtRadius = 100;
	  //10000	BOSS雪兔	Suit1000
		String suitPropId = "10000";
		int minUserLevel = 1;
		int maxUserLevel = 100;
		int requiredGolden = 2000;
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.DAY_OF_MONTH, 3);
		
		/**
		 * 金币
		 */
		Reward reward = new Reward();
		reward.setPropId("-1");
		reward.setPropLevel(-1);
		reward.setType(RewardType.GOLDEN);
		reward.setPropCount(50000);
		/**
		 * 29021	寻宝卡	Prop10212	使用后当天可以增加5次免费寻宝机会
		 */
		ItemPojo item = ItemManager.getInstance().getItemById("29021");
		Reward cardReward = RewardManager.getInstance().getRewardItem(item);
		cardReward.setPropCount(5);
		Reward[] gifts = new Reward[2];
		gifts[0] = reward;
		gifts[1] = cardReward;
		int limit = 10;
		int increasePerHour = 5;
		int winProgress = 1000;
		
		String weaponId = "702";

		BossUtil.addNewBoss(id, bossId, name, title, desc, target, bossType, bossWinType, mapId,
				blood, level, width, height, hurtRadius, suitPropId, minUserLevel,
				maxUserLevel, requiredGolden, startCal, endCal, gifts, limit,
				increasePerHour, winProgress, blood,
				weaponId, ScriptHook.BOSS_ICE_RABBIT_ROLEATTACK, ScriptHook.BOSS_ICE_RABBIT_ROLEDEAD, 3);
	}
	
	@Test
	public void testGetAllBosses() {
		BossManager manager = BossManager.getInstance();
		Collection<BossPojo> bosses = manager.getAllBosses();
		for ( BossPojo boss : bosses ) {
			System.out.println(boss);
		}
	}
	
	@Test
	public void testChallengeBoss() {
		testAddBoss();
		User user = prepareUser();
		Calendar cal = Calendar.getInstance();
		
		BossManager manager = BossManager.getInstance();
		Boss boss = manager.getAllBossInstance(null).iterator().next();
		manager.resetChallengeBossCount(user, boss, cal.getTimeInMillis());
		
		int count = manager.getChallengeCount(user, boss, cal.getTimeInMillis());
		assertEquals(10, count);
		for ( int i=0; i<count; i++ ) {
			boolean success = manager.challengeBoss(user, boss, cal.getTimeInMillis());
			assertTrue("i="+i, success);
		}
		//No change
		boolean success = manager.challengeBoss(user, boss, cal.getTimeInMillis());
		assertTrue(!success);
		
		success = manager.resetChallengeBossCount(user, boss, cal.getTimeInMillis());
		assertTrue(success);
		count = manager.getChallengeCount(user, boss, cal.getTimeInMillis());
		assertEquals(10, count);
	}
	
	@Test
	public void testCreateBossInstance() {
		BossManager manager = BossManager.getInstance();
		BossPojo bossPojo = getBossPojo();
		Boss instance = getBossInstance(bossPojo);
		String bossStr = instance.toString();
		Boss actual = Boss.fromString(bossStr);
		
		System.out.println(bossStr);
		System.out.println(actual.toString());
		assertEquals(bossStr, actual.toString());
	}
	
	@Test
	public void testGetBossInstances() {
		Jedis jedisDB = JedisFactory.getJedisDB();
		BossManager manager = BossManager.getInstance();
		manager.removeAllBossInstance();
		
		Set<Boss> set = manager.getAllBossInstance(null);
		assertEquals(0, set.size());
		
		BossPojo boss = getBossPojo();
		Boss instance = getBossInstance(boss);
		
		manager.saveBossInstance(instance);
		Set<Boss> bosses = manager.getAllBossInstance(null);
		assertEquals(1, bosses.size());
		
		instance.setProgress(100);
		manager.updateBossInstance(instance);
		bosses = manager.getAllBossInstance(null);
		assertEquals(1, bosses.size());
		Boss actual = bosses.iterator().next();
		assertEquals(100, actual.getProgress());
		
		manager.deleteBossInstance(instance);
		bosses = manager.getAllBossInstance(null);
		assertEquals(0, bosses.size());

	}
	
	/**
	 * To test the transaction, you need insert a 'System.in.read();'
	 * into syncBossInstance method and change the boss instance 
	 * progress to 10000 before input any characters.
	 * 
	 * @throws Exception
	 */
	public void testSyncBossInstance() throws Exception {
		BossManager manager = BossManager.getInstance();
		/**
		 * Create a valid boss instance
		 */
		BossPojo bossPojo = manager.getAllBosses().iterator().next();
		Boss boss = getBossInstance(bossPojo);
		BossManager.getInstance().saveBossInstance(boss);
		
		Set<Boss> bosses = manager.getAllBossInstance(null);
		assertTrue(bosses.size()>0);
		
		Boss oldBoss = bosses.iterator().next();
		System.out.println("oldProgress:"+oldBoss.getProgress());
		System.out.println("oldTotalProgress:"+oldBoss.getTotalProgress());
		
		System.out.println("Now modify the bossId: " + boss.getId() + " from client");
		//Thread.sleep(10000);
		
		User user = prepareUser();
		Boss newBoss = manager.syncBossInstance(user, boss.getId(), 100000, 10);
		
		assertEquals(""+newBoss.getProgress(), 110000, newBoss.getProgress());
	}
	
	@Test
	public void testSyncBossBeatenMessage() throws Exception {
		BossManager manager = BossManager.getInstance();
		/**
		 * Create a valid boss instance
		 */
		BossPojo bossPojo = manager.getAllBosses().iterator().next();
		Boss boss = getBossInstance(bossPojo);
		BossManager.getInstance().saveBossInstance(boss);
		
		Set<Boss> bosses = manager.getAllBossInstance(null);
		assertTrue(bosses.size()>0);
		
		Boss oldBoss = bosses.iterator().next();
		System.out.println("oldProgress:"+oldBoss.getProgress());
		System.out.println("oldTotalProgress:"+oldBoss.getTotalProgress());
		
		User user = prepareUser();
		Boss newBoss = manager.syncBossInstance(user, boss.getId(), 100000, 10);
		for ( int i=0; i<100; i++ ) {
			RankManager.getInstance().storeBossHurtRankData(user, 
					newBoss.getBossId(), i*1000, System.currentTimeMillis());
		}
		
		manager.sendBossWinMessageToTop10(newBoss);
	}
	
	/**
	 * @param manager
	 * @param bossPojo
	 * @return
	 */
	private Boss getBossInstance(BossPojo bossPojo) {
		BossManager manager = BossManager.getInstance();
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.DAY_OF_MONTH, 3);
		Boss instance = manager.createBossInstance(bossPojo.getId(), 
				bossPojo, startCal, endCal);
		instance.setLimit(1000);
		instance.setIncreasePerHour(100);
		instance.setWinProgress(2000);
		instance.setBossStatusType(BossStatus.PROGRESS);
		System.out.println(instance.toString());
		return instance;
	}
	
	private BossPojo getBossPojo() {
		BossManager manager = BossManager.getInstance();
		Collection<BossPojo> bosses = manager.getAllBosses();
		BossPojo bossPojo = null;
		for ( BossPojo boss : bosses ) {
			bossPojo = boss;
		}
		return bossPojo;
	}
	
	private User prepareUser() {
		User user = new User();
		String roleName = "test-001";
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		user.setLevelSimple(10);
		UserManager.getInstance().removeUser(roleName);
		UserManager.getInstance().saveUser(user, true);
		return user;
	}
}
