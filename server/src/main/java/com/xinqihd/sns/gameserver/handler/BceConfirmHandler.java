package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceConfirm.BceConfirm;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;

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
public class BceConfirmHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceConfirmHandler.class);
	
	private static final BceConfirmHandler instance = new BceConfirmHandler();
	
	private BceConfirmHandler() {
		super();
	}

	public static BceConfirmHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceConfirm");
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceConfirm confirm = (BceConfirm)request.payload;
		
		SessionKey targetSessionKey = sessionKey;
		String userSession = confirm.getUsersession();
		if ( StringUtil.checkNotEmpty(userSession) ) {
			targetSessionKey = SessionKey.createSessionKeyFromHexString(userSession);
		}
		ConfirmManager.getInstance().receiveConfirmMessage(user, confirm.getType(), confirm.getSelected(), targetSessionKey);
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.Confirm, confirm.getType(), confirm.getSelected());
	}
	
	public void processChat(User user, XinqiMessage request) {
		BceChat chat = (BceChat)request.payload;
		ChatManager.getInstance().processChatAsyn(user, chat);
	}
	
}
