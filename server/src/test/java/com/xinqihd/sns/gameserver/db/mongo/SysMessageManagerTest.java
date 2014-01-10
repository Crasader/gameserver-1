package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBseSysMessage.BseSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class SysMessageManagerTest {
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSendClientInfoMessageUserString() throws Exception {
		User user = prepareUser(userName);
		ArrayList list = new ArrayList();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(user.getUsername()));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		String messageKey = "shop.success";
		
		SysMessageManager.getInstance().sendClientInfoMessage(
				user, messageKey, Type.NORMAL);
		
		Thread.sleep(500);
		
		assertEquals(1, list.size());
		XinqiMessage xinqi = (XinqiMessage)list.get(0);
		BseSysMessage message = (BseSysMessage)xinqi.payload;
		assertEquals("恭喜您，商品购买成功！", message.getMessage());
	}

	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
