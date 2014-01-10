package com.xinqihd.sns.gameserver;

/**
 * The JNDI object's key
 * @author wangqi
 *
 */
public enum JndiContextKey {
	//The zookeeper
	zookeeper,
	//The memory redis
	redis,
	//The persistent redis
	redisdb,
	//The billing mysql database
	mysql_billing_db,
	//The discuz mysql database
	mysql_discuz_db
}
