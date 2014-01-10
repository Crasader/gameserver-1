package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceShowUserInfo.BceShowUserInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseShowUserInfo.BseShowUserInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceShowUserInfoHandler is used for protocol ShowUserInfo 
 * @author wangqi
 *
 */
public class BceShowUserInfoHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceShowUserInfoHandler.class);
	
	private static final BceShowUserInfoHandler instance = new BceShowUserInfoHandler();
	
	private BceShowUserInfoHandler() {
		super();
	}

	public static BceShowUserInfoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceShowUserInfo");
		}
		
		/**
		 * TODO add expire device data
		 */
		
		XinqiMessage request = (XinqiMessage)message;
		BceShowUserInfo userInfo = (BceShowUserInfo)request.payload;
		String userIdStr = userInfo.getUid();
		UserId userId = UserId.fromString(userIdStr);
		String aiRoleName = userId.getUserName();
		SessionKey targetUserSessionKey = GameContext.getInstance().getSessionManager().findSessionKeyByUserId(userId);
		User targetUser = null;
		if ( targetUserSessionKey != null ) {
			targetUser = GameContext.getInstance().findGlobalUserBySessionKey(targetUserSessionKey);
		} else {
			targetUser = UserManager.getInstance().queryUser(userId);
		}
		if ( targetUser == null ) {
			targetUser = UserManager.getInstance().queryUser(aiRoleName);
		}
		if ( targetUser != null ) {
			UserManager.getInstance().queryUserBag(targetUser);
			
			XinqiMessage response = new XinqiMessage();
			BseShowUserInfo.Builder builder = BseShowUserInfo.newBuilder();
			builder.setVisitedUid(userIdStr);
			for ( PropData propData : targetUser.getBag().getWearPropDatas() ) {
				if ( propData != null ) {
					builder.addEquips(propData.toXinqiPropData(targetUser));
				}
			}
			builder.setGender(targetUser.getGender().ordinal());
			builder.setData(targetUser.toUserData());
			builder.setExdata(targetUser.toUserExData());
			builder.setName(targetUser.getRoleName());
			
			response.payload = builder.build();
			
			GameContext.getInstance().writeResponse(sessionKey, response);
			
			User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
			StatClient.getIntance().sendDataToStatServer(user, 
					StatAction.ShowUserInfo, targetUser.getRoleName());
		} else {
			logger.warn("Cannot find user by id: {}", userIdStr);
		}
	}
	
	
}
