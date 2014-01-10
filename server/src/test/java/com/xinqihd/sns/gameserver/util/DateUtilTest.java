package com.xinqihd.sns.gameserver.util;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 18);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		System.out.println(DateUtil.formatDateTime(cal.getTime()));
		System.out.println(cal.getTimeInMillis());
	}

}
