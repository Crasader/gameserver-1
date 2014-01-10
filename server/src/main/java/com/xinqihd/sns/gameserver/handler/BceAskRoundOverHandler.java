package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceAskRoundOver.BceAskRoundOver;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceAskRoundOverHandler is used for protocol AskRoundOver 
 * @author wangqi
 *
 */
public class BceAskRoundOverHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceAskRoundOverHandler.class);
	
	private static final BceAskRoundOverHandler instance = new BceAskRoundOverHandler();
	
	private BceAskRoundOverHandler() {
		super();
	}

	public static BceAskRoundOverHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceAskRoundOver");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceAskRoundOver askRoundOver = (BceAskRoundOver)request.payload;
		GameContext.getInstance().getBattleManager().roundOver(sessionKey, askRoundOver);

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		//StatClient.getIntance().sendDataToStatServer(user, StatAction.AskRoundOver);
	}
	
	
}
