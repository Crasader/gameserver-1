package com.xinqihd.sns.gameserver.chat;

/**
 * required int32 msgType = 1;         
 * //消息类型   0:当前 1:私聊 2:工会 3:小喇叭 4:大喇叭 5:小队
 * @author wangqi
 *
 */
public enum ChatType {

	//The hall messages.
	ChatCurrent,
	//The private to private messages.
	ChatPrivate,
	//The guild messages.
	ChatGuild,
	//The virtual server messages.
	ChatServer,
	//The global world messages.
	ChatWorld,
	//The room messages.
	ChatRoom,
	//The system admin can send a global messages.
	ChatSystem;
	
	private static String[] types = new String[ChatType.values().length];
	
	static {
		for ( int i=0; i<types.length; i++ ) {
			types[i] = ChatType.values()[i].name();
		}
	}
	
	/**
	 * Get all types' name
	 * @return
	 */
	public static String[] allTypes() {
		return types;
	}

}
