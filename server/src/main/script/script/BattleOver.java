package script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.management.relation.RoleNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleCamp;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BattleUserAudit;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossPojo;
import com.xinqihd.sns.gameserver.boss.BossType;
import com.xinqihd.sns.gameserver.boss.BossWinType;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.db.mongo.RankManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The battle is over. Calculate the final statistic data.
 * 
 * @author wangqi
 *
 */
public class BattleOver {
	
	private static final Logger logger = LoggerFactory.getLogger(BattleOver.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		Collection battleUsers = (Collection)parameters[0];
		int winCamp = (Integer)parameters[1];
		Battle battle = (Battle)parameters[2];
		int roundNumber = battle.getRoundCount();
		
		RoomType roomType = battle.getBattleRoom().getRoomLeft().getRoomType();
		BossType bossType = null;
		BossWinType bossWinType = null;
		String bossId = null;
		if ( roomType == RoomType.PVE_ROOM ) {
			for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser battleUser = (BattleUser) iterator.next();
				/**
	  		 * Store the boss combat ranking.
	  		 */
	  		if ( battleUser.getUser().isBoss() ) {
					//Store the boss user.
					User bossUser = battleUser.getUser();
					Boss boss = (Boss)bossUser.getUserData(BossManager.USER_BOSS);
					bossType = boss.getBossPojo().getBossType();
					bossWinType = boss.getBossPojo().getBossWinType();
					bossId = boss.getId();
					break;
	    	}
			}
		}
		for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
			BattleUser battleUser = (BattleUser) iterator.next();
			HashMap auditExpMap = new HashMap();
			logger.debug("calculate exp for user: {}", battleUser.getUser().getRoleName());
			//if ( battleUser.getUser().isAI() || battleUser.getUser().isProxy() ) continue;

	    //命中率
	    if ( battleUser.getTotalAttack() > 0 ) {
	    	float hitRatio = battleUser.getTotalHit()*1.0f/battleUser.getTotalAttack();
	    	if ( hitRatio < 1.0f ) {
	    		battleUser.setHitRatio( hitRatio );
	    	} else {
	    		battleUser.setHitRatio(1.0f);
	    	}
	    } else {
	    	battleUser.setHitRatio( 0.0f );
	    }
	    boolean winGame = false;
	    if ( winCamp == battleUser.getCamp() || winCamp == BattleCamp.BOTH.id() ) {
	    	winGame = true;
	    }

	    Collection hurtUsers = battleUser.getHurtUsers().values();
	    double totalExpDelta = 0;
	    int totalEnemyHurt = 0;
	    int totalKill = 0;
	    boolean canHasExp = !battleUser.isLeaveBattle();
	    boolean hasActionPoint = true;
	    if ( roomType == RoomType.TRAINING_ROOM || roomType == RoomType.DESK_ROOM ) {
	    	canHasExp = false;
	    } else {
	    	//如果玩家没有体力值了，不能获得经验
	    	hasActionPoint = RoleActionManager.getInstance().
	    			checkUserHasRoleActionPoint(battleUser.getUser());
	    	if ( !hasActionPoint ) {
	    		canHasExp = false;
	    		logger.debug("User {} has no more action points so do not have exp",
	    				battleUser.getUser().getRoleName());
	    	}
	    }
	    //Check the diamond collect boss
			if ( battle.getBoss() != null && battle.getBoss().getBossPojo().
					getBossWinType() == BossWinType.COLLECT_DIAMOND ) {
				canHasExp = false;
			}
			/**
			 * 钻石副本没有伤害，所以要单独计算
			 * 这个方法在BossBattleSync中调用了，所以这里可以不再重复调用
			 * 2013-02-28
			 */
			/*
			if ( bossType == BossType.WORLD && bossWinType == BossWinType.COLLECT_DIAMOND ) {
  				RankManager.getInstance().storeBossHurtRankData(battleUser.getUser(), 
  						bossId, battleUser.getTotalDiamonds(), System.currentTimeMillis());
			}
			*/
	    /**
	     * 计算每次杀敌的经验加成
	     */
	    int maxRivalLevel = 0;
			for (Iterator iter = hurtUsers.iterator(); iter.hasNext();) {
	    	BattleUserAudit audit = (BattleUserAudit)iter.next();
	    	if ( audit.isEnemy() ) {
	    		if ( audit.isKilled() || audit.isDroped()) {
	    			totalKill++;
	    		}
	    		totalEnemyHurt += audit.getHurtBlood();

	    		/**
	    		 * Store the boss combat ranking.
	    		 */
	    		if ( audit.getBattleUser().getUser().isBoss() ) {
						//Store the boss hurt ranking.
	    			if ( winGame ) {
	    				if ( bossType == BossType.WORLD ) {
	  	    			if ( totalEnemyHurt > 0 && bossWinType == BossWinType.KILL_ONE ) {
	  	    				RankManager.getInstance().storeBossHurtRankData(battleUser.getUser(), 
	  	    						bossId, totalEnemyHurt, System.currentTimeMillis());
	  	    			} else if ( totalEnemyHurt > 0 && bossWinType == BossWinType.KILL_MANY ) {
	  	    				RankManager.getInstance().storeBossHurtRankData(battleUser.getUser(), 
	  	    						bossId, totalEnemyHurt, System.currentTimeMillis());
	  	    			}
//	  	    			else if ( winType == BossWinType.COLLECT_DIAMOND ) {
//	  	    				RankManager.getInstance().storeBossHurtRankData(battleUser.getUser(), 
//	  	    						boss.getId(), battleUser.getTotalDiamonds(), System.currentTimeMillis());
//	  	    			}
	      			} else if ( bossType == BossType.SINGLE ) {
	    					RankManager.getInstance().storeSingleBossHurtRankData(battleUser.getUser(), 
	      						bossId, totalEnemyHurt, System.currentTimeMillis(), battleUsers);
	      			}
	    			} else {
	    				logger.debug("User {} failed to challenge boss so no ranking", 
	    						new Object[]{battleUser.getUser().getRoleName()});
	    			}
		    	}
	    	}
	    	
	    	int rivalLevel = audit.getBattleUser().getUser().getLevel();
	    	int levelDiff = rivalLevel - battleUser.getUser().getLevel();
	    	float powerRatio = 0.0f;
	    	if ( roomType != RoomType.PVE_ROOM ) {
		    	int absLevelDiff = Math.abs(levelDiff)-3;
		    	if ( absLevelDiff > 0 ) {
		    		//powerRatio = absLevelDiff * absLevelDiff * 0.125f;
		    		powerRatio = absLevelDiff * 0.1f;
		    		if ( levelDiff < 0 ) {
		    			powerRatio *= -1;
		    		}
					}
	    	}
	    	//if ( battleUser.getUser().getPower() > 0 ) {
	    		//powerRatio = audit.getBattleUser().getUser().getPower()*1.0f/battleUser.getUser().getPower();
	    	//}
	    	if ( maxRivalLevel < rivalLevel ) {
	    		maxRivalLevel = rivalLevel;
	    	}
	    	if ( canHasExp ) {
	    		/*
	    		 * int roundNumber,
					 * int perfectKill, int dropNum, int secondKill, 
					 * int accurateHit
	    		 */
		    	totalExpDelta += (int)UserCalculator.calculateBattleExp(
							audit.getHurtBlood(),
							audit.getBattleUser().getUser().getBlood(),
							audit.isKilled()?1:0,
							0, //hitratio
							powerRatio,
							0, //numOfUsers 
							false,
							audit.isEnemy(),
							Integer.MAX_VALUE, //roundNumber
							false, //perfectKill
							audit.isDroped()?1:0,
							audit.isSecondKillNum()?1:0,
							audit.getAccurateNum(),
							audit.isKilled(),
							auditExpMap
					);
	    	}
	    }//for...
	    boolean perfectKill = (winGame && totalEnemyHurt > 0 && battleUser.getBlood() == battleUser.getUser().getBlood());
	    /**
	     * Think of a remark
	     */
	    thinkRemarkForUser(battleUser, winGame, hasActionPoint, perfectKill, 
	    		maxRivalLevel, roomType);
	    /**
	     * 计算整体战斗的经验加成 
	     */
			if ( canHasExp ) {
				totalExpDelta += (int)UserCalculator.calculateBattleExp(
						1, 
						Integer.MAX_VALUE, //totalBlood 
						0, 
						battleUser.getHitRatio(),
						0, //power ratio
						battle.getBattleUserNumber(), 
						winGame,
						true,
						roundNumber, //roundNumber
						perfectKill, //perfectKill
						0, //dropNum
						0, //secondKill
						0, //accurateHit
						false, //killEnemy
						auditExpMap
				);
			}
			
			if ( totalExpDelta < 0 ) {
				totalExpDelta = 0;
			} else if ( totalExpDelta > 1000 ) {
				totalExpDelta = 1000;
			}
			battleUser.setBaseExp((int)totalExpDelta);
			
			if ( canHasExp ) {
				double expRate = VipManager.getInstance().getVipLevelBattleExpRatio(battleUser.getUser());
				int vipExp = (int)Math.round(totalExpDelta * expRate);
				battleUser.setVipExp( vipExp );
				totalExpDelta += vipExp;
				logger.debug("User {} has vip extra exp: {}", battleUser.getUser().getRoleName(), vipExp);
			}
			/*
			if ( user.getExpRate() > 1 ) {
				int doubleExp = (int)(totalExpDelta * (user.getExpRate() - 1));
				user.setDoubleExp(doubleExp);
			} else {
				user.setDoubleExp(0);
			}
			*/
			battleUser.setTotalExp((int)totalExpDelta);
			battleUser.setTotalEnemyHurt(totalEnemyHurt);
			battleUser.setTotalKill(totalKill);
			battleUser.setAuditExpMap(auditExpMap);
			/**
			 * 更新玩家装备的熟练度
			 */
			if ( !battleUser.getUser().isAI() && !battleUser.getUser().isProxy() ) {
				Bag bag = battleUser.getUser().getBag();
				for (Iterator pdIter = bag.getWearPropDatas().iterator(); pdIter.hasNext();) {
					PropData pd = (PropData) pdIter.next();
					if ( pd == null ) continue;
					pd.setSkill(pd.getSkill()+totalKill);
				}
			}
			logger.debug("User {} 's total expDelta is {}", battleUser.getUser().getRoleName(), totalExpDelta);
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}

	/**
	 * 为一个玩家给出评价
	 * 
	 * @param battleUser
	 * @param winGame
	 * @param hasActionPoint
	 * @param perfectKill
	 * @param powerRatio TODO
	 */
	public static void thinkRemarkForUser(BattleUser battleUser, boolean winGame,
			boolean hasActionPoint, boolean perfectKill, int maxRivalLevel, RoomType roomType) {
		try {
			int levelDiff = 0; 
			if ( roomType != RoomType.PVE_ROOM ) {
				levelDiff = battleUser.getUser().getLevel() - maxRivalLevel;
			}
			//针对本方玩家设置评价
			BattleUser killerUser = battleUser.getKillerUser();
			String remark = null;
			if ( roomType != RoomType.PVE_ROOM ) {
				if ( !hasActionPoint ) {
					remark = Text.text("action.exhausted");
				} else {
					if ( winGame ) {
						if ( maxRivalLevel > 0 ) {
							if ( levelDiff > 3f ) {
								remark = Text.text("battle.remark.succ.beatweak");
							} else if ( levelDiff < -3f ) {
								remark = Text.text("battle.remark.succ.beatstrong");
							} else if ( perfectKill ) {
									remark = Text.text("battle.remark.succ.perfect");
							} else if ( killerUser == null ) {
								remark = Text.text("battle.remark.succ");
							}
						}
					} else if ( killerUser != null ) {
						//针对对手设置评价
						float myAgility = battleUser.getUser().getAgility();
						float killerAgility = battleUser.getUser().getAgility();
						float agilityRatio = Math.round((killerAgility/myAgility)*10)/10f;
						if ( agilityRatio >= 1.5f ) {
							remark = Text.text("battle.remark.fail.agility", new Object[]{agilityRatio});
						} else {
							PropData myWeapon = (PropData)battleUser.getUser().getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
							PropData opWeapon = (PropData)killerUser.getUser().getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
							PropData mySuit = (PropData)battleUser.getUser().getBag().getWearPropDatas().get(PropDataEquipIndex.SUIT.index());
							PropData opSuit = (PropData)killerUser.getUser().getBag().getWearPropDatas().get(PropDataEquipIndex.SUIT.index());
							if ( opWeapon.getUserLevel() > myWeapon.getUserLevel() ) {
								//{"battle.remark.fail.level", "对手使用了'{}'等级的武器，而您的武器还停留在'{}'等级"},
								remark = Text.text("battle.remark.fail.level", 
										new Object[]{opWeapon.getName().substring(0, 2), myWeapon.getName().substring(0, 2)});
							} else if ( opWeapon.getLevel() > myWeapon.getLevel() ) {
								remark = Text.text("battle.remark.fail.strength", 
										new Object[]{opWeapon.getLevel(), myWeapon.getLevel()});
							} else if ( mySuit == null && opSuit != null ) {
								remark = Text.text("battle.remark.fail.suit", 
										new Object[]{opSuit.getName(), myWeapon.getLevel()});						
							} else if ( killerUser.getHitRatio() > battleUser.getHitRatio() + 0.2 ) {
								remark = Text.text("battle.remark.fail.hitratio", 
										new Object[]{myWeapon.getLevel(), opWeapon.getLevel()});
							} else if (killerUser.getUser().isVip() && !battleUser.getUser().isVip() ) {
								remark = Text.text("battle.remark.fail.vip");
							} else if ( opWeapon.getPower() > myWeapon.getPower() ) {
								remark = Text.text("battle.remark.fail.strong", 
										new Object[]{myWeapon.getLevel(), opWeapon.getLevel()});
							}
						}
					}
				}
			} else {
				if ( winGame ) {
					remark = Text.text("battle.remark.boss.succ");
				} else {
					remark = Text.text("battle.remark.boss.fail");
				}
			}
			if ( !StringUtil.checkNotEmpty(remark) ) {
				if ( winGame ) {
					remark = Text.text("battle.remark.succ");
				} else {
					remark = Text.text("battle.remark.fail");
				}
			}
			battleUser.setRemark(remark);
			logger.debug("Remark for {} is {}", battleUser.getUser().getRoleName(), remark);
		} catch (Exception e) {
			logger.debug("Failed to output remark: {}", e.getMessage());
		}
	}
	
	/**
	 * Find the audit of the killers who killed given user.
	 * 
	 * @param battleUsers
	 * @return
	 */
	public static final BattleUserAudit findKillerUserAudit(BattleUser battleUser) {
		BattleUser killerUser = battleUser.getKillerUser();
		if ( killerUser != null ) {
			//Find all users that are hurted by the killer
			Collection hurtUsers = killerUser.getHurtUsers().values();
			for (Iterator iter = hurtUsers.iterator(); iter.hasNext();) {
				//杀手杀死玩家时的统计数据
	    	BattleUserAudit victimAudit = (BattleUserAudit)iter.next();
    		//Find the victim's audit
	    	if ( victimAudit != null && victimAudit.getBattleUser() == battleUser ) {
	    		logger.debug("User {} is killed by killer:{}", battleUser.getUser().getRoleName(), 
	    				killerUser.getUser().getRoleName());
	    		return victimAudit;
	    	}
			}
		}
		return null;
	}
	
}
