package com.xinqihd.sns.gameserver.config;

import java.util.Collection;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.mongo.TaskManager;

public class TaskPojoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToLuaString() {
		Collection<TaskPojo> tasks = TaskManager.getInstance().getTasks();
		for ( TaskPojo task : tasks ) {
			System.out.println(task.toLuaString(Locale.SIMPLIFIED_CHINESE));
		}
		for ( TaskPojo task : tasks ) {
			System.out.println(task.toLuaString(Locale.TRADITIONAL_CHINESE));
		}
 	}

}
