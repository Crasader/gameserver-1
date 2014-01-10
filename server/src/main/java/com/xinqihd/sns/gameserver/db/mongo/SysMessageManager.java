package com.xinqihd.sns.gameserver.db.mongo;

import org.apache.mina.core.session.IoSession;
import org.apache.sshd.common.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseSysMessage.BseSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * This manager is used to send system message to users. In some cases, 
 * the system message will bind some operation, like tell client to logout user
 * or tell client to reconnect.
 * 
 * @author wangqi
 *
 */
public class SysMessageManager {

	private static final Logger logger = LoggerFactory.getLogger(SysMessageManager.class);
	
	private static SysMessageManager instance = new SysMessageManager();

	//The default seconds to display a info message.
	private static final int SECONDS_INFO = 3000;

	SysMessageManager() {
	}

	/**
	 * Get a singleton instance for this manager.
	 * @return
	 */
	public static final SysMessageManager getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param messageKey
	 */
	public void sendClientInfoMessage(User user, String messageKey, Type type) {
		sendClientInfoMessage(user, messageKey, Action.NOOP, type, null);
	}
	
	/**
	 * 
	 * @param messageKey
	 */
	public void sendClientInfoMessage(SessionKey userSessionKey, String messageKey, Type type) {
		sendClientInfoMessage(userSessionKey, messageKey, type, null);
	}
	
	
	/**
	 * 
	 * @param messageKey
	 */
	public void sendClientInfoMessage(User user, String messageKey, Type type, Object[] arguments) {
		sendClientInfoMessage(user, messageKey, Action.NOOP, type, arguments);
	}
	
	/**
	 * 
	 * @param messageKey
	 */
	public void sendClientInfoMessage(SessionKey userSessionKey, String messageKey, Type type, Object[] arguments) {
		String message = messageKey;
		try {
			if ( arguments != null ) {
				message = Text.text(messageKey, arguments);
			} else {
				message = Text.text(messageKey);
			}
		} catch (Exception e) {
			logger.warn("The messageKey {} does not exist in GameResource.java", messageKey);
		}
		sendClientInfoRawMessage(userSessionKey, message, Action.NOOP, type);
	}
		
	/**
	 * Send the message by its i18n key
	 * @param messageKey
	 */
	public void sendClientInfoMessage(User user, String messageKey, Action action, Object[] arguments) {
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		String message = messageKey;
		try {
			message = Text.text(messageKey, arguments);
		} catch (Exception e) {
			logger.warn("The messageKey {} does not exist in GameResource.java", messageKey);
		}
		sendClientInfoRawMessage(user, message, action, Type.NORMAL);
	}
	
	/**
	 * Send the message by its i18n key
	 * @param messageKey
	 */
	public void sendClientInfoMessage(User user, String messageKey, Action action, Type type, Object[] arguments) {
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		String message = messageKey;
		try {
			message = Text.text(messageKey, arguments);
		} catch (Exception e) {
			logger.warn("The messageKey {} does not exist in GameResource.java", messageKey);
		}
		sendClientInfoRawMessage(user, message, action, type);
	}
	
	/**
	 * Send the raw message to client
	 * @param messageKey
	 */
	public void sendClientInfoRawMessage(User user, String message, Action action, Type type) {
		if ( user.isAI() ) {
			logger.debug("Ignore the message to ai user '{}'", user.getRoleName());
			return;
		}
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(SECONDS_INFO);
		switch ( action ) {
			case NOOP:
				builder.setAction(XinqiSysMessage.Action.NOOP);
				break;
			case EXIT_GAME:
				builder.setAction(XinqiSysMessage.Action.EXIT_GAME);
				break;
			case RECONNECT:
				builder.setAction(XinqiSysMessage.Action.RECONNECT);
				break;
		}
		builder.setType(type);
		BseSysMessage sysMessage = builder.build();
		
		if ( user.getSessionKey() != null ) {
			XinqiMessage xinqi = new XinqiMessage();
			xinqi.payload = sysMessage;
			GameContext.getInstance().writeResponse(user.getSessionKey(), xinqi);
		}
		logger.debug("Send message '{}' to user '{}'", message, user.getRoleName());
	}
	
	/**
	 * Send the raw message to client
	 * @param messageKey
	 */
	public void sendClientInfoRawMessage(IoSession session, String message, Action action, Type type) {
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(SECONDS_INFO);
		switch ( action ) {
			case NOOP:
				builder.setAction(XinqiSysMessage.Action.NOOP);
				break;
			case EXIT_GAME:
				builder.setAction(XinqiSysMessage.Action.EXIT_GAME);
				break;
			case RECONNECT:
				builder.setAction(XinqiSysMessage.Action.RECONNECT);
				break;
		}
		builder.setType(type);
		BseSysMessage sysMessage = builder.build();
		
		if ( session != null ) {
			XinqiMessage xinqi = new XinqiMessage();
			xinqi.payload = sysMessage;
			session.write(xinqi);
		}
	}
	
	/**
	 * Send the raw message to client
	 * @param messageKey
	 */
	public void sendClientInfoRawMessage(SessionKey userSessionKey, String message, Action action, Type type) {
		if ( userSessionKey == null ) return;
		
		if ( AIManager.getInstance().isAIUser(userSessionKey) ) {
			logger.debug("Ignore the message to ai user '{}'", userSessionKey);
			return;
		}
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(SECONDS_INFO);
		switch ( action ) {
			case NOOP:
				builder.setAction(XinqiSysMessage.Action.NOOP);
				break;
			case EXIT_GAME:
				builder.setAction(XinqiSysMessage.Action.EXIT_GAME);
				break;
			case RECONNECT:
				builder.setAction(XinqiSysMessage.Action.RECONNECT);
				break;
		}
		builder.setType(type);
		BseSysMessage sysMessage = builder.build();
		
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = sysMessage;
		GameContext.getInstance().writeResponse(userSessionKey, xinqi);
		logger.debug("Send message '{}' to user '{}'", message, userSessionKey);
	}
	
	/**
	 * Send the raw message to client
	 * @param messageKey
	 */
	public void sendClientInfoRawMessage(SessionKey userSessionKey, String message, int delay) {
		if ( userSessionKey == null ) return;
		
		if ( AIManager.getInstance().isAIUser(userSessionKey) ) {
			logger.debug("Ignore the message to ai user '{}'", userSessionKey);
			return;
		}
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(delay);
		builder.setAction(XinqiSysMessage.Action.NOOP);
		builder.setType(Type.NORMAL);
		BseSysMessage sysMessage = builder.build();
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = sysMessage;
		GameContext.getInstance().writeResponse(userSessionKey, xinqi);
		logger.debug("Send message '{}' to user '{}'", message, userSessionKey);
	}
	
	/**
	 * Send the raw message to client
	 * @param messageKey
	 */
	public void sendClientInfoWeiboMessage(SessionKey userSessionKey, String message, String weibo, Type type) {
		if ( userSessionKey == null ) return;
		
		if ( AIManager.getInstance().isAIUser(userSessionKey) ) {
			logger.debug("Ignore the message to ai user '{}'", userSessionKey);
			return;
		}
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(SECONDS_INFO);
		builder.setAction(XinqiSysMessage.Action.NOOP);
		builder.setType(type);
		builder.setWeibo(weibo);
		BseSysMessage sysMessage = builder.build();
		
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = sysMessage;
		GameContext.getInstance().writeResponse(userSessionKey, xinqi);
		logger.debug("Send weibo message '{}' to user '{}'", weibo, userSessionKey);
	}
	
	/**
	 * Prompt an dialog to users and download the given url content.
	 * @param session
	 * @param message
	 * @param url
	 * @param type
	 */
	public void sendClientInfoURLMessage(IoSession session, String message, String url, Type type) {
		if ( session == null ) return;
		
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(SECONDS_INFO);
		builder.setAction(XinqiSysMessage.Action.RECONNECT);
		builder.setType(type);
		builder.setWeibo(url);
		BseSysMessage sysMessage = builder.build();
		
		
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = sysMessage;
		session.write(xinqi);
		logger.debug("Send weibo message '{}' to user", url);
	}


}
