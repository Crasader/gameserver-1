package script;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.function.UserCalculator;

public class BattleRoleBitSetAttackTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCalculateEnergy() {
		User user = UserManager.getInstance().createDefaultUser();
		BattleUser bUser = new BattleUser();
		bUser.setUser(user);
		
		int blood = user.getBlood();
		int energy = BattleBitmapRoleAttack.calculateEnergy(new Battle(), bUser, bUser, 100);
		assertTrue("energy>=0 && energy<=100", energy>=0 && energy<=100);
//		System.out.println("blood: " + blood + ", energy: "+ energy);
	}

	@Test
	public void testCalculateHurt() {
		User attackUser = UserManager.getInstance().createDefaultUser();
		User defendUser = UserManager.getInstance().createDefaultUser();
		BattleUser attackBattleUser = new BattleUser();
		attackBattleUser.setUser(attackUser);
		BattleUser defendBattleUser = new BattleUser();
		defendBattleUser.setUser(defendUser);
		
		int hurt = UserCalculator.calculateHurt(
				attackBattleUser.getUser(), defendBattleUser.getUser(), null, 1.0, 1.0);
		System.out.println("hurt: " + hurt);
	}
	
	@Test
	public void testRoleAttack() {
		
	}

}
