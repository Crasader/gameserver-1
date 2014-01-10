package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceActionLimitBuy.BceActionLimitBuy;
import com.xinqihd.sns.gameserver.proto.XinqiBseActionLimitBuy.BseActionLimitBuy;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceBagTidyHandler is used for protocol BagTidy 
 * @author wangqi
 *
 */
public class BceActionLimitBuyHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceActionLimitBuyHandler.class);
	
	private static final BceActionLimitBuyHandler instance = new BceActionLimitBuyHandler();
	
	private BceActionLimitBuyHandler() {
		super();
	}

	public static BceActionLimitBuyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceActionLimitBuy");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceActionLimitBuy actionQuery = (BceActionLimitBuy)request.payload;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		int action = actionQuery.getAction();
		/**
		 * 未来的系统会对玩家的活动进行多种限制，例如
		 * 体力值：玩家每天有固定的行动点数
		 * 探索值: 玩家每天有固定的探索点数(开宝箱数量)
		 * 问答值: 玩家每天有固定的问答点数
		 * 招财值: 玩家每天有固定的招财次数
		 * ...... 
		 * 这个协议解决所有和系统限制资源相关的操作
		 */
		switch ( action ) {
			case 0:
				RoleActionManager.getInstance().buyRoleActionPoint(user, System.currentTimeMillis());
				break;
			default:
				logger.warn("unknown action:{}",action);
				break;
		}
	}
	
}
