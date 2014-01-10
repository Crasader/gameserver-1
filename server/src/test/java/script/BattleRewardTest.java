package script;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRewardTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReward() {
		int slot = 26;
		User user = UserManager.getInstance().createDefaultUser();
		user.setIsvip(true);
		
		ScriptManager manager = ScriptManager.getInstance();
		ScriptResult result = manager.runScript(ScriptHook.BATTLE_REWARD, user, slot);

		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		ArrayList list = (ArrayList)result.getResult();
		assertEquals(slot, list.size());
		
		for ( int i=0; i<list.size(); i++ ) {
			System.out.println("rewards:: " + list.get(i));
		}
	}

	@Test
	public void testReward2() {
		int slot = 10;
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setIsvip(true);
		
		ScriptResult result = manager.runScript(ScriptHook.BATTLE_REWARD, user, slot);

		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		
		ArrayList list = (ArrayList)result.getResult();
		assertEquals(slot, list.size());
		
		for ( int i=0; i<list.size(); i++ ) {
			System.out.println("rewards:: " + list.get(i));
		}
	}
	
}
