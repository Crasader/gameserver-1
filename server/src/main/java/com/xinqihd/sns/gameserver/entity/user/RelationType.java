package com.xinqihd.sns.gameserver.entity.user;


/**
 * User's relation's type. Now support: 
 *    ['friend', 'guild', 'recent', 'rival', 'blacklist']
 *    
 * @author wangqi
 *
 */
public enum RelationType {

	FRIEND("friend"),
	RIVAL("rival"),
	GUILD("guild"),
	BLACKLIST("black"),
	RECENT("recent");
	
	private String tag = null;
	
	RelationType(String tag) {
		this.tag = tag;
	}
	
	public String tag() {
		return this.tag;
	}
	
	/**
	 * Since the number of types is realy small, simple
	 * loop search performs better than Arrays.binarySearch() 
	 * 
	 * @param tag
	 * @return
	 */
	public static RelationType fromTag(String tag) {
		RelationType[] arrays = RelationType.values();
		for ( int idx=0; idx<arrays.length; idx++ ) {
			if ( arrays[idx].tag().equals(tag) ) {
				return arrays[idx];
			}
		}
		return null;
	}
}
