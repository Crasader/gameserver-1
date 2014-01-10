package com.xinqihd.sns.gameserver.db.mongo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.config.CDKeyPojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.MathUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Allow users to input CD-KEY
 * 
 * @author wangqi
 *
 */
public class CDKeyManager extends AbstractMongoManager {

	private static final Logger logger = LoggerFactory.getLogger(CDKeyManager.class);
	
	private static final String COLL_NAME = "cdkeys";
	
	private static final String INDEX_NAME = "_id";
	
	public static final String REDIS_CDKEY = "cdkey";
	
	private static final String[] PADDING = new String[]{
		"0", "00", "000", "0000", "00000", "000000", "0000000"
	};

	private static HashMap<String, CDKeyPojo> dataMap = new HashMap<String, CDKeyPojo>();

	
	private static final CDKeyManager instance = new CDKeyManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static CDKeyManager getInstance() {
		return instance;
	}
	
	CDKeyManager() {
		super(
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_database),
				GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mongo_configdb_namespace),
				GlobalConfig.getInstance().getBooleanProperty("mongdb.safewrite"),
				COLL_NAME, INDEX_NAME);
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		List<DBObject> list = MongoDBUtil.queryAllFromMongo(null, databaseName, namespace, 
				COLL_NAME, null);
		synchronized (dataMap) {
			dataMap.clear();
			for ( DBObject obj : list ) {
				CDKeyPojo cdKey = (CDKeyPojo)MongoDBUtil.constructObject(obj);
				dataMap.put(cdKey.getId(), cdKey);
				logger.debug("Load CDKEY {} from database.", cdKey.getId());
			}
		}
	}
	
	/**
	 * Get all the avaiable cdkey config objects.
	 * 
	 * @return
	 */
	public Collection<CDKeyPojo> getCDKeys() {
		return dataMap.values();
	}
	
	/**
	 * Get the given CDKey by ID.
	 * @param id
	 * @return
	 */
	public CDKeyPojo getCDKeyById(String id) {
		return dataMap.get(id);
	}
	
	/**
	 * Add a new cdkey to the database.
	 * @param cdkey
	 */
	public void addCDKey(CDKeyPojo cdkey) {
		DBObject query = MongoDBUtil.createDBObject(Constant._ID, cdkey.getId());
		DBObject dbObj = MongoDBUtil.createMapDBObject(cdkey);
		MongoDBUtil.saveToMongo(query, dbObj, databaseName, namespace, COLL_NAME, isSafeWrite);
		this.dataMap.put(cdkey.getId(), cdkey);
	}
	
	/**
	 * 领取CDKEY任务
	 * @return
	 */
	public boolean takeCDKeyReward(User user, String cdkey) {
		Jedis jedis = JedisFactory.getJedisDB();
		String cdkeyId = jedis.hget(REDIS_CDKEY, cdkey);
		boolean success = false;
		if ( StringUtil.checkNotEmpty(cdkey) ) {
			if ( cdkeyId != null ) {
				CDKeyPojo keyPojo = this.getCDKeyById(cdkeyId);
				//Check the channel
				if ( keyPojo != null ) {
					if ( StringUtil.checkNotEmpty(keyPojo.getChannel()) ) {
						success = false;
						if ( user.getChannel().contains(keyPojo.getChannel() ) ) {
							success = true;
						} else {
							SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.nochannel", Type.CONFIRM);
						}
					}
					long currentMillis = System.currentTimeMillis();
					if ( success ) {
						success = false;
						if ( keyPojo.getStartMillis() > 0 ) {
							if ( keyPojo.getStartMillis() < currentMillis ) {
								success = true;
							} else {
								SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.begintime", Type.CONFIRM);
							}
						} else {
							success = true;
						}
					}
					if ( success ) {
						success = false;
						if ( keyPojo.getEndMillis() > 0 ) {
							if ( keyPojo.getEndMillis() > currentMillis ) {
								success = true;
							} else {
								SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.endtime", Type.CONFIRM);
							}
						} else {
							success = true;
						}
					}
					//Check wether the key is valid
					Long result = jedis.hdel(REDIS_CDKEY, cdkey);
					if ( result != null && result.longValue() == 1 ) {
						Reward reward = keyPojo.getReward();
						if ( reward != null ) {
							ArrayList<Reward> rewards = new ArrayList<Reward>();
							rewards.add(reward);
							RewardManager.getInstance().pickReward(user, rewards, StatAction.CDKeyReward);
							StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "take", cdkey, cdkeyId, reward.toString());
							success = true;
						} else {
							logger.info("Not found the cdkey {} reward for {}", cdkeyId, keyPojo.getReward());
							StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "notfound", cdkey, cdkeyId, reward.toString());
							success = false;
						}
					} else {
						SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.used", Type.CONFIRM);
						StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "taken", cdkey, cdkeyId);
					}
				} else {
					SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.notfoundid", Type.CONFIRM);
					StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "noid", cdkey, cdkeyId);
				}
			} else {
				SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.invalid", Type.CONFIRM);
				StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "invalid", cdkey, cdkeyId);
			}
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(user, "cdkey.empty", Type.CONFIRM);
			StatClient.getIntance().sendDataToStatServer(user, StatAction.CDKey, "empty", cdkey, cdkeyId);
		}
		return success;
	}
	
	/**
	 * Generate the 0-9 numbers CDKEY
	 * @param bit
	 * @return
	 */
	public String generateCDKey(String cdKeyPojoId) {
		Jedis jedis = JedisFactory.getJedisDB();
		String cdkey = String.valueOf((int)(MathUtil.nextDouble()*100000000));
		while ( jedis.hexists(REDIS_CDKEY, cdkey) ) {
			cdkey = String.valueOf((int)(MathUtil.nextDouble()*100000000));
		}
		int diff = 8 - cdkey.length();
		if ( diff>0 ) {
			cdkey = StringUtil.concat(PADDING[diff-1], cdkey);
		}
		jedis.hset(REDIS_CDKEY, cdkey, cdKeyPojoId);
		return cdkey;
	}
	
	/**
	 * Export the cdkey to file
	 * @param fileName
	 * @param pojoId
	 */
	public boolean exportCDKey(String fileName, String pojoId, int count) throws Exception {
		boolean success = false;
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		if ( file.exists() && file.isFile() ) {
			for ( int i=0; i<count; i++ ) {
				String cdkey = generateCDKey(pojoId);
				bw.append(cdkey).append(Constant.COMMA).append(pojoId).append('\n');
				System.out.println("Export cdkey:"+cdkey+", pojoId:"+pojoId);
			}
			bw.close();
			success = true;
		} else {
			logger.warn("CDKey File cannot be write:{}", file.getAbsolutePath());
		}
		return success;
	}
	
	/**
	 * Import the already generated cdkey into system.
	 * 
	 * @param fileName 
	 * @return
	 */
	public int importCDKey(String fileName) throws IOException {
		int count = 0;
		File file = new File(fileName);
		HashMap<String, String> cdkeyMap = new HashMap<String, String>(); 
		if ( file.exists() && file.isFile() ) {
			List<String> lines = IOUtils.readLines(new FileReader(file));
			for ( String line : lines ) {
				String[] cdkeys = line.split(Constant.COMMA);
				if ( cdkeys.length == 2 ) {
					String cdkey = cdkeys[0];
					String pojoId = cdkeys[1];
					cdkeyMap.put(cdkey, pojoId);
					count++;
					logger.info("Import cdKey:"+cdkey+", pojoId:"+pojoId);
				}
			}
			if ( cdkeyMap.size() > 0 ) {
				Jedis jedis = JedisFactory.getJedisDB();
				jedis.hmset(REDIS_CDKEY, cdkeyMap);
			}
		} else {
			logger.warn("CDKey File cannot be read:{}", file.getAbsolutePath());
		}
		return count;
	}
}
