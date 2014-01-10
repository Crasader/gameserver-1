package com.xinqihd.sns.gameserver.handler;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;

public class AbstractHandlerTest extends AbstractTest  {
	
	private String database = "testdb"; 

	public void setUp(boolean enableZoo, String collection, String index) throws Exception {
		super.setUp();
		if ( !enableZoo ) {
			// Shutdown the Zookeeper because we do not need it.
			GlobalConfig.getInstance().overrideProperty("zookeeper.root", "/not-exist-dir");
			// Change the database to testdb
		}
		GlobalConfig.getInstance().overrideProperty("mongdb.database", database);
		GlobalConfig.getInstance().overrideProperty("mongdb.namespace", null);
		
		MongoDBUtil.dropCollection(database, null, collection);
		if ( index != null ) {
			MongoDBUtil.ensureIndex(database, null, collection, index, true);
		}
		
//		System.out.println(" ============: AbstractHandlerTest: database:"+database+"." + collection+", index:" + index);
	}

	public void tearDown() throws Exception {
	}


}
