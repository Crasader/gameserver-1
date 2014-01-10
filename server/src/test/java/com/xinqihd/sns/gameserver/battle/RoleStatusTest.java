package com.xinqihd.sns.gameserver.battle;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoleStatusTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCheckStatus() {
		assertEquals(RoleStatus.DEAD, RoleStatus.checkStatus(RoleStatus.DEAD.toUserModeBit()));
		assertEquals(RoleStatus.FLYING, RoleStatus.checkStatus(RoleStatus.FLYING.toUserModeBit()));
		assertEquals(RoleStatus.HIDDEN, RoleStatus.checkStatus(RoleStatus.HIDDEN.toUserModeBit()));
		assertEquals(RoleStatus.ICED, RoleStatus.checkStatus(RoleStatus.ICED.toUserModeBit()));
		assertEquals(RoleStatus.NORMAL, RoleStatus.checkStatus(RoleStatus.NORMAL.toUserModeBit()));
	}

}
