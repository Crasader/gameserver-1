package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChangeAutomode.BceChangeAutomode;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 
 * @author wangqi
 *
 */
public class BceChangeAutomodeHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceChangeAutomodeHandler.class);
	
	private static final BceChangeAutomodeHandler instance = new BceChangeAutomodeHandler();
	
	private BceChangeAutomodeHandler() {
		super();
	}

	public static BceChangeAutomodeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceChangeAutomode");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		BceChangeAutomode autoMode = (BceChangeAutomode)request.payload;
		RoomManager.getInstance().changeAutomode(sessionKey, autoMode.getAutomode());
	}
	
}
