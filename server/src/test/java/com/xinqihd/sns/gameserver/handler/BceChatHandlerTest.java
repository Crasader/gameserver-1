package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Pipeline;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.session.MessageQueue;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceChatHandlerTest {

	@Before
	public void setUp() throws Exception {
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testChatErrorType1() throws Exception {
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
		payload.setMsgType(Integer.MAX_VALUE);
		payload.setMsgContent("hello world");
		
		BceChat msg = payload.build();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.index = 1;
		xinqi.type = MessageToId.messageToId(msg);
		xinqi.payload = msg;
		
		GameContext gameContext = GameContext.getTestInstance();
		BceChatHandler handler = BceChatHandler.getInstance();
		
		IoSession session = TestUtil.createIoSession();

		replay(session);
		
		GameContext.getInstance().registerUserSession(session, user, null);
		handler.messageProcess(session, xinqi, user.getSessionKey());
		
		verify(session);
	}
	
	@Test
	public void testChatErrorType2() throws Exception {
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
		payload.setMsgType(-1);
		payload.setMsgContent("hello world");
		
		BceChat msg = payload.build();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.index = 1;
		xinqi.type = MessageToId.messageToId(msg);
		xinqi.payload = msg;
		
		GameContext gameContext = GameContext.getTestInstance();
		BceChatHandler handler = BceChatHandler.getInstance();
		
		IoSession session = TestUtil.createIoSession();
		replay(session);
		
		GameContext.getInstance().registerUserSession(session, user, null);
		handler.messageProcess(session, xinqi, user.getSessionKey());
		
		verify(session);
	}
	
	@Test
	public void testChatEmptyMessage() throws Exception {
		String username = "test-001";
		User user = new User();
		user.set_id(new UserId(username));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
		payload.setMsgType(ChatType.ChatWorld.ordinal());
		payload.setMsgContent("");
		
		BceChat msg = payload.build();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.index = 1;
		xinqi.type = MessageToId.messageToId(msg);
		xinqi.payload = msg;
		
		GameContext gameContext = GameContext.getTestInstance();
		BceChatHandler handler = BceChatHandler.getInstance();
		
		IoSession session = TestUtil.createIoSession();
		replay(session);
		
		GameContext.getInstance().registerUserSession(session, user, null);
		
		handler.messageProcess(session, xinqi, user.getSessionKey());
		
		verify(session);
	}

	@Test
	public void testChatWorld() {
		try {
			final String message = "你好，聊天消息";
			
			int sessionCount = 1;
			UserId userId = new UserId("testChatWorld");
			User user = new User();
			user.set_id(userId);
			registerFakeSession(sessionCount, user);
			
			BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
			payload.setMsgType(ChatType.ChatWorld.ordinal());
			payload.setMsgContent(message);
			
			BceChat msg = payload.build();
			
			XinqiMessage xinqi = new XinqiMessage();
			xinqi.index = 1;
			xinqi.type = MessageToId.messageToId(msg);
			xinqi.payload = msg;
			
			GameContext gameContext = GameContext.getTestInstance();
			BceChatHandler handler = BceChatHandler.getInstance();
			
			IoSession session = TestUtil.createIoSession();
			replay(session);
			GameContext.getInstance().registerUserSession(session, user, null);
			
			MessageQueue messageQueue = createMock(MessageQueue.class);
			messageQueue.sessionWrite(anyObject(SessionKey.class), anyObject());
			expectLastCall().andAnswer(new IAnswer<WriteFuture>(){
				@Override
				public WriteFuture answer() throws Throwable {
					SessionKey sessionKey = (SessionKey)getCurrentArguments()[0];
					XinqiMessage response = (XinqiMessage)getCurrentArguments()[1];
					BseChat chatRsp = (BseChat)response.payload;
					assertEquals(message, chatRsp.getMsgContent());
					assertEquals(ChatType.ChatWorld.ordinal(), chatRsp.getMsgType());
					System.out.println(chatRsp);
					return null;
				}
			}).times(sessionCount);
			TestUtil.setPrivateFieldValue("messageQueue", gameContext, messageQueue);

			replay(messageQueue);
			
			handler.messageProcess(session, xinqi, user.getSessionKey());
			
			Thread.sleep(200);
			
			verify(session);
			verify(messageQueue);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChatPrivate() throws Exception {
		final String message = "你好，聊天消息";
		
		int sessionCount = 1;
		UserId userId = new UserId("test-1234");
		User user = new User();
		user.set_id(userId);
		registerFakeSession(sessionCount, user);
		
		BceChat.Builder payload = BceChat.getDefaultInstance().newBuilderForType();
		payload.setMsgType(ChatType.ChatPrivate.ordinal());
		payload.setMsgContent(message);
		payload.setUsrId(userId.toString());
		
		BceChat msg = payload.build();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.index = 1;
		xinqi.type = MessageToId.messageToId(msg);
		xinqi.payload = msg;
		
		GameContext gameContext = GameContext.getTestInstance();
		BceChatHandler handler = BceChatHandler.getInstance();
		
		IoSession session = TestUtil.createIoSession();
		replay(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		MessageQueue messageQueue = createMock(MessageQueue.class);
		messageQueue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<WriteFuture>(){
			@Override
			public WriteFuture answer() throws Throwable {
				SessionKey sessionKey = (SessionKey)getCurrentArguments()[0];
				XinqiMessage response = (XinqiMessage)getCurrentArguments()[1];
				BseChat chatRsp = (BseChat)response.payload;
				assertEquals(message, chatRsp.getMsgContent());
				assertEquals(ChatType.ChatPrivate.ordinal(), chatRsp.getMsgType());
				return null;
			}
		}).times(1);
		TestUtil.setPrivateFieldValue("messageQueue", gameContext, messageQueue);

		replay(messageQueue);
		
		handler.messageProcess(session, xinqi, user.getSessionKey());
		Thread.sleep(200);
		
		verify(session);
		verify(messageQueue);
		
	}

	private void registerFakeSession(int count, User user) {
		//Clean all sessions
		Jedis jedis = JedisFactory.getJedis();
		Pipeline pipeline = jedis.pipelined();
		Set<byte[]> strs = jedis.keys("*".getBytes());
		for ( byte[] key : strs ) {
			pipeline.del(key);
		}
		pipeline.sync();
		
		UserId userId = user.get_id();
		SessionKey sessionKey = SessionKey.createSessionKeyFromRandomString();
		user.setSessionKey(sessionKey);
		
		//Store it with machineid to redis
		jedis = JedisFactory.getJedis();
		pipeline = jedis.pipelined();
		for ( int i=0; i<count; i++ ) {
			pipeline.hset(sessionKey.toString(), SessionManager.H_MACHINE_KEY, "localhost:10000");
			pipeline.hset(userId.toString(), SessionManager.H_MACHINE_KEY, "localhost:10000");
			pipeline.hset(userId.toString(), SessionManager.H_SESSION_KEY, sessionKey.toString());
		}
		pipeline.sync();
	}
}
