package com.xinqihd.sns.gameserver.transport.stat;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Encode the protocol buffer with its corresponding id.
 * 
 * @author wangqi
 * 
 */
public class ProtobufEncoder extends ProtocolEncoderAdapter {
	
	private static final int HEADER_LENGTH  = 10;
	
	private static final short DEFAULT_EMPTY_LENGTH = 6;
	
	private static final short DEFAULT_EMPTY_TYPE = 0;
	
	private static final Logger logger = LoggerFactory.getLogger(ProtobufEncoder.class);

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
		} else if ( msg instanceof XinqiMessage ) {
			XinqiMessage message = (XinqiMessage)msg;
			if (message != null) {
				IoBuffer body = encodeXinqiMessage(message);
				out.write(body);
			}
		}
	}
	
	/**
	 * Encode the XinqiMessage to IoBuffer
	 *
	 * output.writeInt(_len);    4
	 * output.writeInt(_type);   2
	 * output.writeInt(0);       4
	 *
	 * An encoder that prepends the the Google Protocol Buffers
	 * <a href="http://code.google.com/apis/protocolbuffers/docs/encoding.html#varints">Base
	 * 128 Varints</a> integer length field.  For example:
	 * <pre>
	 * BEFORE DECODE (300 bytes)       AFTER DECODE (308 bytes)
	 * +---------------+               +------------+---------------------+---------------+
	 * | Protobuf Data |-------------->|   Length   | type   |    index   | Protobuf Data |
	 * |  (300 bytes)  |               | 0xFFFFFFFF | 0xFFFF | 0xFFFFFFFF |  (300 bytes)  |
	 * +---------------+               +------------+---------------------+---------------+
	 * </pre> *
	 * @param message
	 * @return
	 */
	public static final IoBuffer encodeXinqiMessage(XinqiMessage message) {
		if ( message.payload != null ) {
			byte[] content = message.payload.toByteArray();
			IoBuffer body = IoBuffer.allocate(content.length + HEADER_LENGTH);
			int length = content.length;
			message.type = MessageToId.messageToId(message.payload);
			
			body.putInt(length + 6);				
			body.putShort((short)message.type);
			body.putInt(message.index+1);
			body.put(content);
			body.flip();
		
			if ( logger.isDebugEnabled() ) {
				logger.debug("Encoded: ["+message.toString()+"]");
			}
			return body;
		} else {
			//No payload message.
			IoBuffer body = IoBuffer.allocate(HEADER_LENGTH);
			body.putInt(DEFAULT_EMPTY_LENGTH);
			body.putShort(DEFAULT_EMPTY_TYPE);
			body.putInt(message.index+1);
			if ( logger.isDebugEnabled() ) {
				logger.debug("Encoded empty message["+message.toString()+"]");
			}
			body.flip();
			return body;
		}
	}

}
