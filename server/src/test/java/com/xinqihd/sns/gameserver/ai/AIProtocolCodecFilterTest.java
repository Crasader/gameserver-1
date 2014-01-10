package com.xinqihd.sns.gameserver.ai;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionMessage;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

public class AIProtocolCodecFilterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProtocol() throws Exception {
		BceLogin.Builder builder = BceLogin.newBuilder();
		builder.setUsername("test-001");
		builder.setPassword("password");
		BceLogin loginMsg = builder.build();
		
		XinqiMessage xinqiMsg = new XinqiMessage();
		xinqiMsg.index = 0;
		xinqiMsg.type = MessageToId.messageToId(loginMsg);
		xinqiMsg.payload = loginMsg;
		
		SessionKey sessionkey = SessionKey.createSessionKeyFromRandomString();
		
		SessionAIMessage aiMessage = new SessionAIMessage();
		aiMessage.setSessionKey(sessionkey);
		aiMessage.setMessage(xinqiMsg);
		
		SessionAIMessage response = encodeAndDecode(aiMessage);
		
		assertEquals(sessionkey, response.getSessionKey());
		XinqiMessage actual = response.getMessage();
		assertEquals(xinqiMsg.type, actual.type);
		assertEquals(xinqiMsg.payload, actual.payload);
	}
	
	private SessionAIMessage encodeAndDecode(SessionAIMessage sessionMessage) throws Exception {
		
		AIProtobufEncoder encoder = new AIProtobufEncoder();
		AIProtobufDecoder decoder = new AIProtobufDecoder();
		final ArrayList<Object> results = new ArrayList<Object>();
		
		IoSession session = createNiceMock(IoSession.class);
		expect(session.getTransportMetadata()).andReturn(
				new DefaultTransportMetadata("testprovider", "default", 
						false, true, InetSocketAddress.class, DefaultSocketSessionConfig.class, 
						SessionMessage.class)).anyTimes();
		
		ProtocolEncoderOutput out = createNiceMock(ProtocolEncoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				results.add(getCurrentArguments()[0]);
				return null;
			}
		}).anyTimes();
		
		replay(session);
		replay(out);
		
		encoder.encode(session, sessionMessage, out);
		
		verify(session);
		verify(out);
		
		assertTrue(results.get(0) instanceof IoBuffer );
		
		IoBuffer buffer = (IoBuffer)results.get(0);
		results.remove(0);
		
		ProtocolDecoderOutput deout = createNiceMock(ProtocolDecoderOutput.class);
		deout.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				results.add(getCurrentArguments()[0]);
				return null;
			}
		}).times(1);
		replay(deout);
		
		decoder.decode(session, buffer, deout);
		
		verify(deout);
		
		SessionAIMessage decodeMsg = (SessionAIMessage)results.get(0);
		return decodeMsg;
	}
}
