package com.xinqihd.sns.gameserver.handler;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.battle.RoomStatus;
import com.xinqihd.sns.gameserver.battle.RoomType;
import com.xinqihd.sns.gameserver.config.GameFuncType;
import com.xinqihd.sns.gameserver.config.Unlock;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.ConfirmManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.db.mongo.UserActionManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.Relation;
import com.xinqihd.sns.gameserver.entity.user.Relation.People;
import com.xinqihd.sns.gameserver.entity.user.RelationType;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.proto.XinqiBceAddFriend.BceAddFriend;
import com.xinqihd.sns.gameserver.proto.XinqiBceUserRefresh.BceUserRefresh;
import com.xinqihd.sns.gameserver.proto.XinqiBseAddFriend.BseAddFriend;
import com.xinqihd.sns.gameserver.proto.XinqiBseFriendList.BseFriendList;
import com.xinqihd.sns.gameserver.proto.XinqiFriendInfoLite.FriendInfoLite;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.GameProxyClient;
import com.xinqihd.sns.gameserver.transport.SimpleChannelHandler;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.transport.XinqiProxyMessage;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * The BceAddFriendHandler is used for protocol AddFriend 
 * @author wangqi
 *
 */
public class BceAddFriendHandler extends SimpleChannelHandler {
	
	private Logger logger = LoggerFactory.getLogger(BceAddFriendHandler.class);
	
	private static final BceAddFriendHandler instance = new BceAddFriendHandler();
	
	private BceAddFriendHandler() {
		super();
	}

	public static BceAddFriendHandler getInstance() {
		return instance;
	}

	@Override
	public void messageProcess(IoSession session, Object message, SessionKey sessionKey)
			throws Exception {
		if ( logger.isDebugEnabled() ) {
			logger.debug("->BceAddFriend");
		}
		
		XinqiMessage request = (XinqiMessage)message;
		BceAddFriend addFriend = (BceAddFriend)request.payload;
		String friendName = addFriend.getUsername();
		boolean isBlackUser = addFriend.getBlacklist();
		boolean isDelete = addFriend.getIsdel();
		boolean isActive = addFriend.getIsactive();
		User user = GameContext.getInstance().findLocalUserBySessionKey(sessionKey);
		boolean changed = false;
		
		if ( StringUtil.checkNotEmpty(friendName) ) {			
			if ( isDelete ) {	
				BasicUser basicFriend = UserManager.getInstance().queryBasicUserByRoleName(friendName);
				if ( basicFriend != null ) {
					Collection<Relation> relations = user.getRelations();
					for ( Relation relation : relations ) {
						People p = relation.findPeopleByUserName(basicFriend.getUsername());
						if ( p != null ) {
							relation.removePeople(p);
							changed = true;
							logger.debug("Remove friend {} from user's friend list {}", friendName, relation);
							break;
						}
					}
				}
				//Send the friend list
				//sendFriendList(user);

				SysMessageManager.getInstance().sendClientInfoMessage(user, 
						"friend.delete", Action.NOOP, new Object[]{friendName});
				
				changed = true;
			} else {
				if ( isBlackUser ) {
					BasicUser basicFriend = UserManager.getInstance().queryBasicUserByRoleName(friendName);
					if ( basicFriend != null ) {
						People people = null;
						Collection<Relation> relations = user.getRelations();
						for ( Relation relation : relations ) {
							if ( relation.getType() != RelationType.BLACKLIST ) {
								People p = relation.findPeopleByUserName(basicFriend.getUsername());
								if ( p != null ) {
									people = relation.removePeople(p);
									logger.debug("Remove friend {} from user's friend list {}", friendName, relation);
								}
							}
						}
						if ( people == null ) {
							BasicUser basicUser = UserManager.getInstance().queryBasicUserByRoleName(friendName);
							if (basicUser != null) { 
								people = new People();
								people.setId(user.get_id());
								people.setMyId(basicUser.get_id());
								people.setUsername(basicUser.getUsername());
								people.setRolename(basicUser.getRoleName());
								people.setLevel(basicUser.getLevel());
							}
						}
						if ( people != null ) {
							changed = true;
							Relation blackRelation = user.getRelation(RelationType.BLACKLIST);
							if (blackRelation == null ) {
								blackRelation = new Relation();
								blackRelation.set_id(user.get_id());
								blackRelation.setParentUser(user);
								blackRelation.setType(RelationType.BLACKLIST);
								user.addRelation(blackRelation);
							}
							blackRelation.addPeople(people);
							logger.debug("Add user to '{}' blacklist.", friendName, user.getRoleName());
						} else {
							logger.info("Cannot find the black user with name: {}", friendName);
						}
					} // if ( basicFriend !=
				} else {
					//Add as a normal friend
					//UserManager.getInstance().queryUserRelation(user);
					Relation relation = user.getRelation(RelationType.FRIEND);
					if ( relation == null ) {
						relation = new Relation();
						relation.set_id(user.get_id());
						relation.setParentUser(user);
						relation.setType(RelationType.FRIEND);
						user.addRelation(relation);
					}
					Relation newRelation = new Relation();
					newRelation.set_id(user.get_id());
					newRelation.setParentUser(user);
					newRelation.setType(RelationType.FRIEND);
					
					if ( user.getRoleName().equals(friendName) ) {
						logger.info("Cannot add yourself as a friend");
						SysMessageManager.getInstance().sendClientInfoMessage(user, 
								"friend.notself", Type.NORMAL);
						return;
					}
					
					User friend = UserManager.getInstance().queryUserByRoleName(friendName);
					if (friend != null) {
						if ( relation.findPeopleByUserName(friend.getUsername()) != null ) {
							/*
							logger.info("You and {} are already friends", friendName);
							SysMessageManager.getInstance().sendClientInfoMessage(user, 
									"friend.already", 
									Action.NOOP, new Object[]{friendName});
							*/
							//Send response to client
							XinqiMessage response = new XinqiMessage();
							BseAddFriend.Builder builder = BseAddFriend.newBuilder();
							builder.setUsername(friendName);
							builder.setBlacklist(isBlackUser);
							builder.setIsdel(isDelete);
							builder.setSucc(changed);
							response.payload = builder.build();
							GameContext.getInstance().writeResponse(user.getSessionKey(), response);

							return;
						}
						
						People people = new People();
						people.setId(user.get_id());
						people.setMyId(friend.get_id());
						people.setUsername(friend.getUsername());
						people.setRolename(friend.getRoleName());
						people.setLevel(friend.getLevel());
						people.setWin(0);
						people.setLose(0);
						
						relation.addPeople(people);
						newRelation.addPeople(people);
						changed = true;
						
						logger.debug("Add {} as user {}'s normal friend.", friendName, user.getRoleName());
						
						if ( isActive ) {
							{
								//Unlock friend room
					  		Unlock unlock = new Unlock();
					  		unlock.setId(user.get_id());
					  		unlock.setFuncType(GameFuncType.Room);
					  		unlock.setFuncValue(RoomType.FRIEND_ROOM.ordinal());
					  		GameContext.getInstance().getUserManager().addUserNewUnlock(user, unlock);
							}
							{
								//Unlock desk room
					  		Unlock unlock = new Unlock();
					  		unlock.setId(user.get_id());
					  		unlock.setFuncType(GameFuncType.Room);
					  		unlock.setFuncValue(RoomType.DESK_ROOM.ordinal());
					  		GameContext.getInstance().getUserManager().addUserNewUnlock(user, unlock);
							}
							
							/**
							 * 互加好友需要对方确认
							 */
							SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friend.get_id());
							if ( friendSessionKey != null ) {
								final User localFriend = GameContext.getInstance().findLocalUserBySessionKey(friendSessionKey);
								if ( localFriend != null ) {
									boolean notify = true;
									final RoomManager roomManager = RoomManager.getInstance();
									final SessionKey friendExistRoomSessionKey = roomManager.findRoomSessionKeyByUserSession(friendSessionKey);
									if ( friendExistRoomSessionKey != null ) {
										Room friendRoom = roomManager.acquireRoom(friendExistRoomSessionKey, true);
										if ( friendRoom != null ) {
											if ( friendRoom.getRoomStatus() == RoomStatus.COMBAT ) {
												notify = false;
											}
										}
									}
									if ( notify ) {
										friend.setSessionKey(friendSessionKey);
										String mutalMessage = Text.text("friend.mutaladd", user.getRoleName());
										final User beingAddUser = user;
										ConfirmManager.getInstance().sendConfirmMessage(localFriend, friendSessionKey, mutalMessage, "friend.mutaladd", 
												new ConfirmManager.ConfirmCallback() {
											@Override
											public void callback(User user, int selected) {
												if ( selected == ConfirmManager.ConfirmResult.YES.ordinal() ) {
													try {
														processMutualAdd(beingAddUser, user);
													} catch (Exception e) {
														logger.warn("Failed to add mutal friend: {} with {}", localFriend.getRoleName(), user.getRoleName());
													}
												}
											}
										});
									}
								} else {
									//TODO need proxy to remote server.
								}
							}

							TaskManager.getInstance().processUserTasks(user, TaskHook.ADD_FRIEND);
						}
												
						SysMessageManager.getInstance().sendClientInfoMessage(user, "friend.add", Action.NOOP, new Object[]{friendName});
						
						//Update the friend list here.
						if ( changed ) {
							sendNewAddFriendList(user, newRelation);
						}
					}
				}
			}
			if ( changed ) {
				UserManager.getInstance().saveUserRelation(user.getRelations());
			}
			//Send response to client
			XinqiMessage response = new XinqiMessage();
			BseAddFriend.Builder builder = BseAddFriend.newBuilder();
			builder.setUsername(friendName);
			builder.setBlacklist(isBlackUser);
			builder.setIsdel(isDelete);
			builder.setSucc(changed);
			
			response.payload = builder.build();
			
			GameContext.getInstance().writeResponse(user.getSessionKey(), response);
			
			StatClient.getIntance().sendDataToStatServer(user, StatAction.AddFriend, friendName, isBlackUser, isDelete, changed);
			UserActionManager.getInstance().addUserAction(user.getRoleName(), UserActionKey.AddFriend, friendName);
		} else {
			logger.warn("The add friend's userName {} is null", friendName);
		}
	}

	/**
	 * @param user
	 * @param friend
	 * @throws Exception
	 */
	private void processMutualAdd(User user, User friend) throws Exception {
		Relation relation;
		//Mutually add this user to his friend's friend
		SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friend.get_id());
		if ( friendSessionKey != null ) {
			BceAddFriend.Builder friendAddFriendBuilder = BceAddFriend.newBuilder();
			friendAddFriendBuilder.setIsdel(false);
			friendAddFriendBuilder.setBlacklist(false);
			friendAddFriendBuilder.setUsername(user.getRoleName());
			friendAddFriendBuilder.setIsactive(false);

			//When friend is online.
			User localUser = GameContext.getInstance().findLocalUserBySessionKey(friendSessionKey);
			if ( localUser != null ) {
				XinqiMessage xinqi = new XinqiMessage();
				xinqi.payload = friendAddFriendBuilder.build();
				IoSession friendIoSession = localUser.getSession();
				BceAddFriendHandler.getInstance().messageProcess(friendIoSession, xinqi, friendSessionKey);
			} else {
				String gameServerId = GameContext.getInstance().findMachineId(friendSessionKey);
				GameContext.getInstance().proxyToRemoteGameServer(friendSessionKey, gameServerId, friendAddFriendBuilder.build());	
			}
		} else {
			//When friend is offline.
			UserManager.getInstance().queryUserRelation(friend);
			relation = friend.getRelation(RelationType.FRIEND);
			if ( relation == null ) {
				relation = new Relation();
				relation.set_id(friend.get_id());
				relation.setParentUser(friend);
				relation.setType(RelationType.FRIEND);
				friend.addRelation(relation);
			}
			if ( relation.findPeopleByUserName(user.getRoleName()) ==null ) {
				People thisPeople = new People();
				thisPeople.setId(friend.get_id());
				thisPeople.setMyId(user.get_id());
				thisPeople.setUsername(user.getUsername());
				thisPeople.setRolename(user.getRoleName());
				thisPeople.setLevel(user.getLevel());
				thisPeople.setWin(0);
				thisPeople.setLose(0);
				relation.addPeople(thisPeople);
				
				UserManager.getInstance().saveUserRelation(friend.getRelations());
												
				//Check if the friend is online
				/*
				//Update the friend list here.
				sendFriendList(user);
				
				SessionKey friendSessionKey = GameContext.getInstance().findSessionKeyByUserId(friend.get_id());
				friend.setSessionKey(friendSessionKey);
				if ( friendSessionKey != null ) {
					sendFriendList(friend);
					SysMessageManager.getInstance().sendClientInfoMessage(friend, "friend.beingadd", Action.NOOP, new Object[]{user.getRoleName()});
				}
				*/
				
				//Since we call saveUserRelation to change remote user's database data.
				//We should use RPC to notify remote user to update his data.
				//UserRefreshService.getInstance().remoteRefresh(4, friendSessionKey);
				String gameServerId = GameContext.getInstance().getSessionManager().findUserGameServerId(friendSessionKey);
				GameProxyClient gameProxyClient = GameContext.getInstance().findGameProxyChannel(gameServerId);
				if ( gameProxyClient !=null ) {
					XinqiProxyMessage proxy = new XinqiProxyMessage();
					proxy.userSessionKey = friendSessionKey;
					BceUserRefresh.Builder builder = BceUserRefresh.newBuilder();
					int refreshMode = 4;
					builder.setRefreshmode(refreshMode);
					XinqiMessage xinqi = new XinqiMessage();
					xinqi.payload = builder.build();
					proxy.xinqi = xinqi;
					gameProxyClient.sendMessageToServer(proxy);
				}
			}
		}
	}
	
	/**
	 * Send all the friend data to given user.
	 * @param user
	 */
	public void sendFriendList(User user) {
		logger.debug("update user {}'s friend list", user.getRoleName());
		ArrayList<FriendInfoLite> allFriends = new ArrayList<FriendInfoLite>();
		for ( RelationType relationType : RelationType.values() ) {
			Relation relation = user.getRelation(relationType);
			if ( relation != null ) {
				allFriends.addAll(relation.toBseFriendList());
			}
		}
		BseFriendList.Builder bseFriendListBuilder = BseFriendList.newBuilder();
		bseFriendListBuilder.setType(1);
		for ( FriendInfoLite lite : allFriends ) {
			bseFriendListBuilder.addFriendList(lite);
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), bseFriendListBuilder.build());
	}
	
	/**
	 * Only send the new added friend list.
	 * @param relations
	 */
	public void sendNewAddFriendList(User user, Relation relation ) {
		BseFriendList.Builder bseFriendListBuilder = BseFriendList.newBuilder();
		if ( relation != null ) {
			bseFriendListBuilder.setType(relation.getType().ordinal());
			Collection<FriendInfoLite> coll = relation.toBseFriendList();
			if ( coll != null ) {
				for ( FriendInfoLite lite : coll ) {
					bseFriendListBuilder.addFriendList(lite);	
				}
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), bseFriendListBuilder.build());
	}
	
}
