package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.PromotionPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

public class ActivityManagerTest {

	@Before
	public void setUp() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.del(ActivityManager.KEY_ACT_URL);
		jedisDB.del(ActivityManager.KEY_ACT_EXPRATE);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetActivityUrl() {
		String activityUrl = "http://m.changyou.com/";
		ActivityManager manager = ActivityManager.getInstance();
		manager.setActivityUrl(activityUrl, 0);
		String actual = manager.getActivityUrl();
		assertEquals(activityUrl, actual);
	}
	
	@Test
	public void testGetActivityExpRateEmpty() {
		float expRate = 2.0f;
		ActivityManager manager = ActivityManager.getInstance();
		assertEquals(0.0f, manager.getActivityExpRate(null), 0.01f);
		//check again
		assertEquals(0.0f, manager.getActivityExpRate(null), 0.01f);
	}
	
	@Test
	public void testGetActivityExpRateEmptyForUser() {
		float expRate = 2.0f;
		User user = new User();
		user.setUsername("test001");
		ActivityManager manager = ActivityManager.getInstance();
		assertEquals(0.0f, manager.getActivityExpRate(user), 0.01f);
		manager.setActivityExpRate(user, 20, expRate, 30);
		float actualExpRate = manager.getActivityExpRate(user);
		assertEquals(expRate, actualExpRate, 0.1);
	}


	@Test
	public void testSetActivityExpRate() throws Exception {
		float expRate = 2.0f;
		ActivityManager manager = ActivityManager.getInstance();
		manager.setActivityExpRate(null, expRate, 1);
		assertEquals(2.0f, manager.getActivityExpRate(null), 0.01f);
		
		//change the expRate
		expRate = 3.0f;
		manager.setActivityExpRate(null, expRate, 60);
		//cache hit
		assertEquals(3.0f, manager.getActivityExpRate(null), 0.01f);
	}
	
	@Test
	public void testReload() throws Exception {
		
	}
	
	@Test
	public void addPromotionMessage() throws Exception {
		String databaseName = "babywarcfg";
		String namespace = "server0001";
		String collection = "promotions";
		MongoDBUtil.dropCollection(databaseName, namespace, collection);

		Calendar startCal = Calendar.getInstance();
		startCal.set(Calendar.DAY_OF_MONTH, 6);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		Calendar endCal = Calendar.getInstance();
		startCal.set(Calendar.DAY_OF_MONTH, 16);
		startCal.set(Calendar.HOUR_OF_DAY, 0);
		
		String[] message = new String[]{
				"号外：单人的雪兔副本现在可以邀请好友一起进行(等级最多差20级)，奖金会按照对雪兔的伤害程度（减血比例）划分",
				"我们对昨天参与元宵节猜灯谜活动的玩家进行了统计，玩家'#ff0000燃烧吧m蛋崽#000000'以117道题目的成绩勇夺第一，他将获得我们送出的100元移动充值卡！感谢大家的参与。",
				"以下是答对的题目数量超过10道题的玩家排名:\n",
				"#ff00bb玩家昵称				答对题目数量",
				"---------------------------------------------------",
				"燃烧吧m蛋崽                       117\n"+
				"※殇涩                                   114\n"+
				"小米疯子	                       89\n"+
				"张冰儿                                   78\n"+
				"Mrwang                                42\n"+
				"小么子                                   38\n"+
				"So.低调                                  12\n"+
				"带点，小忧伤                       11\n"+
				"克里斯保罗	                    10\n"+
				"尧尧小飞侠12                        10\n"+
				"野战军丶子画☆小骨	     10\n"
		};
		
		for ( int i=0; i<message.length; i++ ) {
			PromotionPojo tip = new PromotionPojo();
			tip.setId(i);
			tip.setMessage(message[i]);
			
			tip.setStartMillis(startCal.getTimeInMillis());
			tip.setEndMillis(endCal.getTimeInMillis());
			
			MapDBObject dbObj = new MapDBObject();
			dbObj.putAll(tip);
			MongoDBUtil.saveToMongo(dbObj, dbObj, databaseName, namespace, collection, true);
		}		
		
	}
}
