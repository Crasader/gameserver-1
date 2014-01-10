package com.xinqihd.sns.gameserver.transport.rpc;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class RpcProtocolCodecFilter extends ProtocolCodecFilter {
	private static final ProtocolEncoder encoder = new RpcResponseEncoder();
	private static final ProtocolDecoder decoder = new RpcRequestDecoder();

	public RpcProtocolCodecFilter() {
		super(encoder, decoder);
	}

}
