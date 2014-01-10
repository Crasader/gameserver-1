package com.xinqihd.sns.gameserver.jedis;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
public class JedisDummyAdapter extends JedisAdapter {
	
	public JedisDummyAdapter() {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#ping()
	 */
	@Override
	public String ping() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#set(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String set(String key, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#set(byte[], byte[])
	 */
	@Override
	public String set(byte[] key, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#get(byte[])
	 */
	@Override
	public byte[] get(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#quit()
	 */
	@Override
	public String quit() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#exists(java.lang.String)
	 */
	@Override
	public Boolean exists(String key) {
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#exists(byte[])
	 */
	@Override
	public Boolean exists(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#del(java.lang.String[])
	 */
	@Override
	public Long del(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#del(byte[][])
	 */
	@Override
	public Long del(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#type(java.lang.String)
	 */
	@Override
	public String type(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#type(byte[])
	 */
	@Override
	public String type(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#flushDB()
	 */
	@Override
	public String flushDB() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#keys(java.lang.String)
	 */
	@Override
	public Set<String> keys(String pattern) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#keys(byte[])
	 */
	@Override
	public Set<byte[]> keys(byte[] pattern) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#randomKey()
	 */
	@Override
	public String randomKey() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rename(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String rename(String oldkey, String newkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#randomBinaryKey()
	 */
	@Override
	public byte[] randomBinaryKey() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rename(byte[], byte[])
	 */
	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#renamenx(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long renamenx(String oldkey, String newkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#renamenx(byte[], byte[])
	 */
	@Override
	public Long renamenx(byte[] oldkey, byte[] newkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#dbSize()
	 */
	@Override
	public Long dbSize() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#expire(java.lang.String,
	 * int)
	 */
	@Override
	public Long expire(String key, int seconds) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#expire(byte[], int)
	 */
	@Override
	public Long expire(byte[] key, int seconds) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#expireAt(java.lang.String,
	 * long)
	 */
	@Override
	public Long expireAt(String key, long unixTime) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#expireAt(byte[], long)
	 */
	@Override
	public Long expireAt(byte[] key, long unixTime) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#toString()
	 */
	@Override
	public String toString() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#ttl(java.lang.String)
	 */
	@Override
	public Long ttl(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#ttl(byte[])
	 */
	@Override
	public Long ttl(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#select(int)
	 */
	@Override
	public String select(int index) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#move(java.lang.String,
	 * int)
	 */
	@Override
	public Long move(String key, int dbIndex) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#move(byte[], int)
	 */
	@Override
	public Long move(byte[] key, int dbIndex) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#flushAll()
	 */
	@Override
	public String flushAll() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getSet(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getSet(String key, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getSet(byte[], byte[])
	 */
	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#mget(java.lang.String[])
	 */
	@Override
	public List<String> mget(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#mget(byte[][])
	 */
	@Override
	public List<byte[]> mget(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setnx(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long setnx(String key, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setnx(byte[], byte[])
	 */
	@Override
	public Long setnx(byte[] key, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setex(java.lang.String,
	 * int, java.lang.String)
	 */
	@Override
	public String setex(String key, int seconds, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setex(byte[], int,
	 * byte[])
	 */
	@Override
	public String setex(byte[] key, int seconds, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#mset(java.lang.String[])
	 */
	@Override
	public String mset(String... keysvalues) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#mset(byte[][])
	 */
	@Override
	public String mset(byte[]... keysvalues) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#msetnx(java.lang.String[])
	 */
	@Override
	public Long msetnx(String... keysvalues) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#msetnx(byte[][])
	 */
	@Override
	public Long msetnx(byte[]... keysvalues) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#decrBy(java.lang.String,
	 * long)
	 */
	@Override
	public Long decrBy(String key, long integer) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#decrBy(byte[], long)
	 */
	@Override
	public Long decrBy(byte[] key, long integer) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#decr(java.lang.String)
	 */
	@Override
	public Long decr(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#decr(byte[])
	 */
	@Override
	public Long decr(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#incrBy(java.lang.String,
	 * long)
	 */
	@Override
	public Long incrBy(String key, long integer) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#incrBy(byte[], long)
	 */
	@Override
	public Long incrBy(byte[] key, long integer) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#incr(java.lang.String)
	 */
	@Override
	public Long incr(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#incr(byte[])
	 */
	@Override
	public Long incr(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#append(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long append(String key, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#append(byte[], byte[])
	 */
	@Override
	public Long append(byte[] key, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#substr(java.lang.String,
	 * int, int)
	 */
	@Override
	public String substr(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#substr(byte[], int, int)
	 */
	@Override
	public byte[] substr(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hset(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Long hset(String key, String field, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hset(byte[], byte[],
	 * byte[])
	 */
	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hget(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String hget(String key, String field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hsetnx(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Long hsetnx(String key, String field, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hget(byte[], byte[])
	 */
	@Override
	public byte[] hget(byte[] key, byte[] field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hsetnx(byte[], byte[],
	 * byte[])
	 */
	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hmset(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	public String hmset(String key, Map<String, String> hash) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hmset(byte[],
	 * java.util.Map)
	 */
	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hmget(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public List<String> hmget(String key, String... fields) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hmget(byte[], byte[][])
	 */
	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#hincrBy(java.lang.String,
	 * java.lang.String, long)
	 */
	@Override
	public Long hincrBy(String key, String field, long value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hincrBy(byte[], byte[],
	 * long)
	 */
	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#hexists(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Boolean hexists(String key, String field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hdel(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long hdel(String key, String field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hexists(byte[], byte[])
	 */
	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hlen(java.lang.String)
	 */
	@Override
	public Long hlen(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hdel(byte[], byte[])
	 */
	@Override
	public Long hdel(byte[] key, byte[] field) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hkeys(java.lang.String)
	 */
	@Override
	public Set<String> hkeys(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hlen(byte[])
	 */
	@Override
	public Long hlen(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hvals(java.lang.String)
	 */
	@Override
	public List<String> hvals(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hkeys(byte[])
	 */
	@Override
	public Set<byte[]> hkeys(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#hgetAll(java.lang.String)
	 */
	@Override
	public Map<String, String> hgetAll(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hvals(byte[])
	 */
	@Override
	public List<byte[]> hvals(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpush(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long rpush(String key, String string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#hgetAll(byte[])
	 */
	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpush(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long lpush(String key, String string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpush(byte[], byte[])
	 */
	@Override
	public Long rpush(byte[] key, byte[] string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#llen(java.lang.String)
	 */
	@Override
	public Long llen(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpush(byte[], byte[])
	 */
	@Override
	public Long lpush(byte[] key, byte[] string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lrange(java.lang.String,
	 * long, long)
	 */
	@Override
	public List<String> lrange(String key, long start, long end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#llen(byte[])
	 */
	@Override
	public Long llen(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lrange(byte[], int, int)
	 */
	@Override
	public List<byte[]> lrange(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#ltrim(java.lang.String,
	 * long, long)
	 */
	@Override
	public String ltrim(String key, long start, long end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#ltrim(byte[], int, int)
	 */
	@Override
	public String ltrim(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lindex(java.lang.String,
	 * long)
	 */
	@Override
	public String lindex(String key, long index) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lindex(byte[], int)
	 */
	@Override
	public byte[] lindex(byte[] key, int index) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lset(java.lang.String,
	 * long, java.lang.String)
	 */
	@Override
	public String lset(String key, long index, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lset(byte[], int,
	 * byte[])
	 */
	@Override
	public String lset(byte[] key, int index, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lrem(java.lang.String,
	 * long, java.lang.String)
	 */
	@Override
	public Long lrem(String key, long count, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lrem(byte[], int,
	 * byte[])
	 */
	@Override
	public Long lrem(byte[] key, int count, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpop(java.lang.String)
	 */
	@Override
	public String lpop(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpop(java.lang.String)
	 */
	@Override
	public String rpop(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpop(byte[])
	 */
	@Override
	public byte[] lpop(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpoplpush(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String rpoplpush(String srckey, String dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpop(byte[])
	 */
	@Override
	public byte[] rpop(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpoplpush(byte[],
	 * byte[])
	 */
	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sadd(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long sadd(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#smembers(java.lang.String)
	 */
	@Override
	public Set<String> smembers(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sadd(byte[], byte[])
	 */
	@Override
	public Long sadd(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#srem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long srem(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#smembers(byte[])
	 */
	@Override
	public Set<byte[]> smembers(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#spop(java.lang.String)
	 */
	@Override
	public String spop(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#srem(byte[], byte[])
	 */
	@Override
	public Long srem(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#smove(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Long smove(String srckey, String dstkey, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#spop(byte[])
	 */
	@Override
	public byte[] spop(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#smove(byte[], byte[],
	 * byte[])
	 */
	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#scard(java.lang.String)
	 */
	@Override
	public Long scard(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sismember(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Boolean sismember(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#scard(byte[])
	 */
	@Override
	public Long scard(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sinter(java.lang.String[])
	 */
	@Override
	public Set<String> sinter(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sismember(byte[],
	 * byte[])
	 */
	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sinter(byte[][])
	 */
	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sinterstore(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Long sinterstore(String dstkey, String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sunion(java.lang.String[])
	 */
	@Override
	public Set<String> sunion(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sinterstore(byte[],
	 * byte[][])
	 */
	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sunion(byte[][])
	 */
	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sunionstore(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Long sunionstore(String dstkey, String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sdiff(java.lang.String[])
	 */
	@Override
	public Set<String> sdiff(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sunionstore(byte[],
	 * byte[][])
	 */
	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sdiff(byte[][])
	 */
	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#sdiffstore(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#srandmember(java.lang.String)
	 */
	@Override
	public String srandmember(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sdiffstore(byte[],
	 * byte[][])
	 */
	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zadd(java.lang.String,
	 * double, java.lang.String)
	 */
	@Override
	public Long zadd(String key, double score, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#srandmember(byte[])
	 */
	@Override
	public byte[] srandmember(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zadd(byte[], double,
	 * byte[])
	 */
	@Override
	public Long zadd(byte[] key, double score, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrange(java.lang.String,
	 * int, int)
	 */
	@Override
	public Set<String> zrange(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long zrem(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrange(byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrange(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zincrby(java.lang.String,
	 * double, java.lang.String)
	 */
	@Override
	public Double zincrby(String key, double score, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrem(byte[], byte[])
	 */
	@Override
	public Long zrem(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zincrby(byte[], double,
	 * byte[])
	 */
	@Override
	public Double zincrby(byte[] key, double score, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrank(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long zrank(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrank(byte[], byte[])
	 */
	@Override
	public Long zrank(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrank(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long zrevrank(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrank(byte[], byte[])
	 */
	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrange(java.lang.String,
	 * int, int)
	 */
	@Override
	public Set<String> zrevrange(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeWithScores(java.lang
	 * .String, int, int)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeWithScores(java.
	 * lang.String, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zcard(java.lang.String)
	 */
	@Override
	public Long zcard(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrange(byte[], int,
	 * int)
	 */
	@Override
	public Set<byte[]> zrevrange(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeWithScores(byte[],
	 * int, int)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zscore(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Double zscore(String key, String member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeWithScores(byte[],
	 * int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zcard(byte[])
	 */
	@Override
	public Long zcard(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#watch(java.lang.String[])
	 */
	@Override
	public String watch(String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(java.lang.String)
	 */
	@Override
	public List<String> sort(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zscore(byte[], byte[])
	 */
	@Override
	public Double zscore(byte[] key, byte[] member) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#multi()
	 */
	@Override
	public Transaction multi() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(java.lang.String,
	 * redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#multi(redis.clients.jedis
	 * .TransactionBlock)
	 */
	@Override
	public List<Object> multi(TransactionBlock jedisTransaction) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#connect()
	 */
	@Override
	public void connect() {
		super.connect();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#disconnect()
	 */
	@Override
	public void disconnect() {
		super.disconnect();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#watch(byte[][])
	 */
	@Override
	public String watch(byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#unwatch()
	 */
	@Override
	public String unwatch() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(byte[])
	 */
	@Override
	public List<byte[]> sort(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#blpop(int,
	 * java.lang.String[])
	 */
	@Override
	public List<String> blpop(int timeout, String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(byte[],
	 * redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#blpop(int, byte[][])
	 */
	@Override
	public List<byte[]> blpop(int timeout, byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(java.lang.String,
	 * redis.clients.jedis.SortingParams, java.lang.String)
	 */
	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long sort(String key, String dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#brpop(int,
	 * java.lang.String[])
	 */
	@Override
	public List<String> brpop(int timeout, String... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(byte[],
	 * redis.clients.jedis.SortingParams, byte[])
	 */
	@Override
	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sort(byte[], byte[])
	 */
	@Override
	public Long sort(byte[] key, byte[] dstkey) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#brpop(int, byte[][])
	 */
	@Override
	public List<byte[]> brpop(int timeout, byte[]... keys) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#auth(java.lang.String)
	 */
	@Override
	public String auth(String password) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#subscribe(redis.clients.jedis
	 * .JedisPubSub, java.lang.String[])
	 */
	@Override
	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		super.subscribe(jedisPubSub, channels);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#publish(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long publish(String channel, String message) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#psubscribe(redis.clients.
	 * jedis.JedisPubSub, java.lang.String[])
	 */
	@Override
	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		super.psubscribe(jedisPubSub, patterns);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zcount(java.lang.String,
	 * double, double)
	 */
	@Override
	public Long zcount(String key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(java.lang.String
	 * , double, double)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#pipelined(redis.clients.jedis
	 * .PipelineBlock)
	 */
	@Override
	public List<Object> pipelined(PipelineBlock jedisPipeline) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#pipelined()
	 */
	@Override
	public Pipeline pipelined() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zcount(byte[], double,
	 * double)
	 */
	@Override
	public Long zcount(byte[] key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(byte[],
	 * double, double)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(java.lang.String
	 * , double, double, int, int)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(byte[],
	 * byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScore(byte[],
	 * double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScoreWithScores(java
	 * .lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScoreWithScores(byte
	 * [], double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScoreWithScores(java
	 * .lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrangeByScoreWithScores(byte
	 * [], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(java.lang
	 * .String, double, double)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(java.lang
	 * .String, double, double, int, int)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScoreWithScores
	 * (java.lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScoreWithScores
	 * (java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zremrangeByRank(java.lang
	 * .String, int, int)
	 */
	@Override
	public Long zremrangeByRank(String key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zremrangeByScore(java.lang
	 * .String, double, double)
	 */
	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(byte[],
	 * double, double)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(byte[],
	 * byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zunionstore(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Long zunionstore(String dstkey, String... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScore(byte[],
	 * double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
			int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScoreWithScores
	 * (byte[], double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zrevrangeByScoreWithScores
	 * (byte[], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zremrangeByRank(byte[],
	 * int, int)
	 */
	@Override
	public Long zremrangeByRank(byte[] key, int start, int end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zremrangeByScore(byte[],
	 * double, double)
	 */
	@Override
	public Long zremrangeByScore(byte[] key, double start, double end) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zunionstore(java.lang.String,
	 * redis.clients.jedis.ZParams, java.lang.String[])
	 */
	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zunionstore(byte[],
	 * byte[][])
	 */
	@Override
	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zinterstore(java.lang.String,
	 * java.lang.String[])
	 */
	@Override
	public Long zinterstore(String dstkey, String... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zunionstore(byte[],
	 * redis.clients.jedis.ZParams, byte[][])
	 */
	@Override
	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#zinterstore(java.lang.String,
	 * redis.clients.jedis.ZParams, java.lang.String[])
	 */
	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zinterstore(byte[],
	 * byte[][])
	 */
	@Override
	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#strlen(java.lang.String)
	 */
	@Override
	public Long strlen(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpushx(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long lpushx(String key, String string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#persist(java.lang.String)
	 */
	@Override
	public Long persist(String key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#zinterstore(byte[],
	 * redis.clients.jedis.ZParams, byte[][])
	 */
	@Override
	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpushx(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long rpushx(String key, String string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#echo(java.lang.String)
	 */
	@Override
	public String echo(String string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#linsert(java.lang.String,
	 * redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#brpoplpush(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setbit(java.lang.String,
	 * long, boolean)
	 */
	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getbit(java.lang.String,
	 * long)
	 */
	@Override
	public Boolean getbit(String key, long offset) {
		return Boolean.FALSE;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#setrange(java.lang.String,
	 * long, java.lang.String)
	 */
	@Override
	public Long setrange(String key, long offset, String value) {
		return 0l;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#getrange(java.lang.String,
	 * long, long)
	 */
	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#save()
	 */
	@Override
	public String save() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#bgsave()
	 */
	@Override
	public String bgsave() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#bgrewriteaof()
	 */
	@Override
	public String bgrewriteaof() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lastsave()
	 */
	@Override
	public Long lastsave() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#shutdown()
	 */
	@Override
	public String shutdown() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#info()
	 */
	@Override
	public String info() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#monitor(redis.clients.jedis
	 * .JedisMonitor)
	 */
	@Override
	public void monitor(JedisMonitor jedisMonitor) {
		super.monitor(jedisMonitor);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#slaveof(java.lang.String,
	 * int)
	 */
	@Override
	public String slaveof(String host, int port) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#slaveofNoOne()
	 */
	@Override
	public String slaveofNoOne() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#configGet(java.lang.String)
	 */
	@Override
	public List<String> configGet(String pattern) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#configResetStat()
	 */
	@Override
	public String configResetStat() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#configSet(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String configSet(String parameter, String value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#strlen(byte[])
	 */
	@Override
	public Long strlen(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#sync()
	 */
	@Override
	public void sync() {
		super.sync();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#lpushx(byte[], byte[])
	 */
	@Override
	public Long lpushx(byte[] key, byte[] string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#persist(byte[])
	 */
	@Override
	public Long persist(byte[] key) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#rpushx(byte[], byte[])
	 */
	@Override
	public Long rpushx(byte[] key, byte[] string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#echo(byte[])
	 */
	@Override
	public byte[] echo(byte[] string) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#linsert(byte[],
	 * redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	@Override
	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#debug(redis.clients.jedis
	 * .DebugParams)
	 */
	@Override
	public String debug(DebugParams params) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getClient()
	 */
	@Override
	public Client getClient() {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#brpoplpush(byte[],
	 * byte[], int)
	 */
	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setbit(byte[], long,
	 * byte[])
	 */
	@Override
	public Boolean setbit(byte[] key, long offset, byte[] value) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getbit(byte[], long)
	 */
	@Override
	public Boolean getbit(byte[] key, long offset) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#setrange(byte[], long,
	 * byte[])
	 */
	@Override
	public long setrange(byte[] key, long offset, byte[] value) {
		return 0l;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getrange(byte[], long,
	 * long)
	 */
	@Override
	public String getrange(byte[] key, long startOffset, long endOffset) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#publish(byte[], byte[])
	 */
	@Override
	public Long publish(byte[] channel, byte[] message) {
		return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#subscribe(redis.clients.jedis
	 * .BinaryJedisPubSub, byte[][])
	 */
	@Override
	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		super.subscribe(jedisPubSub, channels);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.xinqihd.sns.gameserver.jedis.JedisAdapter#psubscribe(redis.clients.
	 * jedis.BinaryJedisPubSub, byte[][])
	 */
	@Override
	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		super.psubscribe(jedisPubSub, patterns);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAdapter#getDB()
	 */
	@Override
	public Long getDB() {
		return null;
	}
}