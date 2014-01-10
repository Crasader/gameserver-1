package com.xinqihd.sns.gameserver.util;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * This utility contains common useful methods for the game.
 * @author wangqi
 *
 */
public class CommonUtil {
	
	private static final Random RANDOM = new Random();
	
	/**
	 * Get today's 0:0:0 time millis. 
	 * The hour:minute:second are all zero.
	 * 
	 * @return
	 */
	public final static int getTodayMillis() {
		return getDateMillis(System.currentTimeMillis());
	}
	
	/**
	 * Get a date millis with the hour/minute/second
	 * are all 0.
	 * 
	 * @return
	 */
	public final static int getDateMillis(long millis) {
		int d = (int)(millis/1000);
		d = d - d % DAY_MILLIS;
		int m = d % DAY_MILLIS;
		if ( m == 0 ) {
//			d -= TIME_OFFSET;
			d += TIME_OFFSET_NEXT;
		}
		return d;
		
//		long d = millis - millis % 86400000;
//		long m = millis % 86400000;
//		if ( m == 0 ) {
//			d -= 28800000;
//		}
//		return (int)(d/1000);

	}
	
	/**
	 * Get the user's unique id according to today's millis and user unique name.
	 * @param userName
	 * @return
	 */
	public final static byte[] getNewUserIdBytes(int registerTime, String userName) {
		int length = userName.length();
		ByteBuffer buf = ByteBuffer.allocate(4+length<<1);
		buf.putInt(registerTime);
		for ( int i=0; i<length; i++ ) {
			int ch = userName.charAt(i);
			int b = (byte)(ch>>8 & 0xff);
//			if ( b == 0 ) {
//				buf.put( (byte)(ch & 0xff) );
//			} else {
				buf.put( (byte)b );
				buf.put( (byte)(ch & 0xff) );
//			}
		}
		byte[] idBytes = new byte[buf.position()];
		buf.flip();
		buf.get(idBytes);
		return idBytes;
	}
	
	/**
	 * For HTTP request format, the 'key=value' will be parsed and returned 
	 * only the value.
	 * 
	 * @param param
	 * @return
	 */
	public static final String retrieveParamValue(String param) {
		if ( StringUtil.checkNotEmpty(param) ) {
			int beginIndex = param.indexOf('=');
			if ( beginIndex > 0 ) {
				String value = param.substring(beginIndex+1);
				return value;
			} else {
				return param;
			}
		} else {
			return Constant.EMPTY;
		}
	}
	
	/**
	 * Parse the param url as hashmap
	 * @param paramUrl
	 * @return
	 */
	public static final HashMap<String, String> parseHttpParams(String paramUrl) {
		HashMap<String, String> paramMap = new HashMap<String, String>();
		if ( StringUtil.checkNotEmpty(paramUrl) ) {
			String[] fields = paramUrl.split("&");
			for ( String param :fields ) {
				int beginIndex = param.indexOf('=');
				if ( beginIndex > 0 ) {
					String key = param.substring(0, beginIndex);
					String value = param.substring(beginIndex+1);
					paramMap.put(key, value);
				}
			}
		}
		return paramMap;
	}
	
	/**
	 * Count the number of chars in a string. 
	 * @return
	 */
	public static final int countString(String string) {
		float count = 0f;
		if ( string != null ) {
			for ( char ch : string.toCharArray() ) {
				if ( (int)ch < 128 ) {
					count += 0.5f;
				} else {
					count +=1;
				}
			}
		}
		return (int) Math.floor(count);
	}
	
	/**
	 * Get a random int within [0, max) range.
	 * @param max
	 * @return
	 */
	public static final int getRandomInt(int max) {
		return RANDOM.nextInt(max);
	}
}
