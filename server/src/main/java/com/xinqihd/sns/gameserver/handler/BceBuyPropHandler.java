package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyProp.BceBuyProp;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 玩家购买道具的处理
 * 
 * @author jsding
 *
 */
public class BceBuyPropHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBuyPropHandler.class);
	
	private static final BceBuyPropHandler instance = new BceBuyPropHandler();
	
	private BceBuyPropHandler() {
		super();
	}

	public static BceBuyPropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBuyProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceBuyProp buyProp = (BceBuyProp)request.payload;
		int buyCount = buyProp.getBuyListCount();
		
    User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);

    GameContext.getInstance().getShopManager().buyGoodFromShop(user, buyProp);
	}
	
	
}
