package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class DBObjectFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddDBObject1() {
		DBObject obj = MongoDBUtil.addDBObject(null, null, "login", "password");
		assertNotNull(obj);
		assertEquals("password", obj.get("login"));
	}
	
	@Test
	public void testAddDBObject2() {
		DBObject parent = new BasicDBObject();
		DBObject obj = MongoDBUtil.addDBObject(parent, "user", "login", "password");
		assertNotNull(obj);
		DBObject r = (DBObject)(obj.get("user"));
		assertEquals("password", r.get("login"));
	}

	@Test
	public void testAddDBObject3() {
		DBObject parent = new BasicDBObject();
		parent.put("user", "helloworld");
		DBObject obj = MongoDBUtil.addDBObject(parent, "user", "login", "password");
		assertNotNull(obj);
		DBObject r = (DBObject)(obj.get("user"));
		assertEquals("password", r.get("login"));
	}
}
