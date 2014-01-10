package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;

public class StringUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSubstring1() {
		String line = "map1001={";
		String start = "map";
		String end = "=";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("1001", actual);
	}
	
	@Test
	public void testSubstring2() {
		String line = "map1001={";
		String start = "mapZ";
		String end = "=";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("map1001", actual);
	}
	
	@Test
	public void testSubstring3() {
		String line = "map1001={";
		String start = "map";
		String end = "=Z";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("1001={", actual);
	}
	
	@Test
	public void testSubstring4() {
		String line = "map1001={";
		String start = null;
		String end = "=";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("map1001", actual);
	}
	
	@Test
	public void testSubstring5() {
		String line = "map1001={";
		String start = null;
		String end = null;
		String actual = StringUtil.substring(line, start, end);
		assertEquals("map1001={", actual);
	}
	
	@Test
	public void testSubstring6() {
		String line = null;
		String start = "map";
		String end = "=";
		String actual = StringUtil.substring(line, start, end);
		assertEquals(null, actual);
	}
	
	@Test
	public void testSubstring8() {
		String line = "name=\"死亡沙漠\"";
		String start = "\"";
		String end = "\"";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("死亡沙漠", actual);
	}
	
	@Test
	public void testSubstring7() {
		String line = "map1001={";
		String start = "map";
		String end = "1001";
		String actual = StringUtil.substring(line, start, end);
		assertEquals("", actual);
	}
	
	@Test
	public void testSubstringR1() {
		String line = "GET /data/config/item_config.lua HTTP/1.1";
		String start = "/";
		String end = "?";
		String actual = StringUtil.substringR(line, null, " HTTP");
		actual = StringUtil.substringR(actual, "/", "?");
		assertEquals("item_config.lua", actual);
	}
	
	@Test
	public void testSubstringR2() {
		String line = "GET /data/config/item_config.lua?id=1 HTTP/1.1";
		String start = "/";
		String end = "?";
		String actual = StringUtil.substringR(line, null, " HTTP");
		actual = StringUtil.substringR(actual, "/", "?");
		assertEquals("item_config.lua", actual);
	}
	
	@Test
	public void testConcat1() {
		String actual = StringUtil.concat("a", "b", "c", "d");
		assertEquals("abcd", actual);
	}
	
	@Test
	public void testConcat2() {
		String actual = StringUtil.concat("a", null, "c", "d");
		assertEquals("acd", actual);
	}

	@Test
	public void testConcat3() {
		String actual = StringUtil.concat("a");
		assertEquals("a", actual);
	}
	
	@Test
	public void testConcat4() {
		String actual = StringUtil.concat("a", "b");
		assertEquals("ab", actual);
	}

	@Test
	public void testConcat5() {
		String actual = StringUtil.concat(null);
		assertEquals(null, actual);
	}
	
	@Test
	public void testConcat6() {
		String actual = StringUtil.concat(null, "Hello");
		assertEquals("Hello", actual);
	}
	
	@Test
	public void testConcat7() {
		String actual = StringUtil.concat(null, null);
		assertEquals("", actual);
	}
	
	@Test
	public void testConcat8() {
		String actual = StringUtil.concat(null, null, null);
		assertEquals("", actual);
	}
	
	@Test
	public void testConcat9() {
		String actual = StringUtil.concat(null, null, null, "hello");
		assertEquals("hello", actual);
	}

	@Test
	public void testToInt1() {
		int value = StringUtil.toInt("1", 1);
		assertEquals(1, value);
	}
	
	@Test
	public void testToInt2() {
		int value = StringUtil.toInt("1a", 0);
		assertEquals(0, value);
	}
	
	@Test
	public void testByteToHex1() {
		byte[] array = {0x1, 0x2, 0x3, 0x4, 0x5, 0x10, 0x15, 0x20, 0x64, (byte)99, (byte)128, (byte)255};
		String result = StringUtil.bytesToHexString(array);
		String expect = "0102030405101520646380FF";
		assertEquals(expect, result);
	}
	
	@Test
	public void testHexToBytes1() {
		byte[] array = {0x1, 0x2, 0x3, 0x4, 0x5, 0x10, 0x15, 0x20, 0x64, (byte)99, (byte)128, (byte)255};
		String expect = StringUtil.bytesToHexString(array);
		byte[] result = StringUtil.hexStringToBytes(expect);
		System.out.println(StringUtil.bytesToHexString(result));
		assertArrayEquals(array, result);
	}
	
	@Test
	public void testSplitMachineId() {
		String machineid = "192.168.0.6:2181";
		String[] results = StringUtil.splitMachineId(machineid);
		assertEquals("192.168.0.6", results[0]);
		assertEquals("2181", results[1]);
	}
	
	@Test
	public void testSplitMachineId2() {
		String machineid = "192.168.0.6";
		String[] results = StringUtil.splitMachineId(machineid);
		assertNull(results);
	}
	
	@Test
	public void testSplitMachineId3() {
		String machineid = ":2181";
		String[] results = StringUtil.splitMachineId(machineid);
		assertNull(results);
	}
	
	@Test
	public void testSplitMachineId4() {
		String machineid = "192.168.0.6:";
		String[] results = StringUtil.splitMachineId(machineid);
		assertNull(results);
	}
	
	@Test
	public void testSplitMachineId5() {
		String machineid = null;
		String[] results = StringUtil.splitMachineId(machineid);
		assertNull(results);
	}
	
	@Test
	public void testSplitMachineIdPerformance() throws Exception {
		final String machineid = "192.168.0.6:2181";
		int max = 10000;
		TestUtil.doPerform(new Runnable() {
			public void run() {
				String[] results = machineid.split(":");
			}
		}, "Java SplitString", max);
		TestUtil.doPerform(new Runnable() {
			public void run() {
				String[] results = StringUtil.splitMachineId(machineid);
			}
		}, "Our splitMachineId", max);
	}
	
	@Test
	public void testEncryptSHA1() throws Exception {
		final String password = "hello";
		String passwordEncrypt = StringUtil.encryptSHA1(password);
		System.out.println(passwordEncrypt);
		assertEquals(StringUtil.encryptSHA1(password), passwordEncrypt);
		
		//test performance
		TestUtil.doPerform(new Runnable() {
			public void run() {
				try {
					StringUtil.encryptSHA1(password);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
			}
		}, "SHA1", 10000);
	}
	
	@Test
	public void testParseLocale() {
		String localeStr = "zh_CN";
		Locale locale = StringUtil.parseLocale(localeStr, Locale.US);
		System.out.println(locale);
		assertEquals(Locale.SIMPLIFIED_CHINESE, locale);
	}
	
	@Test
	public void testParseLocaleZH() {
		String localeStr = "zh";
		Locale locale = StringUtil.parseLocale(localeStr, Locale.CHINESE);
		System.out.println(locale);
		assertEquals(Locale.CHINESE, locale);
	}
	
	@Test
	public void testParseLocaleNull() {
		String localeStr = null;
		Locale locale = StringUtil.parseLocale(localeStr, Locale.US);
		System.out.println(locale);
		assertEquals(Locale.US, locale);
	}
	
	@Test
	public void testParseLocaleNull2() {
		String localeStr = Constant.EMPTY;
		Locale locale = StringUtil.parseLocale(localeStr, Locale.US);
		System.out.println(locale);
		assertEquals(Locale.US, locale);
	}
	
	@Test
	public void testCheckValidEmail() {
		String email = "wangqi@xinqihd.com";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(true, valid);
	}
	
	@Test
	public void testCheckValidEmailDomain() {
		String email = "wangqi@xinqihd.com.cn";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(true, valid);
	}
	
	@Test
	public void testCheckValidEmailEmpty() {
		String email = "";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(false, valid);
	}
	
	@Test
	public void testCheckValidEmailNull() {
		String email = null;
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(false, valid);
	}
	
	@Test
	public void testCheckValidEmail1() {
		String email = "wangqi@xinqihd";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(false, valid);
	}
	
	@Test
	public void testCheckValidEmail2() {
		String email = "@xinqihd.com";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(false, valid);
	}
	
	@Test
	public void testCheckValidEmail3() {
		String email = "wangqi@";
		boolean valid = StringUtil.checkValidEmail(email);
		assertEquals(false, valid);
	}
	
}
