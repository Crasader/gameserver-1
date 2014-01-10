package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceCharge.BceCharge;
import com.xinqihd.sns.gameserver.proto.XinqiBseCharge.BseCharge;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceChargeHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceChargeHandler.class);
	
	private static final BceChargeHandler instance = new BceChargeHandler();
	
	private BceChargeHandler() {
		super();
	}

	public static BceChargeHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
	 	if ( logger.isDebugEnabled() ) {
			logger.debug("->BceChargeHandler");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceCharge bceCharge = (BceCharge)request.payload;
		boolean freeChargeMode = bceCharge.getFreecharge();
		boolean success = false;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		if ( user == null ) {
			logger.warn("Failed to find user by local session key: {}", sessionKey);
			XinqiMessage response = new XinqiMessage();
	    BseCharge.Builder builder = BseCharge.newBuilder();
	    builder.setSuccess(false);
	    GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
	    return;
		}
		String token = bceCharge.getToken();
		if ( freeChargeMode ) {
			int chargeMoney = bceCharge.getChargemoney();
			success = true;
			ChargeManager.getInstance().freeCharge(user, chargeMoney, token);
		} else {
			int id = bceCharge.getChargeid();
			success = ChargeManager.getInstance().userChargeMoney(session, user, id, bceCharge.getInvoiceid(), token);
		}
		
		XinqiMessage response = new XinqiMessage();
    BseCharge.Builder builder = BseCharge.newBuilder();
    builder.setSuccess(success);
    builder.setYuanbao(user.getYuanbao());
    String promptMessage = null;

    GameContext.getInstance().writeResponse(session, builder.build(), sessionKey);
	}
	
}
