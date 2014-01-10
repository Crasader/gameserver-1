package com.xinqihd.sns.gameserver.battle;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.session.SessionKey;

public class RoomTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddUser() {
		Room room = new Room();
		room.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		// -----
		
		UserInfo u1 = new UserInfo();
		u1.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.addUser(u1);
		
		assertEquals(1, room.getCurrentUserCount());
		assertEquals(u1, room.getUserInfoList().get(0));
		
		// -----
		
		UserInfo u2 = new UserInfo();
		u2.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.addUser(u2);
		
		assertEquals(2, room.getCurrentUserCount());
		assertEquals(u1, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));

		// -----
		
		room.removeUser(u1.getUserSessionKey());
		assertEquals(1, room.getCurrentUserCount());
		assertEquals(null, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));
		
		// -----
		
		UserInfo u3 = new UserInfo();
		u3.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.addUser(u3);
		
		assertEquals(2, room.getCurrentUserCount());
		assertEquals(u3, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));
	}
	
	@Test
	public void testAddUserOverMax() {
		Room room = new Room();
		room.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		// -----
		for ( int i=0; i<Room.MAX_USER*2; i++ ) {
			UserInfo u1 = new UserInfo();
			u1.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
			room.addUser(u1);
		}
		
		assertEquals(Room.MAX_USER, room.getCurrentUserCount());
	}
	
	@Test
	public void testSetUser() {
		Room room = new Room();
		room.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString());
		// -----
		
		UserInfo u1 = new UserInfo();
		u1.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.addUser(u1);
		
		UserInfo u2 = new UserInfo();
		u2.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.addUser(u2);
		
		assertEquals(2, room.getCurrentUserCount());
		assertEquals(u1, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));
		
		UserInfo u3 = new UserInfo();
		u3.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.setUser(u3, 2);
		
		assertEquals(3, room.getCurrentUserCount());
		assertEquals(u1, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));
		assertEquals(u3, room.getUserInfoList().get(2));
		
		UserInfo u4 = new UserInfo();
		u4.setUserSessionKey(SessionKey.createSessionKeyFromRandomString());
		room.setUser(u4, 2);
		
		assertEquals(3, room.getCurrentUserCount());
		assertEquals(u1, room.getUserInfoList().get(0));
		assertEquals(u2, room.getUserInfoList().get(1));
		assertEquals(u4, room.getUserInfoList().get(2));
	}

}
