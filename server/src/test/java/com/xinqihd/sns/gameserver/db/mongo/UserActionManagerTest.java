package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.entity.user.UserAction;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.util.MathUtil;

public class UserActionManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddUserAction() {
		int max=1000;
		UserActionManager manager = UserActionManager.getInstance();
		for ( int i=0; i<max; i++ ) {
			UserAction userAction = new UserAction();
			userAction.setTextKey(UserActionKey.values()[MathUtil.nextFakeInt(UserActionKey.values().length)]);
			userAction.setRoleName("user"+i);
			manager.addUserAction(userAction);
		}
		ArrayList<String> userActions = manager.getUserActions(max);
		assertTrue(userActions.size()<=101);
		for ( String action: userActions ) {
			System.out.println(action);
		}
	}

}
