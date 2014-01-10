package com.xinqihd.sns.gameserver.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.session.SessionKey;

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
	
	private static final int MAX_LENGTH = 256000;
	
	private static final int HEADER_LENGTH = 4;
	
	private static final Log log = LogFactory.getLog(ProtobufDecoder.class);
		
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
		
		Object obj = ProtobufDecoder.decodeMessage(in);
		if ( obj instanceof XinqiMessage ) {
			XinqiMessage message = (XinqiMessage)obj;
			if ( message == null ) {
				return false;
			}
			out.write(message);
			return true;
		} else if ( obj instanceof XinqiProxyMessage ) {
			XinqiProxyMessage proxy = (XinqiProxyMessage)obj;
			if ( proxy == null ) {
				return false;
			}
			out.write(proxy);
			return true;
		}
		
//		session.setAttribute(DECODER_STATE_KEY, null);
		return false;
	}
	
	/**
	 * Decode the message from byte array. If the message tyep is 0xFFFF,
	 * it is a XinqiProxyMessage. Otherwise, it is a normal XinqiMessage
	 * @param in
	 * @return
	 * @throws InvalidProtocolBufferException 
	 */
	public static final Object decodeMessage(IoBuffer in) 
			throws InvalidProtocolBufferException {
		
		// Make sure all the header bytes are ready.
		if ( !in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH) ) {
	    return null;
		}
		
		int totalLength = in.getInt();
		int type = in.getShort();
		if ( type == -1 ) {
			//XinqiProxyMessage
			XinqiProxyMessage message = new XinqiProxyMessage();
			if ( totalLength - 2 > 0 ) {
				int sessionKeyLength = in.getShort();
				byte[] sessionKeyBytes = new byte[sessionKeyLength];
				in.get(sessionKeyBytes);
				XinqiMessage xinqi = decodeXinqiMessage(in);
			
				message.userSessionKey = SessionKey.createSessionKey(sessionKeyBytes);
				message.xinqi = xinqi;
			} else {
				//heartbeat message.
			}
			return message;
		} else {
			XinqiMessage message = new XinqiMessage();
			//XinqiMessage
			int length = totalLength - 6;
			message.type = type;
			message.index = in.getInt();
			byte[] array = new byte[length];
			in.get(array);
			/*
			if ( log.isDebugEnabled() ) {
				log.debug("length:"+length+", type:"+message.type+", index:"+message.index);
			}
			*/
			MessageLite request = IdToMessage.idToMessage(message.type);
			
			if ( request == null ) {
				log.warn("No id found for message type. return empty message. ");
				//Note: return an empty message rather than null so that
				//next messages can be parsed.
				return message;
			}
			
			request = request.newBuilderForType().mergeFrom(array).build();

			message.payload = request;
			/*
			if ( log.isDebugEnabled() ) {
				log.debug("DecodedMessage:"+request.getClass().getName()+"[\n"+request+"]");
			}
			if ( log.isDebugEnabled() ) {
				log.debug("XinqiMessage:[\n"+message.toString()+"]");
			}
			*/
			return message;
		}
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
		if ( log.isDebugEnabled() ) {
			log.debug("length:"+length+", type:"+message.type+", index:"+message.index);
		}
		
		MessageLite request = IdToMessage.idToMessage(message.type);
		
		if ( request == null ) {
			if ( log.isWarnEnabled() ) {
				log.warn("No id found for message type. return empty message. ");
			}
			//Note: return an empty message rather than null so that
			//next messages can be parsed.
			return message;
		}
		
		request = request.newBuilderForType().mergeFrom(array).build();

//			in.skip(length);
		if ( log.isDebugEnabled() ) {
			log.debug("DecodedMessage:"+request.getClass().getName()+"[\n"+request+"]");
		}
		message.payload = request;
		if ( log.isDebugEnabled() ) {
			log.debug("XinqiMessage:[\n"+message.toString()+"]");
		}
		
		return message;
	}
}