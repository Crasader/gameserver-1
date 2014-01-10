package com.xinqihd.sns.gameserver.invite;

import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.session.SessionKey;

/**
 * A wrapper object for delayed invitation
 * 
 * @author wangqi
 *
 */
public class Invite {
	
	private User user = null;
	
	private SessionKey friendSessionKey = null;
	
	private String challenge = null;
	
	private String message = null;
	
	private ConfirmCallback callback = null;
	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the friendSessionKey
	 */
	public SessionKey getFriendSessionKey() {
		return friendSessionKey;
	}

	/**
	 * @param friendSessionKey the friendSessionKey to set
	 */
	public void setFriendSessionKey(SessionKey friendSessionKey) {
		this.friendSessionKey = friendSessionKey;
	}

	/**
	 * @return the challenge
	 */
	public String getChallenge() {
		return challenge;
	}

	/**
	 * @param challenge the challenge to set
	 */
	public void setChallenge(String challenge) {
		this.challenge = challenge;
	}

	/**
	 * @return the callback
	 */
	public ConfirmCallback getCallback() {
		return callback;
	}

	/**
	 * @param callback the callback to set
	 */
	public void setCallback(ConfirmCallback callback) {
		this.callback = callback;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
}
