package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.AbstractMongoTest;
import com.xinqihd.sns.gameserver.Warmup;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;

public class MapDBObjectTest extends AbstractMongoTest {

	@Before
	public void setUp() throws Exception {
		Warmup.warmup();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testPutAll() {
		PropData propData = new PropData();
		propData.setName("夺命刀");
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		
		MapDBObject mapDBObject = new MapDBObject();
		mapDBObject.putAll(propData);
		
		assertEquals("夺命刀", mapDBObject.get("name"));
		assertEquals(Boolean.TRUE, mapDBObject.get("banded"));
		assertEquals("BONUS", mapDBObject.get("valuetype"));
		assertEquals(null, mapDBObject.get("serialVersionUID"));
	}

	@Test
	public void testPutAllObject() {
		PropData propData = new PropData();
		propData.setName("夺命刀");
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		
		int max = 100000;
		long startM = System.currentTimeMillis();
		try {
			for ( int i=0; i<max; i++ ) {
				MapDBObject dbObject = new MapDBObject();
				dbObject.putAll(makePropData(propData, i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endM = System.currentTimeMillis();
		System.out.println("MapDBObject.putAll(Object) loop " + max + ", time: " + (endM-startM));
		
		startM = System.currentTimeMillis();
		try {
			for ( int i=0; i<max; i++ ) {
				putDBObject(makePropData(propData, i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		endM = System.currentTimeMillis();
		System.out.println("MapDBObject.putHash(Object) loop " + max + ", time: " + (endM-startM));
	}
	
	@Test
	public void testSaveAndQuery() {
		MapDBObject dbObject = new MapDBObject();
		dbObject.put("_id", 1);
		dbObject.put("key1", "value1");
		dbObject.put("key2", "value2");
		dbObject.put("key3", "value3");
		
		MongoDBUtil.saveToMongo(dbObject, dbObject, testDB, null, "mapobj", true);
		
		DBObject fields = new BasicDBObject();
		fields.put("key1", "1");
		DBObject result = MongoDBUtil.queryFromMongo(dbObject, testDB, null, "mapobj", fields);
		
		assertNotNull(result);
		assertEquals(dbObject.get("key1"), result.get("key1"));
		assertEquals(null, result.get("key2"));
		assertEquals(null, result.get("key3"));
	}
	
	private PropData makePropData(PropData propData, int i) {
		propData.setAgilityLev(1000+i);
		propData.setAttackLev(1000+i);
		propData.setDefendLev(1001+i);
		propData.setDuration(1002+i);
		propData.setItemId("510"+i*10);
		propData.setLuckLev(1003+i);
		propData.setSign(1003+i);
		return propData;
	}

	private DBObject putDBObject(PropData propData) {
		BasicDBObject dbObj = new BasicDBObject();
		dbObj.put("AgilityLev", propData.getAgilityLev());
		dbObj.put("AttackLev", propData.getAttackLev());
		dbObj.put("Banded", propData.isBanded());
		dbObj.put("DefendLev", propData.getDefendLev());
		dbObj.put("Duration", propData.getDuration());
		dbObj.put("ItemId", propData.getItemId());
		dbObj.put("LuckLev", propData.getLuckLev());
		dbObj.put("Name", propData.getName());
		dbObj.put("Sign", propData.getSign());
		dbObj.put("ValueType", propData.getValuetype());
		return dbObj;
	}
}
