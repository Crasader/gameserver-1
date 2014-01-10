package script;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.RoleStatus;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.script.ScriptResult;

public class PickRoundUserTest extends AbstractScriptTestCase {
	
	Random r = new Random();

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFunc1() {
		int totalUser = 8;
		HashMap<BattleUser, Integer> roundMap = new HashMap<BattleUser, Integer>();
		ArrayList battleUsers = new ArrayList(totalUser);
		for ( int i=0; i<totalUser; i++ ) {
			BattleUser user = createBattleUser(10*i, i);
			battleUsers.add(user);
			roundMap.put(user, 0);
		}
		
		ScriptManager manager = ScriptManager.getInstance();
		ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, null);
		
		BattleUser minDelayUser = null;
		int minDelay = Integer.MAX_VALUE;
		for ( int i=0; i<battleUsers.size(); i++ ) {
			BattleUser battleUser = (BattleUser)battleUsers.get(i);
			if ( minDelay > battleUser.getDelay() ) {
				minDelay = battleUser.getDelay();
				minDelayUser = battleUser;
			}
			System.out.println("battleUser's delay: " + battleUser.getDelay());
		}
	
		BattleUser actualUser = (BattleUser)result.getResult().get(0);
		System.out.println("Next round's owner: " + minDelayUser.getDelay() + 
				", actual: " + actualUser.getDelay());
		assertEquals(minDelayUser.getDelay(), actualUser.getDelay());
	}

	@Test
	public void testFunc2() {
		int totalUser = 8;
		HashMap<BattleUser, Integer> roundMap = new HashMap<BattleUser, Integer>();
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(totalUser);
		for ( int i=0; i<totalUser; i++ ) {
			BattleUser user = createBattleUser(10*i, i);
			battleUsers.add(user);
			roundMap.put(user, 0);
		}
		
		for ( int i=0; i<100; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, null);
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			roundMap.put(actualUser, roundMap.get(actualUser)+1);
			actualUser.setDelay(actualUser.getDelay()+100);
		}
		
		Set<BattleUser> userSet = roundMap.keySet();
		for ( BattleUser user : userSet ) {
			int roundTime = roundMap.get(user);
			System.out.println("User agility: " + user.getUser().getAgility()+", delay:" + user.getDelay() + ", round: " + roundTime);
			assertTrue("Should have at least one round time.", roundTime>0);
		}
	}
	
	@Test
	public void testRoundOwnerFrozen() {
		int totalUser = 8;
		
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(totalUser);
		for ( int i=0; i<totalUser; i++ ) {
			BattleUser user = createBattleUser(10*i, i);
			battleUsers.add(user);
			user.addStatus(RoleStatus.ICED);
		}
		BattleUser lastUser = battleUsers.get(battleUsers.size()-1);
		lastUser.removeStatus(RoleStatus.ICED);
		lastUser.addStatus(RoleStatus.NORMAL);
		
		for ( int i=0; i<100; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, null);
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			assertEquals(lastUser, actualUser);
			actualUser.setDelay(actualUser.getDelay()+100);
		}
	}
	
	@Test
	public void testRoundOwnerFrozen2User() {
		int totalUser = 2;
		
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(totalUser);
		for ( int i=0; i<totalUser; i++ ) {
			BattleUser user = createBattleUser(10*i, i);
			battleUsers.add(user);
		}
		battleUsers.get(0).addStatus(RoleStatus.ICED);
		
		for ( int i=0; i<100; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, 
					battleUsers.get(1));
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			assertEquals(battleUsers.get(1), actualUser);
			actualUser.setDelay(actualUser.getDelay()+100);
		}
		
		battleUsers.get(0).clearStatus();
		battleUsers.get(0).addStatus(RoleStatus.NORMAL);
		battleUsers.get(0).setDelay(0);
		battleUsers.get(1).setDelay(0);
		int user1=0, user2=0;
		for ( int i=0; i<100; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, 
					battleUsers.get(1));
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			actualUser.setDelay(actualUser.getDelay()+100);
			if ( actualUser == battleUsers.get(0) ) {
				user1++;
			} else if ( actualUser == battleUsers.get(1) ) {
				user2++;
			}
		}
		System.out.println("user1:"+user1+", user2:"+user2);
	}
	
	@Test
	public void testRoundOwnerAllNull() {
		int totalUser = 2;
		
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(totalUser);
		for ( int i=0; i<totalUser; i++ ) {
			BattleUser user = createBattleUser(10*i, i);
			battleUsers.add(user);
			user.addStatus(RoleStatus.DEAD);
		}
		
		for ( int i=0; i<10; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, null);
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			assertNull(actualUser);
		}
	}
	
	@Test
	public void testLastRoundOwner() {
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(2);
		BattleUser user1 = createBattleUser(10, 0);
		battleUsers.add(user1);
		battleUsers.add(null);
		
		for ( int i=0; i<10; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, user1);
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			assertEquals(user1, actualUser);
		}
	}
	
	@Test
	public void testLastRoundOffline() {
		ArrayList<BattleUser> battleUsers = new ArrayList<BattleUser>(2);
		BattleUser user1 = createBattleUser(10, 0);
		battleUsers.add(user1);
		battleUsers.add(null);
		
		for ( int i=0; i<10; i++ ) {
			ScriptManager manager = ScriptManager.getInstance();
			ScriptResult result = manager.runScript(ScriptHook.PICK_ROUND_USER, battleUsers, user1);
			
			BattleUser actualUser = (BattleUser)result.getResult().get(0);
			assertEquals(user1, actualUser);
		}
	}

	private BattleUser createBattleUser(int agility, int i) {
		User user = UserManager.getInstance().createDefaultUser();
		user.setAgility(agility);
		user.setUsername("test-"+i);
		user.setRoleName(user.getUsername());
		BattleUser battleUser = new BattleUser();
		battleUser.setUser(user);
		battleUser.setDelay(r.nextInt(100));
		return battleUser;
	}
}
