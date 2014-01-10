package com.xinqihd.sns.gameserver.transport.http;

public interface HttpProcessor {

	/**
	 * Process the uri request and return the HttpMessage
	 *  
	 * @param uri
	 * @param variable
	 */
	public void process(String uri, String variable, HttpMessage httpMessage);
}
