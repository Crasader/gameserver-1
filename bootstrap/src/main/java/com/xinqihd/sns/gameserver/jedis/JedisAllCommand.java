package com.xinqihd.sns.gameserver.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.BinaryClient.LIST_POSITION;

public interface JedisAllCommand extends JedisCommands, BinaryJedisCommands {

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#ping()
	 */
	public abstract String ping();

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#set(java.lang.String, java.lang.String)
	 */
	public abstract String set(String key, String value);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#get(java.lang.String)
	 */
	public abstract String get(String key);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#set(byte[], byte[])
	 */
	public abstract String set(byte[] key, byte[] value);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#get(byte[])
	 */
	public abstract byte[] get(byte[] key);

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#quit()
	 */
	public abstract String quit();

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#exists(java.lang.String)
	 */
	public abstract Boolean exists(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#exists(byte[])
	 */
	public abstract Boolean exists(byte[] key);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#del(java.lang.String[])
	 */
	public abstract Long del(String... keys);

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public abstract int hashCode();

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#del(byte[][])
	 */
	public abstract Long del(byte[]... keys);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#type(java.lang.String)
	 */
	public abstract String type(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#type(byte[])
	 */
	public abstract String type(byte[] key);

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#flushDB()
	 */
	public abstract String flushDB();

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.Jedis#keys(java.lang.String)
	 */
	public abstract Set<String> keys(String pattern);

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#keys(byte[])
	 */
	public abstract Set<byte[]> keys(byte[] pattern);

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object obj);

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#randomKey()
	 */
	public abstract String randomKey();

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Jedis#rename(java.lang.String, java.lang.String)
	 */
	public abstract String rename(String oldkey, String newkey);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#randomBinaryKey()
	 */
	public abstract byte[] randomBinaryKey();

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#rename(byte[], byte[])
	 */
	public abstract String rename(byte[] oldkey, byte[] newkey);

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Jedis#renamenx(java.lang.String, java.lang.String)
	 */
	public abstract Long renamenx(String oldkey, String newkey);

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#renamenx(byte[], byte[])
	 */
	public abstract Long renamenx(byte[] oldkey, byte[] newkey);

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#dbSize()
	 */
	public abstract Long dbSize();

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.Jedis#expire(java.lang.String, int)
	 */
	public abstract Long expire(String key, int seconds);

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#expire(byte[], int)
	 */
	public abstract Long expire(byte[] key, int seconds);

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.Jedis#expireAt(java.lang.String, long)
	 */
	public abstract Long expireAt(String key, long unixTime);

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#expireAt(byte[], long)
	 */
	public abstract Long expireAt(byte[] key, long unixTime);

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#ttl(java.lang.String)
	 */
	public abstract Long ttl(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#ttl(byte[])
	 */
	public abstract Long ttl(byte[] key);

	/**
	 * @param index
	 * @return
	 * @see redis.clients.jedis.Jedis#select(int)
	 */
	public abstract String select(int index);

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.Jedis#move(java.lang.String, int)
	 */
	public abstract Long move(String key, int dbIndex);

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#move(byte[], int)
	 */
	public abstract Long move(byte[] key, int dbIndex);

	/**
	 * @return
	 * @see redis.clients.jedis.Jedis#flushAll()
	 */
	public abstract String flushAll();

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#getSet(java.lang.String, java.lang.String)
	 */
	public abstract String getSet(String key, String value);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#getSet(byte[], byte[])
	 */
	public abstract byte[] getSet(byte[] key, byte[] value);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#mget(java.lang.String[])
	 */
	public abstract List<String> mget(String... keys);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#mget(byte[][])
	 */
	public abstract List<byte[]> mget(byte[]... keys);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#setnx(java.lang.String, java.lang.String)
	 */
	public abstract Long setnx(String key, String value);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#setnx(byte[], byte[])
	 */
	public abstract Long setnx(byte[] key, byte[] value);

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#setex(java.lang.String, int,
	 *      java.lang.String)
	 */
	public abstract String setex(String key, int seconds, String value);

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#setex(byte[], int, byte[])
	 */
	public abstract String setex(byte[] key, int seconds, byte[] value);

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Jedis#mset(java.lang.String[])
	 */
	public abstract String mset(String... keysvalues);

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#mset(byte[][])
	 */
	public abstract String mset(byte[]... keysvalues);

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Jedis#msetnx(java.lang.String[])
	 */
	public abstract Long msetnx(String... keysvalues);

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#msetnx(byte[][])
	 */
	public abstract Long msetnx(byte[]... keysvalues);

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Jedis#decrBy(java.lang.String, long)
	 */
	public abstract Long decrBy(String key, long integer);

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#decrBy(byte[], long)
	 */
	public abstract Long decrBy(byte[] key, long integer);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#decr(java.lang.String)
	 */
	public abstract Long decr(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#decr(byte[])
	 */
	public abstract Long decr(byte[] key);

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Jedis#incrBy(java.lang.String, long)
	 */
	public abstract Long incrBy(String key, long integer);

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#incrBy(byte[], long)
	 */
	public abstract Long incrBy(byte[] key, long integer);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#incr(java.lang.String)
	 */
	public abstract Long incr(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#incr(byte[])
	 */
	public abstract Long incr(byte[] key);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#append(java.lang.String, java.lang.String)
	 */
	public abstract Long append(String key, String value);

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#append(byte[], byte[])
	 */
	public abstract Long append(byte[] key, byte[] value);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#substr(java.lang.String, int, int)
	 */
	public abstract String substr(String key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#substr(byte[], int, int)
	 */
	public abstract byte[] substr(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#hset(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public abstract Long hset(String key, String field, String value);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hset(byte[], byte[], byte[])
	 */
	public abstract Long hset(byte[] key, byte[] field, byte[] value);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Jedis#hget(java.lang.String, java.lang.String)
	 */
	public abstract String hget(String key, String field);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#hsetnx(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public abstract Long hsetnx(String key, String field, String value);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hget(byte[], byte[])
	 */
	public abstract byte[] hget(byte[] key, byte[] field);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hsetnx(byte[], byte[], byte[])
	 */
	public abstract Long hsetnx(byte[] key, byte[] field, byte[] value);

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.Jedis#hmset(java.lang.String, java.util.Map)
	 */
	public abstract String hmset(String key, Map<String, String> hash);

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hmset(byte[], java.util.Map)
	 */
	public abstract String hmset(byte[] key, Map<byte[], byte[]> hash);

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.Jedis#hmget(java.lang.String, java.lang.String[])
	 */
	public abstract List<String> hmget(String key, String... fields);

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hmget(byte[], byte[][])
	 */
	public abstract List<byte[]> hmget(byte[] key, byte[]... fields);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#hincrBy(java.lang.String, java.lang.String,
	 *      long)
	 */
	public abstract Long hincrBy(String key, String field, long value);

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hincrBy(byte[], byte[], long)
	 */
	public abstract Long hincrBy(byte[] key, byte[] field, long value);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Jedis#hexists(java.lang.String, java.lang.String)
	 */
	public abstract Boolean hexists(String key, String field);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Jedis#hdel(java.lang.String, java.lang.String)
	 */
	public abstract Long hdel(String key, String field);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hexists(byte[], byte[])
	 */
	public abstract Boolean hexists(byte[] key, byte[] field);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#hlen(java.lang.String)
	 */
	public abstract Long hlen(String key);

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hdel(byte[], byte[])
	 */
	public abstract Long hdel(byte[] key, byte[] field);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#hkeys(java.lang.String)
	 */
	public abstract Set<String> hkeys(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hlen(byte[])
	 */
	public abstract Long hlen(byte[] key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#hvals(java.lang.String)
	 */
	public abstract List<String> hvals(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hkeys(byte[])
	 */
	public abstract Set<byte[]> hkeys(byte[] key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#hgetAll(java.lang.String)
	 */
	public abstract Map<String, String> hgetAll(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hvals(byte[])
	 */
	public abstract List<byte[]> hvals(byte[] key);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Jedis#rpush(java.lang.String, java.lang.String)
	 */
	public abstract Long rpush(String key, String string);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#hgetAll(byte[])
	 */
	public abstract Map<byte[], byte[]> hgetAll(byte[] key);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Jedis#lpush(java.lang.String, java.lang.String)
	 */
	public abstract Long lpush(String key, String string);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#rpush(byte[], byte[])
	 */
	public abstract Long rpush(byte[] key, byte[] string);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#llen(java.lang.String)
	 */
	public abstract Long llen(String key);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lpush(byte[], byte[])
	 */
	public abstract Long lpush(byte[] key, byte[] string);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#lrange(java.lang.String, long, long)
	 */
	public abstract List<String> lrange(String key, long start, long end);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#llen(byte[])
	 */
	public abstract Long llen(byte[] key);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lrange(byte[], int, int)
	 */
	public abstract List<byte[]> lrange(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#ltrim(java.lang.String, long, long)
	 */
	public abstract String ltrim(String key, long start, long end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#ltrim(byte[], int, int)
	 */
	public abstract String ltrim(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.Jedis#lindex(java.lang.String, long)
	 */
	public abstract String lindex(String key, long index);

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lindex(byte[], int)
	 */
	public abstract byte[] lindex(byte[] key, int index);

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#lset(java.lang.String, long,
	 *      java.lang.String)
	 */
	public abstract String lset(String key, long index, String value);

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lset(byte[], int, byte[])
	 */
	public abstract String lset(byte[] key, int index, byte[] value);

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#lrem(java.lang.String, long,
	 *      java.lang.String)
	 */
	public abstract Long lrem(String key, long count, String value);

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lrem(byte[], int, byte[])
	 */
	public abstract Long lrem(byte[] key, int count, byte[] value);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#lpop(java.lang.String)
	 */
	public abstract String lpop(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#rpop(java.lang.String)
	 */
	public abstract String rpop(String key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lpop(byte[])
	 */
	public abstract byte[] lpop(byte[] key);

	/**
	 * @param srckey
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Jedis#rpoplpush(java.lang.String,
	 *      java.lang.String)
	 */
	public abstract String rpoplpush(String srckey, String dstkey);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#rpop(byte[])
	 */
	public abstract byte[] rpop(byte[] key);

	/**
	 * @param srckey
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#rpoplpush(byte[], byte[])
	 */
	public abstract byte[] rpoplpush(byte[] srckey, byte[] dstkey);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#sadd(java.lang.String, java.lang.String)
	 */
	public abstract Long sadd(String key, String member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#smembers(java.lang.String)
	 */
	public abstract Set<String> smembers(String key);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sadd(byte[], byte[])
	 */
	public abstract Long sadd(byte[] key, byte[] member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#srem(java.lang.String, java.lang.String)
	 */
	public abstract Long srem(String key, String member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#smembers(byte[])
	 */
	public abstract Set<byte[]> smembers(byte[] key);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#spop(java.lang.String)
	 */
	public abstract String spop(String key);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#srem(byte[], byte[])
	 */
	public abstract Long srem(byte[] key, byte[] member);

	/**
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#smove(java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public abstract Long smove(String srckey, String dstkey, String member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#spop(byte[])
	 */
	public abstract byte[] spop(byte[] key);

	/**
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#smove(byte[], byte[], byte[])
	 */
	public abstract Long smove(byte[] srckey, byte[] dstkey, byte[] member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#scard(java.lang.String)
	 */
	public abstract Long scard(String key);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#sismember(java.lang.String,
	 *      java.lang.String)
	 */
	public abstract Boolean sismember(String key, String member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#scard(byte[])
	 */
	public abstract Long scard(byte[] key);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sinter(java.lang.String[])
	 */
	public abstract Set<String> sinter(String... keys);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sismember(byte[], byte[])
	 */
	public abstract Boolean sismember(byte[] key, byte[] member);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sinter(byte[][])
	 */
	public abstract Set<byte[]> sinter(byte[]... keys);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sinterstore(java.lang.String,
	 *      java.lang.String[])
	 */
	public abstract Long sinterstore(String dstkey, String... keys);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sunion(java.lang.String[])
	 */
	public abstract Set<String> sunion(String... keys);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sinterstore(byte[], byte[][])
	 */
	public abstract Long sinterstore(byte[] dstkey, byte[]... keys);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sunion(byte[][])
	 */
	public abstract Set<byte[]> sunion(byte[]... keys);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sunionstore(java.lang.String,
	 *      java.lang.String[])
	 */
	public abstract Long sunionstore(String dstkey, String... keys);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sdiff(java.lang.String[])
	 */
	public abstract Set<String> sdiff(String... keys);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sunionstore(byte[], byte[][])
	 */
	public abstract Long sunionstore(byte[] dstkey, byte[]... keys);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sdiff(byte[][])
	 */
	public abstract Set<byte[]> sdiff(byte[]... keys);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#sdiffstore(java.lang.String,
	 *      java.lang.String[])
	 */
	public abstract Long sdiffstore(String dstkey, String... keys);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#srandmember(java.lang.String)
	 */
	public abstract String srandmember(String key);

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sdiffstore(byte[], byte[][])
	 */
	public abstract Long sdiffstore(byte[] dstkey, byte[]... keys);

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zadd(java.lang.String, double,
	 *      java.lang.String)
	 */
	public abstract Long zadd(String key, double score, String member);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#srandmember(byte[])
	 */
	public abstract byte[] srandmember(byte[] key);

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zadd(byte[], double, byte[])
	 */
	public abstract Long zadd(byte[] key, double score, byte[] member);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zrange(java.lang.String, int, int)
	 */
	public abstract Set<String> zrange(String key, int start, int end);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zrem(java.lang.String, java.lang.String)
	 */
	public abstract Long zrem(String key, String member);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrange(byte[], int, int)
	 */
	public abstract Set<byte[]> zrange(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zincrby(java.lang.String, double,
	 *      java.lang.String)
	 */
	public abstract Double zincrby(String key, double score, String member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrem(byte[], byte[])
	 */
	public abstract Long zrem(byte[] key, byte[] member);

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zincrby(byte[], double, byte[])
	 */
	public abstract Double zincrby(byte[] key, double score, byte[] member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zrank(java.lang.String, java.lang.String)
	 */
	public abstract Long zrank(String key, String member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrank(byte[], byte[])
	 */
	public abstract Long zrank(byte[] key, byte[] member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrank(java.lang.String, java.lang.String)
	 */
	public abstract Long zrevrank(String key, String member);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrank(byte[], byte[])
	 */
	public abstract Long zrevrank(byte[] key, byte[] member);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrange(java.lang.String, int, int)
	 */
	public abstract Set<String> zrevrange(String key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeWithScores(java.lang.String, int, int)
	 */
	public abstract Set<Tuple> zrangeWithScores(String key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeWithScores(java.lang.String, int,
	 *      int)
	 */
	public abstract Set<Tuple> zrevrangeWithScores(String key, int start, int end);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#zcard(java.lang.String)
	 */
	public abstract Long zcard(String key);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrange(byte[], int, int)
	 */
	public abstract Set<byte[]> zrevrange(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeWithScores(byte[], int, int)
	 */
	public abstract Set<Tuple> zrangeWithScores(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Jedis#zscore(java.lang.String, java.lang.String)
	 */
	public abstract Double zscore(String key, String member);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeWithScores(byte[], int, int)
	 */
	public abstract Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zcard(byte[])
	 */
	public abstract Long zcard(byte[] key);

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#watch(java.lang.String[])
	 */
	public abstract String watch(String... keys);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String)
	 */
	public abstract List<String> sort(String key);

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zscore(byte[], byte[])
	 */
	public abstract Double zscore(byte[] key, byte[] member);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#multi()
	 */
	public abstract Transaction multi();

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String,
	 *      redis.clients.jedis.SortingParams)
	 */
	public abstract List<String> sort(String key, SortingParams sortingParameters);

	/**
	 * @param jedisTransaction
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#multi(redis.clients.jedis.TransactionBlock)
	 */
	public abstract List<Object> multi(TransactionBlock jedisTransaction);

	/**
	 * 
	 * @see redis.clients.jedis.BinaryJedis#connect()
	 */
	public abstract void connect();

	/**
	 * 
	 * @see redis.clients.jedis.BinaryJedis#disconnect()
	 */
	public abstract void disconnect();

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#watch(byte[][])
	 */
	public abstract String watch(byte[]... keys);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#unwatch()
	 */
	public abstract String unwatch();

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[])
	 */
	public abstract List<byte[]> sort(byte[] key);

	/**
	 * @param timeout
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#blpop(int, java.lang.String[])
	 */
	public abstract List<String> blpop(int timeout, String... keys);

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[],
	 *      redis.clients.jedis.SortingParams)
	 */
	public abstract List<byte[]> sort(byte[] key, SortingParams sortingParameters);

	/**
	 * @param timeout
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#blpop(int, byte[][])
	 */
	public abstract List<byte[]> blpop(int timeout, byte[]... keys);

	/**
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String,
	 *      redis.clients.jedis.SortingParams, java.lang.String)
	 */
	public abstract Long sort(String key, SortingParams sortingParameters,
			String dstkey);

	/**
	 * @param key
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Jedis#sort(java.lang.String, java.lang.String)
	 */
	public abstract Long sort(String key, String dstkey);

	/**
	 * @param timeout
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Jedis#brpop(int, java.lang.String[])
	 */
	public abstract List<String> brpop(int timeout, String... keys);

	/**
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[],
	 *      redis.clients.jedis.SortingParams, byte[])
	 */
	public abstract Long sort(byte[] key, SortingParams sortingParameters,
			byte[] dstkey);

	/**
	 * @param key
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#sort(byte[], byte[])
	 */
	public abstract Long sort(byte[] key, byte[] dstkey);

	/**
	 * @param timeout
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#brpop(int, byte[][])
	 */
	public abstract List<byte[]> brpop(int timeout, byte[]... keys);

	/**
	 * @param password
	 * @return
	 * @see redis.clients.jedis.Jedis#auth(java.lang.String)
	 */
	public abstract String auth(String password);

	/**
	 * @param jedisPubSub
	 * @param channels
	 * @see redis.clients.jedis.Jedis#subscribe(redis.clients.jedis.JedisPubSub,
	 *      java.lang.String[])
	 */
	public abstract void subscribe(JedisPubSub jedisPubSub, String... channels);

	/**
	 * @param channel
	 * @param message
	 * @return
	 * @see redis.clients.jedis.Jedis#publish(java.lang.String, java.lang.String)
	 */
	public abstract Long publish(String channel, String message);

	/**
	 * @param jedisPubSub
	 * @param patterns
	 * @see redis.clients.jedis.Jedis#psubscribe(redis.clients.jedis.JedisPubSub,
	 *      java.lang.String[])
	 */
	public abstract void psubscribe(JedisPubSub jedisPubSub, String... patterns);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Jedis#zcount(java.lang.String, double, double)
	 */
	public abstract Long zcount(String key, double min, double max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, double,
	 *      double)
	 */
	public abstract Set<String> zrangeByScore(String key, double min, double max);

	/**
	 * @param jedisPipeline
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#pipelined(redis.clients.jedis.PipelineBlock)
	 */
	public abstract List<Object> pipelined(PipelineBlock jedisPipeline);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#pipelined()
	 */
	public abstract Pipeline pipelined();

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zcount(byte[], double, double)
	 */
	public abstract Long zcount(byte[] key, double min, double max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], double, double)
	 */
	public abstract Set<byte[]> zrangeByScore(byte[] key, double min, double max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public abstract Set<String> zrangeByScore(String key, String min, String max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeByScore(java.lang.String, double,
	 *      double, int, int)
	 */
	public abstract Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], byte[], byte[])
	 */
	public abstract Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScore(byte[], double, double,
	 *      int, int)
	 */
	public abstract Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String,
	 *      double, double)
	 */
	public abstract Set<Tuple> zrangeByScoreWithScores(String key, double min,
			double max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[],
	 *      double, double)
	 */
	public abstract Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
			double max);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Jedis#zrangeByScoreWithScores(java.lang.String,
	 *      double, double, int, int)
	 */
	public abstract Set<Tuple> zrangeByScoreWithScores(String key, double min,
			double max, int offset, int count);

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrangeByScoreWithScores(byte[],
	 *      double, double, int, int)
	 */
	public abstract Set<Tuple> zrangeByScoreWithScores(byte[] key, double min,
			double max, int offset, int count);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, double,
	 *      double)
	 */
	public abstract Set<String> zrevrangeByScore(String key, double max,
			double min);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public abstract Set<String> zrevrangeByScore(String key, String max,
			String min);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeByScore(java.lang.String, double,
	 *      double, int, int)
	 */
	public abstract Set<String> zrevrangeByScore(String key, double max,
			double min, int offset, int count);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String,
	 *      double, double)
	 */
	public abstract Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Jedis#zrevrangeByScoreWithScores(java.lang.String,
	 *      double, double, int, int)
	 */
	public abstract Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zremrangeByRank(java.lang.String, int, int)
	 */
	public abstract Long zremrangeByRank(String key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Jedis#zremrangeByScore(java.lang.String, double,
	 *      double)
	 */
	public abstract Long zremrangeByScore(String key, double start, double end);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], double,
	 *      double)
	 */
	public abstract Set<byte[]> zrevrangeByScore(byte[] key, double max,
			double min);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], byte[],
	 *      byte[])
	 */
	public abstract Set<byte[]> zrevrangeByScore(byte[] key, byte[] max,
			byte[] min);

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Jedis#zunionstore(java.lang.String,
	 *      java.lang.String[])
	 */
	public abstract Long zunionstore(String dstkey, String... sets);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScore(byte[], double,
	 *      double, int, int)
	 */
	public abstract Set<byte[]> zrevrangeByScore(byte[] key, double max,
			double min, int offset, int count);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[],
	 *      double, double)
	 */
	public abstract Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min);

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zrevrangeByScoreWithScores(byte[],
	 *      double, double, int, int)
	 */
	public abstract Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zremrangeByRank(byte[], int, int)
	 */
	public abstract Long zremrangeByRank(byte[] key, int start, int end);

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zremrangeByScore(byte[], double,
	 *      double)
	 */
	public abstract Long zremrangeByScore(byte[] key, double start, double end);

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Jedis#zunionstore(java.lang.String,
	 *      redis.clients.jedis.ZParams, java.lang.String[])
	 */
	public abstract Long zunionstore(String dstkey, ZParams params,
			String... sets);

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zunionstore(byte[], byte[][])
	 */
	public abstract Long zunionstore(byte[] dstkey, byte[]... sets);

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Jedis#zinterstore(java.lang.String,
	 *      java.lang.String[])
	 */
	public abstract Long zinterstore(String dstkey, String... sets);

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zunionstore(byte[],
	 *      redis.clients.jedis.ZParams, byte[][])
	 */
	public abstract Long zunionstore(byte[] dstkey, ZParams params,
			byte[]... sets);

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Jedis#zinterstore(java.lang.String,
	 *      redis.clients.jedis.ZParams, java.lang.String[])
	 */
	public abstract Long zinterstore(String dstkey, ZParams params,
			String... sets);

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zinterstore(byte[], byte[][])
	 */
	public abstract Long zinterstore(byte[] dstkey, byte[]... sets);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#strlen(java.lang.String)
	 */
	public abstract Long strlen(String key);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Jedis#lpushx(java.lang.String, java.lang.String)
	 */
	public abstract Long lpushx(String key, String string);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Jedis#persist(java.lang.String)
	 */
	public abstract Long persist(String key);

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#zinterstore(byte[],
	 *      redis.clients.jedis.ZParams, byte[][])
	 */
	public abstract Long zinterstore(byte[] dstkey, ZParams params,
			byte[]... sets);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Jedis#rpushx(java.lang.String, java.lang.String)
	 */
	public abstract Long rpushx(String key, String string);

	/**
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Jedis#echo(java.lang.String)
	 */
	public abstract String echo(String string);

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#linsert(java.lang.String,
	 *      redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String,
	 *      java.lang.String)
	 */
	public abstract Long linsert(String key, LIST_POSITION where, String pivot,
			String value);

	/**
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return
	 * @see redis.clients.jedis.Jedis#brpoplpush(java.lang.String,
	 *      java.lang.String, int)
	 */
	public abstract String brpoplpush(String source, String destination,
			int timeout);

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#setbit(java.lang.String, long, boolean)
	 */
	public abstract Boolean setbit(String key, long offset, boolean value);

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @see redis.clients.jedis.Jedis#getbit(java.lang.String, long)
	 */
	public abstract Boolean getbit(String key, long offset);

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Jedis#setrange(java.lang.String, long,
	 *      java.lang.String)
	 */
	public abstract Long setrange(String key, long offset, String value);

	/**
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 * @see redis.clients.jedis.Jedis#getrange(java.lang.String, long, long)
	 */
	public abstract String getrange(String key, long startOffset, long endOffset);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#save()
	 */
	public abstract String save();

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#bgsave()
	 */
	public abstract String bgsave();

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#bgrewriteaof()
	 */
	public abstract String bgrewriteaof();

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lastsave()
	 */
	public abstract Long lastsave();

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#shutdown()
	 */
	public abstract String shutdown();

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#info()
	 */
	public abstract String info();

	/**
	 * @param jedisMonitor
	 * @see redis.clients.jedis.BinaryJedis#monitor(redis.clients.jedis.JedisMonitor)
	 */
	public abstract void monitor(JedisMonitor jedisMonitor);

	/**
	 * @param host
	 * @param port
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#slaveof(java.lang.String, int)
	 */
	public abstract String slaveof(String host, int port);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#slaveofNoOne()
	 */
	public abstract String slaveofNoOne();

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#configGet(java.lang.String)
	 */
	public abstract List<String> configGet(String pattern);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#configResetStat()
	 */
	public abstract String configResetStat();

	/**
	 * @param parameter
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#configSet(java.lang.String,
	 *      java.lang.String)
	 */
	public abstract String configSet(String parameter, String value);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#isConnected()
	 */
	public abstract boolean isConnected();

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#strlen(byte[])
	 */
	public abstract Long strlen(byte[] key);

	/**
	 * 
	 * @see redis.clients.jedis.BinaryJedis#sync()
	 */
	public abstract void sync();

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#lpushx(byte[], byte[])
	 */
	public abstract Long lpushx(byte[] key, byte[] string);

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#persist(byte[])
	 */
	public abstract Long persist(byte[] key);

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#rpushx(byte[], byte[])
	 */
	public abstract Long rpushx(byte[] key, byte[] string);

	/**
	 * @param string
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#echo(byte[])
	 */
	public abstract byte[] echo(byte[] string);

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#linsert(byte[],
	 *      redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	public abstract Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value);

	/**
	 * @param params
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#debug(redis.clients.jedis.DebugParams)
	 */
	public abstract String debug(DebugParams params);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#getClient()
	 */
	public abstract Client getClient();

	/**
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#brpoplpush(byte[], byte[], int)
	 */
	public abstract byte[] brpoplpush(byte[] source, byte[] destination,
			int timeout);

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#setbit(byte[], long, byte[])
	 */
	public abstract Boolean setbit(byte[] key, long offset, byte[] value);

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#getbit(byte[], long)
	 */
	public abstract Boolean getbit(byte[] key, long offset);

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#setrange(byte[], long, byte[])
	 */
	public abstract long setrange(byte[] key, long offset, byte[] value);

	/**
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#getrange(byte[], long, long)
	 */
	public abstract String getrange(byte[] key, long startOffset, long endOffset);

	/**
	 * @param channel
	 * @param message
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#publish(byte[], byte[])
	 */
	public abstract Long publish(byte[] channel, byte[] message);

	/**
	 * @param jedisPubSub
	 * @param channels
	 * @see redis.clients.jedis.BinaryJedis#subscribe(redis.clients.jedis.BinaryJedisPubSub,
	 *      byte[][])
	 */
	public abstract void subscribe(BinaryJedisPubSub jedisPubSub,
			byte[]... channels);

	/**
	 * @param jedisPubSub
	 * @param patterns
	 * @see redis.clients.jedis.BinaryJedis#psubscribe(redis.clients.jedis.BinaryJedisPubSub,
	 *      byte[][])
	 */
	public abstract void psubscribe(BinaryJedisPubSub jedisPubSub,
			byte[]... patterns);

	/**
	 * @return
	 * @see redis.clients.jedis.BinaryJedis#getDB()
	 */
	public abstract Long getDB();

}