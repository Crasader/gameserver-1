package script.task;

import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.TaskPojo;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.TaskStep;

/**
	【活动时间】 2012.11.16 至 2012.11.30
	
	【活动奖励】 
	
	火神石LV4 X1
	寻宝卡 X1
	
	【活动内容】：
	
	  《小小飞弹Online》新版本V1.7.0已火热上线，两个世界副本现已开放。玩家只需每天与好友组队，成功挑战5次副本，即可赢取活动奖励。寻宝卡可令您额外参与一次世界寻宝，大量的橙色装备等您来拿！
	
	详情请见游戏内活动公告！
	以上活动最终解释权归畅游移动游戏所有！
 *
 */
public class PVECombatWin {

	public static ScriptResult func(Object[] parameters) {
		ScriptResult result = ScriptManager.checkParameters(parameters, 3);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		TaskPojo task = (TaskPojo)parameters[1];
		TaskHook hook = (TaskHook)parameters[2];
		if ( hook == TaskHook.COMBAT ) {
			Object[] array = (Object[])parameters[3];
			boolean winner = (Boolean)array[0];
			int totalUserNumber = (Integer)array[1];
			RoomType roomType = (RoomType)array[2];
			
			if ( roomType == RoomType.PVE_ROOM ) {
				if ( winner && totalUserNumber>2 ) {
					TaskStep.step(task, user);
				}
			}
		}
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS);
		result.setResult(null);
		return result;
	}
	
}
