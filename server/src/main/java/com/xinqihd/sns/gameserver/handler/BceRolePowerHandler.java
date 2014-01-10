package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.proto.XinqiBceRolePower.BceRolePower;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceRolePowerHandler is used for protocol RolePower 
 * @author wangqi
 *
 */
public class BceRolePowerHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceRolePowerHandler.class);
	
	private static final BceRolePowerHandler instance = new BceRolePowerHandler();
	
	private BceRolePowerHandler() {
		super();
	}

	public static BceRolePowerHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceRolePower");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceRolePower power = (BceRolePower)request.payload;
		
		GameContext.getInstance().getBattleManager().rolePower(sessionKey, power);
	}
	
	
}
