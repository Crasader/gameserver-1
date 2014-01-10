package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.TipPojo;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.proto.XinqiBseTip.BseTip;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class TipManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(TipManager.class);
	
	private static final String COLL_NAME = "tips";
	
	private static final String INDEX_NAME = "_id";
	
	private static ArrayList<TipPojo> dataList = new ArrayList<TipPojo>();

	
	private static final TipManager instance = new TipManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static TipManager getInstance() {
		return instance;
	}
	
	TipManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized (dataList) {
			dataList.clear();
			for ( DBObject obj : list ) {
				TipPojo tip = (TipPojo)MongoDBUtil.constructObject(obj);
				dataList.add(tip);
				logger.debug("Load tip {} from database.", tip.getTip());
			}
		}
	}
	
	/**
	 * Get the underlying tip collection. Please do not modify
	 * it because it is not synchronized.
	 * 
	 * @return
	 */
	public List<TipPojo> getTips() {
		return dataList;
	}
	
	/**
	 * Construct Protobuf's BseEquipment data and 
	 * prepare to send to client.
	 * 
	 * @return
	 */
	public BseTip toBseTip(User user) {
		BseTip.Builder builder = BseTip.newBuilder();
		for ( TipPojo tipPojo : dataList ) {
			if ( tipPojo != null ) {
				boolean sendTip = true;
				if ( user != null ) {
					if ( tipPojo.getChannel() != null ) {
						if ( user.getChannel().contains(tipPojo.getChannel()) ) {
							sendTip = true;
						} else {
							sendTip = false;
						}
						long currentMillis = System.currentTimeMillis();
						if ( sendTip ) {
							if ( tipPojo.getStartMillis() > 0 ) {
								if ( tipPojo.getStartMillis() < currentMillis ) {
									sendTip = true;
								} else {
									sendTip = false;
								}
							}
						}
						if ( sendTip ) {
							if ( tipPojo.getEndMillis() > 0 ) {
								if ( tipPojo.getEndMillis() > currentMillis ) {
									sendTip = true;
								} else {
									sendTip = false;
								}
							}
						}
					}
				}
				if ( sendTip ) {
					String resTip = tipPojo.getTip();
					if ( Constant.I18N_ENABLE ) {
						Locale locale = GameContext.getInstance().getLocaleThreadLocal().get();
						resTip = GameResourceManager.getInstance().getGameResource(
								"tips_tip_".concat(String.valueOf(tipPojo.getId())), 
								locale, tipPojo.getTip());
					}
					builder.addTips(tipPojo.getTip());
				}
			}
		}
		return builder.build();
	}
}
