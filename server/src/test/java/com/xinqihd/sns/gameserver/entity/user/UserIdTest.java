package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserIdTest {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToString() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 30);
//		System.out.println(sdf.format(cal.getTime()));
		int dateValue = (int)(cal.getTimeInMillis()/1000);
		
		UserId userId = new UserId("0123456789", dateValue);
		assertEquals(dateValue+":0123456789", userId.toString());
//		System.out.println(userId);
		
		cal.set(Calendar.MONTH, 10);
		cal.set(Calendar.DAY_OF_MONTH, 10);
		dateValue = (int)(cal.getTimeInMillis()/1000);
		userId = new UserId("0123456789", dateValue);
		assertEquals(dateValue+":0123456789", userId.toString());
	}
	
	@Test
	public void testToString2() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 30);
		System.out.println(sdf.format(cal.getTime()));
		int dateValue = (int)(cal.getTimeInMillis()/1000);
		
		UserId userId = new UserId("0123456789", dateValue);
		assertEquals(dateValue+":0123456789", userId.toString());
//		System.out.println(userId);
		
		//2011 does not has Feb 29.
		cal.set(Calendar.YEAR, 2011);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		dateValue = (int)(cal.getTimeInMillis()/1000);
		userId = new UserId("0123456789", dateValue);
//		assertEquals("2011/03/01:0123456789", userId.toString());
	}
	
	@Test
	public void testToString3() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 30);
		System.out.println(sdf.format(cal.getTime()));
		int dateValue = (int)(cal.getTimeInMillis()/1000);
		
		UserId userId = new UserId("宝贝战争", dateValue);
		assertEquals(dateValue+":宝贝战争", userId.toString());
//		System.out.println(userId);
	}
	
	@Test
	public void testCompare() {		
		UserId testId1 = new UserId("test001");
		UserId testId2 = new UserId("test001");
		
		assertArrayEquals(testId1.getInternal(), testId2.getInternal());
//		System.out.println(userId);
	}

	@Test
	public void testFromString() {		
		UserId userId = null;
		
		userId = UserId.fromString("2012/02/29:宝贝战争");
		assertNull(userId);
		
		userId = UserId.fromString("1330358400:宝贝战争");
		
		
		assertEquals(1330358400, userId.getRegisterDate());
		assertEquals("宝贝战争", userId.getUserName());
	}

	@Test
	public void testFromString2() {
		UserId userId = new UserId("宝贝战争");
		String expectStr = userId.toString();
		UserId actual = UserId.fromString(expectStr);
		String actualStr = actual.toString();
		
		assertEquals(userId, actual);
		assertEquals(expectStr, actualStr);
	}
	
	@Test
	public void testFromBytes1() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 30);
		System.out.println(sdf.format(cal.getTime()));
		int dateValue = (int)(cal.getTimeInMillis()/1000);
		
		UserId expected = new UserId("test001", dateValue);
		byte[] internal = expected.getInternal();

		UserId actual = UserId.fromBytes(internal);
		assertEquals(expected.getRegisterDate(), actual.getRegisterDate());
		assertEquals(expected.getUserName(), actual.getUserName());
		assertArrayEquals(expected.getInternal(), actual.getInternal());
	}
	
	@Test
	public void testFromBytes2() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 29);
		cal.set(Calendar.HOUR, 01);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 30);
		System.out.println(sdf.format(cal.getTime()));
		int dateValue = (int)(cal.getTimeInMillis()/1000);
		
		String userName = "宝贝战争";
		UserId expected = new UserId("宝贝战争", dateValue);
		byte[] internal = expected.getInternal();
		for ( int i=0; i<userName.length(); i++ ) {
			System.out.print(",0x" + Integer.toHexString((userName.charAt(i) >> 8) & 0xff));
			System.out.print(",0x" + Integer.toHexString((userName.charAt(i)) & 0xff));
		}
		System.out.println();
		
		for ( int i=0; i<internal.length; i+=2 ) {
			System.out.print(",0x" + Integer.toHexString((internal[i]) & 0xff));
			System.out.print(",0x" + Integer.toHexString((internal[i+1]) & 0xff));
		}
		System.out.println();

		UserId actual = UserId.fromBytes(internal);
		assertEquals(expected.getRegisterDate(), actual.getRegisterDate());
		assertEquals(expected.getUserName(), actual.getUserName());
		assertArrayEquals(expected.getInternal(), actual.getInternal());
	}
	
	@Test
	public void testHashCode() {
		int max = 1000000;
		long startM =0,  endM = 0l;
				
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			UserId userId = new UserId("test".concat(String.valueOf(i)));
			int h = userId.getRegisterDate()*31 + userId.getUserName().hashCode(); 
		}
		endM = System.currentTimeMillis();
		System.out.println("Use String.hashcode loop " + max +", time: " + (endM-startM));
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			UserId userId = new UserId("test".concat(String.valueOf(i)));
		}
		endM = System.currentTimeMillis();
		System.out.println("Use UserId.hashCode loop " + max +", time: " + (endM-startM));
	}
	
	@Test
	public void testCompareUserId() throws Exception {
		UserId userId = new UserId("test001");
		for ( int i=0; i<100; i++ ) {
			UserId actual = new UserId("test001");
			assertEquals(userId.getRegisterDate(), actual.getRegisterDate());
		}
	}

}
