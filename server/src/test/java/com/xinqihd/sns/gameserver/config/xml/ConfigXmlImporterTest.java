package com.xinqihd.sns.gameserver.config.xml;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.After;
import org.junit.Before;

import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;

public class ConfigXmlImporterTest {
	
	/*
	String database = "babywar";
	String namespace = "server0001";
	String collection = "equipments";
	*/
	String database = "testdb";
	String namespace = null;
	String collection = "equipments";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void testImportXml() {
		ConfigXmlImporter.importEquipXml("src/test/data/equipment_config.xml", database, namespace, collection);
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "equipments");
		assertEquals(count, 323);
	}
	
	public void testImportMapXml() {
		ConfigXmlImporter.importMapXml("src/test/data/map_config.xml", database, namespace, "maps");
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "maps");
		assertEquals(count, 25);
	}
	
	public void testImportItemXml() {
		ConfigXmlImporter.importItemXml("src/test/data/item_config.xml", database, namespace, "items", new HashSet<String>());
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "items");
		assertEquals(count, 125);
	}

	public void testImportShopDat() {
		ConfigXmlImporter.importShopDatFile("src/test/data/shop_multi_currency.dat", database, namespace, "shops", new HashSet<String>());
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "shops");
		assertEquals(count, 434);
	}
	
	public void testImportTaskXml() {
		ConfigXmlImporter.importTaskXml("src/test/data/task_config.xml", database, namespace, "tasks");
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "tasks");
		assertEquals(count, 148);
	}
	
	public void testImportDailyMark() {
		ConfigXmlImporter.importDailyMarkDatFile("src/test/data/dailymark_reward.dat", database, namespace, "dailymarks");
		int count = (int)MongoDBUtil.countQueryResult(null, database, namespace, "dailymarks");
		assertEquals(count, 5);
	}
}
