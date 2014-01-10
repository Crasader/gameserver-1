package com.xinqihd.sns.gameserver.social;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.RowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.JndiContextKey;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.config.GlobalConfigKey;
import com.xinqihd.sns.gameserver.db.mongo.LoginManager;
import com.xinqihd.sns.gameserver.db.mysql.MysqlUtil;
import com.xinqihd.sns.gameserver.util.MessageFormatter;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Sync the local user's game account with discuz.
 * 
 * @author wangqi
 * 
 */
public class DiscuzSync {
	
	public static final String SQL_REG = 
			"replace into {} (username, password, email, regip, regdate, salt) values " +
			"('{}', '{}', '{}', '{}', {}, '{}')";
	
	public static final String SQL_QUERYID = 
			"select uid from {} where username = '{}'";
	
	public static final String SQL_REG_ACTIVE = 
			"replace into {} (uid, email, username, password, groupid, regdate ) values " +
			"({}, '{}', '{}', '{}', 10, {})";
	
	private static final Logger logger = LoggerFactory.getLogger(DiscuzSync.class);
	
	public static DiscuzSync instance = new DiscuzSync();
	
	private String ucenterTable = "ultrax.pre_ucenter_members";
	private String commonTable = "ultrax.pre_common_member";
	
	public DiscuzSync() {
		reload();
	}
	
	public static DiscuzSync getInstance() {
		return instance;
	}
	
	public void reload() {
		String table = GlobalConfig.getInstance().getStringProperty(GlobalConfigKey.mysql_discuz_table_name);
		if ( StringUtil.checkNotEmpty(table) ) {
			ucenterTable = table;
		}
	}
	/**
	 * Register a new user with given information
	 * 
	 * @param username
	 * @param password
	 * @param email
	 */
	public void register(String username, String password, String email, String ip) {
		if ( email == null ) {
			email = Constant.EMPTY;
		}
		if ( ip == null ) {
			ip = "hidden";
		}
		int regdate = (int)(System.currentTimeMillis()/1000);
		String salt = LoginManager.getInstance().getRandomRoleName();
		String encrypted = getDiscuzPwdByUserPwdAndSalt(password, salt);
		String sql = MessageFormatter.arrayFormat(SQL_REG, ucenterTable, username, encrypted, email, ip, regdate, salt).getMessage();
		MysqlUtil.executeUpdate(sql, JndiContextKey.mysql_discuz_db.name());
		sql = MessageFormatter.arrayFormat(SQL_QUERYID, ucenterTable, username).getMessage();
		ArrayList<Map<String, String>> rows = MysqlUtil.executeQueryAllRows(sql, JndiContextKey.mysql_discuz_db.toString());
		/**
		 * The interface is changed. The function is disabled
		 * wangqi 2012-10-23
		 */
		/*
		if ( rows != null ) {
			try {
				if ( rowSet.next() ) {
					String uid = rowSet.getString("uid");
					//uid, email, username, password, groupid, regdate 
					sql = MessageFormatter.arrayFormat(SQL_REG_ACTIVE, commonTable, uid, email, username, encrypted, regdate).getMessage();
					MysqlUtil.executeUpdate(sql, JndiContextKey.mysql_discuz_db.name());
				} else {
					logger.warn("Failed to find uid by username {} in discuz.", username);
				}
			} catch (SQLException e) {
				logger.info("Failed to query discuz uid. Exception: {}", e.toString());
			}
		}
		*/
	}

	/**
	 * Discuz的用户密码存在在 pre_ucenter_members 表中
	 * 注册新用户的方法:
	 * insert into pre_ucenter_members (username, password, email, regip, regdate, salt) values ('test001', '0990eac1d2a41cbab73d78fedb05a3b4', 'test@xinqihd.com', 'hidden', 1343788965, '553a99')
	 * 
	 * 该测试在Discuz x 1.5 *
	 * 
	 * @author Sean
	 * @param args
	 * @throws NoSuchAlgorithmException
	 */
	public static String getDiscuzPwdByUserPwdAndSalt(String password, String salt) {
		// admin为dz_uc_members表的password字段未加密前的明文
		// 用MD5第一次加密
		try {
			String pwd = StringUtil.encryptMD5(password);
			// 将加密后的密文加上dz_uc_members表的salt字段
			// 因为DZ加密是使用MD5加密后加上随机码再次加密，所以需要还原加密
			pwd = pwd + salt;
			return StringUtil.encryptMD5(pwd);
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}
}
