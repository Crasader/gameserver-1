package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class GameDataManagerTest {
	
	GameDataManager manager = null;
	
	public GameDataManagerTest() {
		super();
		MongoDBUtil.dropCollection("testdb", null, "gamedata");
		GameDataManager.saveDefaultValue();
		manager = new GameDataManager("testdb", null, true);
	}

	@Before
	public void setUp() throws Exception {
		manager.reload();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetFromCache() throws Exception {
		ConcurrentHashMap<GameDataKey, Object> testMap = new ConcurrentHashMap<GameDataKey, Object>();
		TestUtil.setPrivateFieldValue("dataMap", manager, testMap);
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 0.15, 0.20);
		double k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 10.0);
		assertEquals(0.15, k, 0.001);
		assertEquals(0.15, testMap.get(GameDataKey.BATTLE_ATTACK_K));
		
		testMap.put(GameDataKey.BATTLE_ATTACK_K, 20.0);
		k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 11.0);
		assertEquals(20.0, k, 0.001);
	}
	
	@Test
	public void testGetGameDataAsDoubleArray() {
		double[] array = new double[]{1.0, 2.0, 3.0};
		manager.setValueToDatabase(GameDataKey.STRENGTH_STONE_RATIO, array, array);
		double[] actual = manager.getGameDataAsDoubleArray(GameDataKey.STRENGTH_STONE_RATIO);
		assertArrayEquals(array, actual, 0.01);
	}
	
	@Test
	public void testGetGameDataAsStringArray() {
		String[] array = new String[]{"07:30", "11:30", "14:30", "16:30", "18:30", "20:30", "22:30", "00:30"};
		manager.setValueToDatabase(GameDataKey.USER_ONLINE_REWARD_STEP, array, array);
		String[] actual = manager.getGameDataAsStringArray(GameDataKey.USER_ONLINE_REWARD_STEP);
		assertArrayEquals(array, actual);
	}

	@Test
	public void testGetGameDataAsInt() {
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 15, 20);
		int k = manager.getGameDataAsInt(GameDataKey.BATTLE_ATTACK_K, 10);
		assertEquals(15, k);
	}
	
	@Test
	public void testGetGameDataAsString() {
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 15, 20);
		String k = manager.getGameDataAsString(GameDataKey.BATTLE_ATTACK_K);
		assertEquals("15", k);
	}

	@Test
	public void testGetGameDataAsBoolean() {
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, true, false);
		boolean k = manager.getGameDataAsBoolean(GameDataKey.BATTLE_ATTACK_K, false);
		assertEquals(true, k);
	}

	@Test
	public void testSetValueToDatabase() {
		double k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 10.0);
		assertEquals(10.0, k, 0.001);
	}
	
	@Test
	public void testSetValueToDatabase2() {
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 0.15, 0.20);
		double k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 10.0);
		assertEquals(0.15, k, 0.001);
	}

	@Test
	public void testReload() {
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 0.15, 0.20);
		double k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 10.0);
		assertEquals(0.15, k, 0.001);
		
		manager.setValueToDatabase(GameDataKey.BATTLE_ATTACK_K, 0.25, 0.30);
		k = manager.getGameDataAsDouble(GameDataKey.BATTLE_ATTACK_K, 10.0);
		assertEquals(0.25, k, 0.001);
	}
	
	@Test
	public void testToBse() {
		manager.toBseGameDataKey(1);
	}
 
}
