package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseVipInfo.BseVipInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * 
 * @author wangqi
 *
 */
public class BceVipInfoHandler extends SimpleChannelHandler {

	private Logger logger = LoggerFactory.getLogger(BceVipInfoHandler.class);

	private static final BceVipInfoHandler instance = new BceVipInfoHandler();

	private BceVipInfoHandler() {
		super();
	}

	public static BceVipInfoHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceVipInfo");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BseVipInfo vipInfo = VipManager.getInstance().toBseVipInfo();
		GameContext.getInstance().writeResponse(sessionKey, vipInfo);
	}
	
}
