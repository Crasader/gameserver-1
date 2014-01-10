package com.xinqihd.sns.gameserver.battle;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.apache.mina.core.session.IoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.util.TestUtil;

public class BattleTest {

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_MESSAGE_SERVER_ID, "localhost:0");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_RPC_SERVERID, "");
		GlobalConfig.getInstance().overrideProperty("zookeeper.root",
				"/snsgame/babywar");

		GlobalConfig.getInstance().overrideProperty(
				GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(
				GlobalConfigKey.deploy_data_dir, "../deploy/data");
		
		BattleDataLoader4Bitmap.loadBattleMaps();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetUserModeBit() {
		int userStatus = RoleStatus.NORMAL.toUserModeBit();
		assertEquals("0", Integer.toBinaryString(userStatus));
//		System.out.println(Integer.toBinaryString(userStatus)); 
		
		userStatus = RoleStatus.DEAD.toUserModeBit();
		assertEquals("1", Integer.toBinaryString(userStatus));
//		System.out.println(Integer.toBinaryString(userStatus));
		
		userStatus = RoleStatus.HIDDEN.toUserModeBit();
		assertEquals("10", Integer.toBinaryString(userStatus));
//		System.out.println(Integer.toBinaryString(userStatus));
		
		userStatus = RoleStatus.ICED.toUserModeBit();
		assertEquals("100", Integer.toBinaryString(userStatus));
//		System.out.println(Integer.toBinaryString(userStatus));
		
		userStatus = RoleStatus.FLYING.toUserModeBit();
		assertEquals("1000", Integer.toBinaryString(userStatus));
//		System.out.println(Integer.toBinaryString(userStatus));
						
	}
	
	@Test
	public void testStartPoint() {
		User user1 = new User();
		user1.set_id(new UserId("test1"));
		IoSession session1 = TestUtil.createIoSession(new ArrayList());
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session1, user1, null);
		UserInfo info1 = new UserInfo();
		info1.setUserSessionKey(user1.getSessionKey());
		
		User user2 = new User();
		user2.set_id(new UserId("test2"));
		IoSession session2 = TestUtil.createIoSession(new ArrayList());
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		GameContext.getInstance().registerUserSession(session2, user2, null);
		UserInfo info2 = new UserInfo();
		info2.setUserSessionKey(user2.getSessionKey());
		
		Room left = new Room();
		left.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString());
		left.addUser(info1);
		Room right = new Room();
		right.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString());
		right.addUser(info2);
		BattleRoom room  = new BattleRoom();
		room.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.setRoomLeft(left);
		room.setRoomRigth(right);
		
		Battle battle = new Battle(room, "");
		BattleUser bUser1 = new BattleUser();
		bUser1.setUser(user1);
		BattleUser bUser2 = new BattleUser();
		bUser2.setUser(user2);
		BattleUser bUser3 = new BattleUser();
		bUser3.setUser(user2);
		BattleUser bUser4 = new BattleUser();
		bUser4.setUser(user2);
		
		ArrayList<BattleUser> bUsers = new ArrayList<BattleUser>();
		bUsers.add(bUser1);
		bUsers.add(bUser2);
		bUsers.add(bUser3);
		bUsers.add(bUser4);
				
		for ( int i=0; i<10; i++ ) {
			battle.pickStartPoint(bUsers, 0, 
					battle.getBattleMap().getMapPojo().getStartPoints());
			System.out.println(bUser1.getPosX()+","+bUser1.getPosY());
			System.out.println(bUser2.getPosX()+","+bUser2.getPosY());
			System.out.println(bUser3.getPosX()+","+bUser3.getPosY());
			System.out.println(bUser4.getPosX()+","+bUser4.getPosY());
			System.out.println("===================");
		}
	}
}
