package com.xinqihd.sns.gameserver.session;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class CipherManagerTest extends AbstractTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEncrypted() {
		UserId userId = new UserId("test-001");
		CipherManager manager = CipherManager.getInstance();
		manager.setDefaultTimeout(300000);
		String token = manager.generateEncryptedUserToken(userId);
		UserId actualId = manager.checkEncryptedUserToken(token);
		assertEquals(userId, actualId);
	}
	
	@Test
	public void testEncryptedAccount() {
		String accountId = "test-001";
		CipherManager manager = CipherManager.getInstance();
		manager.setDefaultTimeout(300000);
		String token = manager.generateEncryptedUserToken(accountId);
		Account account = manager.checkEncryptedAccountToken(token);
		//assertEquals(accountId, actualId);
	}
	
	@Test
	public void testInvalid() {
		CipherManager manager = CipherManager.getInstance();
		UserId actualId = manager.checkEncryptedUserToken("mB5Dp3YNHfy4V6WoTt7yBvUXL8ntMRnzx2rgAd");
		assertEquals(null, actualId);
	}
	
	@Test
	public void testTimeout() {
		UserId userId = new UserId("test-001");
		CipherManager manager = CipherManager.getInstance();
		CipherManager.setDefaultTimeout(-100);
		String token = manager.generateEncryptedUserToken(userId);
		UserId actualId = manager.checkEncryptedUserToken(token);
		assertEquals(null, actualId);
	}
	
	public void testStress() throws Exception {
		int max = 100000;
		final UserId userId = new UserId("test-001");
		final CipherManager manager = CipherManager.getInstance();
		final String[] tokens = new String[1];
		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				tokens[0] = manager.generateEncryptedUserToken(userId);
			}
		}, "Encrypt", max, 2);
		TestUtil.doPerformMultiThread(new Runnable() {
			public void run() {
				UserId actualId = manager.checkEncryptedUserToken(tokens[0]);
			}
		}, "Decrypt", max, 2);
	}
}
