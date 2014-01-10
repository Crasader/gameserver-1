package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChangeMap.BceChangeMap;
import com.xinqihd.sns.gameserver.proto.XinqiBseChangeMap.BseChangeMap;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceChangeMapHandler is used for protocol ChangeMap 
 * @author wangqi
 *
 */
public class BceChangeMapHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceChangeMapHandler.class);
	
	private static final BceChangeMapHandler instance = new BceChangeMapHandler();
	
	private BceChangeMapHandler() {
		super();
	}

	public static BceChangeMapHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceChangeMap");
		}
		if ( ! (message instanceof XinqiMessage) ) {
			return;
		}
		XinqiMessage request = (XinqiMessage)message;
		BceChangeMap changeMap = (BceChangeMap)request.payload;
		int mapId = changeMap.getMapID();
		RoomManager roomManager = GameContext.getInstance().getRoomManager();
		if ( mapId >= 0 ) {
			String strMapId = String.valueOf(mapId);
			boolean success = roomManager.changeMap(sessionKey, strMapId);
			if ( logger.isDebugEnabled() ) {
				if ( success ) {
					User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
					logger.debug("User {} will change combat map to {}", user.getRoleName(), strMapId);
				}
			}
		} else {
			//Choose random map
			roomManager.changeMap(sessionKey, null);
			BseChangeMap.Builder builder = BseChangeMap.newBuilder();
			builder.setMapID(-1);
			builder.setChangeUserID(sessionKey.toString());
			GameContext.getInstance().writeResponse(sessionKey, builder.build());
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		StatClient.getIntance().sendDataToStatServer(user, StatAction.ChangeMap, mapId);
	}
	
	
}
