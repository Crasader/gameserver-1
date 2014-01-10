package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBattlePickBox.BceBattlePickBox;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceBattlePickBoxHandler is used for protocol BattlePickBox 
 * @author wangqi
 *
 */
public class BceBattlePickBoxHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBattlePickBoxHandler.class);
	
	private static final BceBattlePickBoxHandler instance = new BceBattlePickBoxHandler();
	
	private BceBattlePickBoxHandler() {
		super();
	}

	public static BceBattlePickBoxHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBattlePickBox");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBattlePickBox pickBox = (BceBattlePickBox)request.payload;
		
		GameContext.getInstance().getBattleManager().pickTreasureBox(sessionKey, pickBox);
		
	}
	
	
}
