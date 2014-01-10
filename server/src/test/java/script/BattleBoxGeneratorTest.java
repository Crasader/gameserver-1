package script;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.BattleBitSetMap;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.geom.SimplePoint;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;

public class BattleBoxGeneratorTest extends AbstractScriptTestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFunc() {
		User user = new User();
		user.setLevel(19);
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, "../deploy/data");
		BattleDataLoader4Bitmap.loadBattleMaps();
		BattleBitSetMap battleMap = BattleDataLoader4Bitmap.getBattleMapById("1");
		int maxLevel = 100;
		ScriptManager manager = ScriptManager.getInstance();

		for ( int i=0; i<10; i++ ) {
			Reward treasureBox = (Reward)
					manager.runScriptForObject(ScriptHook.BATTLE_BOX_REWARD, user, new SimplePoint(10, 10));
			System.out.println(treasureBox);
		}
	}

}
