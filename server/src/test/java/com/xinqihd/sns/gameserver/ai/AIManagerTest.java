package com.xinqihd.sns.gameserver.ai;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.bootstrap.Bootstrap;
import com.xinqihd.sns.gameserver.bootstrap.ReloadClassLoader;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattleStageReady.BceBattleStageReady;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceEnterRoom.BceEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceReadyStart.BceReadyStart;
import com.xinqihd.sns.gameserver.proto.XinqiBseBattleInit.BseBattleInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoundStart.BseRoundStart;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.server.GameServer;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class AIManagerTest {
	
	GameServer gameServer = GameServer.getInstance();
	GameClient gameClient = null;
	String host = "localhost";
	String gamePort = "13443";
	String aiPort = "13446";
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {		
		Bootstrap.main(new String[]{
				"-t", "zoo", "-h", host, "-p", gamePort, "-u", "target/classes", 
				"-z", "zoo.babywar.xinqihd.com:2181", "-s", "src/main/script", "-d", "../deploy/data"});
		
		Thread.currentThread().setContextClassLoader(ReloadClassLoader.currentClassLoader());
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_READY_TIMEOUT, 5000);
		
		gameClient = new GameClient(host, Integer.parseInt(gamePort));
		gameClient.connectToServer();
	}

	@After
	public void tearDown() throws Exception {
		gameClient.disconnectFromServer();
		
		Bootstrap.getInstance().shutdownServer();
	}

	@Test
	public void testCreateAndDestroyAIUser() throws Exception {
		String localAIServerId = host+":"+aiPort;
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_AI_SERVERID, localAIServerId);
		User realUser = prepareUser(userName, 1000);
		
		AIManager manager = AIManager.getInstance();
		User aiUser = manager.createAIUser(realUser);
		
		UserId aiUserId = GameContext.getInstance().getSessionManager().
				findUserIdBySessionKey(aiUser.getSessionKey());
		assertEquals(aiUser.get_id(), aiUserId);
		
		String aiMachineId = GameContext.getInstance().getSessionManager().
				findUserMachineId(aiUserId);
		
//		assertEquals(localAIServerId, aiMachineId);
		String[] fields = StringUtil.splitMachineId(aiMachineId);
		assertEquals(""+aiPort, fields[1]);
		
		assertNotNull( GameContext.getInstance().findUserIdBySessionKey(aiUser.getSessionKey()));
		
		manager.destroyAIUser(aiUser.getSessionKey());
		
		assertNull( GameContext.getInstance().findUserIdBySessionKey(aiUser.getSessionKey()));
	}

	/**
	 * It is deprecated
	 * @throws Exception
	 */
	public void testChatToAIUser() throws Exception {		
		String localAIServerId = host+":"+aiPort;
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_AI_SERVERID, localAIServerId);
		User realUser = prepareUser(userName, 1000);
		
		//Login the real user
		loginUser(realUser);
		
		Thread.sleep(200);
		gameClient.clearLastMessage();
		
		AIManager manager = AIManager.getInstance();
		
		//Create a AI user
		User aiUser = manager.createAIUser(realUser);
		
		XinqiMessage message = new XinqiMessage();
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgType(ChatType.ChatPrivate.ordinal());
		builder.setMsgContent("hello world");
		builder.setUsrId(aiUser.get_id().toString());
		message.payload = builder.build();
		gameClient.sendMessageToServer(message);
		
		System.out.println("================  to send chat to ai user ===============");
		
		long timeout = System.currentTimeMillis() + 20000;
		boolean success = false;
		while ( System.currentTimeMillis() < timeout ) {
			XinqiMessage xinqi = (XinqiMessage)gameClient.getLastMessage();
			if ( xinqi != null && xinqi.payload != null && 
					xinqi.payload instanceof BseChat ) {
				success = true;
				break;
			}
			Thread.sleep(100);
		}
		assertTrue(success);
	}
	
	@Test
	public void testBattleWithAI() throws Exception {		
		String localAIServerId = host+":"+aiPort;
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_AI_SERVERID, localAIServerId);
		User realUser = prepareUser(userName, 1000);
		
		//Login the real user
		loginUser(realUser);
		
		//Make it enters the room
		enterRoom(realUser);
		
		//Make it press 'ready' button
		readyStart(realUser);

		System.out.println("================  wait for battle to begin ===============");
		
		waitForMessage(BseBattleInit.class);
		
		stageReady(realUser);
		
		System.out.println("================  wait for roundstart  ===============");
		
		waitForMessage(BseRoundStart.class);
		
		Thread.sleep(100000);
	}
	
	@Test
	public void testRandomAIName() {
		for ( int i=0; i<10; i++ ) {
			System.out.println( AIManager.getRandomAIName() );
		}
	}
	
	@Test
	public void testAIMessageFlow() throws Exception {
		BceReadyStart.Builder builder = BceReadyStart.newBuilder();
		builder.setIsReady(true);
		BceReadyStart readyStart = builder.build();
		
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_AI_SERVERID, "www.baidu.com:80");
		User user = prepareUser("test-001", 100);
		user.setAI(true);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session, user, null);
		Map<SessionKey, User> loginUserMap = (Map<SessionKey, User>)
				TestUtil.getPrivateFieldValue("loginUserMap", GameContext.getInstance());
		loginUserMap.remove(user.getSessionKey());
		
		assertTrue(AIManager.getInstance().isAIUser(user.getSessionKey()));
		GameContext.getInstance().writeResponse(user.getSessionKey(), readyStart);
		//Thread.sleep(10000000);
	}
	
	private boolean waitForMessage(Class MessageClass) throws Exception {
		long timeout = System.currentTimeMillis() + 20000;
		boolean success = false;
		while ( System.currentTimeMillis() < timeout ) {
			ArrayList list = new ArrayList(gameClient.getLastMessages());
			gameClient.clearLastMessages();
			for ( int i=0; i<list.size(); i++ ) {
				XinqiMessage xinqi = (XinqiMessage)list.get(i);
				if ( xinqi != null && xinqi.payload != null && 
						MessageClass.isAssignableFrom(xinqi.payload.getClass()) ) {
					success = true;
					return true;
				}
			}
			Thread.sleep(100);
		}
		return false;
	}
	
	private void loginUser(User user) {
		BceLogin.Builder builder = BceLogin.newBuilder();
		builder.setUsername(user.getUsername());
		//123456
		builder.setPassword("123456");
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = builder.build();
		
		gameClient.sendMessageToServer(xinqi);
	}
	
	private void enterRoom(User user) {
		BceEnterRoom.Builder builder = BceEnterRoom.newBuilder();
		builder.setBattleMode(-1);
		builder.setChallengeId("0");
		builder.setChooseMode(1);
		builder.setRoomId("");
		builder.setMapId(-1);
		builder.setRoomType(RoomType.TRAINING_ROOM.ordinal());
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = builder.build();
		
		gameClient.sendMessageToServer(xinqi);
	}
	
	private void readyStart(User user) {
		BceReadyStart.Builder builder = BceReadyStart.newBuilder();
		builder.setIsReady(true);
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = builder.build();
		
		gameClient.sendMessageToServer(xinqi);
	}
	
	private void stageReady(User user) {
		BceBattleStageReady.Builder builder = BceBattleStageReady.newBuilder();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = builder.build();
		
		gameClient.sendMessageToServer(xinqi);
	}
	
	private User prepareUser(String userName, int exp) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setRoleName(userName);
		user.setUsername(userName);
		user.setPassword("7c4a8d09ca3762af61e59520943dc26494f8941b");
		user.setExp(1000);
		//Add weapon
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		PropData propData = weapon.toPropData(28, WeaponColor.WHITE); 
		PropData newPropData = (PropData)ScriptManager.getInstance().runScriptForObject(
				ScriptHook.WEAPON_LEVEL_UPGRADE, propData, 2);
		user.getBag().addOtherPropDatas(newPropData);
		user.getBag().wearPropData(newPropData.getPew(), PropDataEquipIndex.WEAPON.index());
		
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
