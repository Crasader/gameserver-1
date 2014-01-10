package com.xinqihd.sns.gameserver.reward;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.entity.user.User;

public class RewardTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFromStringNull() {
		Reward reward = Reward.fromString(null);
		assertEquals(null, reward);
	}

	@Test
	public void testFromStringEmpty() {
		Reward reward = Reward.fromString("");
		assertEquals(null, reward);
	}
	
	@Test
	public void testFromStringWrongFormat() {
		Reward reward = Reward.fromString("{}");
		assertEquals(null, reward);
	}
	
	@Test
	public void testFromString() {
		User user = new User();
		user.setLevel(10);
		ArrayList<Reward> rewards = RewardManager.getInstance().
				generateRandomRewards(user, 1000, null);
		ArrayList<String> rewardStrs = new ArrayList<String>();
		for ( Reward r : rewards ) {
			rewardStrs.add(r.toString());
			System.out.println(r.toString().length());
		}
		int i=0;
		for ( String r : rewardStrs ) {
			Reward reward = Reward.fromString(r);
			assertEquals(rewards.get(i++), reward);
		}
	}
}
