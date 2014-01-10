package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceGetVoiceChat.BceGetVoiceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseGetVoiceChat.BseGetVoiceChat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceVoiceChatHandler is used for voice chat 
 * 
 * 
 * @author wangqi
 *
 */
public class BceGetVoiceChatHandler extends SimpleChannelHandler {

	private Logger logger = LoggerFactory.getLogger(BceGetVoiceChatHandler.class);

	private static final BceGetVoiceChatHandler instance = new BceGetVoiceChatHandler();

	private BceGetVoiceChatHandler() {
		super();
	}

	public static BceGetVoiceChatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGetVoiceChat");
		}

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;

		processChat(user, request);
	}

	public void processChat(User user, XinqiMessage request) {
		BceGetVoiceChat chat = (BceGetVoiceChat)request.payload;

		byte[] voice = ChatManager.getInstance().retrieveVoiceContent(chat.getVoiceid());

		BseGetVoiceChat.Builder builder = BseGetVoiceChat.newBuilder();
		if ( voice != null ) {
			builder.setVoiceid(chat.getVoiceid());
			builder.setVoice(ByteString.copyFrom(voice));
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
	
}
