package com.xinqihd.sns.gameserver.transport.http;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class HttpDecoderTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDoDecode() throws Exception {
		String request = "GET /equipment_config.lua HTTP/1.1\r\nHost: localhost\r\n\r\n";
		final HttpMessage expect = new HttpMessage();
		expect.setRequestUri("/equipment_config.lua");
		
		HttpDecoder decoder = new HttpDecoder();
		
		IoSession session = createMock(IoSession.class);
		//Set mock behavior
		expect(session.getAttribute(eq("HttpDecoder.Session"))).andReturn(null).anyTimes();
		expect(session.setAttribute(eq("HttpDecoder.Session"), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		ProtocolDecoderOutput out = createNiceMock(ProtocolDecoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				HttpMessage message = (HttpMessage)getCurrentArguments()[0];
				assertTrue(compareHttpMessage(expect, message));
				assertFalse(message.isHead());
				return null;
			}
		});
		replay(out);
		
		IoBuffer in = IoBuffer.wrap(request.getBytes());
		
		int l = in.remaining();
		boolean result = true;
		while ( result ) {
			result = decoder.doDecode(session, in, out);
		}
		
		verify(session);
		verify(out);
		
		assertTrue(true);
	}
	
	@Test
	public void testDoDecodeWithLF() throws Exception {
		String request = "GET /equipment_config.lua HTTP/1.1\nHost: localhost\n\n";
		final HttpMessage expect = new HttpMessage();
		expect.setRequestUri("/equipment_config.lua");
		
		HttpDecoder decoder = new HttpDecoder();
		
		IoSession session = createMock(IoSession.class);
		//Set mock behavior
		expect(session.getAttribute(eq("HttpDecoder.Session"))).andReturn(null).anyTimes();
		expect(session.setAttribute(eq("HttpDecoder.Session"), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		ProtocolDecoderOutput out = createNiceMock(ProtocolDecoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				HttpMessage message = (HttpMessage)getCurrentArguments()[0];
				assertTrue(compareHttpMessage(expect, message));
				assertFalse(message.isHead());
				return null;
			}
		});
		replay(out);
		
		IoBuffer in = IoBuffer.wrap(request.getBytes());
		
		int l = in.remaining();
		boolean result = true;
		while ( result ) {
			result = decoder.doDecode(session, in, out);
		}
		
		verify(session);
		verify(out);
		
		assertTrue(true);
	}
	
	@Test
	public void testDoDecodeWithHEAD() throws Exception {
		String request = "HEAD /equipment_config.lua HTTP/1.1\nHost: localhost\n\n";
		final HttpMessage expect = new HttpMessage();
		expect.setRequestUri("/equipment_config.lua");
		expect.setHead(true);
		
		HttpDecoder decoder = new HttpDecoder();
		
		IoSession session = createMock(IoSession.class);
		//Set mock behavior
		expect(session.getAttribute(eq("HttpDecoder.Session"))).andReturn(null).anyTimes();
		expect(session.setAttribute(eq("HttpDecoder.Session"), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		ProtocolDecoderOutput out = createNiceMock(ProtocolDecoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				HttpMessage message = (HttpMessage)getCurrentArguments()[0];
				assertTrue(compareHttpMessage(expect, message));
				assertTrue(message.isHead());
				return null;
			}
		});
		replay(out);
		
		IoBuffer in = IoBuffer.wrap(request.getBytes());
		
		int l = in.remaining();
		boolean result = true;
		while ( result ) {
			result = decoder.doDecode(session, in, out);
		}
		
		verify(session);
		verify(out);
		
		assertTrue(true);
	}
	
	@Test
	public void testDoDecodeWithPOST() throws Exception {
		String request = 
				"POST /cmcccharge HTTP/1.1\r\n" +
				"Host: localhost\r\n" +
				"\r\n"+
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"+
				"<request>\r\n"+
				"	<hRet>0</hRet>\r\n"+
				"	<status>1800</status>\r\n"+
				"	<transIDO>12345678901234567</transIDO>\r\n"+
				"	<versionId>100</versionId>\r\n"+
				"	<userId>12345678</userId>\r\n"+
				"	<cpServiceId>120123002000</cpServiceId>\r\n"+
				"	<consumeCode>120123002001</consumeCode>\r\n"+
				"	<cpParam>0000000000000000</cpParam>\r\n"+
				"</request>\r\n"+
				"\r\n";
		final HttpMessage expect = new HttpMessage();
		expect.setRequestUri("/cmcccharge");
		
		HttpDecoder decoder = new HttpDecoder();
		
		IoSession session = TestUtil.createIoSession();
		//Set mock behavior
		expect(session.getAttribute(eq("HttpDecoder.Session"))).andReturn(null).anyTimes();
		expect(session.setAttribute(eq("HttpDecoder.Session"), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		ProtocolDecoderOutput out = createNiceMock(ProtocolDecoderOutput.class);
		out.write(anyObject());
		expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() {
				HttpMessage message = (HttpMessage)getCurrentArguments()[0];
				assertTrue(compareHttpMessage(expect, message));
				assertFalse(message.isHead());
				return null;
			}
		});
		replay(out);
		
		IoBuffer in = IoBuffer.wrap(request.getBytes());
		
		int l = in.remaining();
		boolean result = true;
		while ( result ) {
			result = decoder.doDecode(session, in, out);
		}
		
		verify(session);
		verify(out);
		
		assertTrue(true);
	}
	
	private static boolean compareHttpMessage(HttpMessage expect, Object argument) {
		if ( argument == null ) 
			return false;
		if ( argument instanceof HttpMessage ) {
			HttpMessage actual = (HttpMessage)argument;
			return expect.getRequestUri().equals(actual.getRequestUri()) &&
				(expect.isHead() == actual.isHead());
		}
		return false;
	}
	
	// ------------------------------------ EasyMock argument matcher
	
	/**
	 * Add a new EasyMock argument matcher.
	 * @param actual
	 * @return
	 */
	public static <T extends Object> T eqHttpMessage(Object expect) {
		reportMatcher(new HttpMessageMatcher(expect));
		return null;
	}

	static class HttpMessageMatcher implements IArgumentMatcher {
		
		HttpMessage expect = null;
		
		public HttpMessageMatcher(Object expect) {
			this.expect = (HttpMessage)expect;
		}

		@Override
		public boolean matches(Object argument) {
			return compareHttpMessage(expect, argument);
		}

		@Override
		public void appendTo(StringBuffer buffer) {
			buffer.append(expect);
			
		}
		
	}
	
}
