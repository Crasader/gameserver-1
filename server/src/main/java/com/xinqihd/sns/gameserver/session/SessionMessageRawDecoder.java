package com.xinqihd.sns.gameserver.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Decode the transport message structure like that:
 * <pre>
 * BEFORE DECODE (300 bytes)       AFTER DECODE (308 bytes)
 * +---------------+               +-------------------+-----------------+--------------------------+--------------------+
 * | Message Data  |-------------->| sessionkey length |    sessionkey   |        Raw Length        |     Any Raw Data   |
 * |  (300 bytes)  |               |       0xFFFF      |      (bytes)    |        0xFFFFFFFF        |     (300 bytes)    |
 * +---------------+               +-------------------+-----------------+--------------------------+--------------------+
 * </pre> 
 * @author wangqi
 *
 */
public class SessionMessageRawDecoder extends CumulativeProtocolDecoder {
	
	private static final String DECODER_STATE_KEY = "XinqiSessionMessageRawDecoder";
	private static final String DECODER_STAGE_KEY = "XinqiSessionMessageRawDecoder.Stage";
	
	private static final int MAX_LENGTH = Short.MAX_VALUE * 2;
	
	private static final int RAW_MAX_LENGTH = 512000;
	
	private static final int SESSION_LENGTH = 2;
	
	private static final int RAW_LENGTH = 4;
	
	private static final Log log = LogFactory.getLog(SessionMessageRawDecoder.class);
	
		
	/**
	 * Decode the message.
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		SessionRawMessage sessionMessage = (SessionRawMessage) session.getAttribute(DECODER_STATE_KEY);
		DecoderStage stage = (DecoderStage) session.getAttribute(DECODER_STAGE_KEY);
		if ( sessionMessage == null ) {
			sessionMessage = new SessionRawMessage();
			session.setAttribute(DECODER_STATE_KEY, sessionMessage);
		}
		if ( stage == null ) {
			stage = DecoderStage.DECODE_SESSION_KEY;
			session.setAttribute(DECODER_STAGE_KEY, stage);
		}
		
		switch ( stage ) {
			case DECODE_SESSION_KEY:
				// Make sure the sessionkey in the header bytes are ready.
				if ( !in.prefixedDataAvailable(SESSION_LENGTH, MAX_LENGTH) ) {
			    return false;
				}
				int sessionKeyLength = in.getShort() & 0xFFFF;
				byte[] sessionRawBytes = new byte[sessionKeyLength];
				in.get(sessionRawBytes);
				SessionKey sessionKey = SessionKey.createSessionKey(sessionRawBytes);
				sessionMessage.setSessionkey(sessionKey);
				session.setAttribute(DECODER_STAGE_KEY, DecoderStage.DECODE_RAW);
				
			case DECODE_RAW:
				// Make sure the xinqimessage in the header bytes are ready.
				if ( !in.prefixedDataAvailable(RAW_LENGTH, RAW_MAX_LENGTH) ) {
			    return false;
				}
				int rawLength = in.getInt();
				if ( log.isDebugEnabled() ) {
					log.debug("RawMessage length:"+rawLength);
				}
				byte[] rawMessage = new byte[rawLength];
				in.get(rawMessage);
				
				sessionMessage.setRawMessage(rawMessage);
				out.write(sessionMessage);
				
				session.setAttribute(DECODER_STATE_KEY, null);
				session.setAttribute(DECODER_STAGE_KEY, null);
				return true;
			default:
				break;
		}
		
		return false;
	}
	
	private static enum DecoderStage {
		DECODE_SESSION_KEY,
		DECODE_RAW;
	}
}