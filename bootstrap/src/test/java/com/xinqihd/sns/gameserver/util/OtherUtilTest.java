package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OtherUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
//		String hostName = InetAddress.getLocalHost().getHostAddress();
		String hostName = OtherUtil.getHostName();
		System.out.println("hostname: " + hostName);
		assertTrue(!"127.0.0.1".equals(hostName));
	}

}
