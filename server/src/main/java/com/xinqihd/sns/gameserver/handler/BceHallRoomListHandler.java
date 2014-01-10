package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseHallRoomList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceHallRoomListHandler is used for protocol HallRoomList 
 * @author wangqi
 *
 */
public class BceHallRoomListHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceHallRoomListHandler.class);
	
	private static final BceHallRoomListHandler instance = new BceHallRoomListHandler();
	
	private BceHallRoomListHandler() {
		super();
	}

	public static BceHallRoomListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceHallRoomList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseHallRoomList.BseHallRoomList.Builder builder = XinqiBseHallRoomList.BseHallRoomList.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceHallRoomList.BceHallRoomList)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceHallRoomList: " + response);
	}
	
	
}
