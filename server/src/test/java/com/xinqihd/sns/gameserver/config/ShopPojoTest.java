package com.xinqihd.sns.gameserver.config;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.proto.XinqiGoodsInfo;

public class ShopPojoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToGoodsInfo() {
	  //水神石Lv4
		String itemId = "20001";
		Collection<ShopPojo> shopList = ShopManager.getInstance().getShopsByPropInfoId(itemId);
		assertEquals(1, shopList.size());
		ShopPojo shopPojo1 = shopList.iterator().next();
		XinqiGoodsInfo.GoodsInfo goodsInfo = shopPojo1.toGoodsInfo();
		assertEquals(0, goodsInfo.getAgilityLev());
		assertEquals(0, goodsInfo.getAttackLev());
		assertEquals(0, goodsInfo.getDefendLev());
		assertEquals(0, goodsInfo.getLuckLev());
		
		if ( shopPojo1.getCatalogs().contains(ShopCatalog.RECOMMEND ) ) {
			assertEquals(12, goodsInfo.getSign());
		} else {
			assertEquals(4, goodsInfo.getSign());
		}
		assertEquals(0, goodsInfo.getGoldtype());
		assertEquals(100, goodsInfo.getDiscount());
		
		assertEquals(4, goodsInfo.getPriceCount());
		assertEquals(4, goodsInfo.getIndateCount());
	}

	@Test
	public void testToGoodsInfo2() {
	  //"榴弹炮"
		String itemId = UserManager.basicWeaponItemId;
		/**
		 * 黑铁●榴弹炮 
		 * golden
		 * YUANBAO
		 */
		Collection<ShopPojo> shopList = ShopManager.getInstance().getShopsByPropInfoId(itemId);
		assertEquals(2, shopList.size());
		ShopPojo shopPojo1 = shopList.iterator().next();
		XinqiGoodsInfo.GoodsInfo goodsInfo = shopPojo1.toGoodsInfo();
		assertEquals(53, goodsInfo.getAgilityLev());
		assertEquals(9, goodsInfo.getAttackLev());
		assertEquals(10, goodsInfo.getDefendLev());
		assertEquals(243, goodsInfo.getLuckLev());
		
		if ( shopPojo1.getCatalogs().contains(ShopCatalog.RECOMMEND ) ) {
			assertEquals(12, goodsInfo.getSign());
		} else {
			assertEquals(0, goodsInfo.getSign());
		}
		assertEquals(4, goodsInfo.getGoldtype());
		assertEquals(100, goodsInfo.getDiscount());
		
		assertEquals(4, goodsInfo.getPriceCount());
		assertEquals(4, goodsInfo.getIndateCount());
	}
}
