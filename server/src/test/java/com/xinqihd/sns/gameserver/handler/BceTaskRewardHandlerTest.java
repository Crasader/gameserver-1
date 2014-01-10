package com.xinqihd.sns.gameserver.handler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.proto.XinqiBceRegister.BceRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBceTaskReward.BceTaskReward;
import com.xinqihd.sns.gameserver.proto.XinqiBseDelTask.BseDelTask;
import com.xinqihd.sns.gameserver.proto.XinqiBseRegister;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleInfo.BseRoleInfo;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BceTaskRewardHandlerTest {

	String userName = "00001";
	
	@Before
	public void setUp() throws Exception {
		UserManager.getInstance().removeUser(userName);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTaskReward() throws Exception {
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		ArrayList msgList = new ArrayList();
		
		BceLoginHandler loginHandler = BceLoginHandler.getInstance();
		
		IoSession session = TestUtil.createIoSession(msgList);
		//Login the user
		registerUser(userName);
		loginHandler.messageProcess(session, message, null);
		
		User user = (User)session.getAttribute(Constant.USER_KEY);
		
		msgList.clear();
		
		//Finish the task first.
		TaskManager.getInstance().finishTask(user, "149");
		//Take a task reward:
		//taskid	name	userlevel	sequence	step	level	type	action	script
		//149	登陆有奖相送	1	6	1	1	TASK_ACTIVITY	登陆	script.task.Login
		BceTaskReward reward = BceTaskReward.newBuilder().setTaskID(149).setChoose(0).build();
		BceTaskRewardHandler rewardHandler = BceTaskRewardHandler.getInstance();
		
		message.payload = reward;
		rewardHandler.messageProcess(session, message, user.getSessionKey());
		
		verify(session);
		Thread.currentThread().sleep(200);
		assertTrue("Should contain BseDelTask and/or BseAddTask", msgList.size()>2);
		Set<String> classes = new HashSet<String>();
		for ( int i=0; i<msgList.size(); i++ ) {
			XinqiMessage response = (XinqiMessage)msgList.get(i);
			classes.add(response.payload.getClass().getName());
		}
		Thread.currentThread().sleep(200);
//		assertTrue(classes.contains(BseAddProp.class.getName()));
		assertTrue(classes.contains(BseRoleInfo.class.getName()));
		assertTrue(classes.contains(BseDelTask.class.getName()));
	}

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
				assertEquals(LoginManager.RegisterErrorCode.SUCCESS.ordinal(), register.getCode());
				return null;
			}
		});
		
		replay(session);
		
		handler.messageProcess(session, message, null);
		
		verify(session);
	}
}
