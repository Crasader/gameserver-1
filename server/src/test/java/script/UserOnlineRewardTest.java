package script;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class UserOnlineRewardTest {
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetOnlineReward() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		int step = 0;
		
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, step);
		assertEquals(result.getType(), ScriptResult.Type.SUCCESS_RETURN );
		List list = result.getResult();
		assertEquals(4, list.size());
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Reward object = (Reward) iterator.next();
			Reward reward = (Reward)list.get(i++);
			System.out.println(reward);
			if ( i == 0 ) {
				assertEquals(1, reward.getPropCount());
				assertEquals(1, reward.getPropLevel());
			}
		}		
	}

	@Test
	public void testGetOnlineRewardStep2() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		int step = 2;
		
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, step);
		assertEquals(result.getType(), ScriptResult.Type.SUCCESS_RETURN );
		Reward reward = (Reward)result.getResult().get(0);
		System.out.println(reward);
		
		assertEquals(1, reward.getPropCount());
		assertEquals(3, reward.getPropLevel());
	}
	
	@Test
	public void testGetOnlineRewardStep6() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		int step = 6;
		
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, step);
		assertEquals(result.getType(), ScriptResult.Type.SUCCESS_RETURN );
		Reward reward = (Reward)result.getResult().get(0);
		System.out.println(reward);
		
		assertEquals(4, reward.getPropCount());
		assertEquals(1, reward.getPropLevel());
	}
	
	@Test
	public void testGetOnlineRewardUserLevel20() {
		User user = UserManager.getInstance().createDefaultUser();
		user.setUsername(userName);
		user.setLevel(20);
		int step = 6;
		
		ScriptResult result = ScriptManager.getInstance().runScript(ScriptHook.USER_ONLINE_REWARD, user, step);
		assertEquals(result.getType(), ScriptResult.Type.SUCCESS_RETURN );
		Reward reward = (Reward)result.getResult().get(0);
		System.out.println(reward);
		
		assertEquals(6, reward.getPropCount());
		assertEquals(2, reward.getPropLevel());
	}
}
