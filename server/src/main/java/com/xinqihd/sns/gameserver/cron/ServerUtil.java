package com.xinqihd.sns.gameserver.cron;

import com.xinqihd.sns.gameserver.proto.XinqiBceBulletin.BceBulletin;
import com.xinqihd.sns.gameserver.proto.XinqiBceReloadConfig.BceReloadConfig;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class ServerUtil {

	/**
	 * Send a bulletin message to all users.
	 * 
	 * @param hostId
	 * @param message
	 */
	public static final void sendMessage(String hostId, String message) {
		GameClient client = null;
		String[] fields = StringUtil.splitMachineId(hostId);
		String host = fields[0];
		int port = StringUtil.toInt(fields[1], 3443);
		client = new GameClient(host, port);

		//推送全局消息
		sendMessage(client, message);
	}
	
	/**
	 * Send a bulletin message to all users.
	 * 
	 * @param hostId
	 * @param message
	 */
	public static final void sendMessage(GameClient client, String message) {
		//推送全局消息
		BceBulletin.Builder builder = BceBulletin.newBuilder();
		builder.setType(Type.CONFIRM.ordinal());
		builder.setExpire(0);
		builder.setMessage(message);
		XinqiMessage msg = new XinqiMessage();
		msg.payload = builder.build();
		client.sendMessageToServer(msg);
	}
}
