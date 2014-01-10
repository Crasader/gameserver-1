package com.xinqihd.sns.gameserver.session;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.GlobalConfig;
import com.xinqihd.sns.gameserver.db.mongo.AccountManager;
import com.xinqihd.sns.gameserver.entity.user.Account;
import com.xinqihd.sns.gameserver.entity.user.UserId;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The CipherManager is used to encrypt & decrypt important data in system.
 * For example, the user's reconnect token. It uses TripleDES as the secure
 * key.
 * 
 * @author wangqi
 *
 */
public class CipherManager {

	private static final String ALGORITHM = "DESede";

	private static final Logger logger = LoggerFactory.getLogger(CipherManager.class);
		
	private static final String ZOO_KEY = "/config/3des";
	
	private static final byte[] REDIS_KEY = "login_3des".getBytes();
	
	private static final CipherManager manager = new CipherManager(); 
	
	//Default 1 hours timeout
	private static int timeout = 3600000;
	
	private SecretKey key;
	
	private CipherManager() {
    // Check to see whether there is a provider that can do TripleDES
    // encryption. If not, explicitly install the SunJCE provider.
    try {
    	timeout = GlobalConfig.getInstance().getIntProperty("cipher.timeout");
    	
      SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(ALGORITHM);
      
      //Read the secured key from Redis
      this.key = keyfactory.generateSecret(new DESedeKeySpec("我的这个Key你能猜得到吗哈哈哈哈哈".getBytes()));
      /*
      Jedis jedisDB = JedisFactory.getJedisDB();
      if ( jedisDB != null && jedisDB.isConnected() ) {
      	byte[] rawkey = null;
      	if ( !jedisDB.exists(REDIS_KEY) ) {
      		logger.info("Generate a new 3DES secureKey.");
        	KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
        	this.key = keygen.generateKey();
        	//Save the key in Redis
          DESedeKeySpec keyspec = (DESedeKeySpec) keyfactory.getKeySpec(this.key, DESedeKeySpec.class);
          rawkey = keyspec.getKey();
          jedisDB.set(REDIS_KEY, rawkey);
          jedisDB.expire(REDIS_KEY, timeout/1000);
      	} else {
      		rawkey = jedisDB.get(REDIS_KEY);
      		logger.info("Read the 3DES secureKey from Redis. ");
      		DESedeKeySpec keyspec = new DESedeKeySpec(rawkey);
      		this.key = keyfactory.generateSecret(keyspec);
      	}
      	//For security reason.
      	Arrays.fill(rawkey, (byte)0);
      	rawkey = null;
      } else {
      	logger.warn("Redis is unavailable. It will use a random 3-DES key by default.");
      	KeyGenerator keygen = KeyGenerator.getInstance(ALGORITHM);
      	this.key = keygen.generateKey();
      }
      */
    } catch (Exception e) {
      // An exception here probably means the JCE provider hasn't
      // been permanently installed on this system by listing it
      // in the $JAVA_HOME/jre/lib/security/java.security file.
      // Therefore, we have to install the JCE provider explicitly.
      logger.warn("Exception", e);
    }
	}
	
	/**
	 * Change the default timeout value, for test purpose.
	 * @param timeout
	 */
	public static final void setDefaultTimeout(int timeout) {
		CipherManager.timeout = timeout;
	}
	
	/**
	 * Get the Singleton instance.
	 * @return
	 */
	public static final CipherManager getInstance() {
		return manager;
	}
	
	/**
	 * Generate the encrypted user token for client to 
	 * do reconnect.
	 * 
	 * @return
	 */
	public String generateEncryptedUserToken(UserId userId) {
		byte[] userIdBytes = userId.getInternal();
		long timeMillis = System.currentTimeMillis();
		ByteBuffer buf = ByteBuffer.allocate(userIdBytes.length + 8);
		buf.putLong(timeMillis);
		buf.put(userIdBytes);
		byte[] finalBytes = buf.array();
		
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, this.key);
			byte[] encrypted = cipher.doFinal(finalBytes);
			String base64 = new String(Base64.encodeBase64(encrypted));
			return base64;
		} catch (Exception e) {
			logger.warn("generateEncryptedUserToken exception", e);
		}
		return Constant.EMPTY;
	}
	
	public String generateEncryptedUserToken(String id) {
		byte[] userIdBytes = id.getBytes();
		long timeMillis = System.currentTimeMillis();
		ByteBuffer buf = ByteBuffer.allocate(userIdBytes.length + 8);
		buf.putLong(timeMillis);
		buf.put(userIdBytes);
		byte[] finalBytes = buf.array();
		
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, this.key);
			byte[] encrypted = cipher.doFinal(finalBytes);
			String base64 = new String(Base64.encodeBase64(encrypted));
			return base64;
		} catch (Exception e) {
			logger.warn("generateEncryptedUserToken exception", e);
		}
		return Constant.EMPTY;
	}
	
	/**
	 * Check if the token is valid. True if it is. False otherwise.
	 * @param token
	 * @return
	 */
	public UserId checkEncryptedUserToken(String token) {
		if ( !StringUtil.checkNotEmpty(token) ) return null;
		byte[] encrypted = Base64.decodeBase64(token.getBytes());
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, this.key);
			byte[] textBytes = cipher.doFinal(encrypted);
			ByteBuffer buf = ByteBuffer.wrap(textBytes);
			long timeMillis = buf.getLong();
			byte[] userIdBytes = new byte[buf.remaining()];
			buf.get(userIdBytes);
			UserId userId = UserId.fromBytes(userIdBytes);
			if ( userId != null && timeMillis + CipherManager.timeout > System.currentTimeMillis() ) {
				return userId;
			} else {
				logger.debug("User's token is timeout");
			}
		} catch (Exception e) {
			logger.debug("checkEncryptedUserToken exception: {} ", e.getMessage());
			logger.debug("User's token is invalid");
		}
		return null;
	}
	
	/**
	 * Check if the token is valid. True if it is. False otherwise.
	 * @param token
	 * @return
	 */
	public Account checkEncryptedAccountToken(String token) {
		if ( !StringUtil.checkNotEmpty(token) ) return null;
		byte[] encrypted = Base64.decodeBase64(token.getBytes());
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, this.key);
			byte[] textBytes = cipher.doFinal(encrypted);
			ByteBuffer buf = ByteBuffer.wrap(textBytes);
			long timeMillis = buf.getLong();
			byte[] accountIdBytes = new byte[buf.remaining()];
			buf.get(accountIdBytes);
			if ( accountIdBytes != null && accountIdBytes.length>0 && 
					timeMillis + CipherManager.timeout > System.currentTimeMillis() ) {
				String accountId = new String(accountIdBytes);
				Account account = AccountManager.getInstance().queryAccountById(accountId);
				return account;
			} else {
				logger.debug("Account's token is timeout");
			}
		} catch (Exception e) {
			logger.debug("checkEncryptedUserToken exception: {} ", e.getMessage());
			logger.debug("Account's token is invalid");
		}
		return null;
	}
}
