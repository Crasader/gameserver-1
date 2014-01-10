package script;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

/**
 * Consume user's role action point when he / she enters a battle
 * 
 * @author wangqi
 *
 */
public class RoleActionConsume {
	
	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		Battle battle = (Battle)parameters[0];
		User user = (User)parameters[1];
		RoomType roomType = (RoomType)parameters[2];
		
		//check user's roleaction
		RoleActionManager.getInstance().updateRoleActionPointIfChanged(
				user, System.currentTimeMillis());
		if ( roomType == RoomType.SINGLE_ROOM ) {
			//单人对战消耗2点
			RoleActionManager.getInstance().consumeRoleActionPoint(
					user, 5, System.currentTimeMillis(), true);
		} else if ( roomType == RoomType.MULTI_ROOM ) {
			//多人对战消耗5点
			RoleActionManager.getInstance().consumeRoleActionPoint(
					user, 5, System.currentTimeMillis(), true);
		} else if ( roomType == RoomType.FRIEND_ROOM || 
				roomType == RoomType.OFFLINE_ROOM ) {
			//好友对战消耗5点
			RoleActionManager.getInstance().consumeRoleActionPoint(
					user, 5, System.currentTimeMillis(), true);
		} else if ( roomType == RoomType.PVE_ROOM ) {
			//副本对战消耗5点
			RoleActionManager.getInstance().consumeRoleActionPoint(
					user, 5, System.currentTimeMillis(), true);
		} else if ( roomType == RoomType.OFFLINE_ROOM ) {
			//离线对战消耗5点
			RoleActionManager.getInstance().consumeRoleActionPoint(
					user, 5, System.currentTimeMillis(), true);
		} else {
			//其他模式不消耗
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
}
