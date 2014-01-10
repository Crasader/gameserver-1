package com.xinqihd.sns.gameserver;

import org.junit.After;
import org.junit.Before;

import com.mongodb.Mongo;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class AbstractMongoTest {

	protected String mongoString = TestConstant.mongoString;
	protected String testDB = "testdb";
	protected Mongo mongo = null;
	
	@Before
	public void setUp() throws Exception {
		String sysConnectString = System.getProperty("mongoconnectstring");
		if ( sysConnectString == null ) {
			System.out.println("You can specify -Dmongoconnectstring="+TestConstant.mongoString+" as the connect string.");
		} else {
			mongoString = sysConnectString;
			System.out.println("Use " + mongoString + " as the Mongo connectString.");
		}
		String host = StringUtil.substring(mongoString, null, ":");
		int port = StringUtil.toInt(mongoString.substring(host.length()), 27017);
		mongo = new Mongo(host, port);
	}

	@After
	public void tearDown() throws Exception {
		mongo.close();
	}
	
}
