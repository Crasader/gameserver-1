package com.xinqihd.sns.gameserver.transport.http;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * This is a simple http request/response wrapper class.
 * 
 * @author wangqi
 *
 */
public class HttpMessage {
	
	public static final byte[] HTTP_404 = "HTTP/1.1 404 Not Found\r\n".getBytes();
	public static final byte[] HTTP_405 = "HTTP/1.1 405 Method Not Allowed\r\n".getBytes();

	private String requestUri;
	
	private StringBuilder requestContent;
	
	private boolean beginBody = false;
	
	private boolean isPost = false;
	
	private byte[] responseLastModified;
	
	private byte[] responseContentType;
	
	private byte[] responseContent;
	
	private byte[] responseCode;
	
	private ArrayList<byte[]> headers = null;
	
	private boolean isHead;
	

	public byte[] getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(byte[] responseCode) {
		this.responseCode = responseCode;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public String getRequestContent() {
		if ( requestContent != null ) {
			return requestContent.toString().trim();
		} else {
			return Constant.EMPTY;
		}
	}

	/**
	 * Append the POST content
	 * @param requestContent
	 */
	public void appendRequestContent(String requestContent) {
		if ( this.requestContent == null ) {
			this.requestContent = new StringBuilder(100);
		}
		this.requestContent.append(requestContent);
	}
	
	public boolean isBeginBody() {
		return beginBody;
	}

	public void setBeginBody(boolean beginBody) {
		this.beginBody = beginBody;
	}

	public boolean isPost() {
		return isPost;
	}

	public void setPost(boolean isPost) {
		this.isPost = isPost;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
	}

	public byte[] getResponseLastModified() {
		return responseLastModified;
	}

	public void setResponseLastModified(byte[] responseLastModified) {
		this.responseLastModified = responseLastModified;
	}

	public byte[] getResponseContentType() {
		return responseContentType;
	}

	public void setResponseContentType(byte[] responseContentType) {
		this.responseContentType = responseContentType;
	}

	public byte[] getResponseContent() {
		return responseContent;
	}

	public void setResponseContent(byte[] responseContent) {
		this.responseContent = responseContent;
	}

	public boolean isHead() {
		return isHead;
	}

	public void setHead(boolean isHead) {
		this.isHead = isHead;
	}

	public void addHeader(byte[] header) {
		if ( this.headers == null ) {
			this.headers = new ArrayList<byte[]>();
		}
		this.headers.add(header);
	}
	
	public ArrayList<byte[]> getHeaders() {
		return this.headers;
	}
}
