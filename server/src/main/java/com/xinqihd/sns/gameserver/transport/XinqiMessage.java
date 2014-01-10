package com.xinqihd.sns.gameserver.transport;

import org.apache.mina.core.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

/**
 * The message encapsulate the google protobuf data and length and type.
 * 
 * @author wangqi
 *
 */
public class XinqiMessage {

	private static final Logger logger = LoggerFactory.getLogger(XinqiMessage.class);
	
	public int index;
	public int type;
	public MessageLite payload;
	
	/**
	 * Encode this message to byte array.
	 * @return
	 */
	public byte[] toByteArray() {
		IoBuffer buf = ProtobufEncoder.encodeXinqiMessage(this);
		if ( buf != null ) {
			return buf.array();
		} else {
			return null;
		}
	}
	
	/**
	 * Create the XinqiMessage from byte array.
	 * 
	 * @param bytes
	 * @return
	 */
	public final static XinqiMessage fromByteArray(byte[] bytes) {
		IoBuffer buf = IoBuffer.wrap(bytes);
		XinqiMessage message;
		try {
			message = ProtobufDecoder.decodeXinqiMessage(buf);
			return message;
		} catch (InvalidProtocolBufferException e) {
			logger.warn("InvalidProtocolBufferException", e.getMessage());
			logger.debug(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Print the hex data to debug. 
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("XinqiMessage [index=");
		builder.append(index);
		builder.append(", type=");
		builder.append(type);
		builder.append(", payload=");
		if ( payload != null ) {
			builder.append(payload.getClass().getName());
		} else {
			builder.append("null");
		}
		builder.append("]");
		return builder.toString();
	}
		
}
