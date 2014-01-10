package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseSyncPos;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceSyncPosHandler is used for protocol SyncPos 
 * @author wangqi
 *
 */
public class BceSyncPosHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceSyncPosHandler.class);
	
	private static final BceSyncPosHandler instance = new BceSyncPosHandler();
	
	private BceSyncPosHandler() {
		super();
	}

	public static BceSyncPosHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceSyncPos");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseSyncPos.BseSyncPos.Builder builder = XinqiBseSyncPos.BseSyncPos.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceSyncPos.BceSyncPos)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceSyncPos: " + response);
	}
	
	
}
