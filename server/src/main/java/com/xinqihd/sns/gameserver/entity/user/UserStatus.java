package com.xinqihd.sns.gameserver.entity.user;

public enum UserStatus {

	NORMAL,
	//The user's IoSession is closed but he is not removed from system.
	SESSION_CLOSED,
	//The user cannot send chat message in world
	CHAT_DISABLE,
}
