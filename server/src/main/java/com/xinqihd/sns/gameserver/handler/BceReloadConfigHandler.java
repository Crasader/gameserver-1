package com.xinqihd.sns.gameserver.handler;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.db.mongo.BiblioManager;
import com.xinqihd.sns.gameserver.db.mongo.CDKeyManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ExitGameManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.GameResourceManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.MapManager;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.db.mongo.ShopManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TipManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.proto.XinqiBceReloadConfig.BceReloadConfig;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

/**
 * The BceChargeInternalHandler is used to put yuanbao into user's account.
 * It is mainly used by game server to communicate.
 * 
 * @author wangqi
 *
 */
public class BceReloadConfigHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceReloadConfigHandler.class);
	
	private static final BceReloadConfigHandler instance = new BceReloadConfigHandler();
		
	private BceReloadConfigHandler() {
		super();
	}

	public static BceReloadConfigHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		logger.debug("->BceReloadConfig");
		
		XinqiMessage request = (XinqiMessage)message;
		BceReloadConfig reload = (BceReloadConfig)request.payload;
		
		List<String> configs = reload.getConfignameList();
		for ( String config : configs ) {
			try {
				logger.debug("Gameserver will reload config {}", config);
				/**
				 * "maps", "tips", "charges", "gamedata", "tasks", "vips", "shops", 
				 * "equips", "items", "levels", "gameres", "bosses", "rewards"	
				 */
				if ( "gamedata".equals(config) ) {
					GameDataManager.getInstance().reload();
				} else if ( "items".equals(config) ) {
					ItemManager.getInstance().reload();
				} else if ( "equips".equals(config) ) {
					EquipManager.getInstance().reload();
				} else if ( "shops".equals(config) ) {
					ShopManager.getInstance().reload();
				} else if ( "tasks".equals(config) ) {
					TaskManager.getInstance().reload();
				} else if ( "levels".equals(config) ) {
					LevelManager.getInstance().reload();
				} else if ( "bosses".equals(config) ) {
					BossManager.getInstance().reload();
				} else if ( "maps".equals(config) ) {
					MapManager.getInstance().reload();
				} else if ( "tips".equals(config) ) {
					TipManager.getInstance().reload();
				} else if ( "charges".equals(config) ) {
					ChargeManager.getInstance().reload();
				} else if ( "gameres".equals(config) ) {
					GameResourceManager.getInstance().reload();
				} else if ( "vips".equals(config) ) {
					VipManager.getInstance().reload();
				} else if ( "rewards".equals(config) ) {
					RewardManager.getInstance().reload();
				} else if ( "promotions".equals(config) ) {
					ActivityManager.getInstance().reload();
				} else if ( "cdkeys".equals(config) ) {
					CDKeyManager.getInstance().reload();
				} else if ( "exits".equals(config) ) {
					ExitGameManager.getInstance().reload();
				} else if ( "servers".equals(config) ) {
					ServerListManager.getInstance().reload();
				} else if ( "biblio".equals(config) ) {
					BiblioManager.getInstance().reload();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
