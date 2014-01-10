package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class EmailManagerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty("runtime.httpserverid", "192.168.0.77:8080");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSendVerifyEmail() {
		Account user = prepareAccount("test001");
		user.setEmail("wangqi@xinqihd.com");
		EmailManager.getInstance().sendVerifyEmail(user, null);
		assertEquals(false, user.isEmailVerified());
		
		String idStr = user.get_id().toString();
		EmailManager.getInstance().verifyEmail(idStr);
		
		Account actual = AccountManager.getInstance().queryAccountByName("test001");
		assertEquals(true, actual.isEmailVerified());
	}

	private Account prepareAccount(String userName) {
		Account account = new Account();
		account.set_id(userName);
		account.setUserName(userName);
		AccountManager.getInstance().removeAccount(account);
		AccountManager.getInstance().saveAccount(account);
		return account;
	}
}
