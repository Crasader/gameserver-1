package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.mongo.LevelManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class UserLevelUpgradeTest extends AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() {
		int maxLevel = 100;
		ScriptManager manager = ScriptManager.getInstance();
		LevelManager lm = LevelManager.getInstance();
		User user = new User();
		for ( int i=1; i<maxLevel; i++ ) {
			user.setLevel(i);
			ScriptResult result = manager.runScript(ScriptHook.USER_LEVEL_UPGRADE, user);
			System.out.println("Level " + i + " user power:"+ user.getPower()+", blood:"+user.getBlood()+
					", skin:"+user.getSkin()+", attack:"+user.getAttack()+", defend:"+user.getDefend()+
					", agility:"+user.getAgility()+", luck:"+user.getLuck());
			assertEquals(lm.getLevel(i).getBlood(), user.getBlood());
			assertEquals(lm.getLevel(i).getSkin(), user.getSkin());
		}
	}

}
