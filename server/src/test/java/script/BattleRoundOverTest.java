package script;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.ActionType;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRoundOverTest extends AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFire() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		battleUser.setActionType(ActionType.FIRE);
		battleUser.setDelay(20);
		
		ScriptResult result = manager.runScript(ScriptHook.BATTLE_ROUND_OVER, battleUser, true);
		List list = result.getResult();
		BattleUser actualUser = (BattleUser)list.get(0);
		assertEquals(220, actualUser.getDelay());
	}

	@Test
	public void testFly() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		battleUser.setActionType(ActionType.FLY);
		battleUser.setDelay(20);
		
		ScriptResult result = manager.runScript(ScriptHook.BATTLE_ROUND_OVER, battleUser, true);
		List list = result.getResult();
		BattleUser actualUser = (BattleUser)list.get(0);
		assertEquals(175, actualUser.getDelay());
	}
}
