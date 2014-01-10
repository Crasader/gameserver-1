package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.CaishenManager;
import com.xinqihd.sns.gameserver.db.mongo.ChargeManager;
import com.xinqihd.sns.gameserver.db.mongo.RoleActionManager;
import com.xinqihd.sns.gameserver.db.mongo.VipManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.session.CipherManager;

public class VipChargeLevelTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFreeToVIP6() {
		User user = prepareUser();
		int moneyCount = 999;
		String token = CipherManager.getInstance().generateEncryptedUserToken(user.get_id());
		ChargeManager.getInstance().freeCharge(user, moneyCount, token);
		assertTrue(user.isVip());
		assertEquals(6, user.getViplevel());
		
		Bag bag = user.getBag();
		assertEquals(60+36, bag.getMaxCount());
		assertEquals(6, bag.getCurrentCount());
		for ( PropData propData : bag.getOtherPropDatas() ) {
			System.out.println(propData);
		}
		int roleBuyCount = RoleActionManager.getInstance().queryRoleActionLimit(
				user, System.currentTimeMillis(), false)[0];
		assertEquals(21, roleBuyCount);
		int caishenBuyCount = CaishenManager.getInstance().queryCaishenPrayInfo(
				user, System.currentTimeMillis(), false)[0];
		assertEquals(50, caishenBuyCount);
	}
	
	@Test
	public void testGetRoleActionCount() {
		User user = prepareUser();
		user.setIsvip(true);
		user.setViplevel(10);
		
		VipManager manager = VipManager.getInstance();
		int buyCount = manager.getVipLevelRoleActionBuyCount(user);
		assertEquals(46, buyCount);
	}
	
	@Test
	public void testGetCaishenBuyCount() {
		User user = prepareUser();
		user.setIsvip(true);
		user.setViplevel(10);
		
		VipManager manager = VipManager.getInstance();
		int buyCount = manager.getVipLevelCaishenBuyCount(user);
		assertEquals(600, buyCount);
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
