package com.xinqihd.sns.gameserver.handler;

import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseExpireEquipments.BseExpireEquipments;
import com.xinqihd.sns.gameserver.proto.XinqiBseExpireEquipments.ExpireInfo;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * When the user already login, client will notify server to send 
 * extra information .
 * @author wangqi
 *
 */
public class BceLoginReadyHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceLoginReadyHandler.class);
	
	private static final BceLoginReadyHandler instance = new BceLoginReadyHandler();
	
	private BceLoginReadyHandler() {
		super();
	}

	public static BceLoginReadyHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceLoginReadyHandler");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		if ( sessionKey == null ) {
			logger.warn("User session is null. ");
			return;
		}
		
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		// 1. 检查过期装备，涉及到修改玩家power状态，所以在RoleInfo发送前发送
		Collection<PropData> expirePropDataSet = ShopManager.getInstance().checkEquipmentsExpire(user);
		Collection<ExpireInfo> expireInfos = ShopManager.getInstance().getExpireEquipInfos(user, expirePropDataSet);
		if ( expireInfos.size() > 0 ) {
			XinqiMessage expire = new XinqiMessage();
			BseExpireEquipments.Builder expireBuilder = BseExpireEquipments.newBuilder();
			for ( ExpireInfo index : expireInfos ) {
				expireBuilder.addExpireInfos(index);
			}
			expire.payload = expireBuilder.build();
			GameContext.getInstance().writeResponse(user.getSessionKey(), expire);
			
			//TODO should it send BattleRoleInfo here?
			// 1.1. 玩家战斗信息
			XinqiMessage roleBattle = new XinqiMessage();
			//Automatically tidy user's bag.
			//user.getBag().tidyUserBag();
			//GameContext.getInstance().getUserManager().saveUserBag(user, true);
			
			//Send user's information to client.
			roleBattle.payload = user.toBseRoleBattleInfo();
			GameContext.getInstance().writeResponse(user.getSessionKey(), roleBattle);
			
			SysMessageManager.getInstance().sendClientInfoMessage(user, "shop.equipexpire", Type.NORMAL);
		}
		
		StatClient.getIntance().sendDataToStatServer(user, StatAction.ReadyLogin, expireInfos.size());
	}
		
}
