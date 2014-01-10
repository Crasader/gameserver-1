package com.xinqihd.sns.gameserver.transport;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Use to encode & decode protocol buffer data.
 * @author wangqi
 *
 */
public class GameProtocolCodecFilter extends ProtocolCodecFilter {

	private final static ProtocolEncoder protocolEncoder = new ProtobufEncoder();
	
	private final static ProtocolDecoder protocolDecoder = new ProtobufDecoder();
	
	public GameProtocolCodecFilter() {
		super(protocolEncoder, protocolDecoder);
	}
	
}
