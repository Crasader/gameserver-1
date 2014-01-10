package com.xinqihd.sns.gameserver.battle;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;

public class BattleDataLoader4BitmapTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetBattleMapList() {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		boolean success = BattleDataLoader4Bitmap.loadBattleMaps();
		assertTrue(success);
		for ( BattleBitSetMap map : BattleDataLoader4Bitmap.getBattleMapList() ) {
			System.out.println(map);
		}
	}
	
	/*
	public void testGetRandomBattleMap() {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		boolean success = BattleDataLoader4Bitmap.loadBattleMaps();
		BattleBitSetMap map = BattleDataLoader4Bitmap.getRandomBattleMap();
		assertNotNull(map);
		System.out.println(map);
	}
	*/
	
	@Test
	public void testGetBattleMapById() {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		boolean success = BattleDataLoader4Bitmap.loadBattleMaps();
		BattleBitSetMap map = BattleDataLoader4Bitmap.getBattleMapById("1");
		assertNotNull(map);
		System.out.println(map);
	}

	@Test
	public void testGetBattleBulletByName() {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		boolean success = BattleDataLoader4Bitmap.loadBattleBullet();
		assertTrue(success);
		BattleBitSetBullet bullet = BattleDataLoader4Bitmap.getBattleBulletByName("bullet_black");
		System.out.println(bullet);
		assertNotNull(bullet);
	}

	@Test
	public void testGetBattleBullets() {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		boolean success = BattleDataLoader4Bitmap.loadBattleBullet();
		Collection<BattleBitSetBullet> bullets = BattleDataLoader4Bitmap.getBattleBullets();
		for ( BattleBitSetBullet bullet : bullets ) {
			System.out.println(bullet.getBulletName()+":"+bullet.getBullet().getWidth()+","
					+bullet.getBullet().getHeight());
		}
	}
}
