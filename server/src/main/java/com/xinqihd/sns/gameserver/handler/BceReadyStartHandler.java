package com.xinqihd.sns.gameserver.handler;


import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceReadyStart.BceReadyStart;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceReadyStartHandler is used for protocol ReadyStart 
 * @author wangqi
 *
 */
public class BceReadyStartHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceReadyStartHandler.class);
	
	private static final BceReadyStartHandler instance = new BceReadyStartHandler();
	
	private BceReadyStartHandler() {
		super();
	}

	public static BceReadyStartHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceReadyStart");
		}
		
		if ( ! (message instanceof XinqiMessage) ) {
			return;
		}
		XinqiMessage request = (XinqiMessage)message;
		BceReadyStart readyStart = (BceReadyStart)request.payload;
		
		User user = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
		//Check if the given user bear a weapon first.
		Bag bag = user.getBag();
		PropData weapon = bag.getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		boolean success = false;
		if ( weapon == null ) {
			SysMessageManager.getInstance().sendClientInfoMessage(
					user, "prompt.room.noweapon", Type.NORMAL);
			logger.debug("User {} has no weapon and cannot be ready for battle.", user.getRoleName());
		} else if ( weapon.isExpire() ) {
			SysMessageManager.getInstance().sendClientInfoMessage(user, 
					"prompt.room.weaponexpire", Action.NOOP, Type.NORMAL, new Object[]{weapon.getName()});
			logger.debug("User {} weapon is expired and cannot be ready for battle.", user.getRoleName());			
		} else {
			if ( sessionKey == null ) {
				logger.warn("User session is null. ");
				return;
			}
			RoomManager roomManager = GameContext.getInstance().getRoomManager();
			roomManager.readyStart(sessionKey, readyStart.getIsReady());
			success = true;
		}
		/*
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.ReadyStart, readyStart.getIsReady(), success);
    */
	}
	

}
