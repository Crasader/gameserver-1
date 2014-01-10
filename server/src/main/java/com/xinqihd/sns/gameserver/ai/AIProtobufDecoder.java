package com.xinqihd.sns.gameserver.ai;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.xinqihd.sns.gameserver.proto.XinqiAI.AIMessage;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Decode the transport message structure like that:
 * <pre>
 * BEFORE DECODE (300 bytes)       AFTER DECODE (308 bytes)
 * +---------------+               +------------+----------------+
 * | Protobuf Data |-------------->|   Length   |  AI Message    |
 * |  (300 bytes)  |               | 0xFFFFFFFF |  (300 bytes)   |
 * +---------------+               +------------+----------------+
 * </pre> 
 * @author wangqi
 *
 */
public class AIProtobufDecoder extends CumulativeProtocolDecoder {
	
	private static final int MAX_LENGTH = Integer.MAX_VALUE;
	
	private static final int HEADER_LENGTH = 4;
	
	private static final Logger logger = LoggerFactory.getLogger(AIProtobufDecoder.class);
		
	/**
	 * Decode the message.
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// Make sure all the header bytes are ready.
		if ( !in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH) ) {
	    return false;
		}
		
		byte[] bytes = new byte[in.getInt()];
		in.get(bytes);
		
		AIMessage aiMessage = AIMessage.parseFrom(bytes);
		
		ByteString sessionBytes = aiMessage.getSession();
		ByteString contentBytes = aiMessage.getContent();
		
		if ( sessionBytes != null && contentBytes != null) {
			SessionKey sessionKey = SessionKey.createSessionKey(sessionBytes.toByteArray());
			XinqiMessage xinqiMessage = XinqiMessage.fromByteArray(contentBytes.toByteArray());
			
			if ( xinqiMessage != null ) {
				SessionAIMessage sessionMessage = new SessionAIMessage();
				sessionMessage.setSessionKey(sessionKey);
				sessionMessage.setMessage(xinqiMessage);
				out.write(sessionMessage);
			}
		} else {
			logger.debug("AIProtocolDecoder sessionBytes or contentBytes is null");
		}
		return true;
	}
}