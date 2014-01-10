package com.xinqihd.sns.gameserver.session;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.JedisUtil;

public class SessionManagerTest {

	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegisterSession() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		final HashMap map = new HashMap();
		expect(session.setAttribute(anyObject(), anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[1];
				map.put(getCurrentArguments()[0], getCurrentArguments()[1]);
				return arg;
			}

		}).anyTimes();
		expect(session.removeAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[0];
				map.remove(arg);
				return null;
			}
			
		}).anyTimes();
		expect(session.getAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				return map.get(getCurrentArguments()[0]);
			}
			
		}).anyTimes();
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		
		System.out.println(message.getSessionkey().toString());
		assertEquals(message.getSessionkey().toString().substring(0, 10), 
				user.getSessionKey().toString().substring(0, 10));
		Object acutalSessionKey = session.getAttribute(Constant.SESSION_KEY);
		assertEquals(message.getSessionkey().toString().substring(0, 16), 
				acutalSessionKey.toString().substring(0, 16));
		
		Jedis jedis = JedisFactory.getJedis();
		assertEquals("localhost:12345", new String(
				jedis.hget(createdKey.toString(), SessionManager.H_MACHINE_KEY)));
		assertEquals(user.get_id(), UserId.fromString(
				jedis.hget(createdKey.toString(), SessionManager.H_USERID_KEY)));
		
		assertEquals("localhost:12345", new String(
				jedis.hget(user.get_id().toString(), SessionManager.H_MACHINE_KEY)));
		SessionKey queryKey = SessionKey.createSessionKeyFromHexString(
				jedis.hget(user.get_id().toString(), SessionManager.H_SESSION_KEY));
		assertEquals("534553535F", queryKey.toString().substring(0, 10));
		
		//Check if local session exist
		String localSessionPrefix = manager.getLocalSessionPrefix();
		UserId actualUserId = UserId.fromString(
				jedis.get(localSessionPrefix+createdKey.toString()));
		assertEquals(user.get_id(), actualUserId);
		
		manager.deregisterSession(session, user);
		
		verify(session);

		assertNull(jedis.get(localSessionPrefix+createdKey.toString()));
		assertNull(jedis.get(createdKey.getRawKey()));
		assertEquals(null, (session.getAttribute(Constant.SESSION_KEY)));
	}
	
	@Test
	public void testRegisterSessionForAI() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setAI(true);
		
		IoSession session = createNiceMock(IoSession.class);
		final HashMap map = new HashMap();
		expect(session.setAttribute(anyObject(), anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[1];
				map.put(getCurrentArguments()[0], getCurrentArguments()[1]);
				return arg;
			}

		}).anyTimes();
		expect(session.removeAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[0];
				map.remove(arg);
				return null;
			}
			
		}).anyTimes();
		expect(session.getAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				return map.get(getCurrentArguments()[0]);
			}
			
		}).anyTimes();
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		
		boolean isAI = manager.isSessionKeyFromAI(createdKey);
		assertEquals(true, isAI);
		
		verify(session);

	}
	
	@Test
	public void testRegisterSessionWithExistingKey() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		Jedis jedis = JedisFactory.getJedis();
		
		SessionManager manager = new SessionManager();
		
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		final HashMap map = new HashMap();
		expect(session.setAttribute(anyObject(), anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[1];
				map.put(getCurrentArguments()[0], getCurrentArguments()[1]);
				return arg;
			}
		}).anyTimes();
		expect(session.removeAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Object arg = getCurrentArguments()[0];
				map.remove(arg);
				return null;
			}
		}).anyTimes();
		expect(session.getAttribute(anyObject())).andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				return map.get(getCurrentArguments()[0]);
			}
			
		}).anyTimes();
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		
		//Put something new in user's session
		jedis.hset(createdKey.getRawKey(), "hello".getBytes(), "world".getBytes());
		
		SessionKey actualKey = manager.registerSession(session, user, createdKey);
		
		assertEquals(createdKey, actualKey);
		
		//Check if the previous data still exists.
		assertArrayEquals("world".getBytes(), jedis.hget(actualKey.getRawKey(), "hello".getBytes()));
		
		assertEquals("localhost:12345", new String(
				jedis.hget(createdKey.toString(), SessionManager.H_MACHINE_KEY)));
		assertEquals(user.get_id(), UserId.fromString(
				jedis.hget(createdKey.toString(), SessionManager.H_USERID_KEY)));
		
		assertEquals("localhost:12345", new String(
				jedis.hget(user.get_id().toString(), SessionManager.H_MACHINE_KEY)));
		SessionKey queryKey = SessionKey.createSessionKeyFromHexString(
				jedis.hget(user.get_id().toString(), SessionManager.H_SESSION_KEY));
		assertEquals("534553535F", queryKey.toString().substring(0, 10));
		
		//Check if local session exist
		String localSessionPrefix = manager.getLocalSessionPrefix();
		UserId actualUserId = UserId.fromString(
				jedis.get(localSessionPrefix+createdKey.toString()));
		assertEquals(user.get_id(), actualUserId);
		
		manager.deregisterSession(session, user);
		
		verify(session);

		assertNull(jedis.get(localSessionPrefix+createdKey.toString()));
		assertNull(jedis.get(createdKey.toString()));
		assertEquals(null, (session.getAttribute(Constant.SESSION_KEY)));
	}
	
	@Test
	public void testDeregisterSession1() {
		SessionManager manager = new SessionManager();
		manager.deregisterSession(null, null);
	}
	
	
	@Test
	public void testDeregisterSession2() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
//		expect(session.getAttribute(anyObject())).andReturn(message.getSessionkey()).times(1);
		expect(session.removeAttribute(anyObject())).andReturn(null).times(1);
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		manager.deregisterSession(session, user);
		
		verify(session);
		
		assertEquals(message.getSessionkey().toString().substring(0, 16), 
				createdKey.toString().substring(0, 16));
		
		Jedis jedis = JedisFactory.getJedis();
		assertNull(jedis.get(message.getSessionkey().getRawKey()));
		assertEquals(null, (session.getAttribute(Constant.SESSION_KEY)));
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testDeregisterSessionWithTimeout() throws Exception {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();
		
		SessionManager manager = new SessionManager();
		UserId userId = new UserId("test-001");
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(userId);
		
		IoSession session = createNiceMock(IoSession.class);
//		expect(session.getAttribute(anyObject())).andReturn(message.getSessionkey()).times(1);
		expect(session.removeAttribute(anyObject())).andReturn(null).times(1);
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		manager.deregisterSession(session, user);
		
		assertEquals(null, (session.getAttribute(Constant.SESSION_KEY)));
		
		String localSessionPrefix = manager.getLocalSessionPrefix();
		
		assertTrue(jedis.exists(createdKey.toString()));
		assertEquals(new Long(1), jedis.ttl(createdKey.toString()));
		assertEquals(new Long(1), jedis.ttl(userId.toString()));
		assertEquals(new Long(1), jedis.ttl(localSessionPrefix+createdKey.toString()));
	
		Thread.currentThread().sleep(2000);

		assertTrue(!jedis.exists(createdKey.toString()));
		assertEquals(null, jedis.get(userId.toString()));
		assertEquals(null, jedis.get(localSessionPrefix+createdKey.toString()));
		
		verify(session);
	
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testDeregisterSessionAndRegister() throws Exception {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "1");
		
		Jedis jedis = JedisFactory.getJedis();
		
		SessionManager manager = new SessionManager();
		UserId userId = new UserId("test-001");
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(userId);
		
		IoSession session = createNiceMock(IoSession.class);
//		expect(session.getAttribute(anyObject())).andReturn(message.getSessionkey()).times(1);
		expect(session.removeAttribute(anyObject())).andReturn(null).times(1);
		
		replay(session);
		
		SessionKey createdKey = manager.registerSession(session, user);
		manager.deregisterSession(session, user);
		
		assertEquals(null, (session.getAttribute(Constant.SESSION_KEY)));
		
		String localSessionPrefix = manager.getLocalSessionPrefix();
		
		assertTrue(jedis.exists(createdKey.toString()));
		assertEquals(new Long(1), jedis.ttl(createdKey.toString()));
		assertEquals(new Long(1), jedis.ttl(userId.toString()));
		assertEquals(new Long(1), jedis.ttl(localSessionPrefix+createdKey.toString()));
	
		//Register, it should cancel the timeout.
		manager.registerSession(session, user, createdKey);
		 
		Thread.currentThread().sleep(2000);
		
		assertTrue(jedis.exists(createdKey.toString()));
		assertTrue(jedis.exists(userId.toString()));
		assertTrue(jedis.exists(localSessionPrefix+createdKey.toString()));
		
		verify(session);
	
	}
	
	@Test
	public void testFindUserMachineId() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_RPC_SERVERID, "localhost:12346");
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_GAME_SERVERID, "localhost:3443");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		replay(session);
		
		SessionKey sessionKey = manager.registerSession(session, user);
		
		verify(session);
		
		String actualMachineId = manager.findUserMachineId(user.get_id());
		assertEquals("localhost:12345", actualMachineId);
		String actualRpcServerId = manager.findUserRpcId(user.get_id());
		assertEquals("localhost:12346", actualRpcServerId);
		String actualGameServerId = manager.findUserGameServerId(user.get_id());
		assertEquals("localhost:3443", actualGameServerId);
		
		manager.deregisterSession(session, user);
		actualMachineId = manager.findUserMachineId(user.get_id());
		assertEquals(null, actualMachineId);
		actualRpcServerId = manager.findUserRpcId(user.get_id());
		assertEquals(null, actualRpcServerId);
		actualGameServerId = manager.findUserGameServerId(user.get_id());
		assertEquals(null, actualGameServerId);

	}
	
	@Test
	public void testFindUserMachineIdBySessionKey() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_RPC_SERVERID, "localhost:12346");
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_GAME_SERVERID, "localhost:3443");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		replay(session);
		
		SessionKey sessionKey = manager.registerSession(session, user);
		
		verify(session);
		
		String actualRpcServerId = manager.findUserRpcId(sessionKey);
		assertEquals("localhost:12346", actualRpcServerId);
		String actualGameServerId = manager.findUserGameServerId(sessionKey);
		assertEquals("localhost:3443", actualGameServerId);
		
		manager.deregisterSession(session, user);
		actualRpcServerId = manager.findUserRpcId(user.get_id());
		assertEquals(null, actualRpcServerId);
		actualGameServerId = manager.findUserGameServerId(user.get_id());
		assertEquals(null, actualGameServerId);

	}
	
	@Test
	public void testFindSessionMachineId() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);
		
		SessionKey sessionKey = manager.registerSession(session, user);
		
		verify(session);
		
		String actualMachineId = manager.findSessionMachineId(sessionKey);
		assertEquals("localhost:12345", actualMachineId);
		
		manager.deregisterSession(session, user);
		actualMachineId = manager.findSessionMachineId(sessionKey);
		assertEquals(null, actualMachineId);
	}
	
	@Test
	public void testFindUserIdBySessionKey() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);
		
		SessionKey sessionKey = manager.registerSession(session, user);
		
		verify(session);
		
		UserId actualId = manager.findUserIdBySessionKey(sessionKey);
		assertEquals(user.get_id(), actualId);
		
		manager.deregisterSession(session, user);
		actualId = manager.findUserIdBySessionKey(sessionKey);
		assertEquals(null, actualId);
	}
	
	@Test
	public void testFindSessionKeyByUserId() {
		GlobalConfig.getInstance().overrideProperty("runtime.local_messageserver", "localhost:12345");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.session_timeout_seconds, "0");
		
		SessionManager manager = new SessionManager();
		SessionRawMessage message = createSessionRawMessage(40, "10.0.0.1", 10000);
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);
		
		SessionKey sessionKey = manager.registerSession(session, user);
		
		verify(session);
		
		SessionKey actualKey = manager.findSessionKeyByUserId(user.get_id());
		assertEquals(sessionKey, actualKey);
		
		manager.deregisterSession(session, user);
		actualKey = manager.findSessionKeyByUserId(user.get_id());
		assertEquals(null, actualKey);
	}
	
	@Test
	public void testCleanOldSessionKey() {
		Random r = new Random();
		SessionManager manager = new SessionManager();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);
		
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		
		verify(session);
		
		List<SessionKey> sessionKeyList = manager.findAllOnlineUsers();
		assertEquals(1, sessionKeyList.size());

	}
	
	@Test
	public void testFindAllOnlineUsers() {
		Random r = new Random();
		SessionManager manager = new SessionManager();
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);

		User user1 = new User();
		user1.set_id(new UserId("001"));
		manager.registerSession(session, user1);
		User user2 = new User();
		user2.set_id(new UserId("002"));
		manager.registerSession(session, user2);
		User user3 = new User();
		user3.set_id(new UserId("003"));
		manager.registerSession(session, user3);
		User user4 = new User();
		user4.set_id(new UserId("004"));
		manager.registerSession(session, user4);
		
		verify(session);
		
		List<SessionKey> sessionKeyList = manager.findAllOnlineUsers();
		assertEquals(4, sessionKeyList.size());

	}
	
	@Test
	public void testFindAllOnlineUsersLimit() {
		Random r = new Random();
		SessionManager manager = new SessionManager();
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);

		for ( int i=0; i<20; i++) {
			User user = new User();
			user.set_id(new UserId(""+i));
			manager.registerSession(session, user);
		}		
		verify(session);
		
		List<SessionKey> sessionKeyList = manager.findAllOnlineUsers(10);
		assertEquals(10, sessionKeyList.size());

	}
		
	@Test
	public void testFindAllLocalOnlineUsers() {
		Random r = new Random();
		SessionManager manager = new SessionManager();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		
		IoSession session = createNiceMock(IoSession.class);
		
		replay(session);
		
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		manager.registerSession(session, user);
		
		verify(session);
		
		Jedis jedis = JedisFactory.getJedis();
		String localSessionPrefix = manager.getLocalSessionPrefix();
		Set<String> sessionSet = jedis.keys(localSessionPrefix.concat("*"));
		assertEquals(4, sessionSet.size());

	}
	
	private SessionRawMessage createSessionRawMessage(int rawByteLength, String host, int port) {
		SessionKey sessionKey = SessionKey.createSessionKeyFromRandomString("SESS_");
		if ( rawByteLength < 0 ) {
			rawByteLength = 0;
		}
		byte[] raw = new byte[rawByteLength];
		Arrays.fill(raw, (byte)0);
		SessionRawMessage rawMessage = new SessionRawMessage();
		rawMessage.setSessionkey(sessionKey);
		rawMessage.setRawMessage(raw);
		return rawMessage;
	}

}
