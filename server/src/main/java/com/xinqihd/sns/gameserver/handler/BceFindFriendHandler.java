package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceFindFriend.BceFindFriend;
import com.xinqihd.sns.gameserver.proto.XinqiBseFindFriend.BseFindFriend;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The user search his friend by full nickname.
 * 
 * @author wangqi
 *
 */
public class BceFindFriendHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceFindFriendHandler.class);
	
	private static final BceFindFriendHandler instance = new BceFindFriendHandler();
	
	private BceFindFriendHandler() {
		super();
	}

	public static BceFindFriendHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceFindFriend");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceFindFriend findFriend = (BceFindFriend)request.payload;
		
		BseFindFriend.Builder builder = BseFindFriend.newBuilder();
		boolean foundFriend = false;		
		/**
		 * The client does not support friend list
		 * 2012-12-27
		 */
		/*
		ArrayList<BasicUser> friends = UserManager.getInstance().queryBasicUserByRoleNameRegex(findFriend.getNickname());
		if ( friends.size() == 0 ) {
			String info = Text.text("friend.notfound", findFriend.getNickname());
			builder.setResult(1);
			builder.setMessage(info);
		} else {
			foundFriend = true;
			builder.setResult(1);
			String info = Text.text("friend.found", findFriend.getNickname());
			builder.setMessage(info);
			for ( BasicUser friend : friends ) {
				builder.addUserid(friend.get_id().toString());
			}
		}
		*/
		BasicUser friend = UserManager.getInstance().queryBasicUserByRoleName(findFriend.getNickname());
		if ( friend == null ) {
			String info = Text.text("friend.notfound", findFriend.getNickname());
			builder.setResult(1); 
			builder.setMessage(info);
		} else {
			foundFriend = true;
			builder.setResult(1);
			String info = Text.text("friend.found", findFriend.getNickname());
			builder.setMessage(info);
			builder.addUserid(friend.get_id().toString());
		}
		GameContext.getInstance().writeResponse(sessionKey, builder.build());
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.FindFriend, findFriend.getNickname(), foundFriend);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
				UserActionKey.FindFriend);
	}
	
	public void processChat(User user, XinqiMessage request) {
		BceChat chat = (BceChat)request.payload;
		ChatManager.getInstance().processChatAsyn(user, chat);
	}
	
}
