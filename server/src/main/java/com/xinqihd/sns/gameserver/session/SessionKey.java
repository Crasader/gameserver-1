package com.xinqihd.sns.gameserver.session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * It represents a user's sessionkey when he/she login.
 * 
 * @author wangqi
 *
 */
public class SessionKey {
	
	private static final Random RAND = new Random();
	
	private static final byte[] DEFAULT_SUFFIX = new byte[]{'_','S','E','S','S'};
	
	/**
	 * The key contains client's ip and port. It uniqly represents a single user.
	 */
	private final byte[] key;
	
	private final int hashCode;
	
	private final String toString;
	
	private SessionKey(byte[] key) {
		this.key = key;
		int result = 0;
    for ( byte element : key ) {
      result = Constant.BEST_PRIME * result + element;
    }
		this.hashCode = result;
		this.toString = StringUtil.bytesToHexString(this.key);
	}
	
	/**
	 * Convert the byte[] array session key to string.
	 */
	public final String toString() {
		return this.toString;
	}
	
	/**
	 * Get the internal raw bytes.
	 * @return
	 */
	public final byte[] getRawKey() {
		return this.key;
	}
	
	/**
	 * Create an unique session key
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return hashCode;
	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessionKey other = (SessionKey) obj;
		if (!Arrays.equals(key, other.key))
			return false;
		return true;
	}
	
	/**
	 * Create a unique session key by the client socket address.
	 * It supports ipv6 and ipv4.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param address
	 * @return
	 */
	public static final SessionKey createSessionKey(SocketAddress address) {
		return SessionKey.createSessionKey(address, DEFAULT_SUFFIX);
	}
	
	/**
	 * Create a unique session key by the client socket address.
	 * It supports ipv6 and ipv4.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param address
	 * @return
	 */
	public static final SessionKey createSessionKey(SocketAddress address, byte[] suffix) {
		if ( address == null ) return null;
		if ( address instanceof InetSocketAddress ) {
			return createSessionKey((InetSocketAddress)address);
		}
		return null;
	}
	
	/**
	 * Create a unique session key by the client socket address.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param hostip
	 * @param port
	 * @return
	 */
	public static final SessionKey createSessionKey(String hostip, int port) {
		InetSocketAddress address = new InetSocketAddress(hostip, port);
		return createSessionKey(address, DEFAULT_SUFFIX);
	}
	
	/**
	 * Create a unique session key by the client socket address.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param hostip
	 * @param port
	 * @return
	 */
	public static final SessionKey createSessionKey(String hostip, int port, byte[] suffix) {
		InetSocketAddress address = new InetSocketAddress(hostip, port);
		return createSessionKey(address, suffix);
	}
	
	/**
	 * Create a unique sessioin key with the default _SESS suffix.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param address
	 * @return
	 */
	public static final SessionKey createSessionKey(InetSocketAddress address) {
		return SessionKey.createSessionKey(address, DEFAULT_SUFFIX);
	}
	
	/**
	 * Create a unique session key by the client socket address.
	 * It supports ipv6 and ipv4.
	 * 
	 * @deprecated Use the {@link #createSessionKeyFromRandomString(String)} instead.
	 * @param address
	 * @return
	 */
	public static final SessionKey createSessionKey(InetSocketAddress address, byte[] suffix) {
		int suffixLength = 0;
		if ( suffix == null ) {
			suffixLength = 0;
		} else {
			suffixLength = suffix.length;
		}
		byte[] socketAddress = address.getAddress().getAddress();
		//socket address + port(4) + time(4) + random(4) + '_sess'(5)
		ByteBuffer byteBuffer = ByteBuffer.allocate(socketAddress.length+4+4+4+suffixLength);
		byteBuffer.put(socketAddress);
		byteBuffer.putInt(address.getPort());
		int timeValue = (int)(System.currentTimeMillis() & 0xFFFFFFFF );
		byteBuffer.putInt(timeValue);
		byteBuffer.putInt(RAND.nextInt());
		for ( int i=0; i<suffixLength; i++ ) {
			byteBuffer.put(suffix[i]);
		}
		SessionKey sessionKey = new SessionKey(byteBuffer.array());
		return sessionKey;
	}
	
	/**
	 * Create a SessionKey from the internal hex string format.
	 * @param hexString
	 * @return
	 */
	public static final SessionKey createSessionKeyFromHexString(String hexString) {
		byte[] array = StringUtil.hexStringToBytes(hexString);
		return new SessionKey(array);
	}
	
	/**
	 * Create a random SessionKey 
	 * 
	 * @return
	 */
	public static final SessionKey createSessionKeyFromRandomString() {
		return createSessionKeyFromRandomString(null);
	}
	
	/**
	 * Create a random SessionKey with the given prefix. 
	 * 
	 * @param prefix It can be null or a given prefix.
	 * @return
	 */
	public static final SessionKey createSessionKeyFromRandomString(String prefix) {
		ByteBuffer byteBuffer = null;
		int strLength = 0;
		if ( prefix == null ) {
			//time(8) + random(4)
			byteBuffer = ByteBuffer.allocate(8+4);
		} else {
			strLength = prefix.length();
			//string + time(8) + random(4)
			byteBuffer = ByteBuffer.allocate(strLength+8+4);
			byteBuffer.put(prefix.getBytes(Constant.charset));
		}
		long timeValue = System.currentTimeMillis();
		byteBuffer.putLong(timeValue);
		byteBuffer.putInt(RAND.nextInt());
		SessionKey sessionKey = new SessionKey(byteBuffer.array());
		return sessionKey;
	}
	
	/**
	 * Create a SessionKey from the internal hex string format.
	 * @param hexString
	 * @return
	 */
	public static final SessionKey createSessionKey(byte[] bytes) {
		return new SessionKey(bytes);
	}
}
