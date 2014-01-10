package com.xinqihd.sns.gameserver.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

	private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final SimpleDateFormat SDF_DETAIL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final SimpleDateFormat SDF_APPLE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
	
	// The string format for yesterday: "2012-02-09"
	private static String STR_YESTERDAY = null;
	// The timestamp that generate STR_YESTERDAY value
	private static long yesterdayGenerateMillis = 0;
	// The checking for next generation's timeout millis
	private static long yesterdayCheckTimeout = 0;
	
	// The string format for today: "2012-02-15"
	private static String STR_TODAY = null;
	// The timestamp that generate STR_YESTERDAY value
	private static long todayGenerateMillis = 0;
	// The checking for next generation's timeout millis
	private static long todayCheckTimeout = 0;
	
	// The string format for this week: "2012-32"
	private static String STR_WEEK = null;
	// The timestamp that generate STR_WEEK value
	private static long weekGenerateMillis = 0;
	// The checking for next generation's timeout millis
	private static long weekCheckTimeout = 0;

	// The string format for last month: "2012-02"
	private static String STR_LAST_MONTH = null;
	// The timestamp that generate STR_LAST_MONTH value
	private static long lastMonthGenerateMillis = 0;
	// The checking for next generation's timeout millis
	private static long lastMonthCheckTimeout = 0;
	
	// The string format for last month: "2012-02"
	private static String STR_CURRENT_MONTH = null;
	// The timestamp that generate STR_LAST_MONTH value
	private static long currentMonthGenerateMillis = 0;
	// The checking for next generation's timeout millis
	private static long currentMonthCheckTimeout = 0;
	
	private static Lock yestLock = new ReentrantLock();
	private static Lock todayLock = new ReentrantLock();
	private static Lock weekLock = new ReentrantLock();
	private static Lock lastMonthLock = new ReentrantLock();
	private static Lock currMonthLock = new ReentrantLock();
	
	/**
	 * It will reset the internal status for test purpose
	 */
	public static final void resetInternalDate() {
		STR_YESTERDAY = null;
		STR_TODAY = null;
		STR_WEEK = null;
		STR_LAST_MONTH = null;
		STR_CURRENT_MONTH = null;
	}

	/**
	 * Return the yesterday's string format like "2012-02-09"
	 * 
	 * @return
	 */
	public static final String getYesterday(long currentTimeMillis) {
		try {
			yestLock.lock();
			if (STR_YESTERDAY == null
					|| currentTimeMillis - yesterdayGenerateMillis 
						>= yesterdayCheckTimeout) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTimeMillis);
				// Need to re-genereate the STR_YESTERDAY value because it expires
				yesterdayGenerateMillis = currentTimeMillis;
				yesterdayCheckTimeout = getMilliSecondsToNextDateUnit(
						DateUnit.DAILY, cal);
				logger.debug(
						"Regenerate the yesterday string. next checkout millis is: {}",
						yesterdayCheckTimeout);

				// Set it to yesterday
				cal.add(Calendar.DAY_OF_MONTH, -1);

				StringBuilder buf = new StringBuilder(10);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				int day = cal.get(Calendar.DAY_OF_MONTH);
				buf.append(year).append('-');
				if (month < 10) {
					buf.append('0');
				}
				buf.append(month).append('-');
				if (day < 10) {
					buf.append('0');
				}
				buf.append(day);
				STR_YESTERDAY = buf.toString();
			}
			return STR_YESTERDAY;
		} finally {
			yestLock.unlock();
		}
	}
	
	/**
	 * Return the today's string format like "2012-02-09"
	 * 
	 * @return
	 */
	public static final String getToday(long currentTimeMillis) {
		try {
			todayLock.lock();
			if (STR_TODAY == null
					|| currentTimeMillis - todayGenerateMillis 
						>= todayCheckTimeout) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTimeMillis);
				// Need to re-genereate the STR_TODAY value because it expires
				todayGenerateMillis = currentTimeMillis;
				todayCheckTimeout = getMilliSecondsToNextDateUnit(
						DateUnit.DAILY, cal);
				logger.debug(
						"Regenerate the today string. next checkout millis is: {}",
						todayCheckTimeout);

				StringBuilder buf = new StringBuilder(10);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				int day = cal.get(Calendar.DAY_OF_MONTH);
				buf.append(year).append('-');
				if (month < 10) {
					buf.append('0');
				}
				buf.append(month).append('-');
				if (day < 10) {
					buf.append('0');
				}
				buf.append(day);
				STR_TODAY = buf.toString();
			}
			return STR_TODAY;
		} finally {
			todayLock.unlock();
		}
	}
	
	/**
	 * Return the today's string format like "2012-02-09"
	 * 
	 * @return
	 */
	public static final String getWeek(long currentTimeMillis) {
		try {
			weekLock.lock();
			if (STR_WEEK == null
					|| currentTimeMillis - weekGenerateMillis 
						>= weekCheckTimeout) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTimeMillis);
				// Need to re-genereate the STR_TODAY value because it expires
				weekGenerateMillis = currentTimeMillis;
				weekCheckTimeout = getMilliSecondsToNextDateUnit(
						DateUnit.WEEKLY, cal);
				logger.debug(
						"Regenerate the week string. next checkout millis is: {}",
						weekCheckTimeout);

				StringBuilder buf = new StringBuilder(10);
				int year = cal.get(Calendar.YEAR);
				int week = cal.get(Calendar.WEEK_OF_YEAR);
				buf.append(year).append('-');
				if (week < 10) {
					buf.append('0');
				}
				buf.append(week);
				STR_WEEK = buf.toString();
			}
			return STR_WEEK;
		} finally {
			weekLock.unlock();
		}
	}

	/**
	 * Return the yesterday's string format like "2012-02-09"
	 * 
	 * @return
	 */
	public static final String getLastMonth(long currentTimeMillis) {
		try {
			lastMonthLock.lock();
			if (STR_LAST_MONTH == null
					|| currentTimeMillis - lastMonthGenerateMillis 
						>= lastMonthCheckTimeout) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTimeMillis);
				// Need to re-genereate the STR_YESTERDAY value because it expires
				lastMonthGenerateMillis = currentTimeMillis;
				lastMonthCheckTimeout = getMilliSecondsToNextDateUnit(DateUnit.MONTHLY,
						cal);
				logger.debug(
						"Regenerate the last month string. next checkout millis is: {}",
						lastMonthCheckTimeout);

				// Set it to yesterday
				cal.add(Calendar.MONTH, -1);

				StringBuilder buf = new StringBuilder(10);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				buf.append(year).append('-');
				if (month < 10) {
					buf.append('0');
				}
				buf.append(month);
				STR_LAST_MONTH = buf.toString();
			} else {
				long passed = lastMonthCheckTimeout - (currentTimeMillis - lastMonthGenerateMillis);
//				logger.debug("Need {} millisecond to re-generate last_month value",
//						passed);
			}
			return STR_LAST_MONTH;
		} finally {
			lastMonthLock.unlock();
		}
	}
	
	/**
	 * Return the current month format like "2012-02"
	 * 
	 * @return
	 */
	public static final String getCurrentMonth(long currentTimeMillis) {
		try {
			currMonthLock.lock();
			if (STR_CURRENT_MONTH == null
					|| currentTimeMillis - currentMonthGenerateMillis 
						>= currentMonthCheckTimeout) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(currentTimeMillis);
				// Need to re-genereate the STR_YESTERDAY value because it expires
				currentMonthGenerateMillis = currentTimeMillis;
				currentMonthCheckTimeout = getMilliSecondsToNextDateUnit(DateUnit.MONTHLY,
						cal);
				logger.debug(
						"Regenerate the last month string. next checkout millis is: {}",
						lastMonthCheckTimeout);

				StringBuilder buf = new StringBuilder(10);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				buf.append(year).append('-');
				if (month < 10) {
					buf.append('0');
				}
				buf.append(month);
				STR_CURRENT_MONTH = buf.toString();
			} else {
				long passed = currentMonthCheckTimeout - (currentTimeMillis - currentMonthGenerateMillis);
//				logger.debug("Need {} millisecond to re-generate last_month value",
//						passed);
			}
			return STR_CURRENT_MONTH;
		} finally {
			currMonthLock.unlock();
		}
	}

	/**
	 * Every rank will have a expire seconds, like a daily rank, weekly rank or
	 * monthly rank. We will calculate the rank expire time here.
	 * 
	 * @return
	 */
	public static final int getSecondsToNextDateUnit(DateUnit dateUnit,
			Calendar currentTime) {
		int seconds = (int)(getMilliSecondsToNextDateUnit(dateUnit, currentTime)/1000);

		logger.debug("The seconds will be {} to next DateUnit {}", seconds,
				dateUnit);
		return seconds;
	}
	
	/**
	 * Calculate the seconds between current time and the given time clock.
	 * The time clock format is like: "07:30", "11:15" or "14:40" etc.
	 * 
	 * @return
	 */
	public static final int getSecondsToTimeClock(long currentTimeMillis, String timeClock) {
		if ( timeClock == null || timeClock.length() != 5 ) {
			logger.debug("#getSecondsToTimeClock: Wrong format for timeClock: {}", timeClock);
			return 0;
		}
		int hour = StringUtil.toInt(timeClock.substring(0, 2), -1);
		int minutes = StringUtil.toInt(timeClock.substring(3), -1);
		if ( hour != -1 && minutes != -1 ) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(currentTimeMillis);
			Calendar target = (Calendar)cal.clone();
			target.set(Calendar.HOUR_OF_DAY, hour);
			target.set(Calendar.MINUTE, minutes);
			long targetMillis = target.getTimeInMillis();
			int seconds = (int)((targetMillis - currentTimeMillis)/1000);
			return seconds;
		} else {
			logger.debug("#getSecondsToTimeClock: Wrong format for timeClock: {}", timeClock);
			return 0;
		}
	}
	
	/**
	 * Every rank will have a expire seconds, like a daily rank, weekly rank or
	 * monthly rank. We will calculate the rank expire time here.
	 * 
	 * @return
	 */
	public static final long getMilliSecondsToNextDateUnit(DateUnit dateUnit,
			Calendar currentTime) {
		long milliSeconds = Integer.MAX_VALUE;
		if (currentTime == null) {
			currentTime = Calendar.getInstance();
		}
		Calendar cal = (Calendar) currentTime.clone();
		switch (dateUnit) {
			case DAILY:
				long timeMillis = cal.getTimeInMillis();
				cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 1);
				cal.set(Calendar.MILLISECOND, 0);
				long nextDayMillis = cal.getTimeInMillis();
				milliSeconds = nextDayMillis - timeMillis;
				break;
			case WEEKLY:
				timeMillis = cal.getTimeInMillis();
				cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) + 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 1);
				cal.set(Calendar.MILLISECOND, 0);
				nextDayMillis = cal.getTimeInMillis();
				milliSeconds = nextDayMillis - timeMillis;
				break;
			case MONTHLY:
				timeMillis = cal.getTimeInMillis();
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 1);
				cal.set(Calendar.MILLISECOND, 0);
				nextDayMillis = cal.getTimeInMillis();
				milliSeconds = nextDayMillis - timeMillis;
				break;
			default:
				logger.warn("#getMilliSecondsToNextDateUnit: Unsupported DateUnit:{}",
						dateUnit);
				break;
		}

		logger.debug("The millis seconds will be {} to next DateUnit {}",
				milliSeconds, dateUnit);
		return milliSeconds;
	}
	
	/**
	 * Format the Date object to string with format "yyyy-MM-dd"
	 * @param date
	 * @return
	 */
	public static final String formatDate(Date date) {
		if ( date != null ) {
			synchronized (SDF) {
				return SDF.format(date);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Format the Date object to string with format "yyyy-MM-dd HH:mm:ss"
	 * @param date
	 * @return
	 */
	public static final String formatDateTime(Date date) {
		if ( date != null ) {
			synchronized (SDF_DETAIL) {
				return SDF_DETAIL.format(date);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Format the Date object to string with format "HH:mm:ss"
	 * @param date
	 * @return
	 */
	public static final String formatTime(Date date) {
		if ( date != null ) {
			synchronized (SDF_TIME) {
				return SDF_TIME.format(date);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Apple IAP receipt purchase date string format:
	 * "purchase-date-pst" = "2012-07-12 05:54:35 America/Los_Angeles";
	 * 
	 * @param dateStr
	 * @return
	 */
	public static final Date parseApplePurchaseDate(String dateStr) {
		try {
			Date date = SDF_APPLE.parse(dateStr);
			return date;
		} catch (ParseException e) {
		}
		return null;
	}

	public static enum DateUnit {
		DAILY, WEEKLY, MONTHLY,
	}
}
