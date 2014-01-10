package com.xinqihd.sns.gameserver.battle;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xinqihd.sns.gameserver.AbstractTest;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.session.SessionManager;
import com.xinqihd.sns.gameserver.util.JedisUtil;

public class RedisRoomManagerTest {
	
	private String gameServerId = "localhost:3445";
	private RedisRoomManager manager = null;
	private SessionManager sManager = null;

	@Before
	public void setUp() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_GAME_SERVERID, gameServerId);
		com.xinqihd.sns.gameserver.jedis.Jedis jedis = JedisFactory.getJedis();
		JedisUtil.deleteAllKeys();
		
		manager = new RedisRoomManager();
		sManager = new SessionManager();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testJoinRoomSingle() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room expect = manager.createRoom(user, RoomType.SINGLE_ROOM);
		
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-001"));
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		boolean result = manager.joinRoom(expect.getRoomSessionKey(), user2.getSessionKey());

		assertTrue(!result);
	}
	
	@Test
	public void testJoinRoomMulti() throws Exception {
		User user = prepareUser("001");
		
		Room expect = manager.createRoom(user, RoomType.MULTI_ROOM);
		assertEquals(1, expect.getCurrentUserCount() );
		assertEquals(2, expect.getMaxUserCount() );
		
		User user2 = prepareUser("002");
		
		boolean result = manager.joinRoom(expect.getRoomSessionKey(), user2.getSessionKey());
		assertEquals(2, expect.getCurrentUserCount() );
		assertEquals(2, expect.getMaxUserCount() );
		
		assertTrue(result);
	}
		
	@Test
	public void testStoreRoomSingleFull() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room expect = manager.createRoom(user, RoomType.SINGLE_ROOM);
		String oldSetName = expect.getCurrentSetName();
		assertEquals(RoomStatus.FULL, expect.getRoomStatus());
		expect.setRoomStatus(RoomStatus.FULL);
		manager.storeRoom(expect);
		
		//Get the room from redis
		Room actual = manager.acquireRoom(expect.getRoomSessionKey(), false);
		String newSetName = actual.getCurrentSetName();
		assertRoomEquals(expect, actual);
		assertEquals(expect, actual);
		
		Jedis jedis = JedisFactory.getJedis();
		double score1 = jedis.zscore(oldSetName, actual.getRoomSessionKey().toString());
		double score2 = jedis.zscore(newSetName, actual.getRoomSessionKey().toString());
		assertEquals(score1, score2, 0.001);
	}
	
	@Test
	public void testStoreRoomTimeout() throws Exception {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room expect = manager.createRoom(user, RoomType.SINGLE_ROOM);
		String oldSetName = expect.getCurrentSetName();
		assertEquals(RoomStatus.FULL, expect.getRoomStatus());
		expect.setCreatedMillis(0l);
		manager.storeRoom(expect);
		
		//Get the room from redis
		Room actual = manager.acquireRoom(expect.getRoomSessionKey(), false);
		String newSetName = actual.getCurrentSetName();
		assertRoomEquals(expect, actual);
		assertEquals(expect, actual);
		
		Jedis jedis = JedisFactory.getJedis();
		double score1 = jedis.zscore(oldSetName, actual.getRoomSessionKey().toString());
		double score2 = jedis.zscore(newSetName, actual.getRoomSessionKey().toString());
		assertEquals(score1, score2, 0.001);
	}

	
	@Test
	public void testStoreRoomMulti() throws Exception {
		User user1 = prepareUser("001");
		
		Room expect = manager.createRoom(user1, RoomType.MULTI_ROOM);
		String oldSetName = expect.getCurrentSetName();
		assertEquals(RoomStatus.UNFULL, expect.getRoomStatus());
		
		User user2 = prepareUser("002");
		manager.joinRoom(expect.getRoomSessionKey(), user2.getSessionKey());
		
		manager.storeRoom(expect);
		
		//Get the room from redis
		Room actual = manager.acquireRoom(expect.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, actual.getRoomStatus());
		String newSetName = actual.getCurrentSetName();
		assertRoomEquals(expect, actual);
		assertEquals(expect, actual);
		
		Jedis jedis = JedisFactory.getJedis();
		Double score1 = jedis.zscore(oldSetName, actual.getRoomSessionKey().toString());
		assertEquals(null, score1);
		Double score2 = jedis.zscore(newSetName, actual.getRoomSessionKey().toString());
		assertNotNull(score2);
	}
	
	@Test
	public void testCreateRoomSingle() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room expect = manager.createRoom(user, RoomType.SINGLE_ROOM);
		assertEquals("524F4F4D5F", expect.getRoomSessionKey().toString().substring(0, 10));
		assertEquals(gameServerId, expect.getGameServerId()); 
		assertEquals(RoomType.SINGLE_ROOM, expect.getRoomType());
		assertEquals(RoomStatus.FULL, expect.getRoomStatus());
		assertEquals(1, expect.getMaxUserCount());
		assertEquals(1, expect.getCurrentUserCount());
		assertEquals(0, expect.getReadyUserCount());
		assertTrue( expect.getCreatedMillis()>0 );
		assertEquals(user.getPower(), expect.getOwnerPower());
		assertEquals(user.getPower(), expect.getAveragePower());
		assertEquals("room_full_set", expect.getCurrentSetName());
		assertEquals(null, expect.getBattleRoomSessionKey());
		assertEquals(user.getSessionKey(), expect.getOwnerSessionKey());
		
		//Get the room from redis
		Room actual = manager.acquireRoom(expect.getRoomSessionKey(), false);
		assertRoomEquals(expect, actual);
		assertEquals(expect, actual);
		
		Jedis jedis = JedisFactory.getJedis();
		
		//Check the other sets
		Set<String> localRoomSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localRoomSet.size());
		String hexString = localRoomSet.iterator().next();
		SessionKey actualKey = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(expect.getRoomSessionKey(), actualKey);
		
		SessionKey roomSessionKey = manager.findRoomSessionKeyByUserSession(user.getSessionKey());
		assertEquals(expect.getRoomSessionKey(), roomSessionKey);
		
		Set<String> currentSet = jedis.zrange(actual.getCurrentSetName(), 0, 0);
		assertEquals(1, currentSet.size());
		SessionKey roomSessionKey2 = SessionKey.createSessionKeyFromHexString(currentSet.iterator().next());
		assertEquals(expect.getRoomSessionKey(), roomSessionKey2);
	}

	//---------------------------------------------------------------------------------------------------------------------------
	

	@Test
	public void testCreateRoomMulti() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room expect = manager.createRoom(user, RoomType.MULTI_ROOM);
		assertEquals("524F4F4D5F", expect.getRoomSessionKey().toString().substring(0, 10));
		assertEquals(gameServerId, expect.getGameServerId()); 
		assertEquals(RoomType.MULTI_ROOM, expect.getRoomType());
		assertEquals(RoomStatus.UNFULL, expect.getRoomStatus());
		assertEquals(2, expect.getMaxUserCount());
		assertEquals(1, expect.getCurrentUserCount());
		assertEquals(0, expect.getReadyUserCount());
		assertTrue( expect.getCreatedMillis()>0 );
		assertEquals(user.getPower(), expect.getOwnerPower());
		assertEquals(user.getPower(), expect.getAveragePower());
		assertEquals("room_unfull_set_2", expect.getCurrentSetName());
		assertEquals(null, expect.getBattleRoomSessionKey());
		assertEquals(user.getSessionKey(), expect.getOwnerSessionKey());
		
		//Get the room from redis
		Room actual = manager.acquireRoom(expect.getRoomSessionKey(), false);
		System.out.println(actual);
		assertRoomEquals(expect, actual);
		assertEquals(expect, actual);
				
		Jedis jedis = JedisFactory.getJedis();
		
		//Check the other sets
		Set<String> localRoomSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localRoomSet.size());
		String hexString = localRoomSet.iterator().next();
		SessionKey actualKey = SessionKey.createSessionKeyFromHexString(hexString);
		assertEquals(expect.getRoomSessionKey(), actualKey);
		
		SessionKey roomSessionKey = manager.findRoomSessionKeyByUserSession(user.getSessionKey());
		assertEquals(expect.getRoomSessionKey(), roomSessionKey);
		
		Set<String> currentSet = jedis.zrange(actual.getCurrentSetName(), 0, 0);
		assertEquals(1, currentSet.size());
		SessionKey roomSessionKey2 = SessionKey.createSessionKeyFromHexString(currentSet.iterator().next());
		assertEquals(expect.getRoomSessionKey(), roomSessionKey2);
	}
	
	@Test
	public void testAcquireRemoteRoom() {
		Room remoteRoom = new Room();
		remoteRoom.setRoomSessionKey(SessionKey.createSessionKeyFromRandomString("ROOM_"));
		remoteRoom.setOwnerSessionKey(SessionKey.createSessionKeyFromRandomString());
		remoteRoom.setGameServerId("192.168.0.1:3445");
		remoteRoom.setRoomType(RoomType.SINGLE_ROOM);
		remoteRoom.setRoomStatus(RoomStatus.UNFULL);
		remoteRoom.setMaxUserCount(4);
//		remoteRoom.setCurrentUserCount(3);
		remoteRoom.setReadyUserCount(0);
		remoteRoom.setCreatedMillis(System.currentTimeMillis());
		remoteRoom.setOwnerPower(10);
		remoteRoom.setAveragePower(10);
		remoteRoom.setCurrentSetName("room_unfull_set_4");
		remoteRoom.setRemote(true);
		remoteRoom.setBattleRoomSessionKey(null);
		remoteRoom.setMapId("6");

		RoomManager manager = RoomManager.getInstance();
		assertTrue(manager.storeRoom(remoteRoom));
		
		Room actual = manager.acquireRoom(remoteRoom.getRoomSessionKey(), false);
		assertNull(actual);
		
		actual = manager.acquireRoom(remoteRoom.getRoomSessionKey(), true);
//		remoteRoom.setCurrentUserCount(0);
		assertRoomEquals(remoteRoom, actual);
		assertEquals(remoteRoom, actual);
	}
	
	@Test
	public void testFindRoomSessionKeyByUserSessionKey() {
		SessionKey roomSessionKey = manager.findRoomSessionKeyByUserSession(null);
		assertEquals(null, roomSessionKey);
		
		SessionKey sessionKey = SessionKey.createSessionKeyFromRandomString();
		roomSessionKey = manager.findRoomSessionKeyByUserSession(null);
		assertEquals(null, roomSessionKey);
	}
	
	@Test
	public void testDeleteRoomNonEmptyRoom() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		//check delete non empty room.
		boolean result = manager.deleteRoomIfEmpty(room);
		assertEquals(false, result);
	}
	
	@Test
	public void testDeleteRoomEmptyRoom() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		room.removeUser(0);
		
		//check delete non empty room.
		boolean result = manager.deleteRoomIfEmpty(room);
		assertEquals(true, result);
		
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(null, actual);
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(0, localSet.size());
		assertEquals(0, jedis.zcard(room.getCurrentSetName()).longValue());
	}
	
	@Test
	public void testDeleteRoomWithAI() {
		Jedis jedis = JedisFactory.getJedis();
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		jedis.hset(user.getSessionKey().toString(), SessionManager.H_ISAI, SessionManager.V_TRUE);
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		//check delete non empty room.
		boolean result = manager.deleteRoomIfEmpty(room);
		assertEquals(true, result);
		
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(null, actual);
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(0, localSet.size());
		assertEquals(0, jedis.zcard(room.getCurrentSetName()).longValue());
	}
	
	@Test
	public void testKickUserFromSingleUserRoom() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		manager.kickUser(room, user.getSessionKey(), true);
		manager.deleteRoomIfEmpty(room);
		
		//The single room is empty, it should be removed.
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(null, actual);
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(0, localSet.size());
		assertEquals(0, jedis.zcard(room.getCurrentSetName()).longValue());
	}
	
	@Test
	public void testLeaveSingleUserRoom() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		manager.leaveRoom(user.getSessionKey());
		manager.deleteRoomIfEmpty(room);
		
		//The single room is empty, it should be removed.
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(null, actual);
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(0, localSet.size());
		assertEquals(0, jedis.zcard(room.getCurrentSetName()).longValue());
	}
	
	@Test
	public void testKickUserFromMultiUserRoom() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
		
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		assertEquals(2, room.getCurrentUserCount());
		
		manager.kickUser(room, user1.getSessionKey(), true);
		
		//The single room is empty, it should be removed.
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(1, actual.getCurrentUserCount());
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		List<UserInfo> userList = actual.getUserInfoList();
		assertEquals(null, userList.get(0));
		assertNotNull(userList.get(1));
		assertEquals(Room.BLOCKED_USER_INFO, userList.get(2));
		assertEquals(Room.BLOCKED_USER_INFO, userList.get(3));
	}
	
	@Test
	public void testKickReadyUserFromSingleRoom() {
		User user = UserManager.getInstance().createDefaultUser();
		user.set_id(new UserId("test-001"));
		user.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user);
		
		Room room = manager.createRoom(user, RoomType.SINGLE_ROOM);
		room.getUserInfoList().get(0).setUserJoinTime(-1l);
		room.setReadyUserCount(room.getReadyUserCount()+1);
		room.setCurrentSetName("room_full_set");
		
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		manager.kickUser(room, user.getSessionKey(), true);
		manager.deleteRoomIfEmpty(room);
		
		//The single room is empty, it should be removed.
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(null, actual);
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(0, localSet.size());
		assertEquals(0, jedis.zcard(room.getCurrentSetName()).longValue());
	}
	
	@Test
	public void testKickReadyUserFromMultiUserRoom() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
		
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		manager.readyStart(user2.getSessionKey(), true);
		
		Jedis jedis = JedisFactory.getJedis();
		Set<String> localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		assertEquals(2, room.getCurrentUserCount());
		assertEquals(1, room.getReadyUserCount());
		
		manager.kickUser(room, user2.getSessionKey(), true);
		manager.storeRoom(room);
		
		//The room is empty, it should be removed.
		Room actual = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(1, actual.getCurrentUserCount());
		
		localSet = jedis.smembers(manager.getLocalRoomSetName());
		assertEquals(1, localSet.size());
		assertEquals(1, jedis.zcard(room.getCurrentSetName()).longValue());
		
		List<UserInfo> userList = actual.getUserInfoList();
		assertNotNull(userList.get(0));
		assertEquals(null, userList.get(1));
		assertEquals(Room.BLOCKED_USER_INFO, userList.get(2));
		assertEquals(Room.BLOCKED_USER_INFO, userList.get(3));
		
		assertEquals("room_unfull_set_2", actual.getCurrentSetName());
		assertEquals(0, actual.getReadyUserCount());
	}
	
	@Test
	public void testCheckRoomKickOffline() {
		User user1 = prepareUser("001");
		
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
//		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		
		//User2 has no sessionkey in redis, so it will be kicked.
		manager.checkRoom(actualRoom);
		
		assertEquals(1, actualRoom.getCurrentUserCount());
		assertEquals(RoomStatus.UNFULL, actualRoom.getRoomStatus());
		
		actualRoom.getUserInfoList().get(0).setUserJoinTime(System.currentTimeMillis()-30000);
		
		manager.checkRoom(actualRoom);
		assertEquals(RoomStatus.DELETED, actualRoom.getRoomStatus());
		
		assertEquals(0, actualRoom.getCurrentUserCount());
	}
	
	@Test
	public void testCheckRoomRoomTimeout() {
		User user1 = prepareUser("001");
				
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);

		User user2 = prepareUser("002");
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		//Make the room timeout
		room.setCreatedMillis(0);
		
		manager.storeRoom(room);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);

		assertEquals(0, actualRoom.getCreatedMillis());
		assertEquals(RoomStatus.FULL, actualRoom.getRoomStatus());
		
	}
	
	@Test
	public void testCheckRoomRoomTimeout2() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
				
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);

		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		room.setCreatedMillis(0);
		
		manager.storeRoom(room);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, actualRoom.getRoomStatus());
				
		manager.readyStart(user1.getSessionKey(), true);
		manager.readyStart(user2.getSessionKey(), true);
		
		assertEquals(2, actualRoom.getCurrentUserCount());
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
	}
	
	@Test
	public void testCheckRoomRoomTimeout3() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
		
		Room room = manager.createRoom(user1, RoomType.SINGLE_ROOM);
		room.setCreatedMillis(0);
		
		manager.storeRoom(room);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, actualRoom.getRoomStatus());
				
		manager.readyStart(user1.getSessionKey(), true);
		
		actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(1, actualRoom.getCurrentUserCount());
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
	}
	
	@Test
	public void testCheckRoomKickTimeoutUser() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
				
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		assertEquals(RoomStatus.UNFULL, room.getRoomStatus());
		
		//A new user is joining. The room is FULL.
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, actualRoom.getRoomStatus());
				
		//User2 is timeout, so it will be kicked.
		actualRoom.getUserInfoList().get(1).setUserJoinTime(0);
		manager.storeRoom(actualRoom);
		
		actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		
		assertEquals(1, actualRoom.getCurrentUserCount());
		assertEquals(RoomStatus.UNFULL, actualRoom.getRoomStatus());
		
	}
		
	@Test
	public void testCheckRoomAllUsersTimeout() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);

		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);

		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		for ( UserInfo userInfo : room.getUserInfoList() ) {
			if ( userInfo != null ) 
				userInfo.setUserJoinTime(0);
		}
		
		//Since all users are timeout, all users will be removed.
		manager.storeRoom(room);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertNull(actualRoom);
	}
	
	@Test
	public void testReadyStart() {
		Jedis jedis = JedisFactory.getJedis();
		
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), userLeft);
		
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		
		String readyStartStr = jedis.hget(roomLeft.getRoomSessionKey().toString(), "ready_date");
		assertEquals("0", readyStartStr);
		
		manager.readyStart(userLeft.getSessionKey(), true);
		
		readyStartStr = jedis.hget(roomLeft.getRoomSessionKey().toString(), "ready_date");
		assertTrue( readyStartStr.length() > 0 );
		
		Double score = jedis.zscore("room_ready_set_1", roomLeft.getRoomSessionKey().toString());
		assertNotNull(score);
	}
	
	@Test
	public void testMatchSingleRoom() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
				"../deploy/data");
		AbstractTest test = new AbstractTest();
		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		AbstractTest abtest = new AbstractTest();
		abtest.setUp();
		GameContext.getTestInstance().reloadContext();
		BattleDataLoader4Bitmap.loadBattleMaps();
		
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setUsername("test-001");
		userLeft.setRoleName("test-001");
		UserManager.getInstance().removeUser("test-001");
		UserManager.getInstance().saveUser(userLeft, true);
		
		SessionManager sManager = new SessionManager();
		IoSession session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		sManager.registerSession(session, userLeft);
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		assertFalse(manager.readyStart(userLeft.getSessionKey(), true));
		
		User userRight = UserManager.getInstance().createDefaultUser();
		session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).andReturn(null).anyTimes();
		replay(session);
		
		userRight.set_id(new UserId("test-002"));
		userRight.setUsername("test-002");
		userRight.setRoleName("test-002");
		UserManager.getInstance().saveUser(userRight, true);
		
		sManager.registerSession(session, userRight);
		Room roomRight = manager.createRoom(userRight, RoomType.SINGLE_ROOM);
		assertTrue(manager.readyStart(userRight.getSessionKey(), true));
		
		sManager.deregisterSession(session, userLeft);
		sManager.deregisterSession(session, userRight);
		UserManager.getInstance().removeUser(userLeft.get_id());
		UserManager.getInstance().removeUser(userRight.get_id());
		
//		assertNotNull(battleRoom.getSessionKey());
//		assertEquals(roomRight.getRoomSessionKey(), battleRoom.getRoomLeft().getRoomSessionKey());
//		assertEquals(roomLeft.getRoomSessionKey(), battleRoom.getRoomRight().getRoomSessionKey());
	}
		
	@Test
	public void testMatchSingleRoomLoop() throws Exception {
		GlobalConfig.getInstance().overrideProperty(GlobalConfig.RUNTIME_SCRIPT_DIR, "src/main/script");
		GlobalConfig.getInstance().overrideProperty(GlobalConfigKey.deploy_data_dir, 
				"../deploy/data");
		AbstractTest test = new AbstractTest();
		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		AbstractTest abtest = new AbstractTest();
		abtest.setUp();
		GameContext.getTestInstance().reloadContext();
		BattleDataLoader4Bitmap.loadBattleMaps();
		
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setUsername("test-001");
		userLeft.setRoleName("test-001");
		userLeft.setPower(0);
		userLeft.setLevel(5);
		UserManager.getInstance().removeUser("test-001");
		UserManager.getInstance().saveUser(userLeft, true);
		
		SessionManager sManager = new SessionManager();
		IoSession session = createNiceMock(IoSession.class);
		replay(session);
		
		sManager.registerSession(session, userLeft);
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		assertFalse(manager.readyStart(userLeft.getSessionKey(), true));
		
		User userRight = UserManager.getInstance().createDefaultUser();
		session = createNiceMock(IoSession.class);
		replay(session);
		
		userRight.set_id(new UserId("test-002"));
		userRight.setUsername("test-002");
		userRight.setRoleName("test-002");
		userRight.setPower(10000);
		userRight.setLevel(20);
		UserManager.getInstance().saveUser(userRight, true);
		
		sManager.registerSession(session, userRight);
		Room roomRight = manager.createRoom(userRight, RoomType.SINGLE_ROOM);
		assertFalse(manager.readyStart(userRight.getSessionKey(), true));
		
		sManager.deregisterSession(session, userLeft);
		sManager.deregisterSession(session, userRight);
		UserManager.getInstance().removeUser(userLeft.get_id());
		UserManager.getInstance().removeUser(userRight.get_id());
		
//		assertNotNull(battleRoom.getSessionKey());
//		assertEquals(roomRight.getRoomSessionKey(), battleRoom.getRoomLeft().getRoomSessionKey());
//		assertEquals(roomLeft.getRoomSessionKey(), battleRoom.getRoomRight().getRoomSessionKey());
	}
	
	@Test
	public void testRoomChecker() throws Exception {
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), userLeft);
		
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_READY_TIMEOUT, 2000);
		RedisRoomManager.Checker checker = new RedisRoomManager.Checker();
		checker.run();
		
		Room room = manager.acquireRoom(roomLeft.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, room.getRoomStatus());
		
		//Wait until timeout
		Thread.currentThread().sleep(3000);
		
		room = manager.acquireRoom(roomLeft.getRoomSessionKey(), false);
	}
	
	@Test
	public void testRoomCheckerUserOffline() throws Exception {
		RoomManager manager = RoomManager.getInstance();
		
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), userLeft);
		
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_READY_TIMEOUT, 2000);
		RedisRoomManager.Checker checker = new RedisRoomManager.Checker();
		checker.run();
		Room room = manager.acquireRoom(roomLeft.getRoomSessionKey(), false);
		assertEquals(RoomStatus.FULL, room.getRoomStatus());
		
		//Delete user's sessionkey from Redis
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(userLeft.getSessionKey().toString());
		
		Thread.currentThread().sleep(2000);
		
		room = manager.acquireRoom(roomLeft.getRoomSessionKey(), false);
		assertEquals(null, room);
	}
	
	@Test
	public void testResetRoom() {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);

		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		manager.readyStart(user1.getSessionKey(), true);
		manager.readyStart(user2.getSessionKey(), true);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
		//Test reset room
		assertEquals(2, actualRoom.getReadyUserCount());
		long currentTime = System.currentTimeMillis()-1000;
		
		manager.getInstance().resetRoom(actualRoom);
		
		actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(0, actualRoom.getReadyUserCount());
		assertEquals(null, actualRoom.getBattleRoomSessionKey());
		assertEquals(RoomStatus.FULL, actualRoom.getRoomStatus());
		assertEquals(2, actualRoom.getCurrentUserCount());
		assertEquals("room_full_set", actualRoom.getCurrentSetName());
		List<UserInfo> userList = room.getUserInfoList();
		for ( UserInfo userInfo : userList ) {
			if ( userInfo != null && userInfo != Room.BLOCKED_USER_INFO ) {
				assertTrue(userInfo.getUserJoinTime()>currentTime);
			}
		}
	}
	
	@Test
	public void testResetRoomWithSingleRoomAIUser() {
		User user1 = prepareUser("001");

		//user2 is an AI user.
		User user2 = prepareUser("002", true);
		
		Room room1 = manager.createRoom(user1, RoomType.SINGLE_ROOM);
		Room room2 = manager.createRoom(user2, RoomType.SINGLE_ROOM);
		
//		manager.readyStart(user1.getSessionKey(), true);
		manager.readyStart(user2.getSessionKey(), true);
		
		Room actualRoom = manager.acquireRoom(room2.getRoomSessionKey(), false);
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
		//Test reset room
		assertEquals(1, actualRoom.getReadyUserCount());
		long currentTime = System.currentTimeMillis()-1000;
		
		manager.getInstance().resetRoom(actualRoom);
		
		actualRoom = manager.acquireRoom(room2.getRoomSessionKey(), false);
		assertEquals(0, actualRoom.getReadyUserCount());
		assertEquals(null, actualRoom.getBattleRoomSessionKey());
		assertEquals(RoomStatus.DELETED, actualRoom.getRoomStatus());
		assertEquals(0, actualRoom.getCurrentUserCount());
		assertEquals("room_unfull_set_1", actualRoom.getCurrentSetName());
		assertEquals(0, actualRoom.getCurrentUserCount());
	}

	@Test
	public void testResetRoomWithAIUser() {
		User user1 = prepareUser("001");

		//user2 is an AI user.
		User user2 = prepareUser("002", true);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		manager.readyStart(user1.getSessionKey(), true);
		manager.readyStart(user2.getSessionKey(), true);
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
		//Test reset room
		assertEquals(2, actualRoom.getReadyUserCount());
		long currentTime = System.currentTimeMillis()-1000;
		
		manager.getInstance().resetRoom(actualRoom);
		
		actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(0, actualRoom.getReadyUserCount());
		assertEquals(null, actualRoom.getBattleRoomSessionKey());
		assertEquals(RoomStatus.UNFULL, actualRoom.getRoomStatus());
		assertEquals(1, actualRoom.getCurrentUserCount());
		assertEquals("room_unfull_set_2", actualRoom.getCurrentSetName());
		List<UserInfo> userList = room.getUserInfoList();
		for ( UserInfo userInfo : userList ) {
			if ( userInfo != null && userInfo != Room.BLOCKED_USER_INFO ) {
				assertTrue(userInfo.getUserJoinTime()>currentTime);
			}
		}
		
	}
	
	@Test
	public void testChangeMap() {
		Jedis jedis = JedisFactory.getJedis();
		
		User userLeft = UserManager.getInstance().createDefaultUser();
		userLeft.set_id(new UserId("test-001"));
		userLeft.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), userLeft);
		
		Room roomLeft = manager.createRoom(userLeft, RoomType.SINGLE_ROOM);
		
		boolean success = manager.changeMap(userLeft.getSessionKey(), "06");
		assertEquals(true, success);
		
		Room actual = manager.acquireRoom(roomLeft.getRoomSessionKey(), false);
		assertEquals("06", actual.getMapId());
	}
	
	@Test
	public void testGetRoomNumberUser() {
		User user1 = prepareUser("001");
		
		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setRoleName("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		
		int[] users = manager.getRoomUserNumber(room.getRoomType());
		System.out.println("current user: " + users[0]);
		System.out.println("max user: " + users[1]);
	}
	
	@Test
	public void testRoomStatusChange() {
		Jedis jedis = JedisFactory.getJedis();
		
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);

		User user2 = UserManager.getInstance().createDefaultUser();
		user2.set_id(new UserId("test-002"));
		user2.setUsername("test-002");
		user2.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		UserManager uManager = GameContext.getInstance().getUserManager();
		uManager.removeUser("test-002");
		uManager.saveUser(user2, true);
		sManager.registerSession(createIoSession(), user2);
		
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		//The current set should be unfull set
		assertEquals(RedisRoomManager.ZSET_UNFULL_NAME[1], room.getCurrentSetName());
		Long actualRank = jedis.zrank(room.getCurrentSetName(), room.getRoomSessionKey().toString());
		assertEquals(0, actualRank.intValue());
		
		manager.joinRoom(room.getRoomSessionKey(), user2.getSessionKey());
		//The current set should be full set
		assertEquals(RedisRoomManager.ZSET_FULL_NAME, room.getCurrentSetName());
		actualRank = jedis.zrank(room.getCurrentSetName(), room.getRoomSessionKey().toString());
		assertEquals(0, actualRank.intValue());
		
		manager.readyStart(user1.getSessionKey(), true);
		//The current set should be full set
		assertEquals(RedisRoomManager.ZSET_FULL_NAME, room.getCurrentSetName());
		actualRank = jedis.zrank(room.getCurrentSetName(), room.getRoomSessionKey().toString());
		assertEquals(0, actualRank.intValue());
		
		manager.readyStart(user2.getSessionKey(), true);
		
		//The current set should be ready set
		assertEquals(RedisRoomManager.ZSET_READY_NAME[1], room.getCurrentSetName());
		actualRank = jedis.zrank(room.getCurrentSetName(), room.getRoomSessionKey().toString());
		assertEquals(0, actualRank.intValue());
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), false);
		assertEquals(RoomStatus.READY, actualRoom.getRoomStatus());
		
		//Test reset room
		assertEquals(2, actualRoom.getReadyUserCount());
		long currentTime = System.currentTimeMillis()-1000;
		
		manager.getInstance().resetRoom(actualRoom);
		//The current set should be full set
		assertEquals(RedisRoomManager.ZSET_FULL_NAME, room.getCurrentSetName());
		actualRank = jedis.zrank(room.getCurrentSetName(), room.getRoomSessionKey().toString());
		assertEquals(0, actualRank.intValue());
		
		//Make two users offline
		jedis.del(user1.getSessionKey().toString());
		jedis.del(user2.getSessionKey().toString());
		
		manager.deleteRoomIfEmpty(room);
		manager.resetRoom(room);
		//The room should not exist
		assertFalse( jedis.exists(room.getRoomSessionKey().toString()) );
		String roomKey = room.getRoomSessionKey().toString();
		for ( String zsetName : RedisRoomManager.ZSET_UNFULL_NAME ) {
			assertEquals(null, jedis.zrank(zsetName, roomKey));
		}
		for ( String zsetName : RedisRoomManager.ZSET_READY_NAME ) {
			assertEquals(null, jedis.zrank(zsetName, roomKey));					
		}
		assertEquals(null, jedis.zrank(RedisRoomManager.ZSET_FULL_NAME, roomKey));
	}
	
	@Test
	public void testEditSeat() throws Exception {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);

		RoomManager manager = RoomManager.getInstance();
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		manager.editSeat(user1.getSessionKey(), 2, true);
		
		List<UserInfo> list = room.getUserInfoList();
		assertEquals(user1.getSessionKey(), list.get(0).getUserSessionKey());
		assertEquals(null, list.get(1));
		assertEquals(null, list.get(2));
		assertEquals(Room.BLOCKED_USER_INFO, list.get(3));
		assertEquals(3, room.getMaxUserCount());
	}
	
	@Test
	public void testEditSeatSaveAndRestore() throws Exception {
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_JOIN_TIMEOUT, 1500000);
		
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.set_id(new UserId("test-001"));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);

		RoomManager manager = RoomManager.getInstance();
		Room room = manager.createRoom(user1, RoomType.MULTI_ROOM);
		manager.editSeat(user1.getSessionKey(), 2, true);
		manager.storeRoom(room);
		manager.removeLocalRoom(room.getRoomSessionKey());
		
		Room actualRoom = manager.acquireRoom(room.getRoomSessionKey(), true);
		List<UserInfo> list = actualRoom.getUserInfoList();
		assertEquals(user1.getSessionKey(), list.get(0).getUserSessionKey());
		assertEquals(null, list.get(1));
		assertEquals(null, list.get(2));
		assertEquals(Room.BLOCKED_USER_INFO, list.get(3));
		assertEquals(3, room.getMaxUserCount());
	}
	
	@Test
	public void testEediSeatInReadyRoom() throws Exception {
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_JOIN_TIMEOUT, 1500000);
		RoomManager manager = RoomManager.getInstance();
		
		//Make a 2 users room
		User user1 = prepareUser("001");
		Room room1 = manager.createRoom(user1, RoomType.MULTI_ROOM);
		assertEquals(2, room1.getMaxUserCount());
		
		//Make a 3-users room
		User user2 = prepareUser("002");
		manager.joinRoom(room1.getRoomSessionKey(), user2.getSessionKey());
		assertEquals(2, room1.getMaxUserCount());
		assertEquals(2, room1.getCurrentUserCount());
		assertEquals(0, room1.getReadyUserCount());
		assertEquals(RoomStatus.FULL, room1.getRoomStatus());

		manager.readyStart(user1.getSessionKey(), true);
		manager.readyStart(user2.getSessionKey(), true);
		assertEquals(2, room1.getMaxUserCount());
		assertEquals(2, room1.getCurrentUserCount());
		assertEquals(2, room1.getReadyUserCount());
		assertEquals(RoomStatus.READY, room1.getRoomStatus());
		
		manager.editSeat(user1.getSessionKey(), 2, true);
		assertEquals(3, room1.getMaxUserCount());
		assertEquals(2, room1.getCurrentUserCount());
		assertEquals(2, room1.getReadyUserCount());
		assertEquals(RoomStatus.UNFULL, room1.getRoomStatus());
		
		User user3 = prepareUser("003");
		manager.joinRoom(room1.getRoomSessionKey(), user3.getSessionKey());
		assertEquals(3, room1.getMaxUserCount());
		assertEquals(3, room1.getCurrentUserCount());
		assertEquals(2, room1.getReadyUserCount());
		assertEquals(RoomStatus.FULL, room1.getRoomStatus());		
		
		manager.readyStart(user3.getSessionKey(), true);
		assertEquals(3, room1.getMaxUserCount());
		assertEquals(3, room1.getCurrentUserCount());
		assertEquals(3, room1.getReadyUserCount());
		assertEquals(RoomStatus.READY, room1.getRoomStatus());
	}
	
	@Test
	public void testPopUnfullMultiUserRoom() throws Exception {
		GameDataManager.getInstance().overrideRuntimeValue(GameDataKey.ROOM_JOIN_TIMEOUT, 1500000);
		RoomManager manager = RoomManager.getInstance();
		
		//Make a 2 users room
		User user1 = prepareUser("001");
		Room room1 = manager.createRoom(user1, RoomType.MULTI_ROOM);
		assertEquals(2, room1.getMaxUserCount());
		
		//Make a 3-users room
		User user2 = prepareUser("002");
		Room room2 = manager.createRoom(user2, RoomType.MULTI_ROOM);
		manager.editSeat(user2.getSessionKey(), 2, true);
		assertEquals(3, room2.getMaxUserCount());
		
		User user3 = prepareUser("003");
		Room room = manager.popUnfullMultiUserRoom(user3);
		assertEquals(room2.getRoomSessionKey(), room.getRoomSessionKey());
		assertEquals(2, room.getCurrentUserCount());
	}
	
	private User prepareUser(String userName) {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.setUsername(userName);
		user1.setRoleName(userName);
		user1.set_id(new UserId(userName));
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user1, true);
		return user1;
	}
	
	private User prepareUser(String userName, boolean isAI) {
		User user1 = UserManager.getInstance().createDefaultUser();
		user1.setUsername(userName);
		user1.set_id(new UserId(userName));
		user1.setAI(isAI);
		user1.setSessionKey(SessionKey.createSessionKeyFromRandomString());
		sManager.registerSession(createIoSession(), user1);
		UserManager.getInstance().removeUser(userName);
		UserManager.getInstance().saveUser(user1, true);
		return user1;
	}
	
	private void assertRoomEquals(Room expect, Room actual) {
		//Identity is meaningless.
//		assertTrue(expect != actual);
		assertEquals(expect.getRoomSessionKey(), actual.getRoomSessionKey());
		assertEquals(expect.getGameServerId(), actual.getGameServerId()); 
		assertEquals(expect.getRoomType(), actual.getRoomType());
		assertEquals(expect.getRoomStatus(), actual.getRoomStatus());
		assertEquals(expect.getMaxUserCount(), actual.getMaxUserCount());
		assertEquals(expect.getCurrentUserCount(), actual.getCurrentUserCount());
		assertEquals(expect.getReadyUserCount(), actual.getReadyUserCount());
		assertEquals(expect.getCreatedMillis(), actual.getCreatedMillis() );
		assertEquals(expect.getOwnerPower(), actual.getOwnerPower());
		assertEquals(expect.getAveragePower(), actual.getAveragePower());
		assertEquals(expect.getCurrentSetName(), actual.getCurrentSetName());
		assertEquals(expect.getBattleRoomSessionKey(), actual.getBattleRoomSessionKey());
		assertEquals(expect.getOwnerSessionKey(), actual.getOwnerSessionKey());
		assertEquals(expect.isRemote(), actual.isRemote());
		List<UserInfo> expectUserInfos = expect.getUserInfoList();
		List<UserInfo> actualUserInfos = actual.getUserInfoList();
		assertTrue( expectUserInfos == null?actualUserInfos == null:true );
		assertEquals(expectUserInfos.size(), actualUserInfos.size());
		for ( int i=0; i<expectUserInfos.size(); i++ ) {
			assertEquals(expectUserInfos.get(i), actualUserInfos.get(i));
		}
	}
	
	/**
	 * Create a fake IoSession.
	 * @return
	 */
	private IoSession createIoSession() {
		//Note. It is a temporary solution. The Redis should be cleaned
		final HashMap<Object, Object> attrMap = new HashMap<Object, Object>();
		IoSession session = createNiceMock(IoSession.class);
		expect(session.setAttribute(anyObject(), anyObject())).
			andAnswer(new IAnswer<Object>() {
					@Override
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						Object value = getCurrentArguments()[1];
						attrMap.put(key, value);
						return value;
					}
				}).anyTimes();
		expect(session.getAttribute(anyObject())).
			andAnswer(new IAnswer<Object>() {
					public Object answer() throws Throwable {
						Object key = getCurrentArguments()[0];
						return attrMap.get(key);
					}
				}).anyTimes();
		replay(session);
		return session;
	}
}
