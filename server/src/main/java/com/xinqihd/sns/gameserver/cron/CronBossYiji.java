package com.xinqihd.sns.gameserver.cron;

import java.util.Calendar;
import java.util.Collection;

import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 每天提供1000个钻石供玩家收集，其中包含：
 *  1. 强化石lv1 20, 强化石lv2 10, 强化石lv3 5, 强化石lv4 2, 强化石lv5 1
 *  2. 强化石lv1 20, 强化石lv2 10, 强化石lv3 5, 强化石lv4 2, 强化石lv5 1
 * 
 * 每日中午12:00开放
 * 每日晚上20:00开放
 * 
 * @author wangqi
 *
 */
public class CronBossYiji {

	public static void addDiamond(Calendar startCal, Calendar endCal, int count) {
		String id = "yiji";
		String bossId = "yiji";
		String name = "遗迹宝藏";
		String title = "遗迹宝藏";
		String desc = "每次放出1000颗钻石，包含经验、装备、石头宝盒，每局战斗限制16回合结束，大家快来收集吧";
		String target = "16回合内收集尽可能多的钻石";
		BossType bossType = BossType.WORLD;
		BossWinType bossWinType = BossWinType.COLLECT_DIAMOND;
		/**
		 * 1005	遗迹宝藏
		 */
		String mapId = "1005";
		int blood = 10000000;
		int level = 1;
		int width = 100;
		int height = 100;
		int hurtRadius = 100;
		String suitPropId = "";
		int minUserLevel = -1;
		int maxUserLevel = -1;
		int requiredGolden = 15000;
		//钻石的数量
		int totalProgress = count;
		String weaponId = "700";
		int limit = 10;
		int increasePerHour = 10;
		int winProgress = 1;
		//限制16回合
		int totalRound = 20;

		BossUtil.addNewBoss(id, bossId, name, title, desc, target, bossType, bossWinType, mapId,
				blood, level, width, height, hurtRadius, suitPropId, minUserLevel,
				maxUserLevel, requiredGolden, startCal, endCal, null, limit,
				increasePerHour, winProgress, totalProgress,
				weaponId, ScriptHook.BOSS_DIAMOND_COLLECT_ROLEATTACK, ScriptHook.BOSS_DIAMOND_COLLECT_ROLEDEAD, totalRound);

	  //Reset boss id
		Collection<ServerPojo> servers = ServerListManager.getInstance().getServers();
		for ( ServerPojo server : servers ) {
			String zsetName = RankManager.getRankSetNameByServerId(server.getId(), 
					RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, id);
			Jedis jedis = JedisFactory.getJedisDB();
			jedis.del(zsetName);
		}
	}

	public static void main(String[] args) {
		//清除已经领取的记录
		BossUtil.cleanRewardKey("yiji");
		
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.MINUTE, 60);

		int count = 1000;
		addDiamond(startCal, endCal, count);

		BossUtil.updateServer(args, "钻石收集副本已经开启");

		System.exit(0);
	}
}
