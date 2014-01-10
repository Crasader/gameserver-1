package com.xinqihd.sns.gameserver.admin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The manager for priviledge and user
 * @author wangqi
 *
 */
public class PriviledgeManager {
	
	private static final Logger logger = LoggerFactory.getLogger(PriviledgeManager.class);

	private static PriviledgeManager instance = new PriviledgeManager();
	
	
	private PriviledgeManager() {
		reload();
	}
	
	public void reload() {
		
	}
	
	/**
	 * Query all the priviledgeKey for given AdminUser.
	 * @param admin
	 * @param priviledgeKey
	 * @return
	 */
	public void queryPrivledgeKey(AdminUser admin, PriviledgeKey priviledgeKey) {
		
	}
	
}
