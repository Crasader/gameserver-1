package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import script.charge.Apple;

import com.mongodb.DBObject;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.JndiContextKey;
import com.xinqihd.sns.gameserver.config.ChargePojo;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mysql.MysqlUtil;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.proto.XinqiBceChargeInternal.BceChargeInternal;
import com.xinqihd.sns.gameserver.proto.XinqiBseChargeList.ChargeList;
import com.xinqihd.sns.gameserver.session.CipherManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.GameClient;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;

public class ChargeManagerTest {

	@Before
	public void setUp() throws Exception {
		//Initialize mysql connection
		String database = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_database);
		String username = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_username);
		String password = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_password);
		String server = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_billing_server);
		int maxConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_billing_max_conn);
		int minConn = GlobalConfig.getInstance().getIntProperty(GlobalConfigKey.mysql_billing_min_conn);
		String jndi = JndiContextKey.mysql_billing_db.name();
		MysqlUtil.init(database, username, password, server, maxConn, minConn, jndi);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetChargePojoByCurrency() {
		ChargeManager manager = ChargeManager.getInstance();
		Collection<ChargePojo> coll = manager.getChargePojoByCurrency("ios_iap");;
		float lastPrice = 0;
		for ( ChargePojo pojo : coll ) {
			System.out.println(pojo);
			assertTrue ( pojo.getPrice() > lastPrice);
			lastPrice = pojo.getPrice();
		}
		coll = manager.getChargePojoByCurrency("cmcc_sms");
		assertEquals(1, coll.size());
		lastPrice = 0;
		for ( ChargePojo pojo : coll ) {
			System.out.println(pojo);
			assertTrue ( pojo.getPrice() > lastPrice);
			lastPrice = pojo.getPrice();
		}
	}
	
	@Test
	public void testGetCharePojos() {
		ChargeManager manager = ChargeManager.getInstance();
		for ( ChargePojo pojo : manager.getChargePojos() ) {
			System.out.println(pojo);
		}
	}

	@Test
	public void testGetCharePojoById() {
		ChargeManager manager = ChargeManager.getInstance();
		ChargePojo pojo = manager.getCharePojoById(1);
		System.out.println(pojo);
		assertEquals(6, pojo.getPrice(), 0.0001);
	}
	
	@Test
	public void testUserChargeByRMB() throws Exception {
		ChargeManager manager = ChargeManager.getInstance();
		int chargeId = 8;
		ChargePojo pojo = manager.getCharePojoById(chargeId);
		System.out.println(pojo);
		assertEquals(600, pojo.getPrice(), 0.0001);
		
		//Clean database
		cleanBillingTable();
		
		User user = prepareUser();
		
		manager.doCharge(null, user, null, pojo, 0, Constant.EMPTY, true);
		assertEquals(15100, user.getYuanbao());
		Map rowSet = MysqlUtil.executeQueryFirstRow(
				"select * from billing where username = 'test-001'"
				, JndiContextKey.mysql_billing_db.name());
		assertEquals(true, rowSet.size()>0);
		assertEquals("test-001", rowSet.get("username"));
		assertEquals("15100", rowSet.get("total_yuanbao"));
	}
	
	@Test
	public void testUserChargeVip() throws Exception {
		ChargeManager manager = ChargeManager.getInstance();
		int chargeId = 8;
		ChargePojo pojo = manager.getCharePojoById(chargeId);
		System.out.println(pojo);
		assertEquals(600, pojo.getPrice(), 0.0001);
		
		//Clean database
		UserManager.getInstance().removeUser("test-001");
		cleanBillingTable();
		
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setClient("iphone4s");
		user.setYuanbao(100);
		user.setIsvip(true);
		UserManager.getInstance().saveUser(user, true);
		
		manager.doCharge(null, user, null, pojo, 0, Constant.EMPTY, true);
		assertEquals(15100, user.getYuanbao());
		Map rowSet = MysqlUtil.executeQueryFirstRow(
				"select * from billing where username = 'test-001'", JndiContextKey.mysql_billing_db.name());
		assertEquals(true, rowSet.size()>0);
		assertEquals("test-001", rowSet.get("username"));
		assertEquals("15100", rowSet.get("total_yuanbao"));
	}

	@Test
	public void testToBseChargeList() {
		ChargeManager manager = ChargeManager.getInstance();
		ChargeList list = manager.toBseChargeList("kupai");
		System.out.println(list);
	}
	
	@Test
	public void testGenerateTransactionId() {
		ChargeManager manager = ChargeManager.getInstance();
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setClient("iphone4s");
		user.setYuanbao(100);
		user.setIsvip(true);

		String transId = manager.generateTranscationID("s0001", user);
		System.out.println(transId);
	}
	
	@Test
	public void testVerifyIAP() {
		String receiptData = "ewoJInNpZ25hdHVyZSIgPSAiQXFUUjdBMWpGWDhmL3JVUlZLUjhVeW9SR2ZwMUhHajl0M3czRkJCK0gwYU0yMGJuZmZuNjhMMGdvcVh3dFQ0bjJuRU9lS05NVThKNW44YkNXbHgzYmJ2Q0NQZlgxaGRqb1dCbWcxTmtwR2VqdnhQUEZJeGI5WEhtZlJpanFPZDlyL01GU2tMUEZGemwrQ0hHcjNaeDRsWnN5ck0wM0RrdEFUeDF6a0Q2RXE5ekFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV6TFRBeExUSTBJREU1T2pJMk9qUXpJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluVnVhWEYxWlMxcFpHVnVkR2xtYVdWeUlpQTlJQ0kxWVRjNFptRXdaV0ZrTVRWaE56QTFaR1F3TXpSaVlUZGxNelU0WlRnd1lXTTJaVFZqWmpOa0lqc0tDU0p2Y21sbmFXNWhiQzEwY21GdWMyRmpkR2x2YmkxcFpDSWdQU0FpTVRBd01EQXdNREEyTWpneU9UTTJOQ0k3Q2draVluWnljeUlnUFNBaU1TNDRMakVpT3dvSkluUnlZVzV6WVdOMGFXOXVMV2xrSWlBOUlDSXhNREF3TURBd01EWXlPREk1TXpZMElqc0tDU0p4ZFdGdWRHbDBlU0lnUFNBaU1TSTdDZ2tpYjNKcFoybHVZV3d0Y0hWeVkyaGhjMlV0WkdGMFpTMXRjeUlnUFNBaU1UTTFPVEE0TkRRd016YzRNQ0k3Q2draWRXNXBjWFZsTFhabGJtUnZjaTFwWkdWdWRHbG1hV1Z5SWlBOUlDSXpOakJEUlVaRE5TMUJRVEZCTFRSRk5VVXRRVVkzUVMxRE5VUkNNemt5UTBVMU5USWlPd29KSW5CeWIyUjFZM1F0YVdRaUlEMGdJbU52YlM1aVlXSjVkMkZ5TG5ocGJuRnBhR1F1TmpBaU93b0pJbWwwWlcwdGFXUWlJRDBnSWpVM09USXlORFF3TVNJN0Nna2lZbWxrSWlBOUlDSmpiMjB1WTJoaGJtZDViM1V1YUdzdVltRmllWGRoY2lJN0Nna2ljSFZ5WTJoaGMyVXRaR0YwWlMxdGN5SWdQU0FpTVRNMU9UQTRORFF3TXpjNE1DSTdDZ2tpY0hWeVkyaGhjMlV0WkdGMFpTSWdQU0FpTWpBeE15MHdNUzB5TlNBd016b3lOam8wTXlCRmRHTXZSMDFVSWpzS0NTSndkWEpqYUdGelpTMWtZWFJsTFhCemRDSWdQU0FpTWpBeE15MHdNUzB5TkNBeE9Ub3lOam8wTXlCQmJXVnlhV05oTDB4dmMxOUJibWRsYkdWeklqc0tDU0p2Y21sbmFXNWhiQzF3ZFhKamFHRnpaUzFrWVhSbElpQTlJQ0l5TURFekxUQXhMVEkxSURBek9qSTJPalF6SUVWMFl5OUhUVlFpT3dwOSI7CgkiZW52aXJvbm1lbnQiID0gIlNhbmRib3giOwoJInBvZCIgPSAiMTAwIjsKCSJzaWduaW5nLXN0YXR1cyIgPSAiMCI7Cn0=";
		String receipt = Apple.verifyIAP(receiptData);
		System.out.println(receipt);
	}

	@Test
	public void testFreeChargeNoToken() {
		User user = prepareUser();
		ChargeManager manager = ChargeManager.getInstance();
		boolean success = manager.freeCharge(user, 100, null);
		assertTrue(!success);
	}
	
	@Test
	public void testFreeChargeValidToken() {
		User user = prepareUser();
		ChargeManager manager = ChargeManager.getInstance();
		String token = CipherManager.getInstance().generateEncryptedUserToken(user.get_id());
		boolean success = manager.freeCharge(user, 100, token);
		assertTrue(success);
		assertEquals(1100, user.getYuanbao());
	}
	
	@Test
	public void testFreeChargeToRealUser() {
		List<SessionKey> list = GameContext.getInstance().findAllOnlineUsers();
		String roleName = "731";
		User user = MongoUserManager.getInstance().queryUserByRoleName(roleName);
		int moneyCount = 1000;
		/*
		for ( SessionKey sessionKey : list ) {
			User u = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
			if ( u!=null && roleName.equals(u.getRoleName()) ) {
				user = u;
				break;
			}
		}
		*/
		if ( user != null ) {
			String token = CipherManager.getInstance().generateEncryptedUserToken(user.get_id());
			ChargeManager.getInstance().freeCharge(user, moneyCount, token);
		} else {
			System.out.println("User " + roleName + " is not online " );
		}
	}
	
	@Test
	public void testFreeChargeToRealUserAtRemoteServer() throws Exception {
		BceChargeInternal.Builder charge = BceChargeInternal.newBuilder();
		charge.setChargeid(0);
		//xq001
		charge.setUserid("1349971200:小陶陶");
		charge.setFreecharge(true);
		charge.setChargemoney(500);
		XinqiMessage msg = new XinqiMessage();
		msg.payload=charge.build();
		
		GameClient client = new GameClient("g1.babywar.xinqihd.com", 3443);
		//GameClient client = new GameClient("192.168.0.77", 3443);
		client.sendMessageToServer(msg);

		//Thread.sleep(10000);
	}
	
	@Test
	public void testGenerateTransactionId2() throws Exception {
		User user = new User();
		user.setRoleName("test-001");
		for ( int i=0; i<1000; i++) {
			String id = ChargeManager.getInstance().generateTranscationID("s0001", user);
			System.out.println(id);
		}
	}
	
	/**
	 * 我们发现玩家通过使用旧的伪造的TOKEN值对游戏进行充值，需要屏蔽这种行为
	 * @throws Exception
	 */
	@Test
	public void testFakeJSON() throws Exception {
		/**
		 * {
	"original-purchase-date-pst" = "2012-07-12 05:54:35 America/Los_Angeles";
	"purchase-date-ms" = "1342097675882";
	"original-transaction-id" = "170000029449420";
	"bvrs" = "1.4";
	"app-item-id" = "450542233";
	"transaction-id" = "170000029449420";
	"quantity" = "1";
	"original-purchase-date-ms" = "1342097675882";
	"item-id" = "534185042";
	"version-external-identifier" = "9051236";
	"product-id" = "com.zeptolab.ctrbonus.superpower1";
	"purchase-date" = "2012-07-12 12:54:35 Etc/GMT";
	"original-purchase-date" = "2012-07-12 12:54:35 Etc/GMT";
	"bid" = "com.zeptolab.ctrexperiments";
	"purchase-date-pst" = "2012-07-12 05:54:35 America/Los_Angeles";
		}
		 */
		String json = "{ 'receipt-data' : 'ewoJInNpZ25hdHVyZSIgPSAiQXBkeEpkdE53UFUyckE1L2NuM2tJTzFPVGsyNWZlREthMGFhZ3l5UnZlV2xjRmxnbHY2UkY2em5raUJTM3VtOVVjN3BWb2IrUHFaUjJUOHd5VnJITnBsb2YzRFgzSXFET2xXcSs5MGE3WWwrcXJSN0E3ald3dml3NzA4UFMrNjdQeUhSbmhPL0c3YlZxZ1JwRXI2RXVGeWJpVTFGWEFpWEpjNmxzMVlBc3NReEFBQURWekNDQTFNd2dnSTdvQU1DQVFJQ0NHVVVrVTNaV0FTMU1BMEdDU3FHU0liM0RRRUJCUVVBTUg4eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUtEQXBCY0hCc1pTQkpibU11TVNZd0pBWURWUVFMREIxQmNIQnNaU0JEWlhKMGFXWnBZMkYwYVc5dUlFRjFkR2h2Y21sMGVURXpNREVHQTFVRUF3d3FRWEJ3YkdVZ2FWUjFibVZ6SUZOMGIzSmxJRU5sY25ScFptbGpZWFJwYjI0Z1FYVjBhRzl5YVhSNU1CNFhEVEE1TURZeE5USXlNRFUxTmxvWERURTBNRFl4TkRJeU1EVTFObG93WkRFak1DRUdBMVVFQXd3YVVIVnlZMmhoYzJWU1pXTmxhWEIwUTJWeWRHbG1hV05oZEdVeEd6QVpCZ05WQkFzTUVrRndjR3hsSUdsVWRXNWxjeUJUZEc5eVpURVRNQkVHQTFVRUNnd0tRWEJ3YkdVZ1NXNWpMakVMTUFrR0ExVUVCaE1DVlZNd2daOHdEUVlKS29aSWh2Y05BUUVCQlFBRGdZMEFNSUdKQW9HQkFNclJqRjJjdDRJclNkaVRDaGFJMGc4cHd2L2NtSHM4cC9Sd1YvcnQvOTFYS1ZoTmw0WElCaW1LalFRTmZnSHNEczZ5anUrK0RyS0pFN3VLc3BoTWRkS1lmRkU1ckdYc0FkQkVqQndSSXhleFRldngzSExFRkdBdDFtb0t4NTA5ZGh4dGlJZERnSnYyWWFWczQ5QjB1SnZOZHk2U01xTk5MSHNETHpEUzlvWkhBZ01CQUFHamNqQndNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVVOaDNvNHAyQzBnRVl0VEpyRHRkREM1RllRem93RGdZRFZSMFBBUUgvQkFRREFnZUFNQjBHQTFVZERnUVdCQlNwZzRQeUdVakZQaEpYQ0JUTXphTittVjhrOVRBUUJnb3Foa2lHOTJOa0JnVUJCQUlGQURBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQUVhU2JQanRtTjRDL0lCM1FFcEszMlJ4YWNDRFhkVlhBZVZSZVM1RmFaeGMrdDg4cFFQOTNCaUF4dmRXLzNlVFNNR1k1RmJlQVlMM2V0cVA1Z204d3JGb2pYMGlreVZSU3RRKy9BUTBLRWp0cUIwN2tMczlRVWU4Y3pSOFVHZmRNMUV1bVYvVWd2RGQ0TndOWXhMUU1nNFdUUWZna1FRVnk4R1had1ZIZ2JFL1VDNlk3MDUzcEdYQms1MU5QTTN3b3hoZDNnU1JMdlhqK2xvSHNTdGNURXFlOXBCRHBtRzUrc2s0dHcrR0szR01lRU41LytlMVFUOW5wL0tsMW5qK2FCdzdDMHhzeTBiRm5hQWQxY1NTNnhkb3J5L0NVdk02Z3RLc21uT09kcVRlc2JwMGJzOHNuNldxczBDOWRnY3hSSHVPTVoydG04bnBMVW03YXJnT1N6UT09IjsKCSJwdXJjaGFzZS1pbmZvIiA9ICJld29KSW05eWFXZHBibUZzTFhCMWNtTm9ZWE5sTFdSaGRHVXRjSE4wSWlBOUlDSXlNREV5TFRBM0xURXlJREExT2pVME9qTTFJRUZ0WlhKcFkyRXZURzl6WDBGdVoyVnNaWE1pT3dvSkluQjFjbU5vWVhObExXUmhkR1V0YlhNaUlEMGdJakV6TkRJd09UYzJOelU0T0RJaU93b0pJbTl5YVdkcGJtRnNMWFJ5WVc1ellXTjBhVzl1TFdsa0lpQTlJQ0l4TnpBd01EQXdNamswTkRrME1qQWlPd29KSW1KMmNuTWlJRDBnSWpFdU5DSTdDZ2tpWVhCd0xXbDBaVzB0YVdRaUlEMGdJalExTURVME1qSXpNeUk3Q2draWRISmhibk5oWTNScGIyNHRhV1FpSUQwZ0lqRTNNREF3TURBeU9UUTBPVFF5TUNJN0Nna2ljWFZoYm5ScGRIa2lJRDBnSWpFaU93b0pJbTl5YVdkcGJtRnNMWEIxY21Ob1lYTmxMV1JoZEdVdGJYTWlJRDBnSWpFek5ESXdPVGMyTnpVNE9ESWlPd29KSW1sMFpXMHRhV1FpSUQwZ0lqVXpOREU0TlRBME1pSTdDZ2tpZG1WeWMybHZiaTFsZUhSbGNtNWhiQzFwWkdWdWRHbG1hV1Z5SWlBOUlDSTVNRFV4TWpNMklqc0tDU0p3Y205a2RXTjBMV2xrSWlBOUlDSmpiMjB1ZW1Wd2RHOXNZV0l1WTNSeVltOXVkWE11YzNWd1pYSndiM2RsY2pFaU93b0pJbkIxY21Ob1lYTmxMV1JoZEdVaUlEMGdJakl3TVRJdE1EY3RNVElnTVRJNk5UUTZNelVnUlhSakwwZE5WQ0k3Q2draWIzSnBaMmx1WVd3dGNIVnlZMmhoYzJVdFpHRjBaU0lnUFNBaU1qQXhNaTB3TnkweE1pQXhNam8xTkRvek5TQkZkR012UjAxVUlqc0tDU0ppYVdRaUlEMGdJbU52YlM1NlpYQjBiMnhoWWk1amRISmxlSEJsY21sdFpXNTBjeUk3Q2draWNIVnlZMmhoYzJVdFpHRjBaUzF3YzNRaUlEMGdJakl3TVRJdE1EY3RNVElnTURVNk5UUTZNelVnUVcxbGNtbGpZUzlNYjNOZlFXNW5aV3hsY3lJN0NuMD0iOwoJInBvZCIgPSAiMTciOwoJInNpZ25pbmctc3RhdHVzIiA9ICIwIjsKfQ=='}";
		User user = new User();
		user.setRoleName("test-001");
		ChargePojo chargePojo = ChargeManager.getInstance().getCharePojoById(1);
		boolean success = Apple.checkIAPReceipt(user, chargePojo.getBillingIdentifier(), json);
		assertFalse(success);
	}

	/**
	 * Add the 'channel' field to each charge data.
	 */
	public void addChannelToChargeData() {
		Collection<ChargePojo> charges = ChargeManager.getInstance().getChargePojos();
		for ( ChargePojo charge : charges ) {
			charge.setChannel("ios_iap");
			charge.setBillingIdentifier("com.babywar.xinqihd."+charge.getYuanbao());
		}
		
		String database = "babywar";
		String namespace = "server0001";
		String collection = "charges";
		for ( ChargePojo charge : charges ) {
			MapDBObject objToSave = new MapDBObject();
			objToSave.putAll(charge);
			DBObject query = MongoDBUtil.createDBObject("_id", charge.getId());
			MongoDBUtil.saveToMongo(query, objToSave, database, namespace, collection, true);
		}
	}
	
	private void cleanBillingTable() {
		MysqlUtil.executeUpdate("delete from billing", JndiContextKey.mysql_billing_db.name());
	}
	
	/**
	 * @return
	 */
	private User prepareUser() {
		User user = new User();
		user.set_id(new UserId("test-001"));
		user.setUsername("test-001");
		user.setRoleName("test-001");
		user.setClient("iphone4s");
		user.setYuanbao(100);
		UserManager.getInstance().removeUser("test-001");
		UserManager.getInstance().saveUser(user, true);
		return user;
	}
}
