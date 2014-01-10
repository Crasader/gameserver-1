package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class BattleExpRateTest extends AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testVip() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setIsvip(true);
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		
		int expRate = manager.runScriptForInt(ScriptHook.BATTLE_EXP_RATE, battleUser);

		System.out.println("expRate: " + expRate);
		assertEquals(1, expRate);
	}
	
	@Test
	public void testNormal() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setIsvip(false);
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		
		int expRate = manager.runScriptForInt(ScriptHook.BATTLE_EXP_RATE, battleUser);

		System.out.println("expRate: " + expRate);
		assertEquals(1, expRate);
	}

}
