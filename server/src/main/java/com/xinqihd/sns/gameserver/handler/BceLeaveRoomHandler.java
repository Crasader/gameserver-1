package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceLeaveRoomHandler is used for protocol LeaveRoom 
 * @author wangqi
 *
 */
public class BceLeaveRoomHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceLeaveRoomHandler.class);
	
	private static final BceLeaveRoomHandler instance = new BceLeaveRoomHandler();
	
	private BceLeaveRoomHandler() {
		super();
	}

	public static BceLeaveRoomHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceLeaveRoom");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		if ( sessionKey == null ) {
			logger.warn("User session is null. ");
			return;
		}
		BattleManager.getInstance().leaveBattle(sessionKey);
		RoomManager roomManager = GameContext.getInstance().getRoomManager();
		roomManager.leaveRoom(sessionKey);
	}
	
	
}
