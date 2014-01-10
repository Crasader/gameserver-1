package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class OfflineChallManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStoreChallengeInfo() {
		OfflineChallManager manager = OfflineChallManager.getInstance();
		User fromUser = createUser("challengee");
		User toUser = createUser("challenger");
		boolean win = true;
		Calendar cal = Calendar.getInstance();
		long currentTimeMillis = cal.getTimeInMillis();
		
		//clean data
		String dateStr = DateUtil.getYesterday(currentTimeMillis);
		manager.cleanChallengeData(toUser, dateStr);
		
		manager.storeChallengeInfo(fromUser, toUser, win, currentTimeMillis);
		
		cal.add(Calendar.DAY_OF_MONTH, 1);
		currentTimeMillis = cal.getTimeInMillis();

		dateStr = DateUtil.getYesterday(currentTimeMillis);
		int [] winAndLose = manager.queryChallengeInfo(toUser, dateStr);
		assertEquals(2, winAndLose.length);
		assertEquals(1, winAndLose[0]);
		assertEquals(0, winAndLose[1]);
	}

	@Test
	public void testQueryChallengeInfo() {
		//fail("Not yet implemented");
	}

	private User createUser(String userName) {
		User user = new User();
		user.set_id(new UserId(userName));
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setYuanbaoSimple(10000);
		user.setGoldenSimple(500);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		IoSession session = TestUtil.createIoSession(new ArrayList());
		//GameContext.getInstance().registerUserSession(session, user, user.getSessionKey());
		//GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		return user;
	}
}
