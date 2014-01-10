package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceMoveProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseMoveProp;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * The BceMovePropHandler is used for protocol MoveProp 
 * @author wangqi
 *
 */
public class BceMovePropHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceMovePropHandler.class);
	
	private static final BceMovePropHandler instance = new BceMovePropHandler();
	
	private BceMovePropHandler() {
		super();
	}

	public static BceMovePropHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceMoveProp");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		
		XinqiBceMoveProp.BceMoveProp bceMoveProp = (XinqiBceMoveProp.BceMoveProp) request.payload;
		int curPlace = bceMoveProp.getCurPlace();
		int prePlace = bceMoveProp.getPrePlace();
		String curId = bceMoveProp.getCurId();
		String preId = bceMoveProp.getPreId();
		
		User user = (User)session.getAttribute(Constant.USER_KEY);
		
		Bag bag = user.getBag();
		PropData propData = null;
		String propName = null;
		if ( prePlace >=0 && prePlace < Bag.BAG_WEAR_COUNT ) {
			propData = bag.getWearPropDatas().get(prePlace);
		} else {
			propData = bag.getOtherPropData(prePlace);
		}
		
		boolean success = false;
		if ( propData != null ) {
			propName = propData.getName();
			success = bag.movePropData(prePlace, curPlace);
		} else {
			logger.debug("#BceMovePropHandler: propData not found at pew: {}", prePlace);
		}

		if ( success ) {
			UserManager.getInstance().saveUserBag(user, false);
			
			XinqiMessage response = new XinqiMessage();
			XinqiBseMoveProp.BseMoveProp.Builder builder = XinqiBseMoveProp.BseMoveProp.newBuilder();
			builder.setCurId(curId);
			builder.setPreId(preId);
			builder.setCurPlace(propData.getPew());
			logger.debug("Move propData from {} to {}", prePlace, propData.getPew());
			builder.setPrePlace(prePlace);
			response.payload = builder.build();
	    GameContext.getInstance().writeResponse(user.getSessionKey(), response);
	    
	    BseRoleBattleInfo roleBattleInfo = user.toBseRoleBattleInfo();
	    response = new XinqiMessage();
	    response.payload = roleBattleInfo;
	    GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		} else {
			//Don't move it.
			XinqiMessage response = new XinqiMessage();
			XinqiBseMoveProp.BseMoveProp.Builder builder = XinqiBseMoveProp.BseMoveProp.newBuilder();
			builder.setCurId(preId);
			builder.setPreId(preId);
			builder.setCurPlace(-1);
			builder.setPrePlace(-1);
			response.payload = builder.build();
	    GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		}
    
		StatClient.getIntance().sendDataToStatServer(user, StatAction.MoveProp, propName, curPlace, success);
	}
	
	
}
