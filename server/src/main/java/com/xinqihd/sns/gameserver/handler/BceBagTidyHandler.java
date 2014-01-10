package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceBagTidyHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBagTidyHandler.class);
	
	private static final BceBagTidyHandler instance = new BceBagTidyHandler();
	
	private BceBagTidyHandler() {
		super();
	}

	public static BceBagTidyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBagTidy");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		Bag bag = user.getBag();
		bag.tidyUserBag();
		GameContext.getInstance().getUserManager().saveUserBag(user, true);
		GameContext.getInstance().writeResponse(sessionKey, user.toBseRoleBattleInfo(true));
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.BagTidy, 
				bag.getCurrentCount(), bag.getMaxCount());
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), UserActionKey.BagTidy);
	}
	
	
}
