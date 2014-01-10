package com.xinqihd.sns.gameserver.session;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Use to encode & decode protocol buffer data.
 * @author wangqi
 *
 */
public class MessageProtocolCodecFilter extends ProtocolCodecFilter {

	private final static ProtocolEncoder protocolEncoder = new SessionMessageEncoder();
	
	private final static ProtocolDecoder protocolDecoder = new SessionMessageDecoder();
	
	public MessageProtocolCodecFilter() {
		super(protocolEncoder, protocolDecoder);
	}
	
}
