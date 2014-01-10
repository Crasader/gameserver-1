package script;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;

public class UserLevelProcessingTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		UserManager manager= UserManager.getInstance();
		User user = manager.getInstance().queryUser("10000");
		UserLevelProcessing.checkUnlock(user, GameFuncType.Room, RoomType.PVE_ROOM.ordinal());
	}

}
