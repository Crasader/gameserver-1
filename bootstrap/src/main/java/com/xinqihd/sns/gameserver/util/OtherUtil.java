package com.xinqihd.sns.gameserver.util;

import java.io.BufferedInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.xinqihd.sns.gameserver.config.Constant;

/**
 * Some useful tools.
 * 
 * @author wangqi
 *
 */
public class OtherUtil {
	
	/**
	 * Get the meaningful hostname.
	 * @return
	 */
	public static final String getHostName() {
		try {
			String hostName = InetAddress.getLocalHost().getHostAddress();
//			if ( hostName.equals(Constant.LOCALHOST) ) {
//				try {
//					Runtime run = Runtime.getRuntime();
//					Process proc = run.exec( Constant.HOSTNAME );
//					BufferedInputStream in = new BufferedInputStream( proc.getInputStream() );
//					byte [] b = new byte[in.available()];
//					in.read(b);
//					hostName = (new String(b)).trim();
//				} catch (Exception e) {
//				}
//			}
			if ( hostName.equals(Constant.LOCALHOST) ) {
				Enumeration<NetworkInterface> netEnum = NetworkInterface.getNetworkInterfaces();
				LOOP:
				while ( netEnum.hasMoreElements() ) {
					NetworkInterface inet = netEnum.nextElement();
					Enumeration<InetAddress> net = inet.getInetAddresses();
					while ( net.hasMoreElements() ) {
						hostName = net.nextElement().getHostAddress();
						if ( hostName.startsWith("192") ) {
							break LOOP;
						} else if ( hostName.startsWith("10") ) {
							break LOOP;
						}
					}
				}
			}
			return hostName.trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Constant.LOCALHOST;
	}

}
