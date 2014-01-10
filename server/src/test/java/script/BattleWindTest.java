package script;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleRoom;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleWindTest extends AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFunc() {
		int max = 100;
		BattleRoom bRoom = new BattleRoom();
		bRoom.setRoomLeft(new Room());
		bRoom.setRoomRigth(new Room());
		Battle battle = new Battle(bRoom, "rpcserverid");
		BattleUser bUser = new BattleUser();
		User user = new User();
		user.setLevel(10);
		bUser.setUser(user);
		ScriptManager manager = ScriptManager.getInstance();
		
		for ( int i=0; i<max; i++ ) {
			ScriptResult result = manager.runScript(ScriptHook.BATTLE_WIND, battle, bUser);
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
	
			List list = result.getResult();
			Integer wind = (Integer)list.get(0);
			battle.setRoundWind(wind);
			System.out.println("wind: " + wind);
			assertTrue(wind>=-5 && wind<=5);
		}
	}
	
	@Test
	public void testLevelLess5() {
		int max = 10;
		ScriptManager manager = ScriptManager.getInstance();
		
		BattleRoom bRoom = new BattleRoom();
		bRoom.setRoomLeft(new Room());
		bRoom.setRoomRigth(new Room());
		Battle battle = new Battle(bRoom, "rpcserverid");
		BattleUser bUser = new BattleUser();
		User user = new User();
		user.setLevel(3);
		bUser.setUser(user);
		
		for ( int i=0; i<max; i++ ) {
			ScriptResult result = manager.runScript(ScriptHook.BATTLE_WIND, battle, bUser);
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
	
			List list = result.getResult();
			Integer wind = (Integer)list.get(0);
			System.out.println("wind: " + wind);
			assertEquals(0, wind.intValue());
		}
	}

}
