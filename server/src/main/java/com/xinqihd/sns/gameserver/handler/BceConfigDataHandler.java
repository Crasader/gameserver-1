package com.xinqihd.sns.gameserver.handler;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.CurrencyUnit;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBceConfigData.BceConfigData;
import com.xinqihd.sns.gameserver.proto.XinqiBseConfigData.BseConfigData;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceAddFriendHandler is used for protocol AddFriend 
 * @author wangqi
 *
 */
public class BceConfigDataHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceConfigDataHandler.class);
	
	private static final BceConfigDataHandler instance = new BceConfigDataHandler();
	
	private BceConfigDataHandler() {
		super();
	}

	public static BceConfigDataHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceConfigData");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceConfigData configData = (BceConfigData)request.payload;
		int version = configData.getVersion();
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		
		//Send response to client
		XinqiMessage response = new XinqiMessage();
		BseConfigData.Builder builder = BseConfigData.newBuilder();
		builder.setVersion(GameContext.VERSION);
		response.payload = builder.build();
		GameContext.getInstance().writeResponse(user.getSessionKey(), response);
		
		if ( version != GameContext.VERSION ) {
			logger.debug("Config version changed. Send data again.");
			// 0.0 装备的基本数据
			XinqiMessage equipData = new XinqiMessage();
			equipData.payload = GameContext.getInstance().getEquipManager().toBseEquipment();
			GameContext.getInstance().writeResponse(user.getSessionKey(), equipData);
			
			// 0.1 地图基础数据 
			XinqiMessage mapData = new XinqiMessage();
			mapData.payload = GameContext.getInstance().getMapManager().toBseMap();
			GameContext.getInstance().writeResponse(user.getSessionKey(), mapData);
						
			// 0.3 每日打卡奖励
			XinqiMessage dailyMark = new XinqiMessage();
			dailyMark.payload = GameContext.getInstance().getDailyMarkManager().toBseDailyMark();
			GameContext.getInstance().writeResponse(user.getSessionKey(), dailyMark);

			// 0.4 游戏提示
			XinqiMessage tip = new XinqiMessage();
			tip.payload = GameContext.getInstance().getTipManager().toBseTip(user);
			GameContext.getInstance().writeResponse(user.getSessionKey(), tip);

			// 0.5 游戏任务
			XinqiMessage taskList = new XinqiMessage();
			taskList.payload = GameContext.getInstance().getTaskManager().toBseTask(user);
			GameContext.getInstance().writeResponse(user.getSessionKey(), taskList);
			
			// 0.6 商城数据
			//disable vip discount
			XinqiMessage shop = new XinqiMessage();
			/*
			boolean isVip = UserManager.getInstance().checkUserVipStatus(user);
			if ( isVip ) {
				int shopDiscount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.VIP_SHOP_DISCOUNT, 80);
				shop.payload = GameContext.getInstance().getShopManager().toBseShop(
						user, shopDiscount);
				logger.debug("User {} is VIP and all shop item will be discounted to 80%", user.getRoleName(), shopDiscount);
			} else {
				shop.payload = GameContext.getInstance().getShopManager().toBseShop(user);
			}
			*/
			shop.payload = GameContext.getInstance().getShopManager().toBseShop(user);
			GameContext.getInstance().writeResponse(user.getSessionKey(), shop);
			
			// 0.7 游戏可配置参数
			XinqiMessage gameData = new XinqiMessage();
			gameData.payload = GameContext.getInstance().getGameDataManager().toBseGameDataKey(user.getLevel());
			GameContext.getInstance().writeResponse(user.getSessionKey(), gameData);
			
			// 0.8 游戏物品数据
			XinqiMessage itemData = new XinqiMessage();
			itemData.payload = GameContext.getInstance().getItemManager().toBseItem();
			GameContext.getInstance().writeResponse(user.getSessionKey(), itemData);
			
			// 0.9 充值数据
			//XinqiMessage chargeData = new XinqiMessage();
			//disable vip discount
			/*
			if ( isVip ) {
				float chargeDiscount = (float)GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.VIP_CHARGE_DISCOUNT, 8.0);
				chargeData.payload = ChargeManager.getInstance().toBseChargeList(CurrencyUnit.CHINESE_YUAN, chargeDiscount);
				logger.debug("User {} is VIP and charging will be discounted to {}", user.getRoleName(), chargeDiscount);
			} else {
				chargeData.payload = ChargeManager.getInstance().toBseChargeList(CurrencyUnit.CHINESE_YUAN);
			}
			*/
			//chargeData.payload = ChargeManager.getInstance().toBseChargeList("ios_iap");
			//GameContext.getInstance().writeResponse(user.getSessionKey(), chargeData);
			
		}
	}
	
	
}
