package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GameDataKey;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.db.mongo.TaskManager.TaskHook;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseSysMessage.BseSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardType;
import com.xinqihd.sns.gameserver.transport.XinqiMessage;
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
public class EmailManager{
	
	private static final String EMAIL_FROM = "game@xinqihd.com";
	
	private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);
	
	private ArrayList<Reward> gifts = new ArrayList<Reward>();
	
	private static EmailManager instance = new EmailManager();
	
	/**
	 * Get the singleton instance
	 * @return
	 */
	public static EmailManager getInstance() {
		return instance;
	}
	
	EmailManager() {
		reload();
	}
	
	/**
	 * Reload all data from database into memory.
	 */
	public void reload() {
		/*
		String itemId = GameDataManager.getInstance().getGameDataAsString(GameDataKey.EMAIL_REWARD_ITEMID);
		if ( StringUtil.checkNotEmpty(itemId) ) {
			Reward reward = new Reward();
			reward.setId(itemId);
			reward.setType(RewardType.ITEM);
			reward.setPropCount(1);
			reward.setPropId(itemId);
			gifts.add(reward);			
		} else {
			logger.warn("#EmailManager: failed to find the itemId:{}", itemId);
		}
		*/
		Reward reward = new Reward();
		reward.setPropId("-3");
		reward.setPropLevel(-1);
		reward.setType(RewardType.YUANBAO);
		reward.setPropCount(10);
		gifts.add(reward);
	}
	
	/**
	 * Send a verification email to the user's email account if exist.
	 * 
	 * @param user
	 */
	public void sendVerifyEmail(Account account, IoSession session) {
		if ( StringUtil.checkNotEmpty(account.getEmail()) ) {
			if ( StringUtil.checkValidEmail(account.getEmail()) ) {
				try {
					String emailSmtp = GameDataManager.getInstance().getGameDataAsString(GameDataKey.EMAIL_SMTP, "mail.xinqihd.com");
					HtmlEmail email = new HtmlEmail();
					email.setHostName(emailSmtp);
					email.setAuthenticator(new DefaultAuthenticator(EMAIL_FROM, "xinqi@2012"));
					email.setFrom(EMAIL_FROM);
					String subject = Text.text("email.subject");
					String http = StringUtil.concat(GlobalConfig.getInstance().getStringProperty("runtime.httpserverid"), 
							Constant.PATH_SEP, "verifyemail", Constant.QUESTION, account.get_id().toString());
					String content = Text.text("email.content", account.getUserName(), http);
					email.setSubject(subject);
					email.setHtmlMsg(content);
					email.setCharset("GBK");
					email.addTo(account.getEmail());
					email.send();

					SysMessageManager.getInstance().sendClientInfoRawMessage(session, Text.text("email.sent"), 
							Action.NOOP, Type.NORMAL);
				} catch (EmailException e) {
					logger.debug("Failed to send verify email to user {}'s address:{}", account.getUserName(), account.getEmail());
				}
			} else {
				//email not valid
				SysMessageManager.getInstance().sendClientInfoRawMessage(session, "email.invalid", Action.NOOP, Type.NORMAL);
			}
		} else {
			//no email at all
			SysMessageManager.getInstance().sendClientInfoRawMessage(session, "email.no", Action.NOOP, Type.NORMAL);
		}
	}
	
	/**
	 * Send a verification email to the user's email account if exist.
	 * 
	 * @param user
	 */
	public void sendNormalEmail(String subject, String content, String[] addresses ) {
		if ( StringUtil.checkNotEmpty(subject) && StringUtil.checkNotEmpty(content) ) {
			try {
				String emailSmtp = GameDataManager.getInstance().getGameDataAsString(GameDataKey.EMAIL_SMTP, "mail.xinqihd.com");
				SimpleEmail email = new SimpleEmail();
				email.setHostName(emailSmtp);
				email.setAuthenticator(new DefaultAuthenticator(EMAIL_FROM, "xinqi@2012"));
				email.setFrom(EMAIL_FROM);
				email.setSubject(subject);
				email.setMsg(content);
				email.setCharset("GBK");
				for ( String address : addresses) {
					if ( StringUtil.checkNotEmpty(address) ) {
						email.addTo(address);
					}
				}
				email.send();
			} catch (EmailException e) {
				logger.debug("Failed to send normal email", e);
			}
		}
	}
	
	/**
	 * Verify the user's email.
	 * @param userIdStr
	 */
	public boolean verifyEmail(String accountId) {
		if ( StringUtil.checkNotEmpty(accountId) ) {
			//check if the email is verified
			Account account = AccountManager.getInstance().queryAccountById(accountId);
			boolean verified = account != null && !account.isEmailVerified();
			if ( verified ) {
				account.setEmailVerified(true);
				AccountManager.getInstance().saveAccount(account);
				//Send user gift
				String roleName = AccountManager.getCurrentRoleName(account);
				if ( roleName != null ) {
					BasicUser user = UserManager.getInstance().queryBasicUserByRoleName(roleName);
					if ( user != null ) {
						String content = Text.text("email.reward");
						logger.debug("The user {} 's email is verified.", roleName);
						
						//Send the gift to user
						String subject = Text.text("email.gift.subject");
						MailMessageManager.getInstance().sendMail(
								null, user.get_id(), subject, content, gifts, true);
						
						return true;
					}
				} else {
					logger.warn("Failed to find the roleName for account:{}", account.getUserName());
				}
			} else {
				return false;
			}
		} else {
			logger.debug("#verifyEmail. userId is empty: {}", accountId);
		}
		return false;
	}
	
	/**
	 * Process the forget password scenario
	 * @return
	 */
	public boolean forgetPassword(String roleName, IoSession session) {
		String message = Text.text("forget.fail");
		boolean result = false;
		if ( StringUtil.checkNotEmpty(roleName) ) {
			Account account = AccountManager.getInstance().queryAccountByName(roleName);
			//User user = UserManager.getInstance().queryUserByRoleName(roleName);
			//if ( user != null ) {
			if ( account != null ) {
				String emailAddress = account.getEmail();
				if ( StringUtil.checkValidEmail(emailAddress) ) {
					try {
						Jedis jedis = JedisFactory.getJedisDB();
						String key = StringUtil.concat(Constant.FORGET_PASS_KEY, roleName);
						String tempPassword = jedis.get(key);
						if ( tempPassword != null ) {
							message = Text.text("forget.ok");
							result = true;
						} else {
							String emailSmtp = GameDataManager.getInstance().getGameDataAsString(GameDataKey.EMAIL_SMTP, "xinqihd.com");
							HtmlEmail email = new HtmlEmail();
							email.setHostName(emailSmtp);
							email.setAuthenticator(new DefaultAuthenticator(EMAIL_FROM, "xinqi@2012"));
							email.setFrom(EMAIL_FROM);
							String subject = Text.text("forget.subject");
							tempPassword = String.valueOf(System.currentTimeMillis()%10000);
							String displayRoleName = UserManager.getDisplayRoleName(account.getUserName());
							String content = Text.text("forget.content", displayRoleName, tempPassword);
							email.setSubject(subject);
							email.setHtmlMsg(content);
							email.setCharset("GBK");
							email.addTo(emailAddress);
							email.send();

							jedis.set(key, tempPassword);
							jedis.expire(key, Constant.HALF_HOUR_SECONDS);

							message = Text.text("forget.ok");
							result = true;
						}
					} catch (EmailException e) {
						logger.error("Failed to send email to user's email: {}", emailAddress);
						logger.debug("EmailException", e);
					}
				} else {
					message = Text.text("forget.noemail");
				}
				//StatClient.getIntance().sendDataToStatServer(user, StatAction.ForgetPassword, emailAddress, result);
			} else {
				message = Text.text("forget.nouser");	
			}
		} else {
			message = Text.text("forget.noname");
		}
		
		BseSysMessage.Builder builder = BseSysMessage.newBuilder();
		builder.setMessage(message);
		builder.setSeconds(5000);
		builder.setAction(XinqiSysMessage.Action.NOOP);
		builder.setType(XinqiSysMessage.Type.NORMAL);
		BseSysMessage sysMessage = builder.build();
		
		XinqiMessage response = new XinqiMessage();
		response.payload = sysMessage;
		session.write(response);
		
		return result;
	}
	
}
