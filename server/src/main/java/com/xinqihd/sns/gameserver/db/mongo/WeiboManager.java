package com.xinqihd.sns.gameserver.db.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.WeiboOpType;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceWeibo.BceWeibo;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;

/**
 * It is used to manage the user's weibo account.
 * 
 * @author wangqi
 *
 */
public class WeiboManager {

	private static final Logger logger = LoggerFactory.getLogger(WeiboManager.class);
	
	
	private static final WeiboManager instance = new WeiboManager();
		
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static WeiboManager getInstance() {
		return instance;
	}
	
	WeiboManager() {
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
	}
	
	/**
	 * Process the user's Weibo request
	 * @param user
	 * @param weibo
	 */
	public void processWeibo(User user, BceWeibo weibo) {
		if ( user == null ) {
			logger.debug("Null user for the Weibo request");
			return;
		}
		int opType = weibo.getOptype();
		if ( opType < 0 || opType > WeiboOpType.values().length ) {
			logger.debug("invalid weibo opType {}", opType);
		}
		WeiboOpType type = WeiboOpType.values()[opType];
		//qq or sina
		String weiboSource = weibo.getWeibo();
		//weibo token
		String weiboToken = weibo.getToken();
		
		logger.debug("processWeibo: user {} weibo {}. source:{}, token:{}.", 
				new Object[]{user.getRoleName(), type, weiboSource, weiboToken});
		
		//Call the TaskHook
		TaskManager.getInstance().processUserTasks(user, 
				TaskHook.WEIBO, type, weiboSource, weiboToken);
		
		StatClient.getIntance().sendDataToStatServer(user, 
				StatAction.Weibo, type, weiboSource, weiboToken);
		
		UserActionManager.getInstance().addUserAction(user.getRoleName(), 
				UserActionKey.Weibo);
	}
	
}
