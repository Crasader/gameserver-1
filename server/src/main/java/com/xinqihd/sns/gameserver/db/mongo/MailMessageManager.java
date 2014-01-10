package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Pipeline;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.xinqihd.sns.gameserver.GameContext;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.entity.user.UserActionKey;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailList.BseMailList;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailReceive.BseMailReceive;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * This manager is used to implement mail system in game.
 * At current time, since client does not have a mail UI,
 * this mail system will serve as an simple offline messenger.
 * 1) If the user is online, the message is sent to it by SysMessageManager
 * 2) If the user if offline, the message will be stored in database
 * and send when the user first login.
 * 
 * @author wangqi
 * 
 */
public class MailMessageManager {
	
	public static enum MailBoxType {
		inbox,
		sentbox
	}

	private static final Logger logger = LoggerFactory
			.getLogger(MailMessageManager.class);

	private static MailMessageManager instance = new MailMessageManager();
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "mail:";
	
	private static final String KEY_OFFLINE_KEY = "offline:";
	
	private static final String SEP = "`";
	
	private static final String SEP_GIFT = ";";
	
	private ApnsService iosApnService = null;

	MailMessageManager() {
		try {
			//Setup APNS connection
			boolean isDevelopment = GlobalConfig.getInstance().getBooleanProperty(
					GlobalConfigKey.ios_push_develop);
			String certificatePath = GlobalConfig.getInstance().getStringProperty(
					GlobalConfigKey.ios_push_production_pem);
			String password = GlobalConfig.getInstance().getStringProperty(
					GlobalConfigKey.ios_push_certificate_password);
			if ( isDevelopment ) {
				certificatePath = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.ios_push_development_pem);
				iosApnService = APNS.newService()
				    .withCert(certificatePath, password)
				    .withSandboxDestination()
				    .build();
			} else {
				iosApnService = APNS.newService()
				    .withCert(certificatePath, password)
				    .withProductionDestination()
				    .build();
			}
			logger.debug("APNS use certificate: {}", certificatePath);
		} catch (Exception e) {
			iosApnService = null;
			logger.warn("Failed to setup APN service");
		}
	}

	/**
	 * Get a singleton instance for this manager.
	 * 
	 * @return
	 */
	public static final MailMessageManager getInstance() {
		return instance;
	}
	
	/**
	 * Send a mail to given user. If the isAdmin is true,
	 * the mail is treated as a game admin's mail.
	 * 'subject' and 'gift' are optional and can be set to 
	 * null safely. The 'content' is mandatory or this mail
	 * will not be sent.
	 * 
	 * @param fromUserId
	 * @param toUserId
	 * @param subject
	 * @param content
	 * @param gift
	 * @param isAdmin
	 */
	public boolean sendAdminMail(UserId toUserId, 
			String subject, String content, Reward gift) {
		if ( toUserId == null ) {
			logger.debug("#sendMail: null toUserId");
			return false;
		}
		if ( StringUtil.checkNotEmpty(content) ) {
			/**
			 * TODO Now we directly take the gift into user's bag
			 * In the future, users should open the gift in mail box manually.
			 */
			String fromUserName = Text.text("system");
			String today = DateUtil.getToday(System.currentTimeMillis());
			BasicUser toUser = null;
			String mailSub = Constant.EMPTY;
			if ( subject != null ) {
				int maxSubCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_SUBJECT, 40);
				if ( subject.length() > maxSubCount ) {
					mailSub = subject.substring(0, maxSubCount).concat("...");
				} else {
					mailSub = subject;
				}
			}
			if ( mailSub.length()>0 ) {
				mailSub = StringUtil.concat("#ff0000", mailSub);
			}
			String mailContent = content;
			int maxContentCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_CONTENT, 140);
			if ( content.length() > maxContentCount ) {
				mailSub = content.substring(0, maxContentCount).concat("...");
			}
			
			SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(toUserId);
			if ( sessionKey != null ) {
				//The user is online
				toUser = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
				
				//String message = Text.text("mail.template", fromUserName, today, content);
				//Send the message to user immediately
				//SysMessageManager.getInstance().sendClientInfoRawMessage(
				//		toUser, content, Action.NOOP, Type.NORMAL);
				MailData.Builder mail = MailData.newBuilder();
				mail.setFromuser(fromUserName);
				mail.setSubject(mailSub);
				mail.setContent(mailContent);
				mail.setSentdate(today);
				mail.setIsnew(true);
				if ( gift != null ) {
					mail.addGifts(gift.toGift());
				}
				BseMailReceive.Builder builder = BseMailReceive.newBuilder();
				builder.addMails(mail.build());
				GameContext.getInstance().writeResponse(sessionKey, builder.build());
			} else {
				//The user is offline.
				toUser = UserManager.getInstance().queryBasicUser(toUserId);
			}
			if ( toUser == null ) {
				logger.debug("#sendMail: target user is not found. {}", toUserId);
				return false;
			}
							
			//Save the message into database
			String list = getMailboxName(toUser.getUsername());
			int maxListCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_COUNT, 140);
			int expireSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_EXPIRE_SECONDS, 604800);
			
			StringBuilder giftBuf = new StringBuilder(40);
			giftBuf.append(gift);
			
			String message = StringUtil.concat(fromUserName, SEP, today, SEP, mailSub, SEP, mailContent, 
					SEP, System.currentTimeMillis(), SEP, "true", SEP, giftBuf.toString());
			/**
			 * Only allow the this method & checkMail to set expire seconds,
			 * all other methods do not change it. 
			 * So if the user has not been logined for 7 days, his mailbox
			 * will be cleaned. 
			 */
			pushValueInList(list, message, maxListCount, expireSeconds);
			
			String sentlist = getMailboxName(fromUserName, MailBoxType.sentbox);
			pushValueInList(sentlist, message, maxListCount, expireSeconds);
						
		} else {
			logger.debug("#sendMail: null content");
			return false;
		}
		return true;
	}

	/**
	 * Send a mail to given user. If the isAdmin is true,
	 * the mail is treated as a game admin's mail.
	 * 'subject' and 'gift' are optional and can be set to 
	 * null safely. The 'content' is mandatory or this mail
	 * will not be sent.
	 * 
	 * @param fromUserId
	 * @param toUserId
	 * @param subject
	 * @param content
	 * @param gift
	 * @param isAdmin
	 */
	public boolean sendMail(UserId fromUserId, UserId toUserId, 
			String subject, String content, Collection<Reward> gifts, boolean isAdmin) {
		if ( toUserId == null ) {
			logger.debug("#sendMail: null toUserId");
			return false;
		}
		if ( !isAdmin && fromUserId == null ) {
			logger.debug("#sendMail: null fromUserId");
			return false;
		}
		try {
			if ( StringUtil.checkNotEmpty(subject) ) {
				SessionKey fromSessionKey = GameContext.getInstance().findSessionKeyByUserId(fromUserId);
				if ( fromSessionKey != null ) {
					User user = GameContext.getInstance().findLocalUserBySessionKey(fromSessionKey);
					if ( user != null && user.isAdmin() ) {
						String[] command = subject.split(" ");
						if ( command != null ) {
							SessionKey toSessionKey = GameContext.getInstance().findSessionKeyByUserId(toUserId);
							User toUser = null;
							if ( toSessionKey != null ) {
								toUser = GameContext.getInstance().findGlobalUserBySessionKey(toSessionKey);
							} else {
								toUser = UserManager.getInstance().queryUser(toUserId);
							}
							String response = null;
							if ( command[0].equals("charge") ) {
								int money = StringUtil.toInt(command[1], 0);
								if ( money > 0 ) {
									int oldYuanbao = toUser.getYuanbao();
									int oldVip = toUser.getViplevel();
									ChargeManager.getInstance().doCharge(toSessionKey, toUser, null, null, money, "admin", true);
									response = StringUtil.concat("money:", money, ", user:",  toUser.getRoleName(),
											", old yuanbao:", oldYuanbao, ", old vip:", oldVip, ", yuanbao:", toUser.getYuanbao(),
											", vip:", toUser.getViplevel());
								}
							} else if ( command[0].equals("forbid") ) {
								AccountManager.getInstance().forbiddenAccount(toUser.getAccountName(), null);
								response = StringUtil.concat(", user:",  toUser.getRoleName(), ", account:", user.getAccountName(),
										", uuid:", user.getClient(), " is forbidden"
										);
							}
							if ( StringUtil.checkNotEmpty(response) ) {
								sendAdminMail(fromUserId, "admin response:"+new Date(), response, null);
								return true;
							}
						}
					}
				}
			}
			if ( StringUtil.checkNotEmpty(content) ) {
				/**
				 * TODO Now we directly take the gift into user's bag
				 * In the future, users should open the gift in mail box manually.
				 */
				String fromUserName = null;
				if ( isAdmin ) {
					fromUserName = Text.text("system");
				} else {
					BasicUser basicUser = UserManager.getInstance().queryBasicUser(fromUserId);
					fromUserName = UserManager.getDisplayRoleName(basicUser.getRoleName());
				}
				String today = DateUtil.getToday(System.currentTimeMillis());
				BasicUser toUser = null;
				String mailSub = Constant.EMPTY;
				if ( subject != null ) {
					int maxSubCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_SUBJECT, 40);
					if ( subject.length() > maxSubCount ) {
						mailSub = subject.substring(0, maxSubCount).concat("...");
					} else {
						mailSub = subject;
					}
				}
				if ( isAdmin && mailSub.length()>0 ) {
					mailSub = StringUtil.concat("#ff0000", mailSub);
				}
				String mailContent = content;
				int maxContentCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_CONTENT, 140);
				if ( content.length() > maxContentCount ) {
					mailSub = content.substring(0, maxContentCount).concat("...");
				}
				
				SessionKey sessionKey = GameContext.getInstance().findSessionKeyByUserId(toUserId);
				if ( sessionKey != null ) {
					//The user is online
					toUser = GameContext.getInstance().findGlobalUserBySessionKey(sessionKey);
					
					//String message = Text.text("mail.template", fromUserName, today, content);
					//Send the message to user immediately
					//SysMessageManager.getInstance().sendClientInfoRawMessage(
					//		toUser, content, Action.NOOP, Type.NORMAL);
					MailData.Builder mail = MailData.newBuilder();
					mail.setFromuser(fromUserName);
					mail.setSubject(mailSub);
					mail.setContent(mailContent);
					mail.setSentdate(today);
					mail.setIsnew(true);
					if ( gifts != null && gifts.size()>0 ) {
						for ( Reward gift : gifts ) {
							mail.addGifts(gift.toGift());	
						}
					}
					BseMailReceive.Builder builder = BseMailReceive.newBuilder();
					builder.addMails(mail.build());
					GameContext.getInstance().writeResponse(sessionKey, builder.build());
				} else {
					//The user is offline.
					toUser = UserManager.getInstance().queryBasicUser(toUserId);
				}
				if ( toUser == null ) {
					logger.debug("#sendMail: target user is not found. {}", toUserId);
					return false;
				}
								
				//Save the message into database
				String list = getMailboxName(toUser.getUsername());
				int maxListCount = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_MAX_COUNT, 140);
				int expireSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_EXPIRE_SECONDS, 604800);

				StringBuilder giftBuf = new StringBuilder(40);
				if ( gifts != null && gifts.size()>0 ) {
					for ( Reward gift : gifts ) {
						giftBuf.append(gift).append(SEP_GIFT);
					}
					giftBuf.deleteCharAt(giftBuf.length()-1);
				}

				String message = StringUtil.concat(fromUserName, SEP, today, SEP, mailSub, SEP, mailContent, 
						SEP, System.currentTimeMillis(), SEP, "true", SEP, giftBuf.toString());
				
				if ( toUser != null ) {
					logger.info("Mail to {}; content:{}", toUser.getRoleName(), message);
				}

				/**
				 * Only allow the this method & checkMail to set expire seconds,
				 * all other methods do not change it. 
				 * So if the user has not been logined for 7 days, his mailbox
				 * will be cleaned. 
				 */
				pushValueInList(list, message, maxListCount, expireSeconds);
				if ( !isAdmin ) {
					String sentlist = getMailboxName(fromUserName, MailBoxType.sentbox);
					pushValueInList(sentlist, message, maxListCount, expireSeconds);
				}

			} else {
				logger.debug("#sendMail: null content");
				return false;
			}
		} catch (Exception e) {
			logger.warn("Failed to send mail", e);
			return false;
		}
		return true;
	}
	
	/**
	 * The user has read a mail.
	 * @param user
	 * @param mailIndex
	 */
	public void readMail(User user, int mailIndex, MailBoxType type) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = MailMessageManager.getMailboxName(user.getUsername(), type);
		String mailMessage = jedisDB.lindex(mailbox, mailIndex);
		if ( StringUtil.checkNotEmpty(mailMessage) ) {
			String newMailMessage = mailMessage.replace("true", "false");
			jedisDB.lset(mailbox, mailIndex, newMailMessage);
			
			StatClient.getIntance().sendDataToStatServer(
					user, StatAction.MailRead, mailIndex, mailMessage);
			
			UserActionManager.getInstance().addUserAction(user.getRoleName(), 
					UserActionKey.MailRead);
		}
	}
	
	/**
	 * Take the mail's attacked gifts.
	 * @param user
	 * @param mailIndex
	 */
	public boolean takeMailGift(User user, int mailIndex) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String mailbox = MailMessageManager.getMailboxName(user.getUsername());
		String mailMessage = jedisDB.lindex(mailbox, mailIndex);
		if ( StringUtil.checkNotEmpty(mailMessage) ) {
			String[] fields = mailMessage.split(SEP);
			if ( fields.length >= 7 ) {
				//fromUserName, SEP, today, SEP, mailSub, SEP, mailContent);
				String gift = fields[6];
				Reward reward = Reward.fromString(gift);
				ArrayList<Reward> rewards = new ArrayList<Reward>();
				rewards.add(reward);
				PickRewardResult result = RewardManager.getInstance().pickRewardWithResult(
						user, rewards, StatAction.ProduceMailGift);
				
				boolean success = (result == PickRewardResult.SUCCESS);
				if ( success ) {
					//系统`2012-09-19`test5-土神石lv5`test5-土神石lv5`1348039200172`true`{20010,20002,ITEM,0,1,0,0,0,0,false}
					//gift: {20010,20002,ITEM,0,1,0,0,0,0,false}
					String newMailMessage = mailMessage.substring(0, mailMessage.length()-gift.length()-1);
					jedisDB.lset(mailbox, mailIndex, newMailMessage);
				}
				return success;
			} else {
				logger.debug("The mail does not have attacked gift.");
			}
		}
		return false;
	}

	/**
	 * Check if there are new mails for the given user
	 */
	public void checkMail(User user) {
		int expireSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_EXPIRE_SECONDS, 604800);
		String mailbox = MailMessageManager.getMailboxName(user.getUsername());
		Jedis jedisDB = JedisFactory.getJedisDB();
		List<String> mailList = jedisDB.lrange(mailbox, 0, -1);
		jedisDB.expire(mailbox, expireSeconds);
		BseMailList.Builder builder = BseMailList.newBuilder();
		builder.setType(MailBoxType.inbox.ordinal());
		for ( String mail : mailList ) {
			MailData mailData = parseMailString(mail);
			if ( mailData != null ) {
				builder.addMails(mailData);
			}
		}
		//Send the mail back to client
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
		
		checkSendBox(user, jedisDB);
	}

	/**
	 * @param user
	 * @param expireSeconds
	 * @param jedisDB
	 */
	public void checkSendBox(User user, Jedis jedisDB) {
		if ( jedisDB == null ) {
			jedisDB = JedisFactory.getJedisDB();
		}
		int expireSeconds = GameDataManager.getInstance().getGameDataAsInt(GameDataKey.MAIL_EXPIRE_SECONDS, 604800);
		BseMailList.Builder builder = null;
		//Get the sentbox
		String sentMailbox = MailMessageManager.getMailboxName(user.getUsername(), MailBoxType.sentbox);
		List<String> sentMailList = jedisDB.lrange(sentMailbox, 0, -1);
		jedisDB.expire(sentMailbox, expireSeconds);
		builder = BseMailList.newBuilder();
		builder.setType(MailBoxType.sentbox.ordinal());
		for ( String mail : sentMailList ) {
			MailData mailData = parseMailString(mail);
			if ( mailData != null ) {
				builder.addMails(mailData);
			}
		}
		GameContext.getInstance().writeResponse(user.getSessionKey(), builder.build());
	}
		
	/**
	 * Delete the given email from list.
	 * @param user
	 * @param mailIndex
	 */
	public int deleteMail(User user, int[] mailIndex, MailBoxType type) {
		String mailbox = MailMessageManager.getMailboxName(user.getUsername(), type);
		StatClient.getIntance().sendDataToStatServer(
				user, StatAction.MailDelete, type);
		return deleteValueInList(mailbox, mailIndex);
	}
	
	/**
	 * Delete the given email from list.
	 * @param user
	 * @param mailIndex
	 */
	public void deleteAllMail(User user, MailBoxType type) {
		String mailbox = MailMessageManager.getMailboxName(user.getUsername(), type);
		StatClient.getIntance().sendDataToStatServer(
				user, StatAction.MailDelete, type, "ALL");
		deleteAllInList(mailbox);
	}
	
	/**
	 * Push an string into the Redis list and update the expire seconds.
	 * If the count of the list exceeds 'maxListCount', all the old content
	 * are trimmed.
	 * 
	 * @param subject
	 * @param content
	 */
	public void pushValueInList(String list, String content, int maxListCount, int expireSeconds) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Pipeline pipeline = jedisDB.pipelined();
		pipeline.lpush(list, content);
		if ( maxListCount > 0 ) {
			pipeline.ltrim(list, 0, maxListCount-1);
		}
		//update the expire seconds if exist
		if ( expireSeconds > 0 ) {
			pipeline.expire(list, expireSeconds);
		}
		pipeline.sync();
	}
	
	/**
	 * Remove the given index of mail in mailbox.
	 * 
	 * @param list
	 * @param index
	 */
	public int deleteValueInList(String list, int[] indexes) {
		if ( indexes != null && indexes.length > 0 ) {
			Jedis jedisDB = JedisFactory.getJedisDB();
			List<String> mailList = new ArrayList<String>(indexes.length);
			for ( int index : indexes ) {
				String mail = jedisDB.lindex(list, index);
				if ( mail != null ) {
					mailList.add(mail);
				}
			}
			int deleteCount = 0;
			for ( String mail : mailList ) {
				Long delete = jedisDB.lrem(list, 1, mail);
				if ( delete != null && delete.intValue() > 0 ) {
					deleteCount++;
				}
			}
			return deleteCount;
		}
		return 0;
	}
	
	/**
	 * Remove all values in the given list.
	 * @param list
	 * @return
	 */
	public void deleteAllInList(String list) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		jedisDB.del(list);
	}
	
	/**
	 * Pop the latest mail from the database and update the 
	 * expire seconds.
	 *  
	 * @param list
	 * @param expireSeconds
	 * @return 
	 */
	public String popValueFromList(String list, int expireSeconds) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String content = jedisDB.lpop(list);
	  //update the expire seconds if exist
		jedisDB.expire(list, expireSeconds);
		return content;
	}
	
	/**
	 * Acquire the list count
	 * @param list
	 * @return
	 */
	public int acquireMailCount(String list) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		Long l = jedisDB.llen(list);
		if ( l != null ) {
			return l.intValue();
		}
		return 0;
	}
	
	/**
	 * Push simple (text only) message to client.
	 * 
	 * @param roleName
	 * @param deviceToken
	 * @param text
	 * @return
	 */
	public final boolean pushSimpleMessage(final String roleName, 
			final String deviceToken, final String text, int expireSeconds) {
		boolean success = true;
		if ( StringUtil.checkNotEmpty(deviceToken) && iosApnService != null ) {
			GameContext.getInstance().runSmallTask(new Runnable() {
				public void run() {
					//ios device
					PayloadBuilder builder = PayloadBuilder.newPayload();
					builder.badge(1);
					builder.alertBody(text);
					ApnsNotification notif = iosApnService.push(deviceToken, builder.build());
				}
			});
			logger.debug("Push message '{}' to ios device", text);
		} else {
			//android devices
			String offlineBox = getOfflineboxName(roleName);
			pushValueInList(offlineBox, text, 10, expireSeconds);
			logger.debug("Push message '{}' to android device", text);
		}
		return success;
	}
	
	/**
	 * List all offline push message for android clients.
	 * Android client will check the push message everyday by http service.
	 * 
	 * @param roleName
	 * @return
	 */
	public final Collection<String> listSimplePushMessages(final String roleName) {
		String offlineBox = getOfflineboxName(roleName);
		Jedis jedis = JedisFactory.getJedisDB();
		List<String> listSet = jedis.lrange(offlineBox, 0, -1);
		jedis.del(offlineBox);
		return listSet;
	}
	
	/**
	 * Get all iOS inactive devices. This method should not be called 
	 * in gameserver runtime. It may consume a lot of memory.
	 * @return
	 */
	public final Map<String, Date> getIosInactiveDevices() {
		if ( iosApnService != null ) {
			Map<String, Date> inactiveDevices = iosApnService.getInactiveDevices();
			return inactiveDevices;
		} else {
			return null;
		}
	}

	/**
	 * @param builder
	 * @param mail
	 */
	public final MailData parseMailString(String mailMessage) {
		if ( StringUtil.checkNotEmpty(mailMessage) ) {
			String[] fields = mailMessage.split(SEP);
			if ( fields.length >= 5 ) {
				//fromUserName, SEP, today, SEP, mailSub, SEP, mailContent);
				String fromUserName = fields[0];
				String today = fields[1];
				String subject = fields[2];
				String content = fields[3];
				String timestampStr = fields[4];
				String dateTime = today;
				try {
					long timestamp = Long.parseLong(timestampStr);
					dateTime = DateUtil.formatDateTime(new Date(timestamp));
				} catch (NumberFormatException e) {
				}
				String isnew = Boolean.FALSE.toString();
				if ( fields.length>=6 ) {
					isnew = fields[5];
				}
				String gift = null;
				if ( fields.length>=7 ) {
					gift = fields[6];
				}
				
				//Send the message to user immediately
				//SysMessageManager.getInstance().sendClientInfoRawMessage(
				//		user, message, Action.NOOP, Type.NORMAL);
				MailData.Builder mailBuilder = MailData.newBuilder();
				mailBuilder.setFromuser(fromUserName);
				mailBuilder.setSubject(subject);
				mailBuilder.setContent(content);
				mailBuilder.setSentdate(dateTime);
				mailBuilder.setIsnew(Boolean.parseBoolean(isnew));
				if ( gift != null && gift.length()>0 ) {
					String[] giftStrs = gift.split(SEP_GIFT);
					for ( String giftStr : giftStrs ) {
						Reward reward = Reward.fromString(giftStr);
						if ( reward != null ) {
							mailBuilder.addGifts(reward.toGift());
						}
					}
				}
				return mailBuilder.build();
			} else {
				logger.info("#checkMail: the mail {} has wrong field {}", mailMessage, fields.length);
			}
		}
		return null;
	}
	
	/**
	 * Get the Redis "mail" list name for inbox type
	 * @param user
	 * @return
	 */
	public static final String getMailboxName(String userName) {
		return getMailboxName(userName, MailBoxType.inbox);
	}
	
	/**
	 * Get the offline box name
	 * @param roleName
	 * @return
	 */
	public static final String getOfflineboxName(String roleName) {
		return StringUtil.concat(KEY_OFFLINE_KEY, roleName);
	}

	/**
	 * Get the Redis "mail" list name
	 * @param userName
	 * @param type
	 * @return
	 */
	public static final String getMailboxName(String userName, MailBoxType type) {
		return StringUtil.concat(KEY_PREFIX, type, Constant.COLON, userName);
	}
}
