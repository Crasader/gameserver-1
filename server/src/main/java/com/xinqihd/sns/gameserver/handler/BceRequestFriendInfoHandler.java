package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceRequestFriendInfo.BceRequestFriendInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseRequestFriendInfo.BseRequestFriendInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceRequestFriendInfoHandler is used for protocol RequestFriendInfo 
 * @author wangqi
 *
 */
public class BceRequestFriendInfoHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceRequestFriendInfoHandler.class);
	
	private static final BceRequestFriendInfoHandler instance = new BceRequestFriendInfoHandler();
	
	private BceRequestFriendInfoHandler() {
		super();
	}

	public static BceRequestFriendInfoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceRequestFriendInfo");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceRequestFriendInfo friendInfo = (BceRequestFriendInfo)request.payload;
		String friendName = friendInfo.getUsername();
		int type = friendInfo.getType();
		RelationType relationType = null;
		if ( type>=0 && type<RelationType.values().length ) {
			relationType = RelationType.values()[type];
			
			Relation relation = user.getRelation(relationType);
			if ( relation != null ) {
				User friend = UserManager.getInstance().queryUserByRoleName(friendName);
				
				People people = relation.findPeopleByUserName(friend.getUsername());
				if ( people != null ) {
					
					XinqiMessage response = new XinqiMessage();
					BseRequestFriendInfo.Builder builder = BseRequestFriendInfo.newBuilder();
					builder.setFriendInfo(friend.toFriendInfoLite(relationType));
					builder.setType(relationType.ordinal());
					
					response.payload = builder.build();
					
					GameContext.getInstance().writeResponse(user.getSessionKey(), response);
					
					StatClient.getIntance().sendDataToStatServer(user, 
							StatAction.RequestFriendInfo, friend.getRoleName());
					
					logger.info("Get user {}'s friend info lite", user.getRoleName());
				} else {
					logger.info("{} is not user's friend in type: {}.", friendName, relationType);
				}
			} else {
				logger.info("Cannot find user {}'s relation for type: {}", user.getRoleName(), relationType);
			}
		} else {
			logger.info("Invalid relationType index: {}", type);
		}
	}
	
	
}
