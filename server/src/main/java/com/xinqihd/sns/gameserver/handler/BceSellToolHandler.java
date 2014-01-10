package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceSellTool;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceSellToolHandler is used for protocol SellTool 
 * @author wangqi
 *
 */
public class BceSellToolHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceSellToolHandler.class);
	
	private static final BceSellToolHandler instance = new BceSellToolHandler();
	
	private BceSellToolHandler() {
		super();
	}

	public static BceSellToolHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceSellTool");
		}
		
    XinqiMessage request = (XinqiMessage)message;
    XinqiBceSellTool.BceSellTool sellTool = (XinqiBceSellTool.BceSellTool) request.payload;
    int toolIndex = sellTool.getPos();
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		GameContext.getInstance().getShopManager().sellBuffTool(user, toolIndex);
	}
	
	
}
