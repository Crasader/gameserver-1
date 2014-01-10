package com.xinqihd.sns.gameserver.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionManager;

public class JedisUtilTest {
	
	String zsetName = "zsettest";

	@Before
	public void setUp() throws Exception {
		JedisFactory.initJedis();
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public void testPopKeyFromZset() throws Exception {
		Pipeline pipeline = JedisFactory.getJedis().pipelined();
		Random r = new Random();
		int max = 10000;
		for ( int i=0; i<max; i++ ) {
			pipeline.zadd(zsetName, r.nextInt(), "value_"+i);
		}
		pipeline.sync();
		final AtomicInteger counter = new AtomicInteger(0);
		
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					Tuple pop = JedisUtil.popKeyFromZset(zsetName);
					while ( pop != null ) {
//					System.out.println(pop.getScore() + ":" + pop.getElement());
						pop = JedisUtil.popKeyFromZset(zsetName);
						counter.incrementAndGet();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		});
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				try {
					Tuple pop = JedisUtil.popKeyFromZset(zsetName);
					while ( pop != null ) {
//					System.out.println(pop.getScore() + ":" + pop.getElement());
						pop = JedisUtil.popKeyFromZset(zsetName);
						counter.incrementAndGet();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		});
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
		
		Thread.sleep(1000);
		System.out.println(counter);
		
		assertEquals(max, counter.intValue());
	}
	
	@Test
	public void testPopKeyFromZsetUnique() throws Exception {
		for ( int t=0; t<100; t++ ) {
			Pipeline pipeline = JedisFactory.getJedis().pipelined();
			Random r = new Random();
			int max = 100;
			for ( int i=0; i<max; i++ ) {
				pipeline.zadd(zsetName, r.nextInt(), "value_"+i);
			}
			pipeline.sync();
			
			final ArrayList<String> t1List = new ArrayList<String>();
			final ArrayList<String> t2List = new ArrayList<String>();
			
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					try {
						Tuple pop = JedisUtil.popKeyFromZset(zsetName);
						while ( pop != null ) {
							t1List.add(pop.getElement());
							pop = JedisUtil.popKeyFromZset(zsetName);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}	
				}
			});
			Thread t2 = new Thread(new Runnable() {
				public void run() {
					try {
						Tuple pop = JedisUtil.popKeyFromZset(zsetName);
						while ( pop != null ) {
							t2List.add(pop.getElement());
							pop = JedisUtil.popKeyFromZset(zsetName);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}	
				}
			});
			
			t1.start();
			t2.start();
			
			t1.join();
			t2.join();
			
			System.out.println("t1List.size: " + t1List.size());
			System.out.println("t2List.size: " + t2List.size());
			
			assertEquals(max, t1List.size() + t2List.size());
			for ( int i=0; i<t1List.size(); i++ ) {
				String element = t1List.get(i);
				assertFalse( t2List.contains(element) );
			}
		}
	}

	@Test
	public void testDeleteAll() {
		JedisUtil.deleteAllKeys();
	}
	
	@Test
	public void testCleanJedis() {
//		String expectRpcServerId = "redis.xinqihd.com:3445";
		String expectRpcServerId = "localhost:3445";
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_GAME_SERVERID, expectRpcServerId);
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_AI_SERVERID, expectRpcServerId);
		GlobalConfig.getInstance().overrideProperty("zookeeper.root", "/snsgame/babywar");
		
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
				"../deploy/data");
		AbstractTest test = new AbstractTest();
		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		GameContext.getTestInstance().reloadContext();
		BattleDataLoader4Bitmap.loadBattleMaps();
		
		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");
		
		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
//		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for ( int i=0; i<1; i++ ) {
			user1.addTool(makeBuffTool(i));
		}
		user1.setBag(makeBag(user1, 3));
		
		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
//		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for ( int i=0; i<1; i++ ) {
			user2.addTool(makeBuffTool(i));
		}
		user2.setBag(makeBag(user2, 3));
		
		manager.saveUser(user1, true);
		manager.saveUser(user2, true);
		
		SessionManager sessionManager = new SessionManager();
		
		IoSession session1 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session1, user1);
		
		IoSession session2 = createNiceMock(IoSession.class);
		sessionManager.registerSession(session2, user2);
		
		BattleManager battleManager = BattleManager.getInstance();
		battleManager.clearAllBattles();
		
		//Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);
		
		Collection<Battle> battles = battleManager.findAllBattles();
		assertEquals(1, battles.size());
		Battle battle = battles.iterator().next();
		
		//Clean the redis
		JedisUtil.cleanRedis();
		
		Jedis jedis = JedisFactory.getJedis();
		assertEquals(null, jedis.get(user1.getSessionKey().getRawKey()));
		assertEquals(null, jedis.get(user2.getSessionKey().getRawKey()));
		assertEquals(null, jedis.get(battle.getBattleSessionKey().getRawKey()));
		assertEquals(null, jedis.get(battle.getBattleRoom().getSessionKey().getRawKey()));
		assertEquals(null, jedis.get(battle.getBattleRoom().getRoomLeft().getRoomSessionKey().getRawKey()));
		assertEquals(null, jedis.get(battle.getBattleRoom().getRoomRight().getRoomSessionKey().getRawKey()));
	}
	
	@Test
	public void testExpireKey() throws Exception {
		String key = "orderset";
		String[] values = {"1", "2", "3"};
		int second = 2;
		
		Jedis jedis = JedisFactory.getJedis();
		jedis.zadd(key, 1, values[0]);
		long result = jedis.expire(key, second);
		assertEquals(1, result);
		
		Thread.sleep(second*1000+1000);
		assertFalse(jedis.exists(key));
		
		//Set it again
		jedis.zadd(key, 1, values[0]);
		result = jedis.expire(key, second);
		jedis.zadd(key, 2, values[1]);
		assertEquals(2l, (long)jedis.ttl(key));
		assertEquals(1, result);
		Thread.sleep(1000);
		//update it
		jedis.zadd(key, 3, values[2]);
		assertEquals(3l, (long)jedis.zcard(key) );
		assertEquals(1l, (long)jedis.ttl(key));
		//wait for another time
		Thread.sleep(second*1000/2+1000);
		
		//check it
		assertFalse(jedis.exists(key));
	}
	
	@Test
	public void testZRankNonExist() throws Exception {
		String key = "orderset";
		String[] values = {"1", "2", "3"};
		
		Jedis jedis = JedisFactory.getJedis();
		for ( int i = 0; i<values.length; i++ ) {
			jedis.zadd(key, 1, values[0]);
		}
		Long result = jedis.zrank(key, "non-exist");
		assertEquals(null, result);
	}
	
	// ------------------------------------------------- TOOLS
	
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
		propData.setDuration(1003);
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
