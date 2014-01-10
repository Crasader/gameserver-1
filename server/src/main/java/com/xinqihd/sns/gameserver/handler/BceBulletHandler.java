package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBulletHandler is used for protocol Bullet 
 * @author wangqi
 *
 */
public class BceBulletHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBulletHandler.class);
	
	private static final BceBulletHandler instance = new BceBulletHandler();
	
	private BceBulletHandler() {
		super();
	}

	public static BceBulletHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBullet");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
    //TODO BseBullet not exists
		//XinqiBseBullet.BseBullet.Builder builder = XinqiBseBullet.BseBullet.newBuilder();
		//response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceBullet.BceBullet)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceBullet: " + response);
	}
	
	
}
