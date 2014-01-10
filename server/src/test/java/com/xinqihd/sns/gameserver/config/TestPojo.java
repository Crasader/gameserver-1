package com.xinqihd.sns.gameserver.config;

import org.junit.After;
import org.junit.Before;

public class TestPojo {
	
	public static String expectedMapPojoLua = 
			"map1001={\n"+
					"name=\"死亡沙漠\",\n"+
					"type=2,\n"+
					"reqlv=1,\n"+
					"scrollArea={\n"+
					"x=0,\n"+
					"y=0,\n"+
					"width=1670,\n"+
					"height=802,\n"+
					"},\n"+
					"layer={\n"+
					"{\n"+
					"id=\"map_1001\",\n"+
					"num=1,\n"+
					"scrollRate=1,\n"+
					"},\n"+
					"{\n"+
					"id=\"map_1001_bg\",\n"+
					"num=2,\n"+
					"scrollRate=1,\n"+
					"},\n"+
					"},\n"+
					"boss={\n"+
					"{\n"+
					"id=\"1001_b\",\n"+
					"x=1400,\n"+
					"y=575,\n"+
					"},\n"+
					"},\n"+
					"enemy={\n"+
					"{\n"+
					"id=\"1001_e\",\n"+
					"x=1250,\n"+
					"y=575,\n"+
					"},\n"+
					"},\n"+
					"startPoints={\n"+
					"{\n"+
					"x=120,\n"+
					"y=610,\n"+
					"},\n"+
					"{\n"+
					"x=356,\n"+
					"y=450,\n"+
					"},\n"+
					"{\n"+
					"x=540,\n"+
					"y=545,\n"+
					"},\n"+
					"{\n"+
					"x=650,\n"+
					"y=580,\n"+
					"},\n"+
					"{\n"+
					"x=1100,\n"+
					"y=575,\n"+
					"},\n"+
					"{\n"+
					"x=1250,\n"+
					"y=575,\n"+
					"},\n"+
					"{\n"+
					"x=1400,\n"+
					"y=575,\n"+
					"},\n"+
					"{\n"+
					"x=1551,\n"+
					"y=560,\n"+
					"},\n"+
					"},\n"+
					"}";
	
	private String dailyPojoString = 
			"dailymark_reward={\n"+
					"step=1,\n"+
					"daynum=6,\n"+
					"items{\n"+
					"id=\"20004\",\n"+
					"desc=\"物品奖励1\",\n"+
					"level=2,\n"+
					"number=2,\n"+
					"}\n"+
					"}";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


}
