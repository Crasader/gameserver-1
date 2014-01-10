package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class UserUpdateStatTest {
	
	String userName = "test-001";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBattleFail() {
		User user = prepareUser(userName);
		int failCount = 10;
		int winCount = 5;
		for ( int i=0; i<failCount; i++ ) {
			ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.USER_UPDATE_STAT, user, false);
		}
		for ( int i=0; i<winCount; i++ ) {
			ScriptResult result = ScriptManager.getInstance().runScript(
				ScriptHook.USER_UPDATE_STAT, user, true);
		}
		assertEquals(15, user.getBattleCount());
		assertEquals(10, user.getFailcount());
		assertEquals(5, user.getWins());
		assertEquals(33, user.getWinOdds());
		
		User actualUser = UserManager.getInstance().queryUser(userName);
		assertEquals(user.getBattleCount(), actualUser.getBattleCount());
		assertEquals(user.getFailcount(), actualUser.getFailcount());
		assertEquals(user.getWins(), actualUser.getWins());
		assertEquals(user.getWinOdds(), actualUser.getWinOdds());
	}

	private User prepareUser(String userName) {
		UserId userId = new UserId(userName);
		
		UserManager userManager = UserManager.getInstance();
		User user = userManager.createDefaultUser();
		user.set_id(userId);
		user.setUsername(userName);
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		userManager.removeUser(userName);
		userManager.saveUser(user, true);
		userManager.saveUserBag(user, true);
		
		return user;
	}
}
