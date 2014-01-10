package script.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager;
import com.xinqihd.sns.gameserver.entity.rank.RankUser;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * When the boss is beaten, top 10 ranking users will get extra reward
 * 
 * @author wangqi
 *
 */
public class BossSendRankingReward {
	
	private static final Logger logger = LoggerFactory.getLogger(BossSendRankingReward.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}

		Boss boss = (Boss)parameters[0];
		Collection rankUsers = (Collection)parameters[1];
		
		String roleName = boss.getBossPojo().getTitle();
		StringBuilder buf = new StringBuilder(200);
		String msg = Text.text("boss.win.notify", new Object[]{roleName});
		int i = 1;
		buf.append(msg);
		boolean sendGolden = true;
		if ( boss.getBossPojo().getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
			sendGolden = false;
		}

		/**
		 * Prevent the mail are sending twice, use the redis to store user's data.
		 * Why not put the limit at boss level rather user level, because there may
		 * be an user is in battle yet while the boss is beaten, if the last user
		 * reach rank top 10, then he deserve a email.
		 */
		Jedis jedisDB = JedisFactory.getJedisDB();
		
		int bossLevel = boss.getBossPojo().getLevel();
		String bossName = boss.getBossPojo().getName();
		
		for (Iterator iterator = rankUsers.iterator(); iterator.hasNext();) {
			RankUser rankUser = (RankUser) iterator.next();
			BasicUser basicUser = rankUser.getBasicUser();
			
			//Construct the chat message.
			String key = BossManager.getBossRewardUserKey(basicUser, boss.getId());
			String rewardTaken = jedisDB.hget(key, BossManager.FIELD_BOSS_USER_REWARD);
			if ( !Constant.ONE.equals(rewardTaken) ) {
				if ( sendGolden ) {
					Integer rank = new Integer(rankUser.getRank());
					int golden = 10000 * (bossLevel+10);
					if ( rank.intValue() == 1 ) {
					} else if ( rank.intValue() == 2 ) {
						golden /= 2;
					} else if ( rank.intValue() >= 3 ) {
						golden /= 10;
					}
					String subject = Text.text("boss.win.mail.sub", new Object[]{bossName, rank});
					String content = Text.text("boss.win.mail.content", new Object[]{bossName, rank, golden});
					ArrayList gifts = new ArrayList();
					Reward reward = RewardManager.getInstance().getRewardGolden(golden);
					gifts.add(reward);
					MailMessageManager.getInstance().sendMail(null, rankUser.getBasicUser().get_id(), 
							subject, content, gifts, true);
					
					String displayName = UserManager.getDisplayRoleName(basicUser.getRoleName());	
					String userMsg = Text.text("boss.win.notify.user", new Object[]{i++, displayName, reward.getPropCount()});
					buf.append(userMsg);
					jedisDB.hset(key, BossManager.FIELD_BOSS_USER_RANK, Constant.ONE);

					StatClient.getIntance().sendDataToStatServer(
							basicUser, StatAction.BossRankMail, 
							new Object[]{rankUser.getRank(), rankUser.getScore()});
				} else {
					String displayName = UserManager.getDisplayRoleName(basicUser.getRoleName());	
					String userMsg = Text.text("boss.win.notify.user2", new Object[]{i++, displayName});
					buf.append(userMsg);
				}
			}
		}
		
		ChatManager.getInstance().processChatToWorldAsyn(null, buf.toString());
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
