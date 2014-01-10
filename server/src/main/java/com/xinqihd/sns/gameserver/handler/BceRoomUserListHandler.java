package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoomUserList.BseRoomUserList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * 罗列各个房间的人数情况
 *
 */
public class BceRoomUserListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceRoomUserListHandler.class);
	
	private static final BceRoomUserListHandler instance = new BceRoomUserListHandler();
	
	private BceRoomUserListHandler() {
		super();
	}

	public static BceRoomUserListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceRoomUserList");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		String roleName = user.getRoleName();
		logger.info("receive requsest:{}", roleName);

		BseRoomUserList.Builder builder = BseRoomUserList.newBuilder();
		for ( RoomType roomType : RoomType.values() ) {
			int[] userNumbers = RoomManager.getInstance().getRoomUserNumber(roomType);
			builder.addRoomTypeId(roomType.ordinal());
			builder.addRoomUsers(userNumbers[0]);
			builder.addRoomMaxUsers(userNumbers[1]);
			long endMillis = System.currentTimeMillis();
			logger.info("process roomtype:{}", roomType);
		}
		GameContext.getInstance().writeResponse(sessionKey, builder.build());

		logger.info("before send stat");
		StatClient.getIntance().sendDataToStatServer(user, StatAction.RoomUserList);
		logger.info("after send stat");
	}
	
}
