package com.xinqihd.sns.gameserver.db;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.jedis.JedisAdapter;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

public class JedisTest {
	
	JedisPoolConfig config = new JedisPoolConfig();
	int max = 100;
	String host = "redis.babywar.xinqihd.com";
	int port = 6379;
	Random r = new Random();

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJedisPool() {
		JedisPool pool = new JedisPool(config, host);
		Jedis jedis = pool.getResource();
		byte[] key = "hello ".getBytes();
		byte[] value = "world".getBytes();
		long startM = 0l, endM = 0l;
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			key[key.length-1] = (byte)(i&0xFF);
			jedis.set(key, value);
			jedis.del(key);
		}
		endM = System.currentTimeMillis();
		System.out.println("Original Jedis loop " + max + " perform: " + (endM-startM));
	}
	
	@Test
	public void testJedisAdapter() {
		JedisAdapter jedis = new JedisAdapter(host, port, config);
		byte[] key = "hello ".getBytes();
		byte[] value = "world".getBytes();
		long startM = 0l, endM = 0l;
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			key[key.length-1] = (byte)(i&0xFF);
			jedis.set(key, value);
			jedis.del(key);
		}
		endM = System.currentTimeMillis();
		System.out.println("JedisAdapter loop " + max + " perform: " + (endM-startM));
	}
	
	@Test
	public void testJedisAdapter2() throws Exception {
		ExecutorService service = Executors.newFixedThreadPool(5);
		
		final JedisAdapter jedis = new JedisAdapter(host, port, config);
		long startM = 0l, endM = 0l;
		final Random r = new Random();
		
		startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			service.submit(new Runnable() {
				@Override
				public void run() {
					jedis.get("hello-"+r.nextInt());
				}
			});
		}
		service.shutdown();
		service.awaitTermination(10, TimeUnit.SECONDS);
		endM = System.currentTimeMillis();
		
		System.out.println("JedisAdapter loop " + max + " perform: " + (endM-startM));
		
		Set<String> strs = jedis.keys("hello*");
		if ( strs != null && strs.size() > 0 ) {
			jedis.del(strs.toArray(new String[0]));
		}
	}
	
	public void testLongSetPerformance1_1() {
		JedisAdapter jedis = new JedisAdapter(host, port, config);

		//Store a long list
		int count = 500000;
		byte[] key = "longlist".getBytes();
		jedis.del(key);
		
		byte[][] longList = createLongList(count);
		Pipeline pipeline = jedis.pipelined();
		for ( int i=0; i<longList.length; i++ ) {
			pipeline.sadd(key, longList[i]);
		}
		pipeline.sync();
		Set<byte[]> userList = jedis.smembers(key);
		assertTrue(userList.size()>0);
		
		//Getting the list.
		long startM = 0l, endM = 0l;
		startM = System.currentTimeMillis();
		int max = 10;
		for ( int i=0; i<max; i++ ) {
			userList = jedis.smembers(key);
		}
		endM = System.currentTimeMillis();
		System.out.println("Redis longset:"+userList.size()+" loop " + max + " perform: " + (endM-startM)/max);
	}
	
	public void testLongSetPerformance1_2() {
		JedisAdapter jedis = new JedisAdapter(host, port, config);

		//Store a long list
		int count = 500000;
		Set<byte[]> userList = jedis.keys("key_*".getBytes());
		for ( byte[] key : userList ) {
			jedis.del(key);
		}
		
		byte[][] longList = createLongList(count);
		Pipeline pipeline = jedis.pipelined();
		for ( int i=0; i<longList.length; i++ ) {
			pipeline.set(("key_"+r.nextInt()).getBytes(), longList[i]);
		}
		pipeline.sync();
		userList = jedis.keys("key_*".getBytes());
		assertTrue(userList.size()>0);
		
		//Getting the list.
		long startM = 0l, endM = 0l;
		startM = System.currentTimeMillis();
		int max=10;
		for ( int i=0; i<max; i++ ) {
			userList = jedis.keys("key_*".getBytes());
		}
		endM = System.currentTimeMillis();
		System.out.println("Redis keypattern:"+ userList.size() + " loop " + max + " perform: " + (endM-startM)/max);
	}
	
	public void testLongSetPerformance1_3() {
		JedisAdapter jedis = new JedisAdapter(host, port, config);

		//Store a long list
		int count = 500000;
		Set<byte[]> userList = jedis.keys("*_key".getBytes());
		for ( byte[] key : userList ) {
			jedis.del(key);
		}
		
		byte[][] longList = createLongList(count);
		Pipeline pipeline = jedis.pipelined();
		for ( int i=0; i<longList.length; i++ ) {
			pipeline.set((r.nextInt()+"_key").getBytes(), longList[i]);
		}
		pipeline.sync();
		userList = jedis.keys("*_key".getBytes());
		assertTrue(userList.size()>0);
		
		//Getting the list.
		long startM = 0l, endM = 0l;
		startM = System.currentTimeMillis();
		int max=10;
		for ( int i=0; i<max; i++ ) {
			userList = jedis.keys("*_key".getBytes());
		}
		endM = System.currentTimeMillis();
		System.out.println("Redis keypattern:"+ userList.size() + " loop " + max + " perform: " + (endM-startM)/max);
	}
	
	
	@Test
	public void testChat() throws Exception {
		final AtomicInteger received = new AtomicInteger(0);
		final AtomicInteger sent = new AtomicInteger(0);
		Thread s = new Thread(new Runnable(){
			public void run() {
				JedisAdapter jedis = new JedisAdapter(host, port, config);
				ChatListener chat = new ChatListener(received);
				jedis.psubscribe(chat, "chat.*");
				received.incrementAndGet();
				System.out.println("finish...");
			}
		});
		
		final int max=100000;
		Thread t = new Thread(new Runnable(){
			public void run() {
				JedisAdapter jedis = new JedisAdapter(host, port, config);
				for ( int i=0; i<max; i++ ) {
					jedis.publish("chat.world", "您的装备已经升级了！");
					sent.incrementAndGet();
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {}
				}
			}
		});
		long startM = System.currentTimeMillis();
		s.start();
		t.start();
		
		int seconds = 2000;
		try {
			t.join(seconds);
			s.join(seconds);
		} catch (Exception e) {
		}
		
		long endM = System.currentTimeMillis();
		System.out.println((endM-startM)+" millis can sent:" + sent.intValue() + ", received: " + received.intValue());
	}
		
		@Test
		public void testChat2() throws Exception {
			final AtomicInteger received = new AtomicInteger(0);
			final AtomicInteger sent = new AtomicInteger(0);
			final int seconds = 1000;
			
			Thread s = new Thread(new Runnable(){
				long time = System.currentTimeMillis();
				JedisAdapter jedis = new JedisAdapter(host, port, config);
				
				public void run() {
					while ( true ) {
						List<String> strs = jedis.blpop(20, "chat.world", "chat.system");
						received.incrementAndGet();
						System.out.println(strs);
					}
//					System.out.println("finish...");
				}
			});
			
			final int max=10;
			Thread t = new Thread(new Runnable(){
				public void run() {
					JedisAdapter jedis = new JedisAdapter(host, port, config);
					for ( int i=0; i<max; i++ ) {
						jedis.rpush("chat.world", "您的装备已经升级了:"+i);
						jedis.rpush("chat.system", "您的装备已经升级了:"+i);
						sent.incrementAndGet();
//						try {
//							Thread.sleep(10);
//						} catch (InterruptedException e) {}
					}
				}
			});
		
		long startM = System.currentTimeMillis();
		s.start();
		t.start();
		
		try {
			t.join(seconds);
			s.join(seconds);
		} catch (Exception e) {
		}
		
		long endM = System.currentTimeMillis();
		System.out.println((endM-startM)+" millis can sent:" + sent.intValue() + ", received: " + received.intValue());
	}
	
	public void testClean() {
		com.xinqihd.sns.gameserver.jedis.Jedis jedis = JedisFactory.getJedis();
		Set<String> strs = jedis.keys("*");
		Pipeline pipeline = jedis.pipelined();
		for ( String key : strs ) {
			pipeline.del(key);
		}
		pipeline.sync();
	}
	
	private byte[][] createLongList(int count) {
		byte[][] list = new byte[count][];
		for ( int i=0; i<list.length; i++ ) {
			list[i] = String.valueOf(r.nextInt()).getBytes();
		}
		return list;
	}
	
	
	class ChatListener extends JedisPubSub {
		
		private AtomicInteger counter =null;
		public ChatListener(AtomicInteger counter) {
			this.counter = counter;
		}
    public void onMessage(String channel, String message) {
    	System.out.println("onMessage: channel: " + channel + ", message: " + message);
    }

    public void onSubscribe(String channel, int subscribedChannels) {
    	System.out.println("onSubscribe: channel: " + channel + ", subscribedChannels: " + subscribedChannels);
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
    	System.out.println("onUnsubscribe: channel: " + channel + ", subscribedChannels: " + subscribedChannels);
    }

    public void onPSubscribe(String pattern, int subscribedChannels) {
    	System.out.println("onPSubscribe: pattern: " + pattern + ", subscribedChannels: " + subscribedChannels);
    }

    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    	System.out.println("onPUnsubscribe pattern: " + pattern + ", subscribedChannels: " + subscribedChannels);
    }

    public void onPMessage(String pattern, String channel, String message) {
//    	System.out.println("onPMessage pattern: " + pattern + ", channel: " + channel + ", message: " + message);
    	this.counter.incrementAndGet();
    }
}
}
