package com.xinqihd.sns.gameserver.chat;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class ChatManagerTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.chat_word_file, "../deploy/data/word.txt");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testProcessChatAsyn() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		chatManager.processChatAsyn(user1, builder.build());
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(1, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
	}
	
	@Test
	public void testProcessChatAsynFreq() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		BceChat chat = builder.build();
		boolean send = false;
		send = chatManager.processChatAsyn(user1, chat);
		assertTrue(send);
		send = chatManager.processChatAsyn(user1, chat);
		assertFalse(send);
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(1, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
	}

	@Test
	public void testProcessChatAsynFreqVIP() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		user1.setIsvip(true);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		BceChat chat = builder.build();
		boolean send = false;
		send = chatManager.processChatAsyn(user1, chat);
		assertTrue(send);
		send = chatManager.processChatAsyn(user1, chat);
		assertTrue(send);
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(2, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
	}
	
	@Test
	public void testProcessChatAsynFreqPrivate() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		user1.setIsvip(true);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setUsrId(user2.get_id().toString());
		builder.setMsgType(ChatType.ChatPrivate.ordinal());
		
		BceChat chat = builder.build();
		boolean send = false;
		send = chatManager.processChatAsyn(user1, chat);
		assertTrue(send);
		send = chatManager.processChatAsyn(user1, chat);
		assertTrue(send);
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
//		assertEquals(2, list2.size());
//		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
//		assertEquals(BseChat.class, xinqi.payload.getClass());
	}
	
	@Test
	public void testProcessChatToAllAsyn() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		chatManager.processChatToWorldAsyn(user1, "hello world!");
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(1, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
		System.out.println(xinqi.payload);
	}
	
	@Test
	public void testProcessChatToAllAsynFromGameAdmin() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		chatManager.processChatToWorldAsyn(null, "hello world!");
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(1, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
		System.out.println(xinqi.payload);
	}
	
	public void testProcessChatAsynPerformance() throws Exception {
		final ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		final User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		final User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		final BceChat chat = builder.build();
		
		int times = 10000;
		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				chatManager.processChatAsyn(user1, chat);
			}
		}, "processChatAsyn", times, 1);
		
//		while ( Stat.getInstance().chatBuffered > 0 ) {
			System.out.println("chatBuffer: " + Stat.getInstance().chatBuffered);
//		}
	}

	@Test
	public void testProcessChat() throws Exception {
		ChatManager chatManager = ChatManager.getInstance();
		ArrayList list1 = new ArrayList();
		User user1 = prepareUser("test-001", list1);
		ArrayList list2 = new ArrayList();
		User user2 = prepareUser("test-001", list2);
		
		BceChat.Builder builder = BceChat.newBuilder();
		builder.setMsgContent("hello world");
		builder.setMsgType(ChatType.ChatWorld.ordinal());
		
		chatManager.processChat(user1, builder.build(), null);
		
		Thread.sleep(200);
		
		System.out.println(list2);
		
		assertEquals(1, list2.size());
		XinqiMessage xinqi = (XinqiMessage)list2.get(0);
		assertEquals(BseChat.class, xinqi.payload.getClass());
	}
	
	@Test
	public void testFilterWord() {
		ChatManager manager = ChatManager.getInstance();
		String userWord = "我操共产党";
		boolean result = manager.containBadWord(userWord);
		assertTrue(result);
		String newWord = manager.filterWord(userWord);
		assertEquals("**", newWord);
		
		userWord = "正常的词语";
		result = manager.containBadWord(userWord);
		assertFalse(result);
		newWord = manager.filterWord(userWord);
		assertEquals("正常的词语", newWord);
	}

	private User prepareUser(String userName, ArrayList list) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		IoSession session = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session, user, null);
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
