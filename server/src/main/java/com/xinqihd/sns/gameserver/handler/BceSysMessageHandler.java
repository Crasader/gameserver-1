package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceSysMessage.BceSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiBseSysMessage.BseSysMessage;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceInviteHandler is used for protocol Invite 
 * @author wangqi
 *
 */
public class BceSysMessageHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceSysMessageHandler.class);
	
	private static final BceSysMessageHandler instance = new BceSysMessageHandler();
	
	private BceSysMessageHandler() {
		super();
	}

	public static BceSysMessageHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceSysMessage");
		}
		
		User user = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
		XinqiMessage request = (XinqiMessage)message;
		BceSysMessage bce = (BceSysMessage)request.payload;
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setAction(bce.getAction());
		builder.setMessage(bce.getMessage());
		builder.setSeconds(bce.getSeconds());
		builder.setType(bce.getType());
		
		GameContext.getInstance().writeResponse(sessionKey, builder.build());
	}
	
	
}
