package com.xinqihd.sns.gameserver.ai;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * Use to encode & decode protocol buffer data.
 * @author wangqi
 *
 */
public class AIProtocolCodecFilter extends ProtocolCodecFilter {

	private final static ProtocolEncoder protocolEncoder = new AIProtobufEncoder();
	
	private final static ProtocolDecoder protocolDecoder = new AIProtobufDecoder();
	
	public AIProtocolCodecFilter() {
		super(protocolEncoder, protocolDecoder);
	}
	
}
