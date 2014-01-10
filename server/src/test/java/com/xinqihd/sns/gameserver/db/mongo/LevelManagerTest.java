package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.LevelPojo;

public class LevelManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetLevel() {
		int count = LevelManager.getInstance().getLevels().size();
		assertEquals(100, count);
		LevelPojo pojo = LevelManager.getInstance().getLevel(1);
		System.out.println(pojo);
		assertNotNull(pojo);
	}

	@Test
	public void testGetLevelZero() {
		LevelPojo pojo = LevelManager.getInstance().getLevel(0);
		System.out.println(pojo);
		assertEquals(1, pojo.getLevel());
	}

	@Test
	public void testGetLevelMax() {
		LevelPojo pojo = LevelManager.getInstance().getLevel(10000);
		System.out.println(pojo);
		assertEquals(100, pojo.getLevel());
	}
}
