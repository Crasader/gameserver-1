package com.xinqihd.sns.gameserver.db.mongo;

import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceConfirm.BceConfirm;
import com.xinqihd.sns.gameserver.proto.XinqiBseConfirm.BseConfirm;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * Process user's confirm
 * @author wangqi
 *
 */
public class ConfirmManager {

	private static final Logger logger = LoggerFactory.getLogger(ConfirmManager.class);
	
	private static final String CONFIRM_KEY = "_confirm_key";

	private static final ConfirmManager instance = new ConfirmManager();
	
	public ConfirmManager() {
	}
	
	public static ConfirmManager getInstance() {
		return instance;
	}
	
	/**
	 * Send the user an confirm logic
	 * @param user
	 */
	public void sendConfirmMessage(User user, String message, String type, ConfirmCallback callback) {
		sendConfirmMessage(user, user.getSessionKey(), message, type, callback);
	}
	
	/**
	 * Send the user an confirm logic
	 * @param user
	 */
	public void sendConfirmMessage(User user, SessionKey toUserSessionKey, String message, String type, ConfirmCallback callback) {
		BseConfirm.Builder builder = BseConfirm.newBuilder();
		builder.setType(type);
		builder.setMessage(message);
		builder.setUsersession(user.getSessionKey().toString());
		GameContext.getInstance().writeResponse(toUserSessionKey, builder.build());
		
		if ( callback != null ) {
			HashMap<String, ConfirmCallback> callbackMap = (HashMap<String, ConfirmCallback>)
					user.getUserData(CONFIRM_KEY);
			if ( callbackMap == null ) {
				callbackMap = new HashMap<String, ConfirmCallback>();
				user.putUserData(CONFIRM_KEY, callbackMap);
			}
			callbackMap.put(type, callback);
		}
	}
	
	/**
	 * Send the user an confirm logic
	 * @param user
	 */
	public void sendConfirmMessage(User user, IoSession session, String message, String type, ConfirmCallback callback) {
		BseConfirm.Builder builder = BseConfirm.newBuilder();
		builder.setType(type);
		builder.setMessage(message);
		builder.setUsersession(user.getSessionKey().toString());
		GameContext.getInstance().writeResponse(session, builder.build(), null);
		
		if ( callback != null ) {
			HashMap<String, ConfirmCallback> callbackMap = (HashMap<String, ConfirmCallback>)
					user.getUserData(CONFIRM_KEY);
			if ( callbackMap == null ) {
				callbackMap = new HashMap<String, ConfirmCallback>();
				user.putUserData(CONFIRM_KEY, callbackMap);
			}
			callbackMap.put(type, callback);
		}
	}
	
	/**
	 * Receive the confirm message. Try to call the callback.
	 * If the callback is not found, maybe the user lost his connection to the
	 * previous server and connected again to a new server. We should prompt 
	 * user to retry his operation.
	 * 
	 * @param user
	 * @param type
	 * @param selected 0:cancel, 1:yes, 2:no
	 */
	public void receiveConfirmMessage(User user, String type, int selected) {
		receiveConfirmMessage(user, type, selected, user.getSessionKey());
	}
	
	/**
	 * Receive the confirm message. Try to call the callback.
	 * If the callback is not found, maybe the user lost his connection to the
	 * previous server and connected again to a new server. We should prompt 
	 * user to retry his operation.
	 * 
	 * @param user
	 * @param type
	 * @param selected
	 * @param targetUserSessionKey
	 */
	public void receiveConfirmMessage(User user, String type, int selected, SessionKey targetUserSessionKey) {
		String targetGameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(targetUserSessionKey);
		if ( GameContext.getInstance().getGameServerId().equals(targetGameServerId) ) {
			//it is local server confirm request
			boolean notfound = true;
			User targetUser = GameContext.getInstance().findLocalUserBySessionKey(targetUserSessionKey);
			if ( targetUser == null ) {
				logger.debug("#receiveConfirmMessage: not found the target user by sessionKey:{}", targetUserSessionKey);
			} else {
				HashMap<String, ConfirmCallback> callbackMap = (HashMap<String, ConfirmCallback>)
						targetUser.getUserData(CONFIRM_KEY);
				if ( callbackMap != null ) {
					ConfirmCallback callback = callbackMap.get(type);
					if ( callback != null ) {
						callback.callback(targetUser, selected);
						notfound = false;
						if ( logger.isDebugEnabled() ) {
							logger.debug("User {} confirm the type {} with {}", new Object[]{
									targetUser.getUsername(), type, selected
							});
						}
					} else {
						logger.debug("User {} callbackMap does not have the type {}", targetUser.getRoleName(), type);
					}
				} else {
					logger.debug("User {} does not have the callback map", targetUser.getRoleName());
				}
				
				/*
				if ( notfound ) {
					SysMessageManager.getInstance().sendClientInfoMessage(targetUser, "confirm.error", Type.NORMAL);
				}
				*/
			}
		} else {
			BceConfirm.Builder builder = BceConfirm.newBuilder();
			builder.setUsersession(targetUserSessionKey.toString());
			builder.setSelected(selected);
			builder.setType(type);
			GameContext.getInstance().proxyToRemoteGameServer(targetUserSessionKey, targetGameServerId, builder.build());
		}
	}
	
	/**
	 * The confirm callback interface
	 * @author wangqi
	 *
	 */
	public static interface ConfirmCallback {
		
		public void callback(User user, int selected);
	}
	
	public static enum ConfirmResult {
		CANCEL,
		YES,
		NO,
	}
}
