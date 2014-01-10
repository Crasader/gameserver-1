package com.xinqihd.sns.gameserver.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It contains the global game information
 * @author wangqi
 *
 */
public class GlobalConfig {
	
	private static final Log log = LogFactory.getLog(GlobalConfig.class);
	
	private String configFileName = "gameserver.properties";
	
	private Properties properties = new Properties();
	
	private static GlobalConfig instance = new GlobalConfig();
	
	//The ZooKeeper's root for message server list at runtime.
	public static final String RUNTIME_MESSAGE_LIST_ROOT = "runtime.zoo_message";
	
  //The machine id for local message server ( hostname:port )
	public static final String RUNTIME_MESSAGE_SERVER_ID = "runtime.local_messageserver";

	//The ZooKeeper's root for message server list at runtime.
	public static final String RUNTIME_HOSTNAME = "runtime.hostname";

	//The machine id for rpc server.
	public static final String RUNTIME_RPC_SERVERID = "runtime.rpcserverid";
	
	//The machine id for game server.
	public static final String RUNTIME_GAME_SERVERID = "runtime.gameserverid";
	
	//The machine id for ai server.
	public static final String RUNTIME_AI_SERVERID = "runtime.aiserverid";
	
	//The machine id for http server.
	public static final String RUNTIME_HTTP_SERVERID = "runtime.httpserverid";
	
	//The uncompressed script files dir.
	public static final String RUNTIME_TMP_DIR = "runtime.tmpdir";
	
	//The script files directory.
	public static final String RUNTIME_SCRIPT_DIR = "runtime.script.dir";
		
	private GlobalConfig() {
		boolean loadSuccess = false;
		String configDir = System.getProperty("configdir");
		if ( configDir == null ) configDir = ".";
		File configFile = new File(configDir, configFileName);
		if ( configFile.exists() ) {
			try {
				log.info("Load config file from " + configFile.getAbsolutePath());
				properties.load(new FileInputStream(configFile));
				loadSuccess = true;
			} catch (IOException e) {
				log.error("Failed to load config: " + configFile, e);
			}
		}
		if ( !loadSuccess ) {
			properties.setProperty("mongdb.host", "mongos.babywar.xinqihd.com");
//			properties.setProperty("mongdb.host", "localhost");
			properties.setProperty("mongdb.port", "27017");
			properties.setProperty("mongdb.connectionsPerHost", "100");
			properties.setProperty("mongdb.threadsAllowedToBlockForConnectionMultiplier", "10");
			//maxWaitTime = 1000 * 60 * 2;
			properties.setProperty("mongdb.maxWaitTime", "120000");
			properties.setProperty("mongdb.connectTimeout", "0");
			properties.setProperty("mongdb.socketTimeout", "0");
			
			properties.setProperty("mongdb.safewrite", "true");
			
			properties.setProperty("mongdb.database", "babywar");
			properties.setProperty("mongdb.namespace", "server0001");
			
			//Mongo ConfigDB
			properties.setProperty(GlobalConfigKey.mongo_configdb_host.name(), "mongos.babywar.xinqihd.com");
			properties.setProperty(GlobalConfigKey.mongo_configdb_port.name(), "27017");
			properties.setProperty(GlobalConfigKey.mongo_configdb_database.name(), "babywarcfg");
			properties.setProperty(GlobalConfigKey.mongo_configdb_namespace.name(), "server0001");

			properties.setProperty("zookeeper.root", "/snsgame/babywar");
			properties.setProperty("zookeeper.message.root", "/messageservers");
			
			//Message Server
			properties.setProperty("message.host", "localhost");
			properties.setProperty("message.port", "3444");
			properties.setProperty("message.heartbeat.second", "60");
			
			//User data.
			properties.setProperty("user.basic.weapon", "570");
			
			properties.setProperty("common.server.idle.seconds", "600");
			
			//Jedis
			properties.setProperty("jedis.master.host", "redis.babywar.xinqihd.com");
			properties.setProperty("jedis.master.port", "6379");
			//Persistent Jedis
			properties.setProperty(GlobalConfigKey.jedis_db_host.name(), "redisdb.babywar.xinqihd.com");
			properties.setProperty(GlobalConfigKey.jedis_db_port.name(), "6379");
			
//			properties.setProperty("jedis.master.pass", "Xinqi#$123");
			properties.setProperty("jedis.master.pool.maxActive", 											"200");
			properties.setProperty("jedis.master.pool.minIdle",    											"1");
			properties.setProperty("jedis.master.pool.numTestsPerEvictionRun",    			"1");
			properties.setProperty("jedis.master.pool.timeBetweenEvictionRunsMillis",   "30000");
			properties.setProperty("jedis.master.pool.minEvictableIdleTimeMillis",    	"60000");
			
			//Timer
			properties.setProperty(GlobalConfigKey.battle_checker_seconds.name(),    	  "5");
			properties.setProperty(GlobalConfigKey.battle_max_live_seconds.name(),    	"5");
			properties.setProperty(GlobalConfigKey.session_timeout_seconds.name(),    	"10");
			properties.setProperty(GlobalConfigKey.combat_room_timeout_seconds.name(),  "900");
			
			//RoomManager
//			properties.setProperty("room.ready.timeout",   "30000");
//			properties.setProperty("room.userjoin.timeout",   "15000");
			
			//CipherManager
			properties.setProperty("cipher.timeout", "3600000");
			
			//SimpleClient
			properties.setProperty(GlobalConfigKey.simple_client_connect_timeout.name(), "3000");
			
			//Mysql
			properties.setProperty(GlobalConfigKey.mysql_billing_database.name(), "babywardb");
			properties.setProperty(GlobalConfigKey.mysql_billing_username.name(), "root");
			properties.setProperty(GlobalConfigKey.mysql_billing_password.name(), "r00t123");
			properties.setProperty(GlobalConfigKey.mysql_billing_server.name(),   "mysql.babywar.xinqihd.com");
			properties.setProperty(GlobalConfigKey.mysql_billing_max_conn.name(),   "50");
			properties.setProperty(GlobalConfigKey.mysql_billing_min_conn.name(),   "2");
			
			//Discuz Mysql
			properties.setProperty(GlobalConfigKey.mysql_discuz_database.name(), "ultrax");
			properties.setProperty(GlobalConfigKey.mysql_discuz_username.name(), "root");
			properties.setProperty(GlobalConfigKey.mysql_discuz_password.name(), "r00t123");
			properties.setProperty(GlobalConfigKey.mysql_discuz_server.name(),   "mysqlforum.babywar.xinqihd.com");
			properties.setProperty(GlobalConfigKey.mysql_discuz_max_conn.name(),   "50");
			properties.setProperty(GlobalConfigKey.mysql_discuz_min_conn.name(),   "2");
			properties.setProperty(GlobalConfigKey.mysql_discuz_table_name.name(), "ultrax.pre_ucenter_members");
			
			//Pool
			properties.setProperty(GlobalConfigKey.battle_distributed.name(),   "true");
			properties.setProperty(GlobalConfigKey.battle_pool_core_size.name(),   "10");
			properties.setProperty(GlobalConfigKey.battle_pool_max_size.name(),   "1000");
			properties.setProperty(GlobalConfigKey.battle_pool_keepalive_seconds.name(),   "10");
			
			properties.setProperty(GlobalConfigKey.official_site.name(),   "xxfd.changyou.com");
			
			properties.setProperty(GlobalConfigKey.battle_bullettrack_seconds.name(),   "10000");
			
			properties.setProperty(GlobalConfigKey.stat_enabled.name(),   "true");
			properties.setProperty(GlobalConfigKey.stat_host.name(),      "stat.babywar.xinqihd.com");
			properties.setProperty(GlobalConfigKey.stat_port.name(),      "10000");
			
			properties.setProperty(GlobalConfigKey.maintaince_mode.name(),     "false");
			properties.setProperty(GlobalConfigKey.maintaince_url.name(),      "http://maintaince.babywar.xinqihd.com/");
			
			properties.setProperty(GlobalConfigKey.ios_push_develop.name(),     "true");
			properties.setProperty(GlobalConfigKey.ios_push_certificate_password.name(),     "123456");
			properties.setProperty(GlobalConfigKey.ios_push_development_pem.name(),  "../deploy/pki/ios/gen_aps_development.p12");
			properties.setProperty(GlobalConfigKey.ios_push_production_pem.name(),   "../deploy/pki/ios/gen_aps_production.p12");
			
			properties.setProperty(GlobalConfigKey.admin_email.name(),   "to.wangqi@gmail.com");
			
			try {
				File outFile = new File("gameserver.properties");
				FileOutputStream fos = new FileOutputStream(outFile);
				properties.store(fos, "Default GameServer settings created at " + new Date());
			} catch ( Throwable t) {}
		}
	}
	
	/**
	 * Get the GlobalConfig.
	 * @return
	 */
	public static GlobalConfig getInstance() {
		return instance;
	}
	
	/**
	 * Get the string value.
	 * @param key
	 * @return
	 * @deprecated 
	 */
	public String getStringProperty(String key) {
		String value = properties.getProperty(key);
		if ( value == null ) {
			value = System.getProperty(key);
		}
		return value;
	}
	
	/**
	 * Get the string value.
	 * @param key
	 * @return
	 */
	public String getStringProperty(GlobalConfigKey key) {
		return getStringProperty(key.name());
	}

	/**
	 * Get the int value.
	 * @param key
	 * @return
	 * @deprecated 
	 */
	public int getIntProperty(String key) {
		String value = properties.getProperty(key);
		int result = StringUtil.toInt(value, 0);
		return result;
	}
	
	/**
	 * Get the int value.
	 * @param key
	 * @return
	 */
	public int getIntProperty(GlobalConfigKey key) {
		return getIntProperty(key.name());
	}
	
	/**
	 * Get the boolean value.
	 * @param key
	 * @return
	 * @deprecated 
	 */
	public boolean getBooleanProperty(String key) {
		String value = properties.getProperty(key);
		return Boolean.parseBoolean(value);
	}
	
	/**
	 * Get the boolean value.
	 * @param key
	 * @return
	 */
	public boolean getBooleanProperty(GlobalConfigKey key) {
		return getBooleanProperty(key.name());
	}
	
	/**
	 * Override a property in system.
	 * 
	 * @param key
	 * @param value
	 * @deprecated 
	 */
	public void overrideProperty(String key, String value) {
		if ( value == null ) {
			this.properties.remove(key); 
		} else {
			this.properties.setProperty(key, value);
		}
	}
	
	/**
	 * Override a property in system.
	 * 
	 * @param key
	 * @param value
	 */
	public void overrideProperty(GlobalConfigKey key, String value) {
		overrideProperty(key.name(), value);
	}
	
	@Override
	public String toString() {
		Enumeration propNames = this.properties.propertyNames();
		StringBuilder buf = new StringBuilder(256);
		while ( propNames.hasMoreElements() ) {
			String key = (String)propNames.nextElement();
			buf.append(key).append('=').append(this.properties.getProperty(key)).append('\n');
		}
		return buf.toString();
	}
}
