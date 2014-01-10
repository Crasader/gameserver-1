package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister.BceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseExpireEquipments.BseExpireEquipments;
import com.xinqihd.sns.gameserver.script.function.EquipCalculator;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceLoginReadyHandlerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_RPC_SERVERID, "localhost:0");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
		BattleDataLoader4Bitmap.loadBattleMaps();
		BattleDataLoader4Bitmap.loadBattleBullet();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserLoginWithExpireEquipments() throws Exception {		
		String userName = randomUserName();
		
		//First register user
		registerUser2(userName);
		
		User user = UserManager.getInstance().queryUser(userName);
		UserManager.getInstance().queryUserBag(user);
		
		//Wear something
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(5, WeaponColor.WHITE);
		EquipCalculator.weaponUpLevel(propData, 5);
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		bag.wearPropData(21, PropDataEquipIndex.WEAPON.index());
		UserManager.getInstance().saveUser(user, false);
		UserManager.getInstance().saveUserBag(user, false);
		
		List<XinqiMessage> list = null;
//		for ( int i=0; i<5; i++ ) {
//			list = loginUser(userName);
//			BseExpireEquipments equips = getExpireEquips(list);
//			assertNull(equips);
//		}
		
		//Joining 5 battles to make the weapon expire
		list = loginUser(userName);
		for ( int i=0; i<5; i++ ) {
			user = UserManager.getInstance().queryUser(userName);
			UserManager.getInstance().queryUserBag(user);
			makeReadyBattle(user);
			PropData myPropData = user.getBag().getWearPropDatas().get(17);
			assertEquals(i+1, myPropData.getPropUsedTime());
		}
		
		list = loginUser(userName);
		BseExpireEquipments equips = getExpireEquips(list);
		System.out.println(equips);
		assertNotNull(equips);
	}

	
	private String randomUserName() {
		String user = "test";
		Random r = new Random();
		return user + r.nextInt(9999999);
	}
	
	private BseExpireEquipments getExpireEquips(List<XinqiMessage> list) {
		for ( XinqiMessage msg : list ) {
			if ( msg.payload instanceof BseExpireEquipments ) {
				return (BseExpireEquipments)msg.payload;
			}
		}
		return null;
	}
	
	private List<XinqiMessage> loginUser(String userName) throws Exception {
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler1 = BceLoginHandler.getInstance();
		BceLoginReadyHandler handler2 = BceLoginReadyHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler1.messageProcess(session, message, null);
		User user = UserManager.getInstance().queryUser(userName);
		SessionKey userSessionKey = GameContext.getInstance()
				.findSessionKeyByUserId(user.get_id());
		handler2.messageProcess(session, message, userSessionKey);
				
		return list;
	}
	
	private Battle makeReadyBattle(User user1) {
		BattleManager battleManager = BattleManager.getInstance();

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test002");

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		user2.setAI(true);
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(makeBuffTool(i));
		}

		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		return battle;
	}
	
	private void registerUser2(String userName) throws Exception {
		BceRegister.Builder payload = BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createMock(IoSession.class);
		
		session.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	private BuffToolType makeBuffTool(int i) {
		if ( i >= 0 && i<BuffToolType.values().length ) {
			return BuffToolType.values()[i];
		} else {
			return BuffToolType.Recover;
		}
	}
}
