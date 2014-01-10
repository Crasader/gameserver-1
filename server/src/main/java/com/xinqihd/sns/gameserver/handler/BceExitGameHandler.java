package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.CaishenManager;
import com.xinqihd.sns.gameserver.db.mongo.ExitGameManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseExitGame.BseExitGame;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.treasure.TreasureHuntManager;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 
 * @author wangqi
 *
 */
public class BceExitGameHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceExitGameHandler.class);
	
	private static final BceExitGameHandler instance = new BceExitGameHandler();
	
	private BceExitGameHandler() {
		super();
	}

	public static BceExitGameHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceExitGame");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		ExitGameManager.getInstance().exitGame(user, session, sessionKey);
	}
	
}
