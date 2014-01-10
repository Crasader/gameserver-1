package script.boss;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * When the PVE battle is over, sync the battle data to global server.
 * 
 * @author wangqi
 *
 */
public class BossBattleSync {
	
	private static final Logger logger = LoggerFactory.getLogger(BossBattleSync.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}

		Battle battle = (Battle)parameters[0];
		Collection winUserList = (Collection)parameters[1];
		Collection loseUserList = (Collection)parameters[2];

		Boss boss = null;
		int bossCount = 1;
		int userCount = 1;
		boolean winTheBattle = false;
		HashMap hurtMap = null;
		if ( loseUserList != null && loseUserList.size() > 0 ) {
			BattleUser battleUser = (BattleUser)loseUserList.iterator().next();
			User user = battleUser.getUser();
			if ( user.isBoss() ) {
				//real user is win
				boss = (Boss)user.getUserData(BossManager.USER_BOSS);
				bossCount = loseUserList.size();
				userCount = winUserList.size();
				winTheBattle = true;
				hurtMap = (HashMap)user.getUserData(BossManager.USER_TOTAL_HURT);
			} else {
				//read user fails
				battleUser = (BattleUser)winUserList.iterator().next();
				user = battleUser.getUser();
				winTheBattle = false;
				if ( winUserList != null && winUserList.size() > 0 ) {
					BattleUser bUser = (BattleUser)loseUserList.iterator().next();
					User u = battleUser.getUser();
					if ( u.isBoss() ) {
						//real user is win
						boss = (Boss)u.getUserData(BossManager.USER_BOSS);
					}
				}
			}
		}

		/**
		 * If the real user failed to pass the boss,
		 * his hurt will not be sync to server.
		 */
		if ( boss != null ) {
			if ( boss.getBossPojo().getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
				for (Iterator iterator = battle.getBattleUserMap().values().iterator(); iterator.hasNext();) {
					BattleUser bUser = (BattleUser) iterator.next();
					User user = bUser.getUser();
					if ( user.isAI() ) continue;
					
					BossPojo bossPojo = boss.getBossPojo();
					int currentProgress = bUser.getTotalDiamonds();
					if ( bUser.getTotalDiamonds() > 0 ) {
						RankManager.getInstance().storeBossHurtRankData(user, 
  						boss.getId(), bUser.getTotalDiamonds(), System.currentTimeMillis());
					}
					try {
						BossManager.getInstance().syncBossInstance(user, boss.getId(), currentProgress, userCount);
					} catch (Exception e) {
						logger.warn("exception:", e);
					}
				}
			} else if ( winTheBattle ) {
				for (Iterator iterator = winUserList.iterator(); iterator.hasNext();) {
					BattleUser bUser = (BattleUser) iterator.next();
					User user = bUser.getUser();
					
					BossPojo bossPojo = boss.getBossPojo();
					if ( bossPojo.getBossType() == BossType.WORLD ) {
						BossWinType winType = bossPojo.getBossWinType();
						int currentProgress = 0;
						if ( winType == BossWinType.KILL_ONE ) {
							Integer progressInt = (Integer)hurtMap.get(user.getSessionKey().toString());
							if ( progressInt != null ) {
								currentProgress = progressInt.intValue();
							}
						} else if ( winType == BossWinType.KILL_MANY ) {
							currentProgress = bossCount;
						} else if ( winType == BossWinType.COLLECT_DIAMOND ) {
							currentProgress = bUser.getTotalDiamonds();
							if ( bUser.getTotalDiamonds() > 0 ) {
								RankManager.getInstance().storeBossHurtRankData(user, 
		  						boss.getId(), bUser.getTotalDiamonds(), System.currentTimeMillis());
							}
						}
						try {
							BossManager.getInstance().syncBossInstance(user, boss.getId(), currentProgress, userCount);
						} catch (Exception e) {
							logger.warn("exception:", e);
						}
					} else if ( bossPojo.getBossType() == BossType.SINGLE ) {
						BossWinType winType = bossPojo.getBossWinType();
						if ( winType == BossWinType.KILL_ONE ) {
							/**
							 * 玩家成功的挑战了当前的副本，需要创建下一个版本的副本
							 */
							try {
								BossManager.getInstance().syncSingleBossInstance(user, boss.getId(), 1, true);
							} catch (Exception e) {
								logger.warn("exception:", e);
							}
						}
					}
				}
			} else {
				BossPojo bossPojo = boss.getBossPojo();
				if ( bossPojo.getBossType() == BossType.SINGLE ) {
					BossWinType winType = bossPojo.getBossWinType();
					if ( winType == BossWinType.KILL_ONE ) {
						for (Iterator iterator = loseUserList.iterator(); iterator.hasNext();) {
							BattleUser bUser = (BattleUser) iterator.next();
							User user = bUser.getUser();
							
							/**
							 * 玩家挑战失败了，只能领取当前的奖励
							 */
							try {
								BossManager.getInstance().syncSingleBossInstance(user, boss.getId(), 1, false);
							} catch (Exception e) {
								logger.warn("exception:", e);
							}
						}
					}
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
