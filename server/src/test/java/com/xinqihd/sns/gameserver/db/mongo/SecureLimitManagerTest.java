package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.mongo.SecureLimitManager.LimitType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class SecureLimitManagerTest {

	String roleName = "test-001";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExp() {
		LimitType type = LimitType.EXP;
		
		String keyName = SecureLimitManager.getSecureLimitName(roleName);
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(keyName);

		int expLimit = SecureLimitManager.getSecureLimit(type);
		User user = prepareUser();
		user.setExp(200);
		assertEquals(200, user.getExp());
		Long ttl = jedis.ttl(keyName);
		assertTrue(ttl.intValue()>0);
		
		String value = jedis.hget(keyName, type.toString());
		assertEquals("200", value);
		
		user.setExp(200+expLimit);
		assertEquals(200, user.getExp());
		value = jedis.hget(keyName, type.toString());
		assertEquals(""+(expLimit+200), value);
		
		//user can no longer add new exp
		user.setExp(210);
		assertEquals(200, user.getExp());
	}
	
	@Test
	public void testGolden() {
		LimitType type = LimitType.GOLDEN;
		
		String keyName = SecureLimitManager.getSecureLimitName(roleName);
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(keyName);

		int expLimit = SecureLimitManager.getSecureLimit(type);
		User user = prepareUser();
		user.setGolden(200);
		assertEquals(200, user.getGolden());
		Long ttl = jedis.ttl(keyName);
		assertTrue(ttl.intValue()>0);
		
		String value = jedis.hget(keyName, type.toString());
		assertEquals("200", value);
		
		user.setGolden(200+expLimit);
		assertEquals(200, user.getGolden());
		value = jedis.hget(keyName, type.toString());
		assertEquals(""+(expLimit+200), value);
		
		//user can no longer add new exp
		user.setGolden(210);
		assertEquals(200, user.getGolden());
	}
	
	@Test
	public void testYuanbao() {
		LimitType type = LimitType.YUANBAO;
		
		String keyName = SecureLimitManager.getSecureLimitName(roleName);
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(keyName);

		int expLimit = SecureLimitManager.getSecureLimit(type);
		User user = prepareUser();
		user.setYuanbao(200);
		assertEquals(200, user.getYuanbao());
		Long ttl = jedis.ttl(keyName);
		assertTrue(ttl.intValue()>0);
		
		String value = jedis.hget(keyName, type.toString());
		assertEquals("200", value);
		
		user.setYuanbao(200+expLimit);
		assertEquals(200, user.getYuanbao());
		value = jedis.hget(keyName, type.toString());
		assertEquals(""+(expLimit+200), value);
		
		//user can no longer add new exp
		user.setYuanbao(210);
		assertEquals(200, user.getYuanbao());
	}

	/**
	 * @return
	 */
	public User prepareUser() {
		User user = new User();
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		return user;
	}

}
