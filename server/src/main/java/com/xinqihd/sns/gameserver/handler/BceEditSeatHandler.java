package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceEditSeat.BceEditSeat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceEditSeatHandler is used for protocol EditSeat 
 * @author wangqi
 *
 */
public class BceEditSeatHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceEditSeatHandler.class);
	
	private static final BceEditSeatHandler instance = new BceEditSeatHandler();
	
	private BceEditSeatHandler() {
		super();
	}

	public static BceEditSeatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceEditSeat");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceEditSeat  editSeat = (BceEditSeat)request.payload;
		
		RoomManager roomManager = GameContext.getInstance().getRoomManager();
		SessionKey roomSession = roomManager.findRoomSessionKeyByUserSession(user.getSessionKey());
		roomManager.editSeat(user.getSessionKey(), editSeat.getIndex(), editSeat.getOpen());

		StatClient.getIntance().sendDataToStatServer(user, StatAction.EditSeat, editSeat.getIndex(), editSeat.getOpen());
	}
	
	
}
