package com.xinqihd.sns.gameserver.db.mongo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.BSONObject;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.xinqihd.sns.gameserver.config.Constant;
import com.xinqihd.sns.gameserver.entity.user.UserId;

/**
 * My facility class to improve memory and speed.
 * @author wangqi
 *
 */
public class MapDBObject implements DBObject {
	
	private static final Log log = LogFactory.getLog(MapDBObject.class);
	
	private Map<String, Object> delegateMap = null;
	private boolean isPartialObject = false;
	
	public MapDBObject() {
		this.delegateMap = new LinkedHashMap<String, Object>();
	}
	
	public MapDBObject(Map<String, Object> map) {
		this.delegateMap = map;
	}

	@Override
	public final Object put( String key , Object value ) {
		return this.delegateMap.put(key, value);
	}

	@Override
	public final void putAll(BSONObject o) {
		this.delegateMap.putAll(o.toMap());
	}

	@Override
	public final void putAll(Map m) {
		for ( Object key : m.keySet() ) {
			Object value = m.get(key);
			if ( value instanceof Number ) {
				this.delegateMap.put(key.toString(), value);
			} else if ( value instanceof Boolean ) {
				this.delegateMap.put(key.toString(), value);
			} else if ( value instanceof String ) {
				this.delegateMap.put(key.toString(), value);
			} else {
				MapDBObject obj = new MapDBObject();
				obj.putAll(value);
				this.delegateMap.put(key.toString(), obj);
			}
			if ( !(key instanceof UserId) && !(key instanceof String) ) {
				String keyClass = key.getClass().getName();
				this.delegateMap.put(key.toString().concat(Constant.CLASS), keyClass);
			}
		}
	}
	
	/**
	 * It is my extended method for facility of creating complex object map.
	 * @param obj
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final void putAll(Object obj) {
		if ( obj == null ) {
			return;
		}
		if ( obj instanceof Map ) {
			putAll((Map)obj);
			return;
		} else if ( obj instanceof BSONObject) {
			putAll((BSONObject)obj);
			return;
		}
		Class clazz = obj.getClass();
		String className = clazz.getName();
		if ( className.startsWith(MongoDBUtil.CLASS_PREFIX) ) {
			className = className.substring(MongoDBUtil.CLASS_PREFIX_LEN);
		}
		this.delegateMap.put(Constant.CLASS, className);
		Field[] fields = clazz.getDeclaredFields();
		try {
			for ( int i=0; i<fields.length; i++ ) {
				fields[i].setAccessible(true);
				/**
				 * The annotation is 10-times slower than normal reflection speed.
				 * wangqi
				 *  
				 * if ( fields[i].isAnnotationPresent(ExcludeField.class) ) {
				 *   continue;
				 * } 
				 */
				int modifier = fields[i].getModifiers();
				if ( Modifier.isStatic(modifier) || Modifier.isTransient(modifier) ) {
					continue;
				}
 				Object value = fields[i].get(obj);
				if ( value instanceof Enum ) {
					this.delegateMap.put(fields[i].getName(), ((Enum)value).name());
				} else if ( value instanceof UserId ) {
					this.delegateMap.put(fields[i].getName(), ((UserId)value).getInternal());
				} else if ( value instanceof Collection ) {
					Collection list = (Collection)value;
					ArrayList newList = new ArrayList();
					if ( list.size() > 0 ) {
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							Object o = (Object) iterator.next();
							if ( o instanceof Enum ) {
								newList.add(((Enum)o).name());
							} else if ( o instanceof String ) {
								newList.add(String.valueOf(o));
							} else {
								MapDBObject object = new MapDBObject();
								object.putAll(o);
								newList.add(object);							
							}
						}
					}
					this.delegateMap.put(fields[i].getName(), newList);
				} else if ( value instanceof Map ) {
					Map map = (Map)value;
					MapDBObject object = new MapDBObject();
					object.putAll(map);
					this.delegateMap.put(fields[i].getName(), object);
				} else {
					this.delegateMap.put(fields[i].getName(), fields[i].get(obj));
				}
			}
		} catch (Exception e) {
			log.warn("Failed to put Object in Map", e);
		}
	}

	@Override
	public final Object get(String key) {
		return this.delegateMap.get(key);
	}

	@Override
	public final Map toMap() {
		return this.delegateMap;
	}

	@Override
	public final Object removeField(String key) {
		return this.delegateMap.remove(key);
	}

	@Override
	public final boolean containsKey(String key) {
		return containsField(key);
	}

	@Override
	public final boolean containsField(String key) {
		return this.delegateMap.containsKey(key);
	}

	@Override
	public final Set<String> keySet() {
		return this.delegateMap.keySet();
	}

	@Override
	public void markAsPartialObject() {
		this.isPartialObject = true;
	}

	@Override
	public boolean isPartialObject() {
		return isPartialObject;
	}

	@Override
	public String toString() {
		return JSON.serialize(this.delegateMap);
	}
}
