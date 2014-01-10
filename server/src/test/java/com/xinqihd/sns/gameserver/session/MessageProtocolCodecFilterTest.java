package com.xinqihd.sns.gameserver.session;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.DefaultTransportMetadata;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.transport.socket.DefaultSocketSessionConfig;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

public class MessageProtocolCodecFilterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDecode() throws Exception {
		String userName = "test-001";
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		SessionKey sessionKey = SessionKey.createSessionKey("localhost", 12345);
		IoBuffer ioBuffer = IoBuffer.allocate(sessionKey.getRawKey().length + message.payload.getSerializedSize() + 10);
		//session
		ioBuffer.putShort((short)sessionKey.getRawKey().length);
		ioBuffer.put(sessionKey.getRawKey());
		//xinqi
		ioBuffer.putShort((short)(message.payload.getSerializedSize()+6));
		ioBuffer.putShort((short)message.type);
		ioBuffer.putInt(message.index);
		ioBuffer.put(message.payload.toByteArray());
		ioBuffer.flip();
		
		IoSession session = createNiceMock(IoSession.class);
				
		expect(session.getTransportMetadata()).andReturn(new DefaultTransportMetadata("testprovider", "default", 
				false, true, InetSocketAddress.class, DefaultSocketSessionConfig.class, SessionMessage.class)).anyTimes();
				
		ProtocolDecoderOutput out = createNiceMock(ProtocolDecoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				SessionMessage message = (SessionMessage)getCurrentArguments()[0];
				assertEquals("7F00000100003039", message.getSessionkey().toString().substring(0, 16));
				assertEquals(1096, message.getMessage().type);
//				System.out.println(message);
				return null;
			}
		});

		replay(session);
		replay(out);
		
		//Test encoder
		ProtocolDecoder decoder = new SessionMessageDecoder();
		
		decoder.decode(session, ioBuffer, out);
		
		verify(session);
		verify(out);
		
	}

	@Test
	public void testEncode() throws Exception {
		String userName = "test-001";
		
		BceLogin.Builder payload = BceLogin.getDefaultInstance().newBuilderForType();
		payload.setUsername(userName);
		payload.setPassword("000000");
		BceLogin msg = payload.build();
		
		XinqiMessage message = new XinqiMessage();
		message.index = 1;
		message.type = MessageToId.messageToId(msg);
		message.payload = msg;
		
		SessionKey sessionKey = SessionKey.createSessionKey("localhost", 12345);
		
		SessionMessage sessionMessage = new SessionMessage();
		sessionMessage.setSessionkey(sessionKey);
		sessionMessage.setMessage(message);
		
		IoBuffer ioBuffer = IoBuffer.allocate(sessionKey.getRawKey().length + message.payload.getSerializedSize() + 10);
		//session
		ioBuffer.putShort((short)sessionKey.getRawKey().length);
		ioBuffer.put(sessionKey.getRawKey());
		//xinqi
		ioBuffer.putShort((short)(message.payload.getSerializedSize()+6));
		ioBuffer.putShort((short)message.type);
		ioBuffer.putInt(message.index+1);
		ioBuffer.put(message.payload.toByteArray());
		ioBuffer.flip();
		final byte[] expected = ioBuffer.array();
		
		IoSession session = createNiceMock(IoSession.class);
				
		expect(session.getTransportMetadata()).andReturn(new DefaultTransportMetadata("testprovider", "default", 
				false, true, InetSocketAddress.class, DefaultSocketSessionConfig.class, SessionMessage.class)).anyTimes();
				
		ProtocolEncoderOutput out = createNiceMock(ProtocolEncoderOutput.class);
		
		out.write(anyObject());
		
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				IoBuffer ioBuffer = (IoBuffer)getCurrentArguments()[0];
				byte[] actual = ioBuffer.array();
//				System.out.println(Arrays.toString(expected));
//				System.out.println(Arrays.toString(actual));
				assertArrayEquals(expected, actual);
//				System.out.println(message);
				return null;
			}
		});

		replay(session);
		replay(out);
		
		//Test encoder
		ProtocolEncoder encoder = new SessionMessageEncoder();
		
		encoder.encode(session, sessionMessage, out);
		
		verify(session);
		verify(out);
		
	}	
}
