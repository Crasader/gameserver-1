package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.DelayComparator;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;

/**
 * Pick a user from all battle users that will be the next round's owner.
 * 
 * The algorithm is in EXCEL "行动计算"
 * 
 * 1 计算全场所有活着的玩家的最高敏捷值
 * 2 循环所有存活玩家，其DELAY值按照这个公式计算：
 *     玩家的DELAY += BASE * ( 最大敏捷 - 玩家敏捷 ) / 最大敏捷 
 * 3 选出最小DELAY的单位，由它开始下一回合 
 * 
 * BASE数字可配置，推荐为10
 * 
 * 数值循环表
 * 玩家的敏捷从 0 - 70, Delay以一个随机数开始，循环100回合，每回合各玩家
 * 分别增加100的Delay，各个玩家的回合数如下：
 *
 *   User agility: 70, delay:1794, round: 17
 *   User agility: 60, delay:1784, round: 16
 *   User agility: 50, delay:1740, round: 15
 *   User agility: 40, delay:1776, round: 13
 *   User agility: 30, delay:1787, round: 12
 *   User agility: 20, delay:1785, round: 10
 *   User agility: 10, delay:1781, round: 9
 *   User agility: 0,  delay:1817, round: 8
 * 
 * 
 * @author wangqi
 * 
 */
public class PickRoundUser {
	
	private static final Logger logger = LoggerFactory.getLogger(PickRoundUser.class);

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 1);
		if (result != null) {
			return result;
		}
		try {
			BattleUser roundOwner = findRoundOwner(parameters);
			
			ArrayList list = new ArrayList();
			list.add(roundOwner);

			result = new ScriptResult();
			result.setResult(list);
			result.setType(Type.SUCCESS_RETURN);

			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Find proper round owner.
	 * @param parameters
	 * @return
	 */
	private static BattleUser findRoundOwner(Object[] parameters) {
		float maxAgility = Integer.MIN_VALUE;
		//1. Find the highest aligity user
		Collection battleUsers = (Collection) parameters[0];
		BattleUser lastRoundOwner = (BattleUser) parameters[1];
		
		for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
			BattleUser user = (BattleUser) iterator.next();
			if ( user != null && !user.containStatus(RoleStatus.DEAD) ) {
				if ( maxAgility < user.getUser().getAgility() ) {
					maxAgility = user.getUser().getAgility();
				}
			}
		}
		if ( maxAgility == 0 ) {
			logger.debug("maxAgility is 0");
			maxAgility = 50;
		}
		float base = GameDataManager.getInstance().getGameDataAsInt(
				GameDataKey.DELAY_AGLITY_BASE, 100);

		BattleUser roundOwner = null;
		
		if ( lastRoundOwner != null ) {
			/**
			 * 计算每一个回合，根据agility，各个玩家需要额外增加的delay数值
			 */
			for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser user = (BattleUser) iterator.next();
				if ( user == lastRoundOwner ) {
					int userDelay = user.getDelay();
					int userAgility = user.getUser().getAgility();
					if ( userAgility == 0 ) userAgility = 1;
					float addedDelay = base*(maxAgility - userAgility)/userAgility;
					//float delayRatio = maxAgility / userAgility;
//					if ( addedDelay == 0 ) {
//						addedDelay = 10;
//					}
					logger.info("User {} delay {} and addedDelay {}", new Object[]{user.getUser().getRoleName(), userDelay, addedDelay});
					//userDelay *= delayRatio;
					userDelay += addedDelay;
					user.setDelay(userDelay);
				}
			}//for ...
		}
		
		TreeSet battleUserSet = new TreeSet(DelayComparator.getInstance());
		battleUserSet.addAll(battleUsers);
		
		BattleUser validUser = null;
		for (Iterator iterator = battleUserSet.iterator(); iterator.hasNext();) {
			BattleUser user = (BattleUser) iterator.next();
			if ( user != null && !user.containStatus(RoleStatus.DEAD) ) {
				if ( validUser == null ) {
					validUser = user;
				}
				if ( !user.containStatus(RoleStatus.ICED) ) {
					if ( user.getRoundOwnerTimes() > 1 && iterator.hasNext() ) {
						BattleUser nextUser = (BattleUser) iterator.next();
						user.setDelay(nextUser.getDelay());
						roundOwner = nextUser;
					} else {
						roundOwner = user;
						break;
					}
				}
			}
		}

		if ( roundOwner == null ) {
			if ( validUser != null ) {
				roundOwner = validUser;
				logger.debug("Cannot pickup a round user because all users are dead or frozen. validUser: {}", validUser);
			} else {
				roundOwner = validUser;
			}
		}
		
		if ( roundOwner != null ) {
			roundOwner.setRoundOwnerTimes(roundOwner.getRoundOwnerTimes()+1);
			for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser user = (BattleUser) iterator.next();
				if ( user != null && user != roundOwner ) {
					user.setRoundOwnerTimes(0);
				}
			}
			logger.debug("The user '{}' round owner times is: {}", 
					roundOwner.getUser().getRoleName(), roundOwner.getRoundOwnerTimes());
		}
		return roundOwner;
	}

}
