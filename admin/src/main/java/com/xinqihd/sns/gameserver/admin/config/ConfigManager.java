package com.xinqihd.sns.gameserver.admin.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Manager the global configuration.
 * @author wangqi
 *
 */
public class ConfigManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	
	public static final String DEFAULT_CONFIG_FILE_PATH = 
			StringUtil.concat(System.getProperty("user.home"), File.separator, ".gameadmin.properties");
	
	public static final File DEFAULT_CONFIG_FILE = new File(DEFAULT_CONFIG_FILE_PATH);
	
	private static final HashMap<ConfigKey, Object> configMap = new HashMap<ConfigKey, Object>();
	
	static {
		logger.debug("The default config file is {}", DEFAULT_CONFIG_FILE.getAbsolutePath());
		if ( !DEFAULT_CONFIG_FILE.exists() ) {
			try {
				boolean mkDirs = DEFAULT_CONFIG_FILE.getParentFile().mkdirs();
				boolean mkFile = DEFAULT_CONFIG_FILE.createNewFile();
				logger.debug("Make the new config file. Result:{}", mkFile);
			} catch (IOException e) {
				logger.warn("Failed to create the config file.", e);
			}
		}
		try {
			FileReader fr = new FileReader(DEFAULT_CONFIG_FILE);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while ( line != null ) {
				String[] fields = line.split("=");
				String keyStr = fields[0].trim();
				String value = fields[1].trim();
				ConfigKey key = ConfigKey.valueOf(keyStr);
				if ( key != null ) {
					if ( configMap.containsKey(key) ) {
						Object existValue = configMap.get(key);
						if ( existValue instanceof Set ) {
							((Set)existValue).add(value);
						} else {
							Set<String> set = new LinkedHashSet<String>();
							set.add(existValue.toString());
							set.add(value);
							configMap.put(key, set);
						}
					} else {
						configMap.put(key, value);
					}
					logger.debug("Config Key:{}, value:{}", key, value);
				} else {
					logger.warn("Unknown config key: {}", keyStr);
				}
				line = br.readLine();
			}
			br.close();
			
		} catch (Exception e) {
			logger.error("Failed to read the config file", e);
		}
	}
	
	/**
	 * Get the specified value as string.
	 * @param key
	 * @return
	 */
	public static final String getConfigAsString(ConfigKey key) {
		Object value = configMap.get(key);
		if ( value != null ) {
			if ( value instanceof Set ) {
				Set list = (Set)value;
				if ( list.size()>0 ) {
					return (String) list.iterator().next();
				} else {
					return null;
				}
			} else {
				return value.toString();
			}
		}
		return null;
	}
	
	/**
	 * Get the specified value as string.
	 * @param key
	 * @return
	 */
	public static final Set<String> getConfigAsStringArray(ConfigKey key) {
		Object value = configMap.get(key);
		if ( value != null && value instanceof Set ) {
			return (Set<String>)value;
		}
		return new HashSet<String>();
	}


	/**
	 * Save a new key/value pair into config system.
	 * @param key
	 * @param value
	 * @return
	 */
	public static final void saveConfigKeyValue(ConfigKey key, String value) {
		configMap.put(key, value);
		saveFile();
	}
	
	/**
	 * Save a new key/value pair into config system.
	 * @param key
	 * @param value
	 * @return
	 */
	public static final void saveConfigKeyValue(ConfigKey key, Collection<String> values) {
		Set<String> set = new LinkedHashSet<String>(values);
		configMap.put(key, set);
		saveFile();
	}
	
	/**
	 * Save the underlying file
	 */
	private static final void saveFile() {
		try {
			//Create the global saver
			FileWriter fw = new FileWriter(DEFAULT_CONFIG_FILE, false);
			BufferedWriter saver = new BufferedWriter(fw);
			for ( ConfigKey key : configMap.keySet() ) {
				Object value = configMap.get(key);
				if ( value instanceof Collection ) {
					Collection<String> values = (Collection<String>)value;
					for ( String v : values ) {
						try {
							saver.append(key.name());
							saver.append("=");
							saver.append(v);
							saver.append('\n');
							saver.flush();
						} catch (IOException e) {
							logger.warn("Failed to store the config", e);
						}
					}
				} else {
					saver.append(key.name());
					saver.append("=");
					saver.append(value.toString());
					saver.append('\n');
					saver.flush();
				}
			}
			saver.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
