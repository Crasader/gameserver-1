package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserActionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNoParams() {
		UserAction action = new UserAction();
		action.setTextKey(UserActionKey.ActionLimitBuy);
		action.setRoleName("test001");
		String str = action.toString();
		String expect = "ActionLimitBuy\ttest001";
		assertEquals(expect, str);
	}
	
	@Test
	public void testWithParams() {
		UserAction action = new UserAction();
		action.setTextKey(UserActionKey.ActionLimitBuy);
		action.setRoleName("test001");
		String[] params = new String[2];
		params[0] = "hello";
		params[1] = "world";
		action.setParams(params);
		
		String str = action.toString();
		String expect = "ActionLimitBuy\ttest001\thello\tworld";
		assertEquals(expect, str);
	}
}
