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
public class AddSingleXuetu {
	
	public static void addXueto(int mode, Calendar startCal, Calendar endCal) {
		/**
		 * 创建单机版的雪兔BOSS
		 */
		String id = "ice_boss_"+mode;
		String bossId = "ice_boss";
		String name = "雪兔";
		String title = "雪兔(";
		switch ( mode ) {
			case 0:
				title += "简单)";
				break;
			case 1:
				title += "普通)";
				break;
			case 2:
				title += "困难)";
				break;
		}
		String desc = "冰冷极地中，雪兔魔抓走了宝贝大陆去采集雪晶的人们，勇士们，去打败雪兔魔救宝贝大陆的子民吧。";
		String target = "1小时内杀死或者埋葬雪兔则挑战成功";
		BossType bossType = BossType.SINGLE;
		BossWinType bossWinType = BossWinType.KILL_ONE;
		/**
		 * 19 冰天雪地
		 */
		String mapId = "19";
		int blood = 0;
		int level = mode;
		int width = 100;
		int height = 100;
		int hurtRadius = 100;
	  //10000	BOSS雪兔	Suit1000
		String suitPropId = "10000";
		int minUserLevel = 20;
		int maxUserLevel = 101;
		int requiredGolden = 1000 * (mode+1);
		String weaponId = "700";
		int limit = 200;
		int increasePerHour = 50;
		int winProgress = 0;
		int totalProgress = 10;
		/**
		 * 金币
		 */
		Reward goldenReward = new Reward();
		goldenReward.setPropId("-1");
		goldenReward.setPropLevel(-1);
		goldenReward.setType(RewardType.GOLDEN);
		goldenReward.setPropCount(5000*(mode+1));
		/**
		 * 经验
		 */
		Reward expReward = new Reward();
		expReward.setPropId("-5");
		expReward.setPropLevel(-1);
		expReward.setType(RewardType.EXP);
		expReward.setPropCount(1000*(mode+1));
		/**
		 * 29021	寻宝卡	Prop10212	使用后当天可以增加5次免费寻宝机会
		 */
		ItemPojo item = ItemManager.getInstance().getItemById("29021");
		Reward cardReward = RewardManager.getInstance().getRewardItem(item);
		cardReward.setPropCount(mode+1);
		Reward[] gifts = new Reward[3];
		gifts[0] = goldenReward;
		gifts[1] = cardReward;
		gifts[2] = expReward;

		BossUtil.addNewBoss(id, bossId, name, title, desc, target, bossType, bossWinType, mapId,
				blood, level, width, height, hurtRadius, suitPropId, minUserLevel,
				maxUserLevel, requiredGolden, startCal, endCal, gifts, limit,
				increasePerHour, winProgress, totalProgress,
				weaponId, ScriptHook.BOSS_ICE_RABBIT_ROLEATTACK, ScriptHook.BOSS_ICE_RABBIT_ROLEDEAD, -1,
				ScriptHook.BOSS_ICE_RABBIT_ROLECREATE, ScriptHook.BOSS_ICE_RABBIT_USERCREATE, 
				ScriptHook.BOSS_ICE_RABBIT_BATTLEREWARD);
	}
	
	public static void main(String[] args) {
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.MINUTE, 60);
		
		addXueto(0, startCal, endCal);
		addXueto(1, startCal, endCal);
		addXueto(2, startCal, endCal);

		//BossUtil.updateServer(args, "雪兔世界BOSS已经开启");

		System.exit(0);
	}
}
