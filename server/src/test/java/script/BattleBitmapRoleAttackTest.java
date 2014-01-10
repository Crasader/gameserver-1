package script;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Battle;
import com.xinqihd.sns.gameserver.battle.BattleDataLoader4Bitmap;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.BattleUser;
import com.xinqihd.sns.gameserver.battle.BuffToolType;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.WeaponColor;
import com.xinqihd.sns.gameserver.config.equip.WeaponPojo;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EquipManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceRoleAttack.BceRoleAttack;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BattleBitmapRoleAttackTest {
	
	String expectRpcServerId = "localhost:3445";
	
	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_GAME_SERVERID, expectRpcServerId);
		GlobalConfig.getInstance().overrideProperty("zookeeper.root",
				"/snsgame/babywar");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
		
		System.setProperty("mapImgdir", "../data/map");
		BattleDataLoader4Bitmap.loadBattleBullet();
		BattleDataLoader4Bitmap.loadBattleMaps();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRoleAttack() throws Exception {
		Jedis jedis = JedisFactory.getJedis();
		String mapId = "6";
		String bullet = "EXBullet_redRune";
		//RoleAttack: userPos: (610, 417), angle: 14, power: 101, dir: 1
		BceRoleAttack.Builder attack = BceRoleAttack.newBuilder();
		attack.setAngle(147);
		attack.setPower(19);
		attack.setUserx(976);
		attack.setUsery(0);
		
		attack.setAtkAngle(attack.getAngle());
		attack.setDirection(-1);

		BattleManager battleManager = BattleManager.getInstance();

		ArrayList list = new ArrayList();
		Battle battle = makeReadyBattle(battleManager, list, 
				BuffToolType.Ice, mapId, bullet);

		HashMap<SessionKey, BattleUser> battleUserMap = battle.getBattleUserMap();
		assertEquals(2, battleUserMap.size());

		BattleUser battleUser = battle.getBattleUserMap().values().iterator()
				.next();
		int beforeThew = battleUser.getThew();
		int beforeDelay = battleUser.getDelay();
		
		list.clear();
		
		battleManager.roleAttack(battleUser.getUserSessionKey(), attack.build());
		
		Thread.sleep(200);
		System.out.println(list);

		// Clean system.
		jedis.del(battle.getBattleSessionKey().toString());
		for ( BattleUser bUser : battle.getBattleUserMap().values() ) {
			jedis.del(bUser.getUserSessionKey().toString());	
		}
	}

	private Battle makeReadyBattle(BattleManager battleManager, ArrayList list, 
			BuffToolType tool, String mapId, String bullet) {

		UserManager manager = UserManager.getInstance();
		manager.removeUser("test001");
		manager.removeUser("test002");
		
		WeaponPojo weapon = EquipManager.getInstance().getWeaponById(UserManager.basicWeaponItemId);
		weapon.setBullet(bullet);

		User user1 = manager.createDefaultUser();
		user1.set_id(new UserId("test001"));
		// user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user1.setUsername("test001");
		for (int i = 0; i < 1; i++) {
			user1.addTool(tool);
		}
		user1.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));

		User user2 = manager.createDefaultUser();
		user2.set_id(new UserId("test002"));
		// user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		user2.setUsername("test002");
		for (int i = 0; i < 1; i++) {
			user2.addTool(tool);
		}
		user2.getBag().addOtherPropDatas(weapon.toPropData(30, WeaponColor.WHITE));

		manager.saveUser(user1, true);
		manager.saveUser(user2, true);

		SessionManager sessionManager = new SessionManager();

		IoSession session1 = TestUtil.createIoSession(list);
		GameContext.getInstance().registerUserSession(session1, user1, null);

		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		GameContext.getInstance().registerUserSession(session2, user2, null);

		battleManager.clearAllBattles();

		// Create two single room
		RoomManager roomManager = RoomManager.getInstance();
		roomManager.assignRoom(user1, RoomType.SINGLE_ROOM);
		roomManager.assignRoom(user2, RoomType.SINGLE_ROOM);
		Collection<Room> rooms = roomManager.getLocalRoomCollection();
		for ( Room room : rooms ) {
			room.setMapId(mapId);
		}
		roomManager.readyStart(user1.getSessionKey(), true);
		roomManager.readyStart(user2.getSessionKey(), true);

		Collection<Battle> battles = battleManager.findAllBattles();

		// Thread.sleep(Long.MAX_VALUE);
		assertEquals(1, battles.size());

		Battle battle = battles.iterator().next();

		return battle;
	}
}
