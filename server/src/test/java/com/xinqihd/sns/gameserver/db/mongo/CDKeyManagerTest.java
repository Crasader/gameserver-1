package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.CDKeyPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class CDKeyManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTakeCDKey() throws Exception {
		CDKeyManager manager = CDKeyManager.getInstance();
		User user = prepareUser("test001");
		user.setChannel("_xiaomi_");
		String cdId = "1";
		String cdkey = manager.generateCDKey(cdId);
		System.out.println(cdkey);
		Jedis jedis = JedisFactory.getJedisDB();
		jedis.hset(CDKeyManager.REDIS_CDKEY, cdkey, "1");
		
		boolean success = manager.takeCDKeyReward(user, cdkey);
		assertTrue("Should take the valid cdkey", success);
		
		//take again
		success = manager.takeCDKeyReward(user, cdkey);
		assertTrue("Should not take the dup cdkey", !success);
	}
	
	@Test
	public void testGenerateCDKey() throws Exception {
		CDKeyManager manager = CDKeyManager.getInstance();
		int count = 100;
		String cdId = "1";
		ArrayList<String> cdkeys = new ArrayList<String>(count);
		for ( int i=0; i<count; i++ ) {
			String cdkey = manager.generateCDKey(cdId);
			cdkeys.add(cdkey);
		}
		for ( String cdkey : cdkeys ) {
			System.out.println(cdkey);
		}
	}
	
	@Test
	public void testExportImportCDKey() throws Exception {
		CDKeyManager manager = CDKeyManager.getInstance();
		int count = 500;
		String fileName = "cdkey_cyou_500";
		manager.exportCDKey(fileName+".txt", "4", count);
		manager.importCDKey(fileName+".txt");
	}

	public void prepareCDKey(String cdkey) {
		CDKeyPojo cdkeyPojo = new CDKeyPojo();
		cdkeyPojo.setId("1");
		Reward reward = RewardManager.getRewardGolden(1000);
		cdkeyPojo.setReward(reward);
		CDKeyManager.getInstance().addCDKey(cdkeyPojo);
	}
	
	private User prepareUser(String userName) throws Exception {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		String password = StringUtil.encryptSHA1(userName);
		user.setPassword(password);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());

		userManager.removeUser(userName);
		userManager.removeUserByRoleName(userName);
		
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
