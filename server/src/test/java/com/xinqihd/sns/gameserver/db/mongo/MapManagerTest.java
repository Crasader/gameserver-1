package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.MapPojo;
import com.xinqihd.sns.gameserver.config.MapPojo.Layer;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap.BseMap;
import com.xinqihd.sns.gameserver.proto.XinqiBseMap.MapData;

public class MapManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMapById() {
		MapPojo pojo = MapManager.getInstance().getMapById("0");
		assertNotNull(pojo);
		List<Layer> layers = pojo.getLayers();
		Layer layer = layers.get(0);
		assertTrue(layer.width == pojo.getScrollAreaWidth());
		assertTrue(layer.height>0);
	}

	@Test
	public void testGetMaps() {
		BseMap bseMap = MapManager.getInstance().toBseMap();
		for ( MapData map : bseMap.getMapsList() ) {
			System.out.println(map);
		}
	}
	
	@Test
	public void testToBseMap() {
		BseMap bseMap = MapManager.getInstance().toBseMap();
		//assertEquals(30, bseMap.getMapsCount());
		System.out.println(bseMap.getSerializedSize());
	}
	
	/**
	 * 为所有地图增加一个isHidden属性
	 */
	public void addIsHidden() {
		Collection<MapPojo> maps = MapManager.getInstance().getMaps();
		assertEquals(29, maps.size());
		for ( MapPojo map : maps ) {
			System.out.println(map);
			map.setHidden(false);
		}
		String database = "babywar", namespace="server0001", collection="maps";
		for ( MapPojo map : maps ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(map);
			DBObject query = MongoDBUtil.createDBObject("_id", map.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
		}
	}
}
