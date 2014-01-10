package com.xinqihd.sns.gameserver.transport;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.server.GameServer;

public class GameClientTest {
	
	GameServer server = GameServer.getInstance();
	String host = "localhost";
	int port = 65500;

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
		try {
			GameClient client = new GameClient(host, port);
			assertTrue(client.connectToServer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSessionIdle() throws Exception {
		GlobalConfig.getInstance().overrideProperty("message.heartbeat.second", "1");
		GameClient client = new GameClient(host, port);
		assertTrue(client.connectToServer());
		Thread.sleep(2000);
		assertTrue(Stat.getInstance().messageHearbeatSent>=1);
	}
	
	@Test
	public void testDisconnect() throws Exception {
		GameClient client = new GameClient(host, port);
		assertTrue(client.connectToServer());
		Thread.sleep(1000);
		client.disconnectFromServer();
		Thread.sleep(1000);
		client.connectToServer();
	}
	
	public void testStress() throws Exception {
		BceLogin.Builder login = BceLogin.newBuilder();
		login.setUsername("test-001");
		login.setPassword("000000");
		BceLogin payload = login.build();
		XinqiMessage msg = new XinqiMessage();
		msg.index = 0;
		msg.type = MessageToId.messageToId(payload);
		msg.payload = payload;
		
		GameClient client = new GameClient(host, port);
		assertTrue(client.connectToServer());
		for ( int i=0; i<100; i++ ) { 
			client.sendMessageToServer(msg);
			System.out.println("send message #" + i);
		}
		Thread.sleep(100);
		System.out.println(Stat.getInstance().toString());
	}

}
