package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceForge.BceForge;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceForgeHandler is used for protocol Forge 
 * @author wangqi
 *
 */
public class BceForgeHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceForgeHandler.class);
	
	private static final BceForgeHandler instance = new BceForgeHandler();
	
	private BceForgeHandler() {
		super();
	}

	public static BceForgeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceForge");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceForge forge = (BceForge)request.payload;
		int equipPew = forge.getEquipPew();
		int[] pews = new int[forge.getOtherPewsCount()];
		for ( int i=0; i<pews.length; i++ ) {
			pews[i] = forge.getOtherPews(i);
		}
		
		//The User object should not be null because GameHandler is checking it.
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getCraftManager().forgeEquip(user, equipPew, pews);
	}
	
	
}
