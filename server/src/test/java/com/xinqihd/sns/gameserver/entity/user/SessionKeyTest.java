package com.xinqihd.sns.gameserver.entity.user;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class SessionKeyTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSessionKey1() {
		InetSocketAddress address = new InetSocketAddress("localhost", 12342);
		SessionKey key1 = SessionKey.createSessionKey(address);
		String hexString = key1.toString();
		assertEquals(42, hexString.length());
		assertEquals("7F00000100003036", hexString.substring(0, 16));
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}

	@Test
	public void testSessionKey2() {
		InetSocketAddress address = new InetSocketAddress("192.168.0.1", 12342);
		SessionKey key1 = SessionKey.createSessionKey(address);
		String hexString = key1.toString();
		assertEquals("C0A8000100003036", hexString.substring(0, 16));
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}
	
	@Test
	public void testSessionKey3() {
		InetSocketAddress address = new InetSocketAddress("0.0.0.0", 0);
		SessionKey key1 = SessionKey.createSessionKey(address);
		String hexString = key1.toString();
		assertEquals("0000000000000000", hexString.substring(0, 16));
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}
	
	@Test
	public void testSessionKeySuffix() {
		InetSocketAddress address = new InetSocketAddress("0.0.0.0", 0);
		SessionKey key1 = SessionKey.createSessionKey(address, "_ROOM".getBytes());
		String hexString = key1.toString();
		assertEquals("0000000000000000", hexString.substring(0, 16));
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}
	
	@Test
	public void testSessionKeyRandomString() {
		SessionKey key1 = SessionKey.createSessionKeyFromRandomString("ROOM_");
		String hexString = key1.toString();
		assertEquals("524F4F4D5F", hexString.substring(0, 10));
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}
	
	@Test
	public void testSessionKeyRandomString2() {
		SessionKey key1 = SessionKey.createSessionKeyFromRandomString(null);
		String hexString = key1.toString();
		assertEquals(24, hexString.length());
		
		SessionKey key2 = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(key1, key2);
	}
	
	@Test
	public void testSessionKeyRandomStringUniqueness() throws Exception {
		final HashSet<SessionKey> keySet = new HashSet<SessionKey>();
		final AtomicInteger counter = new AtomicInteger(0);
		TestUtil.doPerform(new Runnable() {
			public void run() {
				SessionKey key = SessionKey.createSessionKeyFromRandomString(null);
				if ( keySet.contains(key) ) {
					counter.incrementAndGet();
				} else {
					keySet.add(key);
				}
			}
		}, "Random SessionKey Unique", 1000);
		System.out.println("dupliate: " + counter);
	}
	
	@Test
	public void testPerformance1() {
		int max = 100000000;
		long start = 0l, end = 0l;
		String str = "C0A8000100003036";
				
		//Test string hashcode.
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			str.hashCode();
		}
		end = System.currentTimeMillis();
		System.out.println("string hascode time: " + (end-start));
		
	}
	
	@Test
	public void testPerformance2() {
		int max = 100000000;
		long start = 0l, end = 0l;
		String str = "C0A8000100003036";
		
		//Test sessionkey hashcode.
		SessionKey key = SessionKey.createSessionKeyFromHexString(str);
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			key.hashCode();
		}
		end = System.currentTimeMillis();
		System.out.println("SessionKey hascode time: " + (end-start));
	}
	
	@Test
	public void testPerformance3_1() {
		int max = 10000000;
		long start = 0l, end = 0l;
		String str = "C0A8000100003036";
		
		//Test sessionkey hashcode.
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			str = String.valueOf(i);
		}
		end = System.currentTimeMillis();
		System.out.println("String valueof time: " + (end-start));
	}
	
	@Test
	public void testPerformance3_2() {
		int max = 10000000;
		long start = 0l, end = 0l;
		String str = "C0A8000100003036";
		
		//Test sessionkey hashcode.
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			SessionKey key = SessionKey.createSessionKeyFromHexString(str);
		}
		end = System.currentTimeMillis();
		System.out.println("SessionKey create from string time: " + (end-start));
	}

	@Test
	public void testPerformance4() {
		int max = 10000000;
		long start = 0l, end = 0l;
		String str = "C0A8000100003036";
		
		//Test sessionkey hashcode.
		SessionKey key = SessionKey.createSessionKeyFromHexString(str);
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			key.toString();
		}
		end = System.currentTimeMillis();
		System.out.println("SessionKey to string time: " + (end-start));
	}
	
	@Test
	public void testPerformance5_1() {
		int max = 10000000;
		long start = 0l, end = 0l;
		Random r = new Random();
		String str = "C0A800010000303"+r.nextInt(10);
		
		//Test sessionkey hashcode.
		HashMap<String, String> map = new HashMap<String, String>();
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			str = new String("0000000000000000552DC080541DB33E5F5345535");
			map.get(str);
		}
		end = System.currentTimeMillis();
		System.out.println("HashMap get string time: " + (end-start));
	}
	
	@Test
	public void testPerformance5_2() {
		int max = 10000000;
		long start = 0l, end = 0l;
		Random r = new Random();
		String str = "C0A8000100003036";
		
		//Test sessionkey hashcode.
		HashMap<SessionKey, SessionKey> map = new HashMap<SessionKey, SessionKey>();
		SessionKey key = SessionKey.createSessionKeyFromHexString(str);
		
		start = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			key = SessionKey.createSessionKeyFromRandomString("TEST_");
			map.get(key);
		}
		end = System.currentTimeMillis();
		System.out.println("HashMap get SessionKey time: " + (end-start));
	}
	
	@Test
	public void testDuplicateHash() {
		int max = 1000000;
		long start = 0l, end = 0l;
		InetSocketAddress address = new InetSocketAddress("localhost", 10000);
		//Test sessionkey hashcode.
		SessionKey key = SessionKey.createSessionKey(address);
		
		start = System.currentTimeMillis();
		int foundCount = 0;
		Set<Integer> hashSet = new HashSet<Integer>(10000);
		for ( int i=0; i<max; i++ ) {
			key = SessionKey.createSessionKey(address);
			if ( hashSet.contains(key.hashCode()) ) {
				foundCount++;
			} else {
				hashSet.add(key.hashCode());
			}
		}
		end = System.currentTimeMillis();
		
		System.out.println("SessionKey hash loop " + max + ", duplicate hash code count: " + 
				foundCount + " time: "+ (end-start));
	}
	
	@Test
	public void testDuplicateHashString() {
		int max = 1000000;
		long start = 0l, end = 0l;
		InetSocketAddress address = new InetSocketAddress("localhost", 10000);
		//Test sessionkey hashcode.
		SessionKey key = SessionKey.createSessionKey(address);
		
		start = System.currentTimeMillis();
		int foundCount = 0;
		Set<Integer> hashSet = new HashSet<Integer>(10000);
		for ( int i=0; i<max; i++ ) {
			key = SessionKey.createSessionKey(address);
			int code = key.toString().hashCode();
			if ( hashSet.contains(code) ) {
				foundCount++;
			} else {
				hashSet.add(code);
			}
		}
		end = System.currentTimeMillis();
		
		System.out.println("String loop " + max + ", duplicate hash code count: " + 
				foundCount + " time: "+ (end-start));
	}
	
}
