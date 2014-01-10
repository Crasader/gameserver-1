package com.xinqihd.sns.gameserver.chat;

import java.util.Comparator;

/**
 * Sort the strings according the their length. The longest string
 * should be first to display.
 * 
 * @author wangqi
 *
 */
public class StringLengthComparator implements Comparator<String> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String o1, String o2) {
		if ( o2 == null ) {
			return -1;
		} else if ( o1 == null ) {
			return 1;
		}
		if ( o1.length() != o2.length() ) {
			return o2.length() - o1.length();
		}
		return o1.compareTo(o2);
	}
	
}
