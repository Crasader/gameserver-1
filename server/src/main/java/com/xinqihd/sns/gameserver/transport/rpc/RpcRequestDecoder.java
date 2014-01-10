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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.xinqihd.sns.gameserver.proto.Rpc.RpcMessage;

/**
 * Decode the RCP message into Protobuf Message.
 * +---------------+               +------------+----------------+
 * |  MessageLite  |-------------->|   Length   |  Protobuf Data |
 * |  (300 bytes)  |               | 0xFFFFFFFF |  (300 bytes)   |
 * +---------------+               +------------+----------------+
 */
public class RpcRequestDecoder extends CumulativeProtocolDecoder {
	
	/** max request size is 1MB */
	public static final int MAX_LENGTH = 0x100000;
	
	private static final int HEADER_LENGTH = 4;
	
	private static final Log log = LogFactory.getLog(RpcRequestDecoder.class);

	/**
	 * Decode the RpcRequest
	 */
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		
		if (in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH)) {
			RpcMessage.Builder rpcRequestBuilder = RpcMessage.newBuilder();
			int length = in.getInt();
			byte[] bytes = new byte[length];
			in.get(bytes);
			rpcRequestBuilder.mergeFrom(bytes);
			
			out.write(rpcRequestBuilder.build());
			return true;
		} else {
			// not enough data available
			return false;
		}
	}
}