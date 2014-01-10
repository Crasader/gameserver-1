/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package com.xinqihd.sns.gameserver.transport.rpc;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.google.protobuf.MessageLite;

/**
 * Encode the RPC response to client.
 * +---------------+               +------------+----------------+
 * |  MessageLite  |-------------->|   Length   |  Protobuf Data |
 * |  (300 bytes)  |               | 0xFFFFFFFF |  (300 bytes)   |
 * +---------------+               +------------+----------------+
 * 
 */
public class RpcResponseEncoder extends ProtocolEncoderAdapter {
	
	private static final int HEADER_LEN = 4;

	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
	
		MessageLite resp = (MessageLite) message;
		byte[] respBytes = resp.toByteArray();
		IoBuffer buf = IoBuffer.allocate(HEADER_LEN+respBytes.length);
		buf.putInt(respBytes.length);
		buf.put(respBytes);
		buf.flip();
		out.write(buf);

	}
}
