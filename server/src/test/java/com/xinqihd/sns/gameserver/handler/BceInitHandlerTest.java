package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceInit.BceInit;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister.BceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit.BseInit;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceInitHandlerTest {

	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitWithoutTimeout() throws Exception {
		String userName = "test_inithandler";
		
		//First register user
		registerUser(userName);
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		User user = manager.queryUser(userName);
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, user.getSessionKey());
		
		verify(session);
		
		assertTrue(list.size()>=7);
		ArrayList<String> classNames = new ArrayList<String>();
		int i = 0;
		String token = null;
		for ( XinqiMessage xinqi : list ) {
			classNames.add(xinqi.payload.getClass().getName());
			if ( xinqi.payload instanceof XinqiBseInit.BseInit ) {
				XinqiBseInit.BseInit bseInit = (XinqiBseInit.BseInit)xinqi.payload;
				token = bseInit.getToken();
			}
		}
		list.clear();
		
		//The connection is broken
		GameContext.getInstance().deregisterUserByIoSession(session);
		
		//Make sure the secure token is valid
		assertNotNull("Secure token should not be null.", token);
		
		BceInit bceInit = BceInit.newBuilder().setToken(token).build();
		//Test the reconnect request.
		BceInitHandler initHandler = BceInitHandler.getInstance();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = bceInit;
		
		CipherManager.setDefaultTimeout(360000);
		
		initHandler.messageProcess(session, xinqi, user.getSessionKey());
		
		Thread.sleep(200);
		
		xinqi = (XinqiMessage)list.get(0);
		BseInit bseInit = (BseInit)xinqi.payload;
		
		assertTrue("Reconnect should be success",bseInit.getSuccess());
		assertTrue("Refresh should be false", !bseInit.getRefresh());
		assertNotNull(bseInit.getToken());
	}
	
	@Test
	public void testInitNull() throws Exception {
		String token = "";
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		
		XinqiMessage message = new XinqiMessage();
		
		BceInit bceInit = BceInit.newBuilder().setToken(token).build();
		//Test the reconnect request.
		BceInitHandler initHandler = BceInitHandler.getInstance();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = bceInit;
		
		initHandler.messageProcess(session, xinqi, null);
		
		Thread.sleep(200);
		verify(session);
		
		xinqi = (XinqiMessage)list.get(0);
		BseInit bseInit = (BseInit)xinqi.payload;
		
		assertTrue("Reconnect should be fail", !bseInit.getSuccess());
		assertTrue("Refresh should be true", bseInit.getRefresh());
		assertNotNull(bseInit.getToken());
	}
	
	@Test
	public void testInitWithTimeout() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "1");
		GameContext.getTestInstance();
		
		String userName = "test_inithandler";
		
		//First register user
		registerUser(userName);
		
		//Update the user's bag;
		UserManager manager = UserManager.getInstance();
		User user = manager.queryUser(userName);
		manager.saveUser(user, true);
		manager.saveUserBag(user, true);
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceLoginHandler handler = BceLoginHandler.getInstance();
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);

		handler.messageProcess(session, message, user.getSessionKey());
		
		Thread.sleep(200);
		
		assertTrue(list.size()>=7);
		ArrayList<String> classNames = new ArrayList<String>();
		int i = 0;
		String token = null;
		for ( XinqiMessage xinqi : list ) {
			classNames.add(xinqi.payload.getClass().getName());
			if ( xinqi.payload instanceof XinqiBseInit.BseInit ) {
				XinqiBseInit.BseInit bseInit = (XinqiBseInit.BseInit)xinqi.payload;
				token = bseInit.getToken();
			}
		}
		list.clear();
		
		//The connection is broken
		GameContext.getInstance().deregisterUserByIoSession(session);
		
		//Make sure the secure token is valid
		assertNotNull("Secure token should not be null.", token);
		
		long nextTimeout = System.currentTimeMillis() + 20000;
		while ( System.currentTimeMillis() < nextTimeout ) {
			SessionKey sessionKey = (SessionKey)session.getAttribute(Constant.SESSION_KEY);
			Jedis jedis = JedisFactory.getJedis();
			Long ttl = jedis.ttl(sessionKey.getRawKey());
			System.out.println("================ ttl = " + ttl);
			if ( ttl == null || ttl == -1 ) {
				break;
			}
		}
		
		BceInit bceInit = BceInit.newBuilder().setToken(token).build();
		//Test the reconnect request.
		BceInitHandler initHandler = BceInitHandler.getInstance();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = bceInit;
		
		initHandler.messageProcess(session, xinqi, user.getSessionKey());
		
		Thread.sleep(200);
		
		xinqi = (XinqiMessage)list.get(0);
		BseInit bseInit = (BseInit)xinqi.payload;
		
		assertTrue("Reconnect should be success",bseInit.getSuccess());
		assertTrue("Refresh should be true", bseInit.getRefresh());
		assertNotNull(bseInit.getToken());
	}
	
	// ------------------------------------------------- Tools

	private void registerUser(String userName) throws Exception {
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
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
//				assertEquals(BceRegisterHandler.ErrorCode.EXIST.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}

}
