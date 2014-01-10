package com.xinqihd.sns.gameserver.handler;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserStatusList.BceUserStatusList;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserStatusList.BseUserStatusList;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The game will display status message to all users to improve the game
 * active status.
 * 
 * @author wangqi
 *
 */
public class BceUserStatusListHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceUserStatusListHandler.class);
	
	private static final BceUserStatusListHandler instance = new BceUserStatusListHandler();
	
	private BceUserStatusListHandler() {
		super();
	}

	public static BceUserStatusListHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceUserStatusList");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BceUserStatusList statusList = (BceUserStatusList)request.payload;
		int limit = statusList.getLimit();
		List<String> userActions = UserActionManager.getInstance().getUserActions(limit);
		BseUserStatusList.Builder builder = BseUserStatusList.newBuilder();
		if ( userActions != null ) {
			for ( String userAction : userActions ) {
				builder.addStatuslist(userAction);
			}
		}
		GameContext.getInstance().writeResponse(sessionKey, builder.build());
	}
	
}
