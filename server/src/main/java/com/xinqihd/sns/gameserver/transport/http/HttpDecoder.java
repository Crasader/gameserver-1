package com.xinqihd.sns.gameserver.transport.http;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It only support the GET method in HTTP. No Cookie, no content-encoding, no
 * static file, it is really really fast.
 * 
 * The request should be as following format: 
 * 	GET /data/map_config.lua HTTP/1.1\r\n
 * 	Host: 192.168.0.1\r\n
 * 	\r\n
 * 
 * The response should be:
 * 	HTTP/1.1 200 OK
 * 	Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT
 * 	Content-Length: 438
 * 	Content-Type: text/html; charset=UTF-8
 * 	Connection: close
 * 
 * @author wangqi
 *
 */
public class HttpDecoder extends CumulativeProtocolDecoder {
	
	private static final String KEY = "HttpDecoder.Session";
	
	private static final Logger logger = LoggerFactory.getLogger(HttpDecoder.class);
	
	private static final char CR = '\r';
	private static final char LF = '\n';
	
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String HEAD = "HEAD";
	private static final String HTTP = "HTTP/";

	/**
	 * Try to decode the http message into HttpMessage object.
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) 
			throws Exception {
		//Try to get session object.
		HttpMessage httpMessage = (HttpMessage)session.getAttribute(KEY);
		if ( httpMessage == null ) {
			httpMessage = new HttpMessage();
			session.setAttribute(KEY, httpMessage);
		}
    // Remember the initial position.
    int start = in.position();

    // Now find the first CRLF in the buffer.
    byte previous = 0;
    while (in.hasRemaining()) {
        byte current = in.get();
        if ( previous == CR || current == LF ) {
            // Remember the current position and limit.
            int position = in.position();
            int limit = in.limit();
            try {
                in.position(start);
                in.limit(position);
                // The bytes between in.position() and in.limit()
                // now contain a full CRLF terminated line.
                byte[] chars = new byte[position-start];
                in.get(chars);
                String httpRequest = new String(chars);
                if ( "Content-Length: 0\r\n".equals(httpRequest) ) {
                	//no content
                	break;
                }
                start = limit;
                return parseHttpRequest(httpRequest, httpMessage, out);
            } finally {
                // Set the position to point right after the
                // detected line and set the limit to the old
                // one.
                in.position(position);
                in.limit(limit);
            }
//        } else {
//        	System.out.print((char)current);
        }
        previous = current;
    }
    int limit = in.limit();
    if ( start < limit ) {
    	//Add the left bytes
      in.position(start);
      byte[] chars = new byte[limit-start];
      in.get(chars);
      String httpRequest = new String(chars);
      parseHttpRequest(httpRequest, httpMessage, out);
      out.write(httpMessage);
      return false;
    } else {
      // Could not find CRLF in the buffer. Reset the initial
      // position to the one we recorded above.
      in.position(start);
    }
    return false;
	}

	/**
	 * Parse the http request to HttpMessage object.
	 * @param httpRequest
	 * @return
	 */
	private boolean parseHttpRequest(String httpRequest, HttpMessage httpMessage, ProtocolDecoderOutput out) {
		int getIndex = httpRequest.indexOf(GET);
		int protoIndex = httpRequest.lastIndexOf(HTTP);
		if ( getIndex >= 0 && getIndex + 4 < httpRequest.length() 
				&& getIndex < protoIndex ) {
			//Valid http request
			String requestUri = httpRequest.substring(getIndex+4, protoIndex-1);
			httpMessage.setRequestUri(requestUri);
			out.write(httpMessage);
			return true;
		} else {
			int postIndex = httpRequest.indexOf(POST);
			if ( postIndex >= 0 && postIndex + 5 < httpRequest.length() ) {
				//process post
				httpMessage.setPost(true);
				String requestUri = httpRequest.substring(postIndex+5, protoIndex-1);
				httpMessage.setRequestUri(requestUri);
				return true;
			} else {
				int headIndex = httpRequest.indexOf(HEAD);
				if ( headIndex >= 0 && headIndex + 5 < httpRequest.length() 
						&& headIndex < protoIndex ) {
					//Valid http request
					String requestUri = httpRequest.substring(headIndex+5, protoIndex-1);
					httpMessage.setHead(true);
					httpMessage.setRequestUri(requestUri);
					out.write(httpMessage);
					return true;
				} else {
	        // Decoded one line; CumulativeProtocolDecoder will
	        // call me again until I return false. So just
	        // return true until there are no more lines in the
	        // buffer.
					if ( httpMessage.isPost() ) {
						if ( httpMessage.isBeginBody() ) {
							httpMessage.appendRequestContent(httpRequest);
						}
						if ( "\r\n".equals(httpRequest) ) {
							if ( !httpMessage.isBeginBody() ) {
								httpMessage.setBeginBody(true);
							} else {
								out.write(httpMessage);								
							}
						}
					}
					return true;
				}
			}
		}
	}
}
