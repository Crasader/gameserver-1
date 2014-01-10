package script.task;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class LevelUpBoxTest {

	String userName = "test-001";

	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOpenLevelUpBox() throws Exception {
		String userName = "test-001";
		User user = prepareUser(userName);
		user.setLevelSimple(30);
		
		ArrayList<XinqiMessage> list = new ArrayList<XinqiMessage>();
		IoSession session = TestUtil.createIoSession(list);
		user.setSession(session);
		user.set_id(new UserId(userName));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session, user, null);
		
		RewardManager manager = RewardManager.getInstance();
		manager.clearUserReward(userName);
		
		/*
		 * itemId itemName itemLevel script 	 count 	 q 	rewardId 	 rewardName 	 rewardLevel 	 rewardCount 	 rewardIndate
		 * 25039	升级奖励Lv29	Baoxiang0001	对玩家努力升级的奖赏，VIP玩家会获得双倍奖励，达到29级可以打开，包含1500金币、1个4级强化石。	25011	29	script.box.LevelUpBox
		 */
		
		ItemPojo box = ItemManager.getInstance().getItemById("25039");
		PropData propData = box.toPropData();
		//Put it into user's bag
		Bag bag = user.getBag();
		bag.addOtherPropDatas(propData);
		assertEquals(1, bag.getOtherPropDatas().size());
		
		//open the box
		PickRewardResult pickResult = manager.openItemBox(user, propData.getPew());
		assertEquals(PickRewardResult.SUCCESS, pickResult);
		assertEquals(1, bag.getCurrentCount());
		
		/*
		 * Reward:
			ITEM	20024		0	1	0	0	100%
			GOLDEN	-1		0	1500	0	0	100%
		 */
		PropData newWeapon = bag.getOtherPropDatas().get(1);
		assertNotNull(newWeapon);
		assertEquals("20024", newWeapon.getItemId());
		assertEquals(15, user.getGolden());
		System.out.println(newWeapon);
		
		Thread.sleep(200);
		System.out.println(list);
//		XinqiMessage xinqi = null;
//		for ( XinqiMessage msg : list ) {
//			if (msg.payload instanceof BseFinishAchievement) {
//				xinqi = msg;
//			}
//		}
//		assertNotNull("BseFinishAchievement", xinqi);
	}
	
	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setRoleName(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user.getBag().removeOtherPropDatas(Bag.BAG_WEAR_COUNT);
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
