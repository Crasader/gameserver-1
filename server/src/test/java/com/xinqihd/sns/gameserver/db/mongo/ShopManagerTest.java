package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.MoneyType;
import com.xinqihd.sns.gameserver.config.ShopCatalog;
import com.xinqihd.sns.gameserver.config.ShopPojo;
import com.xinqihd.sns.gameserver.config.ShopPojo.BuyPrice;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.EquipType;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyProp.BceBuyProp;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyProp.BuyInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBceLengthenIndate.LengthenIndate;
import com.xinqihd.sns.gameserver.proto.XinqiBseExpireEquipments.ExpireInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseModiTask.BseModiTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.BseShop;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.util.OtherUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class ShopManagerTest {
  
	//11138	570	黑铁●榴弹炮(570)
	//24, 72, 120, 240
	private String equipId = "570";
	private String goodId = "11136"; 
	//2920	黑铁●海盗船长之帽	100	[ { "price" : 21 , "validTimes" : 30} , { "price" : 63 , "validTimes" : 100} , { "price" : 105 , "validTimes" : 200} , { "price" : 210 , "validTimes" : 2147483647}]
	private String goodGoldenId = "11136";
	
	//11626	680	黑铁●青龙鳞
  private String equipYuanbaoId = "680";
  private String equipShopId = "11626";
  private Integer equipShopInt = 11626;
	
	//9544	2920	黑铁●海盗船长之帽
	private String equipId2 = "2920";
	private String goodId2 = "9546";
	private Integer goodId2Int = 9546;
	private String level1Prefix = "黑铁";
	private String level2Prefix = "青铜";
	
	/*
	 * 294	20025	强化石Lv5	100	[ YUANBAO
	 * { "price" : 500 , "validTimes" : 1} , 
	 * { "price" : 0 , "validTimes" : 0} , 
	 * { "price" : 0 , "validTimes" : 0} , 
	 * { "price" : 0 , "validTimes" : 0}]	
	 * [ "RECOMMEND" , "HOT" , "ITEM"]	8	5	YUANBAO	0	0	0	0	0	true
	 */
	private String stoneId = "294";
	private Integer stoneIdInt = 294;

	@Before
	public void setUp() throws Exception {
		Jedis jedis = JedisFactory.getJedisDB();
		jedis.del(ShopManager.REDIS_HOT_YUANBAO);
		jedis.del(ShopManager.REDIS_HOT_GOLDEN);
		
		SecureLimitManager.getInstance().setDisableSecureChecking(true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetShopById() {
		ShopPojo pojo = ShopManager.getInstance().getShopById("1000");
		assertNotNull(pojo);
	}

	@Test
	public void testGetShops() {
		Collection<ShopPojo> maps = ShopManager.getInstance().getShops();
		assertTrue(maps.size()>1000);
		for ( ShopPojo pojo : maps ) {
//			System.out.println(pojo.getId()+"\t"+pojo.getType()+
//				"\t"+pojo.getPropInfoId());
			if ( !pojo.isItem() ) {
				String id = pojo.getPropInfoId();
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(id);
				if ( weapon != null ) {
					System.out.println(pojo.getInfo()+","+pojo.getMoneyType()+","+weapon.getSex());
				} else {
					System.out.println(pojo.getInfo());
				}
			}
		}
	}

	@Test
	public void testToBseShop() {
		BseShop bseShop = ShopManager.getInstance().toBseShop(null);
		assertTrue(bseShop.getShopsCount()>500);
		System.out.println(bseShop.getSerializedSize());
	}
	
	@Test
	public void testToBseShopWithLevel() {
		User user = new User();
		user.setLevel(9);
		BseShop bseShop = ShopManager.getInstance().toBseShop(user);
		assertTrue(bseShop.getShopsCount()>0);
		System.out.println(bseShop.getSerializedSize());
	}
	
	@Test
	public void testToBseShopWithLevel12() {
		User user = new User();
		user.setLevel(12);
		BseShop bseShop = ShopManager.getInstance().toBseShop(user);
		assertTrue(bseShop.getShopsCount()>0);
		System.out.println(bseShop.getSerializedSize());
	}
	
	@Test
	public void testToBseShopWith100Level() {
		User user = new User();
		user.setLevel(100);
		BseShop bseShop = ShopManager.getInstance().toBseShop(user);
		assertTrue(bseShop.getShopsCount()>0);
		System.out.println(bseShop.getSerializedSize());
	}
	
	@Test
	public void testToBseShopWith100LevelWithDiscount() {
		User user = new User();
		user.setLevel(100);
		BseShop bseShop = ShopManager.getInstance().toBseShop(user, 90);
		assertTrue(bseShop.getShopsCount()>0);
		System.out.println(bseShop.getSerializedSize());
	}
	
	@Test
	public void testLoadBuffToolPrice() {
		
	}
	
	@Test
	public void testGetShopListByPropInfoId() {
		//水神石Lv4
		String propInfoId = "20001";
		Collection<ShopPojo> shopList = ShopManager.getInstance().getShopsByPropInfoId(propInfoId);
		System.out.println(shopList);
		assertEquals(1, shopList.size());
	}
	
	@Test
	public void testGetShopByCatalog() {
		Collection<ShopPojo> shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.WEAPON);
		assertTrue(""+shopList.size(), shopList.size()>100 && shopList.size()<600);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.HOT);
		assertEquals(45, shopList.size());
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.RECOMMEND);
		assertTrue( shopList.size()>0 && shopList.size() < 100);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.ITEM);
		assertTrue( shopList.size()>0 && shopList.size() < 100);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.GIFTPACK);
		assertTrue( shopList.size()>0 && shopList.size() < 100);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.WEAPON);
		assertTrue( shopList.size()>0 && shopList.size() < 600);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.DECORATION);
		assertTrue( shopList.size()>0 && shopList.size() < 6000);
		
		shopList = ShopManager.getInstance().getShopsByCatalog(ShopCatalog.SUITE);
		assertTrue( shopList.size()>0 && shopList.size() < 400);
	}
	
	@Test
	public void testGetShopByMoneyType() {
		Collection<ShopPojo> shopList = ShopManager.getInstance().getShopsByMoneyType(MoneyType.GOLDEN);
		assertTrue(shopList.size()>=20);
		
		shopList = ShopManager.getInstance().getShopsByMoneyType(MoneyType.MEDAL);
		assertEquals(0, shopList.size());
		
		shopList = ShopManager.getInstance().getShopsByMoneyType(MoneyType.VOUCHER);
		assertEquals(0, shopList.size());
		
		shopList = ShopManager.getInstance().getShopsByMoneyType(MoneyType.GOLDEN);
		assertTrue(shopList.size()>0);
		
		shopList = ShopManager.getInstance().getShopsByMoneyType(MoneyType.YUANBAO);
		assertTrue(shopList.size()>0);
	}
	
	@Test
	public void testBuyGoodItem() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(50);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertEquals(5, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testBuyGoodItemMultiTimes() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(500);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		for ( int i=0; i<10; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(290);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(0);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		BceBuyProp bceBuyProp = buyProp.build();
		boolean buyResult = shopManager.buyGoodFromShop(user, bceBuyProp);
		assertTrue("Success buy", buyResult);
		assertEquals(50, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(10, actual.getBag().getOtherPropData(20).getCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testBuyGoodItemMultiTimesWithDiffIndate() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(200);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
	  /*
	   * 11138	570	黑铁●榴弹炮	100	[ 
	   * { "price" : 5 , "validTimes" : 30} , 
	   * { "price" : 16 , "validTimes" : 100} , 
	   * { "price" : 26 , "validTimes" : 200} , 
	   * { "price" : 52 , "validTimes" : 2147483647}]	
	   * [ "WEAPON"]	57	0	YUANBAO
	   * 
	   */
		ShopPojo shop = shopManager.getShopById(goodId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		for ( int i=0; i<5; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(goodId2Int);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(0);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		for ( int i=0; i<5; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(goodId2Int);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(1);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		//Add a different count one
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(goodId2Int);
		buyInfo.setCount(5);
		buyInfo.setLeftTimeType(1);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());

		buyProp.addBuyList(buyInfo.build());
		BceBuyProp bceBuyProp = buyProp.build();
		boolean buyResult = shopManager.buyGoodFromShop(user, bceBuyProp);
		assertTrue("Success buy", buyResult);
		assertEquals(85, user.getYuanbao());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(2, actual.getBag().getCurrentCount());
		assertEquals(equipId2, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(5, actual.getBag().getOtherPropData(20).getCount());
		assertEquals(10, actual.getBag().getOtherPropData(21).getCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testBuyGoodItemWithFullBagWithMultiSameGood() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(50);
		user.setYuanbao(150);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		int count = 69;
		for ( int i=0; i<count; i++ ) {
			PropData propData = new PropData();
			propData.setItemId("10023");
			propData.setName("test");
			user.getBag().addOtherPropDatas(propData.clone());
		}
		assertEquals(count, user.getBag().getCurrentCount());
		
		ShopManager shopManager = ShopManager.getInstance();

		//Buy same good for 5 times
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		for ( int i=0; i<5; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(goodId2Int);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(0);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		assertTrue("Success buy", buyResult);
		assertEquals(70, user.getBag().getCurrentCount());
		
		//Buy another one 
		buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(2);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Full buy", !buyResult);
		assertEquals(50, user.getGolden());
	}
	
	@Test
	public void testBuyGoodItemWithFullBag() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(50);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		int count = 69;
		for ( int i=0; i<count; i++ ) {
			PropData propData = new PropData();
			propData.setItemId("10023");
			propData.setName("test");
			user.getBag().addOtherPropDatas(propData.clone());
		}
		assertEquals(count, user.getBag().getCurrentCount());
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertEquals(5, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(70, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(89).getItemId());
		assertEquals(user.getGolden(), actual.getGolden());
		
		//Buy another two 
		buyProp = BceBuyProp.newBuilder();
		buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(2);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Full buy", !buyResult);
		assertEquals(5, user.getGolden());
	}
	
	@Test
	public void testBuyGoodItemWithoutMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(40);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	45 Golden
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Fail buy", !buyResult);
		assertEquals(40, user.getGolden());
		assertEquals(0, user.getBag().getCurrentCount());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
	}
	
	@Test
	public void testBuyMultiGoodItemWithoutMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setVoucher(9);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(2);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Fail buy", !buyResult);
		assertEquals(9, user.getVoucher());
		assertEquals(0, user.getBag().getCurrentCount());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
	}
		
	@Test
	public void testBuyGoodEquipment() {
		Jedis jedis = JedisFactory.getJedisDB();
		jedis.zrem(ShopManager.REDIS_HOT_YUANBAO, goodId);
		
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(120);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();

		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<120);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(user.getVoucher(), actual.getVoucher());
		
		Double rank = jedis.zscore(ShopManager.REDIS_HOT_YUANBAO, equipShopId);
		System.out.println(rank);
		assertEquals(1.0, rank.doubleValue(), 0.1);
		
		//buy again
		buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		rank = jedis.zscore(ShopManager.REDIS_HOT_YUANBAO, equipShopId);
		System.out.println(rank);
		assertEquals(2.0, rank.doubleValue(), 0.1);
		
	}
	
	@Test
	public void testBuyGoodAndPriceCountUnmatched() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(130);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //233	1	高跟鞋诱惑	12003		0	120
		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(9999);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", !buyResult);
		assertEquals(130, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
	}
	
	@Test
	public void testBuyGoodEquipmentWithYuanbao() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(130);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<130);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(user.getVoucher(), actual.getVoucher());
	}
	
	@Test
	public void testBuyGoodItemAndEquipment() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(50);
		user.setYuanbao(120);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
	  //364	1	狂˙榴弹炮	13001		4	100
		shop = shopManager.getShopById(equipShopId);
		propId = shop.getPropInfoId();
		
		buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertEquals(5, user.getGolden());
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<120);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(2, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(user.getYuanbao(), actual.getYuanbao());
	}
	
	@Test
	public void testBuyGoodItemAndEquipmentWithoutMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(1);
		user.setYuanbao(1);
		UserManager.getInstance().saveUser(user, false);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //290	8	强化石Lv1	price:	9
		ShopPojo shop = shopManager.getShopById("290");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(290);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
	  //364	1	狂˙榴弹炮	13001		4	100
		shop = shopManager.getShopById(equipShopId);
		propId = shop.getPropInfoId();
		
		buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", !buyResult);
		assertEquals(1, user.getGolden());
		assertEquals(1, user.getYuanbao());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getVoucher(), actual.getVoucher());
		assertEquals(user.getYuanbao(), actual.getYuanbao());
	}

	@Test
	public void testBuyGoodItemTaskByGolden() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(200);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		TaskManager taskManager = TaskManager.getInstance();
		//74	英雄的财富	金币是游戏中的通用货币，使用金币去商城购买任意一件物品吧	使用金币购买任意一件物品
		TaskPojo taskPojo = taskManager.getTaskById("74");
		LinkedList<TaskPojo> tasks = new LinkedList<TaskPojo>();
		tasks.add(taskPojo);
		user.addTasks(tasks);
		taskManager.deleteUserTasks(user);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //9544 2920	黑铁●海盗船长之帽	100	[ { "price" : 210 , "validTimes" : 2147483647}]
		ShopPojo shop = shopManager.getShopById(goodId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(Integer.parseInt(goodGoldenId));
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertTrue(""+user.getGolden(), user.getGolden()<200);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		Bag actualBag = actual.getBag();
		assertEquals(1, actualBag.getCurrentCount());
		assertEquals(propId, actualBag.getOtherPropData(20).getItemId());
		assertEquals(30, actualBag.getOtherPropData(20).getPropIndate());
		assertEquals(0, actualBag.getOtherPropData(20).getPropUsedTime());
		assertEquals(user.getVoucher(), actual.getVoucher());
		
		//Check task result.
		System.out.println(list);
		Thread.currentThread().sleep(500);
		assertTrue(list.size()>=1);
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseModiTask ) {
				return;
			}
		}
		fail("Do not find BseModiTask");
	}
	
	@Test
	public void testBuyGoodEquipmentVIP() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(120);
		user.setIsvip(true);
		user.setVipedate(new Date(System.currentTimeMillis()+86400000));
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
	  //364	1	狂˙榴弹炮	13001		4	100
		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		
		assertTrue("Success buy", buyResult);
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<120);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		assertEquals(propId, actual.getBag().getOtherPropData(20).getItemId());
		assertEquals(user.getVoucher(), actual.getVoucher());
	}
	
	@Test
	public void testLengthenItem() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setVoucher(10);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
	  ItemManager itemManager = ItemManager.getInstance();
//	  ItemPojo itemPojo = itemManager.getItemById("item_20005_5");
	  ItemPojo itemPojo = itemManager.getItemById("20025");
		user.getBag().addOtherPropDatas(itemPojo.toPropData());
		
		LengthenIndate.Builder lengthenIndate = LengthenIndate.newBuilder();
	  //290	8	强化石Lv1	price:	9
		//lengthenIndate.setId("item_20005_5");
		lengthenIndate.setId("20025");
		lengthenIndate.setShopid(290);
		lengthenIndate.setIndatetype(0);
		List<LengthenIndate> list = new ArrayList<LengthenIndate>();
		list.add(lengthenIndate.build());
		
		shopManager.resubscribePropData(user, list);
		
		assertEquals(10, user.getVoucher());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
	}
	
	@Test
	public void testLengthEquipment() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(220);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();

		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId);
		PropData oldPropData = weapon.toPropData(10, WeaponColor.WHITE);
		oldPropData.setPropUsedTime(-1);
		user.getBag().addOtherPropDatas(oldPropData);
		UserManager.getInstance().saveUserBag(user, false);
		
		LengthenIndate.Builder lengthenIndate = LengthenIndate.newBuilder();
	  //290	8	强化石Lv1	price:	9
		lengthenIndate.setId("13001");
		lengthenIndate.setShopid(equipShopInt);
		lengthenIndate.setProppos(20);
		lengthenIndate.setIndatetype(1);
		List<LengthenIndate> list = new ArrayList<LengthenIndate>();
		list.add(lengthenIndate.build());
		
	  //364	1	狂˙榴弹炮	13001		4	100
		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
				
		shopManager.resubscribePropData(user, list);
		
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<220);
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(1, actual.getBag().getCurrentCount());
		PropData actualPropData = actual.getBag().getOtherPropData(20);
		assertEquals(equipId, actualPropData.getItemId());
		assertEquals(0, actualPropData.getPropUsedTime());
		assertEquals(user.getVoucher(), actual.getVoucher());
	}
	
	@Test
	public void testLengthMultiEquipments() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(220);
		user.setMedal(99);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		//海盗船长之帽	9544
		WeaponPojo suit = EquipManager.getInstance().getWeaponById(equipId2);
		PropData oldSuitPropData = suit.toPropData(10, WeaponColor.WHITE);
		oldSuitPropData.setPropUsedTime(-1);
		user.getBag().addOtherPropDatas(oldSuitPropData);
		//
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId);
		PropData oldWeaponPropData = weapon.toPropData(10, WeaponColor.WHITE);
		oldSuitPropData.setPropUsedTime(-1);
		user.getBag().addOtherPropDatas(oldWeaponPropData);
		
		UserManager.getInstance().saveUserBag(user, false);
		
		List<LengthenIndate> list = new ArrayList<LengthenIndate>();
		
		LengthenIndate.Builder lengthenIndate = LengthenIndate.newBuilder();
	  //364	1	狂˙榴弹炮	13001		4	200
		lengthenIndate.setId(equipId);
		lengthenIndate.setShopid(equipShopInt);
		lengthenIndate.setProppos(20);
		lengthenIndate.setIndatetype(1);
		list.add(lengthenIndate.build());
		//199	138	海盗船长之帽	8047		2	98
		lengthenIndate = LengthenIndate.newBuilder();
		lengthenIndate.setId(equipId2);
		lengthenIndate.setShopid(equipShopInt);
		lengthenIndate.setProppos(21);
		lengthenIndate.setIndatetype(0);
		list.add(lengthenIndate.build());
		
	  //364	1	狂˙榴弹炮	13001		4	100
		shopManager.resubscribePropData(user, list);
		
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<220);
		assertEquals(99,  user.getMedal());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(2, actual.getBag().getCurrentCount());
		PropData actualSuitPropData = actual.getBag().getOtherPropData(21);
		assertEquals(0, actualSuitPropData.getPropUsedTime());
		assertEquals(30, actualSuitPropData.getPropIndate());
		PropData actualWeaponPropData = actual.getBag().getOtherPropData(20);
		assertEquals(0, actualWeaponPropData.getPropUsedTime());
		assertEquals(100, actualWeaponPropData.getPropIndate());
		assertEquals(user.getVoucher(), actual.getVoucher());
	}
	
	@Test
	public void testLengthMultiEquipmentsWithoutEnoughMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(1);
		user.setMedal(1);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		//海盗船长之帽	8047
		WeaponPojo suit = EquipManager.getInstance().getWeaponById(equipId);
		PropData oldSuitPropData = suit.toPropData(10, WeaponColor.WHITE);
		oldSuitPropData.setPropUsedTime(-1);
		user.getBag().addOtherPropDatas(oldSuitPropData);
		// 狂˙榴弹炮	13001	
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(equipId2);
		PropData oldWeaponPropData = weapon.toPropData(10, WeaponColor.WHITE);
		oldWeaponPropData.setPropUsedTime(-1);
		user.getBag().addOtherPropDatas(oldWeaponPropData);
		
		UserManager.getInstance().saveUserBag(user, false);
		
		List<LengthenIndate> list = new ArrayList<LengthenIndate>();
		
		LengthenIndate.Builder lengthenIndate = LengthenIndate.newBuilder();
	  //364	1	狂˙榴弹炮	13001		4	200
		lengthenIndate.setId(equipId);
		lengthenIndate.setShopid(equipShopInt);
		lengthenIndate.setProppos(20);
		lengthenIndate.setIndatetype(1);
		list.add(lengthenIndate.build());
		//199	138	海盗船长之帽	8047		2	98
		lengthenIndate = LengthenIndate.newBuilder();
		lengthenIndate.setId(equipId2);
		lengthenIndate.setShopid(equipShopInt);
		lengthenIndate.setProppos(21);
		lengthenIndate.setIndatetype(0);
		list.add(lengthenIndate.build());
		
	  //364	1	狂˙榴弹炮	13001		4	100
		shopManager.resubscribePropData(user, list);
		
		assertEquals(1, user.getYuanbao());
		assertEquals(1,  user.getMedal());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(2, actual.getBag().getCurrentCount());
		PropData actualSuitPropData = actual.getBag().getOtherPropData(20);
		assertEquals(-1, actualSuitPropData.getPropUsedTime());
		assertEquals(10, actualSuitPropData.getPropIndate());
		PropData actualWeaponPropData = actual.getBag().getOtherPropData(21);
		assertEquals(-1, actualWeaponPropData.getPropUsedTime());
		assertEquals(10, actualWeaponPropData.getPropIndate());
		assertEquals(user.getVoucher(), actual.getVoucher());
	}
	
	@Test
	public void testPayforService() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(1000);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.payForSomething(user, MoneyType.GOLDEN, 100, 2, message);
		
		assertTrue("Success buy", buyResult);
		assertEquals(800, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testPayforServiceWithoutMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(150);
		UserManager.getInstance().saveUser(user, false);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.payForSomething(user, MoneyType.GOLDEN, 100, 2, message);
		
		assertTrue("Fail buy", !buyResult);
		assertEquals(150, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testBuyBuffTool() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(10000);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		
		assertTrue("Success buy", buyResult);
		assertEquals(9982, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		userManager.saveUser(user, false);
		//Query the user again.
		User actual = userManager.queryUser(userName);
		
		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(1, actual.getTools().size());
		assertEquals(BuffToolType.Recover, actual.getTools().get(0));
	}
	
	@Test
	public void testBuyBuffTooWithoutMoney() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(1);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		
		assertTrue("Fail buy", !buyResult);
		assertEquals(1, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		userManager.saveUser(user, false);
		//Query the user again.
		User actual = userManager.queryUser(userName);
		
		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(0, actual.getTools().size());
	}
	
	@Test
	public void testSellBuffTool() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(10000);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		
		assertTrue("Success buy", buyResult);
		assertEquals(9982, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		userManager.saveUser(user, false);
		//Query the user again.
		User actual = userManager.queryUser(userName);

		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(1, actual.getTools().size());
		assertEquals(BuffToolType.Recover, actual.getTools().get(0));

		//Sell the bufftool
		shopManager.sellBuffTool(user, 1);
		assertEquals(9991, user.getGolden());
		
		//Query the user again
		actual = userManager.queryUser(userName);
		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(0, actual.getCurrentToolCount());
		
		//Sell a null tool index
		//should not throw exception
		shopManager.sellBuffTool(user, 1);
	}
	
	@Test
	public void testSellBuffTool3() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGolden(10000);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		
		String[] message = new String[1];
		boolean buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		assertTrue("Success buy", buyResult);
		assertEquals(9982, user.getGolden());
		buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		assertTrue("Success buy", buyResult);
		assertEquals(9964, user.getGolden());
		buyResult = shopManager.buyBuffTool(user, BuffToolType.Recover);
		assertTrue("Success buy", buyResult);
		assertEquals(9946, user.getGolden());
		
		UserManager userManager = UserManager.getInstance();
		userManager.saveUser(user, false);
		//Query the user again.
		User actual = userManager.queryUser(userName);

		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(3, actual.getTools().size());
		assertEquals(BuffToolType.Recover, actual.getTools().get(0));

		//Sell the bufftool
		shopManager.sellBuffTool(user, 1);
		assertEquals(9955, user.getGolden());
		shopManager.sellBuffTool(user, 2);
		assertEquals(9964, user.getGolden());
		shopManager.sellBuffTool(user, 3);
		assertEquals(9973, user.getGolden());

		//Query the user again
		actual = userManager.queryUser(userName);
		assertEquals(user.getGolden(), actual.getGolden());
		assertEquals(0, actual.getCurrentToolCount());
		
		//Sell a null tool index
		//should not throw exception
		shopManager.sellBuffTool(user, 1);
	}
	
	@Test
	public void testGetShopsByTypesAndMoney() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = {"20005", "21006"};
		int catalogId = -1;
		int money = -1;
		int gender = -1;
		
		Collection<ShopPojo> shops = shopManager.getShopsByGenderMoneyCatalogOrType(types, catalogId, money, gender, user);
				
		assertEquals(7, shops.size());
		
		//Filter the money type
		money = MoneyType.YUANBAO.type();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(types, catalogId, money, gender, user);
		
		assertEquals(4, shops.size());
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByCatalogAndGoodTypeAndGender() {
		//- BceShopping: gender=2, goldType=4, catalogId=2, types=[]
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = ShopCatalog.WEAPON.ordinal();
		int goldType = 4;
		int gender = 2;
		
		Collection<ShopPojo> shops = 
				shopManager.getShopsByGenderMoneyCatalogOrType(
						//types, catalogId, money, gender, user);
						types, catalogId, goldType, gender, user);

		assertEquals(13, shops.size());
		checkShopLevel(shops, level1Prefix);
				
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByCatalogAndMoneyAndGender() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = ShopCatalog.SUITE.ordinal();
		int money = -1;
		int gender = -1;
		
		Collection<ShopPojo> shops = 
				shopManager.getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);

		assertEquals(23, shops.size());
		checkShopLevel(shops, level1Prefix);
		
		//Filter the money type
		money = MoneyType.YUANBAO.type();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);
		
		assertEquals(16, shops.size());
		checkShopLevel(shops, level1Prefix);
		
		//Filter by the gender
		gender = Gender.FEMALE.ordinal();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);
		
		assertEquals(8, shops.size());
		checkShopLevel(shops, level1Prefix);
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByMoneyGender() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = ShopCatalog.SUITE.ordinal();
		int money = -1;
		int gender = -1;
		
		Collection<ShopPojo> shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, null);

		assertEquals(230, shops.size());
		
		//Filter the money type
		money = MoneyType.YUANBAO.type();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, null);
		
		assertEquals(160, shops.size());
		
		//Filter by the gender
		gender = Gender.FEMALE.ordinal();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);
		
		assertEquals(8, shops.size());
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByMoneyGenderLevel() {
		User user = new User();
		user.setLevel(11);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = ShopCatalog.WEAPON.ordinal();
		int money = MoneyType.YUANBAO.type();
		int gender = Gender.FEMALE.ordinal();
		
		Collection<ShopPojo> shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);

		for ( ShopPojo shop : shops ) {
			System.out.println("id:"+shop.getPropInfoId()+", name:"+shop.getInfo());
			assertTrue(shop.getInfo().startsWith(level2Prefix));
		}
				
		printShopPojoList(shops);
	}
	
	/**
	 * un testGetShopsByMoneyGenderLevelPerformance for 10000. Time:58571, Heap:202.13623M
	 * @throws Exception
	 */
	@Test
	public void testGetShopsByMoneyGenderLevelPerformance() throws Exception {
		final User user = new User();
		user.setLevel(11);
		final ShopManager shopManager = ShopManager.getInstance();
		final String[] types = null;
		final int catalogId = ShopCatalog.DECORATION.ordinal();
		final int money = MoneyType.YUANBAO.type();
		final int gender = Gender.FEMALE.ordinal();
		
		TestUtil.doPerform(new Runnable() {
			
			@Override
			public void run() {
				Collection<ShopPojo> shops = shopManager.getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);
				//printShopPojoList(shops);
			}
		}, "testGetShopsByMoneyGenderLevelPerformance", 1);

	}
	
	@Test
	public void testGetShopsByMoney() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = -1;
		int money = MoneyType.GOLDEN.type();
		int gender = -1;
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);
		
		assertTrue(""+shops.size(), shops.size()>0);
		
		//Filter by the gender
		gender = Gender.FEMALE.ordinal();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);

		assertTrue("shops.size()="+shops.size(), shops.size()>=40);
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByGender() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = -1;
		int money = -1;
		int gender = Gender.FEMALE.ordinal();
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);
		
		assertTrue(shops.size()>0);
		
		//Filter by the money
		money = MoneyType.GOLDEN.type();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);

		assertTrue(shops.size()>0);
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByGenderWithCatalog() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = ShopCatalog.ITEM.getCatalogId();
		int money = -1;
		int gender = Gender.FEMALE.ordinal();
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);
		
		assertTrue(shops.size()>0);
		
		//Filter by the money
		money = MoneyType.GOLDEN.type();
		shops = shopManager.getShopsByGenderMoneyCatalogOrType(
				types, catalogId, money, gender, user);

		assertTrue("shops.size():"+shops.size(), shops.size()>10 && shops.size()<20);
				
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByNullCondition() {
		User user = new User();
		user.setLevel(1);
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = null;
		int catalogId = -1;
		int money = -1;
		int gender = -1;
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, user);
		
		assertTrue(shops.size()>0);
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByRecommentCatalog() {
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = new String[0];
		int catalogId = 0;
		int money = 4;
		int gender = Gender.FEMALE.ordinal();
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, null);
		
		//should not contain
		//item:必成符,13043, YUANBAO,24002
		for ( ShopPojo shop : shops ) {
			assertTrue(!shop.getInfo().equals("必成符"));
		}
		
		assertEquals(27, shops.size());
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByGoldenAndGiftBox() {
		//BceShopping: gender=2, goldType=0, catalogId=6, types=[]
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = new String[0];
		int catalogId = ShopCatalog.GIFTPACK.ordinal();
		int money = MoneyType.GOLDEN.type();
		int gender = Gender.MALE.ordinal();
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, null);
		
		assertEquals(0, shops.size());
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testGetShopsByRecommendCatalog() {
		//gender=2, goldType=4, catalogId=1, types=[]
		ShopManager shopManager = ShopManager.getInstance();
		String[] types = new String[0];
		int catalogId = ShopCatalog.RECOMMEND.getCatalogId();
		int money = 4;
		int gender = 2;
		
		Collection<ShopPojo> shops = shopManager.
				getShopsByGenderMoneyCatalogOrType(
						types, catalogId, money, gender, null);
		
		assertTrue(shops.size()>0);
		
		printShopPojoList(shops);
	}
	
	@Test
	public void testSellGoodToShopWithWeapon() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();
	  /*
	   * 9544	2920	黑铁●海盗船长之帽	100	GOLDEN
	   * { "price" : 21 , "validTimes" : 30} , 
	   * { "price" : 63 , "validTimes" : 100} , 
	   * { "price" : 105 , "validTimes" : 200} , 
	   * { "price" : 210 , "validTimes" : 2147483647}
	   * 
	   * 9546	2920	黑铁●海盗船长之帽	100	YUANBAO
	   * { "price" : 4 , "validTimes" : 30} , 
	   * { "price" : 13 , "validTimes" : 100} , 
	   * { "price" : 21 , "validTimes" : 200} , 
	   * { "price" : 42 , "validTimes" : 2147483647}]	
	   */
		ShopPojo shop = shopManager.getShopById(goodId2);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		//Buy five same items
		for ( int i=0; i<5; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(goodId2Int);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(0);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		shopManager.buyGoodFromShop(user, buyProp.build());
		Bag bag = user.getBag();
		assertEquals(1, bag.getCurrentCount());
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<100);
		PropData propData = bag.getOtherPropData(20);
		assertEquals(5, propData.getCount());
		
		//Sell this item five times.
		//1
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(85, user.getYuanbao());
		assertEquals(8, user.getGolden());
		assertEquals(4, propData.getCount());
		assertEquals(20, propData.getPew());
		//2
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(16, user.getGolden());
		assertEquals(3, propData.getCount());
		//3
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(24, user.getGolden());
		assertEquals(2, propData.getCount());
		//4
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(32, user.getGolden());
		assertEquals(1, propData.getCount());
		//Remove the item from bag.
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(40, user.getGolden());
		assertEquals(0, propData.getCount());
		assertEquals(-1, propData.getPew());

		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testSellGoodToShopWithValidTime() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();
	  /*
	   * 9544	2920	黑铁●海盗船长之帽	100	GOLDEN
	   * { "price" : 21 , "validTimes" : 30} , 
	   * { "price" : 63 , "validTimes" : 100} , 
	   * { "price" : 105 , "validTimes" : 200} , 
	   * { "price" : 210 , "validTimes" : 2147483647}
	   * 
	   * 9546	2920	黑铁●海盗船长之帽	100	YUANBAO
	   * { "price" : 4 , "validTimes" : 30} , 
	   * { "price" : 13 , "validTimes" : 100} , 
	   * { "price" : 21 , "validTimes" : 200} , 
	   * { "price" : 42 , "validTimes" : 2147483647}]	
	   */
		ShopPojo shop = shopManager.getShopById(goodId2);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(goodId2Int);
		buyInfo.setCount(1);
		//{ "price" : 105 , "validTimes" : 200} , 
		buyInfo.setLeftTimeType(2);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo.build());
		shopManager.buyGoodFromShop(user, buyProp.build());
		
		Bag bag = user.getBag();
		assertEquals(1, bag.getCurrentCount());
		int buyYuanbao = user.getYuanbao();
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<100);
		PropData propData = bag.getOtherPropData(20);
		assertEquals(1, propData.getCount());
		
		//Use the item
		propData.setPropUsedTime(100);
		
		//Sell this item
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertTrue(""+user.getYuanbao(), user.getYuanbao()==buyYuanbao && user.getYuanbao()<100);
		assertEquals(43, user.getGolden());
		assertEquals(0, propData.getCount());
		assertEquals(-1, propData.getPew());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testSellGoodToShopWithNoValidTime() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(100);
		user.setGoldenSimple(0);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();
	  /*
	   * 9544	2920	黑铁●海盗船长之帽	100	GOLDEN
	   * { "price" : 21 , "validTimes" : 30} , 
	   * { "price" : 63 , "validTimes" : 100} , 
	   * { "price" : 105 , "validTimes" : 200} , 
	   * { "price" : 210 , "validTimes" : 2147483647}
	   * 
	   * 9546	2920	黑铁●海盗船长之帽	100	YUANBAO
	   * { "price" : 4 , "validTimes" : 30} , 
	   * { "price" : 13 , "validTimes" : 100} , 
	   * { "price" : 21 , "validTimes" : 200} , 
	   * { "price" : 42 , "validTimes" : 2147483647}]	
	   */
		ShopPojo shop = shopManager.getShopById(goodId2);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(goodId2Int);
		buyInfo.setCount(1);
		//{ "price" : 105 , "validTimes" : 200} , 
		buyInfo.setLeftTimeType(2);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo.build());
		shopManager.buyGoodFromShop(user, buyProp.build());
		
		Bag bag = user.getBag();
		assertEquals(1, bag.getCurrentCount());
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<100);
		PropData propData = bag.getOtherPropData(20);
		assertEquals(1, propData.getCount());
		
		//Use the item
		propData.setPropUsedTime(200);
		
		//Sell this item
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(83, user.getYuanbao());
		assertEquals(1, user.getGolden());
		assertEquals(0, propData.getCount());
		assertEquals(-1, propData.getPew());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testSellGoodToShopWithItem() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(3000);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();
		/*
		 * 294	20025	强化石Lv5	100	[ YUANBAO
		 * { "price" : 500 , "validTimes" : 1} , 
		 * { "price" : 0 , "validTimes" : 0} , 
		 * { "price" : 0 , "validTimes" : 0} , 
		 * { "price" : 0 , "validTimes" : 0}]	
		 * [ "RECOMMEND" , "HOT" , "ITEM"]	8	5	YUANBAO	0	0	0	0	0	true
		 */
		ShopPojo shop = shopManager.getShopById(stoneId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		//Buy five same items
		for ( int i=0; i<5; i++ ) {
			BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
			buyInfo.setGoodsId(stoneIdInt);
			buyInfo.setCount(1);
			buyInfo.setLeftTimeType(0);
			buyInfo.setColor(WeaponColor.GREEN.ordinal());

			buyProp.addBuyList(buyInfo.build());
		}
		shopManager.buyGoodFromShop(user, buyProp.build());
		Bag bag = user.getBag();
		assertEquals(1, bag.getCurrentCount());
		assertEquals(2800, user.getYuanbao());
		PropData propData = bag.getOtherPropData(20);
		assertEquals(5, propData.getCount());
		
		//Sell this item five times.
		//1
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(2800, user.getYuanbao());
		assertEquals(150, user.getGolden());
		assertEquals(4, propData.getCount());
		assertEquals(20, propData.getPew());
		//2
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(250, user.getGolden());
		assertEquals(3, propData.getCount());
		//3
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(350, user.getGolden());
		assertEquals(2, propData.getCount());
		//4
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(1, bag.getCurrentCount());
		assertEquals(450, user.getGolden());
		assertEquals(1, propData.getCount());
		//Remove the item from bag.
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(550, user.getGolden());
		assertEquals(0, propData.getCount());
		assertEquals(-1, propData.getPew());

		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	/**
	 * BUG #253::出售初始武器价格错误
	 */
	@Test
	public void testSellGoodToShopWithInitialWeapon() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(0);
		user.setGolden(0);
		Bag bag = user.getBag();
		bag.removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		bag.wearPropData(17, -1);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();

		ShopPojo shop = shopManager.getShopById(stoneId);
		String propId = shop.getPropInfoId();
		
		//Sell this item five times.
		//1
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(0, user.getYuanbao());
		assertTrue(""+user.getGolden(), user.getGolden()>0 && user.getGolden()<500);

		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testSellWithoutGolden() {
		/*
		 * 254	25807	粽子宝箱钥匙	100	[ 
		 * { "price" : 10 , "validTimes" : 1} , 
		 * { "price" : 0 , "validTimes" : 0} , 
		 * { "price" : 0 , "validTimes" : 0} , 
		 * { "price" : 0 , "validTimes" : 0}]	
		 * [ "ITEM"]	55	0	YUANBAO	1	0	0	0	0	true
		 */
		String keyId = "254";
		int keyIdInt = 254;
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(110);
		user.setGoldenSimple(0);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		GameContext.getInstance().setGameServerId( OtherUtil.getHostName()+":3443" );
		
		ShopManager shopManager = ShopManager.getInstance();
		ShopPojo shop = shopManager.getShopById(keyId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(keyIdInt);
		buyInfo.setCount(1);
		//{ "price" : 105 , "validTimes" : 200} , 
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo.build());
		shopManager.buyGoodFromShop(user, buyProp.build());
		
		Bag bag = user.getBag();
		assertEquals(1, bag.getCurrentCount());
		assertEquals(100, user.getYuanbao());
		PropData propData = bag.getOtherPropData(20);
		assertEquals(1, propData.getCount());
		
		//Sell this item
		shopManager.sellGoodToShop(user, 20);
		ConfirmManager.getInstance().receiveConfirmMessage(user, "shop.sell", 1);
		assertEquals(0, bag.getCurrentCount());
		assertEquals(100, user.getYuanbao());
		assertEquals(25, user.getGolden());
		assertEquals(0, propData.getCount());
		assertEquals(-1, propData.getPew());
		
		UserManager userManager = UserManager.getInstance();
		User actual = userManager.queryUser(userName);
		userManager.queryUserBag(actual);
		
		assertEquals(0, actual.getBag().getCurrentCount());
		assertEquals(user.getGolden(), actual.getGolden());
	}
	
	@Test
	public void testShopRecommendCatalog() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(120);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		ShopPojo shop = shopManager.getShopById(equipShopId);
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(equipShopInt);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.GREEN.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		assertTrue("Success buy", buyResult);
		
		ShopManager manager = ShopManager.getInstance();
		for ( int l = 1; l<100; l+=10 ) {
			user.setLevel(l);
			int count = manager.getShopsByGenderMoneyCatalogOrType(null, 
					ShopCatalog.RECOMMEND.getCatalogId(), 
				MoneyType.YUANBAO.type(), Gender.FEMALE.ordinal(), user).size();
			assertTrue("Level : " + l + ", count: " + count, count>0);
		}
	}
	
	@Test
	public void testShopRecommendCatalogGolden() {
		/**
		 * 270	20001	水神石Lv1	100
		 * 	[ { "class" :  , "price" : 10 , "validTimes" : 1} , 
		 * { "class" :  , "price" : 0 , "validTimes" : 0} , 
		 * { "class" :  , "price" : 0 , "validTimes" : 0} , 
		 * { "class" :  , "price" : 0 , "validTimes" : 0}]	
		 * [ "RECOMMEND" , "HOT" , "ITEM"]	4	-1	GOLDEN
		 */
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setGoldenSimple(120);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		
		ShopManager shopManager = ShopManager.getInstance();
		ShopPojo shop = shopManager.getShopById("270");
		String propId = shop.getPropInfoId();
		
		BceBuyProp.Builder buyProp = BceBuyProp.newBuilder();
		BuyInfo.Builder buyInfo = BuyInfo.newBuilder();
		buyInfo.setGoodsId(270);
		buyInfo.setCount(1);
		buyInfo.setLeftTimeType(0);
		buyInfo.setColor(WeaponColor.WHITE.ordinal());
		buyProp.addBuyList(buyInfo);
		
		boolean buyResult = shopManager.buyGoodFromShop(user, buyProp.build());
		assertTrue("Success buy", buyResult);
		
		ShopManager manager = ShopManager.getInstance();
		for ( int l = 1; l<100; l+=10 ) {
			user.setLevel(l);
			int count = manager.getShopsByGenderMoneyCatalogOrType(null, 
					ShopCatalog.RECOMMEND.getCatalogId(), 
				MoneyType.YUANBAO.type(), Gender.FEMALE.ordinal(), user).size();
			assertTrue("Level : " + l + ", count: " + count, count>0);
		}
	}
	
	@Test
	public void testCheckEquipmentsExpire() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.getBag().movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(
				UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(5, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 5);
				
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		bag.wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		//Item
		ItemPojo item = ItemManager.getInstance().getItemById("20001");
		PropData itemPropData = item.toPropData();
		bag.addOtherPropDatas(itemPropData);
		
		int wPower = user.getPower();
		int wAttack = user.getAttack();
		int wDefend = user.getDefend();
		assertTrue("power:"+wPower, wPower>oldPower);
		assertTrue("attack:"+wAttack, wAttack>oldAttack);
		assertTrue("defend:"+wDefend, wDefend>oldDefend);
		
		ShopManager manager = ShopManager.getInstance();
		Collection<PropData> set = null;
		for ( int i=0; i<4; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		manager.reduceUserEquipmentDuration(user);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		assertEquals(PropDataEquipIndex.WEAPON.index(), ((PropData)set.iterator().next()).getPew());
		
		int power = user.getPower();
		int attack = user.getAttack();
		int defend = user.getDefend();
		assertEquals("power:"+power, oldPower, power);
		assertEquals("attack:"+attack, oldAttack, attack);
		assertEquals("defend:"+defend, oldDefend, defend);
		
		//unwear the weapon to test the power data.
		boolean success = bag.wearPropData(PropDataEquipIndex.WEAPON.index(), Bag.BAG_WEAR_COUNT);
		assertEquals(true, success);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		assertEquals(Bag.BAG_WEAR_COUNT, ((PropData)set.iterator().next()).getPew());
		
		//try to wear the expired propData. should fail
		success = bag.wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		assertEquals(false, success);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		assertEquals(Bag.BAG_WEAR_COUNT, ((PropData)set.iterator().next()).getPew());
		
		//resubscribe it.
		/*
			11180	580	黑铁夺命刀	100	[ GOLDEN
			{ "price" : 6 , "validTimes" : 30} , 
			{ "price" : 17 , "validTimes" : 100} , 
			{ "price" : 28 , "validTimes" : 200} , 
			{ "price" : 56 , "validTimes" : 2147483647}]

		 */
		user.setYuanbaoSimple(300);
		user.setGoldenSimple(2000);
		LengthenIndate.Builder lengthBuilder = LengthenIndate.newBuilder();
		lengthBuilder.setShopid(11180);
		lengthBuilder.setId(UserManager.basicWeaponItemId);
		lengthBuilder.setIndatetype(2);
		lengthBuilder.setProppos(Bag.BAG_WEAR_COUNT);
		List<LengthenIndate> list = new ArrayList<LengthenIndate>(1);
		list.add(lengthBuilder.build());
		
		manager.resubscribePropData(user, list);
		assertEquals(300, user.getYuanbao());
		assertTrue(user.getGolden()<2000);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(0, set.size());
		assertEquals(false, propData.isExpire());
		assertEquals(bag.BAG_WEAR_COUNT, propData.getPew());
		
		//try to wear the valid propData. should succeed
		success = bag.wearPropData(Bag.BAG_WEAR_COUNT, PropDataEquipIndex.WEAPON.index());
		assertEquals(true, success);
		power = user.getPower();
		attack = user.getAttack();
		defend = user.getDefend();
		assertEquals("power:"+power, wPower, power);
		assertEquals("attack:"+attack, wAttack, attack);
		assertEquals("defend:"+defend, wDefend, defend);
	}
	
	@Test
	public void testCheckEquipmentsExpireAndGetExpireInfo() {
		User user = UserManager.getInstance().createDefaultUser();
		String userName = "test-001";
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(5, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 5);
				
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		bag.wearPropData(20, PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		//Item
		ItemPojo item = ItemManager.getInstance().getItemById("20001");
		PropData itemPropData = item.toPropData();
		bag.addOtherPropDatas(itemPropData);
		
		int wPower = user.getPower();
		int wAttack = user.getAttack();
		int wDefend = user.getDefend();
		assertTrue("power:"+wPower, wPower>oldPower);
		assertTrue("attack:"+wAttack, wAttack>oldAttack);
		assertTrue("defend:"+wDefend, wDefend>oldDefend);
		
		ShopManager manager = ShopManager.getInstance();
		Collection<PropData> set = null;
		for ( int i=0; i<4; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		manager.reduceUserEquipmentDuration(user);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		PropData expireProp = ((PropData)set.iterator().next());
		assertEquals(PropDataEquipIndex.WEAPON.index(), expireProp.getPew());
		
		Collection<ExpireInfo> expires = manager.getExpireEquipInfos(user, set);
		assertEquals(1, expires.size());
		ExpireInfo expireInfo = expires.iterator().next();
		assertEquals(MoneyType.GOLDEN.type(), expireInfo.getGoldtype());
		assertEquals(expireProp.getPew(), expireInfo.getPew());
		assertEquals(3, expireInfo.getPriceCount());
	}
	
	@Test
	public void testResubscribeWearedEquip() {
		User user = UserManager.getInstance().createDefaultUser();
		String userName = "test-001";
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.getBag().movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(5, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 5);
		
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		bag.wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		int wPower = user.getPower();
		int wAttack = user.getAttack();
		int wDefend = user.getDefend();
		assertTrue("power:"+wPower, wPower>oldPower);
		assertTrue("attack:"+wAttack, wAttack>oldAttack);
		assertTrue("defend:"+wDefend, wDefend>oldDefend);
		
		//Make it expire
		ShopManager manager = ShopManager.getInstance();
		Collection<PropData> set = null;
		for ( int i=0; i<4; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		manager.reduceUserEquipmentDuration(user);
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		assertEquals(PropDataEquipIndex.WEAPON.index(), ((PropData)set.iterator().next()).getPew());
		
		int power = user.getPower();
		int attack = user.getAttack();
		int defend = user.getDefend();
		assertEquals("power:"+power, oldPower, power);
		assertEquals("attack:"+attack, oldAttack, attack);
		assertEquals("defend:"+defend, oldDefend, defend);
				
		//resubscribe it.
		/*
			11626	680	黑铁●青龙鳞	100	[ { 
			"price" : 32 , "validTimes" : 30} , 
			"price" : 95 , "validTimes" : 100} , 
			"price" : 158 , "validTimes" : 200} , 
			"price" : 316 , "validTimes" : 2147483647}]

		 */
		user.setYuanbaoSimple(300);
		user.setGoldenSimple(2000);
		LengthenIndate.Builder lengthBuilder = LengthenIndate.newBuilder();
		lengthBuilder.setShopid(11626);
		lengthBuilder.setId(UserManager.basicWeaponItemId);
		lengthBuilder.setIndatetype(2);
		lengthBuilder.setProppos(PropDataEquipIndex.WEAPON.index());
		List<LengthenIndate> list = new ArrayList<LengthenIndate>(1);
		list.add(lengthBuilder.build());
		
		/*
		 * When the propData is resubscribed and it is wearing.
		 * The user's power should be updated.
		 */
		manager.resubscribePropData(user, list);
		assertTrue(""+user.getYuanbao(), user.getYuanbao()<300);
		assertEquals(2000, user.getGolden());
		set = manager.checkEquipmentsExpire(user);
		assertEquals(0, set.size());
		assertEquals(false, propData.isExpire());
		assertEquals(PropDataEquipIndex.WEAPON.index(), propData.getPew());
		
		power = user.getPower();
		attack = user.getAttack();
		defend = user.getDefend();
		assertEquals("power:"+power, wPower, power);
		assertEquals("attack:"+attack, wAttack, attack);
		assertEquals("defend:"+defend, wDefend, defend);
	}
	
	@Test
	public void testResubscribeWearedEquipWithWarranty() {
		User user = UserManager.getInstance().createDefaultUser();
		String userName = "test-001";
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(1000);
		user.getBag().movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		ShopManager manager = ShopManager.getInstance();
		ShopPojo shop = manager.getShopsByPropInfoId(UserManager.basicWeaponItemId).iterator().next();
		BceBuyProp.Builder builder = BceBuyProp.newBuilder();
		BuyInfo.Builder info = BuyInfo.newBuilder();
		info.setCount(1);
		info.setColor(0);
		info.setGoodsId(StringUtil.toInt(shop.getId(),0));
		info.setLeftTimeType(0);
		builder.addBuyList(info.build());
		boolean success = manager.buyGoodFromShop(user, builder.build());
		assertEquals(true, success);
		
		Bag bag = user.getBag();
		PropData propData = bag.getOtherPropData(22);
		assertEquals(UserManager.basicWeaponItemId, propData.getItemId());
		assertTrue(propData.getWarrantMillis()>0);
		assertEquals(15, propData.getWarrantDateLimit());
		
		bag.wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		//Make it expire
		Calendar cal = Calendar.getInstance();
		Collection<PropData> set = null;
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		//Make it tomorrow
		cal.add(Calendar.DAY_OF_MONTH, 1);
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user, cal.getTimeInMillis());
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		//Check database 
		Bag actualBag = UserManager.getInstance().queryUserBag(user);
		PropData actualPropData = actualBag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertEquals(propData.getWarrantDateLimit(), actualPropData.getWarrantDateLimit());
		assertEquals(propData.getPropUsedTime(), actualPropData.getPropUsedTime());
		
		//Make it tomorrow after tomorrow
		cal.add(Calendar.DAY_OF_MONTH, 1);
		manager.reduceUserEquipmentDuration(user, cal.getTimeInMillis());
		manager.reduceUserEquipmentDuration(user, cal.getTimeInMillis());
		set = manager.checkEquipmentsExpire(user);
		assertEquals(1, set.size());
		assertEquals(PropDataEquipIndex.WEAPON.index(), ((PropData)set.iterator().next()).getPew());
		
		//Renew it
		LengthenIndate.Builder lengthenIndate = LengthenIndate.newBuilder();
		lengthenIndate.setId(propData.getItemId());
		lengthenIndate.setShopid(StringUtil.toInt(shop.getId(), 0));
		lengthenIndate.setProppos(propData.getPew());
		lengthenIndate.setIndatetype(0);
		List<LengthenIndate> list = new ArrayList<LengthenIndate>();
		list.add(lengthenIndate.build());

		actualBag = UserManager.getInstance().queryUserBag(user);
		propData = actualBag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());

		manager.resubscribePropData(user, list);
		assertTrue(propData.getWarrantMillis()>0);
		assertEquals(15, propData.getWarrantDateLimit());
		assertEquals(30, propData.getPropIndate());
		assertEquals(0, propData.getPropUsedTime());
	}
	
	@Test
	public void testResubscribeWithWarrantyPassDays() {
		User user = UserManager.getInstance().createDefaultUser();
		String userName = "test-001";
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(1000);
		user.getBag().movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		ShopManager manager = ShopManager.getInstance();
		ShopPojo shop = manager.getShopsByPropInfoId(UserManager.basicWeaponItemId).iterator().next();
		BceBuyProp.Builder builder = BceBuyProp.newBuilder();
		BuyInfo.Builder info = BuyInfo.newBuilder();
		info.setCount(1);
		info.setColor(0);
		info.setGoodsId(StringUtil.toInt(shop.getId(),0));
		info.setLeftTimeType(0);
		builder.addBuyList(info.build());
		boolean success = manager.buyGoodFromShop(user, builder.build());
		assertEquals(true, success);
		
		Bag bag = user.getBag();
		PropData propData = bag.getOtherPropData(22);
		assertEquals(UserManager.basicWeaponItemId, propData.getItemId());
		assertTrue(propData.getWarrantMillis()>0);
		assertEquals(15, propData.getWarrantDateLimit());
		
		bag.wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		//Make it expire
		Calendar cal = Calendar.getInstance();
		Collection<PropData> set = null;
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		//Make it 10 days later
		cal.add(Calendar.DAY_OF_MONTH, 10);
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user, cal.getTimeInMillis());
			set = manager.checkEquipmentsExpire(user);
			if ( set.size()> 0 ) break;
		}
		assertEquals(1, set.size());
		//Check database 
		Bag actualBag = UserManager.getInstance().queryUserBag(user);
		PropData actualPropData = actualBag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertEquals(propData.getWarrantDateLimit(), actualPropData.getWarrantDateLimit());
		assertEquals(propData.getPropUsedTime(), actualPropData.getPropUsedTime());
	}
	
	@Test
	public void testPickRewardWithWarranty() {
		User user = UserManager.getInstance().createDefaultUser();
		String userName = "test-001";
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setGoldenSimple(1000);
		user.getBag().movePropData(PropDataEquipIndex.WEAPON.index(), -1);
		UserManager.getInstance().saveUserBag(user, false);
		
		int oldPower = user.getPower();
		int oldAttack = user.getAttack();
		int oldDefend = user.getDefend();
		
		Reward reward = RewardManager.generateRandomWeapon(user);
		reward.setPropIndate(10);
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		rewards.add(reward);
		RewardManager.getInstance().pickRewardWithResult(user, rewards, StatAction.BattleBegin);
		
		Bag bag = user.getBag();
		PropData propData = bag.getOtherPropData(22);
		assertTrue(propData.getWarrantMillis()>0);
		assertEquals(10, propData.getWarrantDateLimit());
		
		bag.wearPropData(propData.getPew(), PropDataEquipIndex.WEAPON.index());
		user.updatePowerRanking();
		
		//Make it expire
		Calendar cal = Calendar.getInstance();
		Collection<PropData> set = null;
		ShopManager manager = ShopManager.getInstance();
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user);
			set = manager.checkEquipmentsExpire(user);
			assertEquals(0, set.size());
		}
		//Make it 10 days later
		cal.add(Calendar.DAY_OF_MONTH, 10);
		for ( int i=0; i<100; i++ ) {
			manager.reduceUserEquipmentDuration(user, cal.getTimeInMillis());
			set = manager.checkEquipmentsExpire(user);
			if ( set.size()> 0 ) break;
		}
		assertEquals(1, set.size());
		//Check database 
		Bag actualBag = UserManager.getInstance().queryUserBag(user);
		PropData actualPropData = actualBag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		assertEquals(propData.getWarrantDateLimit(), actualPropData.getWarrantDateLimit());
		assertEquals(propData.getPropUsedTime(), actualPropData.getPropUsedTime());
	}
	
	/**
	 * BUG #253::出售初始武器价格错误
	 */
	@Test
	public void testFindPropDataPrice() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(0);
		user.setGolden(0);
		
		ShopManager shopManager = ShopManager.getInstance();

		ItemPojo item = ItemManager.getInstance().getItemById("20025");
		int price = shopManager.findPriceForPropData(user, item.toPropData(), MoneyType.GOLDEN, null, null, false);
		assertEquals(0, price);
		
		price = shopManager.findPriceForPropData(user, item.toPropData(), MoneyType.YUANBAO, null, null, false);
		assertEquals(40, price);
	}
	
	@Test
	public void testFindPropDataPriceWeapon() {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(0);
		user.setGolden(0);
		
		ShopManager shopManager = ShopManager.getInstance();

		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		int price = shopManager.findPriceForPropData(user, 
				weapon.toPropData(30, WeaponColor.WHITE), MoneyType.GOLDEN, null, null, false);
		int simplePrice = price;
		assertEquals(176, price);
		
		PropData normal = weapon.toPropData(100, WeaponColor.BLUE);
		int normalPrice = shopManager.findPriceForPropData(user, 
				normal, MoneyType.GOLDEN, null, null, false);
		assertTrue(normalPrice > simplePrice);
	}
	
	@Test
	public void testFindPropDataGoldenWithoutPrice() {
		//738	钻石●朗基努斯 没有金币的价格
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setYuanbao(0);
		user.setGolden(0);
		
		ShopManager shopManager = ShopManager.getInstance();

		WeaponPojo weapon = EquipManager.getInstance().getWeaponById("738");
		int price = shopManager.findPriceForPropData(user, 
				weapon.toPropData(30, WeaponColor.WHITE), MoneyType.GOLDEN, null, null, false);
		int simplePrice = price;
		assertEquals(21900, price);
		
		PropData normal = weapon.toPropData(100, WeaponColor.BLUE);
		int normalPrice = shopManager.findPriceForPropData(user, 
				normal, MoneyType.GOLDEN, null, null, false);
		assertTrue(normalPrice > simplePrice);
	}
	
	@Test
	public void findAllWeaponsWithoutShopPojo() {
		Collection<WeaponPojo> weapons = EquipManager.getInstance().getWeapons();
		ArrayList<WeaponPojo> noshopWeapons = new ArrayList<WeaponPojo>();
		for ( WeaponPojo weapon : weapons ) {
			Collection<ShopPojo> shops = ShopManager.getInstance().getShopsByPropInfoId(weapon.getId());
			if ( shops == null ) {
				noshopWeapons.add(weapon);
			}
		}
		for ( WeaponPojo weapon : noshopWeapons ) {
			System.out.println(weapon.getId()+":"+weapon.getName());
		}
		System.out.println("size: " + noshopWeapons.size());
		/*
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoUtil.createDBObject("_id", shop.getId());
			MongoUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
		*/
	}
	
	@Test
	public void findAllItemsWithoutShopPojo() {
		Collection<ItemPojo> items = ItemManager.getInstance().getItems();
		ArrayList<ItemPojo> noshopItems = new ArrayList<ItemPojo>();
		for ( ItemPojo item : items ) {
			Collection<ShopPojo> shops = ShopManager.getInstance().getShopsByPropInfoId(item.getId());
			if ( shops == null ) {
				noshopItems.add(item);
			}
		}
		for ( ItemPojo item : noshopItems ) {
			System.out.println(item.getId()+":"+item.getName());
		}
		System.out.println("size: " + noshopItems.size());
		/*
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoUtil.createDBObject("_id", shop.getId());
			MongoUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
		*/
	}
	
	/**
	 * 批量更改数据库中商品的价格
	 * 1. 将所有VOUCHER类型转换为GOLDEN类型
	 */
	public void processHotCatalog() {
		int[] hotGoodIds = new int[]{270,271,275,276,279,280,281,284,285,286,289,290,291,292,294,297,298,299,300,311,
				312,313,349,350,351,360,361,388,389,390,391,392,393,394,395,396,398,409,428,429,430,13039,13040,13041,13042,
		};
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();

		Jedis jedis = JedisFactory.getJedisDB();
		for ( int i : hotGoodIds ) {
			ShopPojo shop = ShopManager.getInstance().getShopById(""+i);
			String zsetName = null;
			if ( shop.getMoneyType() == MoneyType.GOLDEN ) {
				zsetName = ShopManager.REDIS_HOT_GOLDEN;
			} else if ( shop.getMoneyType() == MoneyType.YUANBAO ) {
				zsetName = ShopManager.REDIS_HOT_YUANBAO;
			}
			if ( zsetName == null ) {
				System.out.println(shop);
			}
			jedis.zincrby(zsetName, 1.0, ""+shop.getShopId());
		}
	}
	
	/**
	 * 批量更改数据库中商品栏目的方法
	 */
	public void processTheShopCatalog() {
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		for ( ShopPojo shop : shops ) {
			String id = shop.getPropInfoId();
			WeaponPojo weapon = EquipManager.getInstance().getWeaponById(id);
			if ( weapon == null ) {
				continue;
			}
			shop.removeCatalog(ShopCatalog.WEAPON);
			EquipType equipType = weapon.getSlot();
			switch ( equipType ) {
				case BUBBLE:
				case CLOTHES:
				case DECORATION:
				case EXPRESSION:
				case GLASSES:	
				case HAIR:
				case HAT:
				case JEWELRY:
				case OTHER:
				case WING:
				case FACE:
					shop.addCatalog(ShopCatalog.DECORATION);
					break;
				case GIFT_PACK:
					shop.addCatalog(ShopCatalog.GIFTPACK);
					break;
				case ITEM:
					shop.addCatalog(ShopCatalog.ITEM);
					break;
				case SUIT:
					shop.addCatalog(ShopCatalog.SUITE);
					break;
				case WEAPON:
				case OFFHANDWEAPON:
					shop.addCatalog(ShopCatalog.WEAPON);
					break;
			}
		}
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : shops ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
	}
	
	/**
	 * 批量更改数据库中商品的价格
	 * 1. 将所有VOUCHER类型转换为GOLDEN类型
	 */
	public void processShopVoucherToGolden() {
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		for ( ShopPojo shop : shops ) {
			if ( shop.getMoneyType() == MoneyType.VOUCHER ) {
				shop.setMoneyType(MoneyType.GOLDEN);
			}
		}
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : shops ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
	}
	
	/**
	 * 批量删除所有金币购买武器的'永恒品质'
	 */
	public void processShopGoldenDelete() {
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		ArrayList<ShopPojo> modified = new ArrayList<ShopPojo>();
		for ( ShopPojo shop : shops ) {
			if ( shop.getMoneyType() == MoneyType.GOLDEN && 
					!shop.isItem() ) {
				List<BuyPrice> prices = shop.getBuyPrices();
				prices.remove(prices.size()-1);
				modified.add(shop);
			}
		}
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
	}
	
	/**
	 * 批量删除指定武器的金币购买方式
	 * 
	 */
	public void processShopDeleteWeapons() {
		String[] names = {
				"火箭炮","大哥大","矿泉水","大火球","鹰眼刀","轰天炮","急速锯","朱雀羽","玄武壳","青龙鳞","白虎牙","微微安","朗基努斯","银白风尚","潜水镜","吊猫小灰","骨头饼干","水果硬糖","小猪猪","鲜花天使之翼","雷精灵之翼","爱情天使之翼","红色精灵之翼","堕落天使之翼","蝴蝶之翼","玫瑰之翼","花痴眼镜","狸猫假面","草泥马男装","超能力男装","富家少爷","HIP-POP混搭","骑士装","一酷到底","世界探险家","铁血战士","僵尸道袍","红孩儿","武斗高手","海军上将","吸血伯爵","海盗船长","神秘旅人","尖啸鬼脸","悠长假期","韩流来袭","绿贝雷","小野人","时空斗士","休闲礼帽","阳光少年","幼狐防风帽","晶玉之冠","郊游鸭舌帽","菠萝头","嘻哈盛行","海军上将之帽","魔术师之帽","海盗船长之帽","古怪男巫","爱心之戒","防护之戒","阿波罗神戒","恋爱手镯","幸运手镯","坚固手镯","阿波罗神镯","真火之心","真风的眼泪","真火焰之魂","真神之泪","日本剑士","微风拂面","正太发型","贵公子","伪娘","红色猫王","自然碎发","再现","古惑仔","蓝色风暴","欧美范儿","海绵宝宝","传奇吊车尾","黑珍珠海盗","无敌羔羊","熊猫圆圆","狐狸枫忧郁","阳光正妹","蘑菇头娃娃","银发妖犬","樱之舞银狐","僵尸新娘","俏皮绷带人","地狱伯爵",
		};
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		ArrayList<ShopPojo> modified = new ArrayList<ShopPojo>();
		for ( ShopPojo shop : shops ) {
			if ( shop.getMoneyType() == MoneyType.GOLDEN && 
					!shop.isItem() ) {
				for ( String name : names ) {
					if ( shop.getInfo().contains(name) ) {
						modified.add(shop);	
						System.out.println("name:"+name+", shop:"+shop.getInfo());
					}
				}
			}
		}
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
//			MongoUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
			MongoDBUtil.removeDocument(database, namespace, collection, query);
			System.out.println(shop.getInfo());
		}
	}
	
	/**
	 * 批量删除所有金币购买武器的'永恒品质'
	 */
	public void processShopPrice() {
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		ArrayList<ShopPojo> modified = new ArrayList<ShopPojo>();
		StringBuilder buf = new StringBuilder();
		buf.append("#ShopId\tweaponId\tname\tpower\tattack\tdefend\tagility\tlucky\tskin\n");
		for ( ShopPojo shop : shops ) {
			if ( !shop.isItem() ) {
				String id = shop.getPropInfoId();
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(id);
				if ( weapon != null ) {
					buf.append(shop.getId()).append("\t");
					buf.append(weapon.getId()).append("\t");
					buf.append(weapon.getName()).append("\t");
					buf.append(weapon.getPower()).append("\t");
					buf.append(weapon.getAddAttack()).append("\t");
					buf.append(weapon.getAddDefend()).append("\t");
					buf.append(weapon.getAddAgility()).append("\t");
					buf.append(weapon.getAddLuck()).append("\t");
					buf.append(weapon.getAddSkin()).append("\t");
					buf.append("\n");
				} else {
					System.out.println("##########Weapon " + id + " is not found.");
				}
			}
		}
		System.out.println(buf.toString());

		/*
		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoUtil.createDBObject("_id", shop.getId());
			MongoUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
		*/
	}
	
	/**
	 * 所有武器的价格调整为现在的1/6
	 */
	public void lowerWeaponShopPrice() {
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		ArrayList<ShopPojo> modified = new ArrayList<ShopPojo>();
		StringBuilder buf = new StringBuilder();
		buf.append("#ShopId\tweaponId\tname\tpower\tattack\tdefend\tagility\tlucky\tskin\n");
		for ( ShopPojo shop : shops ) {
			if ( !shop.isItem() && shop.getMoneyType() == MoneyType.YUANBAO) {
				String id = shop.getPropInfoId();
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(id);
				if ( weapon != null && weapon.getSlot() == EquipType.WEAPON ) {
					buf.append(shop.getId()).append("\t");
					buf.append(weapon.getId()).append("\t");
					buf.append(weapon.getName()).append("\t");
					buf.append(weapon.getPower()).append("\t");
					buf.append(weapon.getAddAttack()).append("\t");
					buf.append(weapon.getAddDefend()).append("\t");
					buf.append(weapon.getAddAgility()).append("\t");
					buf.append(weapon.getAddLuck()).append("\t");
					buf.append(weapon.getAddSkin()).append("\t");
					
					List<BuyPrice> prices = shop.getBuyPrices();
					for ( BuyPrice price : prices ) {
						price.price = Math.round(price.price * 1.0f/7);
						if ( price.price == 0 ) {
							price.price = 1;
						}
						buf.append("\tvalid:").append(price.validTimes);
						buf.append(" p:").append(price.price);
					}
					buf.append("\n");
					modified.add(shop);
				}
			}
		}
		System.out.println(buf.toString());

		String database = "babywar";
		String namespace = "server0001";
		String collection = "shops_new";
		for ( ShopPojo shop : modified ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(shop);
			DBObject query = MongoDBUtil.createDBObject("_id", shop.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
	}
	
	private void printShopPojoList(Collection<ShopPojo> shops ) {
		for ( ShopPojo shop : shops ) {
			if ( shop.isItem() ) {
				ItemPojo itemPojo = ItemManager.getInstance().getItemById(shop.getPropInfoId());
				System.out.println("item:" + itemPojo.getName() + "," 
						+ shop.getId()+", "+shop.getMoneyType()+","+itemPojo.getTypeId());
			} else {
				WeaponPojo weaponPojo = EquipManager.getInstance().getWeaponById(shop.getPropInfoId());
				if ( weaponPojo != null ) {
					System.out.println("Weapon: " + weaponPojo.getName() + ", "
						+ shop.getId()+", "+shop.getMoneyType()+", "+weaponPojo.getSex());
				}
			}
		}
	}
	
	private void checkShopLevel(Collection<ShopPojo> shops, String level) {
		for ( ShopPojo shop : shops ) {
			if ( !shop.isItem() ) {
				WeaponPojo weapon = EquipManager.getInstance().getWeaponById(shop.getPropInfoId());
				assertTrue(weapon.getName().contains(level));
			}
		}
	}
	
	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		IoSession session = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session, user, user.getSessionKey());
		return user;
	}
}
