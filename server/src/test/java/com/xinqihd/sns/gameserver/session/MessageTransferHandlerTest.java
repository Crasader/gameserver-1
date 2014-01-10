package com.xinqihd.sns.gameserver.session;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.util.TestUtil;

public class MessageTransferHandlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNullUserSessionMessageReceivedIoSessionObject() throws Exception {
		SessionRawMessage message = createSessionRawMessage(40);
		
		MessageTransferHandler handler = new MessageTransferHandler();

		MessageQueue messageQueue = createMock(MessageQueue.class);
		expect(messageQueue.findSession(anyObject(SessionKey.class))).andReturn(null);
//		messageQueue.sessionWrite(anyObject(IoSession.class), anyObject());
		TestUtil.setPrivateFieldValue("messageQueue", handler, messageQueue);
		replay(messageQueue);
		
		handler.messageReceived(null, message);
		
		verify(messageQueue);
//		fail("Not yet implemented");
	}
	
	@Test
	public void testMessageReceivedIoSessionObjectUnConnected() throws Exception {
		MessageTransferHandler handler = new MessageTransferHandler();

		final IoSession userSession = createMock(IoSession.class);
		expect(userSession.isConnected()).andReturn(Boolean.FALSE);
		
		final MessageQueue messageQueue = createMock(MessageQueue.class);
		
		expect(messageQueue.findSession(anyObject(SessionKey.class))).andReturn(userSession);
				
		TestUtil.setPrivateFieldValue("messageQueue", handler, messageQueue);
		
		replay(messageQueue);
		replay(userSession);
		
		SessionRawMessage message = createSessionRawMessage(40);
		handler.messageReceived(null, message);
		
		verify(messageQueue);
		verify(userSession);
		
	}
	
	@Test
	public void testMessageReceivedIoSessionObject() throws Exception {
		MessageTransferHandler handler = new MessageTransferHandler();

		final IoSession userSession = createMock(IoSession.class);
		expect(userSession.isConnected()).andReturn(Boolean.TRUE);
		
		final MessageQueue messageQueue = createMock(MessageQueue.class);
		
		expect(messageQueue.findSession(anyObject(SessionKey.class))).andReturn(userSession);
		
		messageQueue.sessionWrite(anyObject(SessionKey.class), anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				SessionKey tmpSessionKey = (SessionKey)getCurrentArguments()[0];
				IoBuffer tmpBuf = (IoBuffer)getCurrentArguments()[1];
				assertEquals("7F00000100003039", tmpSessionKey.toString().substring(0, 16));
				assertEquals(40, tmpBuf.array().length);
//				System.out.println(msg);
				return null;
			}
		});
		
		TestUtil.setPrivateFieldValue("messageQueue", handler, messageQueue);
		
		replay(messageQueue);
		replay(userSession);
		
		SessionRawMessage message = createSessionRawMessage(40);
		handler.messageReceived(null, message);
		
		verify(messageQueue);
		verify(userSession);
		
	}
	
	private SessionRawMessage createSessionRawMessage(int rawByteLength) {
		SessionKey sessionKey = SessionKey.createSessionKey("localhost", 12345);
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
