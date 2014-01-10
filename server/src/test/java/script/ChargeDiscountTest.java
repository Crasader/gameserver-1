package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class ChargeDiscountTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() {
		int yuanbao = ScriptManager.getInstance().runScriptForInt(ScriptHook.CHARGE_DISCOUNT, new User(), 400f);
		System.out.println(yuanbao);
		assertTrue(yuanbao > 1000);
	}

}
