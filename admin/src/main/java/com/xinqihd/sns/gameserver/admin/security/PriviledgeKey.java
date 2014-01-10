package com.xinqihd.sns.gameserver.admin.security;

/**
 * The priviledge keys for every priviledge in admin system.
 * @author wangqi
 *
 */
public enum PriviledgeKey {
	
	all_priviledge,
	manage_user_account,
	task_game_level,
	task_game_data,
	task_game_map,
	task_game_equipment,
	task_game_item,
	task_game_shop,
	task_game_tasks,
	task_game_tips,
	task_game_promotions,
	task_game_cdkeys,
	task_game_bosses,
	task_game_serverlist,
	task_game_rewards,
	task_game_logins,
	task_game_dailymarks,
	task_game_battletools,
	task_game_charges,
	task_game_vipperiods,
	
	task_game_addnew,
	task_game_delete,
	task_game_export,
	
	task_user_manage,
	task_guild_manage,
	task_user_notice,
	
	task_setting,
	task_servers,
	
	task_weapon_data_generator,
	task_shop_data_generator,
	task_weapon_balancer,
	task_craft_balancer,
	
	task_redisdb,
	task_reload_config
}
