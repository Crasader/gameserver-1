package script;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.treasure.TreasureHuntManager;
import com.xinqihd.sns.gameserver.treasure.TreasurePojo;

public class TreasureHuntPickTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTreasureHuntBest() {
		User user = prepareUser();
		TreasureHuntManager.getInstance().queryTreasureHuntInfo(user, System.currentTimeMillis(), false);
		HashMap<Integer, TreasurePojo> treasures = 
				(HashMap<Integer, TreasurePojo>)user.getUserData(
						TreasureHuntManager.USER_DATA_HUNT_TREASURE);
		//Normal mode
		TreasurePojo treasure = treasures.get(0);
		Reward best = treasure.getGifts().get(0);
		Reward reward = null;
		int rewardIndex = 0;
		int count = 0;
		while ( true ) {
			count++;
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.TREASURE_HUNT_PICK, user, treasure);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				reward = (Reward)result.getResult().get(0);
				rewardIndex = (Integer)result.getResult().get(1);
			} else {
			  //给予默认奖励
				reward = RewardManager.getInstance().getRewardGolden(user);
			}
			if ( rewardIndex > 0  ) {
				//break;
				System.out.println("hunt count:"+count+"; rewardIndex="+rewardIndex);
			}
		}
		//System.out.println("hunt count:"+count+"; rewardIndex="+rewardIndex);
	}
	
	@Test
	public void testTreasureHuntFourth() {
		User user = prepareUser();
		TreasureHuntManager.getInstance().queryTreasureHuntInfo(user, System.currentTimeMillis(), false);
		HashMap<Integer, TreasurePojo> treasures = 
				(HashMap<Integer, TreasurePojo>)user.getUserData(
						TreasureHuntManager.USER_DATA_HUNT_TREASURE);
		//Normal mode
		TreasurePojo treasure = treasures.get(0);
		Reward best = treasure.getGifts().get(3);
		Reward reward = null;
		int rewardIndex = 0;
		int count = 0;
		while ( true ) {
			count++;
			ScriptResult result = ScriptManager.getInstance().runScript(
					ScriptHook.TREASURE_HUNT_PICK, user, treasure);
			if ( result.getType() == ScriptResult.Type.SUCCESS_RETURN ) {
				reward = (Reward)result.getResult().get(0);
				rewardIndex = (Integer)result.getResult().get(1);
				if ( rewardIndex <= 8 ) {
					break;
				}
			} else {
			//给予默认奖励
				reward = RewardManager.getInstance().getRewardGolden(user);
			}
		}
		System.out.println("hunt count:"+count);
	}

	private User prepareUser() {
		User user = new User();
		String roleName = "test-001";
		user.set_id(new UserId(roleName));
		user.setRoleName(roleName);
		user.setUsername(roleName);
		user.setLevelSimple(30);
		UserManager.getInstance().removeUser(roleName);
		return user;
	}
}
