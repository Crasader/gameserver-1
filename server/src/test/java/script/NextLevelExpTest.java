package script;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class NextLevelExpTest extends AbstractScriptTestCase {

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
		for ( int i=1; i<maxLevel; i++ ) {
			int requiredLevel = manager.runScriptForInt(ScriptHook.NEXT_LEVEL_EXP, i);
			System.out.println("Level " + i + " required " + requiredLevel + " EXP. ");
		}
	}

}
