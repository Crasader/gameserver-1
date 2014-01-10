package com.xinqihd.sns.gameserver.config.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.MongoDBUtil;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Output the system config data to HTTP 
 * @author wangqi
 *
 */
public class ConfigDataOutput {

	private static final Logger logger = LoggerFactory.getLogger(ConfigDataOutput.class);

	public static void main(String[] args) throws Exception {
		logger.debug("Config version changed. Send data again.");
		String dir = "config";
		if ( args != null && args.length>0 ) {
			dir = args[0];
		}
		File dirFile = new File(dir);
		if ( !dirFile.exists() ) {
			dirFile.mkdirs();
		}
		// 0.0 装备的基本数据
		String database = "babywarcfg", namespace="server0001";
		StringBuilder luaBuffer = new StringBuilder(10000);
		luaBuffer.append("weapons = {\n");
		for ( WeaponPojo weapon: EquipManager.getInstance().getWeapons() ) {
			luaBuffer.append(weapon.toLuaString(Locale.CHINESE));
		}
		luaBuffer.append("}\n");
		FileOutputStream fos = new FileOutputStream(new File(dir, "BseEquipment.txt"));
		fos.write(luaBuffer.toString().getBytes());
		fos.close();
		
		// 0.1 成就数据
		XinqiMessage achievements = new XinqiMessage();
		achievements.payload = TaskManager.getInstance().toBseAchievement();
		fos = new FileOutputStream(new File(dir, "BseAchievements.txt"));
		fos.write(achievements.payload.toByteArray());
		fos.close();
		
		// 0.2 地图基础数据 
		XinqiMessage mapData = new XinqiMessage();
		mapData.payload = GameContext.getInstance().getMapManager().toBseMap();
		fos = new FileOutputStream(new File(dir, "BseMap.txt"));
		fos.write(mapData.payload.toByteArray());
		fos.close();

		// 0.3 每日打卡奖励
		XinqiMessage dailyMark = new XinqiMessage();
		dailyMark.payload = GameContext.getInstance().getDailyMarkManager().toBseDailyMark();
		fos = new FileOutputStream(new File(dir, "BseDailyMark.txt"));
		fos.write(dailyMark.payload.toByteArray());
		fos.close();

		// 0.4 游戏提示
		XinqiMessage tip = new XinqiMessage();
		tip.payload = GameContext.getInstance().getTipManager().toBseTip(null);
		fos = new FileOutputStream(new File(dir, "BseTip.txt"));
		fos.write(tip.payload.toByteArray());
		fos.close();

		// 0.5 游戏任务
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, database, namespace, 
				"tasks", null);
		StringBuilder buf = new StringBuilder(10000);
		buf.append("tasks= {\n");
		for ( DBObject obj : list ) {
			TaskPojo task = (TaskPojo)MongoDBUtil.constructObject(obj);
			if ( StringUtil.checkNotEmpty(task.getScript()) ) {
				buf.append(task.toLuaString(Locale.CHINESE));
			}
		}
		buf.append("}\n");
		fos = new FileOutputStream(new File(dir, "BseTask.txt"));
		fos.write(buf.toString().getBytes());
		fos.close();
		
		// 0.6 商城数据
		/*
		Collection<ShopPojo> shops = ShopManager.getInstance().getShops();
		BseShop.Builder builder = BseShop.newBuilder();
		for ( ShopPojo shopPojo : shops ) {
			if ( shopPojo.getLevel() == -1 || 
					(shopPojo.getLevel() >= 0 && shopPojo.getLevel() < 10) ) {
				builder.addShops(shopPojo.toShopData());
			}
		}
		fos = new FileOutputStream(new File(dir, "BseShop.txt"));
		fos.write(builder.build().toByteArray());
		fos.close();
		*/
		
		// 0.7 游戏可配置参数
		/*
		XinqiMessage gameData = new XinqiMessage();
		gameData.payload = GameContext.getInstance().getGameDataManager().toBseGameDataKey(1);
		fos = new FileOutputStream(new File(dir, "BseGameDataKey.txt"));
		fos.write(gameData.payload.toByteArray());
		fos.close();
		*/
		
		// 0.8 游戏物品数据
		luaBuffer = new StringBuilder(10000);
		luaBuffer.append("items = {\n");
		for ( ItemPojo item: ItemManager.getInstance().getItems() ) {
			luaBuffer.append(item.toLuaString(Locale.CHINESE));
		}
		luaBuffer.append("}\n");
		/*
		XinqiMessage itemData = new XinqiMessage();
		itemData.payload = GameContext.getInstance().getItemManager().toBseItem();
		*/
		fos = new FileOutputStream(new File(dir, "BseItem.txt"));
		fos.write(luaBuffer.toString().getBytes());
		fos.close();
		
		// 0.9 充值数据
		/*
		XinqiMessage chargeData = new XinqiMessage();
		chargeData.payload = ChargeManager.getInstance().toBseChargeList("ios_iap");
		fos = new FileOutputStream(new File(dir, "BseChargeList.txt"));
		fos.write(chargeData.payload.toByteArray());
		fos.close();
		*/
		
		// 1.0 VIP数据
		XinqiMessage vipData = new XinqiMessage();
		vipData.payload = VipManager.getInstance().toBseVipInfo();
		fos = new FileOutputStream(new File(dir, "BseVipInfo.txt"));
		fos.write(vipData.payload.toByteArray());
		fos.close();

		System.exit(0);
	}

}
