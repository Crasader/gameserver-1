package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.db.mongo.BiblioManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyTool;
import com.xinqihd.sns.gameserver.proto.XinqiBceBuyTool.BceBuyTool;
import com.xinqihd.sns.gameserver.proto.XinqiBceGetUserBiblio.BceGetUserBiblio;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBuyToolHandler is used for protocol BuyTool 
 * @author wangqi
 *
 */
public class BceGetUserBiblioHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceGetUserBiblioHandler.class);
	
	private static final BceGetUserBiblioHandler instance = new BceGetUserBiblioHandler();
	
	private BceGetUserBiblioHandler() {
		super();
	}

	public static BceGetUserBiblioHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceGetUserBiblioHandler");
		}

		XinqiMessage request = (XinqiMessage)message;
		BceGetUserBiblio biblio = (BceGetUserBiblio)request.payload;
		int sortType  = biblio.getSorttype();

		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BiblioManager.getInstance().sendUserBiblioList(user, sortType);
	}
	
}
