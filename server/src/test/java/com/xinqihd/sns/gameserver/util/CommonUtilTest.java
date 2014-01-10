package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommonUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDateMillis() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		System.out.println(cal.getTime());
		
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(CommonUtil.getDateMillis(cal.getTimeInMillis())*1000l);
		System.out.println(cal2.getTime());
		
		assertEquals(0, cal2.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, cal2.get(Calendar.MINUTE));
		assertEquals(0, cal2.get(Calendar.SECOND));
		
		int year = cal2.get(Calendar.YEAR);
		assertEquals(2011, cal2.get(Calendar.YEAR));
		assertEquals(11, cal2.get(Calendar.MONTH));
		assertEquals(1, cal2.get(Calendar.DAY_OF_MONTH));
	}
	
	@Test
	public void testGetDateMillis2() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		Calendar cal2 = Calendar.getInstance();
		long millis = CommonUtil.getDateMillis(cal.getTimeInMillis())*1000l;
		cal2.setTimeInMillis(millis);
		System.out.println(cal2.getTime());
		Date date = new Date(millis);
		System.out.println(date);
				
		int year = cal2.get(Calendar.YEAR);
		assertEquals(2012, cal2.get(Calendar.YEAR));
		assertEquals(1, cal2.get(Calendar.MONTH));
		assertEquals(29, cal2.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, cal2.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, cal2.get(Calendar.MINUTE));
		assertEquals(0, cal2.get(Calendar.SECOND));
	}
	
	@Test
	public void testGetDateMillis3() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1); // Feb 
		cal.set(Calendar.DAY_OF_MONTH, 29); //28
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		Calendar cal2 = Calendar.getInstance();
		long millis = CommonUtil.getDateMillis(cal.getTimeInMillis())*1000l;
		millis = CommonUtil.getDateMillis(millis)*1000l;
		
		cal2.setTimeInMillis(millis);
		System.out.println(cal2.getTime());
		Date date = new Date(millis);
		System.out.println(date);
		
		int year = cal2.get(Calendar.YEAR);
		assertEquals(2012, cal2.get(Calendar.YEAR));
		assertEquals(1, cal2.get(Calendar.MONTH));
		assertEquals(29, cal2.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, cal2.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, cal2.get(Calendar.MINUTE));
		assertEquals(0, cal2.get(Calendar.SECOND));
	}

	@Test
	public void testGetNewUserIdBytes() {
		byte[] expect1 = {0x4e,(byte)0xd6,0x64,(byte)0x94,0x0,0x30,0x0,0x31,0x0,0x32,0x0,0x33,0x0,0x34,0x0,0x35,0x0,0x36,0x0,0x37,0x0,0x38,0x0,0x39};
		byte[] expect2 = {0x4e,(byte)0xd6,0x64,(byte)0x94,0x0,0x30,0x0,0x31,0x0,0x32,0x0,0x33,0x0,0x34};
		byte[] expect3 = {0x4e,(byte)0xd6,0x64,(byte)0x94,0x0,0x30,0x0,0x31,0x0,0x32,0x0,0x33,0x0,0x34,0x0,0x35,0x0,0x36,0x0,0x37,0x0,0x38,0x0,0x39,0x0,0x30,0x0,0x31,0x0,0x32,0x0,0x33,0x0,0x34,0x0,0x35,0x0,0x36,0x0,0x37,0x0,0x38,0x0,0x39};
		byte[] expect4 = {0x4e,(byte)0xd6,0x64,(byte)0x94,0x5b,(byte)0x9d,(byte)0x8d,0x1d,0x76,(byte)0x84,0x62,0x18,0x4e,(byte)0x89,0x5b,(byte)0x9d,(byte)0x8d,0x1d,0x76,(byte)0x84,0x62,0x18,0x4e,(byte)0x89};
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 0);
		int dateValue = (int)(cal.getTimeInMillis()/1000);
//		System.out.println(Integer.toHexString(dateValue));
		
		byte[] id = CommonUtil.getNewUserIdBytes(dateValue, "0123456789");
//		for ( byte by : id ) {
//			System.out.print("0x"+Integer.toHexString(by&0xff)+",");
//		}
//		System.out.println();
		assertArrayEquals(expect1, id);
		
		id = CommonUtil.getNewUserIdBytes(dateValue, "01234");
//		for ( byte by : id ) {
//			System.out.print("0x"+Integer.toHexString(by&0xff)+",");
//		}
//		System.out.println();
		assertArrayEquals(expect2, id);
		
		id = CommonUtil.getNewUserIdBytes(dateValue, "01234567890123456789");
		for ( byte by : id ) {
			System.out.print("0x"+Integer.toHexString(by&0xff)+",");
		}
		System.out.println();
		assertArrayEquals(expect3, id);
		
		try {
			id = CommonUtil.getNewUserIdBytes(dateValue, "012345678901234567890012345678901234567890");
		} catch (IllegalArgumentException e) {
			fail("Should not throw IllegalArgumentException");
		}
		
		id = CommonUtil.getNewUserIdBytes(dateValue, "宝贝的战争宝贝的战争");
		assertArrayEquals(expect4, id);
//		for ( byte by : id ) {
//			System.out.print("0x"+Integer.toHexString(by&0xff)+",");
//		}
//		System.out.println();
	}
}
