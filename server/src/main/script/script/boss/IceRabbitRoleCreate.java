package script.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import script.reward.BossWeaponReward;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.HardMode;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.entity.rank.RankFilterType;
import com.xinqihd.sns.gameserver.entity.rank.RankScoreType;
import com.xinqihd.sns.gameserver.entity.rank.RankType;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Create an AI user according to a real user's properties.
 * 
 * 
 * @author wangqi
 *
 */
public class IceRabbitRoleCreate {

	private static final Logger logger = LoggerFactory.getLogger(IceRabbitRoleCreate.class);
	static double q = 3.0;
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if ( result != null ) {
			return result;
		}
		
		User user = (User)parameters[0];
		Boss boss = (Boss)parameters[1];
		
		BossPojo bossPojo = boss.getBossPojo();
		bossPojo = bossPojo.clone();
		boss.setBossPojo(bossPojo);
		
		/**
		 * 尝试获取当前的玩家贡献度
		 */
		String zsetName = RankManager.getInstance().getSingleBossRankSetName(user, boss.getId());
		Collection ranks = RankManager.getInstance().
				getAllRankUsers(user, RankType.PVE, RankFilterType.TOTAL, RankScoreType.PVE, 0, 100, boss.getId(), zsetName);
		int totalHurt = 0;
		int myHurt = 0;
		RankUser myRankUser = null;
		for (Iterator iter = ranks.iterator(); iter.hasNext();) {
			RankUser rankUser = (RankUser) iter.next();
			if ( rankUser == null ) continue;
			totalHurt += rankUser.getScore();
			if ( user.getUsername().equals(rankUser.getBasicUser().getUsername()) ) {
				myHurt = rankUser.getScore();
				myRankUser = rankUser;
			}
		}
		float percent = 1.0f;
		if ( totalHurt > 0 ) {
			percent = myHurt*1.0f/totalHurt;
		}
		/**
		 * 创建单机版的雪兔BOSS
		 */
		int totalLevel = 10;
		boss.setTotalProgress(totalLevel);
		int limit = 200;
		boss.setLimit(limit);
		boss.setIncreasePerHour(limit);
		int mode = bossPojo.getLevel();
		int progress = boss.getProgress()+1;
		String title = StringUtil.concat(new Object[]{"雪兔Lv", progress});
		int expBase = 500;
		int goldenBase = 1000;
		HardMode hardMode = HardMode.normal;
		switch ( mode ) {
			case 0:
				title = title.concat("(简单)");
				hardMode = HardMode.simple;
				break;
			case 1:
				title = title.concat("(普通)");
				expBase = 800;
				goldenBase = 3000;
				hardMode = HardMode.normal;
				break;
			case 2:
				title = title.concat("(困难)");
				expBase = 1200;
				goldenBase = 10000;
				hardMode = HardMode.hard;
				break;
		}
		bossPojo.setTitle(title);
		String desc = StringUtil.concat(new Object[]{"雪兔共分为", totalLevel, "个等级，每次挑战成功会进入下一个等级，" +
				"失败后只能领取当前等级的奖励，闯关的等级越高，获得的道具越好，全部通关有稀有道具奖励哟"});
		bossPojo.setDesc(desc);
		String target = "挑战开始后需要在1小时内杀死雪兔则进入下一等级，超时则重回第1等级";
		bossPojo.setTarget(target);
		
		bossPojo.getRewards().clear();
		Jedis jedisDB = JedisFactory.getJedisDB();
		
//		int expireSecond = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.BOSS_SINGLE_EXPIRE, 3600);
//		Calendar startCal = Calendar.getInstance();
//		Calendar endCal = Calendar.getInstance();
//		endCal.add(Calendar.SECOND, expireSecond);

		/**
		 * Create Boss instance
		 */
		String key = StringUtil.concat(new Object[]{BossManager.KEY_BOSS_SINGLE, user.getUsername()});
		String id = bossPojo.getId();
		
		jedisDB.hset(key, id, boss.toString());

		ArrayList list = new ArrayList();
		list.add(boss);

		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
}
