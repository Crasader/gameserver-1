package com.xinqihd.sns.gameserver.admin.config;

/**
 * The system config key
 * @author wangqi
 *
 */
public enum ConfigKey {

	gameHost,
	gamePort,
	adminUsername,
	adminPassword,
	//The mongo database for this management system.
	//The account and security information are stored there.
	adminDatabaseServer,
	
	gameMongoHost,
	gameMongoPort,
	mongoDBName,
	mongoNamespace,
	gameMongoConfigHost,
	gameMongoConfigPort,
	mongoConfigDBName,
	mongoConfigNamespace,
	gameRedisHost,
	gameRedisPort,
	gameRedisDBHost,
	gameRedisDBPort,
	
	dataExportDir,
	weaponExportDir,
	
	mysqlHost,
	mysqlPort,
	
	
	backupDir,
	
	genWeaponNamePrefix,
	//保存子弹文件的路径
	bulletDir,
}
