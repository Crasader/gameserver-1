package com.xinqihd.sns.gameserver.db.mongo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BattleRoom;
import com.xinqihd.sns.gameserver.battle.RedisRoomManager;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomStatus;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.boss.Boss;
import com.xinqihd.sns.gameserver.boss.BossManager;
import com.xinqihd.sns.gameserver.boss.BossStatus;
import com.xinqihd.sns.gameserver.chat.ChatManager;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmCallback;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager.ConfirmResult;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.PropDataEquipIndex;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceEnterRoom.BceEnterRoom;
import com.xinqihd.sns.gameserver.proto.XinqiBceInvite.BceInvite;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used to manage the game's equipment objects
 * 
 * @author wangqi
 *
 */
public class InviteManager{
	
	public static final String CHALLENGE_KEY = "invite.vs:";
	
	private static final Logger logger = LoggerFactory.getLogger(InviteManager.class);
	
	private static InviteManager instance = new InviteManager();
	
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static InviteManager getInstance() {
		return instance;
	}
	
	InviteManager() {

	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
	}
	
	/**
	 * Challenge the friend to start a one VS. one game.
	 * 
	 * @param user
	 * @param invite
	 */
	public boolean challengeFriend(final User user, final BceInvite invite) {
		String userIdStr = invite.getUid();
		
		/**
		 * Local variable declaration
		 */
		final UserId friendUserId = UserId.fromString(userIdStr);
		String redisKey = StringUtil.concat(CHALLENGE_KEY, user.getUsername());
		String friendIdStr = friendUserId.toString();
		final int pos = invite.getPos();
		Room userRoom = null;
		Room friendRoom = null;
		
		if ( friendUserId != null ) {
			if ( user.get_id().equals(friendUserId) ) {
				//The user cannot challenge himself
				logger.info("#challengeFriend: cannot challenge self userid: {}", friendUserId);
				SysMessageManager.getInstance().sendClientInfoMessage(user, "friend.self", Type.NORMAL);
				return false;
			}
		} else {
			logger.info("#challengeFriend: cannot find friend userid: {}", userIdStr);
			return false;
		}

		/**
		 * Check the invitation cool down time.
		 */
		Jedis jedis = JedisFactory.getJedis();
		String lastTimestamp = jedis.hget(redisKey, friendIdStr);
		if ( lastTimestamp == null ) {
			int coolDownMillis = GameDataManager.getInstance().getGameDataAsInt(
					GameDataKey.CHALLENGE_USER_COOLDOWN, 15000);
			lastTimestamp = String.valueOf(System.currentTimeMillis()+coolDownMillis);
			jedis.hset(redisKey, friendIdStr, lastTimestamp);
			jedis.expire(redisKey, 25);
		} else {
			long lastTimeMillis = Long.parseLong(lastTimestamp);
			if ( System.currentTimeMillis() <= lastTimeMillis ) {
				SysMessageManager.getInstance().sendClientInfoMessage(
						user, "challenge.freq", Type.NORMAL);
				jedis.expire(redisKey, 25);
				return false;
			} else {
				//update the new time
				lastTimestamp = String.valueOf(System.currentTimeMillis());
				jedis.hset(redisKey, friendIdStr, lastTimestamp);
				jedis.expire(redisKey, 25);
			}
		}


		boolean sendInvite = true;
		User friendUser = null;
		/**
		 * 下面要判断请求的类型
		 * 1. 同机对战
		 * 2. 离线挑战
		 * 3. 在线挑战
		 * 
		 * 首先要获取房间的类型，如果房间类型为同机，那么一定为同机对战
		 */
		final SessionKey existRoomSessionKey = RoomManager.getInstance().findRoomSessionKeyByUserSession(
				user.getSessionKey());
		if ( existRoomSessionKey != null ) {
			userRoom = RoomManager.getInstance().acquireRoom(existRoomSessionKey, false);
		}
		if ( userRoom == null ) {
			userRoom = RoomManager.getInstance().createRoom(user, RoomType.FRIEND_ROOM);
		}
		/**
		 * 玩家在房间中的基础位置
		 */
		int roomBasePos = 0;
		boolean isGuildInvite = false;
		if ( userRoom != null ) {
			final Room room = userRoom;
			BattleRoom battleRoom = room.getBattleRoom();
			if ( battleRoom != null ) {
				if ( battleRoom.getRoomRight() == room ) {
					roomBasePos = 4;
				}
			}
			if ( room.getRoomType() == RoomType.DESK_ROOM ) {
				friendUser = UserManager.getInstance().queryUser(friendUserId);
				logger.info("Desk room challenge from {} to friend {}", user.getRoleName(), friendUser.getRoleName());
				UserManager.getInstance().queryUserBag(friendUser);
				UserManager.getInstance().queryUserUnlock(friendUser);
				friendUser.setProxy(true);
				friendUser.setProxySessionKey(user.getSessionKey());

				//Remove the proxy user from existing room if exist.
				SessionKey proxySessionKey = GameContext.getInstance().getSessionManager().findSessionKeyByProxyUserId(friendUserId);
				if ( proxySessionKey != null ) {
					SessionKey proxyRoomSessionKey =  RoomManager.getInstance().findRoomSessionKeyByUserSession(proxySessionKey);
					if ( proxyRoomSessionKey != null ) {
						Room alreadyRoom = RoomManager.getInstance().acquireRoom(proxyRoomSessionKey, false);
						RoomManager.getInstance().kickUser(alreadyRoom, proxySessionKey, true);
					}
				}
				GameContext.getInstance().registerUserSession(user.getSession(), friendUser, proxySessionKey);
				if ( pos >= 0 && pos < Room.MAX_USER ) {
					RoomManager.getInstance().joinRoom(room.getRoomSessionKey(), friendUser.getSessionKey(), pos);
					RoomManager.getInstance().storeRoom(room);
				} else {
					RoomManager manager = RoomManager.getInstance();
					Room otherRoom = null;
					if ( battleRoom != null ) {
						otherRoom = battleRoom.getRoomRight();
						if ( room == otherRoom ) {
							otherRoom = battleRoom.getRoomLeft();
						}
						if ( manager.acquireRoom(otherRoom.getRoomSessionKey(), false) != null ) {
							manager.joinRoom(otherRoom.getRoomSessionKey(), friendUser.getSessionKey(), pos);
							manager.storeRoom(otherRoom);
						} else {
							//Create a new battleRoom
							otherRoom = manager.createRoom(friendUser, RoomType.DESK_ROOM, 4);
						}							
					} else {
						//Create a new battleRoom
						otherRoom = manager.createRoom(friendUser, RoomType.DESK_ROOM, 4);
					}
					battleRoom = manager.createBattleRoomWithoutBegin(room, otherRoom);
					if ( battleRoom.getRoomLeft() == room ) {
						battleRoom.setRoomRigth(otherRoom);
					} else {
						battleRoom.setRoomLeft(otherRoom);
					}
					room.setRoomStatus(RoomStatus.UNFULL);
					otherRoom.setRoomStatus(RoomStatus.UNFULL);
					manager.storeRoom(room);
					manager.storeRoom(otherRoom);
				}
				
				StatClient.getIntance().sendDataToStatServer(user, StatAction.Invite, RoomType.DESK_ROOM, friendUser.getRoleName(), pos);
				/**
				 * 离线挑战不需要玩家确认，所以直接返回结果
				 */
				return true;
			} else if ( room.getRoomType() == RoomType.PVE_ROOM ) {
				//Check user's level
				//logger.info("PVE room challenge from {} to friend {}", user.getRoleName(), friendUser.getRoleName());
				final SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friendUserId);
				if ( friendSessionKey != null ) {
					friendUser = GameContext.getInstance().findGlobalUserBySessionKey(friendSessionKey);
				}
				Boss boss = (Boss)user.getUserData(BossManager.USER_BOSS_ID);
				boolean conditionMeet = false;
				if ( boss != null && friendUser != null ) {
					/**
					 * Enable the SINGLE PVE team combat
					 * 2013-02-25
					 */
					/*
					BossPojo bossPojo = boss.getBossPojo();
					if ( bossPojo.getBossType() == BossType.SINGLE ) {
						//单人副本暂时屏蔽邀请好友功能
						SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.single.invite.friend", Type.NORMAL);
						return false;
					} else {
						conditionMeet = BossManager.getInstance().checkBossStatus(friendUser, boss);
						if ( conditionMeet ) {
							conditionMeet = BossManager.getInstance().checkBossRequirement(friendUser, boss.getBossPojo());
							if ( conditionMeet ) {
								int count = BossManager.getInstance().getChallengeCount(friendUser, boss, System.currentTimeMillis());
								if (count > 0) {
									conditionMeet = true;
									sendInvite = true;
								} else {
									SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.challenge.runout", Type.NORMAL);
									conditionMeet = false;
								}
							}
						}
					}
					*/
					Boss friendBoss = BossManager.getInstance().getBossInstance(friendUser, boss.getId());
					if ( friendBoss == null ) {
						friendBoss = boss;
					}
					if ( friendUser.getLevel() >= user.getLevel() + 20 ) {
						String message = Text.text("boss.invite.leveldiff");
						SysMessageManager.getInstance().sendClientInfoMessage(user, message, Type.NORMAL);
						return false;
					} else if ( friendBoss.getBossStatusType() == BossStatus.SUCCESS ) {
						String message = Text.text("boss.invite.unsyncstatus");
						SysMessageManager.getInstance().sendClientInfoMessage(user, message, Type.NORMAL);
						return false;
					} else if ( friendBoss.getProgress() != boss.getProgress() ) {
						String message = Text.text("boss.invite.unsyncprogress", friendBoss.getProgress());
						SysMessageManager.getInstance().sendClientInfoMessage(user, message, Type.NORMAL);
						return false;
					} else {
						conditionMeet = BossManager.getInstance().checkBossStatus(friendUser, boss);
						if ( conditionMeet ) {
							conditionMeet = BossManager.getInstance().checkBossRequirement(friendUser, boss.getBossPojo());
							if ( conditionMeet ) {
								int count = BossManager.getInstance().getChallengeCount(friendUser, boss, System.currentTimeMillis());
								if (count > 0) {
									conditionMeet = true;
									sendInvite = true;
								} else {
									SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.challenge.runout", Type.NORMAL);
									conditionMeet = false;
								}
							}
						}
					}
				} else {
					conditionMeet = false;
				}
				if ( !conditionMeet ) {
					SysMessageManager.getInstance().sendClientInfoMessage(user, "boss.invite.friend", Type.NORMAL);
					return false;
				}
			} else if ( room.getRoomType() == RoomType.FRIEND_ROOM || room.getRoomType() == RoomType.GUILD_ROOM ) {
				/**
				 * 如果为好友对战类型，那么判断好友是否在线，如果在线则为合作或者挑战，否则为离线挑战
				 */
				if ( room.getRoomType() == RoomType.GUILD_ROOM ) {
					isGuildInvite = true;
				}
				final SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friendUserId);
				if ( friendSessionKey == null ) {
					if ( room.getRoomType() == RoomType.FRIEND_ROOM ) {
						String message = Text.text("friend.offline.confirm");

						ConfirmManager.getInstance().sendConfirmMessage(user, message, "offline.challenge", new ConfirmCallback() {

							@Override
							public void callback(User user, int selected) {
								if ( selected == ConfirmResult.YES.ordinal() ) {
									startOfflineChallenge(user, friendUserId);
								} else {
									RedisRoomManager.getInstance().kickUser(room, user.getSessionKey(), true);
								}
							}
						});
						return true;
					} else {
						/**
						 * 禁止公会对战中出现离线挑战
						 * 2013-1-30
						 */
						return false;
					}
				} else {
					//Friend is online because his sessionKey is not null
					friendUser = GameContext.getInstance().findGlobalUserBySessionKey(friendSessionKey);
					/**
					 * Check if the friend user is already in a battle
					 * When player is already in battle
					 * Should be supported in future release
					 * 
					 * wangqi 2012-08-21
					 */
					final RoomManager roomManager = RoomManager.getInstance();
					final SessionKey friendExistRoomSessionKey = roomManager.findRoomSessionKeyByUserSession(friendSessionKey);
					if ( friendExistRoomSessionKey != null ) {
						friendRoom = roomManager.acquireRoom(friendExistRoomSessionKey, true);
						if ( friendRoom != null ) {
							/**
							 * 受到邀请的好友已经加入了一个房间，这时需要判断
							 * 1. 加入的房间就是这次对战的好友房间
							 * 2. 加入的房间就是这次对战的对手房间
							 * 3. 加入的房间是其他对战房间
							 */
							if ( userRoom != null && userRoom.getBattleRoom() == friendRoom.getBattleRoom() ) {
								/**
								 * 受到邀请的玩家和发起邀请的玩家已经在同一个战斗房间中了, 需要先踢出再邀请
								 */
								SysMessageManager.getInstance().sendClientInfoMessage(user, "friend.kickfirst", Type.NORMAL);
								sendInvite = false;
							} else {
								if ( friendRoom.getRoomStatus() == RoomStatus.COMBAT || 
										friendRoom.getRoomStatus() == RoomStatus.READY ) {
									
									if ( friendUser != null ) {
										String chatMessage = Text.text("friend.chall.note", user.getRoleName());
										ChatManager.getInstance().sendSysChat(friendUser, chatMessage);
										String friendRoleName = friendUser.getRoleName();
										String message = Text.text("friend.incombat", friendRoleName);
										SysMessageManager.getInstance().sendClientInfoRawMessage(user, message, Action.NOOP, Type.NORMAL);
									} else {
										logger.debug("Friend {} is offline");
										String message = Text.text("friend.offline");
										SysMessageManager.getInstance().sendClientInfoRawMessage(user, message, Action.NOOP, Type.NORMAL);								
									}
									
									sendInvite = false;
								} else {
									sendInvite = true;
								}
							}
						}
					}
				}
			} else if ( room.getRoomType() == RoomType.OFFLINE_ROOM ) {
				friendUser = startOfflineChallenge(user, friendUserId);
			} else {
				//OTHER roomtypes are not supported for friend challenge.
				String friendRoleName = Constant.EMPTY;
				if ( friendUser != null ) {
					friendRoleName = friendUser.getRoleName();
				}
				logger.info("The user:{} invite friend:{} into unsupported room type: {}", 
						new Object[]{user.getRoleName(), friendRoleName, room.getRoomType()});
				sendInvite = false;
			}
		} else {
			//ROOM is null
			logger.info("There is no existing room. user:{}, friend:{}", 
					user.getRoleName(), friendUser.getRoleName());

			//There is no existing room (room sessionKey is null)
			sendInvite = true;
		}
			
		/**
		 * 根据上面的判断结果，如果需要发送确认消息，则发送给好友
		 */
		if ( sendInvite ) {
			logger.debug("Send invite from user:{} to friend:{}", user.getRoleName(), friendUser.getRoleName());
			
			ConfirmManager manager = ConfirmManager.getInstance();
			String inviteMessage = null;
			if ( (roomBasePos < Room.MAX_USER && pos >= Room.MAX_USER) ||
					(roomBasePos >= Room.MAX_USER && pos < Room.MAX_USER) ) {
				/**
				 * 只有好友对战才能邀请对手玩家
				 */
				if ( !isGuildInvite ) {
					inviteMessage = Text.text("friend.challenge", user.getRoleName());
				} else {
					inviteMessage = Text.text("guild.no.challenge");
					SysMessageManager.getInstance().sendClientInfoRawMessage(user.getSessionKey(), inviteMessage, 3000);
					return false;
				}
			} else {
				inviteMessage = Text.text("friend.cooperate", user.getRoleName());
			}
			
			final RoomManager roomManager = RoomManager.getInstance();
			final SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friendUserId);
			final SessionKey friendExistRoomSessionKey = roomManager.findRoomSessionKeyByUserSession(friendSessionKey);
			manager.sendConfirmMessage(user, friendSessionKey, inviteMessage, CHALLENGE_KEY, 
					new InviteConfirmCallback(user, friendSessionKey, friendExistRoomSessionKey, 
							existRoomSessionKey, pos, roomBasePos, friendUserId));
			
			String friendRoleName = Constant.EMPTY;
			if ( friendUser != null ) {
				friendRoleName = friendUser.getRoleName();
			}
			if ( isGuildInvite ) {
				StatClient.getIntance().sendDataToStatServer(user, StatAction.Invite, RoomType.GUILD_ROOM, friendRoleName, pos);
			} else {
				StatClient.getIntance().sendDataToStatServer(user, StatAction.Invite, RoomType.FRIEND_ROOM, friendRoleName, pos);
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * @param user
	 * @param friendUserId
	 * @return
	 */
	private User startOfflineChallenge(final User user, final UserId friendUserId) {
		boolean sendInvite;
		User friendUser;
		Room room;
		/**
		 * 好友不在线，则为离线挑战
		 */
		sendInvite = false;
		friendUser = UserManager.getInstance().queryUser(friendUserId);

		User aiUser = null;
		if ( friendUser != null ) {
			logger.info("Offline challenge user {} with friend {}", user.getRoleName(), friendUser.getRoleName());
			
			UserManager.getInstance().queryUserBag(friendUser);
			friendUser.setAI(true);
			aiUser = AIManager.getInstance().registerAIUser(friendUser);
		} else {
			aiUser = AIManager.getInstance().createAIUser(user);
			aiUser.setRoleName(friendUser.getRoleName());
			aiUser.setUsername(friendUser.getUsername());
		}
		PropData propData = aiUser.getBag().getWearPropDatas().get(PropDataEquipIndex.WEAPON.index());
		if ( propData != null ) {
			/**
			 * The RoomType.FRIEND_ROOM should be matched with a RoomType.MULTI_ROOM
			 * if the opponent room is not-assigned.
			 */
			Room aiRoom = RoomManager.getInstance().createRoom(aiUser, RoomType.OFFLINE_ROOM);
			
			room = RoomManager.getInstance().createRoom(user, RoomType.OFFLINE_ROOM);
			RoomManager.getInstance().createBattleRoom(room, aiRoom);
			
			//Send ai user readystart
			List<UserInfo> userInfos = aiRoom.getUserInfoList();
			for ( UserInfo aiUserInfo : userInfos ) {
				if ( aiUserInfo == null || aiUserInfo == Room.BLOCKED_USER_INFO ) continue;
				RoomManager.getInstance().sendUserReadyStart(room, aiUserInfo.getUserSessionKey(), 
						room.getUserInfoList(), true);
			}
		} else {
			SysMessageManager.getInstance().sendClientInfoMessage(user, "offline.chall.noweapon", Type.NORMAL);
			AIManager.getInstance().destroyAIUser(aiUser.getSessionKey());
		}
		return friendUser;
	}
	
	public static class InviteConfirmCallback implements ConfirmCallback {
		
		private User targetUser = null;
		private SessionKey friendSessionKey = null;
		private SessionKey friendExistRoomSessionKey = null;
		private SessionKey existRoomSessionKey = null;
		private int pos = 0;
		private int roomBasePos = 0;
		private UserId friendUserId = null;
		
		/**
		 * 
		 * @param user The inviter that want to start the challenge.
		 * @param friendSessionKey The invitee's session key. that is my sessionkey
		 * @param friendExistRoomSessionKey The invitee's current room session key. should make it leave first.
		 * @param existRoomSessionKey The inviter's challenge room's session key.
		 * @param pos The invitee's position
		 * @param friendUserId
		 */
		public InviteConfirmCallback(User user, SessionKey friendSessionKey, SessionKey friendExistRoomSessionKey,
				SessionKey existRoomSessionKey, int pos, int roomBasePos, UserId friendUserId) {
			this.targetUser = user;
			this.friendSessionKey = friendSessionKey;
			this.friendExistRoomSessionKey = friendExistRoomSessionKey;
			this.existRoomSessionKey = existRoomSessionKey;
			this.pos = pos;
			this.roomBasePos = roomBasePos;
			this.friendUserId = friendUserId;
		}

		/**
		 * The contextUser is not used by this method, since 
		 * the user is already set in constructor.
		 */
		@Override
		public void callback(final User contextUser, final int selected) {
			final RoomManager roomManager = RoomManager.getInstance();
			if (selected == ConfirmManager.ConfirmResult.YES.ordinal()) {
				Room targetRoom = roomManager.acquireRoom(existRoomSessionKey, true);
				BattleRoom targetBattleRoom = null;
				if ( targetRoom != null ) {
					targetBattleRoom = targetRoom.getBattleRoom();
				}
				final User friendUser = GameContext.getInstance().findGlobalUserBySessionKey(
						friendSessionKey);
				/**
				 * 如果friendExistRoomSessionKey不是空，说明受到邀请的玩家可能已经加入
				 * 一个房间了。这有两种可能性：
				 * 1. 玩家在其他战斗房间
				 * 2. 玩家已经加入了挑战的房间（重复邀请）
				 * 
				 * wangqi 2012-08-21
				 */
				if (friendExistRoomSessionKey != null) {
					final Room friendRoom = roomManager.acquireRoom(friendExistRoomSessionKey, true);
					BattleRoom battleRoom = friendRoom.getBattleRoom();
					if ( battleRoom != null && battleRoom == targetBattleRoom ) {
						//Do not leave the same room
						logger.debug("friend {} is already in battle room.", friendUser.getRoleName());
					} else {
						/**
						 * 邀请玩家进入副本会卡住
						 * 2012-11-15
						 */
						logger.debug("friend {} is in another room.", friendUser.getRoleName());
						if ( !friendExistRoomSessionKey.equals(existRoomSessionKey) ) {
							GameContext.getInstance().runSmallTask(new Runnable() {
								@Override
								public void run() {
									if (friendRoom != null) {
										if (friendRoom.getRoomStatus() != RoomStatus.COMBAT) {
											roomManager.leaveRoom(friendSessionKey);
										}
									}
								}
							});
						}
					}
				}
				/**
				 * 玩家1发起邀请，玩家2暂时没有回应；
				 * 玩家1进入战斗，玩家2确认玩家1的邀请
				 * 战斗将出现问题
				 */
				/*
				SessionKey existRoomSessionKey = RoomManager.getInstance().
						findRoomSessionKeyByUserSession(targetUser.getSessionKey());
				Room room = roomManager.acquireRoom(existRoomSessionKey, true);
				if ( room!= null && room.getRoomType()!=RoomType.FRIEND_ROOM ) {
					/**
					 * The target user is already in a room which 
					 * is not a friend room.
					 * /
					roomManager.kickUser(room, targetUser.getSessionKey());
					room = null;
					existRoomSessionKey= null;
				}
				*/
				GameContext.getInstance().scheduleTask(new Runnable() {
					@Override
					public void run() {
						if (existRoomSessionKey == null) {
							Room roomLeft = roomManager.createRoom(targetUser, RoomType.FRIEND_ROOM);
							Room roomRight = roomManager.createRoom(friendUser, RoomType.FRIEND_ROOM, 4);
							
							BattleRoom battleRoom = roomManager.createBattleRoomWithoutBegin(
									roomLeft, roomRight);
							roomLeft.setRoomStatus(RoomStatus.UNFULL);
							roomRight.setRoomStatus(RoomStatus.UNFULL);
							roomManager.storeRoom(roomLeft);
							roomManager.storeRoom(roomRight);
						} else {
							Room room = roomManager.acquireRoom(existRoomSessionKey, true);
							if (room != null) {
								if (!room.isRemote()) {
									if ( room.getRoomType() == RoomType.PVE_ROOM ) {
										Boss boss = (Boss)contextUser.getUserData(BossManager.USER_BOSS_ID);
							    	if ( boss != null ) {
							    		room.setRoomKey(boss.getBossId());
							    		friendUser.putUserData(BossManager.USER_BOSS_ID, boss);
							    	}
							    }
									Room toJoinRoom = room;
									if ( room.getBattleRoom() != null ) {
										if ( pos >= Room.MAX_USER ) {
											toJoinRoom = room.getBattleRoom().getRoomRight();
										} else {
											toJoinRoom = room.getBattleRoom().getRoomLeft();
										}
									}
									if (pos >= Room.MAX_USER) {
										setupBattleRoom(roomManager, friendUser, toJoinRoom, pos);
									} else {
										roomManager.joinRoom(toJoinRoom.getRoomSessionKey(),
												friendSessionKey, pos);
									}
								} else {
									/**
									 * The 'challengeId' is used for friendSessionKey The 'battleMode'
									 * is used for pos It is a special case to be called from
									 * InviteManager
									 */
									String gameServerId = room.getGameServerId();
									logger.debug("#challengeFriend: room {} is at remote server:{}",
											room.getRoomSessionKey().toString(), gameServerId);
									BceEnterRoom.Builder builder = BceEnterRoom.newBuilder();
									builder.setBattleMode(pos);
									builder.setChallengeId(friendSessionKey.toString());
									builder.setRoomId(room.getRoomSessionKey().toString());
									builder.setRoomType(RoomType.FRIEND_ROOM.ordinal());
									builder.setMapId(-1);
									builder.setChooseMode(-1);
									GameContext.getInstance().proxyToRemoteGameServer(
											targetUser.getSessionKey(), gameServerId, builder.build());
								}
							} else {
								logger
										.debug(
												"#challengeFriend: Cannot find the room by sessionKey {} for user {}",
												existRoomSessionKey, targetUser.getRoleName());
							}
						}
					}
				}, 1, TimeUnit.SECONDS);
			} else {
				BasicUser friendUser = UserManager.getInstance().queryBasicUser(
						friendUserId);
				String text = Text
						.text("friend.challenge.no", friendUser.getRoleName());
				SysMessageManager.getInstance().sendClientInfoRawMessage(
						targetUser.getSessionKey(), text, Action.NOOP, Type.NORMAL);
			}
		}

		/**
		 * @param roomManager
		 * @param friendUser
		 * @param room
		 * @param basePos
		 */
		private void setupBattleRoom(final RoomManager roomManager,
				final User friendUser, Room room, int pos) {
			BattleRoom battleRoom = room.getBattleRoom();
			if (battleRoom == null) {
				// Create a new battleRoom
				Room otherRoom = roomManager.createRoom(friendUser,
						room.getRoomType(), pos);
				battleRoom = roomManager.createBattleRoomWithoutBegin(room,
						otherRoom);
				if (battleRoom.getRoomLeft() == room) {
					battleRoom.setRoomRigth(otherRoom);
				} else {
					battleRoom.setRoomLeft(otherRoom);
				}
				room.setRoomStatus(RoomStatus.UNFULL);
				otherRoom.setRoomStatus(RoomStatus.UNFULL);
				roomManager.storeRoom(room);
				roomManager.storeRoom(otherRoom);
			} else {
				Room otherRoom = battleRoom.getRoomRight();
				boolean rightRoom = true;
				if ( pos < Room.MAX_USER ) {
					rightRoom = false;
					otherRoom = battleRoom.getRoomLeft();
				}
				/**
				 * Maybe the room is already delete from system
				 */
				if (roomManager.acquireRoom(otherRoom.getRoomSessionKey(),
						true) == null) {
					otherRoom = roomManager.createRoom(friendUser,
							otherRoom.getRoomType(), rightRoom ? 4 : 0,
							otherRoom.getRoomSessionKey());
					if (rightRoom) {
						battleRoom.setRoomRigth(otherRoom);
					} else {
						battleRoom.setRoomLeft(otherRoom);
					}
					roomManager.sendBseEnterRoom(battleRoom);
				} else {
					roomManager.joinRoom(otherRoom.getRoomSessionKey(),
							friendSessionKey, pos);
				}
			}
		}
	}
}
