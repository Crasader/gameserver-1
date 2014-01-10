package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseFindRoom;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceFindRoomHandler is used for protocol FindRoom 
 * @author wangqi
 *
 */
public class BceFindRoomHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceFindRoomHandler.class);
	
	private static final BceFindRoomHandler instance = new BceFindRoomHandler();
	
	private BceFindRoomHandler() {
		super();
	}

	public static BceFindRoomHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceFindRoom");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseFindRoom.BseFindRoom.Builder builder = XinqiBseFindRoom.BseFindRoom.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceFindRoom.BceFindRoom)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceFindRoom: " + response);
	}
	
	
}
