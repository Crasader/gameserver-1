package com.xinqihd.sns.gameserver.cron;

import java.util.Calendar;
import java.util.Collection;

import org.junit.Test;

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
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;

/**
 * 负责创建系统中需要的Boss对象
 * 
 * 每日中午12:00开放
 * 每日晚上20:00开放
 * 
 * @author wangqi
 *
 */
public class CronBossDiaoyudao {
	
	@Test
	public static void addDiaoyudao(Calendar startCal, Calendar endCal) {
		for ( int i=20; i<=100; i+=20 ) {
			int index = (i/20);
			String id = "diayudao_"+index;
			String bossId = "diaoyudao";
			String name = "矮人兵";
			String title = "夺岛大战Lv"+(i/20);
			String desc = "含有丰富鱼类资源的小小岛屿竟然被敌人占领了，还不赶快出手，夺回我们的岛屿！";
			String target = "单次战斗中杀死4名敌人视为一次挑战成功";
			BossType bossType = BossType.WORLD;
			BossWinType bossWinType = BossWinType.KILL_MANY;
			/**
			 * 1003 钓鱼岛地图
			 */
			String mapId = "1003";
			int blood = 20 * i;
			int level = i-10;
			int width = 100;
			int height = 100;
			int hurtRadius = 100;
		  //10001	BOSS敌人	Suit1000
			String suitPropId = "10001";
			int minUserLevel = i-20;
			int maxUserLevel = i;
			if ( maxUserLevel >= LevelManager.MAX_LEVEL ) {
				maxUserLevel = LevelManager.MAX_LEVEL+1;
			}
			int requiredGolden = 1000 * index;
			int totalProgress = 500;
			/**
			 * 520	黑铁●火箭炮
			 */
			String weaponId = "52"+(i/10-1); 
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
			int limit = 200;
			int increasePerHour = 50;
			int winProgress = 4;

			BossUtil.addNewBoss(id, bossId, name, title, desc, target, bossType, bossWinType, mapId,
					blood, level, width, height, hurtRadius, suitPropId, minUserLevel,
					maxUserLevel, requiredGolden, startCal, endCal, gifts, limit,
					increasePerHour, winProgress, totalProgress, 
					weaponId, ScriptHook.BOSS_KILLMANY_ROLEATTACK, ScriptHook.BOSS_KILLMANY_ROLEDEAD, -1);

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
		BossUtil.cleanRewardKey("diayudao_");
		
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.MINUTE, 60);

		addDiaoyudao(startCal, endCal);
		
		BossUtil.updateServer(args, "夺岛大战已经开启");
		
		System.exit(0);
	}
}
