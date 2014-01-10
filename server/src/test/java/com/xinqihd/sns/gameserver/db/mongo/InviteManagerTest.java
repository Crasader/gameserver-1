package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceInvite.BceInvite;

public class InviteManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Check frequency
	 */
	@Test
	public void testChallengeFriend() throws Exception {
		InviteManager manager = InviteManager.getInstance();
		User user = new User();
		BceInvite.Builder builder = BceInvite.newBuilder();
		builder.setPos(0);
		UserId key = new UserId("");
		builder.setUid(key.toString());
		boolean success = manager.challengeFriend(user, builder.build());
		assertTrue(success);
		//Frequency check
		success = manager.challengeFriend(user, builder.build());
		assertTrue(!success);
		
		int coolDownMillis = GameDataManager.getInstance().
				getGameDataAsInt(GameDataKey.CHALLENGE_USER_COOLDOWN, 15000);
		Thread.sleep(coolDownMillis);
		
		success = manager.challengeFriend(user, builder.build());
		assertTrue(success);
	}

}
