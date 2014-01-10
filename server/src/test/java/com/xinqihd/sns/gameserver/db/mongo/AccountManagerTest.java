package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.ServerPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.ServerRoleList;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.util.StringUtil;

public class AccountManagerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void addAdminUser() throws Exception {
		User user = UserManager.getInstance().queryUserByRoleName("心海洋");
		user.setAdmin(true);
		UserManager.getInstance().saveUser(user, true);
	}

	@Test
	public void testConvertUserToAccount() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setUsername("1234567890");
		user.setRoleName("test-001");
		Account account = AccountManager.getInstance().convertUserToAccount(user);
		account.set_id("accountid");
		System.out.println(account);
		assertEquals("s0001.test-001", user.getRoleName());
		//Remove account
		AccountManager.getInstance().removeAccount(account);
		//Save account
		AccountManager.getInstance().saveAccount(account);
		//Query account
		Account actual = AccountManager.getInstance().queryAccountById(account.get_id());
		assertNotNull(actual);
		assertEquals(account.toString(), actual.toString());
	}

	@Test
	public void testSaveAccount() {
		Account account = new Account();
		String accountName = "accountname";
		ServerPojo server = ServerListManager.getInstance().getRecommendServer("", null);
		String accountId = StringUtil.concat(server.getId(), Constant.DOT, accountName);
		String roleName = StringUtil.concat(server.getId(), Constant.DOT, LoginManager.getRandomRoleName());
		account.set_id(accountId);
		account.setUserName(accountName);
		account.setChannel("test-channel");
		account.setRegMillis(System.currentTimeMillis());
		account.setNewAccount(true);
		ServerRoleList serverRole = new ServerRoleList();
		serverRole.setServerId(server.getId());
		serverRole.addRoleName(roleName);
		account.addServerRole(serverRole);
	
		AccountManager.getInstance().saveAccount(account);
	}
	
	@Test
	public void testUpdateAccount() {
		Account account = new Account();
		String accountName = "accountname";
		ServerPojo server = ServerListManager.getInstance().getRecommendServer("", null);
		String accountId = StringUtil.concat(server.getId(), Constant.DOT, accountName);
		String roleName = StringUtil.concat(server.getId(), Constant.DOT, LoginManager.getRandomRoleName());
		account.set_id(accountId);
		account.setUserName(accountName);
		account.setChannel("test-channel");
		account.setRegMillis(System.currentTimeMillis());
		account.setNewAccount(true);

		AccountManager.getInstance().removeAccount(account);
		AccountManager.getInstance().saveAccount(account);
		
		//Try to change some fields
		account.setChannel("channel-test");
		AccountManager.getInstance().updateAccount(account);
		Account actual = AccountManager.getInstance().queryAccountById(accountId);
		assertEquals("channel-test", actual.getChannel());
		
		//Try to add a new server
		ServerRoleList serverRole = new ServerRoleList();
		serverRole.setServerId("s0002");
		serverRole.addRoleName(roleName);
		account.addServerRole(serverRole);
		AccountManager.getInstance().updateAccount(account);
		actual = AccountManager.getInstance().queryAccountById(accountId);
		assertEquals(1, actual.getServerRoles().size());
	}
}
