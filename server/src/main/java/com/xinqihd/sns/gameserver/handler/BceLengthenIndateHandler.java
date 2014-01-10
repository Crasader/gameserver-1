package com.xinqihd.sns.gameserver.handler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceLengthenIndate.BceLengthenIndate;
import com.xinqihd.sns.gameserver.proto.XinqiBceLengthenIndate.LengthenIndate;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceLengthenIndateHandler is used for protocol LengthenIndate 
 * @author wangqi
 *
 */
public class BceLengthenIndateHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceLengthenIndateHandler.class);
	
	private static final BceLengthenIndateHandler instance = new BceLengthenIndateHandler();
	
	private BceLengthenIndateHandler() {
		super();
	}

	public static BceLengthenIndateHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceLengthenIndate");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceLengthenIndate lengthIndate = (BceLengthenIndate)request.payload;
		List<LengthenIndate> lengthIndateList = lengthIndate.getLengthenindatesetList();
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		GameContext.getInstance().getShopManager().resubscribePropData(user, lengthIndateList);
		
	}
	
	
}
