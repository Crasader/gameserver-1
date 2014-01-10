package com.xinqihd.sns.gameserver.reward;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DailyMarkRewardTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToMarkArray() {
		String markArrayStr = "1,0,1,0,0,0,0";
		boolean[] actual = DailyMarkReward.toMarkArray(markArrayStr);
		boolean[] expect = {true,false,true,false,false,false,false};
		assertArrayEquals(expect, actual);
	}
	
	@Test
	public void testToMarkArrayOne() {
		String markArrayStr = "1";
		boolean[] actual = DailyMarkReward.toMarkArray(markArrayStr);
		boolean[] expect = {true};
		assertArrayEquals(expect, actual);
	}
	
	@Test
	public void testToMarkArrayEmpty() {
		String markArrayStr = "";
		boolean[] actual = DailyMarkReward.toMarkArray(markArrayStr);
		boolean[] expect = {};
		assertArrayEquals(expect, actual);
	}
	
	@Test
	public void testToMarkArrayNull() {
		String markArrayStr = null;
		boolean[] actual = DailyMarkReward.toMarkArray(markArrayStr);
		boolean[] expect = {};
		assertArrayEquals(expect, actual);
	}
	
	public void assertArrayEquals(boolean[] expect, boolean[] actual) {
		assertEquals(expect.length, actual.length);
		for ( int i=0; i<expect.length; i++ ) {
			assertEquals(expect[i], actual[i]);
		}
	}

}
