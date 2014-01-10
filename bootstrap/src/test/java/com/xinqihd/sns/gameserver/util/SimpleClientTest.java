package com.xinqihd.sns.gameserver.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.apache.mina.core.filterchain.IoFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

public class SimpleClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalcNextTimeout() {
		//1st
		int count = 0;
		long nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(4000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(8000, nextTimeout);

		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(16000, nextTimeout);

		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(32000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(64000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(128000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(256000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(512000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(1024000, nextTimeout);
		
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(2000, nextTimeout);
	
		nextTimeout = SimpleClient.calcNextTimeout(count++, 0);
		assertEquals(4000, nextTimeout);
		
	}
	
	public void testConnectToServerTimeout() throws Exception {
		IoFilter filter = createNiceMock(IoFilter.class);
		SimpleClient client = new SimpleClient(filter, "10.0.0.1", 3000);
		client.sendMessageToServer(new Object());
		Jedis jedis = JedisFactory.getJedis();
		Thread.sleep(3000);
		assertTrue(jedis.exists("client_timeout_map:10.0.0.1:3000"));
		Thread.sleep(2000);
		assertFalse(jedis.exists("client_timeout_map:10.0.0.1:3000"));
	}

}
