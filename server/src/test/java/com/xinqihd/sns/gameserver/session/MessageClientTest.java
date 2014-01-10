package com.xinqihd.sns.gameserver.session;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;

import org.apache.mina.core.future.WriteFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.server.MessageServer;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class MessageClientTest {

	MessageServer server = MessageServer.getInstance();
	String host = "localhost";
	int port = 65000;
	
	@Before
	public void setUp() throws Exception {
		File classFile = new File("target/classes");
		ReloadClassLoader.newClassloader(classFile.toURL());
		server.startServer(host, port);
	}

	@After
	public void tearDown() throws Exception {
		server.stopServer();
	}

	@Test
	public void testConnectToServer() {
		MessageClient client = new MessageClient(host, port);
		assertTrue(client.connectToServer());
	}
	
	public void testConnectToServer2() throws Exception {
		MessageClient client = new MessageClient(host, port);
		for ( int i=0; i<10; i++ ) {
			assertTrue(client.connectToServer());
		}
		Thread.sleep(100000);
	}
	
	/**
	 * Deprecated test case since the client pool is used.
	 * @throws Exception
	 */
	public void testConnectTimeoutToServer() throws Exception {
		server.stopServer();
		MessageClient client = new MessageClient(host, port);
		assertTrue(!client.connectToServer());
		SessionRawMessage msg = createSessionMessage();
		server.startServer(host, port);
		WriteFuture future = client.sendMessageToServer(msg);
		future.await();
		assertNotNull(future);
	}

	@Test
	public void testSessionIdle() throws Exception {
		GlobalConfig.getInstance().overrideProperty("message.heartbeat.second", "1");
		MessageClient client = new MessageClient(host, port);
		assertTrue(client.connectToServer());
		SessionRawMessage msg = createSessionMessage();
		client.sendMessageToServer(msg);
		Thread.sleep(2000);
		assertTrue(Stat.getInstance().messageHearbeatSent>=1);
	}
	
	@Test
	public void testDisconnect() throws Exception {
		MessageClient client = new MessageClient(host, port);
		assertTrue(client.connectToServer());
		Thread.sleep(1000);
		client.disconnectFromServer();
		Thread.sleep(1000);
		client.connectToServer();
	}
	
	@Test
	public void testDisconnect2() throws Exception {
		MessageClient client = new MessageClient(host, port);
		client.disconnectFromServer();
	}
	
	/**
	 * Old version:
	 * Run MessageClient Stress for 10000. Time:556, Heap:7.5288773M, Thread:2
	 * Run MessageClient Stress for 50000. Time:1089, Heap:17.149239M, Thread:2
	 * Run MessageClient Stress for 50000. Time:1112, Heap:16.45723M, Thread:2
	 * 
	 * New version:
	 * Run MessageClient Stress for 10000. Time:367, Heap:19.728546M, Thread:2
	 * Run MessageClient Stress for 50000. Time:868, Heap:21.511925M, Thread:2
	 * Run MessageClient Stress for 50000. Time:903, Heap:19.639236M, Thread:2
	 * 
	 * @throws Exception
	 */
	public void testStress() throws Exception {
		try {
			GlobalConfig.getInstance().overrideProperty("message.heartbeat.second", "1");
			final SessionRawMessage msg = createSessionMessage();
			
			int max = 10000;
			int threadCount = 2;
			final MessageClient client = new MessageClient(host, port);
			assertTrue(client.connectToServer());
			TestUtil.doPerformMultiThread(new Runnable() {
				public void run() {
					client.sendMessageToServer(msg);
				}
			}, "MessageClient Stress", max, threadCount);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private SessionRawMessage createSessionMessage() {
		SessionRawMessage msg = new SessionRawMessage();
		msg.setSessionkey(SessionKey.createSessionKey(host, port));
		byte[] message = new byte[40];
		Arrays.fill(message, (byte)0);
		msg.setRawMessage(message);
		return msg;
	}
}
