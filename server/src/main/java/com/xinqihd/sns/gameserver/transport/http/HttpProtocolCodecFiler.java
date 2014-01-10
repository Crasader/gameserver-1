package com.xinqihd.sns.gameserver.transport.http;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

/**
 * Encapsulate the HttpEncoder and HttpDecoder
 * @author wangqi
 *
 */
public class HttpProtocolCodecFiler extends ProtocolCodecFilter {
	
	private static final HttpEncoder httpEncoder = new HttpEncoder();
	
	private static final HttpDecoder httpDecoder = new HttpDecoder();

	public HttpProtocolCodecFiler() {
		super(httpEncoder, httpDecoder);
	}
}
