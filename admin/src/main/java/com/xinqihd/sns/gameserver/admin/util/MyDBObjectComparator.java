package com.xinqihd.sns.gameserver.admin.util;

import java.util.Comparator;

import com.mongodb.DBObject;

public class MyDBObjectComparator implements Comparator<DBObject> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(DBObject o1, DBObject o2) {
		if ( o1 == null ) {
			return -1;
		} else if ( o2 == null ) {
			return 1;
		} else if ( o1 == o2 ) {
			return 0;
		} else {
			String id1 = (String)o1.get("_id");
			String id2 = (String)o2.get("_id");
			if ( id1 == null || id2 == null ) {
				return o1.hashCode() - o2.hashCode();
			} else {
				MixComparator comp = new MixComparator();
				return comp.compare(id1, id2);
			}
			
		}
	}

}
