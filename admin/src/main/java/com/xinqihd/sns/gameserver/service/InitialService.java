package com.xinqihd.sns.gameserver.service;

import java.util.List;

import javax.swing.SwingWorker;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;
import com.xinqihd.sns.gameserver.admin.gui.MainPanel;
import com.xinqihd.sns.gameserver.admin.gui.StatusBar;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.admin.util.MongoUtil;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Intialize the underlying system
 * @author wangqi
 *
 */
public class InitialService extends SwingWorker<Void, Void> {
	
	StatusBar bar = MainPanel.getInstance().getStatusBar();

	public InitialService() {
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		//Apply the change to system.
		String mongoHost = ConfigManager.getConfigAsString(ConfigKey.gameMongoHost);
		String mongoPort = ConfigManager.getConfigAsString(ConfigKey.gameMongoPort);
		String mongoConfigHost = ConfigManager.getConfigAsString(ConfigKey.gameMongoConfigHost);
		String mongoConfigPort = ConfigManager.getConfigAsString(ConfigKey.gameMongoConfigPort);
		String redisHost = ConfigManager.getConfigAsString(ConfigKey.gameRedisHost);
		String redisPort = ConfigManager.getConfigAsString(ConfigKey.gameRedisPort);
		String redisDBHost = ConfigManager.getConfigAsString(ConfigKey.gameRedisDBHost);
		String redisDBPort = ConfigManager.getConfigAsString(ConfigKey.gameRedisDBPort);
		
		//设置默认数据库
		GlobalConfig.getInstance().overrideProperty("mongdb.host", mongoConfigHost);
		GlobalConfig.getInstance().overrideProperty("mongdb.port", mongoConfigPort);
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_host.name(), mongoConfigHost);
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.mongo_configdb_port.name(), mongoConfigPort);
		
		bar.updateStatus("获取系统配置");
		bar.updateProgress(10);
		
		//Initialize MongoUtil
		MongoUtil.initUserMongo(mongoHost, StringUtil.toInt(mongoPort, 27017));
		bar.updateStatus("初始化Mongo数据库连接池");
		bar.updateProgress(50);
		
		MongoUtil.initCfgMongo(mongoConfigHost, StringUtil.toInt(mongoConfigPort, 27017));
		bar.updateStatus("初始化Mongo配置数据库连接池");
		bar.updateProgress(80);
		
		//Disable Jedis
		JedisFactory.disableJedis();
		bar.updateStatus("禁用Jedis连接池");
		bar.updateProgress(90);
		
		bar.updateStatus("初始化游戏环境");
		GameContext.getInstance();
		bar.updateProgress(100);
		
		return null;
	}


	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		bar.updateStatus("初始化完毕");
		bar.updateProgress(0);
	}
	
	
}
