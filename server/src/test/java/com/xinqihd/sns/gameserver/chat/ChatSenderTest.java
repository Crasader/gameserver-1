package com.xinqihd.sns.gameserver.chat;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

public class ChatSenderTest {

	@Before
	public void setUp() throws Exception {
		JedisFactory.initJedis();
		//Clear all sessions
		Jedis jedis = JedisFactory.getJedis();
		
//		Set<byte[]> userList = jedis.keys("*".getBytes());
//		Pipeline pipeline = jedis.pipelined();
//		for ( byte[] key : userList ) {
//			pipeline.del(key);
//		}
//		pipeline.sync();
		
		//Clear all chats
		Set<byte[]> chatList = jedis.keys("Chat*".getBytes());
		for ( byte[] key : chatList ) {
			jedis.del(key);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSendSystemMessage() {
		ChatSender sender = new ChatSender();
		sender.sendSystemMessage("这是一条测试中的系统消息");
		assertTrue(true);
	}
	
	@Test
	public void testFakeSender() throws Exception {
		System.setProperty("usefakesender", "true");
		ChatSender sender = new ChatSender();
		Thread.sleep(100000);
	}

}
