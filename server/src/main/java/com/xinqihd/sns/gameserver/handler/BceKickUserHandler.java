package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceKickUser.BceKickUser;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceKickUserHandler is used for protocol KickUser 
 * @author wangqi
 *
 */
public class BceKickUserHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceKickUserHandler.class);
	
	private static final BceKickUserHandler instance = new BceKickUserHandler();
	
	private BceKickUserHandler() {
		super();
	}

	public static BceKickUserHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceKickUser");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BceKickUser kickUser = (BceKickUser)request.payload;
		String outUserId = kickUser.getOutuserid();
		SessionKey outUserSessionKey = SessionKey.createSessionKeyFromHexString(outUserId);
		if ( outUserSessionKey == null ) {
			logger.debug("Cannot find kicked user sessionkey: {}", outUserId);
			return;
		}
		RoomManager roomManager = GameContext.getInstance().getRoomManager();
		SessionKey roomSessionKey = roomManager.getInstance().findRoomSessionKeyByUserSession(outUserSessionKey);
		Room room = null;
		boolean result = false;
		if ( roomSessionKey != null ) {
			room = roomManager.getInstance().acquireRoom(roomSessionKey, true);
			if ( room == null ) {
				logger.debug("Room {} is deleted.", room.getRoomSessionKey());
			} else if ( room.isRemote() ) {
				String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(sessionKey);
				GameContext.getInstance().proxyToRemoteGameServer(sessionKey, gameServerId, kickUser);
			} else {
				//Check if the kicker is the room owner
				if ( user.getSessionKey().equals(room.getOwnerSessionKey()) ) {
					result = roomManager.kickUser(room, outUserSessionKey, true);
				} else {
					SysMessageManager.getInstance().sendClientInfoMessage(user, "room.kick.notowner", Type.NORMAL);
				}
			}
		}
		
		if ( room != null ) {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.KickUser, room.getRoomSessionKey(), outUserId, result);
		} else {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.KickUser, Constant.EMPTY, outUserId, result);
		}
	}
	
	
}
