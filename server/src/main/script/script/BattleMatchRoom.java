package script;

import java.util.ArrayList;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * 
 * 以下行为增加的DELAY值	
 * 行为	      DELAY值
 *  开炮	        100
 *  蓄力	        100
 *  小飞机	       55
 *  该轮时间结束	100
 *  移动	          0
 * 
 * @author wangqi
 *
 */
public class BattleMatchRoom {
	
	private static final String REDIS_ROOM_MATCH = "match:";

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		Room roomLeft = (Room)parameters[0];
		Room roomRight = (Room)parameters[1];
		
		ArrayList list = new ArrayList();
		
		/**
		 * If there is a room with GUILD type, then the other one should be 
		 * guild too.
		 * 
		 * 2013-3-12
		 */
		boolean matched = true;
		if ( roomLeft.getRoomType() == RoomType.GUILD_ROOM ) {
			if ( roomRight.getRoomType() == RoomType.GUILD_ROOM ) {
				matched = true;
			} else {
				matched = false;
			}
		} else if ( roomRight.getRoomType() == RoomType.GUILD_ROOM ) {
			matched = false;
		}
		if ( matched ) {
			/**
			 * Check if the two room's owner's level is in range.
			 */
			int diff = Math.abs(roomLeft.getMaxLevel() - roomRight.getMaxLevel());
//		int maxDiff = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_MATCH_POWER, 500);
			int maxDiff = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.ROOM_MATCH_LEVEL, 5);
			if ( roomLeft.getRoomType() == RoomType.GUILD_ROOM ) {
				maxDiff *= 2;
			}
			if ( diff < maxDiff ) {
				matched = true;
			} else {
				matched = false;
			}
		}
		if ( matched ) {
			if ( roomLeft.getRoomType() == RoomType.SINGLE_ROOM ) {
				/**
				 * For two single type rooms, the system prevent the two room owner
				 * are repeated matched. 
				 */
				UserId leftUserId = GameContext.getInstance().
						getSessionManager().findUserIdBySessionKey(roomLeft.getOwnerSessionKey());
				UserId rightUserId = GameContext.getInstance().
						getSessionManager().findUserIdBySessionKey(roomRight.getOwnerSessionKey());
				if ( leftUserId != null && rightUserId != null ) {
					int timeoutSecond = GameDataManager.getInstance().getGameDataAsInt(
							GameDataKey.BATTLE_ROOM_MATCH_TIMEOUT, 300);
					
					String key = null;
					if ( leftUserId.toString().compareTo(rightUserId.toString()) <= 0 ) {
						key = StringUtil.concat( 
								new Object[]{REDIS_ROOM_MATCH, leftUserId.toString(), rightUserId.toString()});
					} else {
						key = StringUtil.concat( 
								new Object[]{REDIS_ROOM_MATCH, rightUserId.toString(), leftUserId.toString()});
					}
					Jedis jedis = JedisFactory.getJedis();
					if ( jedis.exists(key) ) {
						//they are already matched.
						matched = false;
					} else {
						jedis.set(key, Constant.ONE);
						jedis.expire(key, timeoutSecond);
						matched = true;
					}
				} else {
					matched = false;
				}
			}
		}
		
		list.add(matched);
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
