package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.VipPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class VipManagerTest {
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
		UserManager.getInstance().removeUser(userName);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetVipPeriodPojoById() {
		VipManager manager = VipManager.getInstance();
		VipPojo period = manager.getVipPojoById(1);
		assertEquals(1, period.getId());
	}

	@Test
	public void testGetVipPojos() {
		VipManager manager = VipManager.getInstance();
		Collection<VipPojo> coll = manager.getVipPojos();
		for ( VipPojo pojo : coll ) {
			System.out.println(pojo);
		}
		assertEquals(10, coll.size());
	}
	
	@Test
	public void testGetVipPojoZero() {
		int chargedYuanbao = 0;
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		assertEquals(null, vip);
	}
	
	@Test
	public void testGetVipPojoVIP6() {
		int chargedYuanbao = 10000;
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		assertEquals(6, vip.getId());
	}

	@Test
	public void testGetVipPojoVIP1() {
		int chargedYuanbao = 1;
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		assertEquals(1, vip.getId());
	}
	
	@Test
	public void testGetVipPojoVIP10() {
		int chargedYuanbao = 10000000;
		VipPojo vip = VipManager.getInstance().getVipPojoByYuanbao(chargedYuanbao);
		assertEquals(10, vip.getId());
	}
	
	@Test
	public void testVipOfflineExpNormal() {
		User user = new User();
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), false);
		assertEquals(0, exp);
	}
	
	@Test
	public void testVipOfflineExpVip1() {
		//Vip1-5 has no offline exp
		User user = new User();
		user.setIsvip(true);
		user.setViplevel(1);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), false);
		assertEquals(0, exp);
	}
	
	@Test
	public void testVipOfflineExpVip6() {
		//Vip1-5 has no offline exp
		User user = new User();
		user.setIsvip(true);
		user.setViplevel(6);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), false);
		assertEquals(1000, exp);
	}
	
	@Test
	public void testVipOfflineExpVip10() {
		//Vip1-5 has no offline exp
		User user = new User();
		user.setIsvip(true);
		user.setViplevel(10);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), false);
		assertTrue(exp>0 && exp < 20000);
	}
	
	@Test
	public void testVipOfflineExpVip10After3Hour() {
		//Vip1-5 has no offline exp
		User user = new User();
		user.setIsvip(true);
		user.setViplevel(10);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.HOUR_OF_DAY, -3);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), false);
		assertTrue(exp>0 && exp<20000);
	}
	
	@Test
	public void testVipOfflineExpVipMail() {
		//Vip1-5 has no offline exp
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setIsvip(true);
		user.setViplevel(10);
		UserManager.getInstance().removeUser("test-001");
		UserManager.getInstance().saveUser(user, true);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = lastCal.getTime();
		user.setLdate(lastDate);
		
		Calendar currCal = Calendar.getInstance();
		
		//clean mails
		String mailbox = MailMessageManager.getMailboxName(user.getUsername());
		MailMessageManager.getInstance().deleteAllInList(mailbox);
		
		VipManager manager = VipManager.getInstance();
		int exp = manager.processVipOfflineExp(user, currCal.getTimeInMillis(), true);
		assertTrue(exp>0 && exp < 20000);
		
		//Receive mail
		Jedis jedisDB = JedisFactory.getJedisDB();
		List<String> mailList = jedisDB.lrange(mailbox, 0, -1);
		assertEquals(1, mailList.size());
		System.out.println(mailList.get(0));
	}
	
	@Test
	public void testToBseVipInfo() {
		VipManager manager = VipManager.getInstance();
		VipPojo period = manager.getVipPojoById(8);
		period.toBseVipInfo();
		assertEquals(1, period.getId());
	}
	
	/**
	 * 初始化VIP数据
	 *  27008	VIP9专属礼包
			27007	VIP8专属礼包
			27006	VIP7专属礼包
			27005	VIP6专属礼包
			27004	VIP5专属礼包
			27003	VIP4专属礼包
			27002	VIP3专属礼包
			27001	VIP2专属礼包
			27000	VIP1专属礼包

	 */
	@Test
	public void setupVipData() {
		ArrayList<VipPojo> vips = new ArrayList<VipPojo>();
		//new int[]{30, 40, 50, 60, 70, 80, 90, 100, 100, 100} 
		VipPojo vipPojo = new VipPojo();
		vipPojo.setId(1);
		vipPojo.setYuanbaoPrice(1);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP1礼包 \n2、获得5万金币 \n3、战斗经验加成 10% \n4、背包容量增加8格 \n5、每日祈福机会+30 \n6、小喇叭x10 \n7、可在黑铁到赤钢级别内跨级转移强化等级 \n8、PVP战斗掉落精良装备 \n9、PVE战斗掉落精良装备 \n10、开通不限次数寻宝 "
				);
		vipPojo.setGiftId("27000");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(2);
		vipPojo.setYuanbaoPrice(200);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP2礼包 \n2、获得5万金币 \n3、战斗经验加成 15% \n4、背包容量增加 16 格 \n5、每日祈福机会+50 \n6、小喇叭x15	 \n7、可在黑铁到白银级别内跨级转移强化等级 \n8、PVP战斗掉落精良装备 \n9、PVE战斗掉落精良 装备加成 \n10、开通不限次数寻宝 "
		);
		vipPojo.setGiftId("27001");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(3);
		vipPojo.setYuanbaoPrice(400);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP3礼包 \n2、获得10万金币 \n3、战斗经验加成 20%	 \n4、背包容量增加 24 格  \n5、每日祈福机会+70 \n6、小喇叭x20 \n7、可在黑铁到黄金级别内跨级转移强化等级 \n8、PVP战斗掉落精良装备 \n9、PVE战斗掉落精良装备	 \n10、开通不限次数寻宝 	"
		);
		vipPojo.setGiftId("27002");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(4);
		vipPojo.setYuanbaoPrice(1100);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP4礼包 \n2、获得20万金币 \n3、战斗经验加成 30% \n4、背包容量增加 40 格 \n5、每日祈福机会+90 \n6、小喇叭x30 \n7、可购买体力值机会+6 \n8、可在黑铁到琥珀级别内跨级转移强化等级 \n9、PVP战斗掉落精良装备 \n10、PVE战斗掉落精良装备 \n11、开通不限次数寻宝 "
		);
		vipPojo.setGiftId("27003");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(5);
		vipPojo.setYuanbaoPrice(2000);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP5礼包 \n2、获得30万金币 \n3、战斗经验加成 40% \n4、背包容量增加 64 格 \n5、每日祈福机会+300 \n6、小喇叭x38 \n7、可购买体力值机会+21 \n8、可在黑铁到翡翠级别内跨级转移强化等级 \n9、PVP战斗掉落精良装备 \n10、PVE战斗掉落精良装备 \n11、开通不限次数寻宝 "
		);
		vipPojo.setGiftId("27004");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(6);
		vipPojo.setYuanbaoPrice(9990);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP6礼包 \n2、获得50万金币 \n3、战斗经验加成 60% \n4、背包容量增加 80格 \n5、每日祈福机会+500 \n6、小喇叭x48 \n7、可购买体力值机会+21 \n8、离线经验最高1000 \n9、可在黑铁到水晶级别内跨级转移强化等级 \n10、可在白色到粉色间跨颜色转移强化 \n11、PVP战斗掉落精良装备 \n12、PVE战斗掉落精良装备 \n13、开通不限次数寻宝	 \n14、强化成功概率提升5%	"
		);
		vipPojo.setGiftId("27005");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(7);
		vipPojo.setYuanbaoPrice(19990);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP7礼包 \n2、获得100万金币 \n3、战斗经验加成 80% \n4、背包容量增加 100 格 \n5、每日祈福机会+1000 \n6、小喇叭x60 \n7、可购买体力值机会+46 \n8、离线经验最高1500 \n9、可在黑铁到钻石级别内跨级转移强化等级 \n10、可在白色到橙色间跨颜色转移强化 \n11、PVP战斗掉落精良装备、\n12、PVE战斗掉落精良装备 \n13、开通不限次数寻宝 \n14、强化成功概率提升15%	"
		);
		vipPojo.setGiftId("27006");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(8);
		vipPojo.setYuanbaoPrice(50000);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP8礼包 \n2、获得300万金币 \n3、战斗经验加成 100% \n4、背包容量增加 120 格 \n5、每日祈福机会+2000 \n6、小喇叭x72 \n7、可购买体力值机会+46 \n8、离线经验最高2000 \n9、可在任意级别内跨级转移强化等级 \n10、可在白色到橙色间跨颜色转移强化 \n11、PVP战斗掉落精良装备 \n12、PVE战斗掉落精良装备 \n13、开通不限次数寻宝 \n14、强化成功概率提升25%	"
		);
		vipPojo.setGiftId("27007");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(9);
		vipPojo.setYuanbaoPrice(100000);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP9礼包 \n2、获得600万金币 \n3、战斗经验加成 100% \n4、背包容量增加 200 格 \n5、每日祈福机会+3000 \n6、小喇叭x85 \n7、可购买体力值机会+46 \n8、离线经验最高10000 \n9、可在任意级别内跨级转移强化等级 \n10、可在任意颜色间转移强化 \n11、PVP战斗掉落精良装备 \n12、PVE战斗掉落精良装备 \n13、开通不限次数寻宝 \n14、强化成功概率提升40%	"
		);
		vipPojo.setGiftId("27008");
		vips.add(vipPojo);
		
		vipPojo = new VipPojo();
		vipPojo.setId(10);
		vipPojo.setYuanbaoPrice(200000);
		vipPojo.setValidSeconds(-1);
		vipPojo.setDesc(
				"1、获得VIP10礼包 \n2、获得1200万金币 \n3、战斗经验加成 150% \n4、背包容量增加 400 格 \n5、每日祈福机会+6000 \n6、小喇叭x100 \n7、可购买体力值机会+46 \n8、离线经验最高20000 \n9、可在任意级别内跨级转移强化等级 \n10、可在任意颜色间转移强化 \n11、PVP战斗掉落精良装备 \n12、PVE战斗掉落精良装备 \n13、开通不限次数寻宝	 \n14、强化成功概率提升50%"
		);
		vipPojo.setGiftId("27009");
		vips.add(vipPojo);
		
		String database = "babywarcfg", namespace="server0001", collection="vips";
		for ( VipPojo vip : vips ) {
			MapDBObject dbObject = new MapDBObject();
			dbObject.putAll(vip);
			DBObject query = MongoDBUtil.createDBObject("_id", vip.getId());
			MongoDBUtil.saveToMongo(query, dbObject, database, namespace, collection, true);
			System.out.println(dbObject);
		}
	}
	
	private User createUser() {
		User user = new User();
		user.set_id(new UserId(userName));
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setYuanbao(10000);
		user.setGolden(500);
		user.setVoucher(500);
		user.setMedal(500);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user, true);
		IoSession session = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session, user, user.getSessionKey());
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		return user;
	}
}
