package com.xinqihd.sns.gameserver.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * Provide common string function
 * @author wangqi
 *
 */
public class StringUtil {
	
	private static final String ALG_MD5 = "MD5";
	
	private static final String ALG_SHA1 = "SHA1";
	
	private static final char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		
	/**
	 * Extract a part of the given string. If the start is null or not found,
	 * then use 0 as start. If the end is null or not found, use the length
	 * as the end.
	 * 
	 * @param line
	 * @param start
	 * @param end
	 * @return
	 */
	public static final String substring(String line, String start, String end) {
		if ( line == null || line.length()<=0 ) {
			return null;
		}
		int beginIndex = 0;
		int endIndex = line.length();
		if ( start != null ) {
			beginIndex = line.indexOf(start);
			if ( beginIndex >= 0 ) {
				beginIndex +=  start.length();
			} else {
				beginIndex = 0;
			}
		}
		if ( end != null ) {
			endIndex = line.indexOf(end, beginIndex);
			if ( endIndex <= 0 ) {
				endIndex = line.length();
			} else if ( endIndex <= beginIndex ) {
				return Constant.EMPTY;
			}
		}
		return line.substring(beginIndex, endIndex);
	}
	
	/**
	 * Extract a part of the given string from the right side. It is mainly
	 * used to extract a uri path from Url.
	 * For example:
	 *    url = "GET /data/config/item_config.lua?id=1"
	 * 		substringR(url, "/", "?") => item_config.lua
	 * If the start is null or not found,
	 * then use 0 as start. If the end is null or not found, use the length
	 * as the end.
	 * 
	 * @param line
	 * @param start
	 * @param end
	 * @return
	 */
	public static final String substringR(String line, String start, String end) {
		if ( line == null || line.length()<=0 ) {
			return null;
		}
		int beginIndex = 0;
		int endIndex = line.length();
		if ( start != null ) {
			beginIndex = line.lastIndexOf(start);
			if ( beginIndex >= 0 ) {
				beginIndex +=  start.length();
			} else {
				beginIndex = 0;
			}
		}
		if ( end != null ) {
			endIndex = line.indexOf(end, beginIndex);
			if ( endIndex <= 0 ) {
				endIndex = line.length();
			} else if ( endIndex <= beginIndex ) {
				return Constant.EMPTY;
			}
		}
		return line.substring(beginIndex, endIndex);
	}
	
	/**
	 * Concat all strings 
	 * @param value
	 * @return
	 */
	public static final String concat(Object ... value ) {
		if ( value == null ) {
			return null;
		}
		if ( value.length == 1 ) {
			if ( value[0] == null ) return Constant.EMPTY;
			return value[0].toString();
		}
		if ( value.length == 2 ) {
			if ( value[0] == null ) value[0] = Constant.EMPTY;
			if ( value[1] == null ) value[1] = Constant.EMPTY;
			return value[0].toString().concat(value[1].toString());
		}
		if ( value.length > 2 ) {
			int length = 0;
			String[] strs = new String[value.length];
			for ( int i=0; i<value.length; i++ ) {
				if ( value[i] != null ) {
					strs[i] = value[i].toString();
					length += strs[i].length();
				}
			}
			StringBuilder buf = new StringBuilder(length);
			for ( String v : strs ) {
				if ( v != null )
					buf.append(v);
			}
			return buf.toString();
		}
		return Constant.EMPTY;
	}
	
	/**
	 * Convert the string to int.
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static final int toInt(String str, int defaultValue) {
		int value = defaultValue;
		if ( str == null || str.length() <= 0 ) {
			return value;
		}
		try {
			value = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * Convert the string to int.
	 * @param value
	 * @param defaultValue
	 * @return
	 */
	public static final float toFloat(String str, float defaultValue) {
		float value = defaultValue;
		try {
			value = Float.parseFloat(str);
		} catch (NumberFormatException e) {
			value = defaultValue;
		}
		return value;
	}
	
	/**
	 * Check if a string is empty or null
	 * @param str
	 * @return
	 */
	public static final boolean checkNotEmpty(String str) {
		if ( str == null || str.length() == 0 ) { 
			return false;
		}
		return true;
	}
	
	/**
	 * Pad the length-str.length whitespace at the right side of given str
	 * @param buf
	 * @param str
	 * @param length
	 */
	public static final void padStringRight(StringBuilder buf, String str, int length) {
		buf.append(str);
		for ( int j=str.length(); j<length; j++ ) {
			buf.append(' ');
		}
	}
	
	/**
	 * The fastest method to convert a byte array to hex string.
	 * @param bytes
	 * @return
	 */
	public static final String bytesToHexString(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for ( int j = 0; j < bytes.length; j++ ) {
        v = bytes[j] & 0xFF;
        hexChars[j*2] = HEX_CHARS[v/16];
        hexChars[j*2 + 1] = HEX_CHARS[v%16];
    }
    return new String(hexChars);
	}
	
	/**
	 * Convert the hex string back to byte array. 
	 * Note The 'A','B','C','D','E','F' should be uppercase format.
	 * 
	 * @param hexString
	 * @return
	 */
	public static final byte[] hexStringToBytes(String hexString) {
		byte[] bytes = new byte[hexString.length()/2];
		for ( int i=0; i<bytes.length; i++ ) {
			int ch1 = hexString.charAt(i*2);
			int ch2 = hexString.charAt(i*2+1);
			bytes[i] = (byte)(((ch1 <= '9' ? ch1 - '0' : ch1 - 'A' + 10) << 4) + 
					(ch2 <= '9' ? ch2 - '0' : ch2 - 'A'+10));
		}
		return bytes;
	}
	
	/**
	 * Split the MachineId format like "192.168.0.6:2181" into 
	 * String[]{"192.168.0.6", "2181"} string array.
	 * 
	 * 
	 * @param machineId
	 * @return null if the machineid is malformed.
	 */
	public static final String[] splitMachineId(String machineId) {
		if ( machineId == null ) return null;
		int idx = machineId.indexOf(':');
		if ( idx > 0 && idx < machineId.length()-1 ) {
			String[] results = new String[2]; 
			results[0] = machineId.substring(0, idx);
			results[1] = machineId.substring(idx+1);
			return results;
		} else {
			return null;
		}
	}

	/**
	 * Split the locale format like "zh_CN" into 
	 * String[]{"zh", "CN"} string array.
	 * 
	 * 
	 * @param machineId
	 * @return null if the machineid is malformed.
	 */
	public static final Locale parseLocale(String languageTag, Locale defaultLocale) {
		if ( languageTag == null ) return defaultLocale;
		int idx = languageTag.indexOf('_');
		if ( idx > 0 && idx < languageTag.length()-1 ) {
			String lang = languageTag.substring(0, idx);
			String country = languageTag.substring(idx+1);
			Locale locale = new Locale(lang, country);
			return locale;
		} else {
			return defaultLocale;
		}
	}
	
	/**
	 * A valid email address should contain the name and '@' and 
	 * a dot separated domain name.
	 * @param emailAddress
	 * @return
	 */
	public static final boolean checkValidEmail(String emailAddress) {
		boolean isValid = true;
		if ( StringUtil.checkNotEmpty(emailAddress) ) {
			int atIndex = emailAddress.indexOf('@');
			if ( atIndex > 0 ) {
				if ( emailAddress.indexOf('.', atIndex) > 0 ) {
					isValid = true;
				} else {
					isValid = false;
				}
			} else {
				isValid = false;
			}
		} else {
			isValid = false;
		}
		return isValid;
	}
	
	/**
	 * Encrypt a given string into SHA1 format. It is usually for password encryption
	 * 
	 * @param data
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
  public static String encryptSHA1(String data) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance(ALG_SHA1);
    md.update(data.getBytes());
    byte[] digest = md.digest();
    StringBuffer buf = new StringBuffer();
    for (byte b : digest) {
      buf.append((Character.forDigit((b & 0xF0) >> 4, 16)));
      buf.append((Character.forDigit((b & 0xF), 16)));
    }
    return buf.toString();
  }
  
  /**
   * Encrypt the password as MD5
   * @param data
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static String encryptMD5(String data) throws NoSuchAlgorithmException {
  	if (data == null) return null;
    MessageDigest md = MessageDigest.getInstance(ALG_MD5);
    md.update(data.getBytes());
    byte[] digest = md.digest();
    StringBuffer buf = new StringBuffer();
    for (byte b : digest) {
      buf.append((Character.forDigit((b & 0xF0) >> 4, 16)));
      buf.append((Character.forDigit((b & 0xF), 16)));
    }
    return buf.toString();  	
	}
}
