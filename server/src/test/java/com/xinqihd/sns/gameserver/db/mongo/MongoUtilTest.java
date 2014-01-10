package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.AbstractMongoTest;
import com.xinqihd.sns.gameserver.config.xml.ConfigXmlImporter;

public class MongoUtilTest extends AbstractMongoTest {

	@Before
	public void setUp() throws Exception {
		MongoDBUtil.dropCollection(testDB, null, "mongoutil");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSaveToMongo() {
		DBObject dbObject = new BasicDBObject();
		dbObject.put("_id", 1);
		dbObject.put("key1", "value1");
		dbObject.put("key2", "value2");
		dbObject.put("key3", "value3");
		
		MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		
		DBObject result = MongoDBUtil.queryFromMongo(dbObject, testDB, null, "mongoutil", null);
		
		assertNotNull(result);
		assertEquals(dbObject.get("key1"), result.get("key1"));
		assertEquals(dbObject.get("key2"), result.get("key2"));
		assertEquals(dbObject.get("key3"), result.get("key3"));
	}

	@Test
	public void testQueryFromMongo() {
		DBObject dbObject = new BasicDBObject();
		dbObject.put("_id", 1);
		dbObject.put("key1", "value1");
		dbObject.put("key2", "value2");
		dbObject.put("key3", "value3");
		
		MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		
		DBObject fields = new BasicDBObject();
		fields.put("key1", "1");
		DBObject result = MongoDBUtil.queryFromMongo(dbObject, testDB, null, "mongoutil", fields);
		
		assertNotNull(result);
		assertEquals(dbObject.get("key1"), result.get("key1"));
		assertEquals(null, result.get("key2"));
		assertEquals(null, result.get("key3"));
	}
	
	@Test
	public void testQueryObjectFromMongo() {
		ConfigXmlImporter.importEquipXml("src/test/data/equipment_config.xml", testDB, null, "mongoutil");
		int count = (int)MongoDBUtil.countQueryResult(null, testDB, null, "mongoutil");
		assertEquals(294, count);
		
		DBObject dbObject = new BasicDBObject();
		dbObject.put("_id", "12001");
		Object result = MongoDBUtil.queryObjectFromMongo(dbObject, testDB, null, "mongoutil", null);
//	System.out.println(result);
		assertNotNull(result);
	}
	
	@Test
	public void testQueryAllFromMongo() {
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		}
		
		DBObject fields = new BasicDBObject();
		fields.put("name", "1");
		List<DBObject> result = MongoDBUtil.queryAllFromMongo(new BasicDBObject(), testDB, null, "mongoutil", fields);
		
		assertEquals(10, result.size());
	}
	
	@Test
	public void testQueryAllFromMongo2() {
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		}
		
		List<DBObject> result = MongoDBUtil.queryAllFromMongo(new BasicDBObject(), testDB, null, "mongoutil", null);
		
		assertEquals(10, result.size());
	}
	
	@Test
	public void testQueryAllFromMongo3() {
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		}
		
		List<DBObject> result = MongoDBUtil.queryAllFromMongo(null, testDB, null, "mongoutil", null);
		
		assertEquals(10, result.size());
	}
	
	@Test
	public void testQueryAllFromMongoRange() {
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		}
		
		List<DBObject> result = MongoDBUtil.queryAllFromMongo(null, testDB, null, "mongoutil", null, null, 5, 2);
		
		assertEquals(2, result.size());
		assertEquals(5, result.get(0).get("_id"));
	}
	
	@Test
	public void testDoEval() {
		int x = 3;
		int y = 5;
		String code = "function(x,y) {return x+y}";
		CommandResult result = MongoDBUtil.doEval(testDB, null, "mongoutil", code, new Object[]{x, y});
		//{ "serverUsed" : "mongos.babywar.xinqihd.com:27017" , "retval" : 8.0 , "ok" : 1.0}
		System.out.println(result);
		assertEquals(8, result.getInt("retval"));
	}
	
	@Test
	public void testRemoveDocument() {
		DBObject query = MongoDBUtil.createDBObject();
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mongoutil", true);
		}
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(query, testDB, null, "mongoutil", null);
		assertTrue(list.size()>0);
		MongoDBUtil.removeDocument(testDB, null, "mongoutil", null);
		list = MongoDBUtil.queryAllFromMongo(query, testDB, null, "mongoutil", null);
		assertEquals(0, list.size());
	}
	
	@Test
	public void testCopyCollection() {
		String sourceNamespace = "test";
		String sourceCollection = "mongoutil"; 
		String targetNamespace = "test";
		String targetCollection = "mongoutiltest";
		
		DBObject query = MongoDBUtil.createDBObject();
		int count = 10;
		for ( int i=0; i<count; i++ ) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", i);
			dbObject.put("name", "value"+i);
			MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, sourceNamespace, sourceCollection, true);
		}
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(query, testDB, sourceNamespace, sourceCollection, null);
		assertEquals(count, list.size());
		
		MongoDBUtil.removeDocument(testDB, targetNamespace, targetCollection, null);
		
		String code = 
				"function(sourceDatabase, sourceNamespace, sourceCollection, targetDatabase, targetNamespace, targetCollection) { \n"+
				" var currentdb = db.getSisterDB(sourceDatabase); \n"+
				" var gamedb = db.getSisterDB(targetDatabase); \n"+
				" currentdb.getCollection(sourceNamespace+\".\"+sourceCollection).find().forEach(function(x){gamedb.getCollection(targetNamespace+\".\"+targetCollection).insert(x)}); \n"+
				" return sourceNamespace;\n"+
				"}";
		
		CommandResult result = MongoDBUtil.doEval(
				testDB, sourceNamespace, sourceCollection, code, 
				new Object[]{testDB, sourceNamespace, sourceCollection, 
						testDB, targetNamespace, targetCollection});
		System.out.println(result);
		
		list = MongoDBUtil.queryAllFromMongo(query, testDB, targetNamespace, targetCollection, null);
		assertEquals(count, list.size());
	}
}
