package com.xinqihd.sns.gameserver.cron;

import java.util.Calendar;
import java.util.Collection;

import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.ServerListManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceBulletin.BceBulletin;
import com.xinqihd.sns.gameserver.proto.XinqiBceReloadConfig.BceReloadConfig;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 负责创建系统中需要的Boss对象
 * 
 * 每日中午12:00开放
 * 每日晚上20:00开放
 * 
 * @author wangqi
 *
 */
public class CronBossXuetu {
	
	public static void addXueto(Calendar startCal, Calendar endCal) {
		for ( int i=20; i<=100; i+=20 ) {
			int index = (i/20);
			String id = "ice_boss_"+index;
			String bossId = "ice_boss";
			String name = "雪兔";
			String title = "击杀雪兔Lv"+index;
			String desc = "冰冷极地中，雪兔魔抓走了宝贝大陆去采集雪晶的人们，勇士们，去打败雪兔魔救宝贝大陆的子民吧。单次战斗中伤害雪兔2000点血量视为一次挑战成功。";
			String target = "单次战斗中伤害雪兔"+(i*i)+"点血量视为一次挑战成功";
			BossType bossType = BossType.WORLD;
			BossWinType bossWinType = BossWinType.KILL_ONE;
			/**
			 * 19 冰天雪地
			 */
			String mapId = "19";
			int blood = 50000 * (int)(Math.pow(2, index));
			if ( blood>600000 ) {
				blood = 600000;
			}
			int level = i-10;
			int width = 100;
			int height = 100;
			int hurtRadius = 100;
		  //10000	BOSS雪兔	Suit1000
			String suitPropId = "10000";
			int minUserLevel = i-20;
			int maxUserLevel = i;
			if ( maxUserLevel >= LevelManager.MAX_LEVEL ) {
				maxUserLevel = LevelManager.MAX_LEVEL+1;
			}
			int requiredGolden = 1000 * index;
			int totalProgress = blood;
			String weaponId = "70"+(i/10-1);
			int limit = 200;
			int increasePerHour = 50;
			int winProgress = 2*i*i;
			
			/**
			 * 金币
			 */
			Reward goldenReward = new Reward();
			goldenReward.setPropId("-1");
			goldenReward.setPropLevel(-1);
			goldenReward.setType(RewardType.GOLDEN);
			goldenReward.setPropCount(5000*i);
			/**
			 * 经验
			 */
			Reward expReward = new Reward();
			expReward.setPropId("-5");
			expReward.setPropLevel(-1);
			expReward.setType(RewardType.EXP);
			expReward.setPropCount(100*i);
			/**
			 * 29021	寻宝卡	Prop10212	使用后当天可以增加5次免费寻宝机会
			 */
			ItemPojo item = ItemManager.getInstance().getItemById("29021");
			Reward cardReward = RewardManager.getInstance().getRewardItem(item);
			cardReward.setPropCount(i/20);
			Reward[] gifts = new Reward[3];
			gifts[0] = goldenReward;
			gifts[1] = cardReward;
			gifts[2] = expReward;

			BossUtil.addNewBoss(id, bossId, name, title, desc, target, bossType, bossWinType, mapId,
					blood, level, width, height, hurtRadius, suitPropId, minUserLevel,
					maxUserLevel, requiredGolden, startCal, endCal, gifts, limit,
					increasePerHour, winProgress, totalProgress,
					weaponId, ScriptHook.BOSS_ICE_RABBIT_ROLEATTACK, ScriptHook.BOSS_ICE_RABBIT_ROLEDEAD, -1);

		  //Reset boss id
			Collection<ServerPojo> servers = ServerListManager.getInstance().getServers();
			for ( ServerPojo server : servers ) {
				String zsetName = RankManager.getRankSetNameByServerId(server.getId(), RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, id);
				Jedis jedis = JedisFactory.getJedisDB();
				jedis.del(zsetName);
			}
		}
	}
	
	public static void main(String[] args) {
		//清除已经领取的记录
		BossUtil.cleanRewardKey("ice_boss");
		
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.MINUTE, 60);
		
		addXueto(startCal, endCal);

		BossUtil.updateServer(args, "雪兔世界BOSS已经开启");

		System.exit(0);
	}
}
