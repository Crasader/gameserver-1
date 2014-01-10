package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList.BseDailyMarkList;

public class DailyMarkManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetDailyMarkById() {
		DailyMarkPojo pojo = DailyMarkManager.getInstance().getDailyMarkByDayNum(6);
		assertNotNull(pojo);
	}

	@Test
	public void testGetDailyMarks() {
		Collection<DailyMarkPojo> maps = DailyMarkManager.getInstance().getDailyMarks();
		assertEquals(5, maps.size());
		for ( DailyMarkPojo pojo : maps ) {
			System.out.println(pojo);
		}
	}

	@Test
	public void testToBseDailyMark() {
		BseDailyMarkList bseDailyMark = DailyMarkManager.getInstance().toBseDailyMark();
		assertEquals(5, bseDailyMark.getDailymarksCount());
		System.out.println(bseDailyMark.getSerializedSize());
	}
}
