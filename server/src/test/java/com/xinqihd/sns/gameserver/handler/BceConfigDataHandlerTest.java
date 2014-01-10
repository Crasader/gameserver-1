package com.xinqihd.sns.gameserver.handler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.Message;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.PropDataValueType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceConfigData.BceConfigData;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.BseChargeList;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.ChargeData;
import com.xinqihd.sns.gameserver.proto.XinqiBseConfigData.BseConfigData;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.BseShop;
import com.xinqihd.sns.gameserver.proto.XinqiBseShop.ShopData;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.IdToMessage;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceConfigDataHandlerTest extends AbstractHandlerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		super.setUp(false, "users", Constant.LOGIN_USERNAME);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testGetConfigData() throws Exception {
		String userName = "test001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");

		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername("test001");
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		BceConfigData.Builder payload = BceConfigData.getDefaultInstance().newBuilderForType();
		payload.setVersion(0);
		BceConfigData msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceConfigDataHandler handler = BceConfigDataHandler.getInstance();

		handler.messageProcess(session, message, (SessionKey)session.getAttribute(Constant.SESSION_KEY));
		
		Thread.sleep(200);
		/*
 com.xinqihd.sns.gameserver.proto.XinqiBseConfigData$BseConfigData], 
 com.xinqihd.sns.gameserver.proto.XinqiBseEquipment$BseEquipment], 
 com.xinqihd.sns.gameserver.proto.XinqiBseMap$BseMap], 
 com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList$BseDailyMarkList], 
 com.xinqihd.sns.gameserver.proto.XinqiBseTip$BseTip], 
 com.xinqihd.sns.gameserver.proto.XinqiBseTask$BseTask], 
 com.xinqihd.sns.gameserver.proto.XinqiBseShop$BseShop], 
 com.xinqihd.sns.gameserver.proto.XinqiBseGameDataKey$BseGameDataKey], 
 com.xinqihd.sns.gameserver.proto.XinqiBseItem$BseItem], 
 com.xinqihd.sns.gameserver.proto.XinqiBseChargeList$BseChargeList], 
 com.xinqihd.sns.gameserver.proto.XinqiBseVipPeriodList$BseVipPeriodList]]
		 */
		assertEquals(11, list.size());
		Set<String> classList = new HashSet<String>(12);
		int i = 0;
		BseConfigData configData = null;
		for ( XinqiMessage xinqi : list ) {
			classList.add(xinqi.payload.getClass().getName());
			System.out.println(
					"Type: " + xinqi.type + "[" 
							+ IdToMessage.idToMessage(xinqi.type)
					+"], Payload Class: " + xinqi.payload.getClass()
					+ ", Length: " + xinqi.payload.toByteArray().length);
			if ( xinqi.payload instanceof BseConfigData ) {
				configData = (BseConfigData)xinqi.payload;
			}
		}
		assertNotNull(configData);
		assertTrue(configData.getVersion()>0);
		
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseDailyMarkList$BseDailyMarkList"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseEquipment$BseEquipment"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseGameDataKey$BseGameDataKey"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseMap$BseMap"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseShop$BseShop"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTask$BseTask"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseTip$BseTip"));
		assertTrue(classList.contains("com.xinqihd.sns.gameserver.proto.XinqiBseItem$BseItem"));
	}
	
	@Test
	public void testUserLoginVip() throws Exception {
		String userName = "test001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");

		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername("test001");
		UserManager.getInstance().saveUser(user, true);
		
		List<Message> list = loginUser(userName, user);
		
		ShopData shopData = (ShopData)list.get(0);
		ChargeData chargeData = null;
		for ( Message msg : list ) {
			if ( msg != null ) {
				if ( msg instanceof ChargeData ) {
					chargeData = (ChargeData)msg;
				} else if ( msg instanceof ShopData ) {
					shopData = (ShopData)msg;
				}
			}
		}
		int shopPrice = shopData.getBuyPrices(0).getPrice();
		int shopDiscount = shopData.getDiscount();
		System.out.println("shopPrice = " + shopPrice+", shopDiscount = " + shopDiscount);
		int chargePrice = chargeData.getPrice();
		int chargeDiscount = chargeData.getDiscount();
		int yuanbao = chargeData.getYuanbao();
		System.out.println("chargePrice = " + chargePrice + 
				", chargeDiscount="+chargeDiscount+", yuanbao="+yuanbao);
		
		//Make the user VIP
		user = UserManager.getInstance().queryUser(userName);
		user.setIsvip(true);
		user.setVipedate(new Date(System.currentTimeMillis()+86400));
		UserManager.getInstance().saveUser(user, false);
		
		list = loginUser(userName, user);
		shopData = (ShopData)list.get(0);
		chargeData = (ChargeData)list.get(1);
		int newShopPrice = shopData.getBuyPrices(0).getPrice();
		int newShopDiscount = shopData.getDiscount();
		System.out.println("shopPrice = " + newShopPrice+", shopDiscount = " + newShopDiscount);
		assertEquals( shopPrice, newShopPrice);
		assertTrue( newShopDiscount < shopDiscount );
		int newChargePrice = chargeData.getPrice();
		int newChargeDiscount = chargeData.getDiscount();
		int newYuanbao = chargeData.getYuanbao();
		System.out.println("chargePrice = " + newChargePrice + 
				", chargeDiscount="+newChargeDiscount+", yuanbao="+newYuanbao);
		assertEquals(chargePrice, newChargePrice);
		assertTrue(newChargeDiscount < 100);
		assertTrue(newYuanbao > yuanbao);
	}
	
	@Test
	public void testUserLoginVipExpire() throws Exception {
		String userName = "test001";
		UserManager manager = UserManager.getInstance();
		manager.removeUser(userName);
		
		User user = manager.createDefaultUser();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		UserManager.getInstance().saveUser(user, true);
		
		//Make the user VIP expire
		user = UserManager.getInstance().queryUser(userName);
		user.setIsvip(true);
		user.setVipedate(new Date(System.currentTimeMillis()-86400));
		manager.saveUser(user, true);
		
		List<Message> list = loginUser(userName, user);
		
		ShopData shopData = (ShopData)list.get(0);
		ChargeData chargeData = (ChargeData)list.get(1);
		int shopPrice = shopData.getBuyPrices(0).getPrice();
		int shopDiscount = shopData.getDiscount();
		System.out.println("shopPrice = " + shopPrice+", shopDiscount = " + shopDiscount);
		int chargePrice = chargeData.getPrice();
		int chargeDiscount = chargeData.getDiscount();
		int yuanbao = chargeData.getYuanbao();
		System.out.println("chargePrice = " + chargePrice + 
				", chargeDiscount="+chargeDiscount+", yuanbao="+yuanbao);
		
		assertEquals(100, shopDiscount);
		assertEquals(0, chargeDiscount);
	}
	

	
	private String randomUserName() {
		String user = "test";
		Random r = new Random();
		return user + r.nextInt(9999999);
	}
	
	// ----------------------------------------------------------
	
	private Bag makeBag(User user, int count) {
		Bag bag = new Bag();
//		bag.set_id(user.get_id());
//		bag.setParentUser(user);
		for ( int i=0; i<count; i++) {
			bag.addOtherPropDatas(makePropData(1000+i));
		}
		bag.wearPropData(Constant.BAG_WEAR_COUNT+0, PropDataEquipIndex.WEAPON.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+1, PropDataEquipIndex.RING1.index());
		bag.wearPropData(Constant.BAG_WEAR_COUNT+2, PropDataEquipIndex.RING2.index());
		return bag;
	}
	
	/**
	 * Make a fake PropData
	 * @param i
	 * @return
	 */
	private PropData makePropData(int i) {
		PropData propData = new PropData();
		propData.setItemId("510");
		propData.setName("夺命刀"+i);
		propData.setBanded(true);
		propData.setValuetype(PropDataValueType.BONUS);
		propData.setAgilityLev(1000);
		propData.setAttackLev(1001);
		propData.setDefendLev(1002);
		propData.setDuration(1003);
		propData.setLuckLev(1004);
		propData.setSign(1005);
		return propData;
	}
	
	private BuffToolType makeBuffTool(int i) {
		if ( i >= 0 && i<BuffToolType.values().length ) {
			return BuffToolType.values()[i];
		} else {
			return BuffToolType.Recover;
		}
	}
	
	private List<Message> loginUser(String userName, User user) throws Exception {
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		BceConfigData.Builder payload = BceConfigData.getDefaultInstance().newBuilderForType();
		payload.setVersion(0);
		BceConfigData msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceConfigDataHandler handler = BceConfigDataHandler.getInstance();

		handler.messageProcess(session, message, (SessionKey)session.getAttribute(Constant.SESSION_KEY));
		
		Thread.sleep(200);
		
		BseShop bseShop = null;
		BseChargeList bseCharge = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseShop ) {
				bseShop = (BseShop)xinqi.payload;
			} else if ( xinqi.payload instanceof BseChargeList ) {
				bseCharge = (BseChargeList)xinqi.payload;
			}
		}
		assertNotNull(bseShop);
		assertNotNull(bseCharge);
		
		List<ShopData> shopDataList = bseShop.getShopsList();
		/*
			9276	2860	黑铁●菠萝头
			}
		 */
		ShopData shopData = null;
		for ( ShopData sd : shopDataList ) {
			if ( sd.getId().equals("11712") ) {
				shopData = sd;
				break;
			}
		}

		List<Message> returnMessage = new ArrayList<Message>();
		returnMessage.add(shopData);
		return returnMessage;
	}
}
