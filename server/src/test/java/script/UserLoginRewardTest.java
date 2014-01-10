package script;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.config.equip.ItemPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ItemManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class UserLoginRewardTest {
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoginReward() {
		User user = new User();
		user.setLevel(1);
		ArrayList<Reward> loginRewards = RewardManager.getInstance().generateRewardsFromScript(
				user, 10, ScriptHook.USER_LOGIN_REWARD);
		assertEquals(10, loginRewards.size());
	}
	
	@Test
	public void testLoginRewardNoGiftBox() {
		User user = new User();
		user.setLevel(1);
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		for ( int i=0; i<100; i++ ) {
			ArrayList<Reward> loginRewards = RewardManager.getInstance().generateRewardsFromScript(
					user, 10, ScriptHook.USER_LOGIN_REWARD);
			for ( Reward r : loginRewards ) {
				if ( r.getType() == RewardType.ITEM ) {
					ItemPojo item = ItemManager.getInstance().getItemById(r.getId());
					Integer count = countMap.get(item.getName());
					if ( count == null ) {
						count = 1;
					} else {
						count = count.intValue()+1;
					}
					countMap.put(item.getName(), count);
				}
			}
		}
		for ( String key : countMap.keySet() ) {
			System.out.println(key + "=" + countMap.get(key));
			assertTrue(!key.startsWith("升级"));
		}
	}

	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
