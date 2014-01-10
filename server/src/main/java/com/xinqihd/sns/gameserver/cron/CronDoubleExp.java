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
public class CronDoubleExp {
		
	public static void main(String[] args) {
		if ( args == null || args.length < 1 ) {
			System.out.println("CronDoubleExp <host:port> [seconds]");
			System.exit(-1);
		}
		String hostId = args[0];
		int seconds = 3600;
		if ( args.length > 1 ) {
			seconds = StringUtil.toInt(args[1], 3600);
		}

		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		endCal.add(Calendar.SECOND, seconds);
		String endDateStr = DateUtil.formatDateTime(endCal.getTime());

		ActivityManager.getInstance().setActivityExpRate(null, 1.0f, seconds);

		String message = Text.text("activity.exprate", endDateStr);
		ServerUtil.sendMessage(hostId, message);

		System.exit(0);
	}
}
