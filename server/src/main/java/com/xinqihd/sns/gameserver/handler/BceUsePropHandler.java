package com.xinqihd.sns.gameserver.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceUseProp.BceUseProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseUseProp.BseUseProp;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceUsePropHandler is used for protocol UseProp 
 * @author wangqi
 *
 */
public class BceUsePropHandler extends SimpleChannelHandler {
	
	private Log log = LogFactory.getLog(BceUsePropHandler.class);
	
	private static final BceUsePropHandler instance = new BceUsePropHandler();
	
	private BceUsePropHandler() {
		super();
	}

	public static BceUsePropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("->BceUseProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		BceUseProp useProp = (BceUseProp)request.payload;
		int propPew = useProp.getPropPew();
		PickRewardResult result = RewardManager.getInstance().openItemBox(user, propPew);
		if ( result != PickRewardResult.SUCCESS ) {
			//When result is success, the response is sent back by Box.openItem
			XinqiMessage response = new XinqiMessage();
			BseUseProp.Builder builder = BseUseProp.newBuilder();
			builder.setSuccessed(result.ordinal());
			response.payload = builder.build();
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		}
	}
	
	
}
