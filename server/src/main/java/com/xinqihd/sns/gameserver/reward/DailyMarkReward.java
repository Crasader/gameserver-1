package com.xinqihd.sns.gameserver.reward;

import java.util.ArrayList;
import java.util.List;

import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.config.DailyMarkPojo;
import com.xinqihd.sns.gameserver.util.StringUtil;

/**
 * This class wraps the necessary fields for daily mark
 * @author wangqi
 *
 */
public class DailyMarkReward {
	
	private int dayOfMonth = 0;

	private String currentMonth = null;
	
	private String today = null;
	
	private int totalCount = 0;
	
	private ArrayList<Boolean> markList = new ArrayList<Boolean>(31);
	
	private DailyMarkPojo dailyMark = null;
	
	private boolean isTodayMarked = false;

	/**
	 * @return the currentMonth
	 */
	public String getCurrentMonth() {
		return currentMonth;
	}

	/**
	 * @param currentMonth the currentMonth to set
	 */
	public void setCurrentMonth(String currentMonth) {
		this.currentMonth = currentMonth;
	}

	/**
	 * @return the today
	 */
	public String getToday() {
		return today;
	}

	/**
	 * @param today the today to set
	 */
	public void setToday(String today) {
		this.today = today;
	}

	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return the markArray
	 */
	public List<Boolean> getMarkArray() {
		return markList;
	}

	/**
	 * @param markArray the markArray to set
	 */
	public void addMarkArray(boolean[] markArray) {
		for ( boolean bool : markArray ) {
			this.markList.add(bool);
		}
	}
	
	/**
	 * @return the dayOfMonth
	 */
	public int getDayOfMonth() {
		return dayOfMonth;
	}

	/**
	 * @param dayOfMonth the dayOfMonth to set
	 */
	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	/**
	 * Set the mark array by using the string format:
	 *  "1,0,0,1,0,1"
	 * 
	 * @param markArrayStr
	 */
	public static final boolean[] toMarkArray(String markArrayStr ) {
		if ( StringUtil.checkNotEmpty(markArrayStr) ) {
			String[] fields = markArrayStr.split(",");
			boolean[] boolArray = new boolean[fields.length];
			for ( int i=0; i<fields.length; i++ ) {
				String field = fields[i];
				if ( Constant.ZERO.equals(field) ) {
					boolArray[i] = false;
				} else {
					boolArray[i] = true;
				}
			}
			return boolArray;
		} else {
			return new boolean[0];
		}
	}
	
	/**
	 * @return the dailyMark
	 */
	public DailyMarkPojo getDailyMark() {
		return dailyMark;
	}

	/**
	 * @param dailyMark the dailyMark to set
	 */
	public void setDailyMark(DailyMarkPojo dailyMark) {
		this.dailyMark = dailyMark;
	}

	/**
	 * @return the isTodayMarked
	 */
	public boolean isTodayMarked() {
		return isTodayMarked;
	}

	/**
	 * @param isTodayMarked the isTodayMarked to set
	 */
	public void setTodayMarked(boolean isTodayMarked) {
		this.isTodayMarked = isTodayMarked;
	}

}
