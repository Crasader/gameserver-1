package com.xinqihd.sns.gameserver.admin.util;

import com.xinqihd.sns.gameserver.admin.config.ConfigKey;
import com.xinqihd.sns.gameserver.admin.config.ConfigManager;

import redis.clients.jedis.Jedis;

public class AdminJedis {
	
	private String host;
	private int port;
	
	private static AdminJedis instance = new AdminJedis();
	
	public AdminJedis() {
		this.host = ConfigManager.getConfigAsString(ConfigKey.gameRedisDBHost);
		String portStr = ConfigManager.getConfigAsString(ConfigKey.gameRedisDBPort);
		this.port = Integer.parseInt(portStr);
	}
	
	public AdminJedis(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public static AdminJedis getInstance() {
		return instance;
	}

	public Jedis getJedis() {
		Jedis jedis = new Jedis(host, port);
		return jedis;
	}
}
