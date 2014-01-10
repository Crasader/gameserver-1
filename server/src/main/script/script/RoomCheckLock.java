package script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Caculate the current exp rate for a given user in a battle.
 * It can be used for promotion. For example, VIP users always
 * have double exp rate.
 * 
 * @author wangqi
 *
 */
public class RoomCheckLock {

	public static ScriptResult func(Object[] parameters) {
		int expRate = 1;
		ScriptResult result = ScriptManager.checkParameters(parameters, 2);
		if ( result != null ) {
			return result;
		}
		User user = (User)parameters[0];
		RoomType roomType = (RoomType)parameters[1];
		/**
		 * Check if some of the given mode is locked.
		 */
		boolean isRoomLocked = true;
		String info = null;
		Collection unlocks = user.getUnlocks();
		if ( unlocks != null ) {
			for (Iterator iterator = unlocks.iterator(); iterator.hasNext();) {
				Unlock unlock = (Unlock) iterator.next();
				if ( unlock.getFuncType() == GameFuncType.Room ) {
					if ( unlock.getFuncValue() == roomType.ordinal() ) {
						isRoomLocked = false;
						break;
					}
				}
			}
		}
		if ( roomType == RoomType.DESK_ROOM ) {
			info = Text.text("room.lock.desk", new Object[]{15});
		} else if ( roomType == RoomType.FRIEND_ROOM ) {
			info = Text.text("room.lock.friend", new Object[]{10});
		} else if ( roomType == RoomType.MULTI_ROOM ) {
			info = Text.text("room.lock.multi", new Object[]{5});
		} else if ( roomType == RoomType.PVE_ROOM ) {
			info = Text.text("room.lock.pve", new Object[]{2});
		} else if ( roomType == RoomType.SINGLE_ROOM ) {
			//info = Text.text("room.lock.single");
			isRoomLocked = false;
		} else {
			isRoomLocked = false;
		}
		
		ArrayList list = new ArrayList();
		list.add(isRoomLocked);
		list.add(info);
		
		result = new ScriptResult();
		result.setType(ScriptResult.Type.SUCCESS_RETURN);
		result.setResult(list);
		return result;
	}
	
}
