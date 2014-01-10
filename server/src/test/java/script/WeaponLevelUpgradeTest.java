package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class WeaponLevelUpgradeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {		
		ScriptManager manager = ScriptManager.getInstance();
		PropData propData = new PropData();
		propData.setItemId(UserManager.basicWeaponItemId);
		int level = 9;
		
		for ( int i=0; i<level; i++ ) {
			propData.setAttackLev(100);
			propData.setDefendLev(100);
			ScriptResult result = manager.runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, propData, i);
			assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
			PropData newPropData = (PropData)result.getResult().get(0);
//			System.out.println(newPropData.getAttackLev()+","+newPropData.getDefendLev());
		}
	}
	
	@Test
	public void testUpgrade2Level() {
		ScriptManager manager = ScriptManager.getInstance();
		PropData propData = new PropData();
		propData.setItemId(UserManager.basicWeaponItemId);
		int level = 2;
		
		propData.setAttackLev(100);
		propData.setDefendLev(100);
		ScriptResult result = manager.runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, propData, 2);
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		PropData newPropData = (PropData)result.getResult().get(0);
		System.out.println(newPropData.getAttackLev()+","+newPropData.getDefendLev());
		
	}

	@Test
	public void testUpgrade0Level() {
		ScriptManager manager = ScriptManager.getInstance();
		PropData propData = new PropData();
		propData.setItemId(UserManager.basicWeaponItemId);
		int level = 0;
		
		propData.setAttackLev(100);
		propData.setDefendLev(100);
		ScriptResult result = manager.runScript(ScriptHook.WEAPON_LEVEL_UPGRADE, propData, level);
		assertEquals(ScriptResult.Type.SUCCESS_RETURN, result.getType());
		PropData newPropData = (PropData)result.getResult().get(0);
		assertEquals(100, newPropData.getAttackLev());
		assertEquals(100, newPropData.getDefendLev());
		assertEquals(0, newPropData.getEnhanceMap().size());
	}
}
