package com.xinqihd.sns.gameserver.config;

import java.util.Comparator;

public class BiblioPowerComparator implements Comparator<BiblioPojo> {

	@Override
	public int compare(BiblioPojo b1, BiblioPojo b2) {
		if ( b1 == null ) {
			return -1;
		}
		if ( b2 == null ) {
			return -2;
		}
		int result = b2.getPower() - b1.getPower();
		if ( result == 0 ) {
			return b1.getWeaponType().compareTo(b2.getWeaponType());
		} else {
			return result;
		}
	}

}
