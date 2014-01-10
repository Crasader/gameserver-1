package com.xinqihd.sns.gameserver.treasure;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class TreasureHuntManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testQueryRoleActionLimit() {
		User user = new User();
		user.setLevelSimple(10);
		TreasureHuntManager.getInstance().queryTreasureHuntInfo(user, System.currentTimeMillis(), false);
		fail("Not yet implemented");
	}
	
	@Test
	public void testDoTreasureHuntFreeCount() {
		User user = prepareUser();
		String roleName = user.getRoleName();
		
		Jedis jedis = JedisFactory.getJedisDB();
		String keyName = TreasureHuntManager.getRedisLimitKeyName(roleName);
		jedis.del(keyName);
		
		int mode = 0;
		TreasureHuntManager manager = TreasureHuntManager.getInstance();
		Calendar cal = Calendar.getInstance();

		manager.queryTreasureHuntInfo(user, System.currentTimeMillis(), false);
		int freeCount = (Integer)user.getUserData(TreasureHuntManager.USER_DATA_HUNT_FREE);
		assertEquals(5, freeCount);
		boolean success = manager.doTreasureHunt(user, mode, System.currentTimeMillis());
		assertTrue(success);
		freeCount = (Integer)user.getUserData(TreasureHuntManager.USER_DATA_HUNT_FREE);
		assertEquals(4, freeCount);
		
		freeCount = (Integer)user.getUserData(
				TreasureHuntManager.USER_DATA_HUNT_FREE);
		assertEquals(4, freeCount);
		
		//Use all the freecount
		for ( int i=0; i<freeCount; i++ ) {
			success = manager.doTreasureHunt(user, mode, System.currentTimeMillis());
			assertTrue(success);
		}
		//No yuanbao
		success = manager.doTreasureHunt(user, mode, System.currentTimeMillis());
		assertTrue(!success);
		
		user.setYuanbaoSimple(100);
		success = manager.doTreasureHunt(user, mode, System.currentTimeMillis());
		assertTrue(success);
		assertTrue(user.getYuanbao()<100);
	}
	
//	@Test
//	public void testPropDataConvertToReward() {
//		User user = prepareUser();
//		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.TREASURE_HUNT_GEN, user);
//		HashMap<Integer, TreasurePojo> treasures = (HashMap<Integer, TreasurePojo>)result.getResult().get(0);
//		//Normal mode
//		TreasurePojo treasure = treasures.get(0);
//		List<Reward> propDatas = treasure.getGifts();
//		for ( Reward propData : propDatas ) {
//			Reward reward = RewardManager.getInstance().convertPropDataToReward(propData);
//			PropData actual = null;
//			if ( reward.getType() == RewardType.ITEM ) {
//				actual = RewardManager.getInstance().convertRewardItemToPropData(reward);
//			} else {
//				actual = RewardManager.getInstance().convertRewardWeaponToPropData(reward, user);
//			}
//			String str = propData.toDetailString();
//			String act = actual.toDetailString();
//			if ( !str.equals(act) ) {
//				System.out.println(str);
//				System.out.println(act);
//			}
//			assertEquals(str, act);
//		}
//	}
	
	private User prepareUser() {
		User user = new User();
		String roleName = "test-001";
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		user.setLevelSimple(10);
		UserManager.getInstance().removeUser(roleName);
		return user;
	}
}
