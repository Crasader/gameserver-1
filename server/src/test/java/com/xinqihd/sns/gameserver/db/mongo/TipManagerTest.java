package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.TipPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseTip.BseTip;

public class TipManagerTest {
	
	String[] tips = new String[]{
			"这是一款回合制游戏，每个玩家有30秒时间攻击对方",
			"用手点中角色向外拖拽，将有一条虚线标明攻击的方向和力度",
			"战斗需要在5分钟内结束，否则会以伤害血量的多少决定胜负",
			"便携道具中有很多有用的武器",
			"体力值决定了您能在一个回合中同时使用的道具数量",
			"装备不同的武器和道具可以加强您的角色属性",
			"装备根据品质分为'简陋','普通'、'坚固'和'恒久'",
			"非'恒久'品质的装备战斗时会逐渐损坏，但是不影响战斗效果，完全破损后才需要修复",
			"'蓄力'按钮可以直接切换到下一回合",
			"战斗中可以随时退出，不过系统要判输的哦",
			"强化5级以上的装备强化时若失败会下降一个强化等级，可以使用神恩符避免损失",
			"战斗中可以点击表情按钮快速发送表情",
			"游戏中的聊天窗口可以快速发送语音",
			"你可以通过游戏的信息栏关闭音乐和音效。",
			"创建房间之后，直接点击开始，系统即可为你寻找对手，开始单人游戏。",
			"完成任务是提升等级最好的办法。",
			"建议每游戏一个小时后休息一会。",
			"强化石和属性石目前最高等级为5级。",
			"强化装备使用幸运符能够使强化的成功率获得提高。",
			"相同的属性宝石通过熔炼可以合成高级属性宝石。",
			"更多功能即将开启，敬请期待。",
			"游戏群QQ: 231839605",
			"VIP4级及以上等级可以购买体力",
			"VIP6级及以上等级强化成功率会有额外加成",
			"每件装备都有最高强化上限，升级颜色会有机会提升最高强化上限",
			"每把装备都可以通过黄钻石开孔，嵌入火、水、风、土石头，平级的石头可以反复镶嵌",
	};

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetTips() {
		Collection<TipPojo> maps = TipManager.getInstance().getTips();
		assertTrue(maps.size()>=12);
	}

	@Test
	public void testToBseTipChannel() {
		User user = new User();
		user.setChannel("_xiaomi_");
		BseTip bseTip = TipManager.getInstance().toBseTip(user);
		assertTrue(bseTip.getTipsCount()>=12);
		System.out.println(bseTip.getSerializedSize());
	}
	
	@Test
	public void testToBseTip() {
		BseTip bseTip = TipManager.getInstance().toBseTip(null);
		assertTrue(bseTip.getTipsCount()>=12);
		System.out.println(bseTip.getSerializedSize());
	}
	
	@Test
	public void saveAllTips() {
		String databaseName = "babywarcfg";
		String namespace = "server0001";
		String collection = "tips";
		MongoDBUtil.dropCollection(databaseName, namespace, collection);
		
		for ( int i=0; i<tips.length; i++ ) {
			TipPojo tip = new TipPojo();
			tip.setId(i);
			tip.setTip(tips[i]);
			
			MapDBObject dbObj = new MapDBObject();
			dbObj.putAll(tip);
			MongoDBUtil.saveToMongo(dbObj, dbObj, databaseName, namespace, collection, true);
		}
//		TipPojo tip = new TipPojo();
//		tip.setId(tips.length);
//		tip.setTip("小米游戏中心双12答谢老用户,10元就赢小米手机2. 活动细则： 在活动期间，凡在《小小飞弹》内充值满10元即可获得1次抽奖会，满20元可获得2次抽奖机会，充值多多机会多多!  （小米账户系统会自动检测到您的充值金额） 奖品设置： 一等奖:小米手机2,共6部; 二等奖:小米手机2 F码,共18个; 三等奖:100元小米现金券, 共50张; 四等奖：50元小米现金券, 共100张; 五等奖：10元小米现金券, 共1000张; 活动时间：12月11日零点-12月15日24点 ");
//		Calendar startCal = Calendar.getInstance();
//		Calendar endCal = Calendar.getInstance();
//		endCal.set(Calendar.HOUR_OF_DAY, 0);
//		endCal.set(Calendar.DAY_OF_MONTH, 16);
//		System.out.println(endCal.getTime());
//		tip.setStartMillis(startCal.getTimeInMillis());
//		tip.setEndMillis(endCal.getTimeInMillis());
//		tip.setChannel("xiaomi");
//		MapDBObject dbObj = new MapDBObject();
//		dbObj.putAll(tip);
//		MongoUtil.saveToMongo(dbObj, dbObj, databaseName, namespace, collection, true);		
	}
}
