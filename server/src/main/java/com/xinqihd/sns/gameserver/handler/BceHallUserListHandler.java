package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.proto.XinqiBseHallUserList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceHallUserListHandler is used for protocol HallUserList 
 * @author wangqi
 *
 */
public class BceHallUserListHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceHallUserListHandler.class);
	
	private static final BceHallUserListHandler instance = new BceHallUserListHandler();
	
	private BceHallUserListHandler() {
		super();
	}

	public static BceHallUserListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceHallUserList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiMessage response = new XinqiMessage();
		XinqiBseHallUserList.BseHallUserList.Builder builder = XinqiBseHallUserList.BseHallUserList.newBuilder();
		response.payload = builder.build();
		//TODO BEGIN add logic here.
		//builder.setUid(((XinqiBceHallUserList.BceHallUserList)request.payload).getUid());
		//END
		session.write(response);
		System.out.println("BceHallUserList: " + response);
	}
	
	
}
