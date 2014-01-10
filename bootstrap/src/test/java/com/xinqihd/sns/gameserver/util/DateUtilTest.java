package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.util.BSDateUtil.DateUnit;

public class DateUtilTest {

	@Before
	public void setUp() throws Exception {
		BSDateUtil.resetInternalDate();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalculateSecondsDaily() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 9, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = BSDateUtil.getSecondsToNextDateUnit(DateUnit.DAILY, current);

		Calendar expect = Calendar.getInstance();
		expect.set(2012, 1, 10, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsDaily2() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 O:0:0
		current.set(2012, 1, 9, 0, 0, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = BSDateUtil.getSecondsToNextDateUnit(DateUnit.DAILY, current);

		Calendar expect = Calendar.getInstance();
		expect.set(2012, 1, 10, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsMonthly() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 9, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = BSDateUtil.getSecondsToNextDateUnit(DateUnit.MONTHLY, current);
		
		Calendar expect = Calendar.getInstance();
		expect.set(2012, 2, 1, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testCalculateSecondsMonthly2() {
		Calendar current = Calendar.getInstance();
		//2012-2-9 23:59:00
		current.set(2012, 1, 29, 23, 59, 0);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + current.getTime());
		int seconds = BSDateUtil.getSecondsToNextDateUnit(DateUnit.MONTHLY, current);
		
		Calendar expect = Calendar.getInstance();
		expect.set(2012, 2, 1, 0, 0, 0);
		
		clone.add(Calendar.SECOND, seconds);
		System.out.println("expect:" + expect.getTime() + ", actual: " + clone.getTime());
		assertEquals(expect.getTimeInMillis()/1000, clone.getTimeInMillis()/1000);
	}
	
	@Test
	public void testGetStrYesterday() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		
		String yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		
		assertEquals("2012-02-09", yesterday);
	}
	
	@Test
	public void testGetStrToday() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		
		String yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		assertEquals("2012-02-09", yesterday);
		
		String today = BSDateUtil.getToday(current.getTimeInMillis());
		assertEquals("2012-02-10", today);
	}
	
	@Test
	public void testGetStrWeek() {
		Calendar current = Calendar.getInstance();
		//2012-12-1 11:16:00
		current.set(2012, 0, 1, 11, 16, 0);
		
		String week = BSDateUtil.getWeek(current.getTimeInMillis());
		assertEquals("2012-01", week);
		
		BSDateUtil.resetInternalDate();
		
		//2012-12-29 11:16:00
		current.set(2012, 11, 29, 11, 16, 0);
		week = BSDateUtil.getWeek(current.getTimeInMillis());
		assertEquals("2012-52", week);
	}
	
	@Test
	public void testGetStrYesterdayAtFirstDay() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 1, 11, 16, 0);
		
		String yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		
		assertEquals("2012-01-31", yesterday);
	}
	
	@Test
	public void testGetStrTodayAtFirstDay() {
		Calendar current = Calendar.getInstance();
		//2012-2-1 11:16:00
		current.set(2012, 1, 1, 11, 16, 0);
		
		String yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		assertEquals("2012-01-31", yesterday);
		
		String today = BSDateUtil.getToday(current.getTimeInMillis());
		assertEquals("2012-02-01", today);
	}
	
	@Test
	public void testGetStrYesterdayWithTimeout() throws Exception {
		Calendar current = Calendar.getInstance();
		//2012-2-10 23:59:59
		current.set(2012, 1, 10, 23, 59, 59);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + clone.getTime());
		
		String yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		
		assertEquals("2012-02-09", yesterday);
		
		//Wait for next day
		current.set(2012, 1, 11, 0, 0, 1);
		yesterday = BSDateUtil.getYesterday(current.getTimeInMillis());
		
		assertEquals("2012-02-10", yesterday);
	}
	
	@Test
	public void testGetStrTodayWithTimeout() throws Exception {
		Calendar current = Calendar.getInstance();
		//2012-2-10 23:59:59
		current.set(2012, 1, 10, 23, 59, 59);
		Calendar clone = (Calendar)current.clone();
		System.out.println("current: " + clone.getTime());
		
		String today = BSDateUtil.getToday(current.getTimeInMillis());
		
		assertEquals("2012-02-10", today);
		
		//Wait for next day
		current.set(2012, 1, 11, 0, 0, 1);
		today = BSDateUtil.getToday(current.getTimeInMillis());
		
		assertEquals("2012-02-11", today);
	}
	
	@Test
	public void testGetStrLastMonth() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		String lastMonth = BSDateUtil.getLastMonth(current.getTimeInMillis());
		assertEquals("2012-01", lastMonth);
		
		current.set(2012, 1, 1, 11, 16, 0);
		lastMonth = BSDateUtil.getLastMonth(current.getTimeInMillis());
		assertEquals("2012-01", lastMonth);
		
		current.set(2012, 1, 29, 11, 16, 0);
		lastMonth = BSDateUtil.getLastMonth(current.getTimeInMillis());
		assertEquals("2012-01", lastMonth);
	}
	
	@Test
	public void testGetStrLastMonthWithTimeout() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 1, 0, 0, 1);
		String lastMonth = BSDateUtil.getLastMonth(current.getTimeInMillis());
		assertEquals("2012-01", lastMonth);
		
		current.set(2012, 2, 1, 0, 0, 1);
		System.out.println(current.getTime());
		lastMonth = BSDateUtil.getLastMonth(current.getTimeInMillis());
		assertEquals("2012-02", lastMonth);
	}
	
	@Test
	public void testGetStrCurrentMonth() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		String lastMonth = BSDateUtil.getCurrentMonth(current.getTimeInMillis());
		assertEquals("2012-02", lastMonth);
		
		current.set(2012, 1, 1, 11, 16, 0);
		lastMonth = BSDateUtil.getCurrentMonth(current.getTimeInMillis());
		assertEquals("2012-02", lastMonth);
		
		current.set(2012, 1, 29, 11, 16, 0);
		lastMonth = BSDateUtil.getCurrentMonth(current.getTimeInMillis());
		assertEquals("2012-02", lastMonth);
	}
	
	@Test
	public void testGetStrCurrentMonthWithTimeout() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 1, 0, 0, 1);
		String currentMonth = BSDateUtil.getCurrentMonth(current.getTimeInMillis());
		assertEquals("2012-02", currentMonth);
		
		current.set(2012, 2, 1, 0, 0, 1);
		System.out.println(current.getTime());
		currentMonth = BSDateUtil.getCurrentMonth(current.getTimeInMillis());
		assertEquals("2012-03", currentMonth);
	}
	
	@Test
	public void testGetSecondsToTimeClockNull() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		long currentTimeMillis = current.getTimeInMillis();
		int seconds = BSDateUtil.getSecondsToTimeClock(currentTimeMillis, null);
		assertEquals(0, seconds);
	}
	
	@Test
	public void testGetSecondsToTimeClockFormat() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		long currentTimeMillis = current.getTimeInMillis();
		int seconds = BSDateUtil.getSecondsToTimeClock(currentTimeMillis, "0730");
		assertEquals(0, seconds);
	}
	
	@Test
	public void testGetSecondsToTimeClock() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		long currentTimeMillis = current.getTimeInMillis();
		int seconds = BSDateUtil.getSecondsToTimeClock(currentTimeMillis, "12:30");
		assertEquals(4440, seconds);
	}
	
	@Test
	public void testGetSecondsToTimeClockNeg() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		long currentTimeMillis = current.getTimeInMillis();
		int seconds = BSDateUtil.getSecondsToTimeClock(currentTimeMillis, "10:30");
		assertEquals(-2760, seconds);
	}
	
	@Test
	public void testFormatDate() {
		Calendar current = Calendar.getInstance();
		//2012-2-10 11:16:00
		current.set(2012, 1, 10, 11, 16, 0);
		Date date = current.getTime();
		assertEquals("2012-02-10", BSDateUtil.formatDate(date));
	}
	
	@Test
	public void testFormatDateNull() {
		assertEquals(null, BSDateUtil.formatDate(null));
	}
}
