package com.xinqihd.sns.gameserver.util;

import java.util.Map;

import com.xinqihd.sns.gameserver.db.UserManager;
import com.xinqihd.sns.gameserver.entity.user.BasicUser;
import com.xinqihd.sns.gameserver.jedis.Jedis;
import com.xinqihd.sns.gameserver.jedis.JedisFactory;

/**
 * 统计元宵猜灯谜活动的中奖玩家
 * 
 * @author wangqi
 *
 */
public class StatPuzzleGameRank {

	public static void main(String[] args) {
		Jedis jedisDB = JedisFactory.getJedisDB();
		String key = "puzzle:question:right";
		Map<String, String> map = jedisDB.hgetAll(key);
		StringBuilder buf = new StringBuilder(2000);
		for ( String puzzleId : map.keySet() ) {
			String userName = map.get(puzzleId);
			BasicUser user = UserManager.getInstance().queryBasicUser(userName);
			if ( user != null ) {
				String roleName = user.getRoleName();
				buf.append(puzzleId).append(":").append(roleName).append('\n');
			} else {
				buf.append(puzzleId).append(":").append("<notfound:"+userName+">").append('\n');
			}
		}
		System.out.println(buf.toString());
	}
	
}
