package com.xinqihd.sns.gameserver.db.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The game's activity manager 
 * 
 * @author wangqi
 *
 */
public class BulletinManager {
	
	private static final Logger logger = LoggerFactory.getLogger(BulletinManager.class);
	
	private static final String SCRIPT_HOOK = "script.hook";

	public static final String PROMPT_MESSAGE = "prompt.message";

	private static BulletinManager instance = new BulletinManager();
	
	public static final String KEY_BULLETIN = "bulletin:message";
	
	/**
	 * 当用户登陆时，针对他个人的弹出消息
	 */
	public static final String KEY_USER_LOGIN = "userlogin:message";
		
	
	private BulletinManager() {
	}
	
	/**
	 * Get the singleton ActivityManager
	 * @return
	 */
	public static final BulletinManager getInstance() {
		return instance;
	}
	
	/**
	 * When an user login, check the bulletin board for message to display
	 * @param user
	 */
	public void displayBulletin(User user) {
		Jedis jedis = JedisFactory.getJedisDB();
		String message = jedis.get(KEY_BULLETIN);
		if ( StringUtil.checkNotEmpty(message) ) {
			SysMessageManager.getInstance().sendClientInfoRawMessage(
					user.getSessionKey(), message, Action.NOOP, Type.CONFIRM);
		}
	}
	
	/**
	 * 这个方法给用户发送一条离线待确认消息，用户下次登陆后确认，将执行一段脚本，并传入指定的参数
	 * 
	 * @param userId The target user's id
	 * @param hook The pending script
	 * @param params maybe null
	 */
	public void sendPersonalConfirmMessage(String userId, ScriptHook hook, 
			HashMap<String, String> params, int expire) {
		Jedis jedis = JedisFactory.getJedisDB();
		String key = StringUtil.concat(KEY_USER_LOGIN, userId.toString());
		if ( params == null ) {
			params = new HashMap<String, String>();
		}
		params.put(SCRIPT_HOOK, hook.getHook());
		jedis.hmset(key, params);
		if ( expire <= 0 ) {
			expire = 86400;
		}
		jedis.expire(key, expire);
	}
	
	/**
	 * When the user login, check the confirm message for him
	 * @param userId
	 */
	public void displayPersonalConfirmMessage(final User user) {
		Jedis jedis = JedisFactory.getJedisDB();
		String key = StringUtil.concat(KEY_USER_LOGIN, user.get_id().toString());
		final Map<String, String> map = jedis.hgetAll(key);
		if ( map != null && map.size() > 0  ) {
			try {
				String message = map.get(PROMPT_MESSAGE);
				ConfirmManager.getInstance().sendConfirmMessage(user, message, key, 
						new ConfirmManager.ConfirmCallback() {

							@Override
							public void callback(User user, int selected) {
								if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
									String scriptHookStr = map.get(SCRIPT_HOOK);
									if ( scriptHookStr != null ) {
										ScriptHook hook = ScriptHook.getScriptHook(scriptHookStr);
										ScriptManager.getInstance().runScript(hook, user, map);
									}
								}
							}
						});
			} catch (Exception e) {
				logger.warn("Failed to ", e);
			} finally {
				jedis.del(key);
			}
		}
	}
	
	/**
	 * Send a bulletin message to all users
	 * type:
	 * 0:  sys message type
   * 1:  confirm message type
   * expire > 0: save in database
   * expire <=0 : only display to online users
	 * 
	 * @param user
	 */
	public void sendBulletinMessage(String message, int type, int expire) {
		//Display to all online users
		if ( StringUtil.checkNotEmpty(message) ) {
			List<SessionKey> allOnlineUsers = GameContext.getInstance().
					findAllOnlineUsers();
			for ( SessionKey sessionKey : allOnlineUsers ) {
				if ( type <= 0 ) {
					SysMessageManager.getInstance().sendClientInfoRawMessage(
							sessionKey, message, 5000);
				} else {
					SysMessageManager.getInstance().sendClientInfoRawMessage(
							sessionKey, message, Action.NOOP, Type.CONFIRM);
				}
			}
			if ( expire>0 ) {
				Jedis jedis = JedisFactory.getJedisDB();
				jedis.set(KEY_BULLETIN, message);
				jedis.expire(KEY_BULLETIN, expire);
			}
		}
	}
}
