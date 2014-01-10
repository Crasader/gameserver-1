package com.xinqihd.sns.gameserver.admin.config;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfigManagerTest {
	
	File file = new File(ConfigManager.DEFAULT_CONFIG_FILE_PATH);

	@Before
	public void setUp() throws Exception {
		file.delete();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetConfigAsString() {
		String username = "test";
		String password = "1234";
		ConfigManager.saveConfigKeyValue(ConfigKey.adminUsername, username);
		ConfigManager.saveConfigKeyValue(ConfigKey.adminPassword, password);
		
		assertEquals(username, ConfigManager.getConfigAsString(ConfigKey.adminUsername));
		assertEquals(password, ConfigManager.getConfigAsString(ConfigKey.adminPassword));
	}

	@Test
	public void testGetConfigAsStringArray() {
		List<String> password = new ArrayList<String>();
		password.add("1234");
		password.add("5678");
		ConfigManager.saveConfigKeyValue(ConfigKey.adminPassword, password);
		
		assertEquals("1234", ConfigManager.getConfigAsString(ConfigKey.adminPassword));
		Set<String> set = ConfigManager.getConfigAsStringArray(ConfigKey.adminPassword);
		assertNotNull(set);
	}
	
}