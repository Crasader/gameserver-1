package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.proto.XinqiBceServerList.BceServerList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 *  
 * @author wangqi
 *
 */
public class BceServerListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceServerListHandler.class);
	
	private static final BceServerListHandler instance = new BceServerListHandler();
	
	private BceServerListHandler() {
		super();
	}

	public static BceServerListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceServerList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceServerList serverList = (BceServerList)request.payload;
		String accountId = serverList.getAccountId();
		String accountName = serverList.getAccountname();
		Account account = null;
		if ( accountId != null ) {
			account = AccountManager.getInstance().queryAccountById(accountId);
			if ( account == null ) {
				account = AccountManager.getInstance().queryAccountByName(accountName);
			}
		} else {
			if ( account == null ) {
				account = AccountManager.getInstance().queryAccountByName(accountName);
			}
		}
		if ( account == null ) {
			logger.warn("Failed to find the account");
		}
		
		/**
		 * Send client the server list
		 */
		GameContext.getInstance().writeResponse(
				session, ServerListManager.getInstance().toBseServerList(account), null);

	}
	
}
