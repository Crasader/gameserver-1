package com.xinqihd.sns.gameserver.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Client;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;

/**
 * It is a synchronized version of Pipeline used with Jedis pool.
 * 
 * @author wangqi
 *
 */
public class SyncPipeline extends Pipeline {
	
	private static final Logger logger = LoggerFactory.getLogger(SyncPipeline.class);

	private Pipeline delegate = null;
	private redis.clients.jedis.Jedis jedis = null;
	private JedisPool pool = null;
	
	public SyncPipeline(Pipeline delegate, redis.clients.jedis.Jedis jedis, JedisPool pool) {
		this.delegate = delegate;
		this.jedis = jedis;
		this.pool = pool;
	}
	
	// delegate method...

	/**
	 * @param client
	 * @see redis.clients.jedis.Pipeline#setClient(redis.clients.jedis.Client)
	 */
	public void setClient(Client client) {
		delegate.setClient(client);
	}

	/**
	 * 
	 * @see redis.clients.jedis.Pipeline#sync()
	 */
	public void sync() {
		final String name = Thread.currentThread().getName();
		try {
			delegate.sync();
//			logger.debug(name+ " pool id {}, resource id {}", this.pool, this.jedis);
		} catch ( Throwable t ) {
			logger.debug(name + " " + t.getMessage()+": pool id {}, resource id {}", this.pool, this.jedis);
		} finally {
			this.pool.returnResource(this.jedis);
		}
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#syncAndReturnAll()
	 */
	public List<Object> syncAndReturnAll() {
		return delegate.syncAndReturnAll();
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#append(java.lang.String, java.lang.String)
	 */
	public Response<Long> append(String key, String value) {
		return delegate.append(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#append(byte[], byte[])
	 */
	public Response<Long> append(byte[] key, byte[] value) {
		return delegate.append(key, value);
	}

	/**
	 * @param args
	 * @return
	 * @see redis.clients.jedis.Pipeline#blpop(java.lang.String[])
	 */
	public Response<List<String>> blpop(String... args) {
		return delegate.blpop(args);
	}

	/**
	 * @param args
	 * @return
	 * @see redis.clients.jedis.Pipeline#blpop(byte[][])
	 */
	public Response<List<String>> blpop(byte[]... args) {
		return delegate.blpop(args);
	}

	/**
	 * @param args
	 * @return
	 * @see redis.clients.jedis.Pipeline#brpop(java.lang.String[])
	 */
	public Response<List<String>> brpop(String... args) {
		return delegate.brpop(args);
	}

	/**
	 * @param args
	 * @return
	 * @see redis.clients.jedis.Pipeline#brpop(byte[][])
	 */
	public Response<List<String>> brpop(byte[]... args) {
		return delegate.brpop(args);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#decr(java.lang.String)
	 */
	public Response<Long> decr(String key) {
		return delegate.decr(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#decr(byte[])
	 */
	public Response<Long> decr(byte[] key) {
		return delegate.decr(key);
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Pipeline#decrBy(java.lang.String, long)
	 */
	public Response<Long> decrBy(String key, long integer) {
		return delegate.decrBy(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Pipeline#decrBy(byte[], long)
	 */
	public Response<Long> decrBy(byte[] key, long integer) {
		return delegate.decrBy(key, integer);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#del(java.lang.String[])
	 */
	public Response<Long> del(String... keys) {
		return delegate.del(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#del(byte[][])
	 */
	public Response<Long> del(byte[]... keys) {
		return delegate.del(keys);
	}

	/**
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#echo(java.lang.String)
	 */
	public Response<String> echo(String string) {
		return delegate.echo(string);
	}

	/**
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#echo(byte[])
	 */
	public Response<String> echo(byte[] string) {
		return delegate.echo(string);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#exists(java.lang.String)
	 */
	public Response<Boolean> exists(String key) {
		return delegate.exists(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#exists(byte[])
	 */
	public Response<Boolean> exists(byte[] key) {
		return delegate.exists(key);
	}

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.Pipeline#expire(java.lang.String, int)
	 */
	public Response<Long> expire(String key, int seconds) {
		return delegate.expire(key, seconds);
	}

	/**
	 * @param key
	 * @param seconds
	 * @return
	 * @see redis.clients.jedis.Pipeline#expire(byte[], int)
	 */
	public Response<Long> expire(byte[] key, int seconds) {
		return delegate.expire(key, seconds);
	}

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.Pipeline#expireAt(java.lang.String, long)
	 */
	public Response<Long> expireAt(String key, long unixTime) {
		return delegate.expireAt(key, unixTime);
	}

	/**
	 * @param key
	 * @param unixTime
	 * @return
	 * @see redis.clients.jedis.Pipeline#expireAt(byte[], long)
	 */
	public Response<Long> expireAt(byte[] key, long unixTime) {
		return delegate.expireAt(key, unixTime);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#get(java.lang.String)
	 */
	public Response<String> get(String key) {
		return delegate.get(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#get(byte[])
	 */
	public Response<byte[]> get(byte[] key) {
		return delegate.get(key);
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	/**
	 * @param key
	 * @param offset
	 * @return
	 * @see redis.clients.jedis.Pipeline#getbit(java.lang.String, long)
	 */
	public Response<Boolean> getbit(String key, long offset) {
		return delegate.getbit(key, offset);
	}

	/**
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 * @see redis.clients.jedis.Pipeline#getrange(java.lang.String, long, long)
	 */
	public Response<String> getrange(String key, long startOffset, long endOffset) {
		return delegate.getrange(key, startOffset, endOffset);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#getSet(java.lang.String, java.lang.String)
	 */
	public Response<String> getSet(String key, String value) {
		return delegate.getSet(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#getSet(byte[], byte[])
	 */
	public Response<byte[]> getSet(byte[] key, byte[] value) {
		return delegate.getSet(key, value);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hdel(java.lang.String, java.lang.String)
	 */
	public Response<Long> hdel(String key, String field) {
		return delegate.hdel(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hdel(byte[], byte[])
	 */
	public Response<Long> hdel(byte[] key, byte[] field) {
		return delegate.hdel(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hexists(java.lang.String, java.lang.String)
	 */
	public Response<Boolean> hexists(String key, String field) {
		return delegate.hexists(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hexists(byte[], byte[])
	 */
	public Response<Boolean> hexists(byte[] key, byte[] field) {
		return delegate.hexists(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hget(java.lang.String, java.lang.String)
	 */
	public Response<String> hget(String key, String field) {
		return delegate.hget(key, field);
	}

	/**
	 * @param key
	 * @param field
	 * @return
	 * @see redis.clients.jedis.Pipeline#hget(byte[], byte[])
	 */
	public Response<String> hget(byte[] key, byte[] field) {
		return delegate.hget(key, field);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hgetAll(java.lang.String)
	 */
	public Response<Map<String, String>> hgetAll(String key) {
		return delegate.hgetAll(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hgetAll(byte[])
	 */
	public Response<Map<String, String>> hgetAll(byte[] key) {
		return delegate.hgetAll(key);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hincrBy(java.lang.String, java.lang.String, long)
	 */
	public Response<Long> hincrBy(String key, String field, long value) {
		return delegate.hincrBy(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hincrBy(byte[], byte[], long)
	 */
	public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
		return delegate.hincrBy(key, field, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hkeys(java.lang.String)
	 */
	public Response<Set<String>> hkeys(String key) {
		return delegate.hkeys(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hkeys(byte[])
	 */
	public Response<Set<String>> hkeys(byte[] key) {
		return delegate.hkeys(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hlen(java.lang.String)
	 */
	public Response<Long> hlen(String key) {
		return delegate.hlen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hlen(byte[])
	 */
	public Response<Long> hlen(byte[] key) {
		return delegate.hlen(key);
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.Pipeline#hmget(java.lang.String, java.lang.String[])
	 */
	public Response<List<String>> hmget(String key, String... fields) {
		return delegate.hmget(key, fields);
	}

	/**
	 * @param key
	 * @param fields
	 * @return
	 * @see redis.clients.jedis.Pipeline#hmget(byte[], byte[][])
	 */
	public Response<List<String>> hmget(byte[] key, byte[]... fields) {
		return delegate.hmget(key, fields);
	}

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.Pipeline#hmset(java.lang.String, java.util.Map)
	 */
	public Response<String> hmset(String key, Map<String, String> hash) {
		return delegate.hmset(key, hash);
	}

	/**
	 * @param key
	 * @param hash
	 * @return
	 * @see redis.clients.jedis.Pipeline#hmset(byte[], java.util.Map)
	 */
	public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
		return delegate.hmset(key, hash);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hset(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Response<Long> hset(String key, String field, String value) {
		return delegate.hset(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hset(byte[], byte[], byte[])
	 */
	public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
		return delegate.hset(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hsetnx(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Response<Long> hsetnx(String key, String field, String value) {
		return delegate.hsetnx(key, field, value);
	}

	/**
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#hsetnx(byte[], byte[], byte[])
	 */
	public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
		return delegate.hsetnx(key, field, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hvals(java.lang.String)
	 */
	public Response<List<String>> hvals(String key) {
		return delegate.hvals(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#hvals(byte[])
	 */
	public Response<List<String>> hvals(byte[] key) {
		return delegate.hvals(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#incr(java.lang.String)
	 */
	public Response<Long> incr(String key) {
		return delegate.incr(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#incr(byte[])
	 */
	public Response<Long> incr(byte[] key) {
		return delegate.incr(key);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Pipeline#incrBy(java.lang.String, long)
	 */
	public Response<Long> incrBy(String key, long integer) {
		return delegate.incrBy(key, integer);
	}

	/**
	 * @param key
	 * @param integer
	 * @return
	 * @see redis.clients.jedis.Pipeline#incrBy(byte[], long)
	 */
	public Response<Long> incrBy(byte[] key, long integer) {
		return delegate.incrBy(key, integer);
	}

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.Pipeline#keys(java.lang.String)
	 */
	public Response<Set<String>> keys(String pattern) {
		return delegate.keys(pattern);
	}

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.Pipeline#keys(byte[])
	 */
	public Response<Set<String>> keys(byte[] pattern) {
		return delegate.keys(pattern);
	}

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.Pipeline#lindex(java.lang.String, int)
	 */
	public Response<String> lindex(String key, int index) {
		return delegate.lindex(key, index);
	}

	/**
	 * @param key
	 * @param index
	 * @return
	 * @see redis.clients.jedis.Pipeline#lindex(byte[], int)
	 */
	public Response<String> lindex(byte[] key, int index) {
		return delegate.lindex(key, index);
	}

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#linsert(java.lang.String, redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String, java.lang.String)
	 */
	public Response<Long> linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		return delegate.linsert(key, where, pivot, value);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return delegate.toString();
	}

	/**
	 * @param key
	 * @param where
	 * @param pivot
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#linsert(byte[], redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	public Response<Long> linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		return delegate.linsert(key, where, pivot, value);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#llen(java.lang.String)
	 */
	public Response<Long> llen(String key) {
		return delegate.llen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#llen(byte[])
	 */
	public Response<Long> llen(byte[] key) {
		return delegate.llen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpop(java.lang.String)
	 */
	public Response<String> lpop(String key) {
		return delegate.lpop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpop(byte[])
	 */
	public Response<String> lpop(byte[] key) {
		return delegate.lpop(key);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpush(java.lang.String, java.lang.String)
	 */
	public Response<Long> lpush(String key, String string) {
		return delegate.lpush(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpush(byte[], byte[])
	 */
	public Response<Long> lpush(byte[] key, byte[] string) {
		return delegate.lpush(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpushx(java.lang.String, java.lang.String)
	 */
	public Response<Long> lpushx(String key, String string) {
		return delegate.lpushx(key, string);
	}

	/**
	 * @param key
	 * @param bytes
	 * @return
	 * @see redis.clients.jedis.Pipeline#lpushx(byte[], byte[])
	 */
	public Response<Long> lpushx(byte[] key, byte[] bytes) {
		return delegate.lpushx(key, bytes);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#lrange(java.lang.String, long, long)
	 */
	public Response<List<String>> lrange(String key, long start, long end) {
		return delegate.lrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#lrange(byte[], long, long)
	 */
	public Response<List<String>> lrange(byte[] key, long start, long end) {
		return delegate.lrange(key, start, end);
	}

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#lrem(java.lang.String, long, java.lang.String)
	 */
	public Response<Long> lrem(String key, long count, String value) {
		return delegate.lrem(key, count, value);
	}

	/**
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#lrem(byte[], long, byte[])
	 */
	public Response<Long> lrem(byte[] key, long count, byte[] value) {
		return delegate.lrem(key, count, value);
	}

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#lset(java.lang.String, long, java.lang.String)
	 */
	public Response<String> lset(String key, long index, String value) {
		return delegate.lset(key, index, value);
	}

	/**
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#lset(byte[], long, byte[])
	 */
	public Response<String> lset(byte[] key, long index, byte[] value) {
		return delegate.lset(key, index, value);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#ltrim(java.lang.String, long, long)
	 */
	public Response<String> ltrim(String key, long start, long end) {
		return delegate.ltrim(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#ltrim(byte[], long, long)
	 */
	public Response<String> ltrim(byte[] key, long start, long end) {
		return delegate.ltrim(key, start, end);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#mget(java.lang.String[])
	 */
	public Response<List<String>> mget(String... keys) {
		return delegate.mget(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#mget(byte[][])
	 */
	public Response<List<String>> mget(byte[]... keys) {
		return delegate.mget(keys);
	}

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.Pipeline#move(java.lang.String, int)
	 */
	public Response<Long> move(String key, int dbIndex) {
		return delegate.move(key, dbIndex);
	}

	/**
	 * @param key
	 * @param dbIndex
	 * @return
	 * @see redis.clients.jedis.Pipeline#move(byte[], int)
	 */
	public Response<Long> move(byte[] key, int dbIndex) {
		return delegate.move(key, dbIndex);
	}

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Pipeline#mset(java.lang.String[])
	 */
	public Response<String> mset(String... keysvalues) {
		return delegate.mset(keysvalues);
	}

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Pipeline#mset(byte[][])
	 */
	public Response<String> mset(byte[]... keysvalues) {
		return delegate.mset(keysvalues);
	}

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Pipeline#msetnx(java.lang.String[])
	 */
	public Response<Long> msetnx(String... keysvalues) {
		return delegate.msetnx(keysvalues);
	}

	/**
	 * @param keysvalues
	 * @return
	 * @see redis.clients.jedis.Pipeline#msetnx(byte[][])
	 */
	public Response<Long> msetnx(byte[]... keysvalues) {
		return delegate.msetnx(keysvalues);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#persist(java.lang.String)
	 */
	public Response<Long> persist(String key) {
		return delegate.persist(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#persist(byte[])
	 */
	public Response<Long> persist(byte[] key) {
		return delegate.persist(key);
	}

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#rename(java.lang.String, java.lang.String)
	 */
	public Response<String> rename(String oldkey, String newkey) {
		return delegate.rename(oldkey, newkey);
	}

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#rename(byte[], byte[])
	 */
	public Response<String> rename(byte[] oldkey, byte[] newkey) {
		return delegate.rename(oldkey, newkey);
	}

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#renamenx(java.lang.String, java.lang.String)
	 */
	public Response<Long> renamenx(String oldkey, String newkey) {
		return delegate.renamenx(oldkey, newkey);
	}

	/**
	 * @param oldkey
	 * @param newkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#renamenx(byte[], byte[])
	 */
	public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
		return delegate.renamenx(oldkey, newkey);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpop(java.lang.String)
	 */
	public Response<String> rpop(String key) {
		return delegate.rpop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpop(byte[])
	 */
	public Response<String> rpop(byte[] key) {
		return delegate.rpop(key);
	}

	/**
	 * @param srckey
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpoplpush(java.lang.String, java.lang.String)
	 */
	public Response<String> rpoplpush(String srckey, String dstkey) {
		return delegate.rpoplpush(srckey, dstkey);
	}

	/**
	 * @param srckey
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpoplpush(byte[], byte[])
	 */
	public Response<String> rpoplpush(byte[] srckey, byte[] dstkey) {
		return delegate.rpoplpush(srckey, dstkey);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpush(java.lang.String, java.lang.String)
	 */
	public Response<Long> rpush(String key, String string) {
		return delegate.rpush(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpush(byte[], byte[])
	 */
	public Response<Long> rpush(byte[] key, byte[] string) {
		return delegate.rpush(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpushx(java.lang.String, java.lang.String)
	 */
	public Response<Long> rpushx(String key, String string) {
		return delegate.rpushx(key, string);
	}

	/**
	 * @param key
	 * @param string
	 * @return
	 * @see redis.clients.jedis.Pipeline#rpushx(byte[], byte[])
	 */
	public Response<Long> rpushx(byte[] key, byte[] string) {
		return delegate.rpushx(key, string);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#sadd(java.lang.String, java.lang.String)
	 */
	public Response<Long> sadd(String key, String member) {
		return delegate.sadd(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#sadd(byte[], byte[])
	 */
	public Response<Long> sadd(byte[] key, byte[] member) {
		return delegate.sadd(key, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#scard(java.lang.String)
	 */
	public Response<Long> scard(String key) {
		return delegate.scard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#scard(byte[])
	 */
	public Response<Long> scard(byte[] key) {
		return delegate.scard(key);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sdiff(java.lang.String[])
	 */
	public Response<Set<String>> sdiff(String... keys) {
		return delegate.sdiff(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sdiff(byte[][])
	 */
	public Response<Set<String>> sdiff(byte[]... keys) {
		return delegate.sdiff(keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sdiffstore(java.lang.String, java.lang.String[])
	 */
	public Response<Long> sdiffstore(String dstkey, String... keys) {
		return delegate.sdiffstore(dstkey, keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sdiffstore(byte[], byte[][])
	 */
	public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
		return delegate.sdiffstore(dstkey, keys);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#set(java.lang.String, java.lang.String)
	 */
	public Response<String> set(String key, String value) {
		return delegate.set(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#set(byte[], byte[])
	 */
	public Response<String> set(byte[] key, byte[] value) {
		return delegate.set(key, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setbit(java.lang.String, long, boolean)
	 */
	public Response<Boolean> setbit(String key, long offset, boolean value) {
		return delegate.setbit(key, offset, value);
	}

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setex(java.lang.String, int, java.lang.String)
	 */
	public Response<String> setex(String key, int seconds, String value) {
		return delegate.setex(key, seconds, value);
	}

	/**
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setex(byte[], int, byte[])
	 */
	public Response<String> setex(byte[] key, int seconds, byte[] value) {
		return delegate.setex(key, seconds, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setnx(java.lang.String, java.lang.String)
	 */
	public Response<Long> setnx(String key, String value) {
		return delegate.setnx(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setnx(byte[], byte[])
	 */
	public Response<Long> setnx(byte[] key, byte[] value) {
		return delegate.setnx(key, value);
	}

	/**
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#setrange(java.lang.String, long, java.lang.String)
	 */
	public Response<Long> setrange(String key, long offset, String value) {
		return delegate.setrange(key, offset, value);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sinter(java.lang.String[])
	 */
	public Response<Set<String>> sinter(String... keys) {
		return delegate.sinter(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sinter(byte[][])
	 */
	public Response<Set<String>> sinter(byte[]... keys) {
		return delegate.sinter(keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sinterstore(java.lang.String, java.lang.String[])
	 */
	public Response<Long> sinterstore(String dstkey, String... keys) {
		return delegate.sinterstore(dstkey, keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sinterstore(byte[], byte[][])
	 */
	public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
		return delegate.sinterstore(dstkey, keys);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#sismember(java.lang.String, java.lang.String)
	 */
	public Response<Boolean> sismember(String key, String member) {
		return delegate.sismember(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#sismember(byte[], byte[])
	 */
	public Response<Boolean> sismember(byte[] key, byte[] member) {
		return delegate.sismember(key, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#smembers(java.lang.String)
	 */
	public Response<Set<String>> smembers(String key) {
		return delegate.smembers(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#smembers(byte[])
	 */
	public Response<Set<String>> smembers(byte[] key) {
		return delegate.smembers(key);
	}

	/**
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#smove(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Response<Long> smove(String srckey, String dstkey, String member) {
		return delegate.smove(srckey, dstkey, member);
	}

	/**
	 * @param srckey
	 * @param dstkey
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#smove(byte[], byte[], byte[])
	 */
	public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
		return delegate.smove(srckey, dstkey, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(java.lang.String)
	 */
	public Response<Long> sort(String key) {
		return delegate.sort(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(byte[])
	 */
	public Response<Long> sort(byte[] key) {
		return delegate.sort(key);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(java.lang.String, redis.clients.jedis.SortingParams)
	 */
	public Response<List<String>> sort(String key, SortingParams sortingParameters) {
		return delegate.sort(key, sortingParameters);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(byte[], redis.clients.jedis.SortingParams)
	 */
	public Response<List<String>> sort(byte[] key, SortingParams sortingParameters) {
		return delegate.sort(key, sortingParameters);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(java.lang.String, redis.clients.jedis.SortingParams, java.lang.String)
	 */
	public Response<List<String>> sort(String key,
			SortingParams sortingParameters, String dstkey) {
		return delegate.sort(key, sortingParameters, dstkey);
	}

	/**
	 * @param key
	 * @param sortingParameters
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(byte[], redis.clients.jedis.SortingParams, byte[])
	 */
	public Response<List<String>> sort(byte[] key,
			SortingParams sortingParameters, byte[] dstkey) {
		return delegate.sort(key, sortingParameters, dstkey);
	}

	/**
	 * @param key
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(java.lang.String, java.lang.String)
	 */
	public Response<List<String>> sort(String key, String dstkey) {
		return delegate.sort(key, dstkey);
	}

	/**
	 * @param key
	 * @param dstkey
	 * @return
	 * @see redis.clients.jedis.Pipeline#sort(byte[], byte[])
	 */
	public Response<List<String>> sort(byte[] key, byte[] dstkey) {
		return delegate.sort(key, dstkey);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#spop(java.lang.String)
	 */
	public Response<String> spop(String key) {
		return delegate.spop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#spop(byte[])
	 */
	public Response<String> spop(byte[] key) {
		return delegate.spop(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#srandmember(java.lang.String)
	 */
	public Response<String> srandmember(String key) {
		return delegate.srandmember(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#srandmember(byte[])
	 */
	public Response<String> srandmember(byte[] key) {
		return delegate.srandmember(key);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#srem(java.lang.String, java.lang.String)
	 */
	public Response<Long> srem(String key, String member) {
		return delegate.srem(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#srem(byte[], byte[])
	 */
	public Response<Long> srem(byte[] key, byte[] member) {
		return delegate.srem(key, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#strlen(java.lang.String)
	 */
	public Response<Long> strlen(String key) {
		return delegate.strlen(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#strlen(byte[])
	 */
	public Response<Long> strlen(byte[] key) {
		return delegate.strlen(key);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#substr(java.lang.String, int, int)
	 */
	public Response<String> substr(String key, int start, int end) {
		return delegate.substr(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#substr(byte[], int, int)
	 */
	public Response<String> substr(byte[] key, int start, int end) {
		return delegate.substr(key, start, end);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sunion(java.lang.String[])
	 */
	public Response<Set<String>> sunion(String... keys) {
		return delegate.sunion(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sunion(byte[][])
	 */
	public Response<Set<String>> sunion(byte[]... keys) {
		return delegate.sunion(keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sunionstore(java.lang.String, java.lang.String[])
	 */
	public Response<Long> sunionstore(String dstkey, String... keys) {
		return delegate.sunionstore(dstkey, keys);
	}

	/**
	 * @param dstkey
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#sunionstore(byte[], byte[][])
	 */
	public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
		return delegate.sunionstore(dstkey, keys);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#ttl(java.lang.String)
	 */
	public Response<Long> ttl(String key) {
		return delegate.ttl(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#ttl(byte[])
	 */
	public Response<Long> ttl(byte[] key) {
		return delegate.ttl(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#type(java.lang.String)
	 */
	public Response<String> type(String key) {
		return delegate.type(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#type(byte[])
	 */
	public Response<String> type(byte[] key) {
		return delegate.type(key);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#watch(java.lang.String[])
	 */
	public Response<String> watch(String... keys) {
		return delegate.watch(keys);
	}

	/**
	 * @param keys
	 * @return
	 * @see redis.clients.jedis.Pipeline#watch(byte[][])
	 */
	public Response<String> watch(byte[]... keys) {
		return delegate.watch(keys);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zadd(java.lang.String, double, java.lang.String)
	 */
	public Response<Long> zadd(String key, double score, String member) {
		return delegate.zadd(key, score, member);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zadd(byte[], double, byte[])
	 */
	public Response<Long> zadd(byte[] key, double score, byte[] member) {
		return delegate.zadd(key, score, member);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#zcard(java.lang.String)
	 */
	public Response<Long> zcard(String key) {
		return delegate.zcard(key);
	}

	/**
	 * @param key
	 * @return
	 * @see redis.clients.jedis.Pipeline#zcard(byte[])
	 */
	public Response<Long> zcard(byte[] key) {
		return delegate.zcard(key);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zcount(java.lang.String, double, double)
	 */
	public Response<Long> zcount(String key, double min, double max) {
		return delegate.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zcount(byte[], double, double)
	 */
	public Response<Long> zcount(byte[] key, double min, double max) {
		return delegate.zcount(key, min, max);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zincrby(java.lang.String, double, java.lang.String)
	 */
	public Response<Double> zincrby(String key, double score, String member) {
		return delegate.zincrby(key, score, member);
	}

	/**
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zincrby(byte[], double, byte[])
	 */
	public Response<Double> zincrby(byte[] key, double score, byte[] member) {
		return delegate.zincrby(key, score, member);
	}

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zinterstore(java.lang.String, java.lang.String[])
	 */
	public Response<Long> zinterstore(String dstkey, String... sets) {
		return delegate.zinterstore(dstkey, sets);
	}

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zinterstore(byte[], byte[][])
	 */
	public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
		return delegate.zinterstore(dstkey, sets);
	}

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zinterstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String[])
	 */
	public Response<Long> zinterstore(String dstkey, ZParams params,
			String... sets) {
		return delegate.zinterstore(dstkey, params, sets);
	}

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zinterstore(byte[], redis.clients.jedis.ZParams, byte[][])
	 */
	public Response<Long> zinterstore(byte[] dstkey, ZParams params,
			byte[]... sets) {
		return delegate.zinterstore(dstkey, params, sets);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrange(java.lang.String, int, int)
	 */
	public Response<Set<String>> zrange(String key, int start, int end) {
		return delegate.zrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrange(byte[], int, int)
	 */
	public Response<Set<String>> zrange(byte[] key, int start, int end) {
		return delegate.zrange(key, start, end);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(java.lang.String, double, double)
	 */
	public Response<Set<String>> zrangeByScore(String key, double min, double max) {
		return delegate.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(byte[], double, double)
	 */
	public Response<Set<String>> zrangeByScore(byte[] key, double min, double max) {
		return delegate.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Response<Set<String>> zrangeByScore(String key, String min, String max) {
		return delegate.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(byte[], byte[], byte[])
	 */
	public Response<Set<String>> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		return delegate.zrangeByScore(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(java.lang.String, double, double, int, int)
	 */
	public Response<Set<String>> zrangeByScore(String key, double min,
			double max, int offset, int count) {
		return delegate.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScore(byte[], double, double, int, int)
	 */
	public Response<Set<String>> zrangeByScore(byte[] key, double min,
			double max, int offset, int count) {
		return delegate.zrangeByScore(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScoreWithScores(java.lang.String, double, double)
	 */
	public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
			double max) {
		return delegate.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScoreWithScores(byte[], double, double)
	 */
	public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
			double max) {
		return delegate.zrangeByScoreWithScores(key, min, max);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
			double max, int offset, int count) {
		return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param min
	 * @param max
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
			double max, int offset, int count) {
		return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(java.lang.String, double, double)
	 */
	public Response<Set<String>> zrevrangeByScore(String key, double max,
			double min) {
		return delegate.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(byte[], double, double)
	 */
	public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
			double min) {
		return delegate.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Response<Set<String>> zrevrangeByScore(String key, String max,
			String min) {
		return delegate.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(byte[], byte[], byte[])
	 */
	public Response<Set<String>> zrevrangeByScore(byte[] key, byte[] max,
			byte[] min) {
		return delegate.zrevrangeByScore(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(java.lang.String, double, double, int, int)
	 */
	public Response<Set<String>> zrevrangeByScore(String key, double max,
			double min, int offset, int count) {
		return delegate.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScore(byte[], double, double, int, int)
	 */
	public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
			double min, int offset, int count) {
		return delegate.zrevrangeByScore(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScoreWithScores(java.lang.String, double, double)
	 */
	public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
			double max, double min) {
		return delegate.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScoreWithScores(byte[], double, double)
	 */
	public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
			double max, double min) {
		return delegate.zrevrangeByScoreWithScores(key, max, min);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
			double max, double min, int offset, int count) {
		return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
			double max, double min, int offset, int count) {
		return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeWithScores(java.lang.String, int, int)
	 */
	public Response<Set<Tuple>> zrangeWithScores(String key, int start, int end) {
		return delegate.zrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrangeWithScores(byte[], int, int)
	 */
	public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
		return delegate.zrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrank(java.lang.String, java.lang.String)
	 */
	public Response<Long> zrank(String key, String member) {
		return delegate.zrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrank(byte[], byte[])
	 */
	public Response<Long> zrank(byte[] key, byte[] member) {
		return delegate.zrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrem(java.lang.String, java.lang.String)
	 */
	public Response<Long> zrem(String key, String member) {
		return delegate.zrem(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrem(byte[], byte[])
	 */
	public Response<Long> zrem(byte[] key, byte[] member) {
		return delegate.zrem(key, member);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zremrangeByRank(java.lang.String, int, int)
	 */
	public Response<Long> zremrangeByRank(String key, int start, int end) {
		return delegate.zremrangeByRank(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zremrangeByRank(byte[], int, int)
	 */
	public Response<Long> zremrangeByRank(byte[] key, int start, int end) {
		return delegate.zremrangeByRank(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zremrangeByScore(java.lang.String, double, double)
	 */
	public Response<Long> zremrangeByScore(String key, double start, double end) {
		return delegate.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zremrangeByScore(byte[], double, double)
	 */
	public Response<Long> zremrangeByScore(byte[] key, double start, double end) {
		return delegate.zremrangeByScore(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrange(java.lang.String, int, int)
	 */
	public Response<Set<String>> zrevrange(String key, int start, int end) {
		return delegate.zrevrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrange(byte[], int, int)
	 */
	public Response<Set<String>> zrevrange(byte[] key, int start, int end) {
		return delegate.zrevrange(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeWithScores(java.lang.String, int, int)
	 */
	public Response<Set<Tuple>> zrevrangeWithScores(String key, int start, int end) {
		return delegate.zrevrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrangeWithScores(byte[], int, int)
	 */
	public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start, int end) {
		return delegate.zrevrangeWithScores(key, start, end);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrank(java.lang.String, java.lang.String)
	 */
	public Response<Long> zrevrank(String key, String member) {
		return delegate.zrevrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zrevrank(byte[], byte[])
	 */
	public Response<Long> zrevrank(byte[] key, byte[] member) {
		return delegate.zrevrank(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zscore(java.lang.String, java.lang.String)
	 */
	public Response<Double> zscore(String key, String member) {
		return delegate.zscore(key, member);
	}

	/**
	 * @param key
	 * @param member
	 * @return
	 * @see redis.clients.jedis.Pipeline#zscore(byte[], byte[])
	 */
	public Response<Double> zscore(byte[] key, byte[] member) {
		return delegate.zscore(key, member);
	}

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zunionstore(java.lang.String, java.lang.String[])
	 */
	public Response<Long> zunionstore(String dstkey, String... sets) {
		return delegate.zunionstore(dstkey, sets);
	}

	/**
	 * @param dstkey
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zunionstore(byte[], byte[][])
	 */
	public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
		return delegate.zunionstore(dstkey, sets);
	}

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zunionstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String[])
	 */
	public Response<Long> zunionstore(String dstkey, ZParams params,
			String... sets) {
		return delegate.zunionstore(dstkey, params, sets);
	}

	/**
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 * @see redis.clients.jedis.Pipeline#zunionstore(byte[], redis.clients.jedis.ZParams, byte[][])
	 */
	public Response<Long> zunionstore(byte[] dstkey, ZParams params,
			byte[]... sets) {
		return delegate.zunionstore(dstkey, params, sets);
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#bgrewriteaof()
	 */
	public Response<String> bgrewriteaof() {
		return delegate.bgrewriteaof();
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#bgsave()
	 */
	public Response<String> bgsave() {
		return delegate.bgsave();
	}

	/**
	 * @param pattern
	 * @return
	 * @see redis.clients.jedis.Pipeline#configGet(java.lang.String)
	 */
	public Response<String> configGet(String pattern) {
		return delegate.configGet(pattern);
	}

	/**
	 * @param parameter
	 * @param value
	 * @return
	 * @see redis.clients.jedis.Pipeline#configSet(java.lang.String, java.lang.String)
	 */
	public Response<String> configSet(String parameter, String value) {
		return delegate.configSet(parameter, value);
	}

	/**
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return
	 * @see redis.clients.jedis.Pipeline#brpoplpush(java.lang.String, java.lang.String, int)
	 */
	public Response<String> brpoplpush(String source, String destination,
			int timeout) {
		return delegate.brpoplpush(source, destination, timeout);
	}

	/**
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return
	 * @see redis.clients.jedis.Pipeline#brpoplpush(byte[], byte[], int)
	 */
	public Response<String> brpoplpush(byte[] source, byte[] destination,
			int timeout) {
		return delegate.brpoplpush(source, destination, timeout);
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#configResetStat()
	 */
	public Response<String> configResetStat() {
		return delegate.configResetStat();
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#save()
	 */
	public Response<String> save() {
		return delegate.save();
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#lastsave()
	 */
	public Response<Long> lastsave() {
		return delegate.lastsave();
	}

	/**
	 * @return
	 * @see redis.clients.jedis.Pipeline#discard()
	 */
	public Response<String> discard() {
		return delegate.discard();
	}

	/**
	 * 
	 * @return 
	 * @see redis.clients.jedis.Pipeline#exec()
	 */
	public Response<List<Object>> exec() {
		return delegate.exec();
	}

	/**
	 * 
	 * @see redis.clients.jedis.Pipeline#multi()
	 */
	public void multi() {
		delegate.multi();
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 * @see redis.clients.jedis.Pipeline#publish(java.lang.String, java.lang.String)
	 */
	public Response<Long> publish(String channel, String message) {
		return delegate.publish(channel, message);
	}

	/**
	 * @param channel
	 * @param message
	 * @return
	 * @see redis.clients.jedis.Pipeline#publish(byte[], byte[])
	 */
	public Response<Long> publish(byte[] channel, byte[] message) {
		return delegate.publish(channel, message);
	}
	
	
}
