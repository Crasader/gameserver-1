package com.xinqihd.sns.gameserver.entity.rank;

/**
 * 过滤类型
 * 0: 总排行
 * 1: 日增长
 * 2: 月增长
 * 
 * @author wangqi
 *
 */
public enum RankFilterType {

	TOTAL(0),
	DAILY(1),
	MONTHLY(2),
	//For test purpose.
	FIVE_SECONDS(999);
	
	private int index = 0;
	
	RankFilterType(int index) {
		this.index = index;
	}
	
	public int index() {
		return index;
	}
	
	public static final RankFilterType fromIndex(int index) {
		switch ( index ) {
			case 0:
				return TOTAL;
			case 1:
				return DAILY;
			case 2:
				return MONTHLY;
			case 999:
				return FIVE_SECONDS;
		}
		return null;
	}
}
