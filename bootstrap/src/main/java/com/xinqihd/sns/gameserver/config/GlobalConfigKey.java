package com.xinqihd.sns.gameserver.config;

/**
 * The config key used as an enumerator
 * @author wangqi
 *
 */
public enum GlobalConfigKey {
	//The config database's host
	mongo_configdb_host,
	mongo_configdb_port,
	mongo_configdb_database,
	mongo_configdb_namespace,
	
	//The map and bullet data deployed dir
	deploy_data_dir,
	//The chat word filter file path
	chat_word_file,
	
	//Timer
	battle_checker_seconds,
	//The battle object max live seconds.
	//It is used to prevent battle memory leak.
	battle_max_live_seconds,
	//The user session's timeout seconds
	session_timeout_seconds,
  //The room that is in combat mode's timeout seconds
	combat_room_timeout_seconds,
	
	//Persistent Redis
	jedis_db_host,
	jedis_db_port,
	
	//SimpleClient
	simple_client_connect_timeout,
	
	//mysql
	mysql_billing_database,
	mysql_billing_username,
	mysql_billing_password,
	mysql_billing_server,
	mysql_billing_max_conn,
	mysql_billing_min_conn,

	//discuz mysql
	mysql_discuz_database,
	mysql_discuz_username,
	mysql_discuz_password,
	mysql_discuz_server,
	mysql_discuz_max_conn,
	mysql_discuz_min_conn,
	mysql_discuz_table_name,
	
	//Battle distributed
	battle_distributed,
	battle_pool_core_size,
	battle_pool_max_size,
	battle_pool_keepalive_seconds,
	
	//The game's official site url
	official_site,
	
	battle_bullettrack_seconds,
	
	stat_enabled,
	stat_host,
	stat_port,
	
	maintaince_mode,
	maintaince_url,
	
	ios_push_develop,
	ios_push_certificate_password,
	ios_push_production_pem,
	ios_push_development_pem,
	
	admin_email,
	
	//小米渠道用于验证消息完整性和真实性的key
	xiaomi_appkey,
	
	tcp_sendbuf,
	tcp_recvbuf,
	tcp_backlog,
}
