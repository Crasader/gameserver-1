package com.xinqihd.sns.gameserver.db.mongo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.MailMessageManager.MailBoxType;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailList.BseMailList;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailReceive.BseMailReceive;
import com.xinqihd.sns.gameserver.proto.XinqiGift.Gift;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.util.JedisUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class MailMessageManagerTest {
	
	String list = "mailtest";

	@Before
	public void setUp() throws Exception {
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.del(list);
		JedisUtil.deleteAllKeys();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAcquireMailCount() throws Exception {
		int maxListCount = 50;
		int expireSeconds = 1000;
		String list = "mailtest";
		String[] content = {
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"
		};
		MailMessageManager mail = MailMessageManager.getInstance();
		//Should not throw any errors
		int count = mail.acquireMailCount(list);
		assertEquals(0, count);
		
		for ( int i=0; i<content.length; i++ ) {
			mail.pushValueInList(list, content[i], maxListCount, expireSeconds);
		}
		
		assertEquals(content.length, mail.acquireMailCount(list));
		Jedis jedisDB = JedisFactory.getJedisDB();
		int ttl = jedisDB.ttl(list).intValue();
		Thread.sleep(1000);
		int newttl = jedisDB.ttl(list).intValue();
		assertTrue("expire seconds:", newttl < ttl );
	}
	
	@Test
	public void testPushAndPopValue() throws Exception {
		int maxListCount = 5;
		int expireSeconds = 1000;
		String list = "mailtest";
		String[] content = {
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"
		};
		Jedis jedisDB = JedisFactory.getJedisDB();
		
		MailMessageManager mail = MailMessageManager.getInstance();
		for ( int i=0; i<content.length; i++ ) {
			mail.pushValueInList(list, content[i], maxListCount, expireSeconds);
		}
		Thread.sleep(1000);
		
		int ttl = jedisDB.ttl(list).intValue();
		assertEquals(maxListCount, mail.acquireMailCount(list));
		
		for ( int i=0; i<maxListCount-1; i++ ) {
			String c = mail.popValueFromList(list, expireSeconds);
			System.out.print(c+",");
			assertEquals(content[content.length-1-i], c);
		}
		
		int newttl = jedisDB.ttl(list).intValue();
		assertTrue(newttl+">"+ttl, newttl > ttl);
	}
	
	@Test
	public void testPopFromEmptyMailbox() {
		MailMessageManager mail = MailMessageManager.getInstance();
		int count = mail.acquireMailCount("emtpymailbox");
		assertEquals(0, count);
		String content = mail.popValueFromList("emtpymailbox", 1000);
		//assert no errors
		assertNull(content);
	}

	@Test
	public void testSendMailToOnlineFromAdmin() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), "subject", "这是一封测试邮件", null, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		String message = mail.popValueFromList(mailbox, 1000);
		assertNotNull("online user should receive mail like offline users", message);

		Thread.sleep(200);
		
		BseMailReceive bse = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseMailReceive ) {
				bse = (BseMailReceive)xinqi.payload;
				System.out.println(bse);
				break;
			}
		}
		assertNotNull(bse);
	}
	
	@Test
	public void testSendMailToRealUser() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		User realUser = UserManager.getInstance().queryUserByRoleName("10000");
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, realUser.get_id(), "一起来玩游戏吧", "这是一封测试邮件", null, true);
	}
	
	@Test
	public void testSendMailToRealUserWithGift() throws Exception {
		String userName = "10000";
		User user = UserManager.getInstance().queryUserByRoleName(userName);

		Collection<Reward> rewards = prepareRewards();
		
		int bagCount = user.getBag().getCurrentCount();

		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这封邮件带有附件礼物", rewards, true);
	}
	
	@Test
	public void testSendALotOfMails() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);
		int expireSeconds = 1000;
		
		MailMessageManager mail = MailMessageManager.getInstance();
		for ( int i=0; i<1000; i++) {
			mail.sendMail(null, user.get_id(), "subject", "这是一封测试邮件:"+i, null, true);
		}
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		int mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(150, mailboxCount);
		
		for ( int i=0; i<mailboxCount; i++ ) {
			String message = mail.popValueFromList(mailbox, expireSeconds);
			System.out.println(message);
			assertNotNull("mail:"+message, message);
		}
	}
	
	@Test
	public void testCleanAllMail() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);
		int expireSeconds = 1000;
		
		MailMessageManager mail = MailMessageManager.getInstance();
		for ( int i=0; i<1000; i++) {
			mail.sendMail(null, user.get_id(), "subject", ""+i, null, true);
		}
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		int mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(150, mailboxCount);
		
		mail.deleteAllInList(mailbox);
		mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(0, mailboxCount);
		
		//should can send a new mail.
		mail.sendMail(null, user.get_id(), "subject", "0", null, true);
	}
	
	@Test
	public void testDeleteGivenMail() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);
		int expireSeconds = 1000;
		Jedis jedisDB = JedisFactory.getJedisDB();
		MailMessageManager mail = MailMessageManager.getInstance();
		String mailbox = mail.getMailboxName(userName);
		
		for ( int i=0; i<10; i++) {
			mail.sendMail(null, user.get_id(), "s"+i, ""+i, null, true);
		}
		List<String> mailList = jedisDB.lrange(mailbox, 0, -1);
		System.out.println(mailList);
		/**
		 * 用户看到的邮件列表如下：
		 * [系统`2012-09-02`s9`9`1346553738681, 
		 *  系统`2012-09-02`s8`8`1346553738680, 
		 *  系统`2012-09-02`s7`7`1346553738679, 
		 *  系统`2012-09-02`s6`6`1346553738678, 
		 *  系统`2012-09-02`s5`5`1346553738676, 
		 *  系统`2012-09-02`s4`4`1346553738675, 
		 *  系统`2012-09-02`s3`3`1346553738674, 
		 *  系统`2012-09-02`s2`2`1346553738673, 
		 *  系统`2012-09-02`s1`1`1346553738672, 
		 *  系统`2012-09-02`s0`0`1346553738670]
		 *  
		 * 假设需要删除内容为9,7,5,3,1的邮件，索引如下
		 */
		int[] deletedIndex = {0, 2, 4, 6, 8};
		
		int mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(10, mailboxCount);
		
		int deleteSize = mail.deleteMail(user, deletedIndex, MailBoxType.inbox);
		assertEquals(deletedIndex.length, deleteSize);
		mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(5, mailboxCount);
		mailList = jedisDB.lrange(mailbox, 0, -1);
		int i=8;
		System.out.println(mailList);
		for ( String m : mailList ) {
			assertEquals(""+i, m.substring(17, 18));
			i-=2;
		}
	}
	
	@Test
	public void testDeleteNotExistMail() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);
		int expireSeconds = 1000;
		Jedis jedisDB = JedisFactory.getJedisDB();
		MailMessageManager mail = MailMessageManager.getInstance();
		String mailbox = mail.getMailboxName(userName);
		
		int[] deletedIndex = {0, 2, 4, 6, 8};
		
		int mailboxCount = mail.acquireMailCount(mailbox);
		assertEquals(0, mailboxCount);
		
		int deleteSize = mail.deleteMail(user, deletedIndex, MailBoxType.inbox);
		assertEquals(0, deleteSize);
	}
	
	@Test
	public void testSendMailToOnlineFromUser() throws Exception {
		String userName1 = "test-001";
		String userName2 = "test-002";
		User user1 = prepareUser(userName1);
		User user2 = prepareUser(userName2);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user2.setSession(session);
		GameContext.getInstance().registerUserSession(session, user2, null);
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(user1.get_id(), user2.get_id(), null, "这是一封测试邮件", null, false);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName2);
		
		String message = mail.popValueFromList(mailbox, 1000);
		assertNotNull("online user should receive mail like offline users", message);
		
		Thread.sleep(200);
		
		BseMailReceive bse = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseMailReceive ) {
				bse = (BseMailReceive)xinqi.payload;
				break;
			}
		}
		assertNotNull(bse);
	}
	
	@Test
	public void testSendMailToOnlineFromAdminWithGift() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);

		Collection<Reward> rewards = prepareRewards();
		
		int bagCount = user.getBag().getCurrentCount();
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这是一封测试邮件", rewards, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		String message = mail.popValueFromList(mailbox, 1000);
		assertNotNull("online user should receive mail like offline users", message);
		
		BseMailReceive bse = null;
		for ( XinqiMessage xinqi : list ) {
			if ( xinqi.payload instanceof BseMailReceive ) {
				bse = (BseMailReceive)xinqi.payload;
				break;
			}
		}
		assertNotNull(bse);
		
		Gift gift = bse.getMails(0).getGifts(0);
		Reward reward = Reward.fromGift(gift);
		rewards = new ArrayList<Reward>();
		rewards.add(reward);
		RewardManager.getInstance().pickRewardWithResult(user, rewards, 
				StatAction.BattleReward);
		//The gift is in bag
		assertEquals(bagCount+1, user.getBag().getCurrentCount());
	}
	
	@Test
	public void testSendMailToOfflineUserFromAdmin() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这是一封测试邮件", null, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		String message = mail.popValueFromList(mailbox, 1000);
		assertNotNull("mail should not be null", message);
		System.out.println(message);
	}
	
	@Test
	public void testSendMailToOfflineFromUser() throws Exception {
		String userName1 = "test-001";
		String userName2 = "test-002";
		User user1 = prepareUser(userName1);
		User user2 = prepareUser(userName2);
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(user1.get_id(), user2.get_id(), null, "这是一封测试邮件", null, false);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName2);
		
		String message = mail.popValueFromList(mailbox, 1000);
		assertNotNull("mail should not be null", message);
		System.out.println(message);
	}
	
	@Test
	public void testSendMailToOfflineFromAdminWithGift() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);

		Collection<Reward> rewards = prepareRewards();
		
		int bagCount = user.getBag().getCurrentCount();

		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这封邮件带有附件礼物", rewards, true);

		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		String message = mail.popValueFromList(mailbox, 1000);
		MailData mailData = mail.parseMailString(message);
		assertNotNull("mail should not be null", mailData);
		assertEquals(1, mailData.getGiftsCount());
	}
	
	@Test
	public void testTakeMailGift() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);

		Collection<Reward> rewards = prepareRewards();
		
		int bagCount = user.getBag().getCurrentCount();
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这是一封测试邮件", rewards, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		//First take gift
		mail.takeMailGift(user, 0);
		assertEquals(bagCount+1, user.getBag().getCurrentCount());
		
		//Try to take it again
		mail.takeMailGift(user, 0);
		assertEquals(bagCount+1, user.getBag().getCurrentCount());
	}
	
	@Test
	public void testTakeMailGiftWithWeapon() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setSessionKey(null);

		Collection<Reward> rewards = prepareColorRewards();
		
		int bagCount = user.getBag().getCurrentCount();
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这是一封测试邮件", rewards, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		//First take gift
		mail.takeMailGift(user, 0);
		assertEquals(bagCount+1, user.getBag().getCurrentCount());
		PropData propData = user.getBag().getOtherPropData(21);
		System.out.println(propData);
		
		//Try to take it again
		mail.takeMailGift(user, 0);
		assertEquals(bagCount+1, user.getBag().getCurrentCount());
	}
	
	@Test
	public void testCheckMailFromAdmin() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		
		MailMessageManager mail = MailMessageManager.getInstance();
		mail.sendMail(null, user.get_id(), null, "这是一封测试邮件", null, true);
		
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = mail.getMailboxName(userName);
		
		//Now user login
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		GameContext.getInstance().registerUserSession(session, user, null);
		
		mail.checkMail(user);
		
		Thread.sleep(200);
		
		BseMailList bse = null;
		int size = list.size();
		for ( XinqiMessage xinqi : list ) {
			System.out.println(xinqi.payload);
			if ( xinqi.payload instanceof BseMailList ) {
				bse = (BseMailList)xinqi.payload;
				break;
			}
		}
		assertNotNull(bse);
		
		Thread.sleep(200);
		
  	//check again
		list.clear();
		mail.checkMail(user);
		Thread.sleep(200);
		assertEquals(size, list.size());
	}
	
	@Test
	public void testPushSimpleMessage() {
		MailMessageManager mail = MailMessageManager.getInstance();
		//5de10cca 9c6f49ee 4fd17d85 4ba69728 11499479 ee9e4a7d 93d58374 38b7ebb5
		String deviceToken = "082bc80b8d67af604c163230145a645402489ec4c7e1ed6b3d00925200fee76e";
		mail.pushSimpleMessage("wangqi", deviceToken, "测试一下系统", -1);
		
		Map<String, Date> devices = mail.getIosInactiveDevices();
		for (String token : devices.keySet()) {
	    Date inactiveAsOf = devices.get(token);
	    System.out.println(token+":"+inactiveAsOf);
		}
	}
	
	@Test
	public void testPushSimpleMessageAndroid() {
		MailMessageManager mail = MailMessageManager.getInstance();
		//5de10cca 9c6f49ee 4fd17d85 4ba69728 11499479 ee9e4a7d 93d58374 38b7ebb5
		String deviceToken = null;
		mail.pushSimpleMessage("55146", deviceToken, "测试一下系统1", -1);
		mail.pushSimpleMessage("55146", deviceToken, "测试一下系统2", -1);
		
		Map<String, Date> devices = mail.getIosInactiveDevices();
		for (String token : devices.keySet()) {
	    Date inactiveAsOf = devices.get(token);
	    System.out.println(token+":"+inactiveAsOf);
		}
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
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		//IoSession session = TestUtil.createIoSession(new ArrayList());
		//GameContext.getInstance().registerUserSession(session, user, user.getSessionKey());
		
		return user;
	}
	
	private Collection<Reward> prepareRewards() {
		Reward reward = new Reward();
		reward.setId(UserManager.basicUserGiftBoxId);
		reward.setPropColor(WeaponColor.WHITE);
		reward.setPropCount(1);
		reward.setLevel(1);
		reward.setPropIndate(0);
		reward.setType(RewardType.ITEM);
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		rewards.add(reward);
		return rewards;
	}
	
	private Collection<Reward> prepareColorRewards() {
		Reward reward = new Reward();
		//687	水晶●青龙鳞
		reward.setId("687");
		reward.setPropColor(WeaponColor.ORGANCE);
		reward.setPropCount(1);
		reward.setLevel(10);
		reward.setPropIndate(100);
		reward.setType(RewardType.WEAPON);
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		rewards.add(reward);
		return rewards;
	}
}
