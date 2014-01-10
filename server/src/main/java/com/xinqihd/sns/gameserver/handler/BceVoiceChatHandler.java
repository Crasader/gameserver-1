package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceVoiceChat.BceVoiceChat;
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
public class BceVoiceChatHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceVoiceChatHandler.class);
	
	private static final BceVoiceChatHandler instance = new BceVoiceChatHandler();
	
	private BceVoiceChatHandler() {
		super();
	}

	public static BceVoiceChatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceVoiceChat");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;

		processChat(user, request);
	}
	
	public void processChat(User user, XinqiMessage request) {
		BceVoiceChat chat = (BceVoiceChat)request.payload;
		boolean autoplay = chat.getAutoplay();
		int filter = chat.getFilter();
		byte[] voice = chat.getMsgContent().toByteArray();
		
		ChatManager.getInstance().processVoiceChatAsyn(user, chat);
		
		if ( user != null && !user.isAI() ) {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.VoiceChat, chat.getMsgType());
		}
	}
	
}
