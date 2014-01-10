package com.xinqihd.sns.gameserver.config;

import static org.junit.Assert.*;

import java.util.List;

//import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the ZooKeeperFactory.
 * @author wangqi
 *
 */
public class TestZooKeeperFactory {
	
	/*
	String connectString = TestConstant.connectString;

	@Before
	public void setUp() throws Exception {
		String sysConnectString = System.getProperty("zooconnectstring");
		if ( sysConnectString == null ) {
			System.out.println("You can specify -Dzooconnectstring="+TestConstant.connectString+" as the connect string.");
		} else {
			connectString = sysConnectString;
			System.out.println("Use " + connectString + " as the ZooKeeper connectString.");
		}
	}

	@After
	public void tearDown() throws Exception {
//		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
//		ZooKeeperUtil.deleteZNode("/testroot", zooKeeper.getZooKeeper());
	}

	@Test
	public void testGetInstance() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
		assertNotNull(zooKeeper);
		assertEquals(zooKeeper.hashCode(), ZooKeeperFactory.getInstance(connectString).hashCode());
		zooKeeper.getZooKeeper().close();
		ZooKeeperFacade zooKeeper2 = ZooKeeperFactory.getInstance(connectString);
		assertNotSame(zooKeeper.getZooKeeper(), zooKeeper2.getZooKeeper());
	}
	
	@Test
	public void testCreateZNode() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
		String path = "/testroot/testparent/testchild";
		//Test create process
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		//Test create the same path again.
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		String dataPath = path + "/testdata";
		String dataHello = "hello";
		String dataWorld = "world";
		//Test add the data node 
		ZooKeeperUtil.createZNode(dataPath, dataHello.getBytes(), zooKeeper.getZooKeeper());
		String value = new String(zooKeeper.getZooKeeper().getData(dataPath, false, null));
		assertEquals("hello", value);
		//Test change the data;
		ZooKeeperUtil.createZNode(dataPath, dataWorld.getBytes(), zooKeeper.getZooKeeper());
		value = new String(zooKeeper.getZooKeeper().getData(dataPath, false, null));
		assertEquals("world", value);
	}
	
	@Test
	public void testCreateZNodeSequential() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
		String path = "/testroot/testparent/testchild";
		//Test create process
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		//Test create the same path again.
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		String dataPath = path + "/testdata";
		String dataHello = "hello";
		String dataWorld = "world";
		//Test add the data node 
		ZooKeeperUtil.createZNode(dataPath, dataHello.getBytes(), zooKeeper.getZooKeeper());
		String value = new String(zooKeeper.getZooKeeper().getData(dataPath, false, null));
		assertEquals("hello", value);
		//Test change the data;
		ZooKeeperUtil.createZNode(dataPath, dataWorld.getBytes(), zooKeeper.getZooKeeper());
		value = new String(zooKeeper.getZooKeeper().getData(dataPath, false, null));
		assertEquals("world", value);
	}
	
	@Test
	public void testDeleteZNode() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
		String path = "/testroot/testparent/testchild";
		//Test create process
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		String dataPath1 = path + "/testdata1";
		String dataPath2 = path + "/testdata2";
		String dataHello = "hello";
		String dataWorld = "world";
		//Test add the data node 
		ZooKeeperUtil.createZNode(dataPath1, dataHello.getBytes(), zooKeeper.getZooKeeper());
		ZooKeeperUtil.createZNode(dataPath2, dataWorld.getBytes(), zooKeeper.getZooKeeper());
		boolean result = ZooKeeperUtil.deleteZNode("/testroot/testparent", zooKeeper.getZooKeeper());
		assertTrue(result);
		Stat stat = zooKeeper.getZooKeeper().exists("/testroot/testparent", false);
		assertNull(stat);
		//Test delete non-exist dir.
		result = ZooKeeperUtil.deleteZNode("/testroot123456", zooKeeper.getZooKeeper());
		assertTrue(result);
	}
	
	@Test
	public void testGetValue() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
		String path = "/testroot/testparent/testchild";
		//Test create process
		ZooKeeperUtil.createZNode(path, null, zooKeeper.getZooKeeper());
		String dataPath = path + "/testdata";
		String priorityDataPath = path + "/localhost" + "/testdata";
		String dataHello = "hello";
		String dataWorld = "world";
		//Test add the data node 
		ZooKeeperUtil.createZNode(dataPath, dataHello.getBytes(), zooKeeper.getZooKeeper());
		ZooKeeperUtil.createZNode(priorityDataPath, dataWorld.getBytes(), zooKeeper.getZooKeeper());
		String result = ZooKeeperUtil.getValue(path, "testdata", "localhost", zooKeeper.getZooKeeper());
		assertEquals("world", result);
		result = ZooKeeperUtil.getValue(path, "testdata", null, zooKeeper.getZooKeeper());
		assertEquals("hello", result);
		//Test empty string
		result = ZooKeeperUtil.getValue(path, "testdata", "", zooKeeper.getZooKeeper());
		assertEquals("hello", result);
	}

	@Test
	public void testImportXml() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
//		ZooKeeperUtil.importXml("src/test/config/farm_config.xml", "/testroot/snsgame/babywar", zooKeeper.getZooKeeper());
//		ZooKeeperUtil.importXml("src/test/config/pack_config.xml", "/testroot/snsgame/babywar", zooKeeper.getZooKeeper());
		ZooKeeperUtil.importXml("src/test/config/map_config.xml", "/testroot/snsgame/babywar", zooKeeper.getZooKeeper());
	}
	
	@Test
	public void testImportDat() throws Exception {
		ZooKeeperFacade zooKeeper = ZooKeeperFactory.getInstance(connectString);
//		ZooKeeperUtil.importXml("src/test/config/farm_config.xml", "/testroot/snsgame/babywar", zooKeeper.getZooKeeper());
//		ZooKeeperUtil.importXml("src/test/config/pack_config.xml", "/testroot/snsgame/babywar", zooKeeper.getZooKeeper());
		ZooKeeperUtil.deleteZNode("/testroot/testgameconfig/snsgame/babywar/dailymark_reward.dat", zooKeeper.getZooKeeper());
		ZooKeeperUtil.importDat("src/test/config/dailymark_reward.dat", "/testroot/testgameconfig/snsgame/babywar", zooKeeper.getZooKeeper());
		List<String> children = zooKeeper.getZooKeeper().getChildren("/testroot/testgameconfig/snsgame/babywar/dailymark_reward.dat", false);
		assertEquals(5, children.size());
//		for ( String child : children ) {
//			System.out.println("child: " + child);
//		}
	}
	
	*/
}
