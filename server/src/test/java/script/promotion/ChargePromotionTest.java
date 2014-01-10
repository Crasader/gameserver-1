package script.promotion;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class ChargePromotionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		User user = UserManager.getInstance().createDefaultUser();
		int yuanbao = 1000;
		ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_CHARGE, user, yuanbao);
		fail("Not yet implemented");
	}

}
