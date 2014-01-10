package com.xinqihd.sns.gameserver.admin.util;

import java.util.Comparator;

public class MixComparator implements Comparator<Object> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object o1, Object o2) {
		if ( o1 == null ) {
			return -1;
		}
		if ( o2 == null ) {
			return 1;
		}
		if ( o1 instanceof String && o2 instanceof String) {
			try {
				double d1 = Double.parseDouble(o1.toString());
				double d2 = Double.parseDouble(o2.toString());
				return (int)(d1-d2);
			} catch (NumberFormatException e) {
			}
			return o1.toString().compareTo(o2.toString());
		}
		double d1 = convertObjectToDouble(o1);
		double d2 = convertObjectToDouble(o2);
		return (int)(d1 - d2);
	}
	
	public double convertObjectToDouble(Object obj) {
		double d = Double.NaN;
		if ( obj != null ) {
			if (obj instanceof Double ) {
				d = ((Double) obj).doubleValue();
			} else if ( obj instanceof Integer ) {
				d = ((Integer) obj).intValue();
			} else if ( obj instanceof Float ) {
				d = ((Float) obj).floatValue();
			} else if ( obj instanceof Short ) {
				d = ((Short) obj).shortValue();
			} else if ( obj instanceof Byte ) {
				d = ((Byte) obj).byteValue();
			} else {
				try {
					d = Double.parseDouble(obj.toString());
				} catch (NumberFormatException e) {
				}
			}
		}
		return d;
	}

}
