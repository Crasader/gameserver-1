package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyVip.BceBuyVip;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceBuyVipHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceBuyVipHandler.class);
	
	private static final BceBuyVipHandler instance = new BceBuyVipHandler();
	
	private BceBuyVipHandler() {
		super();
	}

	public static BceBuyVipHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceBuyVipHandler");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BceBuyVip bceBuyVip = (BceBuyVip)request.payload;
		int month = bceBuyVip.getMonth();
		int payType = bceBuyVip.getPayType();
		
		VipManager.getInstance().userBuyVipPeriod(user, month, payType);
	}
	
	
}
