package com.xinqihd.sns.gameserver.transport.stat;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Decode the transport message structure like that:
 * <pre>
 * BEFORE DECODE (300 bytes)       AFTER DECODE (308 bytes)
 * +---------------+               +------------+---------------------+---------------+
 * | Protobuf Data |-------------->|   Length   | type   |    index   | Protobuf Data |
 * |  (300 bytes)  |               | 0xFFFFFFFF | 0xFFFF | 0xFFFFFFFF |  (300 bytes)  |
 * +---------------+               +------------+---------------------+---------------+
 * </pre> 
 * @author wangqi
 *
 */
public class ProtobufDecoder extends CumulativeProtocolDecoder {
	
	private static final String DECODER_STATE_KEY = ProtobufDecoder.class.getName();
	
	private static final int MAX_LENGTH = Integer.MAX_VALUE;
	
	private static final int HEADER_LENGTH = 4;
	
	private static final Logger logger = LoggerFactory.getLogger(ProtobufDecoder.class);
		
	/**
	 * Decode the message.
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
//		XinqiMessage message = (XinqiMessage) session.getAttribute(DECODER_STATE_KEY);
//		if ( message == null ) {
//			message = new XinqiMessage();
//			session.setAttribute(DECODER_STATE_KEY, message);
//		}
		
		Object obj = ProtobufDecoder.decodeXinqiMessage(in);
		if ( obj instanceof XinqiMessage ) {
			XinqiMessage message = (XinqiMessage)obj;
			if ( message == null ) {
				return false;
			}
			out.write(message);
			return true;
		}
		
//		session.setAttribute(DECODER_STATE_KEY, null);
		return false;
	}
		
	/**
	 * Decode the XinqiMessage from byte array.
	 * @param in
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	public static final XinqiMessage decodeXinqiMessage(IoBuffer in) 
			throws InvalidProtocolBufferException {
		
		// Make sure all the header bytes are ready.
		if ( !in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH) ) {
	    return null;
		}
		
		int length = in.getInt() - 6;
		int type = in.getShort();
		XinqiMessage message = new XinqiMessage();
		//XinqiMessage
		message.type = type;
		message.index = in.getInt();
		byte[] array = new byte[length];
		in.get(array);
		if ( logger.isDebugEnabled() ) {
			logger.debug("length:"+length+", type:"+message.type+", index:"+message.index);
		}
		
		MessageLite request = IdToMessage.idToMessage(message.type);
		
		if ( request == null ) {
			logger.warn("No id found for message type. return empty message. ");
			return message;
		}
		
		request = request.newBuilderForType().mergeFrom(array).build();

		message.payload = request;
		
		return message;
	}
}