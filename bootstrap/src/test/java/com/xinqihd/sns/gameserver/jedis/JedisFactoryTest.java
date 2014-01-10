package com.xinqihd.sns.gameserver.jedis;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.util.TestUtil;

public class JedisFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMultiThread() throws Exception {
		final Jedis jedis = JedisFactory.getJedis();
		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				jedis.set("testthread", "helloworld");
			}
		}, "Jedis Thread Test", 10000, 10);
		assertTrue(true);
	}

	@Test
	public void testMultiThread2() throws Exception {
		final Jedis jedis = JedisFactory.getJedis();
		jedis.set("testthread", "helloworld");
		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				Pipeline pipeline = jedis.pipelined();
				pipeline.set("testthread", "helloworld");
				pipeline.sync();
			}
		}, "Jedis Thread Test", 10000, 10);
	}
	
	@Test
	public void testPersistent() throws Exception {
		final Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.set("testthread", "helloworld");
		assertEquals("helloworld", jedisDB.get("testthread"));
	}
}
