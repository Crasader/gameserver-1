package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class UserCalculateBloodTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		user.setLevel(10);
		user.setDefend(50);
		int blood = manager.runScriptForInt(ScriptHook.USER_CALCULATE_BLOOD, user);
		
		assertEquals(1510, blood);
	}

}
