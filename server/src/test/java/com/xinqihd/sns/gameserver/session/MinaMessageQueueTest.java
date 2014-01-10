package com.xinqihd.sns.gameserver.session;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class MinaMessageQueueTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetUpMessageClient() throws Exception {
		try {
			MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
			ConcurrentHashMap<String, MessageClient> messageClientMap = 
					(ConcurrentHashMap<String, MessageClient>)
					TestUtil.getPrivateFieldValue("messageClientMap", messageQueue);
			
			ArrayList<String> serverList = new ArrayList<String>();
			messageQueue.setUpMessageClient(serverList);
			
			assertEquals(0, messageClientMap.keySet().size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSetUpMessageClient2() throws Exception {
		MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
		TestUtil.setPrivateFieldValue("localMessageServerId", messageQueue, "localhost:10000");
		ConcurrentHashMap<String, MessageClient> messageClientMap = 
				(ConcurrentHashMap<String, MessageClient>)
				TestUtil.getPrivateFieldValue("messageClientMap", messageQueue);
		
		ArrayList<String> serverList = new ArrayList<String>();
		serverList.add("localhost:10002");
		serverList.add("localhost:10001");
		serverList.add("localhost:10000");
		messageQueue.setUpMessageClient(serverList);
		
		serverList.clear();
		serverList.add("localhost:10002");
		serverList.add("localhost:10001");
		serverList.add("localhost:10000");
		serverList.add("localhost:10003");
		messageQueue.setUpMessageClient(serverList);
		assertEquals(3, messageClientMap.keySet().size());
		
		serverList.clear();
		serverList.add("localhost:10005");
		serverList.add("localhost:10001");
		serverList.add("localhost:10000");
		serverList.add("localhost:10004");
		messageQueue.setUpMessageClient(serverList);
//		System.out.println(messageClientMap);
		assertEquals(3, messageClientMap.keySet().size());
		assertNotNull(messageClientMap.get("localhost:10001"));
		assertNotNull(messageClientMap.get("localhost:10004"));
		assertNotNull(messageClientMap.get("localhost:10005"));
		
		messageQueue.destroyQueue();
		assertEquals(0, messageClientMap.keySet().size());
	}
	
	@Test
	public void testLocalSessionWrite() throws Exception {
		SessionRawMessage rawLocalMessage = createSessionRawMessage(40, 20000);
		
		MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
		//Replace MinaMessageQueue's message queue.
		TestUtil.setPrivateFieldValue("localMessageServerId", messageQueue, "localhost:10000");
		ConcurrentHashMap<String, MessageClient> messageClientMap = 
				(ConcurrentHashMap<String, MessageClient>)
				TestUtil.getPrivateFieldValue("messageClientMap", messageQueue);
		
		LinkedBlockingQueue internalMessageQueue = (LinkedBlockingQueue)createMock(LinkedBlockingQueue.class);
		internalMessageQueue.put(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object message = getCurrentArguments()[0];
//				System.out.println("====="+message);
				assertNotNull(message);
				return null;
			}
		}).anyTimes();
		Object oldMessageQueue = TestUtil.getPrivateFieldValue("messageQueue", messageQueue);
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, internalMessageQueue);
		
		IoSession localSession = createNiceMock(IoSession.class);
		expect(localSession.getRemoteAddress()).andReturn(new InetSocketAddress("localhost", 20000)).anyTimes();
		

		MessageClient messageClient = createMock(MessageClient.class);
		messageClientMap.put("localhost:10001", messageClient);
		
		ArrayList<String> serverList = new ArrayList<String>();
		serverList.add("localhost:10002");
		serverList.add("localhost:10001");
		serverList.add("localhost:10000");
		messageQueue.setUpMessageClient(serverList);
		assertEquals(2, messageClientMap.keySet().size());
		
		replay(messageClient);
		replay(localSession);
		replay(internalMessageQueue);
		
		messageQueue.sessionWrite(rawLocalMessage.getSessionkey(), rawLocalMessage);
		
		verify(messageClient);
		verify(localSession);
		verify(internalMessageQueue);
		
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, oldMessageQueue);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRemoteSessionWrite() throws Exception {
		SessionRawMessage rawLocalMessage = createSessionRawMessage(40, 20000);
		SessionRawMessage rawRemoteMessage = createSessionRawMessage(40, 20001);
		
		MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
		TestUtil.setPrivateFieldValue("localMessageServerId", messageQueue, "localhost:10000");
		ConcurrentHashMap<String, MessageClient> messageClientMap = 
				(ConcurrentHashMap<String, MessageClient>)
				TestUtil.getPrivateFieldValue("messageClientMap", messageQueue);
		
		LinkedBlockingQueue internalMessageQueue = (LinkedBlockingQueue)createNiceMock(LinkedBlockingQueue.class);
		internalMessageQueue.put(anyObject());
		expectLastCall().anyTimes();
		Object oldMessageQueue = TestUtil.getPrivateFieldValue("messageQueue", messageQueue);
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, internalMessageQueue);
		
		IoSession localSession = createNiceMock(IoSession.class);
		expect(localSession.getRemoteAddress()).andReturn(new InetSocketAddress("localhost", 20001)).anyTimes();
		

		MessageClient messageClient = createMock(MessageClient.class);
		messageClientMap.put("localhost:10001", messageClient);
		
		ArrayList<String> serverList = new ArrayList<String>();
		serverList.add("localhost:10002");
		serverList.add("localhost:10001");
		serverList.add("localhost:10000");
		messageQueue.setUpMessageClient(serverList);
		assertEquals(2, messageClientMap.keySet().size());
		
		replay(messageClient);
		replay(localSession);
		replay(internalMessageQueue);
		
		messageQueue.sessionWrite(rawLocalMessage.getSessionkey(), rawLocalMessage);
		
		verify(messageClient);
		verify(localSession);
		verify(internalMessageQueue);
		
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, oldMessageQueue);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTryToCreateMessageClient() throws Exception {
		SessionRawMessage rawLocalMessage = createSessionRawMessage(40, 20000);
		SessionRawMessage rawRemoteMessage = createSessionRawMessage(40, 20001);
		
		MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
		TestUtil.setPrivateFieldValue("localMessageServerId", messageQueue, "localhost:10000");
		ConcurrentHashMap<String, MessageClient> messageClientMap = 
				(ConcurrentHashMap<String, MessageClient>)
				TestUtil.getPrivateFieldValue("messageClientMap", messageQueue);
		messageClientMap.clear();
		
		LinkedBlockingQueue internalMessageQueue = (LinkedBlockingQueue)createNiceMock(LinkedBlockingQueue.class);
		internalMessageQueue.put(anyObject());
		expectLastCall().anyTimes();
		Object oldMessageQueue = TestUtil.getPrivateFieldValue("messageQueue", messageQueue);
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, internalMessageQueue);
		
		GameContext gameContext = GameContext.getTestInstance();
		SessionManager sessionManager = createNiceMock(SessionManager.class);
		//Create the fake machine id
		String fakeMachineId = "www.baidu.com:80";
		expect(sessionManager.findSessionMachineId(anyObject(SessionKey.class))).andReturn(fakeMachineId);
		TestUtil.setPrivateFieldValue("sessionManager", gameContext, sessionManager);
		
		MessageClient messageClient = createMock(MessageClient.class);
		
		assertEquals(0, messageClientMap.keySet().size());
		
		replay(messageClient);
		replay(internalMessageQueue);
		replay(sessionManager);
		
		messageQueue.sessionWrite(rawLocalMessage.getSessionkey(), rawLocalMessage);
		
		verify(messageClient);
		verify(internalMessageQueue);
		verify(sessionManager);
		
		assertEquals(1, messageClientMap.keySet().size());
		assertTrue(messageClientMap.get(fakeMachineId) != null );
		
		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, oldMessageQueue);
	}
	
	/**
	 * ZooKeeper is deprecated
	 * @throws Exception
	 */
	/*
	public void testConfigWatch() throws Exception {
		//Setup zookeeper
		super.setUp();
		final String messageServerZkRoot = "/testconfig/messages/list"; 
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_MESSAGE_LIST_ROOT, messageServerZkRoot);
		final String localMessageServerId = "localhost:12345";
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, localMessageServerId);
		
		final ZooKeeper zooKeeper = ZooKeeperFactory.getInstance().
				getZooKeeper();
		ZooKeeperUtil.deleteZNode(messageServerZkRoot, zooKeeper);
		ZooKeeperUtil.createZNode(messageServerZkRoot+"/www.baidu.com:80", null, zooKeeper);
		ZooKeeperUtil.createZNode(messageServerZkRoot+"/www.weibo.com:80", null, zooKeeper);
		ZooKeeperUtil.createZNode(messageServerZkRoot+"/www.hao123.com:80", null, zooKeeper);
		
		final Semaphore semaphore = new Semaphore(1);

		semaphore.acquire();
		System.out.println("Start the MinaMessageQueue");

		final MinaMessageQueue minaQueue = new MinaMessageQueue();
		//Replace messageClientMap
		final ConcurrentHashMap<String, MessageClient> messageClientMap = new ConcurrentHashMap<String, MessageClient>(8);
		TestUtil.setPrivateFieldValue("messageClientMap", minaQueue, messageClientMap);
		minaQueue.initQueue();
		
		//Check the clientmap size
		assertEquals(3, messageClientMap.size());
					
		
		//Change the ZooKeeper MapConfig
		Thread changingThread = new Thread(new Runnable(){
			public void run() {
				try {
						semaphore.acquire();
						System.out.println("ChangingThread get the semaphore");
						
						ZooKeeperUtil.deleteZNode(messageServerZkRoot+"/www.weibo.com:80", zooKeeper);
						System.out.println("ChangingThread wait for 1000 millis");
						Thread.sleep(1000);
						ZooKeeperUtil.createZNode(messageServerZkRoot+"/www.163.com:80", null, zooKeeper);
						System.out.println("ChangingThread wait for 1000 millis");
						Thread.sleep(1000);
						ZooKeeperUtil.createZNode(messageServerZkRoot+"/www.taobao.com:80", null, zooKeeper);
						System.out.println("ChangingThread wait for 1000 millis");
						Thread.sleep(1000);

						System.out.println("ChangingThread add a new message server and release the semaphore");
						semaphore.release();
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
		});
		changingThread.start();
		
		System.out.println("MinaMessageQueue is waiting for changing zookeeper...");
		semaphore.release();
		
		Thread.sleep(1000);
		
		semaphore.acquire();
		
		System.out.println("MinaMessageQueue get the semaphore again.");

		//Check the clientmap size again.
		System.out.println(messageClientMap);
		assertTrue(messageClientMap.size()>=3);
		
		changingThread.join();
	}
	*/
	
	private SessionRawMessage createSessionRawMessage(int rawByteLength, int port) {
		SessionKey sessionKey = SessionKey.createSessionKey("localhost", port);
		if ( rawByteLength < 0 ) {
			rawByteLength = 0;
		}
		byte[] raw = new byte[rawByteLength];
		Arrays.fill(raw, (byte)0);
		SessionRawMessage rawMessage = new SessionRawMessage();
		rawMessage.setSessionkey(sessionKey);
		rawMessage.setRawMessage(raw);
		return rawMessage;
	}
}
