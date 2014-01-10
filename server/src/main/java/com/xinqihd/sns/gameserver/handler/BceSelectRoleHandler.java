package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceSelectRole.BceSelectRole;
import com.xinqihd.sns.gameserver.proto.XinqiBseInit.BseInit;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 
 * @author wangqi
 *
 */
public class BceSelectRoleHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceSelectRoleHandler.class);
	
	private static final BceSelectRoleHandler instance = new BceSelectRoleHandler();
	
	private BceSelectRoleHandler() {
		super();
	}

	public static BceSelectRoleHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceSelectRole");
		}

		XinqiMessage request = (XinqiMessage)message;
		BceSelectRole selectRole = (BceSelectRole)(request.payload);
		String serverId = selectRole.getServerid();
		String userIdStr = selectRole.getUserid();
		UserId userId = UserId.fromString(userIdStr);
		Account account = (Account)session.getAttribute(AccountManager.IOSESSION_ACCOUNT);
		if ( account == null ) {
			String token = selectRole.getToken();
			if ( StringUtil.checkNotEmpty(token) ) {
				account = CipherManager.getInstance().checkEncryptedAccountToken(token);
			} else {
				logger.info("account token is null");
			}
		}
		if ( account == null ) {
			BseInit.Builder bseInit = BseInit.newBuilder();
			bseInit.setSuccess(false);
			GameContext.getInstance().writeResponse(session, bseInit.build(), sessionKey);
		} else {
			session.setAttribute(AccountManager.IOSESSION_ACCOUNT, account);
			AccountManager.getInstance().selectRole(session, sessionKey, account, userId, serverId,
					selectRole.getUuid(), selectRole.getScreen(), selectRole.getDevicetoken(), selectRole.getClient(),
					selectRole.getLang(), account.getChannel());
		}
	}
	
}
