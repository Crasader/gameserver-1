package com.xinqihd.sns.gameserver.session;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.Arrays;

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

import com.google.protobuf.InvalidProtocolBufferException;
import com.xinqihd.sns.gameserver.proto.XinqiBceLogin.BceLogin;
import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

public class RawMessageProtocolCodecFilterTest {

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
		//sessionKeyLen(2) + sessionKey + rawLength(4) + protoBufLen(2) + type(2) + index(4) + protoBufdata
		IoBuffer ioBuffer = IoBuffer.allocate(2 + sessionKey.getRawKey().length + 4 + 2 + 2 + 4 + message.payload.getSerializedSize());
		//session
		ioBuffer.putShort((short)sessionKey.getRawKey().length);
		ioBuffer.put(sessionKey.getRawKey());
		//Raw header
		ioBuffer.putInt(message.payload.getSerializedSize()+8);
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
				SessionRawMessage message = (SessionRawMessage)getCurrentArguments()[0];
				assertEquals("7F00000100003039", 
						message.getSessionkey().toString().substring(0, 16));
				System.out.println(Arrays.toString(message.getRawMessage()));
				byte[] protoBytes = new byte[message.getRawMessage().length-8];
				System.arraycopy(message.getRawMessage(), 8, protoBytes, 0, protoBytes.length);
				try {
					BceLogin login = BceLogin.parseFrom(protoBytes);
					assertEquals("test-001", login.getUsername());
					assertEquals("000000", login.getPassword());
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
				return null;
			}
		});

		replay(session);
		replay(out);
		
		//Test encoder
		ProtocolDecoder decoder = new SessionMessageRawDecoder();
		
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
		
		//sessionKeyLen(2) + sessionKey + rawLength(4) + protoBufLen(2) + type(2) + index(4) + protoBufdata
		IoBuffer ioBuffer = IoBuffer.allocate(2 + sessionKey.getRawKey().length + 4 + 2 + 2 + 4 + message.payload.getSerializedSize());
		//session
		ioBuffer.putShort((short)sessionKey.getRawKey().length);
		ioBuffer.put(sessionKey.getRawKey());
		//Raw
		ioBuffer.putInt(message.payload.getSerializedSize()+8);
		//xinqi
		IoBuffer xinqiBuf = IoBuffer.allocate(message.payload.getSerializedSize()+8);
		xinqiBuf.putShort((short)(message.payload.getSerializedSize()+6));
		xinqiBuf.putShort((short)message.type);
		xinqiBuf.putInt(message.index+1);
		xinqiBuf.put(message.payload.toByteArray());
		xinqiBuf.flip();
		ioBuffer.put(xinqiBuf);
		
		final byte[] expected = ioBuffer.array();
		
		SessionRawMessage sessionMessage = new SessionRawMessage();
		sessionMessage.setSessionkey(sessionKey);
		sessionMessage.setRawMessage(xinqiBuf.array());
		
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
		ProtocolEncoder encoder = new SessionMessageRawEncoder();
		
		encoder.encode(session, sessionMessage, out);
		
		verify(session);
		verify(out);
		
	}	
}
