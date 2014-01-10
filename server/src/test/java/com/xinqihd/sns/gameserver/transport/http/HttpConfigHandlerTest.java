package com.xinqihd.sns.gameserver.transport.http;

import static org.easymock.EasyMock.*;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;

public class HttpConfigHandlerTest extends AbstractTest {
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMessageReceivedIoSessionObject() throws Exception {
		HttpConfigHandler handler = new HttpConfigHandler();
		
//		MinaMessageQueue messageQueue = (MinaMessageQueue)MinaMessageQueue.getInstance();
//		LinkedBlockingQueue internalMessageQueue = (LinkedBlockingQueue)createMock(LinkedBlockingQueue.class);
//		internalMessageQueue.put(anyObject());
//		expectLastCall().anyTimes();
//		Object oldMessageQueue = TestUtil.getPrivateFieldValue("messageQueue", messageQueue);
//		TestUtil.setPrivateFieldValue("messageQueue", messageQueue, internalMessageQueue);
		
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.isConnected()).andReturn(Boolean.TRUE).anyTimes();
		replay(session);
		
		HttpMessage message = new HttpMessage();
		message.setRequestUri("/equipment_config.lua");
		try {
			handler.messageReceived(session, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
