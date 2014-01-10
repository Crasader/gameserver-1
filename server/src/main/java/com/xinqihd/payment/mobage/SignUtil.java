package com.xinqihd.payment.mobage;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.xinqihd.payment.Base64;


public class SignUtil {
	private static final String UTF8 = "UTF-8";
	
	public static String signByHmacSHA1(String baseString, String consumerSecret, String tokenSecret) {
		try {
			byte[] data = baseString.getBytes(UTF8);
			byte[] secret = (consumerSecret + "&" + tokenSecret).getBytes(UTF8);
			byte[] codeAfterHmacSHA1 = hmacSha1(secret, data);
			String codeAfterBase64 = new String(Base64.encode(codeAfterHmacSHA1));
			return codeAfterBase64;
		} catch (Exception e) {
			//return null;
			e.printStackTrace();
			return null;
		}
	}
	
	private static byte[] hmacSha1(byte[] secret, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
		String algo = "HmacSHA1";
		SecretKey secretKey = new SecretKeySpec(secret, algo);
	    Mac m = Mac.getInstance(algo);
	    m.init(secretKey);
	    return m.doFinal(data);
	}
	
}
