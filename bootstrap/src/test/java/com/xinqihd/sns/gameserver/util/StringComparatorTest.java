package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StringComparatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompare() {
		ArrayList<String> rows = new ArrayList<String>(20);
		for ( int i=99; i>=0; i-- ) {
			rows.add("row"+i);
		}
		ArrayList<String> expects = new ArrayList<String>(20);
		for ( int i=0; i<100; i++ ) {
			expects.add("row"+i);
		}
		Collections.sort(rows, StringComparator.getInstance());
		assertArrayEquals(expects.toArray(), rows.toArray());
	}

}
