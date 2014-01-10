package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserRefresh.BceUserRefresh;
import com.xinqihd.sns.gameserver.proto.XinqiBseToolList.BseToolList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The user should refresh his status from database.
 * 
 * 1: refresh user data
 * 2: refresh user bag data
 * 4: refresh user relation
 * 
 * @author wangqi
 *
 */
public class BceUserRefreshHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceUserRefreshHandler.class);
	
	private static final BceUserRefreshHandler instance = new BceUserRefreshHandler();
	
	private BceUserRefreshHandler() {
		super();
	}

	public static BceUserRefreshHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceUserRefresh");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceUserRefresh userRefresh = (BceUserRefresh)request.payload;
		int mode = userRefresh.getRefreshmode();
		if ( (mode & 0x1) == 0x1 ) {
			logger.debug("refresh user {} basic data.", user.getRoleName());
			User newUser = UserManager.getInstance().queryUser(user.get_id());
			GameContext.getInstance().updateLocalUserBySessionKey(sessionKey, newUser);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleInfo());
			
			//Only re-sync the bufftool info
			BseToolList bseToolList = user.toBseToolList();
			XinqiMessage xinqi = new XinqiMessage();
			xinqi.payload = bseToolList;
			GameContext.getInstance().writeResponse(user.getSessionKey(), xinqi);
		}
		if ( (mode & 0x2) == 0x2 ) {
			logger.debug("refresh user {} bag data.", user.getRoleName());
			UserManager.getInstance().queryUserBag(user);
			GameContext.getInstance().writeResponse(user.getSessionKey(), user.toBseRoleBattleInfo(true));
		}
		if ( (mode & 0x4) == 0x4 ) {
			logger.debug("refresh user {} relation data.", user.getRoleName());
			UserManager.getInstance().queryUserRelation(user);
		}
		if ( (mode & 0x8) == 0x8 ) {
			logger.debug("refresh user {} unlock data.", user.getRoleName());
			UserManager.getInstance().queryUserUnlock(user);
		}
		if ( (mode & 0x16) == 0x16 ) {
			logger.debug("refresh user {} task data.", user.getRoleName());
			user.clearTasks();
			TaskManager.getInstance().getUserLoginTasks(user);
		}
	}
	
	public void processChat(User user, XinqiMessage request) {
		BceChat chat = (BceChat)request.payload;
		ChatManager.getInstance().processChatAsyn(user, chat);
	}
	
}
