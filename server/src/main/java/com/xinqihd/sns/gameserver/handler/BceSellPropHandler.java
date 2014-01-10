package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceSellProp;
import com.xinqihd.sns.gameserver.proto.XinqiBceSellProp.BceSellProp;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceSellPropHandler is used for protocol SellProp 
 * @author wangqi
 *
 */
public class BceSellPropHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceSellPropHandler.class);
	
	private static final BceSellPropHandler instance = new BceSellPropHandler();
	
	private BceSellPropHandler() {
		super();
	}

	public static BceSellPropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceSellProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		
		XinqiBceSellProp.BceSellProp sellProp = (BceSellProp) request.payload;
		int pew = sellProp.getPropPew();
		int count = sellProp.getCount();
		/**
		 * 1. 物品出售给系统
		 */
    User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
    
    ShopManager.getInstance().sellGoodToShop(user, pew, count);
	}
}
