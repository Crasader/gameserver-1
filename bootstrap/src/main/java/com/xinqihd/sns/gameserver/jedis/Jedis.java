package com.xinqihd.sns.gameserver.jedis;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Pipeline;

/**
 * A Jedis interface both support JedisCommands and JedisCommands
 * @author wangqi
 *
 */
public interface Jedis extends JedisAllCommand {

	public Long del(String... keys);
	
	public Long del(byte[]... keys);

	Set<String> keys(String pattern);

	Set<byte[]> keys(byte[] pattern);

	Pipeline pipelined();

	List<String> blpop(int timeout, String[] keys);

	String quit();

	String flushDB();

	String randomKey();

	String rename(String oldkey, String newkey);

	byte[] randomBinaryKey();

	String rename(byte[] oldkey, byte[] newkey);

	Long renamenx(String oldkey, String newkey);
	
}
