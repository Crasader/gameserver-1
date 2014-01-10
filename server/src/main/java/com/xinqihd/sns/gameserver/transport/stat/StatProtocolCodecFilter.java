package com.xinqihd.sns.gameserver.transport.stat;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

/**
 * Use to encode & decode protocol buffer data.
 * @author wangqi
 *
 */
public class StatProtocolCodecFilter extends ProtocolCodecFilter {

	private final static ProtobufEncoder protocolEncoder = new ProtobufEncoder();
	
	private final static ProtobufDecoder protocolDecoder = new ProtobufDecoder();
	
	public StatProtocolCodecFilter() {
		super(protocolEncoder, protocolDecoder);
	}
	
}
