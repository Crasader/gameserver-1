package com.xinqihd.sns.gameserver.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.google.protobuf.MessageLite;
import com.xinqihd.sns.gameserver.transport.IdToMessage;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Decode the transport message structure like that:
 * <pre>
 * BEFORE DECODE (300 bytes)       AFTER DECODE (308 bytes)
 * +---------------+               +-------------------+-----------------+-------------------+
 * | Message Data  |-------------->| sessionkey length |    sessionkey   | XinqiMessage Data |
 * |  (300 bytes)  |               |       0xFFFF      |      (bytes)    |     (300 bytes)   |
 * +---------------+               +-------------------+-----------------+-------------------+
 * </pre> 
 * @author wangqi
 *
 */
public class SessionMessageDecoder extends CumulativeProtocolDecoder {
	
	private static final String DECODER_STATE_KEY = SessionMessageDecoder.class.getName();
	
	private static final int MAX_LENGTH = Short.MAX_VALUE * 2;
	
	private static final int HEADER_LENGTH = 2;
	
	private static final Log log = LogFactory.getLog(SessionMessageDecoder.class);
		
	/**
	 * Decode the message.
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		SessionMessage sessionMessage = (SessionMessage) session.getAttribute(DECODER_STATE_KEY);
		if ( sessionMessage == null ) {
			sessionMessage = new SessionMessage();
			session.setAttribute(DECODER_STATE_KEY, sessionMessage);
		}
		
		// Make sure the sessionkey in the header bytes are ready.
		if ( !in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH) ) {
	    return false;
		}
		int sessionKeyLength = in.getShort();
		byte[] sessionRawBytes = new byte[sessionKeyLength];
		in.get(sessionRawBytes);
		SessionKey sessionKey = SessionKey.createSessionKey(sessionRawBytes);
		sessionMessage.setSessionkey(sessionKey);
		
		// Make sure the xinqimessage in the header bytes are ready.
		if ( !in.prefixedDataAvailable(HEADER_LENGTH, MAX_LENGTH) ) {
	    return false;
		}
		
		int xinqiMessageLength = in.getShort()-6;
		XinqiMessage message = new XinqiMessage();
		
		message.type = in.getShort();
		message.index = in.getInt();
		if ( log.isDebugEnabled() ) {
			log.debug("XinqiMessage length:"+xinqiMessageLength+", type:"+message.type+", index:"+message.index);
		}
		
		MessageLite request = IdToMessage.idToMessage(message.type);
		if ( request == null ) {
			if ( log.isWarnEnabled() ) {
				log.warn("No id found for message type. return null. ");
			}
			return false;
		}
		
		request = request.newBuilderForType().mergeFrom(in.slice().array(), sessionKeyLength + 10 , xinqiMessageLength).build();

		in.skip(xinqiMessageLength);
		if ( log.isDebugEnabled() ) {
			log.debug("Message:"+request.getClass().getName()+"["+request+"]");
		}
		message.payload = request;
		if ( log.isDebugEnabled() ) {
			log.debug("Response["+message.toString()+"]");
		}
		
		sessionMessage.setMessage(message);
		
		out.write(sessionMessage);
		return true;
	}
}