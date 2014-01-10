package com.xinqihd.payment.mobage;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.xinqihd.payment.mobage.bank.debit.Transaction;
import com.xinqihd.sns.gameserver.charge.MobageCharge;
import com.xinqihd.sns.gameserver.util.RandomStringUtils;

/**
 * 
 * @author yangxia
 *
 */
public class MbgaOauthUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(MbgaOauthUtil.class);

	private static final String UTF8 = "UTF-8";
	private static final String OAUTH_VERSION = "1.0";
	private static final String SIGNATURE_METHOD = "HMAC-SHA1";

	private static final String OAUTH_TOKEN = "oauth_token";
	private static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	
	private static final String bankTransaction = "/bank/debit/@app";


	public static Token getTemporaryToken(MbgaProvider provider, Consumer consumer) {
		Token token = new Token();
		Map<String, Object> map = new TreeMap<String, Object>();

		String uri = provider.getRequestTokenEndpoint();

		try {

			// HttpResponse response = httpClient.execute(httppost);

			URL url = new URL(uri);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");

			String authorization = createOauthHeaderWhenTempToken(uri,
					consumer.getKey(), consumer.getSecret());
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length", "0");
			conn.setRequestProperty("Authorization", authorization);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
			conn.getOutputStream().close();

			Integer status = conn.getResponseCode();// response.getStatusLine().getStatusCode();
			logger.debug("status:{}, authorization:{}", status, authorization);
			String result = Utils.getStreamAsString(conn.getInputStream());// EntityUtils.toString(response.getEntity());
			logger.debug(result);
			String decodeResult = URLDecoder.decode(result, UTF8);
			map.putAll(unjoin(decodeResult, "&"));
			map.put("status", status);

			token.setToken((String) map.get(OAUTH_TOKEN));
			token.setSecret((String) map.get(OAUTH_TOKEN_SECRET));
			token.setStatus(status.toString());
			if (status != 200) {
				token.setErrorMessage(result);
			}
			return token;
		} catch (Exception e) {
			token.setErrorMessage(e.toString());
			e.printStackTrace();
			return token;
		}
	}

	private static Map<String, Object> unjoin(String source, String separator) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] split = source.split(separator);
		for (String s : split) {
			int index = s.indexOf("=");
			if (index < 0) {
				map.put(s, "");
			} else {
				String before = s.substring(0, index);
				String after = s.substring(index + 1);
				map.put(before, after);
			}
		}
		return map;
	}

	private static String createOauthHeaderWhenTempToken(String uri,
			String consumerKey, String consumerSecret) {
		Map<String, String> map = getOauthParamMap("oob", consumerKey, null, null);

		// generate base string
		String oauthParamString = joinParam(map, "&", true, false);
		String baseString = generateBaseString("POST", uri, oauthParamString);

		// generate signature
		String signature = SignUtil.signByHmacSHA1(baseString, consumerSecret, "");

		map.put("realm", "");
		map.put("oauth_signature", signature);
		String oauthHead = joinParam(map, ",", true, true);

		return "OAuth " + oauthHead;
	}

	private static String createOauthHeaderWhenAccessToken(String uri,
			String consumerKey, String consumerSecret, String oauthToken,
			String verifier, String tokenSecret) {
		Map<String, String> map = getOauthParamMap(null, consumerKey, oauthToken,
				verifier);

		// generate base string
		String oauthParamString = joinParam(map, "&", true, false);
		String baseString = generateBaseString("POST", uri, oauthParamString);

		// generate signature
		String signature = SignUtil.signByHmacSHA1(baseString, consumerSecret,
				tokenSecret);
		// logger.debug(signature);

		map.put("realm", "");
		map.put("oauth_signature", signature);
		String oauthHead = joinParam(map, ",", true, true);

		return "OAuth " + oauthHead;
	}

	private static String joinParam(Map<String, String> treeMap,
			String separator, boolean needUrlEncode, boolean needQuatation) {
		int size = treeMap.size();
		String[] keyValuePairList = new String[size];
		int index = 0;
		Set<String> keySet = treeMap.keySet();
		for (String key : keySet) {
			String value = treeMap.get(key);
			try {
				if (needUrlEncode) {
					value = URLEncoder.encode(value, UTF8);
					key = URLEncoder.encode(key, UTF8);
				}
				if (needQuatation) {
					value = "\"" + value + "\"";
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			keyValuePairList[index] = Utils.join(new String[] { key, value }, "=");
			index++;
		}
		String re = Utils.join(keyValuePairList, separator);
		return re;
	}

	private static TreeMap<String, String> getOauthParamMap(String callBack,
			String consumerKey, String oauthToken, String verifier) {
		TreeMap<String, String> map = new TreeMap<String, String>();
		if (callBack != null) {
			map.put("oauth_callback", callBack);
		}
		if (consumerKey != null) {
			map.put("oauth_consumer_key", consumerKey);
		}
		if (oauthToken != null) {
			map.put("oauth_token", oauthToken);
		}
		if (verifier != null) {
			map.put("oauth_verifier", verifier);
		}
		map.put("oauth_nonce", random(16));
		map.put("oauth_signature_method", SIGNATURE_METHOD);
		map.put("oauth_timestamp", String.valueOf(now()));
		map.put("oauth_version", OAUTH_VERSION);
		return map;
	}

	private static String random(int count) {
		return RandomStringUtils.randomAlphanumeric(count);
	}

	private static long now() {
		Date date = new Date();
		long b = TimeUnit.MILLISECONDS.toSeconds(date.getTime());
		return b;
	}

	private static String generateBaseString(String method, String uri,
			String oauthParam) {
		try {
			method = URLEncoder.encode(method, UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			uri = URLEncoder.encode(uri, UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			oauthParam = URLEncoder.encode(oauthParam, UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return Utils.join(new String[] { method, uri, oauthParam }, "&");

	}

	public static Token getAccessToken(MbgaProvider provider, Consumer consumer,
			String tmpTokenStr, String tmpTokenSecretStr, String verifier) {
		Token accessToken = new Token();
		Map<String, Object> map = new TreeMap<String, Object>();

		// HttpClient httpClient = new DefaultHttpClient();
		String uri = provider.getAccessTokenEndpoint();
		// HttpPost httppost = new HttpPost(uri);
		String authorization = createOauthHeaderWhenAccessToken(uri,
				consumer.getKey(), consumer.getSecret(), tmpTokenStr, verifier,
				tmpTokenSecretStr);
		// httppost.setHeader("Authorization", authorization);
		// httppost.setHeader("Content-Type", "application/json; charset=utf8");
		try {

			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			
			conn.setDoOutput(true);
			conn.setRequestProperty("Authorization", authorization);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
			conn.getOutputStream().close();

			// HttpResponse response = httpClient.execute(httppost);
			Integer status = conn.getResponseCode();// response.getStatusLine().getStatusCode();
			String result = Utils.getStreamAsString(conn.getInputStream());// EntityUtils.toString(response.getEntity());
			logger.debug(result);
			String decodeResult = URLDecoder.decode(result, UTF8);
			map.putAll(unjoin(decodeResult, "&"));
			map.put("status", status);
			accessToken.setToken((String) map.get(OAUTH_TOKEN));
			accessToken.setSecret((String) map.get(OAUTH_TOKEN_SECRET));
			accessToken.setStatus(status.toString());
			if (status != 200) {
				accessToken.setErrorMessage(result);
			}
			return accessToken;
		} catch (Exception e) {
			logger.warn("Failed to get access token", e);
			return null;
		}
	}

	private static String createAuthHeaderForResource(String method, String uri,
			TreeMap<String, String> paramMapForSign, Consumer consumer, Token token,
			String verifier) {
		Map<String, String> mapForBaseString = getOauthParamMap(null,
				consumer.getKey(), token.getToken(), verifier);
		Map<String, String> mapForHeader = new TreeMap<String, String>(
				mapForBaseString);
		if (paramMapForSign != null && !paramMapForSign.isEmpty()) {
			mapForBaseString.putAll(paramMapForSign);
		}
		// generate base string
		String oauthParamString = joinParam(mapForBaseString, "&", true, false);
		String baseString = generateBaseString(method, uri, oauthParamString);

		// generate signature
		String signature = SignUtil.signByHmacSHA1(baseString,
				consumer.getSecret(), token.getSecret());

		mapForHeader.put("realm", "");
		mapForHeader.put("oauth_signature", signature);
		String oauthHead = joinParam(mapForHeader, ",", true, true);

		return "OAuth " + oauthHead;
	}

	/**
	 * set header for auth.
	 * 
	 * @param req
	 *          http request
	 * @param uri
	 *          uri without param
	 * @param urlParam
	 *          url's param. split by '&'
	 * @param consumer
	 *          consumer
	 * @param token
	 *          token
	 * @param verifier
	 *          verifier
	 */
	public static void setAuthHeader(
	/* HttpRequestBase req, */HttpURLConnection conn, String uri,
			String urlParam, Consumer consumer, Token token, String verifier) {
		String method = conn.getRequestMethod();// req.getMethod();
		TreeMap<String, String> paramMapForSign = generateMapByParam(urlParam);
		String headerValue = createAuthHeaderForResource(method, uri,
				paramMapForSign, consumer, token, verifier);
		// req.setHeader("Authorization", headerValue);
		// req.setHeader("Content-Type", "application/json; charset=utf8");
		conn.setRequestProperty("Authorization", headerValue);
		conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
		// conn.setRequestProperty("Content-Length","0");
	}

	private static TreeMap<String, String> generateMapByParam(String urlParam) {
		if (urlParam == null || urlParam.trim().length() == 0) {
			return null;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		String[] params = urlParam.split("&");
		for (String param : params) {
			int index = param.indexOf("=");
			if (index < 0) {
				map.put(param, "");
			} else {
				String before = param.substring(0, index);
				String after = param.substring(index + 1);
				map.put(before, after);
			}
		}
		return map;
	}
	
	public static String openTransaction(String transactionId, Consumer consumer, Token token, String verifier) throws Exception {
		String transactionStatus = "open";
		
		HttpURLConnection conn = changeTransactionStatus(transactionStatus, transactionId, consumer, token, verifier);
		Integer status = conn.getResponseCode();//response.getStatusLine().getStatusCode();
		logger.debug("token {} ", token.getToken());
		String result = Utils.getStreamAsString(conn.getInputStream());//EntityUtils.toString(response.getEntity());
		Transaction transaction = JSON.parseObject(result, Transaction.class);
		
		if (transaction.getState().equals(transactionStatus)) {
			return result;
		}
		return null;
	}
	
	public static boolean commitTransaction(String transactionId, Consumer consumer, Token token, 
			String verifier) throws Exception {
		HttpURLConnection conn = changeTransactionStatus("closed", transactionId, consumer, token, verifier);
		Integer status = conn.getResponseCode();//response.getStatusLine().getStatusCode();
		if (status >= 300 || status <200) {
			// cancel transaction when commit failed.
			changeTransactionStatus("canceled", transactionId, consumer, token, verifier);
		} else if ( status == 202 ) {
			logger.info("The transaction is successful.");
			return true;
		} else {
			logger.info("The transcation status is {}", status);
		}
		return false;
	}
	
	public static HttpURLConnection changeTransactionStatus(String transactionStatus, String transactionId, Consumer consumer, Token token, String verifier) throws Exception {
		// set header
		//HttpClient httpClient = new DefaultHttpClient();
		String uri = MobageCharge.PROVIDER.getRestfulHost() + bankTransaction + "/" + transactionId;
		String urlParam = "fields=state";
		//HttpPut httpput = new HttpPut(uri + "?" + urlParam);
		URL url = new URL(uri + "?" + urlParam);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("PUT");
		MbgaOauthUtil.setAuthHeader(conn, uri, urlParam, consumer, token, verifier);
		
		// set body
		State state = new State();
		state.state = transactionStatus;
		String json = JSON.toJSONString(state);
		conn.setDoOutput(true);
		OutputStream out = conn.getOutputStream();
		out.write(json.getBytes());
		out.flush();
		out.close();
	
		//HttpEntity entity = new StringEntity(json, "UTF-8");
		//httpput.setEntity(entity);
		
		// response
		//HttpResponse response = httpClient.execute(httpput);
		return conn;
	}
	
	static class State {
		public String state;
	}

}
