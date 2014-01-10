package com.xinqihd.sns.gameserver.script.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.BattleAuditItem;
import com.xinqihd.sns.gameserver.battle.BuffToolIndex;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.LevelPojo;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceField;
import com.xinqihd.sns.gameserver.entity.user.PropDataEnhanceType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.util.MathUtil;

/**
 * The collection of functions related to calculate user's properties. 
 * @author wangqi
 *
 */
public class UserCalculator {
	
	private static final Logger logger = LoggerFactory.getLogger(UserCalculator.class);
	
	public static final double calculateBattleExp(
			float totalHurt, float totalBlood, int killOthers, 
			float hitRatio, float powerRatio, int numOfUsers, 
			boolean winTheGame, boolean isEnemy, boolean killEnemy) {
		
		return calculateBattleExp(totalHurt, totalBlood, killOthers, hitRatio, powerRatio, numOfUsers, winTheGame, isEnemy, 
				0, false, 0, 0, 0, killEnemy,
				new HashMap<BattleAuditItem, Integer>());
		
	}

	/**
	 * Calculate the experience an user should get after the battle is over.
	 * 
	 * 计算玩家每回合战斗所获得的经验值:
	 * 	
	 *   应得经验=(伤害系数+死亡系数+命中系数+战斗力系数+人数系数+胜负系数)*标准经验	
	 *   伤害系数=	对敌人造成的总伤害/敌人总血量
	 *   死亡系数=	死亡为0.3，未死亡为0
	 *   命中系数=	命中率*0.2
	 *   战斗力系数=	敌方战斗力/我方战斗力
	 *   人数系数	单人为1.0, 双人为1.1, 三人为1.2, 四人为1.5
	 *   胜负系数=	胜利为1.0，失败为0.5
	 * 
	 * @param totalHurt      总伤害
	 * @param totalBlood     敌人总血量
	 * @param killOthers     杀敌数
	 * @param hitRatio       命中率
	 * @param powerRatio     战斗力加成
	 * @param numOfUsers     房间人数
	 * @param winTheGame     赢得游戏
	 * @param isEnemy        杀死敌人
	 * @param roundNumber    回合
	 * @param perfectKill    完美杀敌(未伤血)
	 * @param dropNum        敌人掉落数量
	 * @param secondKill     秒杀数量
	 * @param accurateHit    精确打击(90%命中次数)
	 * @param auditExpMap    
	 * @return
	 */
	public static final double calculateBattleExp(
			float totalHurt, float totalBlood, int killOthers, 
			float hitRatio, float powerRatio, int numOfUsers, 
			boolean winTheGame, boolean isEnemy, int roundNumber,
			boolean perfectKill, int dropNum, int secondKill, 
			int accurateHit, boolean killEnemy,
			HashMap<BattleAuditItem, Integer> auditExpMap ) {
		if ( totalHurt <= 0 ) {
			return 0;
		}
		int standardExp = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.STANDARD_USER_EXP, 100);
		int hurtExp = (int)(standardExp * (totalHurt*1.0f/totalBlood) * 0.5);
		if ( hurtExp > standardExp*2 ) hurtExp = standardExp*2;
		
		addAuditExp(auditExpMap, BattleAuditItem.HurtBlood, hurtExp);
		
		int deathExp = (int)(standardExp * killOthers * 0.3f);
		addAuditExp(auditExpMap, BattleAuditItem.KillNum, deathExp);
		
		double userIndex = 0.0f;
		if ( numOfUsers >= 8 ) {
			userIndex = 4.0f;
		} else if ( numOfUsers >= 6 ) {
			userIndex = 3.0f;
		} else if ( numOfUsers  >= 4 ) {
			userIndex = 1.0f;
		}
		int userExp = (int)(standardExp * userIndex);
		addAuditExp(auditExpMap, BattleAuditItem.TotalUser, userExp);
		
		int winExp = (int)(standardExp * (winTheGame ? 0.5f : 0f));
		addAuditExp(auditExpMap, BattleAuditItem.WinGame, winExp);
		
		int hitExp = (int)(standardExp * hitRatio * 0.5);
		addAuditExp(auditExpMap, BattleAuditItem.HitRatio, hitExp);
		
		//roundNumber    回合
		//10 回合内结束战斗给予10%加成
		int roundNumExp = 0;
		if ( winTheGame && roundNumber < 10 ) {
			roundNumExp = (int)(standardExp * 1/roundNumber * 0.1);
		}
		addAuditExp(auditExpMap, BattleAuditItem.RoundNum, roundNumExp);
		
		//perfectKill    完美杀敌(未伤血)
		int perfectExp = 0;
		if ( perfectKill ) {
			perfectExp = (int)(standardExp * 0.8);
		}
		addAuditExp(auditExpMap, BattleAuditItem.Perfect, perfectExp);
		
		//dropNum        敌人掉落数量
		int dropExp = (int)(standardExp * dropNum * 2);
		addAuditExp(auditExpMap, BattleAuditItem.DropNum, dropExp);
		
		//secondKill     秒杀数量
		int secondExp = (int)(standardExp * secondKill * 0.8);
		addAuditExp(auditExpMap, BattleAuditItem.SecondKill, secondExp);
		
		//accurateHit    精确打击(90%命中次数)
		int accurateExp = (int)(standardExp * accurateHit * 0.5);
		if ( accurateExp > 500 ) {
			accurateExp = 500;
		}
		addAuditExp(auditExpMap, BattleAuditItem.AccurateNum, accurateExp);
		
		double totalExp = hurtExp + deathExp + userExp + winExp + roundNumExp +
				hitExp + perfectExp + dropExp + secondExp + accurateExp;
		
		//战斗力加成
		/*
		int powerSign = 1;
		if ( powerRatio == 0 ) {
				powerRatio = 0;
		} else if ( powerRatio < 1.0 ) {
			//The attacker is weaker than me
			powerSign = -1;
			powerRatio = 1/powerRatio; 
		}
		int powerExp = powerSign * (int)(totalExp * powerRatio / 2);
		if ( totalExp + powerExp <= 0 ) {
			powerExp = -(int)totalExp + 1;
		} else if ( totalExp + powerExp > 1000 ) {
			powerExp = (int)totalExp - 1000;
		}		
		*/
		int powerExp = 0;
		/**
		 * powerRatio < 0 说明用户倚强凌弱，不管是否胜利都会扣减经验
		 * powerRatio > 0 只有在胜利时才加成
		 * 
		 * wangqi 2012-10-09
		 */
		if ( powerRatio>0 ) {
			if ( killEnemy ) {
				powerExp = (int)(standardExp * powerRatio);
			}
		} else {
			powerExp = (int)(standardExp * powerRatio);
		}
		if ( powerExp != 0 ) {
			addAuditExp(auditExpMap, BattleAuditItem.PowerDiff, powerExp);
		}
		
		int finalExp = (int)totalExp + powerExp;

		/**
		 * 杀死自己人，扣除经验
		 */
		if ( !isEnemy ) {
			if ( finalExp > 0 ) {
				finalExp = -finalExp;
			}
			addAuditExp(auditExpMap, BattleAuditItem.Spy, finalExp);
		}
		/*
		logger.debug("totalHurt:{}, totalBlood:{}, killOthers:{}, hitRatio:{}, powerRatio:{}, numOfUsers:{}, winTheGame:{}, \n" +
				"hurtIndex:{}, deathIndex:{}, hitIndex:{}, powerIndex:{}, userIndex:{}, winIndex:{}, finalExp:{}", 
				new Object[]{totalHurt, totalBlood, killOthers, hitRatio, powerRatio, numOfUsers, winTheGame, 
				hurtIndex, deathIndex, hitIndex, powerIndex, userIndex, winIndex, finalExp});
		*/

		if ( logger.isDebugEnabled() ) {
			StringBuilder buf = new StringBuilder(200);
			buf.append("\n");
			Set<BattleAuditItem> set = auditExpMap.keySet();
			int total = 0;
			for ( BattleAuditItem key : set ) {
				Integer value = auditExpMap.get(key);
				total += value.intValue();
				buf.append(key.name()).append(":").append(value).append("\n");
			}
			logger.debug("total exp: {}, {}, {}", new Object[]{total, finalExp, buf.toString()});
		}

		return finalExp;
	}
	
	/**
	 * Add the given value to audit map
	 * @param auditExpMap
	 * @param value
	 */
	private static final void addAuditExp(HashMap<BattleAuditItem, Integer> auditExpMap, BattleAuditItem key, int value) {
		Integer oldValue = auditExpMap.get(key);
		if ( oldValue == null ) {
			auditExpMap.put(key, value);
		} else {
			if ( value != 0 ) {
				int old = oldValue.intValue();
				auditExpMap.put(key, old+value);
			}
		}
	}
	
	/**
	 * 体力=240+敏捷/3
	 * 
   * 敏捷	体力
   * 100	3.333333333
   * 200	6.666666667
   * 300	10
   * 400	13.33333333
   * 500	16.66666667
   * 600	20
   * 700	23.33333333
   * 800	26.66666667
   * 900	30
   * 1000	33.33333333
   * 1100	36.66666667
   * 1200	40
   * 1300	43.33333333
   * 1400	46.66666667
   * 1500	50
   * 1506	50.2
   * 
	 * @return
	 */
	public static final double calculateThew(double agility) {
		double thew = 240 + agility / 3;
		return thew;
	}
	
	/**
	 * Only calucate the added value
	 * @param agility
	 * @return
	 */
	public static final double calculateThewWithoutBase(double agility) {
		double thew = agility / 3;
		return thew;
	}
	
	/** 
	 * 血量
	 * 1~10级 血量=1000+等级*50+防御/5
	 * 11~20级 血量=1500+（等级-10）*100+防御/5
	 * 21~30级 血量=2500+（等级-20）*150+防御/5
	 * 目前程序中的公式为:血量=1000+等级*50+防御/5
	 * 
	 * @param userLevel
	 * @param userDefend
	 * @return
	 */
	public static final double calculateBlood(int userLevel, double userDefend) {
		int basicBlood = 1000;
		int levelBlood = userLevel * 50;
		double finalBlood = basicBlood + levelBlood + userDefend / 5;
		return finalBlood;
	}
	
	/**
	 * 计算用户当前等级的经验值上限，当达到这个经验值时，用户将会提升一个等级
	 * 
	 * @deprecated 已经被LevelManager替代了
	 * @param userCurrentLevel
	 * @return
	 */
	/*
	public static final int calculateLevelExp(int userCurrentLevel) {
		int A = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_EXP_INDEX_A, 1);
		int B = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_EXP_INDEX_B, 7);
		int C = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_EXP_INDEX_C, 52);
		int requiredExp = A * userCurrentLevel * userCurrentLevel + B * userCurrentLevel + C;
		return requiredExp;
	}
	*/
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public static final double calculatePower(User user) {
	  // 战斗力
		Bag bag = user.getBag();
		List<PropData> list = bag.getWearPropDatas();
		int power = 0;
		for ( PropData pd : list ) {
			if ( pd == null || pd.isExpire() ) continue;
			power += pd.getPower();
		}
		LevelPojo level = LevelManager.getInstance().getLevel(user.getLevel());
		if ( level != null ) {
			power += EquipCalculator.calculateWeaponPower(level.getAttack(), level.getDefend(), 
					level.getAgility(), level.getLucky(), level.getBlood(), level.getSkin());
		}
		return power;
		/*
		return EquipCalculator.calculateWeaponPower(user.getAttack(), user.getDefend(), 
				user.getAgility(), user.getLuck(), user.getBlood(), user.getSkin());
				*/
	}
	
	public static final ArrayList<BuffToolIndex> calculateMaxHurtRatio(User attackUser) {
		return calculateMaxHurtRatio(attackUser, attackUser.getTkew());
	}
	/**
	 * 根据用户的敏捷度和体力情况，计算用户最大的伤害比率
	 * @param attackUser
	 * @return
	 */
	public static final ArrayList<BuffToolIndex> calculateMaxHurtRatio(User attackUser, int thew) {
		ArrayList<BuffToolIndex> tools = new ArrayList<BuffToolIndex>();
		boolean advance = false;
		if ( attackUser.getLevel()>=40 ) {
			advance = true;
		}
		if ( advance ) {
			/**
			 * 高级用户优先选择连续攻击
			 */
			int attackTwoMore = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_AttackTwoMoreTimes, 190);
			if ( thew > attackTwoMore ) {
				thew -= attackTwoMore;
				tools.add(BuffToolIndex.AttackOneMoreTimes);
			} else {
				int attackOneMore = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_AttackOneMoreTimes, 210);
				if ( thew > attackOneMore ) {
					thew -= attackOneMore;
					tools.add(BuffToolIndex.AttackOneMoreTimes);
				}
			}
		} else {
			/**
			 * 低级用户概率决定
			 */
			boolean useBranch = MathUtil.nextDouble() < 0.5;
			if ( useBranch ) {
				int attackThreeBranches = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_AttackThreeBranch, 210);
				if ( thew > attackThreeBranches ) {
					thew -= attackThreeBranches;
					tools.add(BuffToolIndex.AttackThreeBranch);
				}
			} else {
				int attackTwoMore = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_AttackTwoMoreTimes, 190);
				if ( thew > attackTwoMore ) {
					thew -= attackTwoMore;
					tools.add(BuffToolIndex.AttackTwoMoreTimes);
				} else {
					int attackOneMore = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_AttackOneMoreTimes, 210);
					if ( thew > attackOneMore ) {
						thew -= attackOneMore;
						tools.add(BuffToolIndex.AttackOneMoreTimes);
					}
				}
			}
		}
		/**
		 * 余下体力全部用于增加攻击力
		 */
		int hurt50Thew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd50, 210);
		while ( thew > hurt50Thew ) {
			thew -= hurt50Thew;
			tools.add(BuffToolIndex.HurtAdd50);
		}
		int hurt40Thew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd40, 196);
		while ( thew > hurt40Thew ) {
			thew -= hurt40Thew;
			tools.add(BuffToolIndex.HurtAdd40);
		}
		int hurt30Thew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd30, 182);
		while ( thew > hurt30Thew ) {
			thew -= hurt30Thew;
			tools.add(BuffToolIndex.HurtAdd30);
		}
		int hurt20Thew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd20, 168);
		while ( thew > hurt20Thew ) {
			thew -= hurt20Thew;
			tools.add(BuffToolIndex.HurtAdd20);
		}
		int hurt10Thew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.THEW_TOOL_HurtAdd10, 154);
		while ( thew > hurt10Thew ) {
			thew -= hurt10Thew;
			tools.add(BuffToolIndex.HurtAdd10);
		}
		return tools;
	}
	
	/**
	 * 计算是否为暴击
	 * 暴击计算方法：
	 * 幸运值即为暴击率，而暴击倍率等于1.5+2*暴击率，所以幸运值为10000时提升3.5倍DPR
	 * 
	 * 
	 * @param attackUser
	 * @return
	 */
	public static final double calculateCritialAttack(User attackUser) {
		int userLevel = attackUser.getLevel();
		double max = GameDataManager.getInstance().getGameDataAsDouble(
				GameDataKey.BATTLE_CRITICAL_MAX, 4000.0) + userLevel * 100;
		double luck = attackUser.getLuckyTotal()/max;
		double luckRatio = luck;
		if ( luckRatio > 0.5 ) {
			luckRatio = 0.5;
		}
		double attackRatio = 1.0;
		double random = MathUtil.nextDouble();
		if ( random < luckRatio ) {
			attackRatio = 1.1 + 0.7*luck;	
		}
		return attackRatio;
	}
	
	/**
	 * 伤害计算
	 * 1 实际伤害=自身伤害*（1+自身攻击/1000）
	 * 2 实际护甲=自身护甲*（1+（自身防御-对方幸运）/1000）
	 * 3 护甲减免率=（60+实际护甲）/（480+实际护甲）
	 * 4 最终伤害值=攻击方实际伤害*（1-被攻击方的护甲减免率）
	 * 5 武器具有爆炸半径，对方处于爆炸范围内才会受到伤害
	 * 6 将爆炸半径除以100，划分为100等分，每远离半径中心1等分，伤害值减少0.5%，即在最边缘的地方，伤害值只有50%
	 * 7 弹坑在命中玩家时会缩小，直接完全命中则弹坑只有40%，然后按照距离逐渐变大，未命中时弹坑为100%
	 * 
	 * 新的伤害计算公式:
	 * 最终伤害 = 攻击者总伤害 - 被攻击者护甲减免 - 被攻击者防御减免
	 * 攻击者总伤害 = (攻击者Attack*0.75) * 道具倍率加成 * (IF(暴击) 暴击倍率 ELSE 1) * 攻击命中系数
	 * 被攻击者护甲减免 = 被攻击者护甲值 * 0.5
	 * 被攻击者防御减免 = 被攻击者防御  / 1.5
	 * IF 最终伤害 < 0 THEN 最终伤害 = 0
	 * 
	 * @param attackUser
	 * @param beingAttackedUser
	 * @param tools         加成道具
	 * @param attackRatio   攻击命中率
	 * @param criticalRatio 暴击倍率
	 * @return
	 */
	public static final int calculateHurt(User attackUser, 
			User beingAttackedUser, ArrayList tools, double attackRatio, 
			double criticalRatio) {
		
		int attackUserAttack = attackUser.getAttackTotal();
		int beingAttackedUserDefend = beingAttackedUser.getDefendTotal();
		int beingAttackedUserSkin = beingAttackedUser.getSkin();

		double toolRatio = 1.0;
		if ( tools != null && tools.size()> 0 ) {
			Iterator toolIter = (new ArrayList(tools)).iterator();
			double attackOneMoreTimes = GameDataManager.getInstance().getGameDataAsDouble(
					GameDataKey.BATTLE_ONE_CONTINUE_HURT_RATIO, 0.85);
			double attackTwoMoreTimes = GameDataManager.getInstance().getGameDataAsDouble(
					GameDataKey.BATTLE_TWO_CONTINUE_HURT_RATIO, 0.6);
			double attackThreeBranchTimes = GameDataManager.getInstance().getGameDataAsDouble(
					GameDataKey.BATTLE_TREE_BRANCH_HURT_RATIO, 0.4);
			while ( toolIter.hasNext() ) {
				BuffToolType type = (BuffToolType)toolIter.next();
				if ( type  == BuffToolType.HurtAdd10 ) {
					toolRatio += 0.1;
				} else if ( type == BuffToolType.HurtAdd20 ) {
					toolRatio += 0.2;
				} else if ( type == BuffToolType.HurtAdd30 ) {
					toolRatio += 0.3;
				} else if ( type == BuffToolType.HurtAdd40 ) {
					toolRatio += 0.4;
				} else if ( type == BuffToolType.HurtAdd50 ) {
					toolRatio += 0.5;
				} else if ( type == BuffToolType.AttackTwoMoreTimes ) {
					toolRatio *= attackTwoMoreTimes;
				} else if ( type == BuffToolType.AttackThreeBranch ) {
					toolRatio *= attackThreeBranchTimes;
				} else if ( type == BuffToolType.AttackOneMoreTimes ) {
					toolRatio *= attackOneMoreTimes;
				}
			}
		}
		double attackToDprRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_ATTACK_INDEX, 0.75);
		double skinToDprRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_SKIN_INDEX, 0.5);
		double defendToDprRatio = GameDataManager.getInstance().getGameDataAsDouble(GameDataKey.GAME_DEFEND_INDEX, 1.5);
		
//		double attackerDamage = attackUserAttack * attackToDprRatio * toolRatio * criticalRatio;
		/**
		 * If the toolRatio is calculated here, the hurt50 will have 
		 * big impact. I think it is not right
		 * wangqi 2012-6-6
		 */
		//double attackerDamage = attackUserAttack * attackToDprRatio * toolRatio;
		/*
		double attackerDamage = attackUserAttack * attackToDprRatio * attackRatio;
		double attackeeDefend = (beingAttackedUserSkin * skinToDprRatio + beingAttackedUserDefend * defendToDprRatio);
//		double defendRatio = ( attackUserAttack / (attackUserAttack + attackeeDefend) );
//		if ( defendRatio > 1.0 ) {
//			defendRatio = 1.0;
//		}
		double finalDamage = (attackerDamage - attackeeDefend)*criticalRatio;
//		double finalDamage = attackerDamage * defendRatio;
		
		finalDamage *= toolRatio;
		*/
		double attackeeDefend = (beingAttackedUserSkin * skinToDprRatio + beingAttackedUserDefend * defendToDprRatio);
		double attackDefendRatio = (attackUserAttack)/(attackUserAttack+attackeeDefend);
		double attackerDamage = attackUserAttack * attackToDprRatio * attackRatio * (criticalRatio + toolRatio);
		
		/**
		 * The toolRatio and criticalRatio is seperated.
		 * 2012-11-14
		 */
		double finalDamage = attackerDamage * attackDefendRatio;
		
		if ( finalDamage <= 1.0 ) {
			finalDamage = 1;
		}
		
		if ( logger.isDebugEnabled() ) {
			logger.debug("#calculateHurt: user'{}', attackeeDefend:{}, attackDefendRatio:{}, attackerDamage:{}, beingAttackedUserDefend:{}, " +
					"attackerAttack:{}, criticalRatio:{}, toolRatio: {}, finalDamage: {}, beingAttackedUserBlood:{}", new Object[]{
					attackUser.getRoleName(), attackeeDefend, attackDefendRatio, attackerDamage, beingAttackedUserDefend, attackUserAttack, criticalRatio,
					toolRatio, finalDamage, beingAttackedUser.getBlood()
				}
			);
		}
		return (int)finalDamage;
	}
	
	/**
	 * Recaculate the user's powers when he wear or unwear an equipment. 
	 * @param user
	 * @param propData
	 * @param wearEquip
	 */
	public static final void updateWeaponPropData(User user, PropData propData, 
			boolean wearEquip) {
		updateSinglePropData(user, propData, wearEquip);
	}
	
	/**
	 * 
	 * @param user
	 */
	public static final void updateUserBasicProp(User user) {
		Bag bag = user.getBag();
		//update basic value
		int baseThew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_THEW_BASE, 210);
		LevelPojo level = LevelManager.getInstance().getLevel(user.getLevel());
		if ( level != null ) {
			user.setAttack(level.getAttack());
			user.setDefend(level.getDefend()); 
			user.setAgility(level.getAgility());
			user.setLuck(level.getLucky());
			user.setBlood(level.getBlood());
			user.setSkin(level.getSkin());
			user.setDamage(0);
			user.setTkew(baseThew);
		}
		List<PropData> propDatas = bag.getWearPropDatas();
		for ( PropData pd : propDatas ) {
			if ( pd != null ) {
				updateSinglePropData(user, pd, true);
			}
		}
	}

	/**
	 * @param user
	 * @param propData
	 * @param wearEquip
	 */
	private static void updateSinglePropData(User user, PropData propData,
			boolean wearEquip) {
		/* 
		 * The expired propData should not be calculated.
		 */
		if ( propData.isExpire() ) return;
		
		int sign = wearEquip?1:-1;
				
		// 重新核算PropData数值
		EquipCalculator.calculatePropDataPower(propData);
		
		/**
		 * 玩家攻击值里面默认包含了合成的数值，不单独增加了
		 * 2013-01-24 
		 */
	  // 玩家的攻击值
		/*
		Integer composeAttackInt = propData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceType.Field.ATTACK); 
		int composeAttack = 0;
		if ( composeAttackInt != null ) {
			composeAttack=composeAttackInt.intValue();
		}
		int newValue = user.getAttack() + sign*(propData.getAttackLev() + composeAttack);
		*/
		int newValue = user.getAttack() + sign*(propData.getAttackLev());
		if ( newValue < 0 ) newValue = 0;
	  user.setAttack( newValue );
	  
	  // 玩家的防御值
	  /*
	  Integer composeDefendInt = propData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceType.Field.DEFEND); 
		int composeDefend = 0;
		if ( composeDefendInt != null ) {
			composeDefend=composeDefendInt.intValue();
		}
	  newValue = user.getDefend() + sign*(propData.getDefendLev() + composeDefend);
	  */
	  newValue = user.getDefend() + sign*(propData.getDefendLev());
	  if ( newValue < 0 ) newValue = 0;
	  user.setDefend(newValue);
	  
	  // 玩家的敏捷值
	  int totalAgility = 0;
	  /*
	  Integer composeAgilityInt = propData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceType.Field.AGILITY); 
		int composeAgility = 0;
		if ( composeAgilityInt != null ) {
			composeAgility=composeAgilityInt.intValue();
		}
		totalAgility = sign*(propData.getAgilityLev() + composeAgility);
	  newValue = user.getAgility() + totalAgility;
	  */
	  totalAgility = sign*(propData.getAgilityLev());
	  newValue = user.getAgility() + totalAgility;
	  if ( newValue < 0 ) newValue = 0;
	  user.setAgility(newValue);
	  
	  // 玩家的幸运值
	  /*
	  Integer composeLuckInt = propData.getEnhanceValue(PropDataEnhanceType.FORGE, PropDataEnhanceType.Field.LUCKY); 
		int composeLuck = 0;
		if ( composeLuckInt != null ) {
			composeLuck=composeLuckInt.intValue();
		}
	  newValue = user.getLuck() + sign*(propData.getLuckLev()+composeLuck);
	  */
	  newValue = user.getLuck() + sign*(propData.getLuckLev());
	  if ( newValue < 0 ) newValue = 0;
	  user.setLuck(newValue);

	  // 玩家体力
	  int agilityUnit = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.GAME_AGILITY_UNIT, 5);
	  int agilityThew = (int)Math.abs((Math.round(totalAgility * 1.0/ agilityUnit))*sign);
	  int baseThew = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.USER_THEW_BASE, 210);
	  newValue = user.getTkew() + sign*(propData.getThewLev() + agilityThew);
	  if ( newValue < baseThew ) newValue = baseThew;
	  if ( newValue > 1200 ) newValue = 1200;
	  user.setTkew( newValue );
	  
	  // 玩家血量
  	if ( wearEquip ) {
  	  int blood = user.getBlood() + sign*propData.getBloodLev();
  	  user.setBlood(blood);
  	  float percent = 1.0f + propData.getBloodPercent()/100.0f;
  	  if ( percent > 1.01 ) {
  	  	user.setBlood(Math.round(blood * percent));
  	  }
  	} else {
  		float percent = 1.0f/(1.0f + propData.getBloodPercent()/100.0f);
  		int blood = user.getBlood();
  		if ( percent < 0.999 ) {
  			blood = Math.round(blood * percent);
  		}
  		blood = blood + sign*propData.getBloodLev();
  		user.setBlood(blood);
  	}
	  
	  // 玩家的伤害值
	  Integer composeDamageInt = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.DAMAGE); 
		int composeDamage = 0;
		if ( composeDamageInt != null ) {
			composeDamage=composeDamageInt.intValue();
		}
	  newValue = user.getDamage() + sign*(propData.getDamageLev()+composeDamage);
	  if ( newValue < 0 ) newValue = 0;
	  user.setDamage(newValue);
	  
	  // 玩家护甲
	  Integer composeSkinInt = propData.getEnhanceValue(PropDataEnhanceType.STRENGTH, PropDataEnhanceField.SKIN); 
		int composeSkin = 0;
		if ( composeSkinInt != null ) {
			composeSkin=composeSkinInt.intValue();
		}
	  newValue = user.getSkin() + sign*(propData.getSkinLev()+composeSkin);
	  user.setSkin(newValue);

	  // 战斗力
	  int power = (int)UserCalculator.calculatePower(user);
	  user.setPower(power);
	  
	  /**
	   * The ranking is moving to BceCloseBagHandler
	   * call User#updatePowerRanking() method instead
	   * wangqi 2012-8-8
	   */
	  /*
		//Update the rank data
		RankManager.getInstance().storeGlobalRankData(
			user, RankScoreType.POWER, System.currentTimeMillis());
		//This is the task that use absolute power value
		TaskManager.getInstance().processUserTasks(user, 
				TaskHook.POWER, user.getPower());
		*/
	}
}
