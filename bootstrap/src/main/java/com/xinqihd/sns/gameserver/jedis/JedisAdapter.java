package com.xinqihd.sns.gameserver.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Client;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.JedisMonitor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.TransactionBlock;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;

/**
 * My adapter for capsulate Jedis with pool operation.
 * 
 * @author wangqi
 *
 */
public class JedisAdapter implements Jedis, JedisAllCommand {
	
	private static final Logger logger = LoggerFactory.getLogger(JedisAdapter.class);

	private static final int CONNECT_TIMEOUT = 50000;
	private JedisPool pool = null;
	
	public JedisAdapter() {
	}

	public JedisAdapter(String host, int port, JedisPoolConfig config) {
		pool = new JedisPool(config, host, port, CONNECT_TIMEOUT);
	}
	
	public JedisAdapter(String host, int port, JedisPoolConfig config, int timeOutMillis) {
		pool = new JedisPool(config, host, port, timeOutMillis);
	}


	// ------------------------------------------------ Delegate to underlying.

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#ping()
	 */
	@Override
	public String ping() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.ping();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#set(java.lang.String, java.lang.String)
	 */
	@Override
	public String set(String key, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.set(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.get(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#set(byte[], byte[])
	 */
	@Override
	public String set(byte[] key, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.set(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#get(byte[])
	 */
	@Override
	public byte[] get(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.get(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#quit()
	 */
	@Override
	public String quit() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.quit();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#exists(java.lang.String)
	 */
	@Override
	public Boolean exists(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.exists(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#exists(byte[])
	 */
	@Override
	public Boolean exists(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.exists(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#del(java.lang.String)
	 */
	@Override
	public Long del(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.del(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hashCode()
	 */
	@Override
	public int hashCode() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hashCode();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#del(byte)
	 */
	@Override
	public Long del(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.del(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#type(java.lang.String)
	 */
	@Override
	public String type(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.type(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#type(byte[])
	 */
	@Override
	public String type(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.type(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#flushDB()
	 */
	@Override
	public String flushDB() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.flushDB();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#keys(java.lang.String)
	 */
	@Override
	public Set<String> keys(String pattern) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.keys(pattern);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#keys(byte[])
	 */
	@Override
	public Set<byte[]> keys(byte[] pattern) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.keys(pattern);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.equals(obj);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#randomKey()
	 */
	@Override
	public String randomKey() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.randomKey();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rename(java.lang.String, java.lang.String)
	 */
	@Override
	public String rename(String oldkey, String newkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rename(oldkey, newkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#randomBinaryKey()
	 */
	@Override
	public byte[] randomBinaryKey() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.randomBinaryKey();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rename(byte[], byte[])
	 */
	@Override
	public String rename(byte[] oldkey, byte[] newkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rename(oldkey, newkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#renamenx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long renamenx(String oldkey, String newkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.renamenx(oldkey, newkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#renamenx(byte[], byte[])
	 */
	@Override
	public Long renamenx(byte[] oldkey, byte[] newkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.renamenx(oldkey, newkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#dbSize()
	 */
	@Override
	public Long dbSize() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.dbSize();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#expire(java.lang.String, int)
	 */
	@Override
	public Long expire(String key, int seconds) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.expire(key, seconds);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#expire(byte[], int)
	 */
	@Override
	public Long expire(byte[] key, int seconds) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.expire(key, seconds);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#expireAt(java.lang.String, long)
	 */
	@Override
	public Long expireAt(String key, long unixTime) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.expireAt(key, unixTime);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#expireAt(byte[], long)
	 */
	@Override
	public Long expireAt(byte[] key, long unixTime) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.expireAt(key, unixTime);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#toString()
	 */
	@Override
	public String toString() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.toString();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#ttl(java.lang.String)
	 */
	@Override
	public Long ttl(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.ttl(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#ttl(byte[])
	 */
	@Override
	public Long ttl(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.ttl(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#select(int)
	 */
	@Override
	public String select(int index) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.select(index);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#move(java.lang.String, int)
	 */
	@Override
	public Long move(String key, int dbIndex) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.move(key, dbIndex);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#move(byte[], int)
	 */
	@Override
	public Long move(byte[] key, int dbIndex) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.move(key, dbIndex);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#flushAll()
	 */
	@Override
	public String flushAll() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.flushAll();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getSet(java.lang.String, java.lang.String)
	 */
	@Override
	public String getSet(String key, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getSet(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getSet(byte[], byte[])
	 */
	@Override
	public byte[] getSet(byte[] key, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getSet(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#mget(java.lang.String)
	 */
	@Override
	public List<String> mget(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.mget(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#mget(byte)
	 */
	@Override
	public List<byte[]> mget(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.mget(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setnx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long setnx(String key, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setnx(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setnx(byte[], byte[])
	 */
	@Override
	public Long setnx(byte[] key, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setnx(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setex(java.lang.String, int, java.lang.String)
	 */
	@Override
	public String setex(String key, int seconds, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setex(key, seconds, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setex(byte[], int, byte[])
	 */
	@Override
	public String setex(byte[] key, int seconds, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setex(key, seconds, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#mset(java.lang.String)
	 */
	@Override
	public String mset(String... keysvalues) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.mset(keysvalues);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#mset(byte)
	 */
	@Override
	public String mset(byte[]... keysvalues) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.mset(keysvalues);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#msetnx(java.lang.String)
	 */
	@Override
	public Long msetnx(String... keysvalues) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.msetnx(keysvalues);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#msetnx(byte)
	 */
	@Override
	public Long msetnx(byte[]... keysvalues) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.msetnx(keysvalues);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#decrBy(java.lang.String, long)
	 */
	@Override
	public Long decrBy(String key, long integer) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.decrBy(key, integer);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#decrBy(byte[], long)
	 */
	@Override
	public Long decrBy(byte[] key, long integer) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.decrBy(key, integer);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#decr(java.lang.String)
	 */
	@Override
	public Long decr(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.decr(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#decr(byte[])
	 */
	@Override
	public Long decr(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.decr(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#incrBy(java.lang.String, long)
	 */
	@Override
	public Long incrBy(String key, long integer) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.incrBy(key, integer);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#incrBy(byte[], long)
	 */
	@Override
	public Long incrBy(byte[] key, long integer) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.incrBy(key, integer);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#incr(java.lang.String)
	 */
	@Override
	public Long incr(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.incr(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#incr(byte[])
	 */
	@Override
	public Long incr(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.incr(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#append(java.lang.String, java.lang.String)
	 */
	@Override
	public Long append(String key, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.append(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#append(byte[], byte[])
	 */
	@Override
	public Long append(byte[] key, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.append(key, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#substr(java.lang.String, int, int)
	 */
	@Override
	public String substr(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.substr(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#substr(byte[], int, int)
	 */
	@Override
	public byte[] substr(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.substr(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hset(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long hset(String key, String field, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hset(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hset(byte[], byte[], byte[])
	 */
	@Override
	public Long hset(byte[] key, byte[] field, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hset(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hget(java.lang.String, java.lang.String)
	 */
	@Override
	public String hget(String key, String field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hget(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hsetnx(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long hsetnx(String key, String field, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hsetnx(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hget(byte[], byte[])
	 */
	@Override
	public byte[] hget(byte[] key, byte[] field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hget(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hsetnx(byte[], byte[], byte[])
	 */
	@Override
	public Long hsetnx(byte[] key, byte[] field, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hsetnx(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hmset(java.lang.String, java.util.Map)
	 */
	@Override
	public String hmset(String key, Map<String, String> hash) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hmset(key, hash);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hmset(byte[], java.util.Map)
	 */
	@Override
	public String hmset(byte[] key, Map<byte[], byte[]> hash) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hmset(key, hash);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hmget(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> hmget(String key, String... fields) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hmget(key, fields);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hmget(byte[], byte)
	 */
	@Override
	public List<byte[]> hmget(byte[] key, byte[]... fields) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hmget(key, fields);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hincrBy(java.lang.String, java.lang.String, long)
	 */
	@Override
	public Long hincrBy(String key, String field, long value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hincrBy(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hincrBy(byte[], byte[], long)
	 */
	@Override
	public Long hincrBy(byte[] key, byte[] field, long value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hincrBy(key, field, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hexists(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean hexists(String key, String field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hexists(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hdel(java.lang.String, java.lang.String)
	 */
	@Override
	public Long hdel(String key, String field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hdel(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hexists(byte[], byte[])
	 */
	@Override
	public Boolean hexists(byte[] key, byte[] field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hexists(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hlen(java.lang.String)
	 */
	@Override
	public Long hlen(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hlen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hdel(byte[], byte[])
	 */
	@Override
	public Long hdel(byte[] key, byte[] field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hdel(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hkeys(java.lang.String)
	 */
	@Override
	public Set<String> hkeys(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hkeys(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hlen(byte[])
	 */
	@Override
	public Long hlen(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hlen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hvals(java.lang.String)
	 */
	@Override
	public List<String> hvals(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hvals(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hkeys(byte[])
	 */
	@Override
	public Set<byte[]> hkeys(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hkeys(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hgetAll(java.lang.String)
	 */
	@Override
	public Map<String, String> hgetAll(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hgetAll(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hvals(byte[])
	 */
	@Override
	public List<byte[]> hvals(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hvals(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpush(java.lang.String, java.lang.String)
	 */
	@Override
	public Long rpush(String key, String string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#hgetAll(byte[])
	 */
	@Override
	public Map<byte[], byte[]> hgetAll(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hgetAll(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpush(java.lang.String, java.lang.String)
	 */
	@Override
	public Long lpush(String key, String string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpush(byte[], byte[])
	 */
	@Override
	public Long rpush(byte[] key, byte[] string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#llen(java.lang.String)
	 */
	@Override
	public Long llen(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.llen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpush(byte[], byte[])
	 */
	@Override
	public Long lpush(byte[] key, byte[] string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lrange(java.lang.String, long, long)
	 */
	@Override
	public List<String> lrange(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#llen(byte[])
	 */
	@Override
	public Long llen(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.llen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lrange(byte[], int, int)
	 */
	@Override
	public List<byte[]> lrange(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#ltrim(java.lang.String, long, long)
	 */
	@Override
	public String ltrim(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.ltrim(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#ltrim(byte[], int, int)
	 */
	@Override
	public String ltrim(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.ltrim(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lindex(java.lang.String, long)
	 */
	@Override
	public String lindex(String key, long index) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lindex(key, index);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lindex(byte[], int)
	 */
	@Override
	public byte[] lindex(byte[] key, int index) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lindex(key, index);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lset(java.lang.String, long, java.lang.String)
	 */
	@Override
	public String lset(String key, long index, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lset(key, index, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lset(byte[], int, byte[])
	 */
	@Override
	public String lset(byte[] key, int index, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lset(key, index, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lrem(java.lang.String, long, java.lang.String)
	 */
	@Override
	public Long lrem(String key, long count, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lrem(key, count, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lrem(byte[], int, byte[])
	 */
	@Override
	public Long lrem(byte[] key, int count, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lrem(key, count, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpop(java.lang.String)
	 */
	@Override
	public String lpop(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpop(java.lang.String)
	 */
	@Override
	public String rpop(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpop(byte[])
	 */
	@Override
	public byte[] lpop(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpoplpush(java.lang.String, java.lang.String)
	 */
	@Override
	public String rpoplpush(String srckey, String dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpoplpush(srckey, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpop(byte[])
	 */
	@Override
	public byte[] rpop(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpoplpush(byte[], byte[])
	 */
	@Override
	public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpoplpush(srckey, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sadd(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sadd(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sadd(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#smembers(java.lang.String)
	 */
	@Override
	public Set<String> smembers(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.smembers(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sadd(byte[], byte[])
	 */
	@Override
	public Long sadd(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sadd(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#srem(java.lang.String, java.lang.String)
	 */
	@Override
	public Long srem(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#smembers(byte[])
	 */
	@Override
	public Set<byte[]> smembers(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.smembers(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#spop(java.lang.String)
	 */
	@Override
	public String spop(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.spop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#srem(byte[], byte[])
	 */
	@Override
	public Long srem(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#smove(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Long smove(String srckey, String dstkey, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.smove(srckey, dstkey, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#spop(byte[])
	 */
	@Override
	public byte[] spop(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.spop(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#smove(byte[], byte[], byte[])
	 */
	@Override
	public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.smove(srckey, dstkey, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#scard(java.lang.String)
	 */
	@Override
	public Long scard(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.scard(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sismember(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean sismember(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sismember(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#scard(byte[])
	 */
	@Override
	public Long scard(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.scard(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sinter(java.lang.String)
	 */
	@Override
	public Set<String> sinter(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sinter(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sismember(byte[], byte[])
	 */
	@Override
	public Boolean sismember(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sismember(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sinter(byte)
	 */
	@Override
	public Set<byte[]> sinter(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sinter(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sinterstore(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sinterstore(String dstkey, String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sinterstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sunion(java.lang.String)
	 */
	@Override
	public Set<String> sunion(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sunion(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sinterstore(byte[], byte)
	 */
	@Override
	public Long sinterstore(byte[] dstkey, byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sinterstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sunion(byte)
	 */
	@Override
	public Set<byte[]> sunion(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sunion(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sunionstore(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sunionstore(String dstkey, String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sunionstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sdiff(java.lang.String)
	 */
	@Override
	public Set<String> sdiff(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sdiff(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sunionstore(byte[], byte)
	 */
	@Override
	public Long sunionstore(byte[] dstkey, byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sunionstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sdiff(byte)
	 */
	@Override
	public Set<byte[]> sdiff(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sdiff(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sdiffstore(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sdiffstore(String dstkey, String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sdiffstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#srandmember(java.lang.String)
	 */
	@Override
	public String srandmember(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srandmember(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sdiffstore(byte[], byte)
	 */
	@Override
	public Long sdiffstore(byte[] dstkey, byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sdiffstore(dstkey, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zadd(java.lang.String, double, java.lang.String)
	 */
	@Override
	public Long zadd(String key, double score, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zadd(key, score, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#srandmember(byte[])
	 */
	@Override
	public byte[] srandmember(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srandmember(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zadd(byte[], double, byte[])
	 */
	@Override
	public Long zadd(byte[] key, double score, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zadd(key, score, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrange(java.lang.String, int, int)
	 */
	@Override
	public Set<String> zrange(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrem(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zrem(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrange(byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrange(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zincrby(java.lang.String, double, java.lang.String)
	 */
	@Override
	public Double zincrby(String key, double score, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zincrby(key, score, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrem(byte[], byte[])
	 */
	@Override
	public Long zrem(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zincrby(byte[], double, byte[])
	 */
	@Override
	public Double zincrby(byte[] key, double score, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zincrby(key, score, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrank(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zrank(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrank(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrank(byte[], byte[])
	 */
	@Override
	public Long zrank(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrank(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrank(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zrevrank(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrank(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrank(byte[], byte[])
	 */
	@Override
	public Long zrevrank(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrank(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrange(java.lang.String, int, int)
	 */
	@Override
	public Set<String> zrevrange(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeWithScores(java.lang.String, int, int)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeWithScores(java.lang.String, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zcard(java.lang.String)
	 */
	@Override
	public Long zcard(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcard(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrange(byte[], int, int)
	 */
	@Override
	public Set<byte[]> zrevrange(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeWithScores(byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrangeWithScores(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zscore(java.lang.String, java.lang.String)
	 */
	@Override
	public Double zscore(String key, String member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zscore(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeWithScores(byte[], int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zcard(byte[])
	 */
	@Override
	public Long zcard(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcard(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#watch(java.lang.String)
	 */
	@Override
	public String watch(String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.watch(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(java.lang.String)
	 */
	@Override
	public List<String> sort(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zscore(byte[], byte[])
	 */
	@Override
	public Double zscore(byte[] key, byte[] member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zscore(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#multi()
	 */
	@Override
	public Transaction multi() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.multi();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(java.lang.String, redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, sortingParameters);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#multi(redis.clients.jedis.TransactionBlock)
	 */
	@Override
	public List<Object> multi(TransactionBlock jedisTransaction) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.multi(jedisTransaction);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#connect()
	 */
	@Override
	public void connect() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.connect();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#disconnect()
	 */
	@Override
	public void disconnect() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.disconnect();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#watch(byte)
	 */
	@Override
	public String watch(byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.watch(keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#unwatch()
	 */
	@Override
	public String unwatch() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.unwatch();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(byte[])
	 */
	@Override
	public List<byte[]> sort(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#blpop(int, java.lang.String)
	 */
	@Override
	public List<String> blpop(int timeout, String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.blpop(timeout, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(byte[], redis.clients.jedis.SortingParams)
	 */
	@Override
	public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, sortingParameters);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#blpop(int, byte)
	 */
	@Override
	public List<byte[]> blpop(int timeout, byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.blpop(timeout, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(java.lang.String, redis.clients.jedis.SortingParams, java.lang.String)
	 */
	@Override
	public Long sort(String key, SortingParams sortingParameters, String dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, sortingParameters, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(java.lang.String, java.lang.String)
	 */
	@Override
	public Long sort(String key, String dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#brpop(int, java.lang.String)
	 */
	@Override
	public List<String> brpop(int timeout, String... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.brpop(timeout, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(byte[], redis.clients.jedis.SortingParams, byte[])
	 */
	@Override
	public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, sortingParameters, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sort(byte[], byte[])
	 */
	@Override
	public Long sort(byte[] key, byte[] dstkey) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sort(key, dstkey);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#brpop(int, byte)
	 */
	@Override
	public List<byte[]> brpop(int timeout, byte[]... keys) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.brpop(timeout, keys);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#auth(java.lang.String)
	 */
	@Override
	public String auth(String password) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.auth(password);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#subscribe(redis.clients.jedis.JedisPubSub, java.lang.String)
	 */
	@Override
	public void subscribe(JedisPubSub jedisPubSub, String... channels) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.subscribe(jedisPubSub, channels);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#publish(java.lang.String, java.lang.String)
	 */
	@Override
	public Long publish(String channel, String message) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.publish(channel, message);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#psubscribe(redis.clients.jedis.JedisPubSub, java.lang.String)
	 */
	@Override
	public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.psubscribe(jedisPubSub, patterns);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zcount(java.lang.String, double, double)
	 */
	@Override
	public Long zcount(String key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcount(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#pipelined(redis.clients.jedis.PipelineBlock)
	 */
	@Override
	public List<Object> pipelined(PipelineBlock jedisPipeline) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.pipelined(jedisPipeline);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#pipelined()
	 */
	@Override
	public Pipeline pipelined() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
//		if ( logger.isDebugEnabled() ) {
//			String name = Thread.currentThread().getName();
//			logger.debug("Thread {} borrow jedis {}", name, delegate);
//		}
		SyncPipeline pipeline = new SyncPipeline(delegate.pipelined(), delegate, pool);
		return pipeline;
//		try {
//			return delegate.pipelined();
//		} finally {
//			pool.returnResource(delegate);
//		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zcount(byte[], double, double)
	 */
	@Override
	public Long zcount(byte[] key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcount(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(byte[], double, double)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<String> zrangeByScore(String key, double min, double max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(byte[], byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScore(byte[], double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrangeByScore(byte[] key, double min, double max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScoreWithScores(java.lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScoreWithScores(byte[], double, double)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScoreWithScores(java.lang.String, double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScoreWithScores(java.lang.String, double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
			double min, int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zremrangeByRank(java.lang.String, int, int)
	 */
	@Override
	public Long zremrangeByRank(String key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByRank(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zremrangeByScore(java.lang.String, double, double)
	 */
	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByScore(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(byte[], double, double)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(byte[], byte[], byte[])
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zunionstore(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zunionstore(String dstkey, String... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zunionstore(dstkey, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScore(byte[], double, double, int, int)
	 */
	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScoreWithScores(byte[], double, double)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zrevrangeByScoreWithScores(byte[], double, double, int, int)
	 */
	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max,
			double min, int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zremrangeByRank(byte[], int, int)
	 */
	@Override
	public Long zremrangeByRank(byte[] key, int start, int end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByRank(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zremrangeByScore(byte[], double, double)
	 */
	@Override
	public Long zremrangeByScore(byte[] key, double start, double end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByScore(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zunionstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String)
	 */
	@Override
	public Long zunionstore(String dstkey, ZParams params, String... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zunionstore(dstkey, params, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zunionstore(byte[], byte)
	 */
	@Override
	public Long zunionstore(byte[] dstkey, byte[]... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zunionstore(dstkey, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zinterstore(java.lang.String, java.lang.String)
	 */
	@Override
	public Long zinterstore(String dstkey, String... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zinterstore(dstkey, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zunionstore(byte[], redis.clients.jedis.ZParams, byte)
	 */
	@Override
	public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zunionstore(dstkey, params, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zinterstore(java.lang.String, redis.clients.jedis.ZParams, java.lang.String)
	 */
	@Override
	public Long zinterstore(String dstkey, ZParams params, String... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zinterstore(dstkey, params, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zinterstore(byte[], byte)
	 */
	@Override
	public Long zinterstore(byte[] dstkey, byte[]... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zinterstore(dstkey, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#strlen(java.lang.String)
	 */
	@Override
	public Long strlen(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.strlen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpushx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long lpushx(String key, String string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpushx(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#persist(java.lang.String)
	 */
	@Override
	public Long persist(String key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.persist(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#zinterstore(byte[], redis.clients.jedis.ZParams, byte)
	 */
	@Override
	public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zinterstore(dstkey, params, sets);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpushx(java.lang.String, java.lang.String)
	 */
	@Override
	public Long rpushx(String key, String string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpushx(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#echo(java.lang.String)
	 */
	@Override
	public String echo(String string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.echo(string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#linsert(java.lang.String, redis.clients.jedis.BinaryClient.LIST_POSITION, java.lang.String, java.lang.String)
	 */
	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot,
			String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.linsert(key, where, pivot, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#brpoplpush(java.lang.String, java.lang.String, int)
	 */
	@Override
	public String brpoplpush(String source, String destination, int timeout) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.brpoplpush(source, destination, timeout);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setbit(java.lang.String, long, boolean)
	 */
	@Override
	public Boolean setbit(String key, long offset, boolean value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setbit(key, offset, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getbit(java.lang.String, long)
	 */
	@Override
	public Boolean getbit(String key, long offset) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getbit(key, offset);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setrange(java.lang.String, long, java.lang.String)
	 */
	@Override
	public Long setrange(String key, long offset, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setrange(key, offset, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getrange(java.lang.String, long, long)
	 */
	@Override
	public String getrange(String key, long startOffset, long endOffset) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getrange(key, startOffset, endOffset);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#save()
	 */
	@Override
	public String save() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.save();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#bgsave()
	 */
	@Override
	public String bgsave() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.bgsave();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#bgrewriteaof()
	 */
	@Override
	public String bgrewriteaof() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.bgrewriteaof();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lastsave()
	 */
	@Override
	public Long lastsave() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lastsave();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#shutdown()
	 */
	@Override
	public String shutdown() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.shutdown();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#info()
	 */
	@Override
	public String info() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.info();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#monitor(redis.clients.jedis.JedisMonitor)
	 */
	@Override
	public void monitor(JedisMonitor jedisMonitor) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.monitor(jedisMonitor);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#slaveof(java.lang.String, int)
	 */
	@Override
	public String slaveof(String host, int port) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.slaveof(host, port);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#slaveofNoOne()
	 */
	@Override
	public String slaveofNoOne() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.slaveofNoOne();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#configGet(java.lang.String)
	 */
	@Override
	public List<String> configGet(String pattern) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.configGet(pattern);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#configResetStat()
	 */
	@Override
	public String configResetStat() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.configResetStat();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#configSet(java.lang.String, java.lang.String)
	 */
	@Override
	public String configSet(String parameter, String value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.configSet(parameter, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#isConnected()
	 */
	@Override
	public boolean isConnected() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			try {
				return delegate.isConnected();
			} catch (Exception e) {
			}
		} finally {
			pool.returnResource(delegate);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#strlen(byte[])
	 */
	@Override
	public Long strlen(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.strlen(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#sync()
	 */
	@Override
	public void sync() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.sync();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#lpushx(byte[], byte[])
	 */
	@Override
	public Long lpushx(byte[] key, byte[] string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpushx(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#persist(byte[])
	 */
	@Override
	public Long persist(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.persist(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#rpushx(byte[], byte[])
	 */
	@Override
	public Long rpushx(byte[] key, byte[] string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpushx(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#echo(byte[])
	 */
	@Override
	public byte[] echo(byte[] string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.echo(string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#linsert(byte[], redis.clients.jedis.BinaryClient.LIST_POSITION, byte[], byte[])
	 */
	@Override
	public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot,
			byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.linsert(key, where, pivot, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#debug(redis.clients.jedis.DebugParams)
	 */
	@Override
	public String debug(DebugParams params) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.debug(params);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getClient()
	 */
	@Override
	public Client getClient() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getClient();
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#brpoplpush(byte[], byte[], int)
	 */
	@Override
	public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.brpoplpush(source, destination, timeout);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setbit(byte[], long, byte[])
	 */
	@Override
	public Boolean setbit(byte[] key, long offset, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setbit(key, offset, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getbit(byte[], long)
	 */
	@Override
	public Boolean getbit(byte[] key, long offset) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getbit(key, offset);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#setrange(byte[], long, byte[])
	 */
	@Override
	public long setrange(byte[] key, long offset, byte[] value) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.setrange(key, offset, value);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getrange(byte[], long, long)
	 */
	@Override
	public String getrange(byte[] key, long startOffset, long endOffset) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getrange(key, startOffset, endOffset);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#publish(byte[], byte[])
	 */
	@Override
	public Long publish(byte[] channel, byte[] message) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.publish(channel, message);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#subscribe(redis.clients.jedis.BinaryJedisPubSub, byte)
	 */
	@Override
	public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.subscribe(jedisPubSub, channels);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#psubscribe(redis.clients.jedis.BinaryJedisPubSub, byte)
	 */
	@Override
	public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			delegate.psubscribe(jedisPubSub, patterns);
		} finally {
			pool.returnResource(delegate);
		}
	}

	/* (non-Javadoc)
	 * @see com.xinqihd.sns.gameserver.jedis.JedisAllCommand#getDB()
	 */
	@Override
	public Long getDB() {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.getDB();
		} finally {
			pool.returnResource(delegate);
		}
	}
	
	// --------------------------------------------------- New methods added in Jedis 2.10.1

	@Override
	public Long hdel(String key, String... field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hdel(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long rpush(String key, String... string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long lpush(String key, String... string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long sadd(String key, String... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sadd(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long srem(String key, String... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zadd(String key, Map<Double, String> scoreMembers) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zadd(key, scoreMembers);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zrem(String key, String... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrange(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeWithScores(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zcount(String key, String min, String max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcount(key, min ,max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScore(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
			String min, int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByRank(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByScore(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long hdel(byte[] key, byte[]... field) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.hdel(key, field);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long rpush(byte[] key, byte[]... string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.rpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long lpush(byte[] key, byte[]... string) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.lpush(key, string);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long sadd(byte[] key, byte[]... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.sadd(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long srem(byte[] key, byte[]... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.srem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zadd(byte[] key, Map<Double, byte[]> scoreMembers) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zadd(key, scoreMembers);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zrem(byte[] key, byte[]... member) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrem(key, member);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zcount(byte[] key, byte[] min, byte[] max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zcount(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrangeByScoreWithScores(key, min, max);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min,
			int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScore(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max,
			byte[] min, int offset, int count) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long zremrangeByScore(byte[] key, byte[] start, byte[] end) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.zremrangeByScore(key, start, end);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long objectRefcount(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.objectRefcount(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public Long objectIdletime(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.objectIdletime(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

	@Override
	public byte[] objectEncoding(byte[] key) {
		redis.clients.jedis.Jedis delegate = pool.getResource();
		try {
			return delegate.objectEncoding(key);
		} finally {
			pool.returnResource(delegate);
		}
	}

}
