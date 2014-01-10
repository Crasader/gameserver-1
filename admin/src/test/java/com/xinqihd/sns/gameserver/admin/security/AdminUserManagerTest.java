package com.xinqihd.sns.gameserver.admin.security;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminUserManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testQueryAdminUser() {
		AdminUser root = new AdminUser();
		root.setUsername("root");
		root.setPassword("r00t");
		root.setEmail("root@xinqihd.com");
		root.addPriviledge(PriviledgeKey.all_priviledge);
		
		AdminUserManager manager = AdminUserManager.getInstance();
		manager.saveAdminUser(root);
		
		AdminUser actualUser = manager.queryAdminUser("root");
		assertNotNull(actualUser);
		assertEquals("root", actualUser.getUsername());
		assertEquals("r00t", actualUser.getPassword());
		assertEquals("root@xinqihd.com", actualUser.getEmail());
		assertEquals(true, actualUser.hasPriviledgeKey(PriviledgeKey.all_priviledge));
		
		manager.removeAdminUser("root");
		actualUser = manager.queryAdminUser("root");
		assertNull(actualUser);
		
	}

}
