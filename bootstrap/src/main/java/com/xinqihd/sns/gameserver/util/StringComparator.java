package com.xinqihd.sns.gameserver.util;

import java.util.Comparator;

/**
 * It apply the digital comparsion between two string with digits ending, as follows:
 *  row1 < row10 
 *  row2 < row10
 *  row1 row2 row3 row4 row5 row6 row7 row8 row9 row10 ...
 * @author wangqi
 *
 */
public class StringComparator implements Comparator<String> {
	
	static final StringComparator instance = new StringComparator();
	
	private StringComparator() {
		
	}
	
	/**
	 * Get the default singleton object.
	 */
	public static final StringComparator getInstance() {
		return instance;
	}

		@Override
		public int compare(String o1, String o2) {
			if ( o1 == o2 ) {
				return 0;
			} else if ( o1 == null ) {
				return -1;
			} else if ( o2 == null ) {
				return 1;
			} else if ( o1.length() < o2.length() ) {
				return -1;
			} else if ( o1.length() > o2.length() ) {
				return 1;
			} else {
				return o1.compareTo(o2);
			}
		}
	
}
