package script;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.ActionType;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleRoom;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class BattleRoleUseToolTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEnergy() {
		ScriptManager manager = ScriptManager.getInstance();
		User user = UserManager.getInstance().createDefaultUser();
		BattleRoom room = new BattleRoom();
		room.setRoomLeft(new Room());
		room.setRoomRigth(new Room());
		Battle battle = new Battle(room, "");
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		battleUser.setActionType(ActionType.FIRE);
		battleUser.setDelay(20);
		battleUser.setThew(battleUser.getUser().getTkew());
		
		int beforeEnergy = battleUser.getEnergy();
		
		ScriptResult result = manager.runScript(ScriptHook.BATTLE_ROLE_USETOOL, battle, 
				battleUser, BuffToolType.Energy);
		
		List list = result.getResult();
		assertEquals(20, battleUser.getDelay());
		assertTrue(battleUser.getThew()>=210);
		
		assertTrue( battleUser.getEnergy() > beforeEnergy ) ;
	}

}
