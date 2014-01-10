package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.ServerPojo;

public class ServerListManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddServers() {
		ServerPojo server1 = new ServerPojo();
		server1.setId("s0001");
		server1.setHost("42.120.60.62");
		server1.setPort(3443);
		server1.setName("1区1服");
		server1.setHot(false);
		server1.setNew(false);
		server1.setStartMillis(1353495507577l);
		ServerListManager.getInstance().addServer(server1);
		
		ServerPojo server2 = new ServerPojo();
		server2.setId("s0002");
		server2.setHost("42.120.60.62");
		server2.setPort(3443);
		server2.setName("1区2服");
		server2.setHot(false);
		server2.setNew(false);
		server2.setStartMillis(1353495518141l);
		ServerListManager.getInstance().addServer(server2);
		
		ServerPojo server3 = new ServerPojo();
		server3.setId("s0003");
		server3.setHost("42.121.118.120");
		server3.setPort(3443);
		server3.setName("1区3服");
		server3.setHot(true);
		server3.setNew(false);
		server3.setStartMillis(1358244000933l);
		ServerListManager.getInstance().addServer(server3);
		
		ServerPojo server4 = new ServerPojo();
		server3.setId("s0004");
		server3.setHost("42.121.118.120");
		server3.setPort(3443);
		server3.setName("1区4服");
		server3.setHot(true);
		server3.setNew(true);
		server3.setChannel("appstore");
		server3.setStartMillis(1358244000933l);
		ServerListManager.getInstance().addServer(server3);
	}
	
	@Test
	public void testGetServers() {
		Collection<ServerPojo> servers = ServerListManager.getInstance().getServers();
		for ( ServerPojo server : servers ) {
			System.out.println(server);
		}
		assertTrue(servers.size()>0);
	}
	
	@Test
	public void testAddAndRemoveServerId() {
		Collection<ServerPojo> servers = ServerListManager.getInstance().getServers();
		ServerPojo serverPojo = servers.iterator().next();
		String serverId = serverPojo.getId();
		String roleName = "test-001";
		String prefixedRoleName = ServerListManager.getInstance().addServerPrefix(roleName, serverId);
		assertEquals(serverId+"."+roleName, prefixedRoleName);
		//do it again
		prefixedRoleName = ServerListManager.getInstance().addServerPrefix(roleName, serverId);
		assertEquals(serverId+"."+roleName, prefixedRoleName);
		//Remove it
		String removedRoleName = ServerListManager.getInstance().removeServerPrefix(roleName, serverId);
		assertEquals(roleName, removedRoleName);
		//do it again
		removedRoleName = ServerListManager.getInstance().removeServerPrefix(roleName, serverId);
		assertEquals(roleName, removedRoleName);
	}

}
