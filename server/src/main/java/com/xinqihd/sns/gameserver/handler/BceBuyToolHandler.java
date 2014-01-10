package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyTool;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyTool.BceBuyTool;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBuyToolHandler is used for protocol BuyTool 
 * @author wangqi
 *
 */
public class BceBuyToolHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceBuyToolHandler.class);
	
	private static final BceBuyToolHandler instance = new BceBuyToolHandler();
	
	private BceBuyToolHandler() {
		super();
	}

	public static BceBuyToolHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceBuyTool");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		XinqiBceBuyTool.BceBuyTool buyTool = (BceBuyTool) request.payload;
		int toolId  = buyTool.getTool();
		BuffToolType tool = BuffToolType.fromId(toolId);
		if ( tool == null ) {
			logger.warn("Failed to find the BuffToolType by id:{}", toolId);
			return;
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getShopManager().buyBuffTool(user, tool);
	}
	
	
}
