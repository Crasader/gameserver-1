package com.xinqihd.sns.gameserver.db.mongo;

import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserInput.BceUserInput;
import com.xinqihd.sns.gameserver.proto.XinqiBseUserInput.BseUserInput;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * Process user's input
 * @author wangqi
 *
 */
public class UserInputManager {

	private static final Logger logger = LoggerFactory.getLogger(UserInputManager.class);
	
	private static final String INPUT_KEY = "_input_key";

	private static final UserInputManager instance = new UserInputManager();
	
	public UserInputManager() {
	}
	
	public static UserInputManager getInstance() {
		return instance;
	}
	
	/**
	 * Send the user an input logic
	 * @param user
	 */
	public void sendInputMessage(User user, String title, String message, String type, InputCallback callback) {
		sendInputMessage(user, user.getSessionKey(), title, message, type, callback);
	}
	
	/**
	 * Send the user an input logic
	 * @param user
	 */
	public void sendInputMessage(User user, SessionKey toUserSessionKey, String title, String message, 
			String type, InputCallback callback) {
		BseUserInput.Builder builder = BseUserInput.newBuilder();
		builder.setType(type);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setUsersession(user.getSessionKey().toString());
		GameContext.getInstance().writeResponse(toUserSessionKey, builder.build());
		
		if ( callback != null ) {
			HashMap<String, InputCallback> callbackMap = (HashMap<String, InputCallback>)
					user.getUserData(INPUT_KEY);
			if ( callbackMap == null ) {
				callbackMap = new HashMap<String, InputCallback>();
				user.putUserData(INPUT_KEY, callbackMap);
			}
			callbackMap.put(type, callback);
		}
	}
	
	/**
	 * Send the user an input logic
	 * @param user
	 */
	public void sendInputMessage(User user, IoSession session, String message, String type, InputCallback callback) {
		BseUserInput.Builder builder = BseUserInput.newBuilder();
		builder.setType(type);
		builder.setMessage(message);
		builder.setUsersession(user.getSessionKey().toString());
		GameContext.getInstance().writeResponse(session, builder.build(), null);
		
		if ( callback != null ) {
			HashMap<String, InputCallback> callbackMap = (HashMap<String, InputCallback>)
					user.getUserData(INPUT_KEY);
			if ( callbackMap == null ) {
				callbackMap = new HashMap<String, InputCallback>();
				user.putUserData(INPUT_KEY, callbackMap);
			}
			callbackMap.put(type, callback);
		}
	}
	
	/**
	 * Receive the input message. Try to call the callback.
	 * If the callback is not found, maybe the user lost his connection to the
	 * previous server and connected again to a new server. We should prompt 
	 * user to retry his operation.
	 * 
	 * @param user
	 * @param type
	 * @param selected 0:cancel, 1:yes, 2:no
	 */
	public void receiveInputMessage(User user, String type, String userInput) {
		receiveInputMessage(user, type, userInput, user.getSessionKey());
	}
	
	/**
	 * Receive the input message. Try to call the callback.
	 * If the callback is not found, maybe the user lost his connection to the
	 * previous server and connected again to a new server. We should prompt 
	 * user to retry his operation.
	 * 
	 * @param user
	 * @param type
	 * @param selected
	 * @param targetUserSessionKey
	 */
	public void receiveInputMessage(User user, String type, String userInput, SessionKey targetUserSessionKey) {
		String targetGameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(targetUserSessionKey);
		if ( GameContext.getInstance().getGameServerId().equals(targetGameServerId) ) {
			//it is local server input request
			boolean notfound = true;
			User targetUser = GameContext.getInstance().findLocalUserBySessionKey(targetUserSessionKey);
			if ( targetUser == null ) {
				logger.debug("#receiveinputMessage: not found the target user by sessionKey:{}", targetUserSessionKey);
			} else {
				HashMap<String, InputCallback> callbackMap = (HashMap<String, InputCallback>)
						targetUser.getUserData(INPUT_KEY);
				if ( callbackMap != null ) {
					InputCallback callback = callbackMap.get(type);
					if ( callback != null ) {
						callback.callback(targetUser, userInput);
						notfound = false;
						if ( logger.isDebugEnabled() ) {
							logger.debug("User {} input the type {} with {}", new Object[]{
									targetUser.getUsername(), type, userInput
							});
						}
					} else {
						logger.debug("User {} callbackMap does not have the type {}", targetUser.getRoleName(), type);
					}
				} else {
					logger.debug("User {} does not have the callback map", targetUser.getRoleName());
				}
				
				if ( notfound ) {
					SysMessageManager.getInstance().sendClientInfoMessage(targetUser, "input.error", Type.NORMAL);
				}
			}
		} else {
			BceUserInput.Builder builder = BceUserInput.newBuilder();
			builder.setUsersession(targetUserSessionKey.toString());
			builder.setInput(userInput);
			builder.setType(type);
			GameContext.getInstance().proxyToRemoteGameServer(targetUserSessionKey, targetGameServerId, builder.build());
		}
	}
	
	/**
	 * The input callback interface
	 * @author wangqi
	 *
	 */
	public static interface InputCallback {
		
		public void callback(User user, String userInput);
	}
	
}
