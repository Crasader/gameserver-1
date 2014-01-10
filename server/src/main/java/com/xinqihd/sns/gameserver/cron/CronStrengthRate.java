package com.xinqihd.sns.gameserver.cron;

import java.util.Calendar;

import com.xinqihd.sns.gameserver.db.mongo.ActivityManager;
import com.xinqihd.sns.gameserver.util.DateUtil;
import com.xinqihd.sns.gameserver.util.StringUtil;
import com.xinqihd.sns.gameserver.util.Text;

/**
 * 启动每日双倍经验奖励
 * 
 * 每日中午13:00开放
 * 每日晚上22:00开放
 * 
 * @author wangqi
 *
 */
public class CronStrengthRate {
		
	public static void main(String[] args) {
		if ( args == null || args.length < 1 ) {
			System.out.println("CronStrengthRate <rate> [seconds]");
			System.exit(-1);
		}
		String rateStr = args[0];
		int seconds = 86400*3;
		if ( args.length > 1 ) {
			seconds = StringUtil.toInt(args[1], seconds);
		}

		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.SECOND, seconds);
		String endDateStr = DateUtil.formatDateTime(endCal.getTime());

		float rate = 0.0f;
		rate = Float.parseFloat(rateStr);
		if ( rate > 0 ) {
			ActivityManager.getInstance().setActivityStrengthRate(null, rate, seconds);
			System.out.println("Strength rate will improve " + rateStr + " to " + endDateStr);
		} else {
			System.out.println("Strength rate is " + rate);
		}

		System.exit(0);
	}
}
