package com.xinqihd.sns.gameserver.handler;

import static com.xinqihd.sns.gameserver.config.Constant.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.Gender;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceRegisterHandlerTest extends AbstractHandlerTest {
	
	@Before
	public void setUp() throws Exception {
		super.setUp(false, "users", LOGIN_USERNAME);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMessageReceived() throws Exception {
		String userName = randomUserName();
		XinqiBceRegister.BceRegister.Builder payload = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		XinqiBceRegister.BceRegister msg = payload.build();
		
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
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMessageReceived2() throws Exception {
		String userName = randomUserName();
		
		XinqiBceRegister.BceRegister.Builder payload = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		XinqiBceRegister.BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		
		
		expect(session.write(anyObject())).andAnswer(new IAnswer() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				return null;
			}
		});
		expect(session.write(anyObject())).andAnswer(new IAnswer() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.EXIST.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMessageReceived3() throws Exception {
		String userName = randomUserName();
		
		XinqiBceRegister.BceRegister.Builder payload = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setRolename("nickname");
		payload.setPassword("000000");
		payload.setEmail("nick@nick.com");
		payload.setGender(1);
		payload.setClient("iphone4");
		payload.setChannel("channel");
		payload.setCountry("cn");
		payload.setLocx(100);
		payload.setLocy(900);
		
		XinqiBceRegister.BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		
		
		expect(session.write(anyObject())).andAnswer(new IAnswer() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
		
		//Check result
		User user = GameContext.getInstance().getUserManager().queryUser(userName);
		assertEquals("nickname", user.getRoleName());
		assertEquals(StringUtil.encryptSHA1("000000"), user.getPassword());
		assertEquals("nick@nick.com", user.getEmail());
		assertEquals(Gender.FEMALE, user.getGender());
		assertEquals("iphone4", user.getClient());
		assertEquals("channel", user.getChannel());
		assertEquals("cn", user.getCountry());
		assertEquals(100, user.getLocation().x);
		assertEquals(900, user.getLocation().y);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMessageReceivedWithBag() throws Exception {
		final String userName = randomUserName();
		
		XinqiBceRegister.BceRegister.Builder payload = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		XinqiBceRegister.BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();
		
		IoSession session = createNiceMock(IoSession.class);
		
		
		expect(session.write(anyObject())).andAnswer(new IAnswer() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				User user = UserManager.getInstance().queryUser(userName);
				UserManager.getInstance().queryUserBag(user);
				PropData weapon = (PropData)(user.getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index()));
				assertNotNull(weapon);
				return null;
			}
		});
		expect(session.write(anyObject())).andAnswer(new IAnswer() {
			public Object answer() {
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[0];
				XinqiBseRegister.BseRegister register = (XinqiBseRegister.BseRegister)response.payload;
				assertEquals(LoginManager.RegisterErrorCode.EXIST.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
	
	@Test
	public void testRegisterWithNewMethod() throws Exception {
		String userName = "10000";
		String roleName = "nickname";
		String email = "email";
		int gender = 1;
		
		UserManager.getInstance().removeUser(userName);
		User user = new User();
		user.set_id(new UserId(userName));
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setPassword("");
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager.getInstance().saveUser(user, true);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session, user, user.getSessionKey());
		
		XinqiBceRegister.BceRegister.Builder payload = XinqiBceRegister.BceRegister.getDefaultInstance().newBuilderForType();
		//New method need empty userName
		payload.setUsername("");
		payload.setRolename(roleName);
		payload.setPassword("000000");
		payload.setGender(gender);
		payload.setEmail(email);
		XinqiBceRegister.BceRegister msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		BceRegisterHandler handler = BceRegisterHandler.getInstance();		
		handler.messageProcess(session, message, user.getSessionKey());
		
		User newUser = UserManager.getInstance().queryUser(userName);
		assertEquals(roleName, newUser.getRoleName());
		assertEquals(email, newUser.getEmail());
		assertEquals(gender, newUser.getGender().ordinal());
	}
	
	private String randomUserName() {
		String user = "";
		Random r = new Random();
		return user + r.nextInt(99999);
	}
}
