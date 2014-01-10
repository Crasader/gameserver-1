package com.xinqihd.sns.gameserver.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * The XinqiProxyMessage is used to proxy an user's request (BceXXX) to
 * another server. That may happen when multi-servers run and a distributed
 * battle occurs among different servers.
 * 
 * @author wangqi
 *
 */
public class XinqiProxyMessage {

	private static final Logger logger = LoggerFactory.getLogger(XinqiProxyMessage.class);
	
	public SessionKey userSessionKey;
	public XinqiMessage xinqi;
			
}
