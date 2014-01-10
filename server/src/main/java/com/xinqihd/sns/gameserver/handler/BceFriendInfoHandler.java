package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceFriendInfo.BceFriendInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseFriendsInfo.BseFriendsInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The BceFriendInfoHandler is used for protocol FriendInfo 
 * @author wangqi
 *
 */
public class BceFriendInfoHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceFriendInfoHandler.class);
	
	private static final BceFriendInfoHandler instance = new BceFriendInfoHandler();
	
	private BceFriendInfoHandler() {
		super();
	}

	public static BceFriendInfoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceFriendInfo");
		}
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceFriendInfo friendInfo = (BceFriendInfo)request.payload;
		String friendName = friendInfo.getUsername();
		if ( StringUtil.checkNotEmpty(friendName) ) {
			User friend = UserManager.getInstance().queryUserByRoleName(friendName);
			if ( friend != null ) {
				People people = null;
				Collection<Relation> relations = user.getRelations();
				for ( Relation relation : relations ) {
					people = relation.findPeopleByUserName(friend.getUsername());
					if ( people != null ) {
						break;
					}
				}
				if ( people != null ) {
					BseFriendsInfo.Builder builder = BseFriendsInfo.newBuilder();
					builder.setFriBlood(friend.getBlood());
					builder.setFriThew(friend.getTkew());
					builder.setFriDamage(friend.getDamage());
					builder.setFriSkin(friend.getSkin());
					builder.setFriName(friendName);
					
					XinqiMessage response = new XinqiMessage();
					response.payload = builder.build();
					
					GameContext.getInstance().writeResponse(user.getSessionKey(), response);
				} else {
					logger.debug("Cannot find friend in user's relations by name: {}", friendName);
				}
			} else {
				logger.debug("Cannot find User object in mongodb by name: {}", friendName);
			}			
		} else {
			logger.debug("Cannot find friend by name: {}", friendName);
		}
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.FriendInfo, friendName);
	}
	
	
}
