package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.proto.XinqiBceOfflinePush.BceOfflinePush;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceChargeInternalHandler is used to put yuanbao into user's account.
 * It is mainly used by game server to communicate.
 * 
 * @author wangqi
 *
 */
public class BceOfflinePushHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceOfflinePushHandler.class);
	
	private static final BceOfflinePushHandler instance = new BceOfflinePushHandler();
	
	private BceOfflinePushHandler() {
		super();
	}

	public static BceOfflinePushHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		logger.debug("->BceOfflinePush");
		
		XinqiMessage request = (XinqiMessage)message;
		BceOfflinePush push = (BceOfflinePush)request.payload;
		
		String userIdStr = push.getUserid();
		String roleName = push.getRolename();
		String text = push.getMessage();
		User user = null;
		if ( StringUtil.checkNotEmpty(userIdStr) ) {
			UserId userId = UserId.fromString(userIdStr);
			user = UserManager.getInstance().queryUser(userId);
		}
		if ( user == null ) {
			user = UserManager.getInstance().queryUserByRoleName(roleName);
		}
		if ( user == null ) {
			logger.debug("Failed to find the user by userId {} or rolename {}", userIdStr, roleName);
		} else {
			if ( StringUtil.checkNotEmpty(text) ) { 
				MailMessageManager.getInstance().pushSimpleMessage(user.getRoleName(), user.getDeviceToken(), text, 1800);
				logger.debug("Success to send the offline push message to {}", user.getRoleName());
			}
		}
	}
	
}
