package com.xinqihd.sns.gameserver.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Encode the XinqiMessage with its corresponding session key.
 * 
 * @author wangqi
 * 
 */
public class SessionMessageRawEncoder extends ProtocolEncoderAdapter {
	
	private static final int SESSION_HEADER_LENGTH  = 2;
	private static final int RAW_HEADER_LENGTH  = 4;
	
	private static final Log log = LogFactory.getLog(SessionMessageRawEncoder.class);

	/**
	 * Check the id value and set it into context attachment.
	 */
	@Override
	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) 
			throws Exception {
		
		SessionRawMessage sessionMessage = null;
		if ( msg instanceof SessionRawMessage ) {
			sessionMessage = (SessionRawMessage)msg;
		}
		
		byte[] rawMessage = sessionMessage.getRawMessage();
		if (rawMessage != null) {
			byte[] sessionKeyBytes = sessionMessage.getSessionkey().getRawKey();
			IoBuffer body = IoBuffer.allocate(SESSION_HEADER_LENGTH + sessionKeyBytes.length + RAW_HEADER_LENGTH + rawMessage.length);
			/*
			 * +---------------+               +-------------------+-----------------+--------------------------+--------------------+
			 * | Message Data  |-------------->| sessionkey length |    sessionkey   |        Raw Length        |     Any Raw Data   |
			 * |  (300 bytes)  |               |       0xFFFF      |      (bytes)    |        0xFFFFFFFF        |     (300 bytes)    |
			 * +---------------+               +-------------------+-----------------+--------------------------+--------------------+
			 */
			body.putShort((short)sessionKeyBytes.length);
			body.put(sessionKeyBytes);
			body.putInt(rawMessage.length);
			body.put(rawMessage);
			body.flip();
			out.write(body);
		
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug("Raw message's content is empty.");
			}
		}
	}

}
