package com.xinqihd.sns.gameserver.entity.user;

import static com.xinqihd.sns.gameserver.config.Constant.*;

import java.nio.ByteBuffer;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.xinqihd.sns.gameserver.util.CommonUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * The UserId is optimized for database sharding. It contains 4 bytes for time 
 * and an unique string for user name.
 * Note: the time part does not contain hour/minute/second.
 * 
 *  
 * @author wangqi
 *
 */
public class UserId {
	
	private static final int STRING_LENGTH = 31;

	private final byte[] internal;
	
	private final String userName;
	
	private final int registerDate;
	
	private final String toString;
	
	
	public UserId(String userName) {
		this(userName, CommonUtil.getTodayMillis());
	}
	
	public UserId(String userName, int registerDate) {
		if ( userName == null ) {
			throw new IllegalArgumentException("userName is null." + userName);
		}
		this.registerDate = registerDate;
		this.userName = userName;
		this.internal = CommonUtil.getNewUserIdBytes(registerDate, userName);
		this.toString = convertToString();
	}
	
	public UserId(String userName, int registerDate, byte[] internal) {
		if ( userName == null ) {
			throw new IllegalArgumentException("userName is null." + userName);
		}
		if ( internal == null || internal.length < 4) {
			throw new IllegalArgumentException("internal is null or too small." + userName);
		}
		this.registerDate = registerDate;
		this.userName = userName;
		this.internal = internal;
		this.toString = convertToString();
	}

	/**
	 * @return the internal
	 */
	public byte[] getInternal() {
		return internal;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the registerDate
	 */
	public int getRegisterDate() {
		return registerDate;
	}
	
	/**
	 * Convert to string
	 */
	@SuppressWarnings("deprecation")
	public final String toString() {
		return this.toString;
	}
	
	/**
	 * Convert the key to string format
	 * @return
	 */
	public final String convertToString() {
	//	Date date = new Date(registerDate*1000l);
		StringBuilder buf = new StringBuilder(STRING_LENGTH);
	//	buf.append(date.getYear()+1900);
	//	buf.append(PATH_SEP);
	//	int month = date.getMonth()+1;
	//	if ( month < 10 ) {
	//		buf.append(0);
	//	}
	//	buf.append(month);
	//	buf.append(PATH_SEP);
	//	int dayOfMonth = date.getDate();
	//	if ( dayOfMonth < 10 ) {
	//		buf.append(0);
	//	}
	//	buf.append(dayOfMonth);
		buf.append(registerDate);
		buf.append(COLON);
		buf.append(userName);
		return buf.toString();
	}
	
	/**
	 * http://stackoverflow.com/questions/1835976/what-is-a-sensible-prime-for-hashcode-calculation/2816747#2816747
	 * I do acknowledge that it is quite debatable whether these calculation make much 
	 * sense in practice. But I do think that taking 92821 as prime makes much more sense 
	 * than 31, unless you have good reasons not to.
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = BEST_PRIME;
		int result = 1;
		result = prime * result + registerDate;
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserId other = (UserId) obj;
		if (registerDate != other.registerDate)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	/**
	 * For MapDBObject to construct it.
	 * @param userIdString
	 * @return
	 */
	public final static UserId valueOf(String userIdString) {
		return fromString(userIdString);
	}
	
	/**
	 * Construct the UserId object from a string.
	 * Format: yyyy/mm/dd:userName
	 * e.g. 2012/02/29:0123456789
	 * 
	 * @param userIdString
	 * @return
	 */
	public final static UserId fromString(String userIdString) {
//		String yearStr = userIdString.substring(0, 4);
//		String monthStr = userIdString.substring(5, 7);
//		String dayStr = userIdString.substring(8, 10);
//		String userName = userIdString.substring(11);
//		
//		int year = Integer.parseInt(yearStr);
//		int month = Integer.parseInt(monthStr);
//		int day = Integer.parseInt(dayStr);
//		
//		Date date = new Date(year-1900, month-1, day);
		try {
			int dateIndex = userIdString.indexOf(':');
			String dateValue = null;
			String userName = null;
			if ( dateIndex != -1 ) {
				dateValue = userIdString.substring(0, dateIndex);
				userName = userIdString.substring(dateIndex+1);
			}
			if ( dateValue != null && userName != null ) {
				int timeMillis = StringUtil.toInt(dateValue, -1);
				if ( timeMillis == -1 ) {
					return null;
				}
				UserId userId = new UserId(userName, timeMillis);
				return userId;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Construct an UserId object from internal bytes format.
	 * @param bytes
	 * @return
	 */
	public final static UserId fromBytes(byte[] bytes) {
		if ( bytes == null ) return null;
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		int time = buf.getInt();
		int ch = -1;
		StringBuilder builder = new StringBuilder(20);
		while ( buf.hasRemaining() ) {
			ch = buf.get();
			if ( buf.hasRemaining() ) {
				ch = ( (ch ) << 8 | buf.get() & 0xff) ;
//				System.out.print(",0x" + Integer.toHexString(ch&0xffff));
				builder.append((char)ch);
			}
		}
		UserId userId = new UserId(builder.toString(), time, bytes);
		return userId;
	}

}
