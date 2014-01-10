package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceChatHandler is used for protocol Chat 
 * 
		message BceChat {
		    //消息类型  0:当前 1:私聊 2:工会 3:小喇叭 4:大喇叭 5:小队
		    required int32 msgType = 1;         
		    //消息内容
		    required string msgContent = 2;     
		    //发送给谁 私聊用到
		    optional string usrId = 3;          
		}
 * 
 * @author wangqi
 *
 */
public class BceChatHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceChatHandler.class);
	
	private static final BceChatHandler instance = new BceChatHandler();
	
	private BceChatHandler() {
		super();
	}

	public static BceChatHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceChat");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;

		processChat(user, request);
	}
	
	public void processChat(User user, XinqiMessage request) {
		BceChat chat = (BceChat)request.payload;
		boolean send = ChatManager.getInstance().processChatAsyn(user, chat);
		
		if ( user != null && !user.isAI() ) {
			StatClient.getIntance().sendDataToStatServer(user, StatAction.Chat, chat.getMsgType(), send, chat.getMsgContent());
		}
	}
	
}
