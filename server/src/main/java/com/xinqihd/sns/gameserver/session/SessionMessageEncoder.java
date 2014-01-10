package com.xinqihd.sns.gameserver.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.xinqihd.sns.gameserver.transport.MessageToId;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * Encode the XinqiMessage with its corresponding session key.
 * 
 * @author wangqi
 * 
 */
public class SessionMessageEncoder extends ProtocolEncoderAdapter {
	
	private static final int SESSION_HEADER_LENGTH  = 2;
	private static final int XINQI_HEADER_LENGTH  = 8;
	
	private static final Log log = LogFactory.getLog(SessionMessageEncoder.class);

	/**
	 * Check the id value and set it into context attachment.
	 */
	@Override
	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) 
			throws Exception {
		
		SessionMessage sessionMessage = null;
		if ( msg instanceof SessionMessage ) {
			sessionMessage = (SessionMessage)msg;
		}
		
		XinqiMessage xinqiMessage = sessionMessage.getMessage();
		if (xinqiMessage != null) {
			if ( xinqiMessage.payload != null ) {
				byte[] sessionKeyBytes = sessionMessage.getSessionkey().getRawKey();
				byte[] content = xinqiMessage.payload.toByteArray();
				IoBuffer body = IoBuffer.allocate(SESSION_HEADER_LENGTH + sessionKeyBytes.length + XINQI_HEADER_LENGTH + content.length);
				/*
				 * +---------------+               +-------------------+-----------------+-------------------+
				 * | Message Data  |-------------->| sessionkey length |    sessionkey   | XinqiMessage Data |
				 * |  (300 bytes)  |               |       0xFFFF      |      (bytes)    |     (300 bytes)   |
				 * +---------------+               +-------------------+-----------------+-------------------+
				 */
				body.putShort((short)sessionKeyBytes.length);
				body.put(sessionKeyBytes);
				int length = content.length;
				body.putShort((short)(length + 6));
				xinqiMessage.type = MessageToId.messageToId(xinqiMessage.payload);
				body.putShort((short)xinqiMessage.type);
				body.putInt(xinqiMessage.index+1);
				body.put(content);
				body.flip();
				out.write(body);
			
				if ( log.isDebugEnabled() ) {
					log.debug("Request["+xinqiMessage.toString()+"]");
				}
			} else {
				//No payload message.
				byte[] sessionKeyBytes = sessionMessage.getSessionkey().getRawKey();
				short totalLength = 6;
				IoBuffer body = IoBuffer.allocate(SESSION_HEADER_LENGTH + sessionKeyBytes.length + XINQI_HEADER_LENGTH);
				
				body.putShort((short)sessionKeyBytes.length);
				body.put(sessionKeyBytes);
				
				body.putShort(totalLength);
				xinqiMessage.type = MessageToId.messageToId(xinqiMessage.payload);
				body.putShort((short)xinqiMessage.type);
				body.putInt(xinqiMessage.index+1);
				if ( log.isDebugEnabled() ) {
					log.debug("Request["+xinqiMessage.toString()+"]");
				}
				body.flip();
				out.write(body);
			}
		}
	}

}
