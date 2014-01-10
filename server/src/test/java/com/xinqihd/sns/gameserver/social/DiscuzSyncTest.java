package com.xinqihd.sns.gameserver.social;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;

public class DiscuzSyncTest {

	@Before
	public void setUp() throws Exception {
		GameContext.getInstance().initContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegister() {
		String password = "123456";
		String encrypted = DiscuzSync.getDiscuzPwdByUserPwdAndSalt(password, "553a99");
		System.out.println(encrypted);
		assertEquals("0990eac1d2a41cbab73d78fedb05a3b4", encrypted);
	}
	
	@Test
	public void testRegisterByApi() {
		String username = "hehehe";
		String password = "123456";
		DiscuzSync.getInstance().register(username, password, null, null);
	}

}
