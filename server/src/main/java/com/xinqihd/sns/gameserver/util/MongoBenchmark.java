package com.xinqihd.sns.gameserver.util;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.xinqihd.sns.gameserver.db.mongo.MapDBObject;
import com.xinqihd.sns.gameserver.entity.user.UserId;

/**
 * Test some very important performance factors of MongoDB
 * @author wangqi
 *
 */
public class MongoBenchmark {
	
	public static void testMongoUserId(int max, DB db) {
		String collName = "testmongobjid";
		DBCollection coll = db.getCollection(collName);
		
		//Setup a sharded collection
		BasicDBObject command = new BasicDBObject();
		command.put("shardcollection", collName);
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		command.put("key", key);
		command.put("unique", true);
		db.command(command);
		
		long startM = System.currentTimeMillis();
		for ( int i=0; i<max; i++ ) {
			BasicDBObject obj = new BasicDBObject();
			obj.put("test", "value-"+i);
			coll.save(obj);
		}
		long endM = System.currentTimeMillis();
		
		System.out.println("Insert " + max + " mongo objectid. time: " + (endM-startM) + " benchmark()");
		
		CommandResult result = db.getStats();
		System.out.println(result);
	}
	
	public static void testMyUserId(int max, DB db) {
		String collName = "testmyuserid";
		DBCollection coll = db.getCollection(collName);
		
		//Setup a sharded collection
		BasicDBObject command = new BasicDBObject();
		command.put("shardcollection", collName);
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		command.put("key", key);
		command.put("unique", true);
		db.command(command);
		
		long startM = System.currentTimeMillis();
		BasicDBObject obj = new BasicDBObject();
		for ( int i=0; i<max; i++ ) {
			UserId userId = new UserId("username"+i);
			obj.put("_id",  userId.getInternal());
			obj.put("test", "value-"+i);
			coll.save(obj);
		}
		long endM = System.currentTimeMillis();
		
		System.out.println("Insert " + max + " my objectid. time: " + (endM-startM) + " benchmark()");
		
		CommandResult result = db.getStats();
		System.out.println(result);
	}
	
	public static void testStringUserId(int max, DB db) {
		String collName = "teststringid";
		DBCollection coll = db.getCollection(collName);
		
		//Setup a sharded collection
		BasicDBObject command = new BasicDBObject();
		command.put("shardcollection", collName);
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		command.put("key", key);
		command.put("unique", true);
		db.command(command);
		
		long startM = System.currentTimeMillis();
		BasicDBObject obj = new BasicDBObject();
		for ( int i=0; i<max; i++ ) {
			obj.put("_id",  "username"+i);
			obj.put("test", "value-"+i);
			coll.save(obj);
		}
		long endM = System.currentTimeMillis();
		
		System.out.println("Insert " + max + " my objectid. time: " + (endM-startM) + " benchmark()");
		
		CommandResult result = db.getStats();
		System.out.println(result);
	}
	
	public static void testBasicBson(int max, DB db) {
		String collName = "testbasicbson";
		DBCollection coll = db.getCollection(collName);
		
		//Setup a sharded collection
		BasicDBObject command = new BasicDBObject();
		command.put("shardcollection", collName);
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		command.put("key", key);
		command.put("unique", true);
		db.command(command);
		
		long startM = System.currentTimeMillis();
		BasicDBObject objKey = new BasicDBObject();
		UserId userId = new UserId("username");
		objKey.put("_id", userId.getInternal());
		
		BasicDBObject obj = new BasicDBObject();
		for ( int i=0; i<max; i++ ) {
			obj.put("_id", userId.getInternal());
			obj.put("test-"+(i)%10, "value-"+i);
			coll.update(objKey, obj, true, false);
		}
		long endM = System.currentTimeMillis();
		
		System.out.println(collName+ " update " + max + " my objectid. time: " + (endM-startM) + " benchmark(56273)");
		
		CommandResult result = db.getStats();
		System.out.println(result);
	}
	
	public static void testMapDBObject(int max, DB db) {
		String collName = "testmapobject";
		DBCollection coll = db.getCollection(collName);
		
		//Setup a sharded collection
		BasicDBObject command = new BasicDBObject();
		command.put("shardcollection", collName);
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		command.put("key", key);
		command.put("unique", true);
		db.command(command);
		
		long startM = System.currentTimeMillis();
		BasicDBObject objKey = new BasicDBObject();
		UserId userId = new UserId("username");
		objKey.put("_id", userId.getInternal());
		
		MapDBObject obj = new MapDBObject();
		for ( int i=0; i<max; i++ ) {
			obj.put("_id", userId.getInternal());
			obj.put("test-"+(i)%10, "value-"+i);
			coll.update(objKey, obj, true, false);
		}
		long endM = System.currentTimeMillis();
		
		System.out.println(collName+ " update " + max + " my objectid. time: " + (endM-startM) + " benchmark(114892)");
		
		CommandResult result = db.getStats();
		System.out.println(result);
	}
	
	public static void main(String[] args) throws Exception {
		if ( args.length < 4 ) {
			System.out.println("MongoBenchmark <host> <port> <db> <max>");
			System.exit(-1);
		}
		String host = args[0];
		int port = StringUtil.toInt(args[1], 27017);
		String database = args[2];
		int max = StringUtil.toInt(args[3], 1);
		
		System.out.println("Connect to " + host + ":" + port + " db: " + database + ", max: " + max);
		Mongo mongo = new Mongo(host, port);
		System.out.println("First compare Mongo native ObjectId, our customized ObjectId and String Id");
		
		DB db = null;
		
//		db = mongo.getDB(database+1);
//		db.dropDatabase();
//		testMongoUserId(max, db);
//		
//		db = mongo.getDB(database+2);
//		db.dropDatabase();
//		testMyUserId(max, db);
//		
//		db = mongo.getDB(database+3);
//		db.dropDatabase();
//		testStringUserId(max, db);
		
		System.out.println("Second: compare Mongo BasicDBObject and our MapDBObject");
		
		db = mongo.getDB(database+4);
		db.dropDatabase();
		testBasicBson(max, db);
		
		db = mongo.getDB(database+5);
		db.dropDatabase();
		testMapDBObject(max, db);
	}

}
