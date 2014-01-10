package com.xinqihd.sns.gameserver.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArrayMapTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateMap1() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(2, map.size());
	}
	
	@Test
	public void testCreateMap2() {
		String[] keys = new String[]{"key1"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map;
		try {
			map = ArrayMap.createMap(keys, values);
			fail("should throw exception");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testCreateMap3() {
		String[] keys = new String[0];
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map;
		try {
			map = ArrayMap.createMap(keys, values);
			fail("should throw exception");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testCreateMap4() {
		String[] keys = null;
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map;
		try {
			map = ArrayMap.createMap(keys, values);
			fail("should throw exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void testCreateMap5() {
		String[] keys = new String[]{"key1"};
		String[] values = null;
		Map<String, String> map;
		try {
			map = ArrayMap.createMap(keys, values);
			fail("should throw exception");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testCreatePerformance1_1() throws Exception {
		final String[] keys = new String[]{"key1", "key2"};
		final String[] values = new String[]{"value1", "value2"};
		
		final int max = 100000;
		
		TestUtil.doPerform(new Runnable() {
			@Override
			public void run() {
				ArrayMap.createMap(keys, values);
			}
		}, "ArrayMap create", max);
		
		TestUtil.doPerform(new Runnable() {
			@Override
			public void run() {
				HashMap map = new HashMap<String, String>();
				map.put(keys[0], values[0]);
				map.put(keys[1], values[1]);
			}
		}, "HashMap create", max);
	}
	
	
	@Test
	public void testSize() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(2, map.size());
	}

	@Test
	public void testIsEmpty() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertTrue(!map.isEmpty());
	}

	@Test
	public void testContainsKey1() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(true, map.containsKey("key1"));
		assertEquals(true, map.containsKey("key2"));
		assertEquals(false, map.containsKey("key3"));
	}
	
	@Test
	public void testContainsKey2() {
		byte[][] keys = {"key1".getBytes(), "key2".getBytes()};
		byte[][] values = {"value1".getBytes(), "value2".getBytes()};
		Map<byte[], byte[]> map = ArrayMap.createMap(keys, values);
		Map<byte[], byte[]> hashMap = new HashMap<byte[], byte[]>();
		for ( int i=0; i<keys.length; i++ ) {
			hashMap.put(keys[i], values[i]);
		}
		assertEquals(hashMap.containsKey("key1".getBytes()), map.containsKey("key1".getBytes()));
		assertEquals(hashMap.containsKey("key2".getBytes()), map.containsKey("key2".getBytes()));
		assertEquals(hashMap.containsKey("key3".getBytes()), map.containsKey("key3".getBytes()));
	}

	@Test
	public void testContainsValue1() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(true, map.containsValue("value1"));
		assertEquals(true, map.containsValue("value2"));
		assertEquals(false, map.containsValue("value3"));
	}
	
	@Test
	public void testContainsValue2() {
		byte[][] keys = {"key1".getBytes(), "key2".getBytes()};
		byte[][] values = {"value1".getBytes(), "value2".getBytes()};
		Map<byte[], byte[]> map = ArrayMap.createMap(keys, values);
		Map<byte[], byte[]> hashMap = new HashMap<byte[], byte[]>();
		for ( int i=0; i<keys.length; i++ ) {
			hashMap.put(keys[i], values[i]);
		}
		assertEquals(hashMap.containsKey("value1".getBytes()), map.containsKey("value1".getBytes()));
		assertEquals(hashMap.containsKey("value2".getBytes()), map.containsKey("value2".getBytes()));
		assertEquals(hashMap.containsKey("value3".getBytes()), map.containsKey("value3".getBytes()));
	}

	@Test
	public void testGet() {
		String[] keys = new String[]{"key1", "key2"};
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals("value1", map.get("key1"));
		assertEquals("value2", map.get("key2"));
		assertEquals(null, map.get("key3"));
	}
	
	@Test
	public void testGetPerformance() throws Exception {
		final String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		final String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		final Map<String, String> arrayMap = ArrayMap.createMap(keys, values);
		final Map<String, String> hashMap = new HashMap<String, String>();
		for ( int i=0; i<keys.length; i++ ) {
			hashMap.put(keys[i], values[i]);
		}
		
		final Random r = new Random();
		final int max = 100000;
		
		TestUtil.doPerform(new Runnable() {
			@Override
			public void run() {
				arrayMap.get("key"+r.nextInt(100));
			}
		}, "ArrayMap get", max);
		
		TestUtil.doPerform(new Runnable() {
			@Override
			public void run() {
				hashMap.get("key"+r.nextInt(100));
			}
		}, "HashMap get", max);
	}
	

	@Test
	public void testPut() {
		String[] keys = new String[]{"key1", "key2"};;
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		try {
			map.put("key3", "value3");
			fail("should throw exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void testRemove() {
		String[] keys = new String[]{"key1", "key2"};;
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		try {
			map.remove("key3");
			fail("should throw exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void testPutAll() {
		String[] keys = new String[]{"key1", "key2"};;
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		try {
			map.putAll(new HashMap<String,String>());
			fail("should throw exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void testClear() {
		String[] keys = new String[]{"key1", "key2"};;
		String[] values = new String[]{"value1", "value2"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		try {
			map.clear();
			fail("should throw exception");
		} catch (Exception e) {
		}
	}
	
	@Test
	public void testKeySet() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(5, map.keySet().size());
		Iterator iterator = map.keySet().iterator();
		for ( int i=0; i<keys.length; i++ ) {
			String type = (String) iterator.next();
			assertEquals(keys[i], type);
		}
	}

	@Test
	public void testKeySet1_1() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(5, map.keySet().size());
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.keySet();
		}
		endM = System.currentTimeMillis();
		System.out.println("ArrayMap#keySet loop " + max + " instance perform: " + (endM-startM));

	}
	
	@Test
	public void testKeySet1_2() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = new HashMap<String, String>();
		for ( int i=0; i<keys.length; i++ ) {
			map.put(keys[i], values[i]);
		}
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.keySet();
		}
		endM = System.currentTimeMillis();
		System.out.println("HashMap#keySet loop " + max + " instance perform: " + (endM-startM));
		
	}

	@Test
	public void testValues() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(5, map.values().size());
		Iterator iterator = map.values().iterator();
		for ( int i=0; i<values.length; i++ ) {
			String type = (String) iterator.next();
			assertEquals(values[i], type);
		}
	}
	
	@Test
	public void testValues1_1() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(5, map.keySet().size());
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.values();
		}
		endM = System.currentTimeMillis();
		System.out.println("ArrayMap#values loop " + max + " instance perform: " + (endM-startM));

	}
	
	@Test
	public void testValues1_2() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = new HashMap<String, String>();
		for ( int i=0; i<keys.length; i++ ) {
			map.put(keys[i], values[i]);
		}
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.values();
		}
		endM = System.currentTimeMillis();
		System.out.println("HashMap#values loop " + max + " instance perform: " + (endM-startM));
		
	}

	@Test
	public void testEntrySet() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		Map<String, String> hashMap = new LinkedHashMap<String, String>();
		for ( int i=0; i<keys.length; i++ ) {
			hashMap.put(keys[i], values[i]);
		}
		Set<Entry<String, String>> set1 = map.entrySet();
		Set<Entry<String, String>> set2 = hashMap.entrySet();
		Iterator iterator1 = set1.iterator();
		for (Iterator iterator2 = set2.iterator(); iterator2.hasNext();) {
			Entry<String, String> entry1 = (Entry<String, String>) iterator1.next();
			Entry<String, String> entry2 = (Entry<String, String>) iterator2.next();
			assertEquals(entry2.getKey(), entry1.getKey());
			assertEquals(entry2.getValue(), entry1.getValue());
		}
	}

	@Test
	public void testEntrySet1_1() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = ArrayMap.createMap(keys, values);
		assertEquals(5, map.keySet().size());
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.entrySet();
		}
		endM = System.currentTimeMillis();
		System.out.println("ArrayMap#entrySet loop " + max + " instance perform: " + (endM-startM));

	}
	
	@Test
	public void testEntrySet1_2() {
		String[] keys = new String[]{"key1", "key2", "key3", "key4", "key5"};
		String[] values = new String[]{"value1", "value2", "value3", "value4", "value5"};
		Map<String, String> map = new HashMap<String, String>();
		for ( int i=0; i<keys.length; i++ ) {
			map.put(keys[i], values[i]);
		}
		
		int max = 100000;
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			map.entrySet();
		}
		endM = System.currentTimeMillis();
		System.out.println("HashMap#entrySet loop " + max + " instance perform: " + (endM-startM));
		
	}
}
