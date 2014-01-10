package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.ExitPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;

public class ExitGameManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void setupExitGamePojo() throws Exception {
		/**
		 * 玩家隔日访问游戏，赠送lv4级火神石
		 * 20019	火神石Lv4	RedStoneLv4	很高的几率随机附加攻击属性。
		 */
		ItemPojo item = ItemManager.getInstance().getItemById("20019");
		Reward reward = RewardManager.getRewardItem(item);
		ExitPojo day1 = new ExitPojo();
		day1.setId(1);
		day1.setDays(1);
		day1.setReward(reward);
		addExitPojo(day1);
		
		/**
		 * 玩家隔3日访问游戏，赠送lv4级强化石
		 * 20024	强化石Lv4	StrengthStoneLv4	用来强化提高装备的攻击属性和防御属性。比3级强化石拥有更高的成功率。
		 */
		item = ItemManager.getInstance().getItemById("20024");
		reward = RewardManager.getRewardItem(item);
		ExitPojo day3 = new ExitPojo();
		day3.setId(3);
		day3.setDays(3);
		day3.setReward(reward);
		addExitPojo(day3);
		
		/**
		 * 玩家隔7日访问游戏，赠送50元宝
		 */
		reward = RewardManager.getRewardYuanbao(50);
		ExitPojo day7 = new ExitPojo();
		day7.setId(7);
		day7.setDays(7);
		day7.setReward(reward);
		addExitPojo(day7);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	/**
	 * Add a new exitPojo
	 * @param exitPojo
	 */
	public void addExitPojo(ExitPojo exitPojo) {
		MapDBObject dbObj = MongoDBUtil.createMapDBObject(exitPojo);
		DBObject query = MongoDBUtil.createDBObject("_id", exitPojo.getId());
		MongoDBUtil.saveToMongo(query, dbObj, "babywarcfg", "server0001", "exits", true);
	}
}
