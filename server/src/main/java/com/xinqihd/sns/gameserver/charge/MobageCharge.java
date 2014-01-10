package com.xinqihd.sns.gameserver.charge;

import static com.xinqihd.sns.gameserver.util.CommonUtil.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xinqihd.payment.mobage.Consumer;
import com.xinqihd.payment.mobage.MbgaMstgProvider;
import com.xinqihd.payment.mobage.MbgaOauthUtil;
import com.xinqihd.payment.mobage.MbgaProvider;
import com.xinqihd.payment.mobage.MbgaSandboxProvider;
import com.xinqihd.payment.mobage.Token;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;
import com.xinqihd.sns.gameserver.script.ScriptHook;
import com.xinqihd.sns.gameserver.script.ScriptManager;
import com.xinqihd.sns.gameserver.transport.http.HttpMessage;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * Support Mobage charge process.
 * @author wangqi
 *
 */
public final class MobageCharge {
	
	private static final String FIELD_VERIFIER = "verifier";

	private static final String FIELD_ACCTOKEN = "acctoken";
	private static final String FIELD_ACCSECRET = "accsecret";

	private static final String FIELD_TMPTOKEN = "tmptoken";
	private static final String FIELD_TMPSECRET = "tmpsecret";

	private static final Logger logger = LoggerFactory.getLogger(MobageCharge.class);

	private static final String KEY_CONSUMER_KEY = "251d222ab68410466c4d";
	private static final String KEY_CONSUMER_SECRET = "74ac35ee9c5082cc143431e6aa9cc1e631d9";

	private static final String SESSION_TMP_TOKEN_KEY = "sessionTmpToken";
	private static final String SESSION_ACCESS_TOKEN_KEY = "sessionAccessToken";
	private static final String SESSION_VERIFIER_KEY = "sessionVerifier";
	
	public static final Consumer CONSUMER = new Consumer();
	/**
	 * SANDBOX or PRODUCTION
	 */
	//public static final MbgaProvider PROVIDER = new MbgaSandboxProvider();
	public static final MbgaProvider PROVIDER = new MbgaMstgProvider();
	
	private static MobageCharge instance = new MobageCharge();
	
	private MobageCharge() {
		CONSUMER.setKey(KEY_CONSUMER_KEY);
		CONSUMER.setSecret(KEY_CONSUMER_SECRET);
	}
	
	public static final MobageCharge getInstance() {
		return instance;
	}

	/**
	 * The game client will send tmp token to gameserver as the first step.
	 * 
	 */
	public HttpMessage processTmpToken(HttpMessage httpMessage, String variable) {
		logger.debug("Tmptoken request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("tmptoken HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("tmptoken HTTP request content is null");
					success = false;
				}
			}
		}
		HashMap map = parseHttpParams(content);
		String userName = (String)(map.get("username"));

		Token tmpToken = MbgaOauthUtil.getTemporaryToken(PROVIDER, CONSUMER);
		
		if ( tmpToken == null ) {
			HttpMessage response = new HttpMessage();
			response.setResponseCode("501".getBytes());
			response.setResponseContent("tmpToken is null".getBytes());
			return response;
		} else {
			StringBuilder buf = new StringBuilder();
			if ( tmpToken.getToken() != null && tmpToken.getStatus().equals("200") ) {
				storeSession(userName, tmpToken, null, null);
				buf.append(tmpToken.getToken());
				
				HttpMessage response = new HttpMessage();
				response.setResponseContent(buf.toString().getBytes());
				return response;
			} else {
				if ( tmpToken.getStatus() != null ) {
					buf.append(tmpToken.getStatus()).append("\r\n");
				}
				if ( tmpToken.getErrorMessage() != null ) {
					buf.append(tmpToken.getErrorMessage());
				}
				HttpMessage response = new HttpMessage();
				response.setResponseContent(buf.toString().getBytes());
				return response;
			}
		}
		
	}
	
	/**
	 * 
	 * @param session
	 * @param httpMessage
	 * @param variable
	 * @return
	 */
	public HttpMessage processAccessToken(HttpMessage httpMessage, String variable) {
		logger.debug("accessToken request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("accessToken HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("accessToken HTTP request content is null");
					success = false;
				}
			}
		}
		
		HashMap map = parseHttpParams(content);
		String userName = (String)(map.get("username"));
		String verifier = (String)(map.get(FIELD_VERIFIER));
		
		Map<String, String> sessionMap = getSession(userName);
		String tmpTokenStr = sessionMap.get(FIELD_TMPTOKEN);
		String tmpTokenSecretStr = sessionMap.get(FIELD_TMPSECRET);

		StringBuilder buf = new StringBuilder();
		if ( StringUtil.checkNotEmpty(tmpTokenStr) ) {
			if ( StringUtil.checkNotEmpty(verifier) ) {
				Token accessToken = MbgaOauthUtil.getAccessToken(PROVIDER, CONSUMER, 
						tmpTokenStr, tmpTokenSecretStr, verifier);
				storeSession(userName, null, accessToken, null);
				if ( accessToken != null ) {
					if ( accessToken.getToken() != null ) {
						buf.append(accessToken.getToken()).append("\r\n");
					}
					if ( accessToken.getErrorMessage() != null ) {
						buf.append(accessToken.getErrorMessage());
					}
					HttpMessage response = new HttpMessage();
					response.setResponseContent(buf.toString().getBytes());
					return response;
				} else {
					HttpMessage response = new HttpMessage();
					response.setResponseCode("501".getBytes());
					response.setResponseContent("accessToken is not accquired.".getBytes());
					return response;
				}
			} else {
				HttpMessage response = new HttpMessage();
				response.setResponseCode("502".getBytes());
				response.setResponseContent("verifyStr is not found".getBytes());
				return response;
			}
		} else {
			HttpMessage response = new HttpMessage();
			response.setResponseCode("503".getBytes());
			response.setResponseContent("tmpTokenStr is not found".getBytes());
			return response;
		}
	
	}
	
	/**
	 * Process the transaction.
	 * @param session
	 * @param httpMessage
	 * @param variable
	 */
	public HttpMessage processTransaction(HttpMessage httpMessage, String variable) {
		logger.debug("transaction request is coming");
		String content = null;
		boolean success = true;
		if ( httpMessage == null ) {
			logger.warn("transaction HTTP is null");
			success = false;
		} else {
			content = httpMessage.getRequestContent();
			if ( content == null || content.length()==0 ) {
				if ( variable != null ) {
					content = variable;
				} else {
					logger.warn("transaction HTTP request content is null");
					success = false;
				}
			}
		}
		HashMap map = parseHttpParams(content);
		String userName = (String)(map.get("username"));
		String transactionId = (String)(map.get("transactionId"));

		StringBuilder buf = new StringBuilder();
		// get consumer and token and verifier
		Map<String, String> sessionMap = getSession(userName);
		String accTokenStr = sessionMap.get(FIELD_ACCTOKEN);
		String accTokenSecretStr = sessionMap.get(FIELD_ACCSECRET);
		String verifier = sessionMap.get(FIELD_VERIFIER);
		Token token = new Token();
		token.setToken(accTokenStr);
		token.setSecret(accTokenSecretStr);
				
		try {
			String json = MbgaOauthUtil.openTransaction(transactionId, CONSUMER, token, verifier);
			if ( json != null ) {
				if ( MbgaOauthUtil.commitTransaction(transactionId, CONSUMER, token, verifier) ) {
					//do charge
					ScriptManager.getInstance().runScript(ScriptHook.CHARGE_MOBAGE, json, userName);
				}
			}
		} catch (Exception e) {
			logger.warn("Error:", e);
		}
		
		HttpMessage response = new HttpMessage();
		response.setResponseContent(buf.toString().getBytes());
		return response;
	}

	/**
	 * Store the information into session
	 * @param userName
	 * @param tmpTokenStr
	 * @param accessTokenString
	 * @param verifier
	 */
	private void storeSession(String userName, Token tmpToken, Token accessToken, String verifier) {
		String key = StringUtil.concat("mobage:", userName);
		Jedis jedis = JedisFactory.getJedis();
		if ( tmpToken != null ) {
			jedis.hset(key, FIELD_TMPTOKEN, tmpToken.getToken());
			jedis.hset(key, FIELD_TMPSECRET, tmpToken.getSecret());
		}
		if ( accessToken != null ) {
			jedis.hset(key, FIELD_ACCTOKEN, accessToken.getToken());
			jedis.hset(key, FIELD_ACCSECRET, accessToken.getSecret());
		}
		if ( StringUtil.checkNotEmpty(verifier) ) {
			jedis.hset(key, FIELD_VERIFIER, verifier);
		}
		jedis.expire(key, 600);
	}
	
	/**
	 * Retrieve the data from session.
	 * @param userName
	 * @return
	 */
	private Map<String, String> getSession(String userName) {
		String key = StringUtil.concat("mobage:", userName);
		Jedis jedis = JedisFactory.getJedis();
		return jedis.hgetAll(key);
	}
}
