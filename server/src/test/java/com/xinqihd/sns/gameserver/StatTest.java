package com.xinqihd.sns.gameserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StatTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Stat stat = Stat.getInstance();
		System.out.println(stat.toString().length());
	}

}
