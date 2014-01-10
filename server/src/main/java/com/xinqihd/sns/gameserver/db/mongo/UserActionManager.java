package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.List;

import com.xinqihd.sns.gameserver.entity.user.UserAction;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

/**
 * 系统保存一个长度为500的链表，其中存储每个玩家的状态信息，为了实现国际化，翻译的细节在输出时实现。
 * 
 * @author wangqi
 *
 */
public class UserActionManager {
	
	private static final String REDIS_KEY = "useraction";
	
	private static final int MAX = 100;
	
	private static UserActionManager instance = new UserActionManager();
	
	private static final boolean ENABLE = false;
	
	private UserActionManager() {
	}
	
	/**
	 * Get the singleton ActivityManager
	 * @return
	 */
	public static final UserActionManager getInstance() {
		return instance;
	}
	
	/**
	 * Add a new user action to system.
	 * @param userAction
	 */
	public void addUserAction(UserAction userAction) {
		if ( ENABLE ) {
			Jedis jedis = JedisFactory.getJedis();
			//Pipeline pipeline = jedis.pipelined();
			jedis.lpush(REDIS_KEY, userAction.toString());
			jedis.ltrim(REDIS_KEY, 0, MAX);
			//pipeline.exec();
		}
	}
	
	/**
	 * Add the given user action.
	 * 
	 * @param roleName
	 * @param actionKey
	 */
	public void addUserAction(String roleName, UserActionKey actionKey) {
		if ( ENABLE ) {
			UserAction userAction = new UserAction();
			userAction.setRoleName(roleName);
			userAction.setTextKey(actionKey);
			addUserAction(userAction);
		}
	}
	
	/**
	 * Add the given user action with only one params.
	 * @param roleName
	 * @param actionKey
	 * @param param
	 */
	public void addUserAction(String roleName, UserActionKey actionKey, String param) {
		if ( ENABLE ) {
			UserAction userAction = new UserAction();
			userAction.setRoleName(roleName);
			userAction.setTextKey(actionKey);
			userAction.setParams(new String[]{param});
			addUserAction(userAction);
		}
	}
	
	/**
	 * Add the given user action with only one params.
	 * @param roleName
	 * @param actionKey
	 * @param param
	 */
	public void addUserAction(String roleName, UserActionKey actionKey, String param1, String param2) {
		if ( ENABLE ) {
			UserAction userAction = new UserAction();
			userAction.setRoleName(roleName);
			userAction.setTextKey(actionKey);
			userAction.setParams(new String[]{param1, param2});
			addUserAction(userAction);
		}
	}
	
	/**
	 * Get the latest user actions.
	 * @return
	 */
	public ArrayList<String> getUserActions(int limit) {
		if ( ENABLE ) {
			Jedis jedis = JedisFactory.getJedis();
			int max = Math.min(limit, MAX);
			List<String> actionList = jedis.lrange(REDIS_KEY, 0, max);
			if (actionList!=null) {
				ArrayList<String> userActions = new ArrayList<String>(
						actionList.size());
				for ( String str : actionList ) {
					UserAction userAction = UserAction.fromString(str);
					if ( userAction != null ) {
						userActions.add(userAction.getLocalizedMessage());
					}
				}
				return userActions;
			}
		}
		return null;
	}
	
}
