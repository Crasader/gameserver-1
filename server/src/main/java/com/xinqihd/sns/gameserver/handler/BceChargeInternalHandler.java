package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.BillingJedis;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceChargeInternalHandler is used to put yuanbao into user's account.
 * It is mainly used by game server to communicate.
 * 
 * @author wangqi
 *
 */
public class BceChargeInternalHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceChargeInternalHandler.class);
	
	private static final BceChargeInternalHandler instance = new BceChargeInternalHandler();
	
	private BceChargeInternalHandler() {
		super();
	}

	public static BceChargeInternalHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		logger.debug("->BceChargeHandlerInternal");
		
		XinqiMessage request = (XinqiMessage)message;
		BceChargeInternal bceCharge = (BceChargeInternal)request.payload;
		boolean freeChargeMode = bceCharge.getFreecharge();		
		int moneyCount = bceCharge.getChargemoney();
		String channel = bceCharge.getChannel();
		String orderId = bceCharge.getOrderid();
		
		String userIdStr = bceCharge.getUserid();
		logger.info("UserId {}, money:{}, channel:{}, orderId:{}", new Object[]{userIdStr, moneyCount, channel, orderId});
		boolean charged = false;
		if ( StringUtil.checkNotEmpty(userIdStr) ) {
			UserId userId = UserId.fromString(userIdStr);
			SessionKey userSessionKey = GameContext.getInstance().getSessionManager().findSessionKeyByUserId(userId);
			if ( userSessionKey == null ) {
				userSessionKey = BillingJedis.getInstance().findSessionKeyByUserId(userId);
			}
			if ( userSessionKey != null ) {
				logger.info("Charge user {} is online", userSessionKey);
				//The user is online now
				String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(userSessionKey);
				if ( GameContext.getInstance().getGameServerId().equals(gameServerId) )  {
					//The user is online
					User user = GameContext.getInstance().findLocalUserBySessionKey(userSessionKey);
					if ( user != null ) {
						logger.info("Charge {} rmb to user {}", moneyCount, user.getRoleName());
						charge(bceCharge, freeChargeMode, moneyCount, user, channel, orderId);
						charged = true;
					} else {
						logger.warn("Charge {} rmb to user {} at remote server.", moneyCount, user.getRoleName());
					}
				} else {
					logger.info("Proxy charge request to remote server {}", gameServerId);
					BceChargeInternal.Builder Builder = BceChargeInternal.newBuilder();
					Builder.setUserid(userId.toString());
					Builder.setFreecharge(true);
					Builder.setChargemoney(moneyCount);
					Builder.setChannel(channel);
					Builder.setOrderid(orderId);
					GameContext.getInstance().proxyToRemoteGameServer(userSessionKey, gameServerId, Builder.build());
				}
			}
			
			if ( !charged ) {
				//The user is offline.
				User user = UserManager.getInstance().queryUser(userId);
				UserManager.getInstance().queryUserBag(user);
				if ( user != null ) {
					logger.info("Charge user {} is offline.", user.getRoleName());
					charge(bceCharge, freeChargeMode, moneyCount, user, channel, orderId);	
				} else {
					logger.warn("#Failed to find the user by id: {}", userId);
				}
			}
		} else {
			logger.warn("The userId {} is empty. ", userIdStr);
			return;
		}
	}

	/**
	 * @param bceCharge
	 * @param freeChargeMode
	 * @param moneyCount
	 * @param user
	 */
	private void charge(BceChargeInternal bceCharge, boolean freeChargeMode,
			int moneyCount, User user, String channel, String orderId) {
		if ( freeChargeMode ) {
			int chargeMoney = bceCharge.getChargemoney();
			ChargeManager.getInstance().doCharge(user.getSessionKey(), user,
					orderId, null, chargeMoney, null, true);
		} else {
			int id = bceCharge.getChargeid();
			orderId = bceCharge.getOrderid();
			ChargePojo chargePojo = ChargeManager.getInstance().getCharePojoById(id);
			if ( chargePojo != null ) {
				ChargeManager.getInstance().doCharge(
						null, user, orderId, chargePojo, moneyCount, channel, true);
			} else {
				logger.warn("#Failed to find the given chargepojo by {}", id);
			}
		}
	}
	
}
