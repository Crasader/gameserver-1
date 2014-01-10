package com.xinqihd.sns.gameserver.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.Stat;
import com.xinqihd.sns.gameserver.ai.AIManager;
import com.xinqihd.sns.gameserver.battle.BattleManager;
import com.xinqihd.sns.gameserver.battle.Room;
import com.xinqihd.sns.gameserver.battle.Room.UserInfo;
import com.xinqihd.sns.gameserver.battle.RoomManager;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.config.equip.TextColor;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.EmailManager;
import com.xinqihd.sns.gameserver.db.mongo.GameDataManager;
import com.xinqihd.sns.gameserver.db.mongo.SysMessageManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.Bag;
import com.xinqihd.sns.gameserver.entity.user.PropData;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.entity.user.UserStatus;
import com.xinqihd.sns.gameserver.guild.GuildManager;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBceChat.BceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBceVoiceChat.BceVoiceChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseChat.BseChat;
import com.xinqihd.sns.gameserver.proto.XinqiBseRoleBattleInfo.BseRoleBattleInfo;
import com.xinqihd.sns.gameserver.proto.XinqiBseVoiceChat.BseVoiceChat;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * Manage the chat service
 * @author wangqi
 *
 */
public class ChatManager {

	private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
	
	private static final ChatManager instance = new ChatManager();
	
	private static final String USER_KEY = "chat_freq";
	
	private static final String VOICE_KEY_PREFIX = "voice:";
	
	private BlockingQueue<ChatMessage> queue = new LinkedBlockingQueue<ChatMessage>();
	
	private ChatProcessor chatProcessor = null;
	
	private RandomData random = new RandomDataImpl();
	
	private TreeSet<String> wordSet = new TreeSet<String>(new StringLengthComparator());
	private TreeSet<String> chargeWordSet = new TreeSet<String>(new StringLengthComparator());
	
	private Pattern wordPattern = null;
	private Pattern chargeWordPattern = null;
	
	ChatManager() {
		chatProcessor = new ChatProcessor(queue);
		chatProcessor.start();
		
		reload();
	}
	
	public static final ChatManager getInstance() {
		return instance;
	}
	
	/**
	 * Reload the filter word
	 */
	public void reload() {
		String wordFileName = GlobalConfig.getInstance().getStringProperty(
				GlobalConfigKey.chat_word_file);
		if ( wordFileName == null ) {
			wordFileName = "../deploy/data/word.txt";
		}
		importWord(wordFileName, wordSet);
		wordPattern = Pattern.compile(getFilteredWordRegex(wordSet));
		
		String chargeWordFileName = GlobalConfig.getInstance().getStringProperty(
				"charge_word_file");
		if ( chargeWordFileName == null ) {
			chargeWordFileName = "../deploy/data/chargeword.txt";
		}
		importWord(chargeWordFileName, chargeWordSet);
		chargeWordPattern = Pattern.compile(getFilteredWordRegex(chargeWordSet));
	}
	
	/**
	 * Import the filtered word list
	 */
	public final void importWord(String wordFileName, TreeSet<String> wordSet) {
		File wordFile = new File(wordFileName);
		if ( wordFile.exists() ) {
			try {
				FileReader fr = new FileReader(wordFile);
				BufferedReader br = new BufferedReader(fr);
				String word = br.readLine();
				while ( word != null ) {
					wordSet.add(word.toLowerCase().trim());
					word = br.readLine();
				}
			} catch (IOException e) {
				logger.warn("Failed to read word list.", e);
			}
		} else {
			logger.warn("The word filter file {} does not exist.", wordFile.getAbsolutePath());
		}
	}
	
	/**
	 * Get the filtered word set.
	 * @return
	 */
	public Set<String> getFilteredWord() {
		return this.wordSet;
	}
	
	/**
	 * Get the regex of filtered word
	 * @return
	 */
	public String getFilteredWordRegex(TreeSet<String> wordSet) {
		StringBuilder buf = new StringBuilder(5000);
		for ( String w : wordSet ) {
			buf.append(w).append('|');
		}
		if ( buf.length()>0 ) {
			buf.deleteCharAt(buf.length()-1);
		}
		return buf.toString();
	}
	
	/**
	 * Replace all bad words in user sentence 
	 * with '*'
	 * @param userWord
	 * @return
	 */
	public String filterWord(String userWord) {
		if ( StringUtil.checkNotEmpty(userWord) ) {
			String lower = userWord.toLowerCase();
			Matcher matcher = wordPattern.matcher(lower);
			lower = matcher.replaceAll("*");
			return lower;
		}
		return userWord;
	}
	
	/**
	 * Replace all bad words in user sentence 
	 * with '*'
	 * @param userWord
	 * @return
	 */
	public boolean hasFilterWord(Pattern pattern, String userWord) {
		if ( StringUtil.checkNotEmpty(userWord) ) {
			String lower = userWord.toLowerCase();
			Matcher matcher = pattern.matcher(lower);
			return matcher.find();
		}
		return false;
	}
	
	/**
	 * Check if the sentence contains bad word
	 * @param userWord
	 * @return
	 */
	public boolean containBadWord(String userWord) {
		if ( StringUtil.checkNotEmpty(userWord) ) {
			String lower = userWord.toLowerCase();
			Matcher matcher = wordPattern.matcher(lower);
			return matcher.find();
		}
		return false;
	}
	
	/**
	 * Process the chat request asynchronously
	 * @param user
	 * @param chat
	 */
	public final boolean processChatAsyn(User user, BceChat chat) {
		//Check user chat frequency
		boolean send = true;
		Room room = null;
		if ( user == null ) return false;
		if ( user.getStatus() == UserStatus.CHAT_DISABLE ) {
			logger.debug("User {} is forbidden to chat.", user.getRoleName());
			send = false;
		} else {
			/**
			 * 私聊、战斗聊天和使用喇叭聊天不限制频率
			 */
			ChatType chatType = null;
			if ( chat.getMsgType() >= 0 && chat.getMsgType() < ChatType.values().length ) {
				chatType = ChatType.values()[chat.getMsgType()];
			}
			if ( chatType == ChatType.ChatPrivate || 
					 chatType == ChatType.ChatWorld ||
					chatType == ChatType.ChatCurrent ) {
				send = true;
			} else {
				if ( !user.isVip() ) {
					Long lastMillis = (Long)user.getUserData(USER_KEY);
					if ( lastMillis != null ) {
						int coolDownMillis = GameDataManager.getInstance().
								getGameDataAsInt(GameDataKey.CHAT_USER_COOLDOWN, 15000);
						if ( System.currentTimeMillis() < lastMillis.longValue() + coolDownMillis ) {
							SysMessageManager.getInstance().sendClientInfoMessage(
									user, "chat.freq", Type.NORMAL);
							send = false;
						}
					}
				}
			}
		}
		if ( send ) {
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.user = user;
			chatMessage.chat = chat;
			chatMessage.room = room;
			queue.offer(chatMessage);
			user.putUserData(USER_KEY, System.currentTimeMillis());
			Stat.getInstance().chatBuffered++;
		}
		
		return send;
	}
	
	/**
	 * Process the chat request asynchronously
	 * @param user
	 * @param chat
	 */
	public final boolean processVoiceChatAsyn(User user, BceVoiceChat chat) {
		//Check user chat frequency
		boolean send = true;
		Room room = null;
		if ( user == null ) return false;
		if ( user.getStatus() == UserStatus.CHAT_DISABLE ) {
			logger.debug("User {} is forbidden to chat.", user.getRoleName());
			send = false;
		} else {
			/**
			 * 私聊、战斗聊天和使用喇叭聊天不限制频率
			 */
			ChatType chatType = null;
			if ( chat.getMsgType() >= 0 && chat.getMsgType() < ChatType.values().length ) {
				chatType = ChatType.values()[chat.getMsgType()];
			}
			if ( chatType == ChatType.ChatPrivate || 
					 chatType == ChatType.ChatWorld ||
					chatType == ChatType.ChatCurrent ) {
				send = true;
			} else {
				if ( !user.isVip() ) {
					Long lastMillis = (Long)user.getUserData(USER_KEY);
					if ( lastMillis != null ) {
						int coolDownMillis = GameDataManager.getInstance().
								getGameDataAsInt(GameDataKey.CHAT_USER_COOLDOWN, 15000);
						if ( System.currentTimeMillis() < lastMillis.longValue() + coolDownMillis ) {
							SysMessageManager.getInstance().sendClientInfoMessage(
									user, "chat.freq", Type.NORMAL);
							send = false;
						}
					}
				}
			}
		}
		if ( send ) {
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.user = user;
			chatMessage.voiceChat = chat;
			chatMessage.room = room;
			queue.offer(chatMessage);
			user.putUserData(USER_KEY, System.currentTimeMillis());
			Stat.getInstance().chatBuffered++;
		}
		
		return send;
	}
	
	/**
	 * Process the chat request asynchronously
	 * @param user
	 * @param chat
	 */
	public final void processChatToWorldAsyn(User user, String content) {
		if ( StringUtil.checkNotEmpty(content) ) {
			BceChat.Builder chat = BceChat.newBuilder();
			chat.setMsgType(ChatType.ChatWorld.ordinal());
			chat.setMsgContent(content);
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.user = user;
			chatMessage.chat = chat.build();
			queue.offer(chatMessage);
			Stat.getInstance().chatBuffered++;
		}
	}
	
	/**
	 * Send system chat message to target user.
	 * 
	 * @param toUser
	 * @param content
	 */
	public final void sendSysChat(User toUser, String content) {
		BseChat.Builder chatBuilder = BseChat.newBuilder();
		chatBuilder.setMsgType(ChatType.ChatWorld.ordinal());
		chatBuilder.setMsgContent(content);
		String nickName = Text.text("gameadmin");
		chatBuilder.setUsrNickname(nickName);
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = chatBuilder.build();
		GameContext.getInstance().writeResponse(toUser.getSessionKey(), xinqi);
	}
	
	/**
	 * Send system chat message to target user.
	 * 
	 * @param toUser
	 * @param content
	 */
	public final void sendPrivateSysChat(SessionKey userSessionKey, String content) {
		BseChat.Builder chatBuilder = BseChat.newBuilder();
		chatBuilder.setMsgType(ChatType.ChatPrivate.ordinal());
		chatBuilder.setMsgContent(content);
		String nickName = Text.text("gameadmin");
		chatBuilder.setUsrNickname(nickName);
		XinqiMessage xinqi = new XinqiMessage();
		xinqi.payload = chatBuilder.build();
		GameContext.getInstance().writeResponse(userSessionKey, xinqi);
	}

	/**
	 * Process the chat request synchronously
	 * @param user
	 * @param chat
	 * @param room TODO
	 */
	public final void processChat(User user, BceChat chat, Room room) {
		int type = chat.getMsgType();
		ChatType chatType = null;
		if ( type >=0 && type < ChatType.values().length ) {
			chatType = ChatType.values()[type];
		}
		
		if ( chatType == null ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Unsupported chattype: " + type);
			}
			return ;
		}

		if ( user == null && chatType != ChatType.ChatWorld ) {
			logger.warn("Cannot send chat to null user");
			return;
		}
		
		String content = chat.getMsgContent();
		
		if ( !StringUtil.checkNotEmpty(content) ) {
			return ;
		} else {
			//content = this.filterWord(content);
			boolean hasBadWord = this.hasFilterWord(wordPattern, content);
			if ( hasBadWord ) {
				sendPrivateChat(user, user.get_id().toString(), content, ChatType.ChatWorld);
				return;
			}
		}
		
		if ( user != null ) {
			Account account = user.getAccount();
			if ( account != null && "chat_disable".equals(account.getStatus()) ) {
				sendPrivateChat(user, user.get_id().toString(), content, ChatType.ChatWorld);
				return;
			}
		}
		
		if ( content.length() > 120 ) {
			content = StringUtil.concat(content.substring(0, 77), "...");
		}
		
		/**
		 * Check if this is a charge related content
		 */
		String unfilteredContent = chat.getMsgContent();
		if ( user != null && unfilteredContent.startsWith("@充值") ) {
			String roleName = user.getRoleName();
			String dateStr = DateUtil.formatDateTime(new Date());
			String subject = StringUtil.concat("充值问题, 玩家:", roleName, ",日期:", dateStr, unfilteredContent);
			String message = StringUtil.concat("充值问题, 玩家:", roleName, ",渠道:", user.getChannel(), unfilteredContent);
			EmailManager.getInstance().sendNormalEmail(subject, message, 
					new String[]{"to.wangqi@gmail.com", "418313139@qq.com"});
			sendSysChat(user, Text.text("chat.charge.recevied"));
			return;
		}
		
		if ( chatType == ChatType.ChatCurrent ) {
			//Check if it is the battle ready window chat
			SessionKey roomSessionKey = RoomManager.getInstance().
					findRoomSessionKeyByUserSession(user.getSessionKey());
			room = RoomManager.getInstance().acquireRoom(roomSessionKey, true);
			if ( room != null ) {
				chatType = ChatType.ChatRoom;
			}
		}
		//TODO Do some business check here
		switch (chatType) {
			//The system admin can send a global messages.
			case ChatSystem:
				if ( logger.isDebugEnabled() ) {
					logger.debug("Only system admin can send system message.");
				}
				break;
			//The global world messages.‰
			/**
			 * 根据畅游的建议，ChatServer类型只显示在聊天历史中
			 * ChatWorld显示在聊天历史和滚动历史中
			 */
			case ChatServer:
			case ChatWorld:
				//Get all online users
				//Send them the message.
				List<SessionKey> allOnlineUsers = GameContext.getInstance().
					findAllOnlineUsers();
				if ( allOnlineUsers != null ) {
					BseChat.Builder chatBuilder = BseChat.newBuilder();
					chatBuilder.setMsgType(chatType.ordinal());
					chatBuilder.setMsgContent(content);
					String nickName = formatNickName(user);
					chatBuilder.setUsrNickname(nickName);
					if ( user != null ) {
						chatBuilder.setUsrId(StringUtil.concat(user.get_id().toString(), 
								"|", user.getViplevel()) );
					}
					XinqiMessage xinqi = new XinqiMessage();
					xinqi.payload = chatBuilder.build();
					for (Iterator iter = allOnlineUsers.iterator(); iter.hasNext();) {
						SessionKey userKey = (SessionKey) iter.next();
						GameContext.getInstance().writeResponse(userKey, xinqi);
					}
				}
				if ( user != null && chatType == ChatType.ChatWorld ) {
					//扣掉用户一个大喇叭
					Bag bag = user.getBag();
					ArrayList<PropData> bagProps = new ArrayList<PropData>(bag.getOtherPropDatas());
					String speakerId = GameDataManager.getInstance().getGameDataAsString(GameDataKey.SMALL_SPEAKER_ID);
					if ( speakerId == null ) speakerId = "26001";
					boolean foundSpeaker = false;
					for ( PropData prop : bagProps ) {
						if ( prop == null ) continue;
						if ( speakerId.equals(prop.getItemId()) ) {
							bag.removeOtherPropDatas(prop.getPew());
							if ( prop.getPew() < 0 ) {
								//The propData is removed.
								//Sync with client
								BseRoleBattleInfo roleBattleInfo = user.toBseRoleBattleInfo(true);
								GameContext.getInstance().writeResponse(user.getSessionKey(), roleBattleInfo);
							}
							logger.debug("User {} use a small speaker in bag", user.getUsername());
							foundSpeaker = true;
							UserManager.getInstance().saveUserBag(user, false);
							
							TaskManager.getInstance().processUserTasks(user, TaskHook.CHAT_WORLD);
							
							ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_CHAT, user, content);
							
							break;
						}
					}
					if ( !foundSpeaker ) {
						//TODO client try to verify this
						logger.warn("User {} does not have speaker item in bag");
					}
				}
				if ( user != null ) {
					/**
					 * Check the 充值 keywords
					 */
					boolean chargeRelated = hasFilterWord(chargeWordPattern, content);
					if ( chargeRelated ) {
						//auto reply
						sendSysChat(user, Text.text("chat.charge.reply"));
					}
				}
								
				break;
			//The hall messages.
			case ChatCurrent:
				SessionKey userSessionKey = user.getSessionKey();
				SessionKey battleSessionKey = BattleManager.getInstance().
					findBattleSessionKeyByUserSessionKey(userSessionKey);
				if ( battleSessionKey != null ) {
					logger.debug("User is in battle room. Send the chat message '{}'", content);
					BattleManager.getInstance().sendChatToAllUsers(userSessionKey, content);
				}				break;
			//The guild messages.
			case ChatGuild:
				Collection<String> sessionKeyStrList = GuildManager.getInstance().
					listGuildMemberOnline(user.getGuildId());
				if ( sessionKeyStrList != null ) {
					BseChat.Builder chatBuilder = BseChat.newBuilder();
					chatBuilder.setMsgType(ChatType.ChatGuild.ordinal());
					chatBuilder.setMsgContent(content);
					chatBuilder.setUsrId(user.getSessionKey().toString());
					String roleName = UserManager.getDisplayRoleName(user.getRoleName());
					chatBuilder.setUsrNickname(roleName);
					BseChat bseChat = chatBuilder.build();
					
					for (Iterator iter = sessionKeyStrList.iterator(); iter
							.hasNext();) {
						String sessionKeyStr = (String) iter.next();
						SessionKey sessionKey = SessionKey.createSessionKeyFromHexString(sessionKeyStr);
						GameContext.getInstance().writeResponse(sessionKey, bseChat);
					}
				}
				break;
			//The room messages.
			case ChatRoom:
				if ( room != null ) {
					BseChat.Builder chatBuilder = BseChat.newBuilder();
					/**
					 * Now the client can only support chat private and world types.
					 * wangqi 2012-11-1
					 */
					chatBuilder.setMsgType(ChatType.ChatCurrent.ordinal());
					chatBuilder.setMsgContent(content);
					chatBuilder.setUsrId(user.getSessionKey().toString());
					String roleName = UserManager.getDisplayRoleName(user.getRoleName());
					chatBuilder.setUsrNickname(roleName);
					BseChat bseChat = chatBuilder.build();
					for ( UserInfo userInfo : room.getUserInfoList() ) {
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
						if ( !AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), bseChat);
						}
					}
				}
				break;
			//The private to private messages.
			case ChatPrivate:
				sendPrivateChat(user, chat.getUsrId(), content, chatType);
				break;
			default:
				logger.debug("Unknown chat type: {}", type);
				break;
		}
	}

	/**
	 * Process the chat request synchronously
	 * @param user
	 * @param chat
	 * @param room TODO
	 */
	public final void processVoiceChat(User user, BceVoiceChat chat, Room room) {
		int type = chat.getMsgType();
		ChatType chatType = null;
		if ( type >=0 && type < ChatType.values().length ) {
			chatType = ChatType.values()[type];
		}
		
		if ( chatType == null ) {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Unsupported chattype: " + type);
			}
			return ;
		}

		if ( user == null && chatType != ChatType.ChatWorld ) {
			logger.warn("Cannot send chat to null user");
			return;
		}

		if ( user != null ) {
			Account account = user.getAccount();
			if ( account != null && "chat_disable".equals(account.getStatus()) ) {
				return;
			}
		}
		
		byte[] content = chat.getMsgContent().toByteArray();
		
		if ( chatType == ChatType.ChatCurrent ) {
			//Check if it is the battle ready window chat
			SessionKey roomSessionKey = RoomManager.getInstance().
					findRoomSessionKeyByUserSession(user.getSessionKey());
			room = RoomManager.getInstance().acquireRoom(roomSessionKey, true);
			if ( room != null ) {
				chatType = ChatType.ChatRoom;
			}
		}
		//TODO Do some business check here
		switch (chatType) {
			//The system admin can send a global messages.
			case ChatSystem:
				if ( logger.isDebugEnabled() ) {
					logger.debug("Only system admin can send system message.");
				}
				break;
			//The global world messages.‰
			/**
			 * 根据畅游的建议，ChatServer类型只显示在聊天历史中
			 * ChatWorld显示在聊天历史和滚动历史中
			 */
			case ChatServer:
			case ChatWorld:
				//Get all online users
				//Send them the message.
				List<SessionKey> allOnlineUsers = GameContext.getInstance().
					findAllOnlineUsers();
				if ( allOnlineUsers != null ) {
					BseVoiceChat.Builder chatBuilder = BseVoiceChat.newBuilder();
					chatBuilder.setMsgType(chatType.ordinal());
					if ( chat.getAutoplay() ) {
						chatBuilder.setMsgContent(ByteString.copyFrom(content));
					} else {
						byte[] voiceId = storeVoiceContent(content);
						chatBuilder.setVoiceid(ByteString.copyFrom(voiceId));
					}
					chatBuilder.setAutoplay(chat.getAutoplay());
					chatBuilder.setSecond(chat.getSecond());
					String nickName = formatNickName(user);
					chatBuilder.setUsrNickname(nickName);
					if ( user != null ) {
						chatBuilder.setUsrId(StringUtil.concat(user.get_id().toString(), 
								"|", user.getViplevel()) );
					}
					XinqiMessage xinqi = new XinqiMessage();
					xinqi.payload = chatBuilder.build();
					for (Iterator iter = allOnlineUsers.iterator(); iter.hasNext();) {
						SessionKey userKey = (SessionKey) iter.next();
						GameContext.getInstance().writeResponse(userKey, xinqi);
					}
				}
				if ( user != null && chatType == ChatType.ChatWorld ) {
					//扣掉用户一个大喇叭
					Bag bag = user.getBag();
					ArrayList<PropData> bagProps = new ArrayList<PropData>(bag.getOtherPropDatas());
					String speakerId = GameDataManager.getInstance().getGameDataAsString(GameDataKey.SMALL_SPEAKER_ID);
					if ( speakerId == null ) speakerId = "26001";
					boolean foundSpeaker = false;
					for ( PropData prop : bagProps ) {
						if ( speakerId.equals(prop.getItemId()) ) {
							bag.removeOtherPropDatas(prop.getPew());
							if ( prop.getPew() < 0 ) {
								//The propData is removed.
								//Sync with client
								BseRoleBattleInfo roleBattleInfo = user.toBseRoleBattleInfo(true);
								GameContext.getInstance().writeResponse(user.getSessionKey(), roleBattleInfo);
							}
							logger.debug("User {} use a small speaker in bag", user.getUsername());
							foundSpeaker = true;
							UserManager.getInstance().saveUserBag(user, false);
							
							TaskManager.getInstance().processUserTasks(user, TaskHook.CHAT_WORLD);
							
							ScriptManager.getInstance().runScript(ScriptHook.PROMOTION_CHAT, user, content);
							
							break;
						}
					}
					if ( !foundSpeaker ) {
						//TODO client try to verify this
						logger.warn("User {} does not have speaker item in bag");
					}
				}								
				break;
			//The hall messages.
			case ChatCurrent:
				SessionKey userSessionKey = user.getSessionKey();
				SessionKey battleSessionKey = BattleManager.getInstance().
					findBattleSessionKeyByUserSessionKey(userSessionKey);
				if ( battleSessionKey != null ) {
					logger.debug("User is in battle room. Send the chat message '{}'", content);
					BattleManager.getInstance().sendVoiceChatToAllUsers(userSessionKey, content, chat.getAutoplay(), chat.getSecond());
				}				break;
			//The guild messages.
			case ChatGuild:
				break;
			//The room messages.
			case ChatRoom:
				if ( room != null ) {
					BseVoiceChat.Builder chatBuilder = BseVoiceChat.newBuilder();
					/**
					 * Now the client can only support chat private and world types.
					 * wangqi 2012-11-1
					 */
					chatBuilder.setMsgType(ChatType.ChatCurrent.ordinal());
					if ( chat.getAutoplay() ) {
						chatBuilder.setMsgContent(ByteString.copyFrom(content));
					} else {
						byte[] voiceId = storeVoiceContent(content);
						chatBuilder.setVoiceid(ByteString.copyFrom(voiceId));
					}
					chatBuilder.setUsrId(user.getSessionKey().toString());
					chatBuilder.setAutoplay(chat.getAutoplay());
					chatBuilder.setSecond(chat.getSecond());
					String roleName = UserManager.getDisplayRoleName(user.getRoleName());
					chatBuilder.setUsrNickname(roleName);
					BseVoiceChat bseChat = chatBuilder.build();
					for ( UserInfo userInfo : room.getUserInfoList() ) {
						if ( userInfo == null || userInfo == Room.BLOCKED_USER_INFO ) continue;
						if ( !AIManager.getInstance().isAIUser(userInfo.getUserSessionKey()) ) {
							GameContext.getInstance().writeResponse(userInfo.getUserSessionKey(), bseChat);
						}
					}
				}
				break;
			//The private to private messages.
			case ChatPrivate:
				sendPrivateVoiceChat(user, chat.getUsrId(), content, chatType, chat.getAutoplay(), chat.getSecond());
				break;
			default:
				logger.debug("Unknown chat type: {}", type);
				break;
		}
	}
	
	/**
	 * Since the voice is too big to transfer, the better
	 * way to process is to store it on server-side, and 
	 * send the given user when asked so.
	 * 
	 */
	public final byte[] storeVoiceContent(byte[] content) {
		int rid = 0;
		String key = null;
		boolean duplicate = true;
		Jedis jedis = JedisFactory.getJedisDB();
		while ( duplicate) {
			rid = random.nextInt(0, Integer.MAX_VALUE);
			key = StringUtil.concat(VOICE_KEY_PREFIX, rid);
			duplicate = jedis.exists(key);
		}
		byte[] keyBytes = key.getBytes();
		jedis.set(keyBytes, content);
		//10 minutes expire
		jedis.expire(keyBytes, 600);
		return keyBytes;
	}

	/**
	 * Retrieve the voice content from database if exists
	 * @param voiceKey
	 */
	public final byte[] retrieveVoiceContent(String voiceKey) {
		Jedis jedis = JedisFactory.getJedisDB();
		byte[] voice = jedis.get(voiceKey.getBytes());
		return voice;
	}
	
	/**
	 * @param user
	 * @param chat
	 * @param chatType
	 * @param content
	 */
	public void sendPrivateChat(User user, String toUserId, String content, ChatType chatType) {
		String fromUserId = user.get_id().toString();
		if ( StringUtil.checkNotEmpty(toUserId) ) {
			UserId userId = UserId.fromString(toUserId);
			if ( userId != null ) {
				//Check if user is online
				SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(userId);

				if ( sessionKey != null ) {
					BseChat.Builder chatBuilder = BseChat.newBuilder();
					chatBuilder.setMsgType(chatType.ordinal());
					chatBuilder.setMsgContent(content);
					//The usrId and nickname should be the user who send this message
					chatBuilder.setUsrId(fromUserId);
					String nickName = formatNickName(user);
					chatBuilder.setUsrNickname(nickName);
					XinqiMessage xinqi = new XinqiMessage();
					xinqi.payload = chatBuilder.build();
					GameContext.getInstance().writeResponse(sessionKey, xinqi);
				} else {
					if ( logger.isDebugEnabled() ) {
						logger.debug("User " + userId + " is offline. ");
					}
				}
			}
		} else {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Target userid is empty.");
			}
		}
	}
	
	/**
	 * @param user
	 * @param chat
	 * @param chatType
	 * @param content
	 */
	private void sendPrivateVoiceChat(User user, String toUserId, byte[] content, 
			ChatType chatType, boolean autoPlay, int seconds) {
		String fromUserId = user.get_id().toString();
		if ( StringUtil.checkNotEmpty(toUserId) ) {
			UserId userId = UserId.fromString(toUserId);
			if ( userId != null ) {
				//Check if user is online
				SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(userId);

				if ( sessionKey != null ) {
					BseVoiceChat.Builder chatBuilder = BseVoiceChat.newBuilder();
					chatBuilder.setMsgType(chatType.ordinal());
					chatBuilder.setMsgContent(ByteString.copyFrom(content));
					if ( autoPlay ) {
						chatBuilder.setMsgContent(ByteString.copyFrom(content));
					} else {
						byte[] voiceId = storeVoiceContent(content);
						chatBuilder.setVoiceid(ByteString.copyFrom(voiceId));
					}
					chatBuilder.setAutoplay(autoPlay);
					chatBuilder.setSecond(seconds);
					//The usrId and nickname should be the user who send this message
					chatBuilder.setUsrId(fromUserId);
					String nickName = formatNickName(user);
					chatBuilder.setUsrNickname(nickName);
					XinqiMessage xinqi = new XinqiMessage();
					xinqi.payload = chatBuilder.build();
					GameContext.getInstance().writeResponse(sessionKey, xinqi);
				} else {
					if ( logger.isDebugEnabled() ) {
						logger.debug("User " + userId + " is offline. ");
					}
				}
			}
		} else {
			if ( logger.isDebugEnabled() ) {
				logger.debug("Target userid is empty.");
			}
		}
	}
	
	/**
	 * Format the nickname according to user vip status
	 * @param user
	 * @param chatBuilder
	 */
	private String formatNickName(User user) {
		if ( user == null ) {
			return Text.text("gameadmin");
		/*
		} else if ( user.isVip() ) {
			return StringUtil.concat("#FF0000<vip", user.getViplevel(), ">#00ee33", user.getRoleName());
		*/
		} else {
			String roleName = UserManager.getDisplayRoleName(user.getRoleName());
			if ( user.getGuild() != null ) {
				String guildName = user.getGuild().getTitle();
				roleName = StringUtil.concat(roleName, TextColor.ORANGE.getColorStr(), " [", guildName, "]");
			}
			return roleName;
		}
	}
	
	private static final class ChatMessage {
		User user;
		BceChat chat;
		BceVoiceChat voiceChat;
		Room room;
	}
	
	private static final class ChatProcessor extends Thread {
		
		BlockingQueue<ChatMessage> queue = null;
		
		boolean isRunning = false;
		
		ChatProcessor( BlockingQueue<ChatMessage> queue ) {
			this.queue = queue;
			this.setName("ChatProcessor");
		}
		
		public boolean isRunning() {
			return isRunning;
		}
		
		public void run() {
			try {
				logger.info("ChatProcessor starts to run");
				isRunning = true; 
				while ( true ) {
					try {
						ChatMessage chatMessage = queue.take();
						Stat.getInstance().chatBuffered--;
						if ( chatMessage.chat != null ) {
							ChatManager.getInstance().processChat(chatMessage.user, chatMessage.chat, null);
						}
						if ( chatMessage.voiceChat != null ) {
							ChatManager.getInstance().processVoiceChat(chatMessage.user, chatMessage.voiceChat, null);
						}
						logger.debug("Send chat message. Buffer count:{}", Stat.getInstance().chatBuffered);
					} catch (Throwable t) {
						logger.warn("#ChatProcessor.run(): ", t);
					}
				}
			} finally {
				logger.info("ChatProcessor finished.");
				isRunning = false;
			}
		}
	}
}
