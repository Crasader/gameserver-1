package com.xinqihd.sns.gameserver.transport.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * It gets HttpMessage and output them to client.
 * The socket connection will be closed after writing.
 * 
 * @author wangqi
 *
 */
public class HttpEncoder extends ProtocolEncoderAdapter {
	
	private static final Log log = LogFactory.getLog(HttpEncoder.class);
	
	private static final byte[] HTTP_OK = "HTTP/1.1 200 OK\r\n".getBytes();
	private static final byte[] CONTENT_TYPE = "Content-Type: ".getBytes();
	private static final byte[] CONTENT_LENGTH = "Content-Length: ".getBytes();
	private static final byte[] LAST_MODIFIED = "Last-Modified: ".getBytes();
	private static final byte[] CONTENT_CLOSE = "Connection: close\r\n".getBytes();
	
	private static final byte[] ZERO = "0".getBytes();
	private static final byte[] LINE_END = "\r\n".getBytes();

	/**
	 * Encode the HttpMessage to ChannelBuffer.
	 */
	@Override
	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out) 
			throws Exception {
		HttpMessage message = null;
		if ( msg instanceof HttpMessage ) {
			message = (HttpMessage)msg;
		}
		IoBuffer response = IoBuffer.allocate(Constant.CACHE_HTTP_SIZE);
		response.setAutoExpand(true);
		if ( message.getResponseCode() == null ) {
			response.put(HTTP_OK);
		} else {
			response.put(message.getResponseCode());
		}
		response.put(CONTENT_CLOSE);
		if ( message.getResponseLastModified() != null ) {
			response.put(LAST_MODIFIED);
			response.put(message.getResponseLastModified());
			response.put(LINE_END);
		}
		if ( message.getResponseContentType() != null ) {
			response.put(CONTENT_TYPE);
			response.put(message.getResponseContentType());
			response.put(LINE_END);
		}
		if ( message.getHeaders() != null ) {
			for ( byte[] header : message.getHeaders() ) {
				response.put(header);
				response.put(LINE_END);	
			}
		}
		if ( !message.isHead() && message.getResponseContent() != null && message.getResponseContent().length > 0 ) {
			int length = message.getResponseContent().length;
			response.put(CONTENT_LENGTH);
			response.put(String.valueOf(length).getBytes());
			response.put(LINE_END);
			response.put(LINE_END);
			response.put(message.getResponseContent());
		} else {
			response.put(CONTENT_LENGTH);
			response.put(ZERO);
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Write http response finished.");
		}
		response.flip();
		out.write(response);
	} 
	
}
