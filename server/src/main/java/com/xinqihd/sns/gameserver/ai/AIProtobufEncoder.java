package com.xinqihd.sns.gameserver.ai;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.google.protobuf.ByteString;
import com.xinqihd.sns.gameserver.proto.XinqiAI.AIMessage;

/**
 * Encode the protocol buffer with its corresponding id.
 * 
 * @author wangqi
 * 
 */
public class AIProtobufEncoder extends ProtocolEncoderAdapter {
	
	private static final int HEADER_LENGTH  = 4;
	
	private static final Log log = LogFactory.getLog(AIProtobufEncoder.class);

	/**
	 * Check the id value and set it into context attachment.
	 */
	@Override
	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) 
			throws Exception {
		
		/**
		 * If the message is already in IoBuffer format, 
		 * directly output it to client.
		 * 
		 * TODO Maybe used to optimize SessionMessage.
		 */
		if (msg instanceof IoBuffer) {
			out.write(msg);
			return;
		}
		
		SessionAIMessage sessionMessage = null;
		
		if (msg instanceof SessionAIMessage) {
			sessionMessage = (SessionAIMessage)msg;
		}
		if (sessionMessage != null && sessionMessage.getSessionKey() != null ) {
			AIMessage.Builder builder = AIMessage.newBuilder();
			builder.setSession(ByteString.copyFrom(sessionMessage.getSessionKey().getRawKey()));
			if ( sessionMessage.getMessage() != null ) {
				builder.setContent(ByteString.copyFrom(sessionMessage.getMessage().toByteArray()));
			}
			
			byte[] content = builder.build().toByteArray();
			IoBuffer body = IoBuffer.allocate(HEADER_LENGTH + content.length);
			body.putInt(content.length);
			body.put(content);
			body.flip();
			
			out.write(body);
		} else {
			//It is an heart-beat message to server
			AIMessage.Builder builder = AIMessage.newBuilder();
			
			IoBuffer body = IoBuffer.allocate(HEADER_LENGTH);
			body.putInt(0);
			body.flip();
			
			out.write(body);
		}
	}

}
