package com.xinqihd.sns.gameserver.db.mongo;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailList.BseMailList;
import com.xinqihd.sns.gameserver.proto.XinqiBseMailReceive.BseMailReceive;
import com.xinqihd.sns.gameserver.proto.XinqiMailData.MailData;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Action;
import com.xinqihd.sns.gameserver.proto.XinqiSysMessage.Type;
import com.xinqihd.sns.gameserver.reward.PickRewardResult;
import com.xinqihd.sns.gameserver.reward.Reward;
import com.xinqihd.sns.gameserver.reward.RewardManager;
import com.xinqihd.sns.gameserver.session.SessionKey;
import com.xinqihd.sns.gameserver.transport.stat.StatAction;
import com.xinqihd.sns.gameserver.transport.stat.StatClient;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;

/**
 * This is a secure system to protect the game eco-system runs normally.
 * Some times, the severe bug in game system will let hackers to gain
 * great yuanbao, golden or exp to break economics in game. This system
 * put a limit on the amount every day.
 * 
 * @author wangqi
 * 
 */
public class SecureLimitManager {

	private static final Logger logger = LoggerFactory
			.getLogger(SecureLimitManager.class);

	private static SecureLimitManager instance = new SecureLimitManager();
	
	//The key's prefix used in Redis
	private static final String KEY_PREFIX = "secure:";
	
	private boolean disableSecureChecking = false;
	
	public static enum LimitType {
		YUANBAO,
		GOLDEN,
		EXP,
	}
	

	SecureLimitManager() {
	}

	/**
	 * Get a singleton instance for this manager.
	 * 
	 * @return
	 */
	public static final SecureLimitManager getInstance() {
		return instance;
	}
	
	/**
	 * @return the disableSecureChecking
	 */
	public boolean isDisableSecureChecking() {
		return disableSecureChecking;
	}

	/**
	 * @param disableSecureChecking the disableSecureChecking to set
	 */
	public void setDisableSecureChecking(boolean disableSecureChecking) {
		this.disableSecureChecking = disableSecureChecking;
	}

	/**
	 * Set the new value for given user.
	 * 
	 * @param user
	 * @param type
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	public boolean setValueForUser(User user, LimitType type, 
			int oldValue, int newValue) {
		
		if ( disableSecureChecking ) return true;
		
		String roleName = user.getUsername();
		Jedis jedis = JedisFactory.getJedis();
		String keyName = getSecureLimitName(roleName);
		String valueStr = jedis.hget(keyName, type.toString());
		int limit = getSecureLimit(type);
		int value = 0;
		if ( valueStr != null ) {
			value = StringUtil.toInt(valueStr, 0);
			if ( value > limit ) {
				String message = getSecureLimitMessage(type, value);
				SysMessageManager.getInstance().sendClientInfoRawMessage(
						user.getSessionKey(), message, Action.NOOP, Type.NORMAL);
				return false;
			}
		} else {
			//First time
			jedis.hset(keyName, type.toString(), String.valueOf(value));
			jedis.expire(keyName, DateUtil.getSecondsToNextDateUnit(
					DateUnit.DAILY, Calendar.getInstance()));
		}
		boolean passCheck = true;
		int gain = newValue - oldValue;
		if ( gain > 0 ) {
			value += gain;
			if ( limit > 0 ) {
				if ( value >= limit ) {
					passCheck = false;
					String message = getSecureLimitMessage(type, value);
					SysMessageManager.getInstance().sendClientInfoRawMessage(
							user.getSessionKey(), message, Action.NOOP, Type.NORMAL);
					logger.warn("User '{}' {} exceeds the secure limit {}", 
							new Object[]{roleName, type, limit});
					
					String adminEmails = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.admin_email);
					if ( StringUtil.checkNotEmpty(adminEmails) ) {
						final String[] addresses = adminEmails.split(Constant.COMMA);
						final String content = MessageFormatter.arrayFormat(
								"Babywar Secure Warning:\n User '{}''s  {} limit grow from {} to {}. The limit is {} ", 
								new Object[]{roleName, type, oldValue, newValue, value, limit}).getMessage();
						GameContext.getInstance().runSmallTask(new Runnable() {
							public void run() {
								EmailManager.getInstance().sendNormalEmail(content, content, addresses);
							}
						});
					}
				}
				jedis.hincrBy(keyName, type.toString(), gain);
			}
		}
		return passCheck;
	}
	
	/**
	 * Get the Redis "mail" list name for inbox type
	 * @param user
	 * @return
	 */
	public static final String getSecureLimitName(String roleName) {
		return StringUtil.concat(KEY_PREFIX, roleName);
	}
	
	/**
	 * Get the given secure limit.
	 * @param type
	 * @return
	 */
	public static final int getSecureLimit(LimitType type) {
		int value = 0;
		switch ( type ) {
			case EXP:
				value = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.SECURE_LIMIT_EXP_DAILY, 100000);
				break;
			case GOLDEN:
				value = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.SECURE_LIMIT_GOLDEN_DAILY, 90000);
				break;
			case YUANBAO:
				value = GameDataManager.getInstance().getGameDataAsInt(
						GameDataKey.SECURE_LIMIT_YUANBAO_DAILY, 200000);
				break;
			default:
				value = -1;
				break;
		}
		return value;
	}
	
	/**
	 * Get the given secure limit.
	 * @param type
	 * @return
	 */
	public static final String getSecureLimitMessage(LimitType type, int value) {
		String text = null;
		switch ( type ) {
			case EXP:
				text = Text.text("secure.limit.exp", value);
				break;
			case GOLDEN:
				text = Text.text("secure.limit.golden", value);
				break;
			case YUANBAO:
				text = Text.text("secure.limit.yuanbao", value);
				break;
			default:
				break;
		}
		return text;
	}
}
