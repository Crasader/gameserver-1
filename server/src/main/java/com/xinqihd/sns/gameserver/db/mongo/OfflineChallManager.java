package com.xinqihd.sns.gameserver.db.mongo;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.entity.user.User;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.DateUtil.DateUnit;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * It is used for store and query the offline challenge data.
 * 
 * @author wangqi
 *
 */
public class OfflineChallManager {
	
	public static final String OFFLIEN_CHALL_KEY = "offline:chall:";

	private static final Logger logger = LoggerFactory.getLogger(OfflineChallManager.class);
	
	private static final OfflineChallManager instance = new OfflineChallManager();
	
	OfflineChallManager() {
		
	}
	
	public void reload() {
		
	}
	
	public final static OfflineChallManager getInstance() {
		return instance;
	}
	
	/**
	 * The data structure in Redis
	 * 
	 * key: offline:chall:<yesterday>
	 * value: 
	 *    timeStr,<fromUserRoleName>,win
	 * where timeStr is the time that challenge occurred,
	 *  fromUserRoleName is the challenger,
	 *  win is true means you win the game.
	 * 
	 * 
	 * @param fromUser The user who starts the challenge
	 * @param toUser The user who accept the challenge
	 * @param win The toUser win or lose.
	 * @param currentTimeMillis The current time millis
	 * 
	 */
	public final void storeChallengeInfo(User fromUser, User toUser, boolean win, long currentTimeMillis) {
		if ( fromUser == null || toUser == null ) {
			logger.debug("#storeChallengeInfo: No from user or to user.");
			return;
		}
		if ( fromUser.get_id().equals(toUser.get_id() )) {
			return;
		}
		String dateStr = DateUtil.getToday(currentTimeMillis);
		String fromRoleName = fromUser.getRoleName();
		String toRoleName = toUser.getRoleName();
		String key = getRedisKeyName(dateStr, toRoleName);
		Jedis jedis = JedisFactory.getJedis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTimeMillis);
		
		String timeStr = DateUtil.formatDateTime(cal.getTime());
		String value = StringUtil.concat(timeStr, Constant.COMMA, fromRoleName, Constant.COMMA, win);
		
		jedis.lpush(key, value);
		//store the data for two days.
		int seconds = DateUtil.getSecondsToNextDateUnit(DateUnit.DAILY, cal)+86400;
		jedis.expire(key, seconds);
	}
	
	/**
	 * Query the user's yesterday challenge data.
	 * @param user
	 * @param currentTimeMillis
	 * @return new int[]{winCount, loseCount};
	 */
	public final int[] queryChallengeInfo(User user, String dateStr) {
		String roleName = user.getRoleName();
		String key = getRedisKeyName(dateStr, roleName);
		Jedis jedis = JedisFactory.getJedis();
		List<String> infos = jedis.lrange(key, 0, -1);
		int winCount = 0;
		int loseCount = 0;
		StringBuilder buf = new StringBuilder();
		if ( infos != null ) {
			for ( String value : infos ) {
				String[] fields = value.split(Constant.COMMA);
				if ( fields.length >= 3 ) {
					String timeStr = fields[0];
					String fromRoleName = fields[1];
					boolean win = Boolean.parseBoolean(fields[2]);
					String text = null;
					if ( win ) {
						winCount++;
						text = Text.text("offline.chall.user.win", fromRoleName, timeStr);
					} else {
						loseCount++;
						text = Text.text("offline.chall.user.fail", fromRoleName, timeStr);
					}
					buf.append(text).append("\n");
				}
			}
		}
		int totalCount = winCount + loseCount;
		if ( totalCount > 0 ) {
			String subject = Text.text("offline.chall.subject", totalCount, winCount, loseCount);
			String content = buf.toString();
			MailMessageManager.getInstance().sendMail(null, user.get_id(), subject, content, null, true);
			logger.debug(content);
		}
		
		return new int[]{winCount, loseCount};
	}
	
	/**
	 * Clean all the challenge data for given user.
	 * @param user
	 * @param currentTimeMillis
	 */
	public final void cleanChallengeData(User user, String dateStr) {
		String roleName = user.getRoleName();
		String key = OfflineChallManager.getRedisKeyName(dateStr, roleName);
		Jedis jedis = JedisFactory.getJedis();
		jedis.del(key);
	}
	
	/**
	 * 
	 * @param currentTimeMillis
	 * @return
	 */
	public static final String getRedisKeyName(String dateStr, String roleName) {
		String key = StringUtil.concat(OFFLIEN_CHALL_KEY, dateStr, Constant.COLON, roleName);
		return key;
	}

}
