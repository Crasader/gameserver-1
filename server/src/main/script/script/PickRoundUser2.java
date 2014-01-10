package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.battle.BattleRoom;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.DelayComparator;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.script.ScriptResult.Type;

/**
 * 以阵营为单位，分别计算双方的回合的数量和次数
 * 
 * @author wangqi
 * 
 */
public class PickRoundUser2 {
	
	private static final Logger logger = LoggerFactory.getLogger(PickRoundUser2.class);

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
		BattleRoom battleRoom  = (BattleRoom) parameters[2];
		List userLeftList  = (List) parameters[3];
		List userRightList  = (List) parameters[4];
		
		//首先：计算当前战斗中所有玩家的最大敏捷值
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
		
		/**
		 * 其次：计算每一个回合，根据agility，各个玩家需要额外增加的delay数值
		 */
		if ( lastRoundOwner != null ) {
			for (Iterator iterator = battleUsers.iterator(); iterator.hasNext();) {
				BattleUser user = (BattleUser) iterator.next();
				if ( user == lastRoundOwner ) {
					int userDelay = user.getDelay();
					int userAgility = user.getUser().getAgility();
					if ( userAgility == 0 ) userAgility = 1;
					float addedDelay = base*(maxAgility - userAgility)/userAgility;
					logger.info("User {} delay {} and addedDelay {}", 
							new Object[]{user.getUser().getRoleName(), userDelay, addedDelay});
					//userDelay *= delayRatio;
					userDelay += addedDelay;
					user.setDelay(userDelay);
				}
			}//for ...
		}
		
	  //第三：计算两个房间的敏捷值总和，敏捷值低的房间先出手
		boolean isRoundOwnerInLeftRoom = false;
		boolean found = false;
		int roomCount = 0;
		Collection nextBattleUserList = null;
		int roomLeftDelay = 0;
		int roomRightDelay = 0;
		int roomLeftAgility = 0;
		int roomRightAgility = 0;
		
		for (Iterator iterator = userLeftList.iterator(); iterator.hasNext();) {
			BattleUser bUser = (BattleUser) iterator.next();
			roomLeftDelay+=bUser.getDelay();
			roomLeftAgility+=bUser.getUser().getAgility();
			if ( lastRoundOwner != null && 
					bUser.getUserSessionKey().equals(lastRoundOwner.getUserSessionKey()) ) {
				battleRoom.setRoomLeftRound(battleRoom.getRoomLeftRound()+1);
				isRoundOwnerInLeftRoom = true;
				found = true;
				roomCount = battleRoom.getRoomLeftRound();
			}
		}
		for (Iterator iterator = userRightList.iterator(); iterator.hasNext();) {
			BattleUser bUser = (BattleUser) iterator.next();
			roomRightDelay+=bUser.getDelay();
			roomRightAgility+=bUser.getUser().getAgility();
			if ( lastRoundOwner != null && 
					bUser.getUserSessionKey().equals(lastRoundOwner.getUserSessionKey()) ) {
				battleRoom.setRoomRightRound(battleRoom.getRoomRightRound()+1);
				isRoundOwnerInLeftRoom = false;
				found = true;
				roomCount = battleRoom.getRoomRightRound();
			}
		}
		
		if ( roomLeftDelay == 0 && roomLeftDelay == roomRightDelay ) {
			//第一次出手
			if ( roomLeftAgility >= roomRightAgility ) {
				if ( battleRoom.getRoomLeftRound() <= 2 ) {
					nextBattleUserList = userLeftList;
					battleRoom.setRoomLeftRound(battleRoom.getRoomLeftRound()+1);
				} else {
					nextBattleUserList = userRightList;
					battleRoom.setRoomLeftRound(0);
					battleRoom.setRoomRightRound(1);
				}
			} else if ( roomLeftAgility < roomRightAgility ) {
				if ( battleRoom.getRoomRightRound() <= 2 ) {
					nextBattleUserList = userRightList;
					battleRoom.setRoomRightRound(battleRoom.getRoomRightRound()+1);
				} else {
					nextBattleUserList = userLeftList;
					battleRoom.setRoomLeftRound(1);
					battleRoom.setRoomRightRound(0);
				}
			}
		} else if ( roomLeftDelay <= roomRightDelay ) {
			//如果左侧的房间延迟较低，并且连续回合数不超过2回合，则左侧出手
			if ( battleRoom.getRoomLeftRound() <= 2 ) {
				nextBattleUserList = userLeftList;
				battleRoom.setRoomLeftRound(battleRoom.getRoomLeftRound()+1);
			} else {
				nextBattleUserList = userRightList;
				battleRoom.setRoomLeftRound(0);
				battleRoom.setRoomRightRound(1);
			}
		} else if ( roomRightDelay <= roomLeftDelay ) {
			if ( battleRoom.getRoomRightRound() <= 2 ) {
				nextBattleUserList = userRightList;
				battleRoom.setRoomRightRound(battleRoom.getRoomRightRound()+1);
			} else {
				nextBattleUserList = userLeftList;
				battleRoom.setRoomLeftRound(1);
				battleRoom.setRoomRightRound(0);
			}
		} else {
			nextBattleUserList = battleUsers;
		}

		BattleUser roundOwner = null;
		
		TreeSet battleUserSet = new TreeSet(DelayComparator.getInstance());
		battleUserSet.addAll(nextBattleUserList);
		
		for (Iterator iterator = battleUserSet.iterator(); iterator.hasNext();) {
			BattleUser user = (BattleUser) iterator.next();
			if ( user != null && !user.containStatus(RoleStatus.DEAD) ) {
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
			/**
			 * 没有找到任何符合条件的玩家，则使用合理玩家
			 */
			battleUserSet = new TreeSet(DelayComparator.getInstance());
			battleUserSet.addAll(battleUsers);
			
			for (Iterator iterator = battleUserSet.iterator(); iterator.hasNext();) {
				BattleUser user = (BattleUser) iterator.next();
				if ( user != null && !user.containStatus(RoleStatus.DEAD) ) {
					roundOwner = user;
				}
			}
		} else {
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
