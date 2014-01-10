package com.xinqihd.sns.gameserver;

import org.junit.After;
import org.junit.Before;

public class AbstractTest {
	
	protected String connectString = TestConstant.connectString;
	
	@Before
	public void setUp() throws Exception {
		String sysConnectString = System.getProperty("zooconnectstring");
		if ( sysConnectString == null ) {
			System.out.println("You can specify -Dzooconnectstring="+TestConstant.connectString+" as the connect string.");
		} else {
			connectString = sysConnectString;
		}
		System.out.println("Use " + connectString + " as the ZooKeeper connectString.");
//		zooKeeper = ZooKeeperFactory.getInstance(connectString).getZooKeeper();
	}

	@After
	public void tearDown() throws Exception {
	}
	
}
